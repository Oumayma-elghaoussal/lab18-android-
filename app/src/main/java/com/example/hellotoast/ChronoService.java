package com.example.hellotoast;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;

/**
 * LAB 16 — Foreground + Bound Service : Chronomètre.
 *
 * Cycle de vie :
 *   startForegroundService() → onCreate() → onStartCommand() → chrono démarre
 *   bindService()            → onBind()   → l'Activity reçoit le Binder
 *   stopService()            → onDestroy() → chrono arrêté, notification supprimée
 *
 * START_STICKY : si le système tue le service, il le relance automatiquement.
 */
public class ChronoService extends Service {

    private static final String TAG = "ChronoService";
    private static final String CHANNEL_ID = "chrono_channel";
    private static final int NOTIFICATION_ID = 1;

    // Binder pour la communication Activity ↔ Service
    private final IBinder binder = new ChronoBinder();

    // Chronomètre
    private Handler handler;
    private Runnable timerRunnable;
    private long startTime = 0;
    private long elapsedSeconds = 0;
    private boolean isRunning = false;

    // Listener pour notifier l'Activity
    private OnTickListener tickListener;

    /** Interface callback : l'Activity l'implémente pour recevoir les ticks. */
    public interface OnTickListener {
        void onTick(long seconds);
    }

    /** Binder exposé à l'Activity via onBind(). */
    public class ChronoBinder extends Binder {
        public ChronoService getService() {
            return ChronoService.this;
        }
    }

    // ════════════════════════════════════════════════
    //  Cycle de vie du Service
    // ════════════════════════════════════════════════

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate() — Service créé");

        handler = new Handler(Looper.getMainLooper());
        createNotificationChannel();
    }

    /**
     * onStartCommand() — appelé à chaque startService() / startForegroundService().
     *
     * START_STICKY : si le système tue le service (manque de RAM),
     * il sera relancé automatiquement avec un Intent null.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand() — startId=" + startId + ", flags=" + flags);

        // Lancer en Foreground immédiatement (obligatoire dans les 5 secondes)
        startForeground(NOTIFICATION_ID, buildNotification("00:00:00"));

        // Démarrer le chrono
        startTimer();

        return START_STICKY;
    }

    /**
     * onBind() — appelé quand l'Activity fait bindService().
     * Retourne le Binder pour permettre la communication directe.
     */
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind() — Activity liée au service");
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "onUnbind() — Activity déliée");
        tickListener = null;
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy() — Service détruit");
        stopTimer();
        super.onDestroy();
    }

    // ════════════════════════════════════════════════
    //  Chronomètre (Handler + Runnable)
    // ════════════════════════════════════════════════

    private void startTimer() {
        if (isRunning) return;

        isRunning = true;
        startTime = System.currentTimeMillis();

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isRunning) return;

                elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
                String formatted = formatTime(elapsedSeconds);

                // Mettre à jour la notification
                NotificationManager nm = getSystemService(NotificationManager.class);
                if (nm != null) {
                    nm.notify(NOTIFICATION_ID, buildNotification(formatted));
                }

                // Notifier l'Activity (si liée)
                if (tickListener != null) {
                    tickListener.onTick(elapsedSeconds);
                }

                // Re-planifier dans 1 seconde
                handler.postDelayed(this, 1000);
            }
        };

        handler.post(timerRunnable);
        Log.i(TAG, "Timer démarré");
    }

    private void stopTimer() {
        isRunning = false;
        if (handler != null && timerRunnable != null) {
            handler.removeCallbacks(timerRunnable);
        }
        elapsedSeconds = 0;
        Log.i(TAG, "Timer arrêté");
    }

    // ════════════════════════════════════════════════
    //  Notification (Foreground Service)
    // ════════════════════════════════════════════════

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notif_channel_name),
                NotificationManager.IMPORTANCE_LOW   // pas de son
        );
        channel.setDescription(getString(R.string.notif_channel_desc));

        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm != null) {
            nm.createNotificationChannel(channel);
        }
        Log.i(TAG, "NotificationChannel créé : " + CHANNEL_ID);
    }

    private Notification buildNotification(String timeText) {
        // PendingIntent pour ouvrir l'Activity au clic sur la notification
        Intent notifIntent = new Intent(this, MainActivity.class);
        notifIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notifIntent, PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.notif_title))
                .setContentText("Temps : " + timeText)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentIntent(pendingIntent)
                .setOngoing(true)           // non-dismissable
                .setOnlyAlertOnce(true)     // pas de son à chaque mise à jour
                .build();
    }

    // ════════════════════════════════════════════════
    //  API publique (utilisée par l'Activity via le Binder)
    // ════════════════════════════════════════════════

    public void setOnTickListener(OnTickListener listener) {
        this.tickListener = listener;
    }

    public long getElapsedSeconds() {
        return elapsedSeconds;
    }

    public boolean isRunning() {
        return isRunning;
    }

    // ════════════════════════════════════════════════
    //  Utilitaire
    // ════════════════════════════════════════════════

    public static String formatTime(long totalSeconds) {
        long h = totalSeconds / 3600;
        long m = (totalSeconds % 3600) / 60;
        long s = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }
}

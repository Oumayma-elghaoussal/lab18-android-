package com.example.hellotoast;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * LAB 16 — MainActivity : contrôle du ChronoService.
 *
 * Utilise :
 *   - startForegroundService() pour lancer le service
 *   - bindService() pour recevoir les ticks en temps réel
 *   - stopService() pour tout arrêter
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQ_NOTIF = 200;

    private TextView tvTime, tvStatus;
    private Button btnStart, btnStop;

    // Référence au service (via Bound Service)
    private ChronoService chronoService;
    private boolean isBound = false;

    // ════════════════════════════════════════════════
    //  ServiceConnection — liaison avec le service
    // ════════════════════════════════════════════════
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "onServiceConnected — lié au service");

            ChronoService.ChronoBinder binder = (ChronoService.ChronoBinder) service;
            chronoService = binder.getService();
            isBound = true;

            // Écouter les ticks pour mettre à jour l'UI
            chronoService.setOnTickListener(seconds -> runOnUiThread(() ->
                    tvTime.setText(ChronoService.formatTime(seconds))
            ));

            // Synchroniser l'affichage si le service tournait déjà
            if (chronoService.isRunning()) {
                tvTime.setText(ChronoService.formatTime(chronoService.getElapsedSeconds()));
                setUiState(true);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.w(TAG, "onServiceDisconnected — service déconnecté");
            isBound = false;
            chronoService = null;
        }
    };

    // ════════════════════════════════════════════════
    //  Cycle de vie Activity
    // ════════════════════════════════════════════════

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTime   = findViewById(R.id.tvTime);
        tvStatus = findViewById(R.id.tvStatus);
        btnStart = findViewById(R.id.btnStart);
        btnStop  = findViewById(R.id.btnStop);

        // ── DÉMARRER SERVICE ──
        btnStart.setOnClickListener(v -> {
            Log.i(TAG, "Bouton DÉMARRER cliqué");

            // Vérifier la permission POST_NOTIFICATIONS (Android 13+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIF);
                    return;
                }
            }

            startChronoService();
        });

        // ── ARRÊTER SERVICE ──
        btnStop.setOnClickListener(v -> {
            Log.i(TAG, "Bouton ARRÊTER cliqué");
            stopChronoService();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Tenter de se lier au service (s'il tourne déjà)
        Intent intent = new Intent(this, ChronoService.class);
        bindService(intent, connection, 0); // 0 = ne pas créer si inexistant
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Délier (le service continue en Foreground)
        if (isBound) {
            if (chronoService != null) {
                chronoService.setOnTickListener(null);
            }
            unbindService(connection);
            isBound = false;
            Log.i(TAG, "onStop — unbindService");
        }
    }

    // ════════════════════════════════════════════════
    //  Démarrer / Arrêter le service
    // ════════════════════════════════════════════════

    private void startChronoService() {
        Intent intent = new Intent(this, ChronoService.class);

        // startForegroundService() obligatoire depuis Android 8.0 (API 26)
        ContextCompat.startForegroundService(this, intent);

        // Se lier pour recevoir les mises à jour en temps réel
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

        setUiState(true);
        Toast.makeText(this, "Service démarré !", Toast.LENGTH_SHORT).show();
    }

    private void stopChronoService() {
        // Délier d'abord
        if (isBound) {
            if (chronoService != null) {
                chronoService.setOnTickListener(null);
            }
            unbindService(connection);
            isBound = false;
        }

        // Arrêter le service
        Intent intent = new Intent(this, ChronoService.class);
        stopService(intent);

        setUiState(false);
        tvTime.setText("00:00:00");
        Toast.makeText(this, "Service arrêté", Toast.LENGTH_SHORT).show();
    }

    // ════════════════════════════════════════════════
    //  UI helpers
    // ════════════════════════════════════════════════

    private void setUiState(boolean running) {
        btnStart.setEnabled(!running);
        btnStop.setEnabled(running);
        tvStatus.setText(running ? "🟢 Service en cours…" : "🔴 Service arrêté");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_NOTIF) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startChronoService();
            } else {
                Toast.makeText(this,
                        "Permission notifications requise pour le Foreground Service",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}

package com.example.hellotoast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * LAB 17 — BroadcastReceiver Demo.
 *
 * 1) Receiver DYNAMIQUE : détecte ACTION_AIRPLANE_MODE_CHANGED
 * 2) Receiver STATIQUE  : BootReceiver (BOOT_COMPLETED, déclaré dans Manifest)
 * 3) Broadcast CUSTOM    : envoie et reçoit un broadcast interne
 * 4) Journal des événements : affiche tous les broadcasts reçus
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // Action custom pour le broadcast interne
    public static final String ACTION_CUSTOM =
            "com.example.hellotoast.ACTION_CUSTOM_BROADCAST";

    private TextView tvAirplaneStatus, tvCustomResult, tvLog;
    private EditText etMessage;

    // StringBuilder pour le journal
    private final StringBuilder logBuilder = new StringBuilder();

    // ── Receiver DYNAMIQUE : Mode Avion ──────────────
    private final BroadcastReceiver airplaneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Lire l'état du mode avion
            boolean isAirplaneOn = Settings.Global.getInt(
                    context.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, 0) != 0;

            String status = isAirplaneOn
                    ? "✈️ Mode Avion : ACTIVÉ"
                    : "📶 Mode Avion : DÉSACTIVÉ";

            tvAirplaneStatus.setText(status);
            appendLog("AIRPLANE_MODE → " + (isAirplaneOn ? "ON" : "OFF"));

            Log.i(TAG, "airplaneReceiver.onReceive() → " + status);
            Toast.makeText(context, status, Toast.LENGTH_SHORT).show();
        }
    };

    // ── Receiver DYNAMIQUE : Broadcast custom ────────
    private final BroadcastReceiver customReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            if (message == null) message = "(vide)";

            tvCustomResult.setText("📨 Reçu : " + message);
            appendLog("CUSTOM_BROADCAST → \"" + message + "\"");

            Log.i(TAG, "customReceiver.onReceive() → " + message);
            Toast.makeText(context, "Broadcast reçu : " + message, Toast.LENGTH_SHORT).show();
        }
    };

    // ════════════════════════════════════════════════
    //  Cycle de vie
    // ════════════════════════════════════════════════

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Binding des vues
        tvAirplaneStatus = findViewById(R.id.tvAirplaneStatus);
        tvCustomResult   = findViewById(R.id.tvCustomResult);
        tvLog            = findViewById(R.id.tvLog);
        etMessage        = findViewById(R.id.etMessage);
        Button btnSend   = findViewById(R.id.btnSendBroadcast);
        Button btnClear  = findViewById(R.id.btnClearLog);

        // ── Enregistrement DYNAMIQUE : Mode Avion ──
        IntentFilter airplaneFilter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(airplaneReceiver, airplaneFilter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(airplaneReceiver, airplaneFilter);
        }
        appendLog("Receiver Mode Avion enregistré (dynamique)");
        Log.i(TAG, "airplaneReceiver enregistré");

        // ── Enregistrement DYNAMIQUE : Broadcast custom ──
        IntentFilter customFilter = new IntentFilter(ACTION_CUSTOM);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(customReceiver, customFilter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(customReceiver, customFilter);
        }
        appendLog("Receiver Custom enregistré (dynamique)");
        Log.i(TAG, "customReceiver enregistré");

        // ── Lire l'état actuel du mode avion ──
        boolean airplaneNow = Settings.Global.getInt(
                getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        tvAirplaneStatus.setText(airplaneNow
                ? "✈️ Mode Avion : ACTIVÉ"
                : "📶 Mode Avion : DÉSACTIVÉ");

        // ── Bouton : Envoyer broadcast custom ──
        btnSend.setOnClickListener(v -> {
            String msg = etMessage.getText().toString().trim();
            if (msg.isEmpty()) {
                Toast.makeText(this, "Entrez un message !", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(ACTION_CUSTOM);
            intent.putExtra("message", msg);

            // setPackage() pour cibler uniquement notre app (bonne pratique sécurité)
            intent.setPackage(getPackageName());

            sendBroadcast(intent);

            appendLog("Broadcast envoyé : \"" + msg + "\"");
            Log.i(TAG, "sendBroadcast() → " + msg);
            etMessage.setText("");
        });

        // ── Bouton : Effacer le journal ──
        btnClear.setOnClickListener(v -> {
            logBuilder.setLength(0);
            tvLog.setText("");
        });

        appendLog("Application démarrée");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Désenregistrer les receivers dynamiques (éviter les fuites mémoire)
        unregisterReceiver(airplaneReceiver);
        unregisterReceiver(customReceiver);

        Log.i(TAG, "Receivers désenregistrés dans onDestroy()");
    }

    // ════════════════════════════════════════════════
    //  Journal des événements
    // ════════════════════════════════════════════════

    private void appendLog(String event) {
        String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        logBuilder.insert(0, "[" + time + "] " + event + "\n");
        tvLog.setText(logBuilder.toString().trim());
    }
}

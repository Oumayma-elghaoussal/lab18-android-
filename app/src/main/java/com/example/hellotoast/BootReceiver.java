package com.example.hellotoast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * LAB 17 — Receiver STATIQUE pour BOOT_COMPLETED.
 *
 * Déclaré dans le Manifest avec un <intent-filter>.
 * Se déclenche au démarrage du téléphone, même si l'app n'est pas ouverte.
 *
 * Restrictions Android récentes :
 *   - Android 8.0+ : la plupart des broadcasts implicites ne fonctionnent plus
 *     en statique, SAUF BOOT_COMPLETED (il fait partie des exceptions).
 *   - Android 14+ : le receiver doit avoir android:exported="true" pour
 *     recevoir des broadcasts système.
 *
 * Cycle de vie :
 *   onReceive() est appelé sur le main thread.
 *   Vous avez ~10 secondes pour finir le travail.
 *   Ne lancez PAS de tâche longue ici — utilisez un Service ou WorkManager.
 */
public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.i(TAG, "═══ BOOT_COMPLETED reçu ! Le téléphone a démarré. ═══");

            // Afficher un Toast (visible brièvement après le boot)
            Toast.makeText(context,
                    "ReceiverDemo : Boot détecté !",
                    Toast.LENGTH_LONG).show();

            // En production : lancer un Service, planifier un WorkManager, etc.
            // Exemple : context.startForegroundService(new Intent(context, MonService.class));
        }
    }
}

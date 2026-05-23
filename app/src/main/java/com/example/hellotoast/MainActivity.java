package com.example.hellotoast;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

/**
 * LAB 15 — SQLite : Gestion simple des étudiants.
 *
 * Interface : Ajouter, Chercher par ID, Supprimer par ID, Afficher tous.
 * Les résultats sont affichés dans le TextView et loggés dans Logcat (tag "MainActivity").
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private EditText etId, etNom, etPrenom;
    private TextView tvResultat;
    private EtudiantService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialiser le service (crée la base si nécessaire)
        service = new EtudiantService(this);

        // Binding des vues
        etId      = findViewById(R.id.etId);
        etNom     = findViewById(R.id.etNom);
        etPrenom  = findViewById(R.id.etPrenom);
        tvResultat = findViewById(R.id.tvResultat);

        Button btnAjouter     = findViewById(R.id.btnAjouter);
        Button btnChercher    = findViewById(R.id.btnChercher);
        Button btnSupprimer   = findViewById(R.id.btnSupprimer);
        Button btnAfficherTous = findViewById(R.id.btnAfficherTous);

        // ── Ajouter ──────────────────────────────────
        btnAjouter.setOnClickListener(v -> {
            String nom = etNom.getText().toString().trim();
            String prenom = etPrenom.getText().toString().trim();

            if (nom.isEmpty() || prenom.isEmpty()) {
                toast("Remplissez Nom et Prénom !");
                return;
            }

            Etudiant e = new Etudiant(nom, prenom);
            long newId = service.create(e);

            if (newId != -1) {
                String msg = "✅ Étudiant ajouté (id=" + newId + ")\n"
                        + "Nom : " + nom + "\nPrénom : " + prenom;
                tvResultat.setText(msg);
                Log.i(TAG, "Ajouté : " + e + " → id=" + newId);
                clearFields();
            } else {
                tvResultat.setText("❌ Erreur lors de l'insertion");
            }
        });

        // ── Chercher par ID ──────────────────────────
        btnChercher.setOnClickListener(v -> {
            String idStr = etId.getText().toString().trim();
            if (idStr.isEmpty()) {
                toast("Entrez un ID !");
                return;
            }

            int id = Integer.parseInt(idStr);
            Etudiant e = service.getById(id);

            if (e != null) {
                String msg = "📖 Étudiant trouvé :\n"
                        + "• ID : " + e.getId() + "\n"
                        + "• Nom : " + e.getNom() + "\n"
                        + "• Prénom : " + e.getPrenom();
                tvResultat.setText(msg);
                Log.i(TAG, "Trouvé : " + e);
            } else {
                tvResultat.setText("⚠️ Aucun étudiant avec l'ID " + id);
                Log.w(TAG, "getById(" + id + ") → introuvable");
            }
        });

        // ── Supprimer par ID ─────────────────────────
        btnSupprimer.setOnClickListener(v -> {
            String idStr = etId.getText().toString().trim();
            if (idStr.isEmpty()) {
                toast("Entrez un ID !");
                return;
            }

            int id = Integer.parseInt(idStr);
            int rows = service.delete(id);

            if (rows > 0) {
                tvResultat.setText("🗑️ Étudiant id=" + id + " supprimé");
                Log.i(TAG, "Supprimé : id=" + id);
                clearFields();
            } else {
                tvResultat.setText("⚠️ Aucun étudiant avec l'ID " + id);
                Log.w(TAG, "delete(" + id + ") → aucune ligne");
            }
        });

        // ── Afficher tous ────────────────────────────
        btnAfficherTous.setOnClickListener(v -> {
            List<Etudiant> list = service.getAll();

            if (list.isEmpty()) {
                tvResultat.setText("📭 Aucun étudiant dans la base");
                Log.i(TAG, "getAll → vide");
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("📋 ").append(list.size()).append(" étudiant(s) :\n\n");

            for (Etudiant e : list) {
                sb.append("• [").append(e.getId()).append("] ")
                  .append(e.getNom()).append(" ")
                  .append(e.getPrenom()).append("\n");
            }

            tvResultat.setText(sb.toString().trim());
            Log.i(TAG, "getAll → " + list.size() + " résultat(s)");
        });

        // ── Test initial via Logcat ──────────────────
        testLogcat();
    }

    /**
     * Test automatique au démarrage : insère 2 étudiants, lit, supprime.
     * Visible dans Logcat avec le tag "MainActivity".
     */
    private void testLogcat() {
        Log.i(TAG, "═══ TEST LOGCAT DÉBUT ═══");

        // Insertion
        long id1 = service.create(new Etudiant("Alami", "Youssef"));
        long id2 = service.create(new Etudiant("Benali", "Fatima"));
        Log.i(TAG, "Insérés : id1=" + id1 + ", id2=" + id2);

        // Lecture
        Etudiant e1 = service.getById((int) id1);
        Log.i(TAG, "Lu : " + e1);

        // Liste
        List<Etudiant> all = service.getAll();
        Log.i(TAG, "Tous : " + all);

        // Suppression
        service.delete((int) id2);
        Log.i(TAG, "Après suppression de id2 :");
        List<Etudiant> afterDelete = service.getAll();
        for (Etudiant e : afterDelete) {
            Log.i(TAG, "  → " + e);
        }

        Log.i(TAG, "═══ TEST LOGCAT FIN ═══");
    }

    private void clearFields() {
        etId.setText("");
        etNom.setText("");
        etPrenom.setText("");
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}

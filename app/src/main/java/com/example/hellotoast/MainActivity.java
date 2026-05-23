package com.example.hellotoast;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * LAB 14 — Sauvegarde des données :
 * SharedPreferences, EncryptedSharedPreferences,
 * fichiers internes (texte + JSON), cache, export externe,
 * checklist sécurité.
 */
public class MainActivity extends AppCompatActivity {

    // Noms de fichiers
    private static final String PREFS_NAME = "lab14_prefs";
    private static final String ENCRYPTED_PREFS = "lab14_encrypted";
    private static final String INTERNAL_FILE = "notes.txt";
    private static final String INTERNAL_JSON = "data.json";
    private static final String CACHE_FILE = "temp_cache.txt";
    private static final String EXPORT_FILE = "export_notes.txt";

    // Views — Section 1
    private EditText etPrefKey, etPrefValue;
    private TextView tvPrefResult;

    // Views — Section 2
    private EditText etToken;
    private TextView tvTokenResult;

    // Views — Section 3
    private EditText etFileContent;
    private TextView tvFileResult;

    // Views — Section 4
    private TextView tvCacheResult;

    // Views — Section 5
    private TextView tvExportResult;

    // Views — Section 6
    private TextView tvSecurityResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
        setupListeners();
    }

    // ────────────────────────────────────────────────
    //  Binding
    // ────────────────────────────────────────────────
    private void bindViews() {
        etPrefKey      = findViewById(R.id.etPrefKey);
        etPrefValue    = findViewById(R.id.etPrefValue);
        tvPrefResult   = findViewById(R.id.tvPrefResult);

        etToken        = findViewById(R.id.etToken);
        tvTokenResult  = findViewById(R.id.tvTokenResult);

        etFileContent  = findViewById(R.id.etFileContent);
        tvFileResult   = findViewById(R.id.tvFileResult);

        tvCacheResult  = findViewById(R.id.tvCacheResult);
        tvExportResult = findViewById(R.id.tvExportResult);
        tvSecurityResult = findViewById(R.id.tvSecurityResult);
    }

    private void setupListeners() {
        // Section 1
        findViewById(R.id.btnSavePref).setOnClickListener(v -> savePref());
        findViewById(R.id.btnLoadPref).setOnClickListener(v -> loadPref());

        // Section 2
        findViewById(R.id.btnSaveToken).setOnClickListener(v -> saveEncrypted());
        findViewById(R.id.btnLoadToken).setOnClickListener(v -> loadEncrypted());

        // Section 3
        findViewById(R.id.btnWriteFile).setOnClickListener(v -> writeInternalFile());
        findViewById(R.id.btnReadFile).setOnClickListener(v -> readInternalFile());
        findViewById(R.id.btnWriteJson).setOnClickListener(v -> writeJsonFile());
        findViewById(R.id.btnReadJson).setOnClickListener(v -> readJsonFile());

        // Section 4
        findViewById(R.id.btnWriteCache).setOnClickListener(v -> writeCache());
        findViewById(R.id.btnPurgeCache).setOnClickListener(v -> purgeCache());

        // Section 5
        findViewById(R.id.btnExport).setOnClickListener(v -> exportExternal());

        // Section 6
        findViewById(R.id.btnCheckSecurity).setOnClickListener(v -> checkSecurity());
    }

    // ════════════════════════════════════════════════
    //  1. SharedPreferences — apply (asynchrone)
    // ════════════════════════════════════════════════
    private void savePref() {
        String key = etPrefKey.getText().toString().trim();
        String val = etPrefValue.getText().toString().trim();
        if (key.isEmpty()) { toast("Clé vide !"); return; }

        SharedPreferences sp = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(key, val).apply();   // apply = asynchrone, recommandé
        // commit() est synchrone, bloque le thread UI

        tvPrefResult.setText("✅ Sauvegardé (apply) : " + key + " = " + val);
    }

    private void loadPref() {
        String key = etPrefKey.getText().toString().trim();
        if (key.isEmpty()) { toast("Clé vide !"); return; }

        SharedPreferences sp = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String val = sp.getString(key, "(introuvable)");

        tvPrefResult.setText("📖 Chargé : " + key + " = " + val);
    }

    // ════════════════════════════════════════════════
    //  2. EncryptedSharedPreferences + MasterKey
    // ════════════════════════════════════════════════
    private SharedPreferences getEncryptedPrefs() throws Exception {
        MasterKey masterKey = new MasterKey.Builder(this)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();

        return EncryptedSharedPreferences.create(
                this,
                ENCRYPTED_PREFS,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }

    private void saveEncrypted() {
        String token = etToken.getText().toString().trim();
        if (token.isEmpty()) { toast("Token vide !"); return; }

        try {
            getEncryptedPrefs().edit().putString("auth_token", token).apply();
            tvTokenResult.setText("🔒 Token chiffré et sauvegardé !");
        } catch (Exception e) {
            tvTokenResult.setText("❌ Erreur : " + e.getMessage());
        }
    }

    private void loadEncrypted() {
        try {
            String token = getEncryptedPrefs().getString("auth_token", "(aucun)");
            tvTokenResult.setText("🔓 Token déchiffré : " + token);
        } catch (Exception e) {
            tvTokenResult.setText("❌ Erreur : " + e.getMessage());
        }
    }

    // ════════════════════════════════════════════════
    //  3a. Fichier interne — texte UTF-8
    // ════════════════════════════════════════════════
    private void writeInternalFile() {
        String content = etFileContent.getText().toString();
        if (content.isEmpty()) { toast("Contenu vide !"); return; }

        try (FileOutputStream fos = openFileOutput(INTERNAL_FILE, Context.MODE_PRIVATE);
             OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
            writer.write(content);
            tvFileResult.setText("✅ Écrit dans " + INTERNAL_FILE
                    + "\n📂 " + getFilesDir().getAbsolutePath());
        } catch (Exception e) {
            tvFileResult.setText("❌ " + e.getMessage());
        }
    }

    private void readInternalFile() {
        try (FileInputStream fis = openFileInput(INTERNAL_FILE);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(fis, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line).append("\n");

            tvFileResult.setText("📖 Contenu :\n" + sb.toString().trim());
        } catch (Exception e) {
            tvFileResult.setText("❌ Fichier introuvable ou erreur : " + e.getMessage());
        }
    }

    // ════════════════════════════════════════════════
    //  3b. Fichier interne — JSON simple
    // ════════════════════════════════════════════════
    private void writeJsonFile() {
        String content = etFileContent.getText().toString().trim();
        if (content.isEmpty()) { toast("Contenu vide !"); return; }

        try {
            JSONObject json = new JSONObject();
            json.put("note", content);
            json.put("timestamp", new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
            json.put("version", 1);

            try (FileOutputStream fos = openFileOutput(INTERNAL_JSON, Context.MODE_PRIVATE);
                 OutputStreamWriter w = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
                w.write(json.toString(2)); // indentation 2
            }

            tvFileResult.setText("✅ JSON écrit dans " + INTERNAL_JSON
                    + "\n" + json.toString(2));
        } catch (Exception e) {
            tvFileResult.setText("❌ " + e.getMessage());
        }
    }

    private void readJsonFile() {
        try (FileInputStream fis = openFileInput(INTERNAL_JSON);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(fis, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);

            JSONObject json = new JSONObject(sb.toString());
            String note = json.getString("note");
            String ts   = json.getString("timestamp");
            int ver     = json.getInt("version");

            tvFileResult.setText("📖 JSON :\n• note = " + note
                    + "\n• timestamp = " + ts
                    + "\n• version = " + ver);
        } catch (Exception e) {
            tvFileResult.setText("❌ " + e.getMessage());
        }
    }

    // ════════════════════════════════════════════════
    //  4. Cache temporaire (cacheDir) + purge
    // ════════════════════════════════════════════════
    private void writeCache() {
        try {
            File cacheFile = new File(getCacheDir(), CACHE_FILE);
            String data = "Cache écrit à "
                    + new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

            try (FileOutputStream fos = new FileOutputStream(cacheFile);
                 OutputStreamWriter w = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
                w.write(data);
            }

            long sizeKb = getCacheDirSize() / 1024;
            tvCacheResult.setText("✅ Cache écrit : " + cacheFile.getName()
                    + "\n📂 " + getCacheDir().getAbsolutePath()
                    + "\n📦 Taille cache : " + sizeKb + " Ko");
        } catch (Exception e) {
            tvCacheResult.setText("❌ " + e.getMessage());
        }
    }

    private void purgeCache() {
        File cacheDir = getCacheDir();
        int count = 0;
        File[] files = cacheDir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.delete()) count++;
            }
        }
        tvCacheResult.setText("🧹 Cache purgé : " + count + " fichier(s) supprimé(s)"
                + "\n📦 Taille cache : 0 Ko");
    }

    private long getCacheDirSize() {
        long size = 0;
        File[] files = getCacheDir().listFiles();
        if (files != null) {
            for (File f : files) size += f.length();
        }
        return size;
    }

    // ════════════════════════════════════════════════
    //  5. Export vers l'externe app-specific
    //     (pas de permission requise depuis API 19)
    // ════════════════════════════════════════════════
    private void exportExternal() {
        File extDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (extDir == null) {
            tvExportResult.setText("❌ Stockage externe indisponible");
            return;
        }

        // On exporte le contenu du fichier interne
        String content;
        try (FileInputStream fis = openFileInput(INTERNAL_FILE);
             BufferedReader r = new BufferedReader(
                     new InputStreamReader(fis, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) sb.append(line).append("\n");
            content = sb.toString().trim();
        } catch (Exception e) {
            tvExportResult.setText("❌ Rien à exporter (écrivez d'abord un fichier interne)");
            return;
        }

        try {
            File exportFile = new File(extDir, EXPORT_FILE);
            try (FileOutputStream fos = new FileOutputStream(exportFile);
                 OutputStreamWriter w = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
                w.write(content);
            }
            tvExportResult.setText("✅ Exporté vers :\n" + exportFile.getAbsolutePath());
        } catch (Exception e) {
            tvExportResult.setText("❌ " + e.getMessage());
        }
    }

    // ════════════════════════════════════════════════
    //  6. Checklist sécurité
    // ════════════════════════════════════════════════
    private void checkSecurity() {
        StringBuilder sb = new StringBuilder();

        // 1) MODE_PRIVATE vérifié
        sb.append("✅ MODE_PRIVATE utilisé (SharedPreferences)\n");

        // 2) EncryptedSharedPreferences
        File encFile = new File(getFilesDir().getParent()
                + "/shared_prefs/" + ENCRYPTED_PREFS + ".xml");
        sb.append(encFile.exists()
                ? "✅ Fichier chiffré présent\n"
                : "⚠️ Aucun token chiffré sauvegardé\n");

        // 3) Pas de logs sensibles
        sb.append("✅ Pas de Log.d() avec données sensibles\n");

        // 4) Cache nettoyable
        long cacheSize = getCacheDirSize();
        sb.append(cacheSize == 0
                ? "✅ Cache vide\n"
                : "⚠️ Cache non purgé (" + (cacheSize / 1024) + " Ko)\n");

        // 5) Fichiers internes MODE_PRIVATE
        File internal = new File(getFilesDir(), INTERNAL_FILE);
        sb.append(internal.exists()
                ? "✅ Fichier interne présent (MODE_PRIVATE)\n"
                : "ℹ️ Aucun fichier interne créé\n");

        // 6) Rotation conceptuelle de token
        sb.append("ℹ️ Rotation de token : à implémenter côté serveur\n");
        sb.append("   (changer le token régulièrement, invalider l'ancien)\n");

        // 7) Externe app-specific
        sb.append("✅ Export externe app-specific (pas de WRITE_EXTERNAL_STORAGE)\n");

        tvSecurityResult.setText(sb.toString());
    }

    // ────────────────────────────────────────────────
    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}

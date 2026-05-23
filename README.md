# LAB 14 — Sauvegarde des données : SharedPreferences et fichiers

## Objectifs d'apprentissage

| # | Objectif |
|---|----------|
| 1 | Écrire et lire des préférences via **SharedPreferences** (`apply` vs `commit`) |
| 2 | Stocker un secret (token) chiffré via **EncryptedSharedPreferences** + **MasterKey** |
| 3 | Écrire et lire des **fichiers internes** (texte UTF-8, JSON simple) |
| 4 | Utiliser un **cache temporaire** (`cacheDir`) et le purger |
| 5 | **Exporter** un fichier vers l'**externe app-specific** et comprendre les permissions |
| 6 | Appliquer une **checklist sécurité** (logs, MODE_PRIVATE, nettoyage, rotation de token) |

---

## Architecture du projet

```
app/src/main/
├── AndroidManifest.xml
├── java/com/example/hellotoast/
│   └── MainActivity.java          ← Logique complète (6 sections)
└── res/
    ├── layout/activity_main.xml   ← UI ScrollView avec 6 cartes Material
    └── values/
        ├── strings.xml
        ├── colors.xml
        └── themes.xml
```

---

## Dépendances

```groovy
dependencies {
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    // Chiffrement
    implementation 'androidx.security:security-crypto:1.1.0-alpha06'
}
```

---

## Section 1 — SharedPreferences (`apply` vs `commit`)

**Concept :**
- `SharedPreferences` = fichier XML clé-valeur dans `/data/data/<package>/shared_prefs/`
- `apply()` = écriture **asynchrone** (recommandé, ne bloque pas le thread UI)
- `commit()` = écriture **synchrone** (retourne `boolean`, bloque le thread)

**Code clé :**
```java
SharedPreferences sp = getSharedPreferences("lab14_prefs", Context.MODE_PRIVATE);
sp.edit().putString(key, val).apply();  // asynchrone
String val = sp.getString(key, "(introuvable)");
```

> ⚠️ **Toujours utiliser `MODE_PRIVATE`** — les autres modes (`MODE_WORLD_READABLE`, `MODE_WORLD_WRITABLE`) sont dépréciés depuis API 17.

---

## Section 2 — EncryptedSharedPreferences + MasterKey

**Concept :**
- Les SharedPreferences classiques sont stockées en **clair** (XML lisible)
- `EncryptedSharedPreferences` chiffre les **clés** (AES256-SIV) et les **valeurs** (AES256-GCM)
- `MasterKey` gère la clé maîtresse via **Android Keystore** (hardware-backed)

**Code clé :**
```java
MasterKey masterKey = new MasterKey.Builder(this)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build();

SharedPreferences encPrefs = EncryptedSharedPreferences.create(
        this,
        "lab14_encrypted",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
);
encPrefs.edit().putString("auth_token", token).apply();
```

> 🔐 Le fichier XML résultant est **illisible** même avec accès root.

---

## Section 3 — Fichiers internes (texte + JSON)

### 3a. Fichier texte UTF-8

**Concept :**
- `openFileOutput()` écrit dans le répertoire interne privé de l'app
- Toujours spécifier `StandardCharsets.UTF_8` pour la portabilité
- `MODE_PRIVATE` = le fichier n'est accessible que par l'app

```java
try (FileOutputStream fos = openFileOutput("notes.txt", Context.MODE_PRIVATE);
     OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
    writer.write(content);
}
```

### 3b. Fichier JSON

**Concept :**
- Créer un `JSONObject`, le sérialiser avec `toString(2)` (indentation)
- Pour lire : reconstruire l'objet depuis la chaîne lue

```java
JSONObject json = new JSONObject();
json.put("note", content);
json.put("timestamp", "2024-01-15 14:30:00");
json.put("version", 1);
// Écrire json.toString(2) dans un fichier
```

---

## Section 4 — Cache temporaire

**Concept :**
- `getCacheDir()` retourne un répertoire de cache interne
- Le système **peut supprimer** ces fichiers quand l'espace est faible
- **Bonne pratique** : purger le cache manuellement quand il n'est plus utile

```java
// Écriture
File cacheFile = new File(getCacheDir(), "temp_cache.txt");
// Purge
for (File f : getCacheDir().listFiles()) f.delete();
```

---

## Section 5 — Export externe (app-specific)

**Concept :**
- `getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)` = dossier **app-specific** sur le stockage externe
- **Pas de permission** requise depuis API 19 (KitKat)
- Les fichiers sont **supprimés** si l'app est désinstallée
- Chemin typique : `/storage/emulated/0/Android/data/<package>/files/Documents/`

```java
File extDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
File exportFile = new File(extDir, "export_notes.txt");
```

---

## Section 6 — Checklist sécurité

| # | Point de contrôle | Statut |
|---|---|---|
| 1 | `MODE_PRIVATE` pour SharedPreferences | ✅ |
| 2 | Secrets chiffrés via EncryptedSharedPreferences | ✅ |
| 3 | Pas de `Log.d()` avec données sensibles en production | ✅ |
| 4 | Cache nettoyé après utilisation | ✅ |
| 5 | Fichiers internes en `MODE_PRIVATE` | ✅ |
| 6 | Rotation de token (côté serveur) | ℹ️ Conceptuel |
| 7 | Export app-specific (pas de `WRITE_EXTERNAL_STORAGE`) | ✅ |

---

## Comparatif `apply()` vs `commit()`

| | `apply()` | `commit()` |
|---|---|---|
| **Mode** | Asynchrone | Synchrone |
| **Retour** | `void` | `boolean` |
| **Bloque le thread** | ❌ Non | ✅ Oui |
| **Recommandé** | ✅ Oui | Uniquement si le résultat est critique |

---

## Permissions requises

**Aucune permission spéciale n'est requise** pour ce lab :
- Stockage interne = toujours accessible
- SharedPreferences = toujours accessible
- Stockage externe app-specific = pas de permission depuis API 19

---

## Comment exécuter

1. Ouvrir le projet dans Android Studio
2. Sync Gradle (la dépendance `security-crypto` sera téléchargée)
3. Run sur émulateur ou appareil (API 26+)
4. Tester chaque section dans l'ordre

---

## Concepts clés abordés

- `SharedPreferences` et `apply()` vs `commit()`
- Chiffrement avec `EncryptedSharedPreferences` et `MasterKey` (AES256)
- I/O fichier interne avec `openFileOutput()` / `openFileInput()`
- Sérialisation/désérialisation JSON avec `JSONObject`
- Gestion du cache avec `getCacheDir()`
- Stockage externe app-specific avec `getExternalFilesDir()`
- Bonnes pratiques de sécurité Android

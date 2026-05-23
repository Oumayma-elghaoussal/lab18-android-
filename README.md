# LAB 15 — SQLite et Android : Gestion simple des étudiants

## Objectifs d'apprentissage

| # | Objectif |
|---|----------|
| 1 | Créer un **modèle métier** `Etudiant` |
| 2 | Initialiser une base SQLite locale avec **SQLiteOpenHelper** |
| 3 | Implémenter des opérations **CRUD** via un service dédié |
| 4 | Tester les services via **Logcat** |
| 5 | Développer une **interface simple** (Ajouter, Chercher, Supprimer) |

---

## Architecture du projet

```
app/src/main/java/com/example/hellotoast/
├── Etudiant.java          ← Modèle métier
├── MySQLiteHelper.java    ← SQLiteOpenHelper (création de la BDD)
├── EtudiantService.java   ← Service CRUD (insert, select, update, delete)
└── MainActivity.java      ← Interface + tests Logcat

app/src/main/res/
├── layout/activity_main.xml   ← UI (formulaire + résultats)
└── values/
    ├── strings.xml
    ├── colors.xml
    └── themes.xml
```

---

## Classe 1 — `Etudiant.java` (Modèle)

Représente un étudiant dans la base.

| Champ    | Type     | Description |
|----------|----------|-------------|
| `id`     | `int`    | Clé primaire (AUTO_INCREMENT) |
| `nom`    | `String` | Nom de famille |
| `prenom` | `String` | Prénom |

Deux constructeurs :
- `Etudiant(int id, String nom, String prenom)` — lecture depuis la BDD
- `Etudiant(String nom, String prenom)` — insertion (id généré automatiquement)

---

## Classe 2 — `MySQLiteHelper.java` (SQLiteOpenHelper)

| Élément | Valeur |
|---------|--------|
| **Nom de la base** | `etudiants.db` |
| **Version** | `1` |
| **Table** | `etudiant` |

**Script de création :**
```sql
CREATE TABLE etudiant (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nom TEXT NOT NULL,
    prenom TEXT NOT NULL
);
```

**Méthodes clés :**
- `onCreate(SQLiteDatabase db)` — exécute le `CREATE TABLE`
- `onUpgrade(...)` — `DROP TABLE` + `onCreate` (pour les montées de version)

---

## Classe 3 — `EtudiantService.java` (CRUD)

| Méthode | SQL | Description |
|---------|-----|-------------|
| `create(Etudiant)` | `INSERT` | Insère un étudiant, retourne l'id généré |
| `getById(int id)` | `SELECT ... WHERE id = ?` | Retourne un `Etudiant` ou `null` |
| `getAll()` | `SELECT *` | Retourne la liste complète |
| `update(Etudiant)` | `UPDATE ... WHERE id = ?` | Met à jour nom/prénom |
| `delete(int id)` | `DELETE ... WHERE id = ?` | Supprime par id |

**Bonnes pratiques appliquées :**
- Utilisation de `ContentValues` pour l'insertion et la mise à jour
- Requêtes paramétrées (`?`) pour éviter l'injection SQL
- `db.close()` après chaque opération
- `Log.i()` / `Log.w()` pour tracer chaque opération dans Logcat
- Méthode utilitaire `cursorToEtudiant(Cursor)` pour la conversion

---

## Classe 4 — `MainActivity.java` (Interface)

### Interface utilisateur

L'écran contient :
- **3 champs** : ID, Nom, Prénom
- **4 boutons** :
  - **Ajouter** — insère un étudiant (nom + prénom requis)
  - **Chercher par ID** — affiche l'étudiant correspondant
  - **Supprimer par ID** — supprime l'étudiant correspondant
  - **Afficher tous** — liste tous les étudiants
- **Zone résultat** — affiche le résultat de chaque opération

### Test automatique Logcat

Au démarrage, `testLogcat()` exécute automatiquement :
1. Insertion de 2 étudiants test ("Alami Youssef", "Benali Fatima")
2. Lecture par ID
3. Liste de tous
4. Suppression d'un étudiant
5. Re-liste pour vérifier

> 💡 Ouvrez **Logcat** dans Android Studio, filtrez par tag `MainActivity` ou `EtudiantService` pour voir les traces.

---

## Concepts SQLite clés

### SQLiteOpenHelper
- Gère la **création** et les **mises à jour** de la base
- `getWritableDatabase()` — ouvre en écriture
- `getReadableDatabase()` — ouvre en lecture

### ContentValues
- Dictionnaire clé-valeur utilisé pour `INSERT` et `UPDATE`
- Plus sûr que la concaténation SQL

### Cursor
- Résultat d'une requête `SELECT`
- Parcours avec `moveToFirst()` / `moveToNext()`
- Accès aux colonnes via `getColumnIndexOrThrow()`

---

## Dépendances

**Aucune dépendance externe** — SQLite est intégré dans Android.

```groovy
dependencies {
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
}
```

---

## Comment exécuter

1. Ouvrir le projet dans **Android Studio**
2. **Sync Gradle**
3. **Run** sur émulateur ou appareil (API 26+)
4. Observer le **Logcat** (tag `MainActivity`) pour le test automatique
5. Utiliser l'interface pour ajouter, chercher et supprimer des étudiants

---

## Vérification Logcat

Filtrez Logcat avec `MainActivity` ou `EtudiantService` :

```
I/MainActivity: ═══ TEST LOGCAT DÉBUT ═══
I/EtudiantService: create → id=1 | Etudiant{id=0, nom='Alami', prenom='Youssef'}
I/EtudiantService: create → id=2 | Etudiant{id=0, nom='Benali', prenom='Fatima'}
I/EtudiantService: getById(1) → Etudiant{id=1, nom='Alami', prenom='Youssef'}
I/EtudiantService: getAll → 2 étudiant(s)
I/EtudiantService: delete(2) → 1 ligne(s)
I/MainActivity: ═══ TEST LOGCAT FIN ═══
```

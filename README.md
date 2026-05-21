# LAB 9 — Consommer un Web Service PHP 8 depuis Android avec Volley

## 📋 Objectifs

- Créer une base de données MySQL pour gérer les étudiants
- Développer un Web Service PHP capable d'ajouter et de renvoyer les données en JSON
- Construire une application Android qui consomme ce service via **Volley**
- Utiliser **Gson** pour parser la réponse JSON et afficher les résultats

---

## 🏗️ Architecture du projet

```
lab1-android/
├── app/                          # Application Android
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/hellotoast/
│       │   ├── AddEtudiant.java          # Activité : formulaire d'ajout
│       │   ├── ListEtudiantActivity.java # Activité : liste des étudiants
│       │   └── beans/
│       │       └── Etudiant.java         # Classe modèle
│       └── res/
│           ├── layout/
│           │   ├── activity_add_etudiant.xml
│           │   ├── activity_list_etudiant.xml
│           │   └── item_etudiant.xml
│           ├── values/
│           │   ├── strings.xml
│           │   ├── colors.xml
│           │   └── themes.xml
│           └── xml/
│               └── network_security_config.xml
├── php/                          # Backend PHP (à copier dans XAMPP)
│   ├── classes/Etudiant.php
│   ├── connexion/Connexion.php
│   ├── dao/IDao.php
│   ├── service/EtudiantService.php
│   ├── ws/
│   │   ├── createEtudiant.php    # POST : ajouter un étudiant
│   │   └── loadEtudiant.php      # GET  : charger tous les étudiants
│   └── sql/
│       └── setup.sql             # Script de création de la BDD
└── README.md
```

---

## 🚀 Mise en place

### Partie 1 — Base de données MySQL

1. Démarrer **XAMPP** (Apache + MySQL)
2. Ouvrir [http://localhost/phpmyadmin](http://localhost/phpmyadmin)
3. Exécuter le script SQL situé dans `php/sql/setup.sql` :

```sql
CREATE DATABASE IF NOT EXISTS school1;
USE school1;

CREATE TABLE IF NOT EXISTS Etudiant (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nom VARCHAR(50),
  prenom VARCHAR(50),
  ville VARCHAR(50),
  sexe VARCHAR(10)
);

INSERT INTO Etudiant (nom, prenom, ville, sexe)
VALUES ('Lachgar', 'Mohamed', 'Rabat', 'homme'),
       ('Safi', 'Amine', 'Marrakech', 'homme');
```

### Partie 2 — Déploiement du Web Service PHP

1. Copier le dossier `php/` dans `C:\xampp\htdocs\projet` :
   ```
   C:\xampp\htdocs\projet\
   ├── classes/
   ├── connexion/
   ├── dao/
   ├── service/
   └── ws/
   ```

2. Tester les endpoints :
   - **GET** : `http://localhost/projet/ws/loadEtudiant.php`
   - **POST** : `http://localhost/projet/ws/createEtudiant.php` (avec les paramètres `nom`, `prenom`, `ville`, `sexe`)

### Partie 3 — Application Android

1. Ouvrir le projet dans **Android Studio**
2. Synchroniser Gradle (`Sync Now`)
3. Lancer l'application sur l'émulateur Android

> **Note** : L'émulateur Android accède à `localhost` de la machine hôte via l'adresse `10.0.2.2`. C'est pourquoi les URLs dans le code Java utilisent `http://10.0.2.2/projet/ws/...`.

---

## 📱 Fonctionnalités de l'application

| Écran | Description |
|-------|-------------|
| **AddEtudiant** | Formulaire avec Nom, Prénom, Ville (Spinner), Sexe (RadioButton) et bouton ADD. Envoie une requête POST via Volley. |
| **ListEtudiantActivity** | Affiche la liste complète des étudiants depuis le serveur via une requête GET. Clic sur un élément = popup avec détails. |

---

## 🔧 Technologies utilisées

| Composant | Technologie |
|-----------|-------------|
| Backend | PHP 8 + PDO + MySQL |
| Android HTTP | Volley 1.2.1 |
| JSON Parsing | Gson 2.10.1 |
| UI | Material Components |
| Architecture | MVC (PHP) + Activity-based (Android) |

---

## ⚠️ Configuration réseau (Android 9+)

Le fichier `res/xml/network_security_config.xml` autorise le trafic HTTP clair vers `10.0.2.2` (localhost de l'émulateur). C'est indispensable car Android 9+ bloque les connexions HTTP non sécurisées par défaut.

---

## 🧪 Tester avec Postman / Advanced REST Client

### Ajouter un étudiant (POST)
- **URL** : `http://localhost/projet/ws/createEtudiant.php`
- **Method** : POST
- **Body** (x-www-form-urlencoded) :
  - `nom` = Dupont
  - `prenom` = Sara
  - `ville` = Casablanca
  - `sexe` = femme

### Charger les étudiants (GET)
- **URL** : `http://localhost/projet/ws/loadEtudiant.php`
- **Method** : GET

### Réponse attendue
```json
[
  {"id": 1, "nom": "Lachgar", "prenom": "Mohamed", "ville": "Rabat", "sexe": "homme"},
  {"id": 2, "nom": "Safi", "prenom": "Amine", "ville": "Marrakech", "sexe": "homme"}
]
```

---

## 📝 Logcat attendu

```
D/RESPONSE: [{"id":"1","nom":"LACHGAR","prenom":"Mohamed","ville":"Rabat","sexe":"homme"}, ...]
D/ETUDIANT: Etudiant{id=1, nom='LACHGAR', prenom='Mohamed', ville='Rabat', sexe='homme'}
```

# TP 11 — Localisation d'un smartphone et envoi des coordonnées vers un serveur distant

## 📋 Objectifs pédagogiques

- Récupérer la **latitude** et la **longitude** d'un smartphone via GPS
- Comprendre le rôle des **permissions Android** liées à la localisation
- Envoyer les données d'une application Android vers un **service PHP**
- Enregistrer les coordonnées dans une base **MySQL**
- Structurer un mini projet mobile connecté à un backend

---

## 🏗️ Architecture du projet

```
lab1-android/
├── app/                              # Application Android
│   └── src/main/
│       ├── AndroidManifest.xml       # Permissions : INTERNET, FINE_LOCATION, COARSE_LOCATION
│       ├── java/com/example/hellotoast/
│       │   └── MainActivity.java     # Localisation GPS + envoi Volley POST
│       └── res/
│           ├── layout/
│           │   └── activity_main.xml # Interface avec cards Material
│           ├── xml/
│           │   └── network_security_config.xml
│           └── values/
│               ├── strings.xml
│               ├── colors.xml
│               └── themes.xml
├── php/                              # Backend PHP (à copier dans XAMPP)
│   ├── connexion/Connexion.php       # Connexion PDO
│   ├── ws/
│   │   ├── savePosition.php          # POST : enregistrer une position
│   │   └── getPositions.php          # GET  : lister les positions
│   └── sql/
│       └── setup.sql                 # Script de création de la BDD
└── README.md
```

---

## 🚀 Mise en place

### Partie 1 — Base de données MySQL

1. Démarrer **XAMPP** (Apache + MySQL)
2. Ouvrir [http://localhost/phpmyadmin](http://localhost/phpmyadmin)
3. Exécuter le script `php/sql/setup.sql` :

```sql
CREATE DATABASE IF NOT EXISTS localisation_db;
USE localisation_db;

CREATE TABLE IF NOT EXISTS position (
    id INT AUTO_INCREMENT PRIMARY KEY,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    date_heure DATETIME NOT NULL,
    device_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Partie 2 — Déploiement du Web Service PHP

Copier le dossier `php/` dans `C:\xampp\htdocs\localisation` :

```
C:\xampp\htdocs\localisation\
├── connexion/Connexion.php
├── ws/
│   ├── savePosition.php
│   └── getPositions.php
└── sql/setup.sql
```

**Tester avec Postman :**
- **POST** `http://localhost/localisation/ws/savePosition.php`
  - Body (x-www-form-urlencoded) : `latitude=33.97`, `longitude=-6.85`, `date=2025-05-21 14:30:00`, `deviceId=test123`
- **GET** `http://localhost/localisation/ws/getPositions.php`

### Partie 3 — Application Android

1. Ouvrir le projet dans **Android Studio**
2. **Sync Gradle** (Sync Now)
3. Lancer sur émulateur ou appareil physique (API 26+)

> **Note** : L'émulateur utilise `10.0.2.2` pour accéder au `localhost` de la machine hôte.

---

## 📱 Fonctionnement de l'application

| Étape | Action |
|-------|--------|
| 1 | Cliquer sur **📍 Obtenir la position** |
| 2 | Accepter la permission de localisation |
| 3 | L'app affiche : latitude, longitude, altitude, précision, date, ID appareil |
| 4 | Cliquer sur **☁️ Envoyer au serveur** |
| 5 | Les données sont envoyées en POST au serveur PHP |
| 6 | Le serveur insère dans MySQL et retourne une confirmation JSON |

---

## 🔧 Technologies utilisées

| Composant | Technologie |
|-----------|-------------|
| Localisation | FusedLocationProviderClient (Google Play Services) |
| HTTP Client | Volley 1.2.1 |
| Backend | PHP 8 + PDO + MySQL |
| UI | Material Components (Cards, Buttons) |
| Permissions | Runtime permissions (Android 6+) |

---

## 🔒 Permissions Android

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

La permission de localisation est demandée à l'exécution (runtime permission). Le fichier `network_security_config.xml` autorise le trafic HTTP clair vers `10.0.2.2`.

---

## 🧪 Résultat attendu

L'application doit :
- ✅ Détecter la position géographique du smartphone
- ✅ Afficher latitude, longitude, altitude, précision
- ✅ Envoyer les données (lat, lng, date, device ID) au serveur PHP
- ✅ Insérer les coordonnées dans la table `position` de MySQL
- ✅ Afficher la confirmation du serveur dans l'interface

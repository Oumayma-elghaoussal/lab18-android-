# LAB 16 — Maîtriser les Services dans une Application Android

## Objectifs du lab

| # | Objectif |
|---|----------|
| 1 | Créer un **Foreground Service** (obligatoire depuis Android 8.0) |
| 2 | Afficher une **notification persistante** avec le temps en direct |
| 3 | Utiliser un **Bound Service** pour communiquer avec l'Activity |
| 4 | **Démarrage / arrêt** depuis l'interface |
| 5 | Comprendre le **cycle de vie** des Services, `onStartCommand`, `onBind`, `START_STICKY` |

---

## Architecture du projet

```
app/src/main/java/com/example/hellotoast/
├── ChronoService.java     ← Foreground + Bound Service (chronomètre)
└── MainActivity.java      ← Interface de contrôle

app/src/main/res/
├── layout/activity_main.xml   ← UI (timer + boutons start/stop)
└── values/
    ├── strings.xml
    ├── colors.xml
    └── themes.xml
```

---

## Concepts clés expliqués

### 1. Foreground Service (obligatoire depuis Android 8.0)

Depuis **Android 8.0 (API 26)**, un service qui tourne en arrière-plan **doit** afficher une notification. Sinon, le système le tue après quelques secondes.

```java
// Lancer un Foreground Service
ContextCompat.startForegroundService(this, intent);

// Dans le Service (dans les 5 premières secondes !)
startForeground(NOTIFICATION_ID, notification);
```

### 2. `onStartCommand()` et `START_STICKY`

```java
@Override
public int onStartCommand(Intent intent, int flags, int startId) {
    startForeground(NOTIFICATION_ID, buildNotification("00:00:00"));
    startTimer();
    return START_STICKY;  // Le système relance le service s'il est tué
}
```

| Valeur de retour | Comportement |
|---|---|
| `START_STICKY` | Relancé automatiquement (intent = null) |
| `START_NOT_STICKY` | Pas relancé |
| `START_REDELIVER_INTENT` | Relancé avec le dernier Intent |

### 3. Bound Service (communication Activity ↔ Service)

Le pattern **Binder** permet à l'Activity d'appeler directement les méthodes du Service :

```java
// Dans le Service
public class ChronoBinder extends Binder {
    public ChronoService getService() {
        return ChronoService.this;
    }
}

@Override
public IBinder onBind(Intent intent) {
    return binder;
}

// Dans l'Activity
bindService(intent, connection, Context.BIND_AUTO_CREATE);
// → connection.onServiceConnected() reçoit le Binder
```

### 4. Notification Channel (obligatoire depuis Android 8.0)

```java
NotificationChannel channel = new NotificationChannel(
    CHANNEL_ID,
    "Chronomètre",
    NotificationManager.IMPORTANCE_LOW  // pas de son
);
notificationManager.createNotificationChannel(channel);
```

### 5. Permission POST_NOTIFICATIONS (Android 13+)

Depuis **Android 13 (API 33)**, il faut demander la permission à l'exécution :

```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

---

## Cycle de vie du Service

```
startForegroundService()
    ↓
onCreate()  ← créé une seule fois
    ↓
onStartCommand()  ← à chaque startService()
    ↓                 retourne START_STICKY
[Service tourne en Foreground avec notification]
    ↓
bindService() → onBind() → Activity reçoit le Binder
    ↓
[Communication directe Activity ↔ Service]
    ↓
unbindService() → onUnbind()
    ↓
stopService() → onDestroy()
```

---

## Fichiers détaillés

### `ChronoService.java`

| Méthode | Rôle |
|---|---|
| `onCreate()` | Crée le Handler et le NotificationChannel |
| `onStartCommand()` | Lance `startForeground()` + démarre le timer |
| `onBind()` | Retourne le `ChronoBinder` à l'Activity |
| `onUnbind()` | Supprime le listener |
| `onDestroy()` | Arrête le timer |
| `startTimer()` | Planifie un Runnable toutes les secondes |
| `stopTimer()` | Annule le Runnable |
| `buildNotification()` | Construit la notification avec le temps actuel |
| `setOnTickListener()` | API pour l'Activity (callback chaque seconde) |

### `MainActivity.java`

| Méthode | Rôle |
|---|---|
| `startChronoService()` | `startForegroundService()` + `bindService()` |
| `stopChronoService()` | `unbindService()` + `stopService()` |
| `onStart()` | Tente de se lier au service (s'il tourne déjà) |
| `onStop()` | Se délie (le service continue en Foreground) |
| `ServiceConnection` | Reçoit le Binder, configure le tick listener |

---

## Permissions

```xml
<!-- Foreground Service (API 28+) -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />

<!-- Notifications (API 33+) -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

---

## Comment tester

1. **Lancez** l'app sur émulateur ou appareil (API 26+)
2. Cliquez **DÉMARRER SERVICE** → la notification apparaît, le chrono tourne
3. **Quittez l'app** complètement (bouton retour ou swipe) → le service **continue**
4. Ouvrez le **tiroir de notifications** → le temps continue à défiler
5. Cliquez sur la notification → retour dans l'app, le chrono est synchronisé
6. Cliquez **ARRÊTER SERVICE** → tout s'arrête proprement

### Vérification Logcat

Filtrez par tag `ChronoService` ou `MainActivity` :

```
I/ChronoService: onCreate() — Service créé
I/ChronoService: NotificationChannel créé : chrono_channel
I/ChronoService: onStartCommand() — startId=1, flags=0
I/ChronoService: Timer démarré
I/ChronoService: onBind() — Activity liée au service
I/MainActivity: onServiceConnected — lié au service
...
I/MainActivity: Bouton ARRÊTER cliqué
I/ChronoService: onUnbind() — Activity déliée
I/ChronoService: onDestroy() — Service détruit
I/ChronoService: Timer arrêté
```

---

## Dépendances

**Aucune dépendance externe** — les Services et Notifications sont natifs Android.

```groovy
dependencies {
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
}
```

---

## Bonnes pratiques appliquées

| Pratique | Détail |
|---|---|
| ✅ `startForeground()` immédiat | Dans les 5 premières secondes (sinon crash) |
| ✅ `START_STICKY` | Relancé si tué par le système |
| ✅ Notification `IMPORTANCE_LOW` | Pas de son à chaque mise à jour |
| ✅ `setOnlyAlertOnce(true)` | Évite le bip répété |
| ✅ `PendingIntent.FLAG_IMMUTABLE` | Requis depuis API 31 |
| ✅ Unbind dans `onStop()` | Évite les fuites de mémoire |
| ✅ Permission runtime (API 33) | `POST_NOTIFICATIONS` demandée au clic |

# LAB 17 — Maîtriser les BroadcastReceiver en Android


Objectifs du lab
Apprendre à créer et utiliser les BroadcastReceiver (composants qui réagissent aux événements diffusés par le système ou par votre app).
Vous allez réaliser une application ReceiverDemo qui :
Utilise un Receiver dynamique pour détecter le changement de mode avion (ACTION_AIRPLANE_MODE_CHANGED)
Utilise un Receiver statique pour BOOT_COMPLETED (démarrage du téléphone)
Envoie et reçoit un Broadcast custom depuis l’Activity
Comprend la différence Statique vs Dynamique, le cycle de vie (onReceive), les permissions et les restrictions Android 14/15/16 (exported, background limits, etc.)
À la fin vous saurez exactement comment fonctionnent les notifications système (batterie, WiFi, SMS, etc.) et les broadcasts internes.

Créer le projet : 

<img width="1652" height="870" alt="image" src="https://github.com/user-attachments/assets/89ec1c56-4506-4975-9aa0-b79a785fe245" />


<img width="1635" height="875" alt="image" src="https://github.com/user-attachments/assets/90a48dca-3f6e-461c-b8b1-2fcbd2160f8e" />


<img width="1247" height="1020" alt="image" src="https://github.com/user-attachments/assets/d062b178-b474-43b5-b732-42ab4711392d" />


interface 

<img width="639" height="1023" alt="image" src="https://github.com/user-attachments/assets/45793671-2bb0-478f-b96f-21cfad1bf0a4" />


<img width="643" height="1024" alt="image" src="https://github.com/user-attachments/assets/60249b5e-9c98-455b-9642-3db74d6cd7bd" />



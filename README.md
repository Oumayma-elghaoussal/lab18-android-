# LAB 10 — Démo Navigation Drawer et Fragments

## 📋 Objectifs

- Mettre en place un **Navigation Drawer** avec `DrawerLayout` et `NavigationView`
- Utiliser des **Fragments** pour afficher le contenu dynamique de chaque section
- Intégrer une **Toolbar** personnalisée avec un hamburger menu (`ActionBarDrawerToggle`)
- Ajouter un **FloatingActionButton** (FAB) avec une icône email
- Gérer la navigation entre fragments via le menu du drawer

---

## 🏗️ Architecture du projet

```
app/src/main/
├── AndroidManifest.xml
├── java/com/example/hellotoast/
│   ├── MainActivity.java          # Activité principale avec DrawerLayout
│   ├── HomeFragment.java          # Fragment accueil (liste numérotée)
│   ├── GalleryFragment.java       # Fragment galerie
│   ├── SlideshowFragment.java     # Fragment diaporama
│   └── ToolsFragment.java         # Fragment outils
└── res/
    ├── layout/
    │   ├── activity_main.xml       # DrawerLayout + Toolbar + FAB
    │   ├── nav_header.xml          # En-tête du drawer
    │   ├── fragment_home.xml       # RecyclerView
    │   ├── fragment_gallery.xml
    │   ├── fragment_slideshow.xml
    │   ├── fragment_tools.xml
    │   └── item_list.xml           # Item de la liste (numéro + texte)
    ├── menu/
    │   ├── drawer_menu.xml         # Menu du Navigation Drawer
    │   └── main_menu.xml           # Menu overflow de la Toolbar
    └── values/
        ├── strings.xml
        ├── colors.xml
        └── themes.xml
```

---

## 📱 Fonctionnalités

| Composant | Description |
|-----------|-------------|
| **DrawerLayout** | Conteneur principal avec navigation latérale |
| **NavigationView** | Menu latéral avec en-tête et items (Home, Gallery, Slideshow, Tools, Share, Send) |
| **Toolbar** | Barre d'actions personnalisée avec icône hamburger et menu overflow |
| **FAB** | Bouton flottant rose avec icône email en bas à droite |
| **HomeFragment** | Affiche une liste numérotée (Item 1 à Item 20) via RecyclerView |
| **GalleryFragment** | Fragment placeholder "Gallery" |
| **SlideshowFragment** | Fragment placeholder "Slideshow" |
| **ToolsFragment** | Fragment placeholder "Tools" |

---

## 🔧 Technologies utilisées

| Composant | Technologie |
|-----------|-------------|
| Navigation | DrawerLayout + NavigationView |
| UI | Material Components, Toolbar, FAB |
| Liste | RecyclerView avec DividerItemDecoration |
| Fragments | AndroidX Fragment |
| Thème | NoActionBar (Toolbar personnalisée) |

---

## 🚀 Comment exécuter

1. Ouvrir le projet dans **Android Studio**
2. **Sync Gradle** (Sync Now)
3. Lancer sur un émulateur ou appareil physique (API 26+)
4. L'application s'ouvre avec le **HomeFragment** (liste numérotée)
5. Utiliser l'icône **☰** (hamburger) ou glisser depuis la gauche pour ouvrir le drawer
6. Sélectionner un item du menu pour changer de fragment

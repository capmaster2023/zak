# MEPC Montréal — App Android Native

App Android native (Kotlin + Material Design 3) pour [mepcmontreal.com](https://mepcmontreal.com).

## Structure

```
mepc-app/
├── android/
│   ├── app/
│   │   └── src/main/
│   │       ├── java/com/mepc/montreal/
│   │       │   └── MainActivity.kt       ← App entière en Kotlin
│   │       ├── res/
│   │       │   ├── layout/activity_main.xml
│   │       │   ├── menu/bottom_nav_menu.xml
│   │       │   ├── drawable/             ← Icônes vectorielles
│   │       │   ├── values/               ← Couleurs, thèmes, strings
│   │       │   └── xml/network_security_config.xml
│   │       └── AndroidManifest.xml
│   ├── build.gradle
│   ├── settings.gradle
│   └── gradle.properties
├── .github/workflows/build.yml           ← CI/CD GitHub Actions
├── setup-termux.sh                       ← Script de démarrage
└── .gitignore
```

## Démarrage (Termux)

```bash
# 1. Clone le repo sur ton téléphone
git clone https://github.com/TON_USER/mepc-app
cd mepc-app

# 2. Lance le script de setup (UNE SEULE FOIS)
chmod +x setup-termux.sh
./setup-termux.sh
```

Le script va :
1. Installer `git`, `openjdk-17`, `gh`
2. Générer ton **keystore** de signature
3. Afficher les 4 secrets à copier dans GitHub
4. Pousser le code sur GitHub → le build démarre automatiquement

## Secrets GitHub requis

| Secret | Description |
|--------|-------------|
| `KEYSTORE_BASE64` | Keystore encodé en base64 |
| `KEYSTORE_PASSWORD` | Mot de passe du keystore |
| `KEY_ALIAS` | Alias de la clé |
| `KEY_PASSWORD` | Mot de passe de la clé |

**Repo → Settings → Secrets and variables → Actions**

## Télécharger l'APK

Après chaque push sur `main` :
- **GitHub Actions** : Repo → Actions → Build & Sign APK → Artifacts
- **GitHub Releases** : Repo → Releases (APK signée prête à installer)

## Fonctionnalités natives

- ✅ Bottom Navigation native (Accueil, Sermons, Événements, Contact)
- ✅ Toolbar Material Design 3 avec titre dynamique
- ✅ Barre de progression de chargement
- ✅ Injection CSS qui cache le header/footer du site
- ✅ Gestion du bouton Retour Android
- ✅ Deep links (`mepcmontreal.com` → ouvre l'app)
- ✅ URLs externes → navigateur système
- ✅ Edge-to-edge (status bar transparente)
- ✅ APK signée et minifiée (ProGuard)

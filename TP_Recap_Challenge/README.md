# Challenge SecureNotesLab — Attaque, correction, vérification

**Module :** Sécurité des Smartphones
**Étudiante :** Chaymae Noughou

## Présentation

Version « Challenge » de **SecureNotesLab** fournie par l'enseignant : du code
volontairement vulnérable, **sans indication** du nombre ni de l'emplacement des
failles. Le travail : les trouver, les démontrer avec **AttackLab** et **ADB**,
les corriger, puis rejouer les attaques pour vérifier que la correction marche.

Méthode : **Observer → Attaquer → Comprendre → Corriger → Retester.**

## Les deux applications

- **SecureNotesLab** (`com.lab.securenotes`) : appli victime (notes + profil), vulnérable.
- **AttackLab** (`com.lab.attacklab`) : appli tierce de test, un bouton par faille.

Les deux s'installent sur le **même émulateur**. Données fictives, périmètre limité au laboratoire.

## Failles trouvées (C1 à C7)

| # | Faille | Où | Preuve (AttackLab / ADB) | Correction |
|---|---|---|---|---|
| C1 | Path traversal | `NoteDetailActivity` exportée, extra `cheminFichier` non vérifié | `cheminFichier = "../../databases/notes.db"` → la base s'affiche (note de bob) | Refuser `/` et `..`, vérifier que le chemin reste dans le dossier `notes` |
| C2 | Provider : accès cassé | `NotesProvider.query()` renvoie toute la table | Lecture de toutes les notes (alice + bob) | Filtrer par utilisateur authentifié |
| C3 | Permission trop faible | `READ_NOTES` en `protectionLevel="normal"` | AttackLab déclare la permission et l'obtient automatiquement | `protectionLevel="signature"` |
| C4 | Activity sensible exportée | `ProfilActivity` montre le token + « administrateur » sans connexion | Lancement direct → token `demo_token_C7F2` visible | `exported="false"`, vérifier la session, ne pas afficher le token |
| C5 | Secret dans les logs | `SyncManager` journalise la clé API | Clé visible via `adb logcat` | Ne jamais logger de secret |
| C6 | Secrets codés en dur | `API_KEY` et `token` en clair dans le code | Trouvés dans le `.dex` de l'APK (`SNL-SYNC-98213`, `demo_token_C7F2`) | Ne pas livrer de secret dans l'APK |
| C7 | Sauvegarde autorisée | `android:allowBackup="true"` | Données incluses dans une sauvegarde | `android:allowBackup="false"` |

## Comment tester

1. Lancer **SecureNotesLab** sur l'émulateur.
2. Lancer **AttackLab** sur le **même** émulateur.
3. Dans AttackLab, cliquer sur C1 → C6 et lire le résultat. Pour C5, utiliser `adb logcat`.

```bash
adb shell pm list packages | findstr "securenotes attacklab"
```

## Ce qu'on retient

La sandbox Android tient (AttackLab ne lit pas directement les fichiers de SecureNotesLab).
Ce sont les **portes laissées ouvertes par le développeur** (composants exportés, données
non filtrées, secrets livrés dans l'appli) qui causent les failles. Une permission
technique n'est pas une autorisation métier.

## Contenu du dépôt

- `SecureNotesLab/` : le projet Android vulnérable (version Challenge).
- `AttackLab/` : le projet Android d'attaque (boutons C1 à C6).
- `Rapport_Challenge_SecureNotesLab.pdf` : le rapport détaillé (preuves + corrections).

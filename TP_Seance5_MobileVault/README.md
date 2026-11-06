# TP Séance 5 — MobileVault (stockage sécurisé + pentest)

**Module :** Sécurité des Smartphones
**Étudiante :** Chaymae Noughou

## But du TP

Sécuriser le stockage du jeton de session d'une application Android, puis
l'auditer (pentest guidé). L'application de départ avait des défauts volontaires :
secret embarqué, token et note en clair, session = simple booléen, Base64 présenté
comme une protection, log sensible, sauvegarde permissive.

## Le projet

- **MobileVaultLab** (`edu.lab.mobilevault`) : Kotlin, hors ligne, minSdk 26, SDK 35, JDK 17.

## TP1 — Stockage sécurisé du jeton (AES-GCM + Android Keystore)

Fichier clé : `SecureTokenStore.kt`

- Clé **AES-256 dans l'Android Keystore** (non exportable), mode **GCM**.
- Chiffrement **AES/GCM/NoPadding** : IV de 12 octets + tag d'authentification 128 bits.
- Stockage : `version | IV | chiffré+tag`, encodé en Base64 dans `SharedPreferences` (Base64 = encodage, pas protection).
- En cas d'altération ou de clé absente → l'enregistrement est effacé et `null` est renvoyé.
- Le token n'est **jamais affiché** ni mis dans les logs.

Tests dans l'appli (boutons) :

| Bouton | Test | Résultat attendu |
|---|---|---|
| 1 · 2 · 5 | Connexion, restauration, déconnexion | Session restaurée après redémarrage, effacée à la déconnexion |
| 3 | Altérer le chiffré | Déchiffrement refusé |
| 6 (P3) | Test d'intégrité (inverser 1 bit) | Intact : OK · Modifié : `AEADBadTagException` |
| 7 (P4) | Perte de clé (suppression de l'alias Keystore) | `load()` renvoie `null`, pas de clé régénérée en douce |

## TP2 — Pentest guidé et corrections

| Constat | Correction |
|---|---|
| Sauvegarde permissive | `allowBackup="false"` + `data_extraction_rules.xml` |
| Token / note en clair | Stockage chiffré (`SecureTokenStore`) |
| Log sensible | Les logs n'indiquent que des événements, jamais le token |
| Trafic en clair | `usesCleartextTraffic="false"` |
| Session = booléen côté client | Validation côté serveur simulée par `FakeServer.kt` |

**P5 — Scénarios serveur** (bouton 8, `FakeServer`) :

| Scénario | Code | Réaction |
|---|---|---|
| Alice lit son compte | 200 | Données affichées |
| Jeton expiré | 401 | Session effacée, reconnexion |
| Alice lit le compte de Bob | 403 | Accès refusé, session conservée |
| Même requête sans contrôle serveur | 200 | Montre l'IDOR si le serveur ne vérifie pas le propriétaire |

Conclusion : le client protège le stockage local, mais **seul le serveur** décide de
la validité d'une session et des droits d'accès.

## Lancer

1. Ouvrir `MobileVaultLab` dans Android Studio (SDK 35, JDK 17), exécuter `app` en mode **debug**.
2. Suivre les boutons 1 → 8 ; observer les logs : `adb logcat -s MobileVaultLab`.
3. Vérifier le stockage chiffré :
```bash
adb shell run-as edu.lab.mobilevault cat shared_prefs/secure_session.xml
```

## Contenu du dépôt

- `MobileVaultLab/` : le projet Android.
- `TP1_Rapport_chaymae_MobileVault.pdf` : rapport TP1 (stockage sécurisé).
- `TP2_Rapport_chaymae_MobileVault.pdf` : rapport TP2 (pentest).

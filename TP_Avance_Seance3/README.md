# TP Séance 3 (avancé) — Audit de FinTrustVault

**Module :** Sécurité des Smartphones
**Étudiante :** Chaymae Noughou

## But du TP

Jouer le rôle d'un auditeur : on a une application Android avec des failles,
et on développe une 2ᵉ application pour prouver chaque faille, puis on explique
comment la corriger.

Les deux applis sont installées sur le **même émulateur**.

## Les deux projets

- **FinTrustVault** (`com.fintrust.vault`) : l'application cible, avec 4 failles.
- **AuditProbe** (`com.audit.probe`) : l'application d'attaque, un bouton par faille.

## Les 4 failles

| # | Où | Problème |
|---|---|---|
| V1 | `SettingsActivity` | Écran exporté → on l'ouvre sans être connecté, il montre le jeton |
| V2 | `NotesProvider` | Provider exporté → on lit toutes les notes (injection dans la clause SQL) |
| V3 | `WipeReceiver` | Receiver exporté → on efface les notes sans aucune permission |
| V4 | Partage d'une note | Le jeton de session part dans l'Intent → une autre appli peut l'intercepter |

## Comment tester

1. Lancer **FinTrustVault** sur l'émulateur, se connecter (n'importe quel identifiant/mot de passe).
2. Lancer **AuditProbe** sur le **même** émulateur.
3. Dans AuditProbe, cliquer sur les boutons :
   - **V1** → l'écran des paramètres de FinTrustVault s'ouvre et montre le jeton.
   - **V2** → les notes de FinTrustVault s'affichent.
   - **V3** → les notes sont effacées (vérifier en rouvrant FinTrustVault).
   - **V4** → dans FinTrustVault, faire « Partager » une note et choisir AuditProbe : le jeton apparaît.

## Les corrections

| Faille | Correction |
|---|---|
| V1 | Mettre `SettingsActivity` en `exported="false"` et vérifier la connexion |
| V2 | Mettre le provider en `exported="false"` (ou permission `signature`) et filtrer la requête |
| V3 | Protéger le receiver par une permission `signature`, ou l'enregistrer en interne |
| V4 | Ne pas mettre le jeton dans le texte partagé |

## Remarque

Applications de laboratoire, données fictives, tout se passe sur mon émulateur.
Aucune vraie application n'est visée.

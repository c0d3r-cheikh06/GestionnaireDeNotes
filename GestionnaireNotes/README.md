# Gestionnaire de Notes

Application Android (Java) de gestion de notes personnelles, avec persistance locale via Room.

## Architecture

Package racine : `com.tpmobile.gestionnairenotes`

- `modele/Note.java` : entité Room représentant une note (table "notes")
- `donnees/NoteDao.java` : interface DAO avec les requêtes Insert/Update/Delete/Query
- `donnees/GestionnaireBaseDeDonnees.java` : base de données Room en singleton thread-safe
- `adaptateur/AdaptateurNotes.java` : affichage de la liste des notes (RecyclerView)
- `utilitaire/PaletteCouleurs.java` : palette de couleurs centralisée
- `ActivitePrincipale.java` : écran liste des notes, seule classe qui communique avec Room
- `ActiviteFormulaireNote.java` : écran de création/modification, renvoie le résultat via setResult()
- `ApplicationNotes.java` : initialisation du mode sombre au démarrage

## Fonctionnement de la persistance

Toutes les opérations Room sont exécutées dans un thread secondaire via `ExecutorService`,
puis l'interface est mise à jour sur le thread principal avec `runOnUiThread()`.
`ActiviteFormulaireNote` ne contient aucune référence à la base de données : elle renvoie
la note créée, modifiée ou à supprimer à `ActivitePrincipale` via un `ActivityResultLauncher`,
qui est la seule classe responsable des accès à Room.

## Comment ouvrir le projet

1. Ouvrir Android Studio
2. `File > Open`, puis sélectionner le dossier `GestionnaireNotes`
3. Laisser Gradle synchroniser (Room génère du code via annotationProcessor, la première synchronisation peut prendre un peu plus de temps)
4. Lancer l'application sur un émulateur ou un téléphone

## Comment déposer le projet sur GitHub

```
git init
git add .
git commit -m "Gestionnaire de Notes avec persistance Room"
git branch -M main
git remote add origin LIEN_DE_TON_DEPOT_GITHUB
git push -u origin main
```

package com.tpmobile.gestionnairenotes;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.tpmobile.gestionnairenotes.modele.Note;
import com.tpmobile.gestionnairenotes.utilitaire.PaletteCouleurs;

import java.util.Calendar;

public class ActiviteFormulaireNote extends AppCompatActivity {

    private ImageView boutonRetour;
    private ImageView boutonChangerCouleur;
    private ImageView boutonPartagerNote;
    private ImageView boutonSupprimerNote;
    private View fondColoreFormulaire;
    private EditText editTexteTitre;
    private EditText editTexteContenu;
    private TextView boutonEnregistrer;

    private boolean modeModification;
    private Note noteAModifier;
    private String couleurActuelle;

    @Override
    protected void onCreate(Bundle etatSauvegarde) {
        super.onCreate(etatSauvegarde);
        setContentView(R.layout.ecran_formulaire_note);

        lierVues();
        chargerNoteDepuisIntention();
        appliquerCouleurAuFond();
        configurerEcouteurs();
    }

    private void lierVues() {
        boutonRetour = findViewById(R.id.boutonRetour);
        boutonChangerCouleur = findViewById(R.id.boutonChangerCouleur);
        boutonPartagerNote = findViewById(R.id.boutonPartagerNote);
        boutonSupprimerNote = findViewById(R.id.boutonSupprimerNote);
        fondColoreFormulaire = findViewById(R.id.fondColoreFormulaire);
        editTexteTitre = findViewById(R.id.editTexteTitre);
        editTexteContenu = findViewById(R.id.editTexteContenu);
        boutonEnregistrer = findViewById(R.id.boutonEnregistrer);
    }

    private void chargerNoteDepuisIntention() {
        String mode = getIntent().getStringExtra(ActivitePrincipale.EXTRA_MODE);
        modeModification = ActivitePrincipale.MODE_MODIFICATION.equals(mode);

        if (modeModification) {
            noteAModifier = (Note) getIntent().getSerializableExtra(ActivitePrincipale.EXTRA_NOTE);

            if (noteAModifier != null) {
                editTexteTitre.setText(noteAModifier.getTitre());
                editTexteContenu.setText(noteAModifier.getContenu());
                couleurActuelle = noteAModifier.getCouleur();
            }

            boutonEnregistrer.setText("Modifier");
            boutonPartagerNote.setVisibility(View.VISIBLE);
            boutonSupprimerNote.setVisibility(View.VISIBLE);
        } else {
            couleurActuelle = getIntent().getStringExtra(ActivitePrincipale.EXTRA_COULEUR_SELECTIONNEE);
            boutonEnregistrer.setText("Créer");
        }
    }

    private void configurerEcouteurs() {
        boutonRetour.setOnClickListener(v -> finish());
        boutonChangerCouleur.setOnClickListener(v -> afficherSelecteurCouleur());
        boutonEnregistrer.setOnClickListener(v -> enregistrerNote());

        if (modeModification) {
            boutonSupprimerNote.setOnClickListener(v -> confirmerSuppressionNote());
            boutonPartagerNote.setOnClickListener(v -> partagerNote());
        }
    }

    private void enregistrerNote() {
        String titre = editTexteTitre.getText().toString().trim();
        String contenu = editTexteContenu.getText().toString().trim();

        if (titre.isEmpty() && contenu.isEmpty()) {
            Toast.makeText(this, "Une note vide ne peut pas être enregistrée", Toast.LENGTH_SHORT).show();
            return;
        }

        Note note;

        if (modeModification && noteAModifier != null) {
            note = new Note(
                    noteAModifier.getIdentifiant(),
                    titre,
                    contenu,
                    couleurActuelle,
                    noteAModifier.estFavori(),
                    obtenirDateFormateeActuelle()
            );
        } else {
            note = new Note(
                    0,
                    titre,
                    contenu,
                    couleurActuelle,
                    false,
                    obtenirDateFormateeActuelle()
            );
        }

        Intent intentionResultat = new Intent();
        intentionResultat.putExtra(ActivitePrincipale.EXTRA_MODE,
                modeModification ? ActivitePrincipale.MODE_MODIFICATION : ActivitePrincipale.MODE_CREATION);
        intentionResultat.putExtra(ActivitePrincipale.EXTRA_NOTE, note);

        setResult(RESULT_OK, intentionResultat);
        finish();
    }

    private void confirmerSuppressionNote() {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer la note")
                .setMessage("Voulez-vous vraiment supprimer cette note ?")
                .setPositiveButton("Supprimer", (dialogue, lequel) -> supprimerNote())
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void supprimerNote() {
        Intent intentionResultat = new Intent();
        intentionResultat.putExtra(ActivitePrincipale.EXTRA_MODE, ActivitePrincipale.MODE_SUPPRESSION);
        intentionResultat.putExtra(ActivitePrincipale.EXTRA_NOTE, noteAModifier);

        setResult(RESULT_OK, intentionResultat);
        finish();
    }

    private void partagerNote() {
        String titre = editTexteTitre.getText().toString().trim();
        String contenu = editTexteContenu.getText().toString().trim();

        Intent intentionPartage = new Intent(Intent.ACTION_SEND);
        intentionPartage.setType("text/plain");
        intentionPartage.putExtra(Intent.EXTRA_SUBJECT, titre);
        intentionPartage.putExtra(Intent.EXTRA_TEXT, titre + "\n\n" + contenu);
        startActivity(Intent.createChooser(intentionPartage, "Partager la note"));
    }

    private void afficherSelecteurCouleur() {
        View vueDialogue = LayoutInflater.from(this).inflate(R.layout.dialogue_choix_couleur, null);
        View[] cercles = new View[]{
                vueDialogue.findViewById(R.id.couleurVert),
                vueDialogue.findViewById(R.id.couleurRouge),
                vueDialogue.findViewById(R.id.couleurBleu),
                vueDialogue.findViewById(R.id.couleurJaune),
                vueDialogue.findViewById(R.id.couleurOrange),
                vueDialogue.findViewById(R.id.couleurGris)
        };

        AlertDialog dialogue = new AlertDialog.Builder(this)
                .setTitle("Choisir une couleur")
                .setView(vueDialogue)
                .setNegativeButton("Annuler", null)
                .create();

        String[] couleurs = PaletteCouleurs.obtenirToutesLesCouleurs();
        for (int i = 0; i < cercles.length; i++) {
            String couleurHexa = couleurs[i];
            cercles[i].setBackground(PaletteCouleurs.creerFormeRonde(couleurHexa));
            cercles[i].setOnClickListener(v -> {
                couleurActuelle = couleurHexa;
                appliquerCouleurAuFond();
                dialogue.dismiss();
            });
        }

        dialogue.show();
    }

    private void appliquerCouleurAuFond() {
        fondColoreFormulaire.setBackground(PaletteCouleurs.creerFormeRectangleArrondi(couleurActuelle, 28f));
    }

    private String obtenirDateFormateeActuelle() {
        String[] mois = {
                "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
        };
        Calendar calendrier = Calendar.getInstance();
        int jour = calendrier.get(Calendar.DAY_OF_MONTH);
        int moisActuel = calendrier.get(Calendar.MONTH);
        int annee = calendrier.get(Calendar.YEAR);
        return String.format("%02d %s %d", jour, mois[moisActuel], annee);
    }
}

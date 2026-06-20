package com.tpmobile.gestionnairenotes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tpmobile.gestionnairenotes.adaptateur.AdaptateurNotes;
import com.tpmobile.gestionnairenotes.donnees.GestionnaireBaseDeDonnees;
import com.tpmobile.gestionnairenotes.modele.Note;
import com.tpmobile.gestionnairenotes.utilitaire.PaletteCouleurs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ActivitePrincipale extends AppCompatActivity implements AdaptateurNotes.EcouteurActionNote {

    public static final String EXTRA_MODE = "extra_mode";
    public static final String EXTRA_NOTE = "extra_note";
    public static final String EXTRA_COULEUR_SELECTIONNEE = "extra_couleur_selectionnee";
    public static final String MODE_CREATION = "creation";
    public static final String MODE_MODIFICATION = "modification";
    public static final String MODE_SUPPRESSION = "suppression";

    private static final String NOM_PREFERENCES = "preferences_notes";
    private static final String CLE_MODE_SOMBRE = "mode_sombre_active";

    private GestionnaireBaseDeDonnees gestionnaireBaseDeDonnees;
    private ExecutorService executorService;
    private ActivityResultLauncher<Intent> lanceurFormulaire;

    private final List<Note> toutesLesNotes = new ArrayList<>();
    private final List<Note> notesAffichees = new ArrayList<>();
    private AdaptateurNotes adaptateurNotes;

    private EditText editTexteRecherche;
    private TextView boutonBasculeFavoris;
    private TextView texteCompteurNotes;
    private TextView texteEtatVide;
    private RecyclerView listeNotesRecyclerView;
    private ImageView boutonTrier;
    private ImageView boutonModeSombre;
    private ImageView boutonAjouterNote;
    private View[] vuesCerclesCouleurs;

    private boolean paletteVisible = false;
    private boolean filtreFavorisActif = false;
    private String modeTriActuel = "date";

    @Override
    protected void onCreate(Bundle etatSauvegarde) {
        super.onCreate(etatSauvegarde);
        setContentView(R.layout.ecran_liste_notes);

        gestionnaireBaseDeDonnees = GestionnaireBaseDeDonnees.obtenirInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        lierVues();
        configurerPaletteCouleurs();
        configurerEcouteurs();
        initialiserLanceurFormulaire();
        mettreAJourApparenceBoutonFavoris();

        adaptateurNotes = new AdaptateurNotes(notesAffichees, this);
        listeNotesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        listeNotesRecyclerView.setAdapter(adaptateurNotes);

        chargerNotesDepuisRoom();
    }

    @Override
    protected void onResume() {
        super.onResume();
        paletteVisible = false;
        mettreAJourVisibilitePalette();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (executorService != null) {
            executorService.shutdown();
        }
    }

    private void lierVues() {
        editTexteRecherche = findViewById(R.id.editTexteRecherche);
        boutonBasculeFavoris = findViewById(R.id.boutonBasculeFavoris);
        texteCompteurNotes = findViewById(R.id.texteCompteurNotes);
        texteEtatVide = findViewById(R.id.texteEtatVide);
        listeNotesRecyclerView = findViewById(R.id.listeNotesRecyclerView);
        boutonTrier = findViewById(R.id.boutonTrier);
        boutonModeSombre = findViewById(R.id.boutonModeSombre);
        boutonAjouterNote = findViewById(R.id.boutonAjouterNote);

        vuesCerclesCouleurs = new View[]{
                findViewById(R.id.couleurVert),
                findViewById(R.id.couleurRouge),
                findViewById(R.id.couleurBleu),
                findViewById(R.id.couleurJaune),
                findViewById(R.id.couleurOrange),
                findViewById(R.id.couleurGris)
        };
    }

    private void configurerPaletteCouleurs() {
        String[] couleurs = PaletteCouleurs.obtenirToutesLesCouleurs();
        for (int i = 0; i < vuesCerclesCouleurs.length; i++) {
            String couleurHexa = couleurs[i];
            vuesCerclesCouleurs[i].setBackground(PaletteCouleurs.creerFormeRonde(couleurHexa));
            vuesCerclesCouleurs[i].setOnClickListener(v -> {
                paletteVisible = false;
                mettreAJourVisibilitePalette();
                ouvrirFormulairePourCreation(couleurHexa);
            });
        }
    }

    private void configurerEcouteurs() {
        boutonAjouterNote.setOnClickListener(v -> {
            paletteVisible = !paletteVisible;
            mettreAJourVisibilitePalette();
        });

        boutonBasculeFavoris.setOnClickListener(v -> {
            filtreFavorisActif = !filtreFavorisActif;
            mettreAJourApparenceBoutonFavoris();
            actualiserNotesAffichees();
        });

        boutonTrier.setOnClickListener(this::afficherMenuTri);

        boutonModeSombre.setOnClickListener(v -> basculerModeSombre());

        editTexteRecherche.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence sequence, int debut, int compte, int apres) {
            }

            @Override
            public void onTextChanged(CharSequence sequence, int debut, int avant, int compte) {
                actualiserNotesAffichees();
            }

            @Override
            public void afterTextChanged(Editable sequence) {
            }
        });
    }

    private void initialiserLanceurFormulaire() {
        lanceurFormulaire = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                resultat -> {
                    if (resultat.getResultCode() == RESULT_OK && resultat.getData() != null) {

                        Intent donnees = resultat.getData();

                        String mode = donnees.getStringExtra(EXTRA_MODE);
                        Note note = (Note) donnees.getSerializableExtra(EXTRA_NOTE);

                        if (note == null) {
                            return;
                        }

                        if (MODE_CREATION.equals(mode)) {
                            ajouterNoteDansRoom(note);
                        } else if (MODE_MODIFICATION.equals(mode)) {
                            modifierNoteDansRoom(note);
                        } else if (MODE_SUPPRESSION.equals(mode)) {
                            supprimerNoteDansRoom(note);
                        }
                    }
                }
        );
    }

    private void mettreAJourVisibilitePalette() {
        for (View vueCercle : vuesCerclesCouleurs) {
            vueCercle.setVisibility(paletteVisible ? View.VISIBLE : View.GONE);
        }
    }

    private void mettreAJourApparenceBoutonFavoris() {
        if (filtreFavorisActif) {
            boutonBasculeFavoris.setBackgroundResource(R.drawable.forme_favoris_actif);
            boutonBasculeFavoris.setTextColor(ContextCompat.getColor(this, R.color.couleur_blanc));
        } else {
            boutonBasculeFavoris.setBackgroundResource(R.drawable.forme_fond_arrondi);
            boutonBasculeFavoris.setTextColor(ContextCompat.getColor(this, R.color.couleur_texte_principal));
        }
    }

    private void afficherMenuTri(View vueAncrage) {
        PopupMenu menuPopup = new PopupMenu(this, vueAncrage);
        menuPopup.getMenu().add(0, 0, 0, "Plus récentes");
        menuPopup.getMenu().add(0, 1, 1, "Titre (A-Z)");
        menuPopup.setOnMenuItemClickListener(item -> {
            modeTriActuel = item.getItemId() == 1 ? "titre" : "date";
            actualiserNotesAffichees();
            return true;
        });
        menuPopup.show();
    }

    private void basculerModeSombre() {
        SharedPreferences preferences = getSharedPreferences(NOM_PREFERENCES, MODE_PRIVATE);
        boolean modeSombreActifActuellement = preferences.getBoolean(CLE_MODE_SOMBRE, false);
        boolean nouvelleValeurModeSombre = !modeSombreActifActuellement;
        preferences.edit().putBoolean(CLE_MODE_SOMBRE, nouvelleValeurModeSombre).apply();
        AppCompatDelegate.setDefaultNightMode(
                nouvelleValeurModeSombre ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    private void chargerNotesDepuisRoom() {
        executorService.execute(() -> {
            List<Note> notesDepuisBase = gestionnaireBaseDeDonnees.noteDao().recupererToutesLesNotes();

            runOnUiThread(() -> {
                toutesLesNotes.clear();
                toutesLesNotes.addAll(notesDepuisBase);
                actualiserNotesAffichees();
            });
        });
    }

    private void ajouterNoteDansRoom(Note note) {
        executorService.execute(() -> {
            note.setIdentifiant(0);
            gestionnaireBaseDeDonnees.noteDao().ajouterNote(note);

            runOnUiThread(() -> {
                Toast.makeText(this, "Note créée", Toast.LENGTH_SHORT).show();
                chargerNotesDepuisRoom();
            });
        });
    }

    private void modifierNoteDansRoom(Note note) {
        executorService.execute(() -> {
            gestionnaireBaseDeDonnees.noteDao().modifierNote(note);

            runOnUiThread(() -> {
                Toast.makeText(this, "Note modifiée", Toast.LENGTH_SHORT).show();
                chargerNotesDepuisRoom();
            });
        });
    }

    private void supprimerNoteDansRoom(Note note) {
        executorService.execute(() -> {
            gestionnaireBaseDeDonnees.noteDao().supprimerNote(note);

            runOnUiThread(() -> {
                Toast.makeText(this, "Note supprimée", Toast.LENGTH_SHORT).show();
                chargerNotesDepuisRoom();
            });
        });
    }

    private void actualiserNotesAffichees() {
        String texteRecherche = editTexteRecherche.getText().toString().trim().toLowerCase();

        List<Note> notesFiltrees = new ArrayList<>();
        for (Note note : toutesLesNotes) {
            boolean correspondAuFiltreFavoris = !filtreFavorisActif || note.estFavori();
            boolean correspondALaRecherche = texteRecherche.isEmpty()
                    || note.getTitre().toLowerCase().contains(texteRecherche);
            if (correspondAuFiltreFavoris && correspondALaRecherche) {
                notesFiltrees.add(note);
            }
        }

        if (modeTriActuel.equals("titre")) {
            Collections.sort(notesFiltrees, Comparator.comparing(note -> note.getTitre().toLowerCase()));
        }

        notesAffichees.clear();
        notesAffichees.addAll(notesFiltrees);
        adaptateurNotes.notifyDataSetChanged();

        boolean listeVide = notesAffichees.isEmpty();
        texteEtatVide.setVisibility(listeVide ? View.VISIBLE : View.GONE);
        listeNotesRecyclerView.setVisibility(listeVide ? View.GONE : View.VISIBLE);

        int nombreDeNotes = toutesLesNotes.size();
        texteCompteurNotes.setText(nombreDeNotes + (nombreDeNotes == 1 ? " note" : " notes"));
    }

    private void ouvrirFormulairePourCreation(String couleurHexa) {
        Intent intention = new Intent(this, ActiviteFormulaireNote.class);
        intention.putExtra(EXTRA_MODE, MODE_CREATION);
        intention.putExtra(EXTRA_COULEUR_SELECTIONNEE, couleurHexa);
        lanceurFormulaire.launch(intention);
    }

    private void ouvrirFormulairePourModification(Note note) {
        Intent intention = new Intent(this, ActiviteFormulaireNote.class);
        intention.putExtra(EXTRA_MODE, MODE_MODIFICATION);
        intention.putExtra(EXTRA_NOTE, note);
        lanceurFormulaire.launch(intention);
    }

    @Override
    public void onNoteCliquee(Note note) {
        ouvrirFormulairePourModification(note);
    }

    @Override
    public void onFavoriBascule(Note note) {
        note.setFavori(!note.estFavori());

        executorService.execute(() -> {
            gestionnaireBaseDeDonnees.noteDao().modifierNote(note);

            runOnUiThread(() -> {
                Toast.makeText(this,
                        note.estFavori() ? "Ajoutée aux favoris" : "Retirée des favoris",
                        Toast.LENGTH_SHORT).show();
                chargerNotesDepuisRoom();
            });
        });
    }
}

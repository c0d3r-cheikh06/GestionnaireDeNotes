package com.tpmobile.gestionnairenotes.adaptateur;

import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tpmobile.gestionnairenotes.R;
import com.tpmobile.gestionnairenotes.modele.Note;
import com.tpmobile.gestionnairenotes.utilitaire.PaletteCouleurs;

import java.util.List;

public class AdaptateurNotes extends RecyclerView.Adapter<AdaptateurNotes.VueNote> {

    public interface EcouteurActionNote {
        void onNoteCliquee(Note note);

        void onFavoriBascule(Note note);
    }

    private final List<Note> notes;
    private final EcouteurActionNote ecouteur;

    public AdaptateurNotes(List<Note> notes, EcouteurActionNote ecouteur) {
        this.notes = notes;
        this.ecouteur = ecouteur;
    }

    @NonNull
    @Override
    public VueNote onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vue = LayoutInflater.from(parent.getContext()).inflate(R.layout.element_note, parent, false);
        return new VueNote(vue);
    }

    @Override
    public void onBindViewHolder(@NonNull VueNote vueNote, int position) {
        vueNote.lier(notes.get(position));
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    class VueNote extends RecyclerView.ViewHolder {

        private final View conteneurNote;
        private final TextView texteTitreNote;
        private final TextView texteDateNote;
        private final ImageView badgeFavori;
        private final GestureDetector detecteurGestes;

        VueNote(@NonNull View vueElement) {
            super(vueElement);
            conteneurNote = vueElement.findViewById(R.id.conteneurNote);
            texteTitreNote = vueElement.findViewById(R.id.texteTitreNote);
            texteDateNote = vueElement.findViewById(R.id.texteDateNote);
            badgeFavori = vueElement.findViewById(R.id.badgeFavori);

            detecteurGestes = new GestureDetector(vueElement.getContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapConfirmed(MotionEvent evenement) {
                    ecouteur.onNoteCliquee(notes.get(getAdapterPosition()));
                    return true;
                }

                @Override
                public boolean onDoubleTap(MotionEvent evenement) {
                    ecouteur.onFavoriBascule(notes.get(getAdapterPosition()));
                    return true;
                }
            });

            vueElement.setOnTouchListener((v, evenement) -> detecteurGestes.onTouchEvent(evenement));
        }

        void lier(Note note) {
            texteTitreNote.setText(note.getTitre());
            texteDateNote.setText(note.getDate());
            conteneurNote.setBackground(PaletteCouleurs.creerFormeRectangleArrondi(note.getCouleur(), 28f));
            badgeFavori.setVisibility(note.estFavori() ? View.VISIBLE : View.GONE);
        }
    }
}

package com.tpmobile.gestionnairenotes.donnees;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.tpmobile.gestionnairenotes.modele.Note;

import java.util.List;

@Dao
public interface NoteDao {

    @Insert
    void ajouterNote(Note note);

    @Update
    void modifierNote(Note note);

    @Delete
    void supprimerNote(Note note);

    @Query("SELECT * FROM notes ORDER BY identifiant DESC")
    List<Note> recupererToutesLesNotes();

    @Query("DELETE FROM notes")
    void supprimerToutesLesNotes();
}

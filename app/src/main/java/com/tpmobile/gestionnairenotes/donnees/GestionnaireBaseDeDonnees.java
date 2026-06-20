package com.tpmobile.gestionnairenotes.donnees;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.tpmobile.gestionnairenotes.modele.Note;

@Database(entities = {Note.class}, version = 1, exportSchema = false)
public abstract class GestionnaireBaseDeDonnees extends RoomDatabase {

    private static volatile GestionnaireBaseDeDonnees INSTANCE;

    public abstract NoteDao noteDao();

    public static GestionnaireBaseDeDonnees obtenirInstance(Context contexte) {
        if (INSTANCE == null) {
            synchronized (GestionnaireBaseDeDonnees.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            contexte.getApplicationContext(),
                            GestionnaireBaseDeDonnees.class,
                            "gestionnaire_notes_database"
                    )
                            .fallbackToDestructiveMigration(false)
                            .build();
                }
            }
        }

        return INSTANCE;
    }
}

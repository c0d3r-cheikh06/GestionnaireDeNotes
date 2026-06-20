package com.tpmobile.gestionnairenotes;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public class ApplicationNotes extends Application {

    private static final String NOM_PREFERENCES = "preferences_notes";
    private static final String CLE_MODE_SOMBRE = "mode_sombre_active";

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences preferences = getSharedPreferences(NOM_PREFERENCES, MODE_PRIVATE);
        boolean modeSombreActive = preferences.getBoolean(CLE_MODE_SOMBRE, false);
        AppCompatDelegate.setDefaultNightMode(
                modeSombreActive ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }
}

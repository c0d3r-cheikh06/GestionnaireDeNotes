package com.tpmobile.gestionnairenotes.utilitaire;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

public class PaletteCouleurs {

    public static final String VERT = "#219653";
    public static final String ROUGE = "#EB5757";
    public static final String BLEU = "#2F80ED";
    public static final String JAUNE = "#F2C94C";
    public static final String ORANGE = "#F2994A";
    public static final String GRIS = "#828282";

    public static String[] obtenirToutesLesCouleurs() {
        return new String[]{VERT, ROUGE, BLEU, JAUNE, ORANGE, GRIS};
    }

    public static GradientDrawable creerFormeRonde(String couleurHexa) {
        GradientDrawable forme = new GradientDrawable();
        forme.setShape(GradientDrawable.OVAL);
        forme.setColor(Color.parseColor(couleurHexa));
        return forme;
    }

    public static GradientDrawable creerFormeRectangleArrondi(String couleurHexa, float rayonCoins) {
        GradientDrawable forme = new GradientDrawable();
        forme.setShape(GradientDrawable.RECTANGLE);
        forme.setColor(Color.parseColor(couleurHexa));
        forme.setCornerRadius(rayonCoins);
        return forme;
    }
}

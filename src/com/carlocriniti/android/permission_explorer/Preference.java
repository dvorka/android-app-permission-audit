/*
 * Projet 	: Permission Explorer
 * Auteur 	: Carlo Criniti
 * Date   	: 2011.06.10
 * 
 * Classe Preference
 * Activité d'affichage des préférences
 */
package com.carlocriniti.android.permission_explorer;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Preference extends PreferenceActivity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Création de l'interface de préférences
        addPreferencesFromResource(R.xml.preference);
    }
}

/*
 * Projet 	: Permission Explorer
 * Auteur 	: Carlo Criniti
 * Date   	: 2011.06.10
 * 
 * Classe PermissionDetail
 * Activité d'affichage du détail d'une permission
 * avec les applications qui l'utilisent
 */
package com.carlocriniti.android.permission_explorer;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class PermissionDetail extends Activity {
	// Liste des applications
	ListView applicationList;
	
	/*
	 * onCreate :
	 * Exécuté à la création de l'activité. Récupère
	 * les informations sur la permission reçue par
	 * l'Intent et les inscrits dans les composants
	 * graphiques
	 */
	@Override
    public void onCreate(Bundle savedInstanceState) {        
    	super.onCreate(savedInstanceState);
        
    	// Création de l'interface graphique
    	setContentView(R.layout.permission_detail);
    	
    	// Récupération de l'intent
    	Intent thisIntent = getIntent();
        String permissionId = Long.toString(thisIntent.getExtras().getLong("permissionId"));
        
        // Récupération des informations sur la permission
        Cursor data = Tools.database.database.query("permission", new String[]{"name"}, "id = ?", new String[]{permissionId}, null, null, null);
        if (data.getCount() == 1) {
        	data.moveToFirst();
        	
        	// Affichage des informations
        	((TextView)findViewById(R.id.permission_detail_name)).setText(data.getString(0));
        	((TextView)findViewById(R.id.permission_detail_description)).setText(Tools.getStringResourceByName("permission_" + data.getString(0), getResources(), this));
        	
        	SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        	boolean hideSystemApp = pref.getBoolean("hide_system_app", false); // Hide system applications
        	
        	String systemAppWhere = "";
        	if (hideSystemApp)
        		systemAppWhere = " AND system = 0";
        	
        	// Selection et affichage du nombre d'applications qui utilisent cette permission
        	data = Tools.database.database.rawQuery("SELECT Count(*) AS number " +
        											"FROM relation_application_permission " +
        											"LEFT OUTER JOIN application ON relation_application_permission.application = application.id " +
        											"WHERE permission = ?" + systemAppWhere + ";", new String[]{permissionId});
        	data.moveToFirst();
        	((TextView)findViewById(R.id.permission_detail_application_count)).setText(data.getString(0));
        	
        	// Récupération des préférences d'affichage
        	boolean applicationName = pref.getBoolean("application_name", true); // Display true:label / false:package 
        	
        	// Champ à afficher
        	String nameField;
        	if (applicationName)
        		nameField = "application.label";
        	else
        		nameField = "application.name";
        	
        	// Récupération de la liste des applications et affichage dans la liste
        	Cursor applicationListCursor = Tools.database.database.rawQuery("SELECT application.id AS _id, " + nameField + " AS name, application.name AS package " +
        																	"FROM relation_application_permission " +
        																	"INNER JOIN application ON relation_application_permission.application = application.id " +
        																	"WHERE relation_application_permission.permission = ?" + systemAppWhere + " " +
        																	"ORDER BY " + nameField + " COLLATE NOCASE ASC;", new String[] {permissionId});
        	startManagingCursor(applicationListCursor);

        	List<ApplicationListItem> items = new ArrayList<ApplicationListItem>();
        	
        	PackageManager pm = getPackageManager();
        	try {
    	    	for(applicationListCursor.moveToFirst(); !applicationListCursor.isAfterLast(); applicationListCursor.moveToNext()) {
    				items.add(new ApplicationListItem(applicationListCursor.getLong(0), pm.getApplicationIcon(applicationListCursor.getString(2)), applicationListCursor.getString(1)));
    	    	}
        	} catch (NameNotFoundException e) {
    			e.printStackTrace();
    		}
        	
        	ApplicationListAdapter applicationAdapter = new ApplicationListAdapter(this, items);
        	
        	applicationList = (ListView)findViewById(R.id.permission_detail_application_list);
        	applicationList.setAdapter(applicationAdapter);
        	
        	// Ajout d'un évenement sur la liste
        	applicationList.setOnItemClickListener(new OnItemClickListener() {
    			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
    				// Ouverture du détail de l'application sélectionnée
    				Intent intent = new Intent(getBaseContext() , ApplicationDetail.class);     
    				intent.putExtra("applicationId",id); 			  		  
    				startActivity(intent); 
    			}
            });
        } else {
        	// Aucune permission ne correspond
        	((TextView)findViewById(R.id.permission_detail_name)).setText(getString(R.string.permission_detail_nodata));
        }
        data.close();
	}
}

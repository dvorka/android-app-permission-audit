/*
 * Projet 	: Permission Explorer
 * Auteur 	: Carlo Criniti
 * Date   	: 2011.06.10
 * 
 * Classe ApplicationDetail
 * Activité d'affichage du détail d'une application
 * avec les permissions qu'elle utilise
 */

package com.carlocriniti.android.permission_explorer;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class ApplicationDetail extends Activity {
	private ListView permissionList; // Composant graphique gérant la liste de permissions
	private ImageButton manageButton; // Bouton pemrettant d'ouvrir l'application manager
	private String packageName;
	private Context context;
	/*
	 * onCreate :
	 * Exécuté à la création de l'activité. Récupère
	 * les informations sur l'application reçue par
	 * l'Intent et les inscrits dans les composants
	 * graphiques
	 */
	@Override
    public void onCreate(Bundle savedInstanceState) {        
    	super.onCreate(savedInstanceState);
    	
    	// Création de l'interface graphique et récupération de l'Intent
        setContentView(R.layout.application_detail);
        
        this.context = this;
    	Intent thisIntent = getIntent();
        String applicationId = Long.toString(thisIntent.getExtras().getLong("applicationId"));
        
        // Récupération des données
        Cursor data = Tools.database.database.query("application", new String[]{"label", "name", "version_code", "version_name", "system"}, "id = ?", new String[]{applicationId}, null, null, null);
        if (data.getCount() == 1) {
        	data.moveToFirst();
        	
        	packageName = data.getString(1);
        	
        	// Affichage du nom de l'application, du package et de la version
        	((TextView)findViewById(R.id.application_detail_label)).setText(data.getString(0));
        	((TextView)findViewById(R.id.application_detail_name)).setText(data.getString(1));
        	((TextView)findViewById(R.id.application_detail_version)).setText(data.getString(2) + " / " + data.getString(3));
        	
        	if (data.getInt(4) == 1)
        		((TextView)findViewById(R.id.application_detail_system)).setVisibility(View.VISIBLE);
        	else
        		((TextView)findViewById(R.id.application_detail_system)).setVisibility(View.GONE);
        	
        	
        	manageButton = (ImageButton)findViewById(R.id.application_detail_manage_button); 
        	manageButton.setImageResource(R.drawable.ic_menu_manage);
        	manageButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
				    if (Build.VERSION.SDK_INT >= 9) {
						try {
					    	Intent i = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
						    i.addCategory(Intent.CATEGORY_DEFAULT);
					    	i.setData(Uri.parse("package:" + packageName));
					        startActivity(i);
					    } catch (ActivityNotFoundException anfe) {
					    	Intent i = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
						    i.addCategory(Intent.CATEGORY_DEFAULT);
					        startActivity(i);
					    }
				    } else {
				    	Toast.makeText(ApplicationDetail.this, context.getText(R.string.application_detail_manager_unavailable), Toast.LENGTH_LONG).show();
				    }
					
				}
            });
        	
        	// Récupération du nombre de permissions utilisées et affichage
        	data = Tools.database.database.rawQuery("SELECT Count(*) AS number " +
        											"FROM relation_application_permission " +
        											"WHERE application = ?;", new String[]{applicationId});
        	data.moveToFirst();
        	((TextView)findViewById(R.id.application_detail_permission_count)).setText(data.getString(0));
        	
        	// Récupération des permissions et création de la liste
        	Cursor permissionListCursor = Tools.database.database.rawQuery("SELECT permission.id AS _id, permission.name AS name FROM relation_application_permission INNER JOIN permission ON relation_application_permission.permission = permission.id WHERE relation_application_permission.application = ? ORDER BY permission.name COLLATE NOCASE ASC;", new String[] {applicationId});
        	startManagingCursor(permissionListCursor);
        	ListAdapter permissionAdapter = new SimpleCursorAdapter(this, R.layout.permission_list_item, permissionListCursor, new String[] {"name"}, new int[]{R.id.listviewpermissiontext});
        	permissionList = (ListView)findViewById(R.id.application_detail_permission_list);
        	permissionList.setAdapter(permissionAdapter);
        	
        	// Evenement au clic sur la liste
        	permissionList.setOnItemClickListener(new OnItemClickListener() {
    			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
    				// Ouverture de l'activite détail de la permission selectionnee
    				Intent intent = new Intent(getBaseContext() , PermissionDetail.class);     
    				intent.putExtra("permissionId",id); 			  		  
    				startActivity(intent); 
    			}
            });
        	
        	
        } else {
        	// Application non trouvée dans la base de donnees
        	((TextView)findViewById(R.id.application_detail_label)).setText(getString(R.string.application_detail_nodata));
        }
        // Fermeture de l'accès a la base de donnees
        data.close();
	}
}

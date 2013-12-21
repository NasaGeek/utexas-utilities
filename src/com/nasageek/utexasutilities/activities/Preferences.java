package com.nasageek.utexasutilities.activities;

import java.io.IOException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.BaseAdapter;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.nasageek.utexasutilities.ConnectionHelper;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.SecurePreferences;
import com.nasageek.utexasutilities.Utility;

public class Preferences extends SherlockPreferenceActivity{

	private Preference loginfield;
    private Preference passwordfield;
    private CheckBoxPreference autologin;
    private BaseAdapter ba;
    private ActionBar actionbar;
    private SecurePreferences sp;
    
	@Override
    protected void onCreate(Bundle savedInstanceState) 
    {
    	super.onCreate(savedInstanceState);

    	sp = new SecurePreferences(this,"com.nasageek.utexasutilities.password", false);

   		actionbar = getSupportActionBar();
    	actionbar.setTitle("Preferences");
    	actionbar.setHomeButtonEnabled(true);
    	actionbar.setDisplayHomeAsUpEnabled(true);
    	
        addPreferencesFromResource(R.xml.preferences);
        getListView().setCacheColorHint(Color.TRANSPARENT);
        ba = (BaseAdapter)getPreferenceScreen().getRootAdapter();
  
        autologin = (CheckBoxPreference) findPreference("autologin");
        loginfield = (Preference) findPreference("eid");
        passwordfield = (Preference) findPreference("password");
        
        //bypass the default SharedPreferences and save the password to the encrypted SP instead
        passwordfield.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				sp.put(preference.getKey(), (String)newValue);
				return false;
			}
		});
        
        final Preference logincheckbox = (Preference) findPreference("loginpref");
        
        // TODO: figure out why this is here, was it related to the old Login Pref stuff?
        logincheckbox.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if((Boolean)newValue == false)
					autologin.setChecked(false);
				return true;
			}
		});
        
        logincheckbox.setOnPreferenceClickListener(new OnPreferenceClickListener() 
        {
        	public boolean onPreferenceClick(final Preference preference)
        	{        		
        		if(((CheckBoxPreference)preference).isChecked())
            	{
            		AlertDialog.Builder nologin_builder = new AlertDialog.Builder(Preferences.this);
            		nologin_builder.setMessage("NOTE: This will save your UT credentials to your device! If that worries you, " +
            				"uncheck this preference and go tap one of the buttons on the main screen to log in. See " +
            				"the Privacy Policy on the About page for more information.")
	            			.setCancelable(true)
	            			.setNeutralButton("Okay", new DialogInterface.OnClickListener() 
	            			{
	    	                    public void onClick(DialogInterface dialog, int id) 
	    	                    {               
	    	                    	dialog.cancel();
	    		                }
	    	            	});	
            		
            		AlertDialog nologin = nologin_builder.create();
	            	nologin.show();     	
            	}
            	else
            	{	           		
            		loginfield.setEnabled(true);
               	  	passwordfield.setEnabled(true);
               	  	/*if they switch to temp login we'll save their EID, but clear their password
               	  	for security purposes*/
               	  	sp.removeValue("password");
            		ba.notifyDataSetChanged();
            	}
        	//  whenever they switch between temp and persistent, log them out
        		ConnectionHelper.logout(Preferences.this);
	        	
            	return true;	
            }
        });

        //disable the EID and password preferences if the user is logged in
        //TODO: make a method to check if user is logged in, it's cleaner that way
        if(ConnectionHelper.cookieHasBeenSet() && ConnectionHelper.PNACookieHasBeenSet() && ConnectionHelper.bbCookieHasBeenSet())
        {
        	loginfield.setEnabled(false);
        	passwordfield.setEnabled(false);
        }
        else
        {
        	loginfield.setEnabled(true);
        	passwordfield.setEnabled(true);      
        }
        
        final CheckBoxPreference sendcrashes = (CheckBoxPreference)findPreference("acra.enable");
        sendcrashes.setOnPreferenceChangeListener(new OnPreferenceChangeListener() 
        {	
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) 
			{
				return true;
			}
		});
        
        final Preference about = (Preference)findPreference("about");
        about.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				final Intent about_intent = new Intent(Preferences.this, AboutMeActivity.class);
				startActivity(about_intent);
				return true;
			}
		});
        final Preference updateBusStops = (Preference) findPreference("update_stops");
        updateBusStops.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				try {
					Utility.updateBusStops(Preferences.this);
				} catch (IOException e) {
					e.printStackTrace();
					Toast.makeText(Preferences.this, "Stops could not be written to file.", Toast.LENGTH_SHORT).show();
				}
				return true;
			}
		});
    }
    @Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
    	int id = item.getItemId();
    	switch(id)
    	{
	    	case android.R.id.home:
	            // app icon in action bar clicked; go home
	            super.onBackPressed();
	            break;
    	}
    	return false;
	}
    @Override
    public void onResume()
    {
    	super.onResume();
    	if(ConnectionHelper.cookieHasBeenSet() && ConnectionHelper.PNACookieHasBeenSet() && ConnectionHelper.bbCookieHasBeenSet())
        {
	        loginfield.setEnabled(false);
	        passwordfield.setEnabled(false);
	        ba.notifyDataSetChanged();
        }
        else
        {
		  	loginfield.setEnabled(true);
		    passwordfield.setEnabled(true);  
		  	ba.notifyDataSetChanged();
        }
    }
}
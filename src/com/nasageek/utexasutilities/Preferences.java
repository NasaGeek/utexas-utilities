package com.nasageek.utexasutilities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.crittercism.app.Crittercism;

public class Preferences extends SherlockPreferenceActivity{

	boolean set;
	Toast toast;
	ProgressDialog pd;
	SharedPreferences settings;
	Preference loggedin;
	Preference loginfield;
    Preference passwordfield;
    CheckBoxPreference autologin;
    Editor edit;
    BaseAdapter ba;
    ActionBar actionbar;
    OnPreferenceChangeListener listen;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
    	requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    	super.onCreate(savedInstanceState);

    	settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
       
       
    	edit = settings.edit();
   		toast = Toast.makeText(this, "", Toast.LENGTH_LONG);
      
   		actionbar = getSupportActionBar();
    	actionbar.setTitle("Preferences");
    	actionbar.setHomeButtonEnabled(true);
//  	actionbar.setDisplayHomeAsUpEnabled(true);
    	if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)	
			actionbar.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.actionbar_bg));
       
    	setSupportProgressBarIndeterminateVisibility(false);
        addPreferencesFromResource(R.xml.preferences);
        getListView().setCacheColorHint(Color.TRANSPARENT);
        ba = (BaseAdapter)getPreferenceScreen().getRootAdapter();
   //   PreferenceGroup loginfields = (PreferenceGroup) findPreference("loginfields");
        
    /*  if(!settings.getBoolean("loginpref", true))
        	loginfields.setEnabled(false);
        else loginfields.setEnabled(true);*/
   //   	loggedin = (Preference) findPreference("loggedin");
        autologin = (CheckBoxPreference) findPreference("autologin");
        loginfield = (Preference) findPreference("eid");
        passwordfield = (Preference) findPreference("password");
        
        final Preference logincheckbox = (Preference) findPreference("loginpref");
        
        logincheckbox.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if((Boolean)newValue==false)
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
            		nologin_builder.setMessage("NOTE: This will save your UT credentials to your device!  If that makes you " +
            				"uncomfortable just uncheck this setting and log in by tapping the \"Login\" button on the main screen.")
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
            		ConnectionHelper.logout(Preferences.this);
            		loginfield.setEnabled(true);
               	  	passwordfield.setEnabled(true);    
            		ba.notifyDataSetChanged();
            	}
            	ClassDatabase.getInstance(Preferences.this).deleteDb(Preferences.this, true);
            	return true;	
            }
        });
        if(ConnectionHelper.cookieHasBeenSet())
        {
        	loginfield.setEnabled(false);
        	passwordfield.setEnabled(false);
        }
        else
        {
        	loginfield.setEnabled(true);
        	passwordfield.setEnabled(true);      
        }
        listen = new OnPreferenceChangeListener()
        {
        	public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				return false;
			}
      	};
        Preference resetclassesbutton = (Preference) findPreference("resetclassesbutton");
        resetclassesbutton.setOnPreferenceClickListener(new OnPreferenceClickListener() 
        { 
            public boolean onPreferenceClick(Preference preference)
            {
                ClassDatabase.getInstance(Preferences.this).deleteDb(Preferences.this, false);
                return true;
            }
        });
        final CheckBoxPreference sendcrashes = (CheckBoxPreference)findPreference("sendcrashes");
        sendcrashes.setOnPreferenceChangeListener(new OnPreferenceChangeListener() 
        {	
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) 
			{
				Crittercism.setOptOutStatus(!(Boolean)newValue);
				return true;
			}
		});
    }
    public void refreshPrefs()
    {
    	if(ConnectionHelper.cookieHasBeenSet())
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
    @Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
    	int id = item.getItemId();
    	switch(id)
    	{
	    	case android.R.id.home:
	            // app icon in action bar clicked; go home
	            Intent home = new Intent(this, UTilitiesActivity.class);
	            home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(home);break;
    	}
    	return false;
	}
    @Override
    public void onResume()
    {
    	super.onResume();
    	if(ConnectionHelper.cookieHasBeenSet())
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
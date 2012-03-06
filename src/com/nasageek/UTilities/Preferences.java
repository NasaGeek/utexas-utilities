package com.nasageek.UTilities;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.EditText;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Toast;

public class Preferences extends PreferenceActivity {

	boolean set;
	boolean pnalogindone, logindone;
	Toast toast;
	ProgressDialog pd;
	SharedPreferences settings;
	Preference loginfield, loginButton;
    Preference passwordfield;
 Editor edit;
    BaseAdapter ba;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
        settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
       edit = settings.edit();
       toast = Toast.makeText(this, "", Toast.LENGTH_LONG);
        addPreferencesFromResource(R.xml.preferences);
        ba = (BaseAdapter)getPreferenceScreen().getRootAdapter();
   //     PreferenceGroup loginfields = (PreferenceGroup) findPreference("loginfields");
        
    /*    if(!settings.getBoolean("loginpref", true))
        	loginfields.setEnabled(false);
        else loginfields.setEnabled(true);*/
        loginfield = (Preference) findPreference("eid");
        passwordfield = (Preference) findPreference("password");
       loginButton = (Preference) findPreference("loggedin");
        final Preference logincheckbox = (Preference) findPreference("loginpref");
        
        
        	logincheckbox.setOnPreferenceClickListener(new OnPreferenceClickListener() {
       	 
        	
        	public boolean onPreferenceClick(final Preference preference) {

            	if(((CheckBoxPreference)preference).isChecked())
            	{
            		AlertDialog.Builder nologin_builder = new AlertDialog.Builder(Preferences.this);
	            	nologin_builder.setMessage("NOTE: This app is unofficial, so if you enter your EID and password into the " +
	            			"settings, their protection is left up to me, a random developer.  Some people may feel more " +
	            			"secure by just logging in through UTDirect and then using the app.")
	            			.setCancelable(false)
	            			.setPositiveButton("Persistent", new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int id) {
	                       ((CheckBoxPreference) preference).setChecked(true);
	                       ConnectionHelper.logout(Preferences.this);
	                       loginButton.setTitle("Login");
	                       loginfield.setEnabled(true);
	                       passwordfield.setEnabled(true);    
	                       ba.notifyDataSetChanged();
	                       dialog.cancel();
	                       
	                    }
            			})	
	                .setNegativeButton("UTDirect", new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int id) {
	                    	((CheckBoxPreference) preference).setChecked(false);
	                    	
	                        dialog.cancel();
	                        Intent login_intent = new Intent(Preferences.this, LoginActivity.class);
	                    	startActivity(login_intent);
	                    }
	                });
	            	AlertDialog nologin = nologin_builder.create();
	            	nologin.show();            	
            	}
            	else
            	{	ConnectionHelper.logout(Preferences.this);
            		loginButton.setTitle("Login");
            		loginfield.setEnabled(true);
               	  	passwordfield.setEnabled(true);    
            		ba.notifyDataSetChanged();
            	}	
            	
            	new ClassDatabase(Preferences.this).deleteDb();
            	return true;
            	
            }

    });
      
      if(ConnectionHelper.cookieHasBeenSet() && loginButton.isEnabled())
      {
    	  loginButton.setTitle("Logout");
    	  loginfield.setEnabled(false);
    	  passwordfield.setEnabled(false);
      }
      else
      {
    	  loginButton.setTitle("Login");
    	  loginfield.setEnabled(true);
    	  passwordfield.setEnabled(true);      
    }
      loginButton.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			// TODO Auto-generated method stub
			return false;
		}
      });
      
      
      loginButton.setOnPreferenceClickListener(new OnPreferenceClickListener(){
    	  
    	  public boolean onPreferenceClick(Preference preference)
    	  {
    		  SharedPreferences settings = preference.getSharedPreferences();
    		 
    		 if(preference.getTitle().equals("Login"))
    		 {
    			 if( !settings.contains("eid") || 
        				 !settings.contains("password") || 
        				 settings.getString("eid", "error").equals("") ||
        				 settings.getString("password", "error").equals("") )
      				 {	
        				toast.setText("Please enter your credentials to log in");
            			 toast.setDuration(Toast.LENGTH_LONG);
            			 toast.show();
        				 
        				 return true;
      				 }
    			 
    			 
    			 pd = ProgressDialog.show(Preferences.this, "", "Logging in...");
    		//     pd.setCancelable(true);
    		    
    			 ConnectionHelper ch = new ConnectionHelper(Preferences.this);
    			 DefaultHttpClient httpclient = ConnectionHelper.getThreadSafeClient();
    			 DefaultHttpClient pnahttpclient = ConnectionHelper.getThreadSafeClient();

    			 ConnectionHelper.resetPNACookie();
    			
    			 
    			
    			 
    			 
    			 
    			 new loginTask(httpclient,pnahttpclient,preference).execute(ch);
				 new PNALoginTask(httpclient,pnahttpclient,preference).execute(ch);
	 
    		 }
    		 else if(preference.getTitle().equals("Logout"))
    		 {
    			ConnectionHelper.logout(Preferences.this); 
    			Toast.makeText(Preferences.this, "You have been successfully logged out", Toast.LENGTH_SHORT).show();
    			preference.setTitle("Login");
    			loginfield.setEnabled(true);
    	    	passwordfield.setEnabled(true);  
    			ba.notifyDataSetChanged();
    		 }
    		 
    		 return true;
    	  }
      });
        
        Preference resetclassesbutton = (Preference) findPreference("resetclassesbutton");
        resetclassesbutton.setOnPreferenceClickListener(new OnPreferenceClickListener() {
        	 
            public boolean onPreferenceClick(Preference preference) {
                    new ClassDatabase(Preferences.this).deleteDb();
                    Toast.makeText(getBaseContext(), "Classes deleted!", Toast.LENGTH_SHORT).show();
                    return true;
            }
    });     
                
    }
 @Override
    public void onResume()
    {
    	super.onResume();
    	if(ConnectionHelper.cookieHasBeenSet() && loginButton.isEnabled())
        {
      	  loginButton.setTitle("Logout");
      	  loginfield.setEnabled(false);
      	  passwordfield.setEnabled(false);
      	  ba.notifyDataSetChanged();
        }
        else
        {
      	  loginButton.setTitle("Login");
      	  loginfield.setEnabled(true);
      	  passwordfield.setEnabled(true);  
      	  ba.notifyDataSetChanged();
      }
    }
    private class loginTask extends AsyncTask<Object,Integer,Boolean>
	{
		
		DefaultHttpClient pnahttpclient;
		DefaultHttpClient httpclient;
		Preference preference;
    	
    	public loginTask(DefaultHttpClient httpclient, DefaultHttpClient pnahttpclient, Preference pref)
		{
			this.httpclient = httpclient;
			this.pnahttpclient = pnahttpclient;
			this.preference = pref;
			
		}
    	@Override
    	protected Boolean doInBackground(Object... params)
		{
			boolean loginStatus = ((ConnectionHelper)params[0]).Login(Preferences.this, (DefaultHttpClient)httpclient);
			publishProgress(loginStatus?0:1);
			return loginStatus;
			
				
		}
    	@Override
		protected void onProgressUpdate(Integer... progress)
		{
			
    		switch(progress[0])
			{
    		case 1:
    			if(pd.isShowing())
	    			pd.dismiss();
    		Toast.makeText(Preferences.this, "There was an error while connecting to UTDirect, please check your internet connection and try again", Toast.LENGTH_LONG).show();
				
				break;
    		case 0:break;
			}
		}
		@Override
		protected void onPostExecute(Boolean b)
		{
			logindone = b;
			if(logindone && pnalogindone)
			{
				logindone = false;pnalogindone = false;
				
				if(!ConnectionHelper.getAuthCookie(Preferences.this, httpclient).equals("") && !ConnectionHelper.getPNAAuthCookie(Preferences.this, pnahttpclient).equals(""))
				 {
					Toast.makeText(Preferences.this, "You're now logged in; feel free to access any of the app's features", Toast.LENGTH_LONG).show();
					preference.setTitle("Logout");
					loginfield.setEnabled(false);
			    	passwordfield.setEnabled(false);
					ba.notifyDataSetChanged();
					
					edit.putBoolean("loggedin", true);
					edit.commit();
					if(pd.isShowing())
		    			pd.dismiss();
				 }
			}
		}
		
	}
    private class PNALoginTask extends AsyncTask<Object,Integer,Boolean>
	{
    	
		DefaultHttpClient pnahttpclient;
		DefaultHttpClient httpclient;
		Preference preference;
    	
    	public PNALoginTask(DefaultHttpClient httpclient, DefaultHttpClient pnahttpclient, Preference pref)
		{
			this.httpclient = httpclient;
			this.pnahttpclient = pnahttpclient;
			this.preference = pref;
			
		}
    	
    	
		@Override
		protected void onProgressUpdate(Integer... progress)
		{
			
    		switch(progress[0])
			{
    		case 1:
    			if(pd.isShowing())
	    			pd.dismiss();
    		Toast.makeText(Preferences.this, "There was an error while connecting to UT PNA, please check your internet connection and try again", Toast.LENGTH_LONG).show();
				
				break;
    		case 0:break;
			}
		}
    	
		protected Boolean doInBackground(Object... params)
		{
			boolean pnaLoginStatus = ((ConnectionHelper)params[0]).PNALogin(Preferences.this, (DefaultHttpClient)pnahttpclient);
			publishProgress(pnaLoginStatus?0:1);
			return pnaLoginStatus;
		}
		@Override
		protected void onPostExecute(Boolean b)
		{
			pnalogindone = b;
			if(logindone && pnalogindone)
			{
				logindone = false;pnalogindone = false;
				
				if(!ConnectionHelper.getAuthCookie(Preferences.this, httpclient).equals("") && !ConnectionHelper.getPNAAuthCookie(Preferences.this, pnahttpclient).equals(""))
				 {
					Toast.makeText(Preferences.this, "You're now logged in; feel free to access any of the app's features", Toast.LENGTH_LONG).show();
					preference.setTitle("Logout");
					loginfield.setEnabled(false);
			    	passwordfield.setEnabled(false);
					ba.notifyDataSetChanged();
					
					edit.putBoolean("loggedin", true);
					edit.commit();
					if(pd.isShowing())
		    			pd.dismiss();
					
				 }
			}
		}
		
	}

    
    
    
    
    
 
}
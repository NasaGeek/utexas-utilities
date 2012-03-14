package com.nasageek.UTilities;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends SherlockActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState);
    	ActionBar actionbar = getSupportActionBar();
    	actionbar.setTitle("Login");
    	actionbar.setHomeButtonEnabled(true);
	    actionbar.setDisplayHomeAsUpEnabled(true);
    	
    	WebView wv = new WebView(this);
    	
    	LoginWebViewClient wvlc = new LoginWebViewClient(this);
    	
    	wv.setWebViewClient(wvlc);
    	
    	wv.loadUrl("https://utdirect.utexas.edu");
    	setContentView(wv);
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
    	
    	
    	default: return super.onOptionsItemSelected(item);
    	}
    	return true;
    }
    
    
    
    
    
    
    
    //Old login stuff, just keeping if I feel like using it in the future
    /*public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginlayout);
        final EditText eidfield = (EditText) findViewById(R.id.edit_eid);
        final EditText passwordfield = (EditText) findViewById(R.id.edit_password);
        final Button login_button = (Button) findViewById(R.id.login_button);
        final AlertDialog.Builder empty_builder = new AlertDialog.Builder(this);
        final AlertDialog.Builder success_builder = new AlertDialog.Builder(this);
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        
        
        login_button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	if(eidfield.getText().length()==0 || passwordfield.getText().length()==0)
            	{	
	            	empty_builder.setMessage("Both fields must have something in them")
	            	       .setNeutralButton("OK", new DialogInterface.OnClickListener() {
	            	           public void onClick(DialogInterface dialog, int id) {
	            	                dialog.cancel();
	            	           }
	            	       });
	            	AlertDialog empty = empty_builder.create();
	            	empty.show();
            	}
            	else
            	{
            		
            		SharedPreferences.Editor editor = settings.edit();
            		editor.putString("eid", eidfield.getText()+"");
            		editor.putString("password",passwordfield.getText()+"");
            		editor.commit();
            		success_builder.setMessage("UT EID and password successfully saved.")
            					.setNeutralButton("Hooray!", new DialogInterface.OnClickListener()
            					{	
            						public void onClick(DialogInterface dialog, int id)
            						{
            							dialog.cancel();
            						}
            					});
            		AlertDialog success = success_builder.create();
            		success.show();
            	}
            	
            }
        });
    }*/
}
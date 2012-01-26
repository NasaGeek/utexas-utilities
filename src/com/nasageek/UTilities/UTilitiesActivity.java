package com.nasageek.UTilities;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.preference.PreferenceManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;

import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;


public class UTilitiesActivity extends Activity {
    
	ProgressDialog pd; 
	SharedPreferences settings;
	Intent about_intent;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
  //    Window win = getWindow();
        settings = PreferenceManager.getDefaultSharedPreferences(this.getBaseContext());
   //   win.setFormat(PixelFormat.RGBA_8888);
       
        setContentView(R.layout.main);
        final Intent exams = new Intent(getBaseContext(), ExamScheduleActivity.class);
        final Intent schedule = new Intent(getBaseContext(), ScheduleActivity.class);
    	final Intent balance = new Intent(getBaseContext(), BalanceActivity.class);
    	final Intent map = new Intent(getBaseContext(), CampusMapActivity.class);
    	final Intent data = new Intent(getBaseContext(), DataUsageActivity.class);
    	about_intent = new Intent(this, AboutMeActivity.class);
  	
   // 	BitmapDrawable bmd = (BitmapDrawable) getResources().getDrawable(R.drawable.main_background);
   // 	bmd.setDither(true);
       
  /*    TableLayout tl = (TableLayout) findViewById(R.id.button_table);
        tl.setBackgroundDrawable(bmd);
        */
        if(!ConnectionHelper.cookieHasBeenSet() && (!settings.contains("eid") || !settings.contains("password")||settings.getString("eid", "error").equals("")||settings.getString("password", "error").equals("")))
        {
        	AlertDialog.Builder nologin_builder = new AlertDialog.Builder(this);
        	nologin_builder.setMessage("It would seem that you are not logged into UTDirect yet, why don't we take care of that?")
        			.setCancelable(false)
        			.setPositiveButton("Ok!", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    loadSettings();
                }
            })
            .setNegativeButton("No thanks", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                     dialog.cancel();
                }
            });
        	AlertDialog nologin = nologin_builder.create();
        	nologin.show();
        }
        
        
        
        final ImageButton schedulebutton = (ImageButton) findViewById(R.id.schedule_button);
        schedulebutton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	if(!ConnectionHelper.cookieHasBeenSet() && new ClassDatabase(UTilitiesActivity.this).size()==0)// && (!settings.getBoolean("loginpref", true)||!settings.contains("eid") || !settings.contains("password")||settings.getString("eid", "error").equals("")||settings.getString("password", "error").equals("")))
            	{
            		Toast.makeText(UTilitiesActivity.this, "Please log in before using this feature",Toast.LENGTH_SHORT).show();
            	}
            	else{
            	
            	
            	
            	startActivity(schedule);
            	}
            }
            
    });
        final ImageButton balancebutton = (ImageButton) findViewById(R.id.balance_button);
        balancebutton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	if(!ConnectionHelper.cookieHasBeenSet()) /*&& 
            			(!settings.getBoolean("loginpref", true)||
            					!settings.contains("eid") || 
            					!settings.contains("password")||
            					settings.getString("eid", "error").equals("")||
            					settings.getString("password", "error").equals("")))*/
            	{
            		Toast.makeText(UTilitiesActivity.this, "Please log in before using this feature",Toast.LENGTH_SHORT).show();
            	}
            	else{
            
            	startActivity(balance);
            		}
            }
            
    });
        final ImageButton mapbutton = (ImageButton) findViewById(R.id.map_button);
        mapbutton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	
         		pd = ProgressDialog.show(UTilitiesActivity.this, "", "Loading. Please wait...");
            
            	startActivity(map);
            		
            }
            
    });
        final ImageButton databutton = (ImageButton) findViewById(R.id.data_button);
        databutton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	
            	if(!ConnectionHelper.PNACookieHasBeenSet())// && (!settings.getBoolean("loginpref", true)|| !settings.contains("eid") || !settings.contains("password")||settings.getString("eid", "error").equals("")||settings.getString("password", "error").equals("")))
            	{
            		Toast.makeText(UTilitiesActivity.this, "Please log in before using this feature",Toast.LENGTH_SHORT).show();
            	}
            	else{
            
            	startActivity(data);
            		}
            		
            }
            
    });
 /*       final ImageButton examsbutton = (ImageButton) findViewById(R.id.exams_button);
        examsbutton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	
            	if(!ConnectionHelper.cookieHasBeenSet() )//&& (!settings.getBoolean("loginpref", true)|| !settings.contains("eid") || !settings.contains("password")||settings.getString("eid", "error").equals("")||settings.getString("password", "error").equals("")))
            	{
            		Toast.makeText(UTilitiesActivity.this, "Please log in before using this feature",Toast.LENGTH_SHORT).show();
            	}
            	else
            	{
            		startActivity(exams);	
            	}
            		
            }
            
    });*/
        
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.menu, menu);
        return true;
    }
    public boolean onPrepareOptionsMenu(Menu menu)
    {
    	
    	if(settings.getBoolean("loginpref", true))
    	{
    		menu.removeItem(R.id.login);
    		menu.removeItem(11);
    	}
    	else if(ConnectionHelper.cookieHasBeenSet())
    	{
    		menu.removeItem(R.id.login);
    		menu.removeItem(11);
    		menu.add(Menu.NONE, 11, Menu.NONE, "Log out");
    	}
    	else if(!ConnectionHelper.cookieHasBeenSet())
    	{
    		menu.removeItem(R.id.login);
    		menu.removeItem(11);
    		menu.add(Menu.NONE, R.id.login, Menu.NONE, "Log in");
    	}
    	 return super.onPrepareOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	int id = item.getItemId();
    	switch(id)
    	{
    		case R.id.login:login();break;
    		case R.id.settings:loadSettings();break;
    		case R.id.about:aboutMe();break;
    		case 11:logout();break;
    	}
    	return true;
    }
    public void aboutMe()
    {
    	
    	startActivity(about_intent);
    }
    public void loadSettings()
    {
    	Intent pref_intent = new Intent(this, Preferences.class);
    	startActivity(pref_intent);
    }
    
    public void login()
    {
    	Intent login_intent = new Intent(this, LoginActivity.class);
    	startActivity(login_intent);
    }
    public void logout()
    {
    	ConnectionHelper.logout(this);
    	Toast.makeText(this, "You have been successfully logged out", Toast.LENGTH_SHORT).show();
    }
    public void onResume()
    {
    	super.onResume();
    	if(pd!=null)
    		pd.dismiss();
    }
    public void onStart()
    {
    	super.onStart();
    	if(pd!=null)
    		pd.dismiss();
    }

}
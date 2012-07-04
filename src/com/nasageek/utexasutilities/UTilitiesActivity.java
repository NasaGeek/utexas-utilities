package com.nasageek.utexasutilities;

import java.util.ArrayList;

import com.crittercism.app.Crittercism;

import org.apache.http.impl.client.DefaultHttpClient;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;


import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.preference.PreferenceManager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Transformation;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;

import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;


public class UTilitiesActivity extends SherlockActivity {
    
	ProgressDialog pd; 
	SharedPreferences settings;
	Intent about_intent;
	private Menu menu;
	ActionBar actionbar;
	Toast message;
	 AnimationDrawable frameAnimation;
	 ConnectionHelper.loginTask lt;
	 ConnectionHelper.PNALoginTask plt;
	 ConnectionHelper.bbLoginTask bblt;
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crittercism.init(getApplicationContext(), "4fed1764be790e4597000001");
  //    Window win = getWindow();
        settings = PreferenceManager.getDefaultSharedPreferences(this.getBaseContext());
   //   win.setFormat(PixelFormat.RGBA_8888);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        
        setContentView(R.layout.main);
        setSupportProgressBarIndeterminateVisibility(false);
        final Intent schedule = new Intent(getBaseContext(), ScheduleActivity.class);
    	final Intent balance = new Intent(getBaseContext(), BalanceActivity.class);
  //  	final Intent map = new Intent(getBaseContext(), CampusMapActivity.class);
    	final Intent data = new Intent(getBaseContext(), DataUsageActivity.class);
    	final Intent menu = new Intent(getBaseContext(), MenuActivity.class);
    	final Intent blackboard = new Intent(getBaseContext(), BlackboardActivity.class);
    	about_intent = new Intent(this, AboutMeActivity.class);
    	
    	actionbar = getSupportActionBar();
    	
    	message = Toast.makeText(this, R.string.login_first, Toast.LENGTH_SHORT);
    	
    	
    	
    	if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)	
    		actionbar.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.actionbar_bg));
   // 	BitmapDrawable bmd = (BitmapDrawable) getResources().getDrawable(R.drawable.main_background);
   // 	bmd.setDither(true);
       
  /*    TableLayout tl = (TableLayout) findViewById(R.id.button_table);
        tl.setBackgroundDrawable(bmd);
        */
        if(!settings.contains("firstRun"))
        {
        	AlertDialog.Builder nologin_builder = new AlertDialog.Builder(this);
        	nologin_builder.setMessage("This is your first time running UTilities; why don't you try logging in to get the most use out of the app?")
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
        	settings.edit().putBoolean("firstRun", false).commit();
        }
    	
    	
   /* 	if(!ConnectionHelper.cookieHasBeenSet() && (!settings.contains("eid") || !settings.contains("password")||settings.getString("eid", "error").equals("")||settings.getString("password", "error").equals("")))
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
        }*/
        if(settings.getBoolean("autologin", false) && !ConnectionHelper.cookieHasBeenSet() && !ConnectionHelper.isLoggingIn())
        {
        	login(); 
        }
        
        final ImageButton schedulebutton = (ImageButton) findViewById(R.id.schedule_button);
    //    schedulebutton.setBackgroundResource(R.drawable.schedule_button_anim);
    //    AlphaAnimation aa = new AlphaAnimation(0.0f,1.0f);
        
    //    Transformation tran = new Transformation();
        
  //      tran.setTransformationType(Transformation.TYPE_ALPHA);
        // Get the background, which has been compiled to an AnimationDrawable object.
    //    frameAnimation = (AnimationDrawable) schedulebutton.getBackground();
        
        // Start the animation (looped playback by default).
        
        schedulebutton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
          //  	frameAnimation.start();
            	if((!ConnectionHelper.cookieHasBeenSet() && new ClassDatabase(UTilitiesActivity.this).size()==0) || ConnectionHelper.isLoggingIn())// && (!settings.getBoolean("loginpref", true)||!settings.contains("eid") || !settings.contains("password")||settings.getString("eid", "error").equals("")||settings.getString("password", "error").equals("")))
            	{
            		message.setText(R.string.login_first);
                	message.setDuration(Toast.LENGTH_SHORT);
            		message.show();
            	}
            	
            	else
            	{
            		startActivity(schedule);
            	}
            }
            
    });
        final ImageButton balancebutton = (ImageButton) findViewById(R.id.balance_button);
        balancebutton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	if(!ConnectionHelper.cookieHasBeenSet() || ConnectionHelper.isLoggingIn()) /*&& 
            			(!settings.getBoolean("loginpref", true)||
            					!settings.contains("eid") || 
            					!settings.contains("password")||
            					settings.getString("eid", "error").equals("")||
            					settings.getString("password", "error").equals("")))*/
            	{
            		message.setText(R.string.login_first);
                	message.setDuration(Toast.LENGTH_SHORT);
            		message.show();
            	}
            	else{
            
            	startActivity(balance);
            		}
            }
            
    });
        final ImageButton mapbutton = (ImageButton) findViewById(R.id.map_button);
        mapbutton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	
         //		pd = ProgressDialog.show(UTilitiesActivity.this, "", "Loading. Please wait...");
            
      //      	startActivity(map);
            		
            }
            
    });
        final ImageButton databutton = (ImageButton) findViewById(R.id.data_button);
        databutton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	
            	if(!ConnectionHelper.PNACookieHasBeenSet() || ConnectionHelper.isLoggingIn())// && (!settings.getBoolean("loginpref", true)|| !settings.contains("eid") || !settings.contains("password")||settings.getString("eid", "error").equals("")||settings.getString("password", "error").equals("")))
            	{
            		message.setText(R.string.login_pna_first);
                	message.setDuration(Toast.LENGTH_SHORT);
            		message.show();
            	}
            	else{
            
            	startActivity(data);
            		}
            		
            }
            
    });
        final ImageButton menubutton = (ImageButton) findViewById(R.id.menu_button);
        menubutton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {        
           		startActivity(menu);	          		
            }
            
    });
        final ImageButton blackboardbutton = (ImageButton) findViewById(R.id.blackboard_button);
        blackboardbutton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	if(!ConnectionHelper.bbCookieHasBeenSet() || ConnectionHelper.isLoggingIn())
            	{
            		message.setText(R.string.login_bb_first);
                	message.setDuration(Toast.LENGTH_SHORT);
            		message.show();
            	}
            	else{
            
            	startActivity(blackboard);
            		}
            }
            
    });
 
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = this.getSupportMenuInflater();
        inflater.inflate(R.layout.main_menu, menu);
        
  
    	if(!ConnectionHelper.isLoggingIn())
    	{
	        if(ConnectionHelper.cookieHasBeenSet())
	    	{
	    		menu.removeGroup(R.id.log);
	    		menu.add(R.id.log, 11, Menu.NONE, "Log out");
	    		MenuItem item = menu.findItem(11);
	    		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
	    	}
	    	else if(!ConnectionHelper.cookieHasBeenSet())
	    	{
	    		menu.removeGroup(R.id.log);
	    		menu.add(R.id.log, R.id.login, Menu.NONE, "Log in");
	    		MenuItem item = menu.findItem(R.id.login);
	    		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
	    	}
    	
    	}
    	else if(ConnectionHelper.isLoggingIn())
    	{
    		menu.removeGroup(R.id.log);
    		menu.add(R.id.log, 12, Menu.NONE, "Cancel");
    		MenuItem item = menu.findItem(12);
    		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    	}
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	int id = item.getItemId();
    	switch(id)
    	{
    		case R.id.login:login();invalidateOptionsMenu();break;
    		case R.id.settings:loadSettings();break;
    		case R.id.about:aboutMe();break;
    		case 11:logout();invalidateOptionsMenu();break;
    		case 12:cancelLogin();invalidateOptionsMenu();break;
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
    	if(settings.getBoolean("loginpref", false))
    	{
    		if( !settings.contains("eid") || 
      				!settings.contains("password") || 
      				 settings.getString("eid", "error").equals("") ||
      				 settings.getString("password", "error").equals("") )
			{	
  				message.setText("Please enter your credentials to log in");
      			message.setDuration(Toast.LENGTH_LONG);
      			message.show();
			}
           	else
           	{
           		
           		message.setText("Logging in...");
         		message.setDuration(Toast.LENGTH_SHORT);
         		message.show();
         		ConnectionHelper.loggingIn=true;
         		
           		setSupportProgressBarIndeterminateVisibility(true);
           		
           		
           		ConnectionHelper ch = new ConnectionHelper(this);
      			DefaultHttpClient httpclient = ConnectionHelper.getThreadSafeClient();
      			DefaultHttpClient pnahttpclient = ConnectionHelper.getThreadSafeClient();

      			ConnectionHelper.resetPNACookie();

      	//		ch.new loginTask(this,httpclient,pnahttpclient).execute(ch);
      	//		ch.new PNALoginTask(this,httpclient,pnahttpclient).execute(ch);
      			bblt = ch.new bbLoginTask(this, httpclient, pnahttpclient);
      			bblt.execute(ch);
      			lt = ch.new loginTask(this,httpclient,pnahttpclient);
      			lt.execute(ch);
      			plt = ch.new PNALoginTask(this,httpclient,pnahttpclient);
      			plt.execute(ch);
           	}
  		
    	}
    	else
    	{
	    	Intent login_intent = new Intent(this, LoginActivity.class);
	    	startActivity(login_intent);
    	}
    }
    public void cancelLogin()
    {
    	lt.cancel(true);
   		plt.cancel(true);
 		bblt.cancel(true);
   		message.setText("Cancelled");
    	ConnectionHelper.logout(this);
    	setSupportProgressBarIndeterminateVisibility(false);

    }
    public void logout()
    {
    	ConnectionHelper.logout(this);
    	message.setText("You have been successfully logged out");
    	message.show();
    }
    public void onResume()
    {
    	super.onResume();
    	invalidateOptionsMenu();
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
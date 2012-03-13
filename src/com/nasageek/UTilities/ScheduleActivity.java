package com.nasageek.UTilities;

import android.graphics.Color;
import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;


import android.view.View;
import android.app.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.util.Log;
import android.util.TimingLogger;
import android.view.View.OnClickListener;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

public class ScheduleActivity extends SherlockActivity implements SlidingDrawer.OnDrawerCloseListener, SlidingDrawer.OnDrawerOpenListener, AdapterView.OnItemClickListener{
	
	private GridView gv;
	private ConnectionHelper ch;
	private WrappingSlidingDrawer sd ;
	private LinearLayout sdll;
	private ClassDatabase cdb;
	private SharedPreferences sp;
	private ClassAdapter ca;
	private DefaultHttpClient client;
	
	private ProgressBar pb;
	private LinearLayout pb_ll;
	private LinearLayout ll;
	private ImageView ci_iv;
	private TextView ci_tv;
	private Button ci_button;
	private ActionBar actionbar;
	private Menu menu;
	private classtime current_clt;
	
		
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.schedule_layout);
		ch = new ConnectionHelper(this);
		sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		cdb = new ClassDatabase(this);
		sd = (WrappingSlidingDrawer) findViewById(R.id.drawer);
	    sdll = (LinearLayout) findViewById(R.id.llsd);
	    
	    ci_iv = (ImageView) findViewById(R.id.class_info_color);
	    ci_tv = (TextView) findViewById(R.id.class_info_text);
  //    ci_button = (Button) findViewById(R.id.class_locate_button);
	    
	    pb_ll = (LinearLayout) findViewById(R.id.schedule_progressbar_ll);
	    gv = (GridView) findViewById(R.id.scheduleview);
		ll = (LinearLayout) findViewById(R.id.schedule_ll);
		actionbar = getSupportActionBar();
		
		ca = new ClassAdapter(this,sd,sdll,ci_iv,ci_tv,ci_button);
		
		
		actionbar.setTitle("Schedule");
		actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
	    actionbar.setHomeButtonEnabled(true);
	    actionbar.setDisplayHomeAsUpEnabled(true);
		
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler(){
		public void uncaughtException(Thread thread, Throwable ex)
		{
			// TODO Auto-generated method stub
	//		Log.e("UNCAUGHT",ex.getMessage(),ex);
			finish();
			return;
		}});
	
		Cursor sizecheck = cdb.getReadableDatabase().query("classes", null, null, null, null, null, null);
		
		
	//		TimingLogger timings = new TimingLogger("Timing", "talk to website, parse, add classes");
		if (sizecheck.getCount()<1)
		{	
			sizecheck.close();
		
			//	Log.d("SCHEDULE", "parsing");
			//	timings.addSplit("split");
			    parser();
			//  timings.addSplit("finished");
		}
		else
		{
			sizecheck.close();
			ca.updateTime();
	//		gv.setOnItemLongClickListener(ca);
			gv.setOnItemClickListener(this);
		    gv.setAdapter(ca);
		    pb_ll.setVisibility(GridView.GONE);
			gv.setVisibility(GridView.VISIBLE);
		    if(!this.isFinishing())
		    	Toast.makeText(this, "Tap a class to see its information.", Toast.LENGTH_LONG).show();
		}
		    
	   sd.setOnDrawerCloseListener(this);
	   sd.setOnDrawerOpenListener(this);
       sd.setVisibility(View.INVISIBLE);
	}
	

		public void parser()
	    {

			client = ConnectionHelper.getThreadSafeClient();
			new parseTask(client).execute();
			
	    }
		private class parseTask extends AsyncTask<Object,Void,Object>
		{
			private DefaultHttpClient client;
			
			public parseTask(DefaultHttpClient client)
			{
				this.client = client;
			}
			
			@Override
			protected Object doInBackground(Object... params)
			{
				Document doc = null;

			    	try{
			    		doc = Jsoup.connect("https://utdirect.utexas.edu/registration/classlist.WBX")
			    				.cookie("SC", ConnectionHelper.getAuthCookie(ScheduleActivity.this, client))
			    				.get();}
			    	catch(Exception e)
			    	{
			    //		Log.d("JSOUP", "Jsoup could not connect to utexas.edu");
			    		Log.d("JSOUP EXCEPTION",e.getMessage());
			    		finish();
			    		return null;
			    	}
			
		    	Elements classels  = doc.select("div[align]").get(0).select("tr[valign]");
		    	
		    	
		    	for(int i = 0; i<classels.size(); i++)
		    	{
		    		Element temp = classels.get(i);
		    		Element uniqueid = temp.child(0);
		    		Element classid = temp.child(1);
		    		Element classname = temp.child(2);
		    		
		    		Element building = temp.child(3);
		    		String[] buildings = building.text().split(" ");
		    		
		    		Element room = temp.child(4);
		    		String[] rooms = room.text().split(" ");
		    		
		    		Element day = temp.child(5);
		    		String[] days = day.text().split(" ");
		    		for(int a = 0; a<days.length;a++) days[a] = days[a].replaceAll("TH", "H");
		    		
		    		Element time = temp.child(6);
		    		String tempstr = time.text().replaceAll("- ","-");
		    		String[] times = tempstr.split(" ");
		    		
		    		cdb.addClass(new UTClass(uniqueid.ownText(),classid.ownText(), classname.ownText(),buildings, rooms, days, times));
		    	}
		    	return null;
				
			}
			protected void onPostExecute(Object result)
			{
				ca = new ClassAdapter(ScheduleActivity.this,sd,sdll,ci_iv,ci_tv,ci_button);
				ca.updateTime();
				
				
		//		gv.setOnItemLongClickListener(ca);
				gv.setOnItemClickListener(ScheduleActivity.this);
			    gv.setAdapter(ca);
			
				
				pb_ll.setVisibility(GridView.GONE);
				gv.setVisibility(GridView.VISIBLE);
				
				
				if(!ScheduleActivity.this.isFinishing())
			    	Toast.makeText(ScheduleActivity.this, "Tap a class to see its information.\nTap and hold to see the class on a map.", Toast.LENGTH_LONG).show();
				
			}
		}
		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			
			MenuInflater inflater = this.getSupportMenuInflater();
	        inflater.inflate(R.layout.schedule_menu, menu);
			this.menu = menu;
			if(current_clt == null)
				menu.removeItem(R.id.locate_class);
			return true;
		}
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
	    	
	    	case R.id.locate_class:
	    		Intent map = new Intent(getString(R.string.building_intent), null, this, CampusMapActivity.class);
				map.setData(Uri.parse(current_clt.getBuilding().getId()));
				startActivity(map);break;
	    	default: return super.onOptionsItemSelected(item);
	    	}
	    	return true;
	    }
		@Override
		public void onResume()
		{
			super.onResume();
			ca.updateTime();
			gv.invalidateViews(); 		
		}

		public void onDrawerClosed()
		{
			// TODO Auto-generated method stub
		
			((ImageView)(sd.getHandle())).setImageResource(R.drawable.ic_expand_half);
		}
		public void onDrawerOpened()
		{
			// TODO Auto-generated method stub
			((ImageView)(sd.getHandle())).setImageResource(R.drawable.ic_collapse_half);
		}


		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			// TODO Auto-generated method stub
			
			
			sd.close();
		//	sdll.removeAllViews();
			current_clt = (classtime) parent.getItemAtPosition(position);
			if(current_clt!=null)
			{
		//		menu.add(R.id.locate_class);
				this.invalidateOptionsMenu();
	
	
				sd.setVisibility(View.VISIBLE);
				//Cursor cur = cdb.getReadableDatabase().query("classes", null, "eid = \"" + sp.getString("eid", "eid not found")+"\" AND day = \""+ clt.getDay()+"\" AND start = \""+ clt.getStartTime()+"\"", null,null,null,null);
				Cursor cur = cdb.getReadableDatabase().query("classes", null, "day = \""+ current_clt.getDay()+"\" AND start = \""+ current_clt.getStartTime()+"\"", null,null,null,null);
			    cur.moveToFirst();
			    while(!cur.isAfterLast())
			    {
			    	String text = " ";
			    	text+=cur.getString(3)+" - "+cur.getString(4)+" ";
			    	String unique = cur.getString(2);
			    	while(!cur.isAfterLast() && unique.equals(cur.getString(2)))
			    	{
			    		String daytext = "\n\t";
			    		String building = cur.getString(5)+" "+cur.getString(6);
			    		String checktext = cur.getString(8)+building;
			    		String time = cur.getString(8);
			    		String end = cur.getString(9);
			    		while(!cur.isAfterLast() && checktext.equals(cur.getString(8)+cur.getString(5)+" "+cur.getString(6)) )
			    		{
			    			if(cur.getString(7).equals("H"))
			    				daytext+="TH";
			    			else
			    				daytext+=cur.getString(7);
			    			cur.moveToNext();
			    		}
			    		
			    		text+=(daytext+" from " + time + "-"+end + " in "+building);
			
			    	}
			    	text+="\n";
			  //  	ImageView iv = new ImageView(currentContext);
			    	ci_iv.setBackgroundColor(Color.parseColor("#"+cdb.getColor(current_clt.getUnique(),current_clt.getStartTime(), current_clt.getDay()+"")));
			    	ci_iv.setMinimumHeight(10);
			    	ci_iv.setMinimumWidth(10);
			    	
		    		ci_tv.setTextColor(Color.BLACK);
		    		ci_tv.setTextSize((float) 15);
		    		ci_tv.setBackgroundColor(0x99F0F0F0);
		    		ci_tv.setText(text);
		 //   		sdll.addView(iv);
		 //  		sdll.addView(tv);

		    	}
			//    sdll.addView(button);
			    
			    sd.open();
			    
			    
			}
			else
			{	menu.removeItem(R.id.locate_class);
				sd.setVisibility(View.INVISIBLE);
				this.invalidateOptionsMenu();}
		//	Log.d("CLICKY", position+"");
		}
		
}

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

import android.view.View;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TimingLogger;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

public class ScheduleActivity extends Activity implements SlidingDrawer.OnDrawerCloseListener, SlidingDrawer.OnDrawerOpenListener{
	
	private GridView gv;
	private ConnectionHelper ch;
	private WrappingSlidingDrawer sd ;
	private LinearLayout sdll;
	private ClassDatabase cdb;
	private SharedPreferences sp;
	private ClassAdapter ca;
	private DefaultHttpClient client;
	private ProgressDialog pd;
	
		
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.schedulelayout);
		ch = new ConnectionHelper(this);
		sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		cdb = new ClassDatabase(this);
		sd = (WrappingSlidingDrawer) findViewById(R.id.drawer);
	    sdll = (LinearLayout) findViewById(R.id.llsd);
	    ca = new ClassAdapter(this,sd,sdll);
	    gv = (GridView) findViewById(R.id.scheduleview);
			
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
			pd = ProgressDialog.show(this, "", "Loading. Please wait...");
			//	Log.d("SCHEDULE", "parsing");
			//	timings.addSplit("split");
			    parser();
			//  timings.addSplit("finished");
		}
		else
		{
			sizecheck.close();
			ca.updateTime();
			gv.setOnItemLongClickListener(ca);
			gv.setOnItemClickListener(ca);
		    gv.setAdapter(ca);
		    if(!this.isFinishing())
		    	Toast.makeText(this, "Tap a class to see its information.\nTap and hold to see the class on a map.", Toast.LENGTH_LONG).show();
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
		    		
		    		cdb.addClass(new Class(uniqueid.ownText(),classid.ownText(), classname.ownText(),buildings, rooms, days, times));
		    	}
		    	return null;
				
			}
			protected void onPostExecute(Object result)
			{
				ca = new ClassAdapter(ScheduleActivity.this,sd,sdll);
				ca.updateTime();
				
				
				gv.setOnItemLongClickListener(ca);
				gv.setOnItemClickListener(ca);
			    gv.setAdapter(ca);
				if(pd.isShowing())
					pd.dismiss();
				if(!ScheduleActivity.this.isFinishing())
			    	Toast.makeText(ScheduleActivity.this, "Tap a class to see its information.\nTap and hold to see the class on a map.", Toast.LENGTH_LONG).show();
			}
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
		
}

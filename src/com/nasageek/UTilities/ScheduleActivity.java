package com.nasageek.UTilities;

import android.graphics.Color;
import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.viewpagerindicator.TabPageIndicator;


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

import android.support.v4.view.ViewPager;
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

public class ScheduleActivity extends SherlockFragmentActivity implements SlidingDrawer.OnDrawerCloseListener, SlidingDrawer.OnDrawerOpenListener, AdapterView.OnItemClickListener, ViewPager.OnPageChangeListener{
	
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
	
	private ActionBar actionbar;
	private Menu menu;
	private classtime current_clt;
	private ActionMode mode;
	private PagerAdapter mPagerAdapter;
	private List<SherlockFragment> fragments;
	
		
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.schedule_layout);
		initialisePaging();
		ch = new ConnectionHelper(this);
		sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
//		cdb = new ClassDatabase(this);
//		sd = (WrappingSlidingDrawer) findViewById(R.id.drawer);
//	    sdll = (LinearLayout) findViewById(R.id.llsd);
	    
//	    ci_iv = (ImageView) findViewById(R.id.class_info_color);
//	    ci_tv = (TextView) findViewById(R.id.class_info_text);

	    
//	    pb_ll = (LinearLayout) findViewById(R.id.schedule_progressbar_ll);
//	    gv = (GridView) findViewById(R.id.scheduleview);
//		ll = (LinearLayout) findViewById(R.id.schedule_ll);
		actionbar = getSupportActionBar();
		
//		ca = new ClassAdapter(this,sd,sdll,ci_iv,ci_tv);
		
		
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
	
/*		Cursor sizecheck = cdb.getReadableDatabase().query("classes", null, null, null, null, null, null);

		if (sizecheck.getCount()<1)
		{	
			sizecheck.close();
		
			//	Log.d("SCHEDULE", "parsing");
			    parser();
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
       sd.setVisibility(View.INVISIBLE); */
		}
		private void initialisePaging() 
		{
			
			fragments = new Vector<SherlockFragment>();
	        Bundle args = new Bundle(1);
	        args.putString("title", "Course Schedule");
	        fragments.add((SherlockFragment)SherlockFragment.instantiate(this, CourseScheduleFragment.class.getName(), args));
	        args = new Bundle(1);
	        args.putString("title", "Exam Schedule");
	        fragments.add((SherlockFragment)SherlockFragment.instantiate(this, ExamScheduleFragment.class.getName(), args));
	
	      
	        this.mPagerAdapter  = new PagerAdapter(getSupportFragmentManager(), fragments);	
	        ViewPager pager = (ViewPager)findViewById(R.id.viewpager);
	        pager.setPageMargin(2);
	        pager.setAdapter(this.mPagerAdapter);
	        TabPageIndicator tabIndicator = (TabPageIndicator)findViewById(R.id.titles);
			tabIndicator.setViewPager(pager);
			
			tabIndicator.setOnPageChangeListener(this);
						
	   }
		@Override
		public void onPause()
		{
			super.onPause();
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
				ca = new ClassAdapter(ScheduleActivity.this,sd,sdll,ci_iv,ci_tv);
				ca.updateTime();
				
				
		//		gv.setOnItemLongClickListener(ca);
				gv.setOnItemClickListener(ScheduleActivity.this);
			    gv.setAdapter(ca);
			
				
				pb_ll.setVisibility(GridView.GONE);
				gv.setVisibility(GridView.VISIBLE);
				
				
				if(!ScheduleActivity.this.isFinishing())
			    	Toast.makeText(ScheduleActivity.this, "Tap a class to see its information.", Toast.LENGTH_SHORT).show();
				
				}
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
	    	
	    	default: return super.onOptionsItemSelected(item);
	    	}
	    	return true;
	    }
/*		@Override
		public void onResume()
		{
			super.onResume();
			ca.updateTime();
			gv.invalidateViews(); 		
		}*/

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
				mode = startActionMode(new ScheduleActionMode());
	
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
			    	ci_iv.setBackgroundColor(Color.parseColor("#"+cdb.getColor(current_clt.getUnique(),current_clt.getStartTime(), current_clt.getDay()+"")));
			    	ci_iv.setMinimumHeight(10);
			    	ci_iv.setMinimumWidth(10);
			    	
		    		ci_tv.setTextColor(Color.BLACK);
		    		ci_tv.setTextSize((float) 14);
		    		ci_tv.setBackgroundColor(0x99F0F0F0);
		    		ci_tv.setText(text);

		    	}
			    sd.open();    
			}
			else
			{	if(mode!=null)
					mode.finish();
				//menu.removeItem(R.id.locate_class);
				sd.setVisibility(View.INVISIBLE);
				this.invalidateOptionsMenu();}
		//	Log.d("CLICKY", position+"");
		}
		
		private final class ScheduleActionMode extends ViewPager.SimpleOnPageChangeListener implements ActionMode.Callback {
	        @Override
	        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
	            mode.setTitle("Class Info");
	            MenuInflater inflater = getSupportMenuInflater();
	            inflater.inflate(R.layout.schedule_menu, menu);
	            return true;
	        }

	        @Override
	        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
	            return false;
	        }

	        @Override
	        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
	            switch(item.getItemId())
	            {
	            	case R.id.locate_class:
	            		Intent map = new Intent(getString(R.string.building_intent), null, ScheduleActivity.this, CampusMapActivity.class);
	    				map.setData(Uri.parse(current_clt.getBuilding().getId()));
	    				startActivity(map);break;
	            }
	            return true;
	        }

	        @Override
	        public void onDestroyActionMode(ActionMode mode) {
	        }
		}
		
		@Override
		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub
			
		}

		// TODO not sure if I want this yet, maybe a more elegant solution?
		@Override
		public void onPageSelected(int location) {
			// TODO Auto-generated method stub
			
			for(SherlockFragment csf : fragments)
				if(((ActionModeFragment)csf).getActionMode()!=null)
					((ActionModeFragment)csf).getActionMode().finish();
		}
		
}

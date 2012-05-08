package com.nasageek.UTilities;



import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;



public class CourseScheduleFragment extends ActionModeFragment implements SlidingDrawer.OnDrawerCloseListener, SlidingDrawer.OnDrawerOpenListener, AdapterView.OnItemClickListener {
	
	private GridView gv;
	private ConnectionHelper ch;
	private WrappingSlidingDrawer sd ;
	private LinearLayout sdll;
	private ClassDatabase cdb;
	private SharedPreferences sp;
	private ClassAdapter ca;
	private DefaultHttpClient client;
	
	
	private LinearLayout pb_ll;
	private LinearLayout ll;
	private ImageView ci_iv;
	private TextView ci_tv;
	
	
	
	private classtime current_clt;
	public ActionMode mode;
	private View vg;
	private SherlockFragmentActivity parentAct;
	String semId;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		vg =  inflater.inflate(R.layout.course_schedule_fragment_layout, container, false);
		
		//updateView(semId);

		return vg;	
	}
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		parentAct = this.getSherlockActivity();
		semId = getArguments().getString("semId");
		ch = new ConnectionHelper(parentAct);
		sp = PreferenceManager.getDefaultSharedPreferences(parentAct.getBaseContext());
		cdb = new ClassDatabase(parentAct);
	}
	public void updateView(String semId)
	{
		this.semId = semId;
		
		sd = (WrappingSlidingDrawer) vg.findViewById(R.id.drawer);
	    sdll = (LinearLayout) vg.findViewById(R.id.llsd);
	    
	    ci_iv = (ImageView) vg.findViewById(R.id.class_info_color);
	    ci_tv = (TextView) vg.findViewById(R.id.class_info_text);

	    
	    pb_ll = (LinearLayout) vg.findViewById(R.id.schedule_progressbar_ll);
	    gv = (GridView) vg.findViewById(R.id.scheduleview);
		ll = (LinearLayout) vg.findViewById(R.id.schedule_ll);
		
		cdb.resetColorCount();
		
		Cursor sizecheck = cdb.getReadableDatabase().query("classes", null, "semester = \""+this.semId+"\"" , null, null, null, null);
		
		if (sizecheck.getCount()<1)
		{	
			sizecheck.close();
			client = ConnectionHelper.getThreadSafeClient();
			new parseTask(client).execute();	   
		}
		else
		{
			sizecheck.close();
			ca = new ClassAdapter(parentAct,sd,sdll,ci_iv,ci_tv,semId);
			ca.updateTime();
			gv.setOnItemClickListener(this);
		    gv.setAdapter(ca);
		    pb_ll.setVisibility(GridView.GONE);
		    
			gv.setVisibility(GridView.VISIBLE);
		    if(!parentAct.isFinishing())
		    	Toast.makeText(parentAct, "Tap a class to see its information.", Toast.LENGTH_LONG).show();
		}
		sd.setOnDrawerCloseListener(this);
		sd.setOnDrawerOpenListener(this);
	    sd.setVisibility(View.INVISIBLE);
	}
	@Override
	public void onResume()
	{
		super.onResume();
		if(ca != null)
			ca.updateTime();
		if(gv != null)
			gv.invalidateViews(); 		
	}

	public ActionMode getActionMode()
	{
		return mode;
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
			mode = parentAct.startActionMode(new ScheduleActionMode());

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
		    cur.close();
		    sd.open();    
		}
		else
		{	if(mode!=null)
				mode.finish();
			//menu.removeItem(R.id.locate_class);
			sd.setVisibility(View.INVISIBLE);
			
	//	Log.d("CLICKY", position+"");
		}
	}
	
	private class parseTask extends AsyncTask<Object,Void,Object>
	{
		private DefaultHttpClient client;
		
		public parseTask(DefaultHttpClient client)
		{
			this.client = client;
		}
		@Override
		protected void onPreExecute()
		{
			((ScheduleActivity)parentAct).spinner.setClickable(false);
			((ScheduleActivity)parentAct).spinner.setActivated(false);
			pb_ll.setVisibility(GridView.VISIBLE);
			gv.setVisibility(GridView.GONE);
		}
		
		@Override
		protected Object doInBackground(Object... params)
		{
			Document doc = null;

		    	try
		    	{
		    		doc = Jsoup.connect("https://utdirect.utexas.edu/registration/classlist.WBX?sem="+semId)
		    				.cookie("SC", ConnectionHelper.getAuthCookie(parentAct, client))
		    				.get();
		    	}
		    	catch(Exception e)
		    	{
		    //		Log.d("JSOUP", "Jsoup could not connect to utexas.edu");
		    		Log.d("JSOUP EXCEPTION",e.getMessage());
		    		
		    		parentAct.finish();
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
	    		
	    		cdb.addClass(new UTClass(uniqueid.ownText(),classid.ownText(), classname.ownText(),buildings, rooms, days, times, semId));
	    	}
	    	return null;
			
		}
		protected void onPostExecute(Object result)
		{
			ca = new ClassAdapter(parentAct,sd,sdll,ci_iv,ci_tv,semId);
			ca.updateTime();
			
			
	//		gv.setOnItemLongClickListener(ca);
			gv.setOnItemClickListener(CourseScheduleFragment.this);
		    gv.setAdapter(ca);
		
	//	    ((ScheduleActivity)parentAct).spinner.setEnabled(true);
			pb_ll.setVisibility(GridView.GONE);
			gv.setVisibility(GridView.VISIBLE);
			
			
			if(!parentAct.isFinishing())
		    	Toast.makeText(parentAct, "Tap a class to see its information.", Toast.LENGTH_SHORT).show();
			
		}
	}
	
	private final class ScheduleActionMode implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.setTitle("Class Info");
            MenuInflater inflater = ((SherlockFragmentActivity)getActivity()).getSupportMenuInflater();
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
            		Intent map = new Intent(getString(R.string.building_intent), null, parentAct, CampusMapActivity.class);
    				map.setData(Uri.parse(current_clt.getBuilding().getId()));
    				startActivity(map);break;
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
        }
    }
	
}



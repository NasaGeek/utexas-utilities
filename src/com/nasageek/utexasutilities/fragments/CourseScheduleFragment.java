package com.nasageek.utexasutilities.fragments;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.crittercism.app.Crittercism;
import com.nasageek.utexasutilities.ConnectionHelper;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.UTClass;
import com.nasageek.utexasutilities.WrappingSlidingDrawer;
import com.nasageek.utexasutilities.Classtime;
import com.nasageek.utexasutilities.activities.CampusMapActivity;
import com.nasageek.utexasutilities.activities.ScheduleActivity;
import com.nasageek.utexasutilities.adapters.ClassAdapter;

public class CourseScheduleFragment extends SherlockFragment implements ActionModeFragment, SlidingDrawer.OnDrawerCloseListener, SlidingDrawer.OnDrawerOpenListener, AdapterView.OnItemClickListener {
	
	private GridView gv;
	private WrappingSlidingDrawer sd ;
	private LinearLayout sdll;
//	private ClassDatabase cdb;
	private ClassAdapter ca;
	private DefaultHttpClient client;
	private String[] colors = {"488ab0","00b060","b56eb3","94c6ff","81b941","ff866e","ffad46","ffe45e"};
	
	private Menu mMenu;
	private LinearLayout pb_ll;
	private LinearLayout daylist;
	private ImageView ci_iv;
	private TextView ci_tv;
	private TextView nc_tv;
	private TextView etv;
	
	private ArrayList<UTClass> classList;
	private Classtime current_clt;
	public ActionMode mode;

	private SherlockFragmentActivity parentAct;
	String semId;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View vg =  inflater.inflate(R.layout.course_schedule_fragment_layout, container, false);
		
		updateView(semId, vg);

		return vg;	
	}
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		parentAct = this.getSherlockActivity();
		semId = getArguments().getString("semId");
	}
	public void updateView(String semId, View vg)
	{
		this.semId = semId;
		
		sd = (WrappingSlidingDrawer) vg.findViewById(R.id.drawer);
	    sdll = (LinearLayout) vg.findViewById(R.id.llsd);
	    
	    ci_iv = (ImageView) vg.findViewById(R.id.class_info_color);
	    ci_tv = (TextView) vg.findViewById(R.id.class_info_text);
	    etv = (TextView) vg.findViewById(R.id.schedule_error);
	    
	    pb_ll = (LinearLayout) vg.findViewById(R.id.schedule_progressbar_ll);
	    nc_tv = (TextView) vg.findViewById(R.id.no_courses);
	    gv = (GridView) vg.findViewById(R.id.scheduleview);
		daylist = (LinearLayout) vg.findViewById(R.id.daylist);
	
		client = ConnectionHelper.getThreadSafeClient();
		new parseTask(client).execute();
		
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
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) 
	{
		this.mMenu = menu;
		menu.removeItem(R.id.map_all_classes);
	    inflater.inflate(R.menu.schedule_menu, menu);
	   
	}
	@Override
	public void onPrepareOptionsMenu(Menu menu)
	{
		if(classList == null || classList.size() == 0)
		{
			menu.findItem(R.id.map_all_classes).setEnabled(false);
		}
		else
			menu.findItem(R.id.map_all_classes).setEnabled(true);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
    {
    	int id = item.getItemId();
    	switch(id)
    	{
    	case R.id.map_all_classes:
    		//check to see if we're done loading the schedules (the ClassAdapter is initialized in onPostExecute)
    		if(ca != null)
    		{
    			//populate an array with the buildings IDs of all of the user's classtimes
	    		ArrayList<String> buildings = new ArrayList<String>();
	    		
				for(UTClass clz : classList)
				{
					for(Classtime clt : clz.getClassTimes())
	    				if(!buildings.contains(clt.getBuilding().getId()))
	    					buildings.add(clt.getBuilding().getId());
				}
	    		
	    		Intent map = new Intent(getString(R.string.building_intent), null, parentAct, CampusMapActivity.class);
		//		map.setData(Uri.parse(current_clt.getBuilding().getId()));
				map.putStringArrayListExtra("buildings", buildings);
				startActivity(map);
				break;
    		}
    	
    	
    	default: return super.onOptionsItemSelected(item);
    	}
    	return true;
    }
	@Override
	public ActionMode getActionMode()
	{
		return mode;
	}
	@Override
	public void onDrawerClosed()
	{
		((ImageView)(sd.getHandle())).setImageResource(R.drawable.ic_expand_half);
	}
	@Override
	public void onDrawerOpened()
	{
		((ImageView)(sd.getHandle())).setImageResource(R.drawable.ic_collapse_half);
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		sd.close();
	//	sdll.removeAllViews();
		current_clt = (Classtime) parent.getItemAtPosition(position);
		if(current_clt!=null)
		{
			mode = parentAct.startActionMode(new ScheduleActionMode());

			sd.setVisibility(View.VISIBLE);
			//Cursor cur = cdb.getReadableDatabase().query("classes", null, "eid = \"" + sp.getString("eid", "eid not found")+"\" AND day = \""+ clt.getDay()+"\" AND start = \""+ clt.getStartTime()+"\"", null,null,null,null);
//			Cursor cur = cdb.getReadableDatabase().query("classes", null, "day = \""+ current_clt.getDay()+"\" AND start = \""+ current_clt.getStartTime()+"\"", null,null,null,null);
//		    cur.moveToFirst();
//		    while(!cur.isAfterLast())
		    {
		    	String text = " ";
		    	text+=current_clt.getUTClass().getId()+" - "+current_clt.getUTClass().getName()+" ";
		    	String unique = current_clt.getUTClass().getUnique();
		  //  	while(!cur.isAfterLast() && unique.equals(cur.getString(2)))
		  //  	{
		    		String daytext = "\n\t";
		    		String building = current_clt.getBuilding().getId()+" "+current_clt.getBuilding().getRoom();
		    		String checktext = current_clt.getStartTime()+building;
		    		String time = current_clt.getStartTime();
		    		String end = current_clt.getEndTime();
		   // 		while(!cur.isAfterLast() && checktext.equals(cur.getString(8)+cur.getString(5)+" "+cur.getString(6)) )
		   // 		{
		    			if(current_clt.getDay()=='H')
		    				daytext+="TH";
		    			else
		    				daytext+=current_clt.getDay();
		 //   			cur.moveToNext();
		//    		}
		    		
		    		text+=(daytext+" from " + time + "-"+end + " in "+building);
		
	//	    	}
		    	text+="\n";
		    	ci_iv.setBackgroundColor(Color.parseColor("#"+current_clt.getColor()));
		    	ci_iv.setMinimumHeight(10);
		    	ci_iv.setMinimumWidth(10);
		    	
	    		ci_tv.setTextColor(Color.BLACK);
	    		ci_tv.setTextSize((float) 14);
	    		ci_tv.setBackgroundColor(0x99F0F0F0);
	    		ci_tv.setText(text);

	    	}
//		    cur.close();
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
	
	private class parseTask extends AsyncTask<Object,String,Integer>
	{
		private DefaultHttpClient client;
		private String errorMsg;
		private boolean classParseIssue = false;
		
		public parseTask(DefaultHttpClient client)
		{
			this.client = client;
		}
		@Override
		protected void onPreExecute()
		{
			pb_ll.setVisibility(GridView.VISIBLE);
			gv.setVisibility(GridView.GONE);
			
			client.getCookieStore().clear();
			
	    	BasicClientCookie cookie = new BasicClientCookie("SC", ConnectionHelper.getAuthCookie(parentAct,client));
	    	cookie.setDomain(".utexas.edu");
	    	client.getCookieStore().addCookie(cookie);
			
		}
		@Override
		protected void onProgressUpdate(String...params)
		{
			Bundle args = new Bundle(2);
	        args.putString("title", params[0].trim());
	        args.putString("semId", params[1]);
			((ScheduleActivity)getActivity()).getFragments().add((SherlockFragment)SherlockFragment.instantiate(getActivity(), CourseScheduleFragment.class.getName(),args));
			((ScheduleActivity)getActivity()).getAdapter().notifyDataSetChanged();
			((ScheduleActivity)getActivity()).getIndicator().notifyDataSetChanged();
		}
		
		@Override
		protected Integer doInBackground(Object... params)
		{
		//	Object[] result = new Object[3];
			
			HttpGet hget = new HttpGet("https://utdirect.utexas.edu/registration/classlist.WBX?sem=" +semId);
	    	String pagedata="";

		    	try
		    	{
		    		HttpResponse res = client.execute(hget);
		    		pagedata = EntityUtils.toString(res.getEntity());

		    	}
		    	catch(Exception e)
		    	{
		    		Log.d("JSOUP EXCEPTION",e.getMessage());
		    		e.printStackTrace();
		    		errorMsg = "UTilities could not fetch your class listing";
		    		cancel(true);
		    		return null;
		    	}
		    	if(pagedata.contains("<title>Information Technology Services - UT EID Logon</title>"))
		    	{
					errorMsg = "You've been logged out of UTDirect, back out and log in again.";
					if(parentAct != null)
						ConnectionHelper.logout(parentAct);
					cancel(true);
					return null;
		    	}
		    	Pattern semSelectPattern = Pattern.compile("<select  name=\"sem\">.*</select>", Pattern.DOTALL);
		    	Matcher semSelectMatcher = semSelectPattern.matcher(pagedata);
		    	
		    	//TODO: un-hardcode this eventually! Shouldn't be too hard to figure out the dropdown size
		    	if(semSelectMatcher.find() && parentAct != null && ((ScheduleActivity)getActivity()).getFragments().size()<3)
		    	{
		    		Pattern semesterPattern = Pattern.compile("<option.*?value=\"(\\d*)\"\\s*>([\\w\\s]*?)</option>", Pattern.DOTALL);
		    		Matcher semesterMatcher = semesterPattern.matcher(semSelectMatcher.group());
		    		while(semesterMatcher.find())
		    		{
		    			if(semesterMatcher.group(0).contains("selected=\"selected\""))
		    				continue;
		    			else
		    			{	
		    				publishProgress(semesterMatcher.group(2), semesterMatcher.group(1));
		    			}
		    		}	
		    	}
		    	
		    	Pattern pattern3 = Pattern.compile("<table.*</table>",Pattern.DOTALL);
		    	Matcher matcher3 = pattern3.matcher(pagedata);
		    	
		    	
		    	if(matcher3.find())
		    		pagedata = matcher3.group(0);
		    	else
		    	{
		    		//if no <table>, user probably isn't enrolled for semester
		    		return 0;
		    	}
		    	Pattern classPattern = Pattern.compile("<tr  .*?</tr>",Pattern.DOTALL);
		    	Matcher classMatcher = classPattern.matcher(pagedata);
		    	int classCount = 0, colorCount = 0;
		    	classList = new ArrayList<UTClass>();
		    	while(classMatcher.find())
		    	{
		    		String classContent = classMatcher.group();
		    		String uniqueid="", classid="", classname="";
		    		String [] buildings=null, rooms=null, days=null, times=null;
		    		Pattern classAttPattern = Pattern.compile("<td >(.*?)</td>",Pattern.DOTALL);
		    		Matcher classAttMatcher = classAttPattern.matcher(classContent);
		    		if(classAttMatcher.find())
		    			uniqueid = classAttMatcher.group(1);
		    		else
		    		{	classParseIssue = true;
		    			continue;
		    		}
		    		if(classAttMatcher.find())
		    			classid = classAttMatcher.group(1);
		    		else
		    		{	classParseIssue = true;
		    			continue;
		    		}
		    		if(classAttMatcher.find())
		    			classname =  classAttMatcher.group(1);
		    		else
		    		{	classParseIssue = true;
		    			continue;
		    		}
		    		if(classAttMatcher.find())
		    		{
		    			buildings = classAttMatcher.group(1).split("<br />");
		    			for(int i = 0; i<buildings.length; i++)
		    				buildings[i] = buildings[i].replaceAll("<.*?>", "").trim();
		    		}
		    		else
		    		{	classParseIssue = true;
		    			continue;
		    		}
		    		if(classAttMatcher.find())
		    		{
		    			rooms = classAttMatcher.group(1).split("<br />");
		    			for(int i = 0; i<rooms.length; i++)
		    				rooms[i] = rooms[i].trim();
		    		}
		    		else
		    		{	classParseIssue = true;
		    			continue;
		    		}
		    		if(classAttMatcher.find())
		    		{	
		    			days = classAttMatcher.group(1).split("<br />");
		    			for(int a = 0; a<days.length;a++) days[a] = days[a].replaceAll("TH", "H").trim();
		    		}
		    		else
		    		{	classParseIssue = true;
		    			continue;
		    		}
		    		if(classAttMatcher.find())
		    		{
		    			times = classAttMatcher.group(1).replaceAll("- ", "-").split("<br />");	
		    			for(int i = 0; i<times.length; i++)
		    				times[i] = times[i].trim();
		    		}
		    		else
		    		{	classParseIssue = true;
		    			continue;
		    		}
		    		classList.add(new UTClass(uniqueid,classid,classname,buildings, rooms, days, times, semId, colors[colorCount]));
		    		colorCount = (colorCount == colors.length-1) ? 0 : colorCount+1;
		    		classCount++;
		    	}
		    	
	
	 //   	} 
	//    	result[1] = itemSelected;
	//    	result[2] = semesters;
	    	return Integer.valueOf(classCount);
			
		}
		@Override
		protected void onPostExecute(Integer result)
		{
//			ArrayList<String> semesters = (ArrayList<String>)result[2];
			pb_ll.setVisibility(GridView.GONE);
//			parentAct.getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
//			parentAct.getSupportActionBar().setSelectedNavigationItem((Integer)result[1]);
//			((ArrayAdapter)((ScheduleActivity)parentAct).spinner.getAdapter()).clear();
/*			for(int i = 0; i<semesters.size(); i++)
			{
				switch(semesters.get(i).charAt(4))
				{
				case '2':((ArrayAdapter)((ScheduleActivity)parentAct).spinner.getAdapter()).add("Spring - "+semesters.get(i).substring(2,4));break;
				case '6':((ArrayAdapter)((ScheduleActivity)parentAct).spinner.getAdapter()).add("Summer - "+semesters.get(i).substring(2,4));break;
				case '9':((ArrayAdapter)((ScheduleActivity)parentAct).spinner.getAdapter()).add("Fall - "+semesters.get(i).substring(2,4));break;
				}
				
			}
			((ArrayAdapter)((ScheduleActivity)parentAct).spinner.getAdapter()).notifyDataSetChanged();
			*/
			if(result != null)
			{	
				if(result.intValue()==0)
				{	
					daylist.setVisibility(View.GONE);
					nc_tv.setText("You aren't enrolled for the current semester.");
					nc_tv.setVisibility(View.VISIBLE);
					if(mMenu != null)
						mMenu.findItem(R.id.map_all_classes).setEnabled(false);
					
					return;
				}
				else
				{	
					ca = new ClassAdapter(parentAct,sd,sdll,ci_iv,ci_tv,semId, classList);
					ca.updateTime(); // not really necessary
	
					gv.setOnItemClickListener(CourseScheduleFragment.this);
				    gv.setAdapter(ca);
	
					gv.setVisibility(GridView.VISIBLE);
					daylist.setVisibility(View.VISIBLE);
					if(mMenu != null)
						mMenu.findItem(R.id.map_all_classes).setEnabled(true);
					if(!parentAct.isFinishing())
				    	Toast.makeText(parentAct, "Tap a class to see its information.", Toast.LENGTH_SHORT).show();
					Crittercism.leaveBreadcrumb("Loaded schedule from web");
				}
			}
			else
			{
				errorMsg = "UTilities could not fetch your class listing";
				etv.setText(errorMsg);
				etv.setVisibility(View.VISIBLE);
				pb_ll.setVisibility(View.GONE);
				daylist.setVisibility(View.GONE);
				nc_tv.setVisibility(View.GONE);
				gv.setVisibility(View.GONE);
				if(mMenu != null)
					mMenu.findItem(R.id.map_all_classes).setEnabled(false);
			}	
			if(classParseIssue)
				Toast.makeText(parentAct, "One or more classes could not be parsed correctly, try emailing the dev ;)", Toast.LENGTH_LONG).show();
		}
		@Override
		protected void onCancelled()
		{
			etv.setText(errorMsg);
			etv.setVisibility(View.VISIBLE);
			pb_ll.setVisibility(View.GONE);
			daylist.setVisibility(View.GONE);
			nc_tv.setVisibility(View.GONE);
			gv.setVisibility(View.GONE);
		}
	}
	public ArrayList<UTClass> getClassList()
	{
		return classList;
	}
	private final class ScheduleActionMode implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) 
        {
            mode.setTitle("Class Info");
            MenuInflater inflater = parentAct.getSupportMenuInflater();
            inflater.inflate(R.menu.schedule_action_mode, menu);
            return true;
        }
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu)
        {
            return false;
        }
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item)
        {
            switch(item.getItemId())
            {
            	case R.id.locate_class:
            		ArrayList<String> building = new ArrayList<String>();
            		Intent map = new Intent(getString(R.string.building_intent), null, parentAct, CampusMapActivity.class);
            		building.add(current_clt.getBuilding().getId());
            		map.putStringArrayListExtra("buildings", building);
    			//	map.setData(Uri.parse(current_clt.getBuilding().getId()));
    				startActivity(map);
    				break;
            }
            return true;
        }
        @Override
        public void onDestroyActionMode(ActionMode mode) { }
    }
	
}
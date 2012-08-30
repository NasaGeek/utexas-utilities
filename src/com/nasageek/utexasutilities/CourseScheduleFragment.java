package com.nasageek.utexasutilities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;

import android.content.Intent;
import android.database.Cursor;
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

public class CourseScheduleFragment extends SherlockFragment implements ActionModeFragment, SlidingDrawer.OnDrawerCloseListener, SlidingDrawer.OnDrawerOpenListener, AdapterView.OnItemClickListener {
	
	private GridView gv;
	private WrappingSlidingDrawer sd ;
	private LinearLayout sdll;
	private ClassDatabase cdb;
	private ClassAdapter ca;
	private DefaultHttpClient client;
	
	private LinearLayout pb_ll;
	private LinearLayout daylist;
	private ImageView ci_iv;
	private TextView ci_tv;
	private TextView nc_tv;
	private TextView etv;
	
	private classtime current_clt;
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
		parentAct = this.getSherlockActivity();
		semId = "";
		cdb = ClassDatabase.getInstance(parentAct);
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
		
		cdb.resetColorCount();
		
		Cursor sizecheck = cdb.getWritableDatabase().query("classes", null, "semester = \""+this.semId+"\"" , null, null, null, null);
//		Cursor sizecheck = cdb.getReadableDatabase().query("classes", null, null , null, null, null, null);
		
		if (sizecheck.getCount()<1)
		{	
			//TODO: DIALOG STUFF
			sizecheck.close();
			client = ConnectionHelper.getThreadSafeClient();
			new parseTask(client).execute();
		}
		else
		{
			sizecheck.close();
			//lol hacky way for me to continue avoiding CursorAdapters.  Maybe one of these days...
			new dbQueryTask().execute();
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
		((ImageView)(sd.getHandle())).setImageResource(R.drawable.ic_expand_half);
	}
	public void onDrawerOpened()
	{
		((ImageView)(sd.getHandle())).setImageResource(R.drawable.ic_collapse_half);
	}
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
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
	
	private class parseTask extends AsyncTask<Object,Void,Integer>
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
			////figure this shit out
//			((ScheduleActivity)parentAct).spinner.setClickable(false);
//			((ScheduleActivity)parentAct).spinner.setActivated(false);
			pb_ll.setVisibility(GridView.VISIBLE);
			gv.setVisibility(GridView.GONE);
		}
		
		@Override
		protected Integer doInBackground(Object... params)
		{
		//	Object[] result = new Object[3];
			client.getCookieStore().clear();
				
	    	BasicClientCookie cookie = new BasicClientCookie("SC", ConnectionHelper.getAuthCookie(parentAct,client));
	    	cookie.setDomain(".utexas.edu");
	    	client.getCookieStore().addCookie(cookie);

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
		    	int classCount = 0;
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
		    		cdb.addClass(new UTClass(uniqueid,classid,classname,buildings, rooms, days, times, semId));
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
					
					return;
				}
				else
				{	
					ca = new ClassAdapter(parentAct,sd,sdll,ci_iv,ci_tv,semId);
					ca.updateTime(); // not necessary
	
					gv.setOnItemClickListener(CourseScheduleFragment.this);
				    gv.setAdapter(ca);
	
					gv.setVisibility(GridView.VISIBLE);
					daylist.setVisibility(View.VISIBLE);
					
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
			}	
			if(classParseIssue)
				Toast.makeText(parentAct, "One or more classes could not be parsed correctly, try emailing the dev ;)", Toast.LENGTH_SHORT).show();
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
	private class dbQueryTask extends AsyncTask<Object,Void,Integer>
	{
		private DefaultHttpClient client;
		private String errorMsg;

		@Override
		protected void onPreExecute()
		{
			pb_ll.setVisibility(GridView.VISIBLE);
			gv.setVisibility(GridView.GONE);
		}
		
		@Override
		protected Integer doInBackground(Object... params)
		{
			ca = new ClassAdapter(parentAct,sd,sdll,ci_iv,ci_tv,semId);
	    	return null;	
		}
		@Override
		protected void onPostExecute(Integer result)
		{

			pb_ll.setVisibility(GridView.GONE);
		
			ca.updateTime(); // not necessary

			gv.setOnItemClickListener(CourseScheduleFragment.this);
		    gv.setAdapter(ca);

			gv.setVisibility(GridView.VISIBLE);
			daylist.setVisibility(View.VISIBLE);
			
			if(!parentAct.isFinishing())
		    	Toast.makeText(parentAct, "Tap a class to see its information.", Toast.LENGTH_SHORT).show();
			Crittercism.leaveBreadcrumb("Loaded schedule from web");
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

	private final class ScheduleActionMode implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.setTitle("Class Info");
            MenuInflater inflater = ((SherlockFragmentActivity)getActivity()).getSupportMenuInflater();
            inflater.inflate(R.menu.schedule_menu, menu);
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
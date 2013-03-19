package com.nasageek.utexasutilities.fragments;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
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
import com.nasageek.utexasutilities.WrappingSlidingDrawer;
import com.nasageek.utexasutilities.activities.CampusMapActivity;
import com.nasageek.utexasutilities.activities.ScheduleActivity;
import com.nasageek.utexasutilities.adapters.ClassAdapter;
import com.nasageek.utexasutilities.model.Classtime;
import com.nasageek.utexasutilities.model.UTClass;

public class CourseScheduleFragment extends SherlockFragment implements ActionModeFragment, SlidingDrawer.OnDrawerCloseListener, SlidingDrawer.OnDrawerOpenListener, AdapterView.OnItemClickListener {
	
	private GridView gv;
	private WrappingSlidingDrawer sd ;
	private LinearLayout sdll;
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
	private parseTask fetch;
	
	private ActionMode mode;

	private SherlockFragmentActivity parentAct;
	String semId;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View vg =  inflater.inflate(R.layout.course_schedule_fragment_layout, container, false);
			
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
		
	/*	if(savedInstanceState != null)
			classList = savedInstanceState.getParcelableArrayList("classList"); */
		
		sd.setOnDrawerCloseListener(this);
		sd.setOnDrawerOpenListener(this);
		sd.setVisibility(View.INVISIBLE);
		
		fetch = new parseTask(client);
			
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			fetch.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		else
			fetch.execute();
		
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
/*	@Override
	public void onSaveInstanceState(Bundle out)
	{
		super.onSaveInstanceState(out);
		out.putParcelableArrayList("classList", classList);
	}*/
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if(fetch!=null)
			fetch.cancel(true);
	}
	@Override
	public void onPrepareOptionsMenu(Menu menu)
	{
		if(classList == null || classList.size() == 0)
		{
			menu.findItem(R.id.map_all_classes).setEnabled(false);
			menu.findItem(R.id.export_schedule).setEnabled(false);
		}
		else
		{
			menu.findItem(R.id.map_all_classes).setEnabled(true);
			menu.findItem(R.id.export_schedule).setEnabled(true);
		}
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
				map.putStringArrayListExtra("buildings", buildings);
				startActivity(map);
				break;
    		}
    	case R.id.export_schedule:
    		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    		{
    			//check to see if we're done loading the schedules (the ClassAdapter is initialized in onPostExecute)
    			if(ca != null)
	    		{	
    				FragmentManager fm = parentAct.getSupportFragmentManager();
    		        DoubleDatePickerDialogFragment ddpDlg = DoubleDatePickerDialogFragment.newInstance(classList);
    		        ddpDlg.show(fm, "fragment_double_date_picker");
	    		}
    		}
    		else
    			Toast.makeText(parentAct, "Export to calendar is not supported on this version of Android", Toast.LENGTH_SHORT).show();
    	
    	break;
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
		current_clt = (Classtime) parent.getItemAtPosition(position);
		
		if(current_clt!=null)
		{
			mode = parentAct.startActionMode(new ScheduleActionMode());
			sd.setVisibility(View.VISIBLE);
			
	    	String text = " ";
	    	text+=current_clt.getCourseId()+" - "+current_clt.getName()+" ";
	 
    		String daytext = "\n\t";
    		String building = current_clt.getBuilding().getId()+" "+current_clt.getBuilding().getRoom();
    		String unique = current_clt.getUnique();
    		
    		String time = current_clt.getStartTime();
    		String end = current_clt.getEndTime();
  
			if(current_clt.getDay()=='H')
				daytext+="TH";
			else
				daytext+=current_clt.getDay();

    		//TODO: stringbuilder
			text += (daytext+" from " + time + "-"+end + " in "+building) + "\n";
    		
    		text += "\tUnique: " + unique + "\n";

	    	ci_iv.setBackgroundColor(Color.parseColor("#"+current_clt.getColor()));
	    	ci_iv.setMinimumHeight(10);
	    	ci_iv.setMinimumWidth(10);
	    	
    		ci_tv.setTextColor(Color.BLACK);
    		ci_tv.setTextSize(14f);
    		ci_tv.setBackgroundColor(0x99F0F0F0);
    		ci_tv.setText(text);
	    	
		    sd.open();    
		}
		//they clicked an empty cell
		else
		{	
			if(mode!=null)
				mode.finish();
			sd.setVisibility(View.INVISIBLE);
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
			((ScheduleActivity)parentAct).getFragments().add((SherlockFragment)SherlockFragment.instantiate(parentAct, CourseScheduleFragment.class.getName(),args));
			((ScheduleActivity)parentAct).getAdapter().notifyDataSetChanged();
			((ScheduleActivity)parentAct).getIndicator().notifyDataSetChanged();
		}
		private String convertStreamToString(InputStream is)
		{
			Scanner s = new Scanner(is, "iso-8859-1").useDelimiter("\\A");
			return s.hasNext() ? s.next() : "";
		}
		
		@Override
		protected Integer doInBackground(Object... params)
		{
	//"stateful" stuff, I'll get it figured out in the next release
	//		if(classList == null)
				classList = new ArrayList<UTClass>();
	//		else
	//			return classList.size();
			
	    	String pagedata="";

	    	if(Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) 
	    	{
	    		URL location;
	    		HttpsURLConnection conn = null;
	    		
	    		try {

					location = new URL("https://utdirect.utexas.edu/registration/classlist.WBX?sem=" +semId);
					conn = (HttpsURLConnection) location.openConnection();
					
					if(getSherlockActivity() == null)
					{	
						cancel(true);
						errorMsg = "";
						return -1;
					}
					//TODO: why not just do this in onPreExecute?
					conn.setRequestProperty("Cookie", "SC="+ConnectionHelper.getAuthCookie(getSherlockActivity(),client));
					
			//		conn.setUseCaches(true); 
			//		conn.setRequestProperty("Cache-Control", "only-if-cached");
					conn.setRequestMethod("GET");
					conn.setDoInput(true);
				/*	if(HttpResponseCache.getInstalled().get(new URI("https://utdirect.utexas.edu/registration/classlist.WBX?sem=" +semId), "GET", conn.getHeaderFields()) != null)
					{	
						pagedata = convertStreamToString(HttpResponseCache.getInstalled().get(new URI("https://utdirect.utexas.edu/registration/classlist.WBX?sem=" +semId), "GET", conn.getHeaderFields()).getBody());
					}
					else
					{	*/
						conn.connect();
						pagedata = convertStreamToString(conn.getInputStream());
			//		}
					
				} catch (IOException e) {
					e.printStackTrace();
		    		errorMsg = "UTilities could not fetch your class listing";
		    		cancel(true);
		    		return -1;
				} finally {
					if(conn != null)
						conn.disconnect();
				}
	    	}
	    	else
	    	{	
	    		HttpGet hget = new HttpGet("https://utdirect.utexas.edu/registration/classlist.WBX?sem=" +semId);
				try
		    	{
					HttpResponse res = client.execute(hget);
		    		pagedata = EntityUtils.toString(res.getEntity());
		    	}
		    	catch(Exception e)
		    	{
		    		e.printStackTrace();
		    		errorMsg = "UTilities could not fetch your class listing";
		    		cancel(true);
		    		return -1;
		    	}
	    	}
	    	
	    	if(pagedata.contains("<title>Information Technology Services - UT EID Logon</title>"))
	    	{
				errorMsg = "You've been logged out of UTDirect, back out and log in again.";
				if(parentAct != null)
					ConnectionHelper.logout(parentAct);
				cancel(true);
				return -1;
	    	}
	    	Pattern semSelectPattern = Pattern.compile("<select  name=\"sem\">.*</select>", Pattern.DOTALL);
	    	Matcher semSelectMatcher = semSelectPattern.matcher(pagedata);
	    	
	    	//TODO: un-hardcode this eventually! Shouldn't be too hard to figure out the dropdown size
	    	if(semSelectMatcher.find() && parentAct != null && ((ScheduleActivity)parentAct).getFragments().size()<3)
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
	    	
	    	while(classMatcher.find())
	    	{
	    		String classContent = classMatcher.group();
	    		
	    		String uniqueid="", classid="", classname="";
	    		String[] buildings=null, rooms=null, days=null, times=null;
	    		
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
	    			//Thursday represented by H so I can treat all days as characters
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
	    	return Integer.valueOf(classCount);
			
		}
		//TODO: nullchecks everywhere, figure out what the real problem is
		@Override
		protected void onPostExecute(Integer result)
		{
			pb_ll.setVisibility(View.GONE);

			if(result != null && result >= 0)
			{	
				if(result.intValue()==0)
				{	
					daylist.setVisibility(View.GONE);
					nc_tv.setText("You aren't enrolled for this semester.");
					nc_tv.setVisibility(View.VISIBLE);
					
					//if they're not enrolled for the semester, disable the calendar-specific options
					setMenuItemsEnabled(false);
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
					
					setMenuItemsEnabled(true);
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
				
				setMenuItemsEnabled(false);
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
			
			setMenuItemsEnabled(false);
		}
		
		private void setMenuItemsEnabled(boolean enable)
		{
			if(mMenu != null)
			{	if(mMenu.findItem(R.id.map_all_classes) != null)
					mMenu.findItem(R.id.map_all_classes).setEnabled(enable);
				if(mMenu.findItem(R.id.export_schedule) != null)
					mMenu.findItem(R.id.export_schedule).setEnabled(enable);
			}
		}
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
    				startActivity(map);
    				break;
            }
            return true;
        }
        @Override
        public void onDestroyActionMode(ActionMode mode) { }
    }
	
}
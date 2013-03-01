package com.nasageek.utexasutilities.fragments;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.crittercism.app.Crittercism;
import com.foound.widget.AmazingListView;
import com.nasageek.utexasutilities.ConnectionHelper;
import com.nasageek.utexasutilities.Pair;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.activities.CourseMapActivity;
import com.nasageek.utexasutilities.adapters.BBClassAdapter;
import com.nasageek.utexasutilities.model.BBClass;

public class BlackboardCourseListFragment extends SherlockFragment {
	
	private DefaultHttpClient httpclient;
	private LinearLayout bb_pb_ll;
	private TextView bbtv;
	private AmazingListView bblv;
	private ArrayList<BBClass> classList;
	private ArrayList<Pair<String,ArrayList<BBClass>>> classSectionList;
	private fetchClassesTask fetch;
//	public static String currentBBCourseId;
//	public static String currentBBCourseName;
	
	
	public BlackboardCourseListFragment() {}
	
	public static BlackboardCourseListFragment newInstance(String title)
	{
		BlackboardCourseListFragment f = new BlackboardCourseListFragment();
		Bundle args = new Bundle();
		args.putString("title", title);
		f.setArguments(args);
		
		return f;
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
    	classList = new ArrayList<BBClass>();
    	classSectionList = new ArrayList<Pair<String,ArrayList<BBClass>>>();
		
		httpclient = ConnectionHelper.getThreadSafeClient();
		httpclient.getCookieStore().clear();
		BasicClientCookie cookie = new BasicClientCookie("s_session_id", ConnectionHelper.getBBAuthCookie(getSherlockActivity(),httpclient));
    	cookie.setDomain("courses.utexas.edu");
    	httpclient.getCookieStore().addCookie(cookie);
    	
    	//TODO: where to callll, also, helper?  -  helper for what? shit I don't remember writing this...
    	fetch = new fetchClassesTask(httpclient);
    	
    	if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			fetch.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		else
			fetch.execute();
		
    	Crittercism.leaveBreadcrumb("Loaded BlackboardActivity");
	}

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{	
		View vg = inflater.inflate(R.layout.blackboard_courselist_fragment, container, false);

    	bb_pb_ll = (LinearLayout) vg.findViewById(R.id.blackboard_progressbar_ll);
    	bblv = (AmazingListView) vg.findViewById(R.id.blackboard_class_listview);
    	bbtv = (TextView) vg.findViewById (R.id.blackboard_error);

		return vg;
	}
	
	private class fetchClassesTask extends AsyncTask<Object,Void,String>
	{
		private DefaultHttpClient client;
		private String errorMsg;
		
		public fetchClassesTask(DefaultHttpClient client)
		{
			this.client = client;
		}
		
		@Override
		protected String doInBackground(Object... params)
		{
			HttpGet hget = new HttpGet("https://courses.utexas.edu/webapps/Bb-mobile-BBLEARN/enrollments?course_type=COURSE");
	    	String pagedata="";

	    	try
			{
				HttpResponse response = client.execute(hget);
		    	pagedata = EntityUtils.toString(response.getEntity());
			} catch (Exception e)
			{
				errorMsg = "UTilities could not fetch the Blackboard course list";
				e.printStackTrace();
				cancel(true);
				return null;
			}

	    	Pattern class_pattern = Pattern.compile("bbid=\"(.*?)\" name=\"(.*?)\" courseid=\"(.*?)\"");
	    	Matcher class_matcher = class_pattern.matcher(pagedata);
	    	
	    	while(class_matcher.find())
	    	{
	    		classList.add(new BBClass(class_matcher.group(2).replace("&amp;","&"),class_matcher.group(1).replace("&amp;","&"),class_matcher.group(3)));	
	    	}
	    	
			return pagedata;
		}
		@Override
		protected void onPostExecute(String result)
		{
			//build the list of courses here
			if(!this.isCancelled()) // not necessary
	    	{
	    		String currentCategory="";
	    		ArrayList<BBClass> sectionList=null;
				for(int i = 0; i<classList.size(); i++)
	    		{
	    			//first course is always in a new category (the first category)
					if(i==0)
	    			{	
	    				currentCategory = classList.get(i).getSemester();
	    				sectionList = new ArrayList<BBClass>();
	    				sectionList.add(classList.get(i));
	    			}
					//if the current course is not part of the current category or we're on the last course
					//weird stuff going on here depending on if we're at the end of the course list
	    			else if(!classList.get(i).getSemester().equals(currentCategory) || i == classList.size()-1)
	    			{
	    				
	    				if(i == classList.size()-1)
	    					sectionList.add(classList.get(i));
	    					
	    				classSectionList.add(new Pair<String, ArrayList<BBClass>>(currentCategory,sectionList));
	    				
	    				currentCategory = classList.get(i).getSemester();
	    				sectionList=new ArrayList<BBClass>();
	    				
	    				if(i != classList.size()-1)
	    					sectionList.add(classList.get(i));
	    			}
					//otherwise just add to the current category
	    			else
	    			{
	    				sectionList.add(classList.get(i));
	    			}
	    			
	    		}
				
				bblv.setAdapter(new BBClassAdapter(getSherlockActivity(),classSectionList));
				bblv.setPinnedHeaderView(getSherlockActivity().getLayoutInflater().inflate(R.layout.menu_header_item_view, bblv, false));
				
				bblv.setOnItemClickListener(new OnItemClickListener() {
					
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position,
							long id) {
						//TODO: figure out course id stuff here
						Intent classLaunch = new Intent(getString(R.string.coursemap_intent), null, getSherlockActivity(), CourseMapActivity.class);
						BBClass bbclass = (BBClass)(parent.getItemAtPosition(position));
						classLaunch.putExtra("courseid", bbclass.getBbid());
						classLaunch.setData(Uri.parse((bbclass).getBbid()));
						classLaunch.putExtra("folderName", "Course Map");
						classLaunch.putExtra("coursename", bbclass.getCourseId());
						classLaunch.putExtra("showViewInWeb", true);
						startActivity(classLaunch);
					} 
				});
				
				bb_pb_ll.setVisibility(View.GONE);
				bbtv.setVisibility(View.GONE);
	    		bblv.setVisibility(View.VISIBLE);
	    	}	
		}
		@Override
		protected void onCancelled()
		{
			bbtv.setText(errorMsg);
			bb_pb_ll.setVisibility(View.GONE);
			bbtv.setVisibility(View.VISIBLE);
    		bblv.setVisibility(View.GONE);
		}
	}
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if(fetch!=null)
			fetch.cancel(true);
	}
/*	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
    	int id = item.getItemId();
    	switch(id)
    	{
	    	case android.R.id.home:
	            // app icon in action bar clicked; go home
	            super.onBackPressed();
	            break;
    	}
    	return false;
	}*/

}

package com.nasageek.utexasutilities;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.crittercism.app.Crittercism;
import com.foound.widget.AmazingListView;

public class BlackboardActivity extends SherlockActivity {
	
	private ActionBar actionbar;
	private DefaultHttpClient httpclient;
	private SharedPreferences settings;
	private ConnectionHelper ch;
	private LinearLayout bb_pb_ll;
	private LinearLayout blackboardlinlay;
	private AmazingListView bblv;
	private ArrayList<BBClass> classList;
	private ArrayList<Pair<String,ArrayList<BBClass>>> classSectionList;
	private fetchClassesTask fetch;
	public static String currentBBCourseId;
	public static String currentBBCourseName;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
    	setContentView(R.layout.blackboard_layout);
    	
    	bb_pb_ll = (LinearLayout) findViewById(R.id.blackboard_progressbar_ll);
    	bblv = (AmazingListView) findViewById(R.id.blackboard_class_listview);
    	blackboardlinlay = (LinearLayout) findViewById(R.id.blackboard_courselist);
    	
    	classList = new ArrayList<BBClass>();
    	classSectionList = new ArrayList<Pair<String,ArrayList<BBClass>>>();
    	
    	actionbar = getSupportActionBar();
		actionbar.setTitle("Blackboard");
		actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionbar.setHomeButtonEnabled(true);
		// actionbar.setDisplayHomeAsUpEnabled(true);
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)	
    		actionbar.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.actionbar_bg));
		
		ch = new ConnectionHelper(this);
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		
		httpclient = ConnectionHelper.getThreadSafeClient();
		httpclient.getCookieStore().clear();
		BasicClientCookie cookie = new BasicClientCookie("s_session_id", ConnectionHelper.getBBAuthCookie(this,httpclient));
    	cookie.setDomain("courses.utexas.edu");
    	httpclient.getCookieStore().addCookie(cookie);
    	
    	fetch = new fetchClassesTask(httpclient);
    	fetch.execute();
		
    	 Crittercism.leaveBreadcrumb("Loaded BlackboardActivity");
	}

	
	private class fetchClassesTask extends AsyncTask<Object,Void,String>
	{
		private DefaultHttpClient client;
		
		public fetchClassesTask(DefaultHttpClient client)
		{
			this.client = client;
		}
		
		@Override
		protected String doInBackground(Object... params)
		{
	//		DefaultHttpClient httpclient = ch.getThreadSafeClient();

			HttpGet hget = new HttpGet("https://courses.utexas.edu/webapps/Bb-mobile-BBLEARN/enrollments?course_type=COURSE");
	    	String pagedata="";

	    	try
			{
				HttpResponse response = client.execute(hget);
		    	pagedata = EntityUtils.toString(response.getEntity());
			} catch (Exception e)
			{
				e.printStackTrace();
			}

	    	Pattern class_pattern = Pattern.compile("bbid=\"(.*?)\" name=\"(.*?)\" courseid=\"(.*?)\"");
	    	Matcher class_matcher = class_pattern.matcher(pagedata);
	    	
	    	while(class_matcher.find())
	    	{
	    		classList.add(new BBClass(class_matcher.group(2),class_matcher.group(1).replace("&amp;","&"),class_matcher.group(3)));
	    		
	    	}
	    	
			return pagedata;
		}
		@Override
		protected void onPostExecute(String result)
		{
			if(!this.isCancelled())
	    	{
	    		String currentCategory="";
	    		ArrayList sectionList=null;
				for(int i = 0; i<classList.size(); i++)
	    		{
	    			if(i==0)
	    			{	
	    				currentCategory = classList.get(i).getSemester();
	    				sectionList = new ArrayList();
	    				sectionList.add(classList.get(i));
	    			}
	    			else if(!classList.get(i).getSemester().equals(currentCategory) || i == classList.size()-1)
	    			{
	    				if(i == classList.size()-1)
	    					sectionList.add(classList.get(i));
	    					
	    				classSectionList.add(new Pair(currentCategory,sectionList));
	    				
	    				currentCategory = classList.get(i).getSemester();
	    				sectionList=new ArrayList();
	    				
	    				if(i != classList.size()-1)
	    					sectionList.add(classList.get(i));
	    			}
	    			else
	    			{
	    				sectionList.add(classList.get(i));
	    			}
	    			
	    		}
				
				bblv.setAdapter(new BBClassAdapter(BlackboardActivity.this,classSectionList));
				bblv.setPinnedHeaderView(BlackboardActivity.this.getLayoutInflater().inflate(R.layout.menu_header_item_view, bblv, false));
				
				bblv.setOnItemClickListener(new OnItemClickListener() {
					
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position,
							long id) {

						Intent classLaunch = new Intent(getString(R.string.coursemap_intent), null, BlackboardActivity.this, CourseMapActivity.class);
						BBClass bbclass = (BBClass)(parent.getItemAtPosition(position));
						currentBBCourseId = bbclass.getBbid();
						classLaunch.setData(Uri.parse((bbclass).getBbid()));
						String unique = bbclass.getCourseid().split("_")[2];
						String cid = bbclass.getCourseid().substring(bbclass.getCourseid().indexOf(unique)+6).replaceAll("_"," ");
						classLaunch.putExtra("folderName", cid);
						currentBBCourseName = cid;
						startActivity(classLaunch);

					}
				});
				
				
				bb_pb_ll.setVisibility(View.GONE);
	    		bblv.setVisibility(View.VISIBLE);
	    	}
		}	
	}
	@Override
	public void onStop()
	{
		super.onStop();
		if(fetch!=null)
			fetch.cancel(true);
	}
	@Override
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
	    	}
	    	return false;
	}

}

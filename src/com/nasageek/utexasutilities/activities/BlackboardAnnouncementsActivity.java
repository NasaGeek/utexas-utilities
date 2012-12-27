 package com.nasageek.utexasutilities.activities;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.nasageek.utexasutilities.ConnectionHelper;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.R.id;
import com.nasageek.utexasutilities.R.layout;
import com.nasageek.utexasutilities.R.menu;


public class BlackboardAnnouncementsActivity extends SherlockActivity 
{
	private ActionBar actionbar;
	private LinearLayout a_pb_ll;
	private ListView alv;
	private TextView atv;
	private TextView etv;
	private SharedPreferences settings;
	private DefaultHttpClient httpclient;
	private fetchAnnouncementsTask fetch;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
			super.onCreate(savedInstanceState);
			setContentView(R.layout.blackboard_announcements_layout);
			
			a_pb_ll = (LinearLayout) findViewById(R.id.announcements_progressbar_ll);
	    	alv = (ListView) findViewById(R.id.announcementsListView);
	    	atv = (TextView) findViewById(R.id.no_announcements_textview);
	    	etv = (TextView) findViewById(R.id.announcements_error);
	    	
	    	actionbar = getSupportActionBar();
	    	actionbar.setTitle(getIntent().getStringExtra("coursename"));
	    	actionbar.setSubtitle("Announcements");
			actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			actionbar.setHomeButtonEnabled(true);
			actionbar.setDisplayHomeAsUpEnabled(true);
			
		
			settings = PreferenceManager.getDefaultSharedPreferences(this);
			
			httpclient = ConnectionHelper.getThreadSafeClient();
			httpclient.getCookieStore().clear();
			BasicClientCookie cookie = new BasicClientCookie("s_session_id", ConnectionHelper.getBBAuthCookie(this,httpclient));
	    	cookie.setDomain("courses.utexas.edu");
	    	httpclient.getCookieStore().addCookie(cookie);
	    	
	    	fetch = new fetchAnnouncementsTask(httpclient);
	    	fetch.execute();
			
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		MenuInflater inflater = this.getSupportMenuInflater();
        inflater.inflate(R.menu.blackboard_dlable_item_menu, menu);
		return true;
		 
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
	    	int id = item.getItemId();
	    	switch(id)
	    	{
		    	case android.R.id.home:
		            // app icon in action bar clicked; go home
		            super.onBackPressed();
		            break;
		    	case R.id.viewInWeb:
		    		showAreYouSureDlg(BlackboardAnnouncementsActivity.this);
		    		break;
	    	}
	    	return false;
	}
	private void showAreYouSureDlg(Context con)
	{
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(con);
		alertBuilder.setMessage("Would you like to view this item on the Blackboard website?");
		alertBuilder.setNegativeButton("No", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which) {

				dialog.dismiss();
				
			}
		});
		
		alertBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() 
		{
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
	
				Intent web = new Intent(null,Uri.parse(getIntent().getStringExtra("viewUri")),BlackboardAnnouncementsActivity.this,BlackboardExternalItemActivity.class);
	    		web.putExtra("itemName", "Announcements");
	    		web.putExtra("coursename", getIntent().getStringExtra("coursename"));
	    		startActivity(web);
			}		
		});
		alertBuilder.setTitle("View on Blackboard");
		alertBuilder.show();
	}	
	private class fetchAnnouncementsTask extends AsyncTask<Object,Void,ArrayList<bbAnnouncement>>
	{
		private DefaultHttpClient client;
		private String errorMsg;
		
		public fetchAnnouncementsTask(DefaultHttpClient client)
		{
			this.client = client;
		}

		@Override
		protected ArrayList<bbAnnouncement> doInBackground(Object... params)
		{
			HttpGet hget = new HttpGet("https://courses.utexas.edu/webapps/Bb-mobile-BBLEARN/courseData?course_section=ANNOUNCEMENTS&course_id="+getIntent().getStringExtra("courseid"));
	    	String pagedata="";

	    	try
			{
				HttpResponse response = client.execute(hget);
		    	pagedata = EntityUtils.toString(response.getEntity());
			} catch (Exception e)
			{
				errorMsg = "UTilities could not fetch this course's announcements";
				e.printStackTrace();
				cancel(true);
				return null;
			}
	    	ArrayList<bbAnnouncement> data=new ArrayList<bbAnnouncement>();
	//    	pagedata = pagedata.replaceAll("comments=\".*?\"", ""); //might include later, need to strip for now for grade recognition
	    	
	    	Pattern announcementPattern = Pattern.compile("<announcement .*?subject=\"(.*?)\".*?startdate=\"(.*?)\".*?>(.*?)</announcement>",Pattern.DOTALL);
	    	Matcher announcementMatcher = announcementPattern.matcher(pagedata);
	    	
	    	while(announcementMatcher.find())
	    	{
	    		data.add(new bbAnnouncement(announcementMatcher.group(1),announcementMatcher.group(2),announcementMatcher.group(3)));
	    	}
			return data;
		}
		@Override
		protected void onPostExecute(ArrayList<bbAnnouncement> result)
		{
			if(!this.isCancelled())
	    	{
				a_pb_ll.setVisibility(View.GONE);
				etv.setVisibility(View.GONE);
				if(!result.isEmpty())
				{
					alv.setAdapter(new AnnouncementsAdapter(BlackboardAnnouncementsActivity.this,result));
					alv.setVisibility(View.VISIBLE);
					atv.setVisibility(View.GONE);
				}
				else
				{	
					atv.setVisibility(View.VISIBLE);
					alv.setVisibility(View.GONE);
				}
	    	}
		}
		@Override
		protected void onCancelled()
		{
			etv.setText(errorMsg);
			a_pb_ll.setVisibility(View.GONE);
			alv.setVisibility(View.GONE);
			atv.setVisibility(View.GONE);
			etv.setVisibility(View.VISIBLE);
		}
	}

	class AnnouncementsAdapter extends ArrayAdapter<bbAnnouncement> {
	
		private Context con;
		private ArrayList<bbAnnouncement> items;
		private LayoutInflater li;
		
		public AnnouncementsAdapter(Context c, ArrayList<bbAnnouncement> items)
		{
			super(c,0,items);
			con = c;
			this.items=items;
			li = (LayoutInflater)con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		}
		public int getCount() {
			
			return items.size();
		}
	
		public bbAnnouncement getItem(int position) {
	
			return items.get(position);
		}
	
		public long getItemId(int position) {
			
			return 0;
		}
		@Override
		public boolean areAllItemsEnabled()
		{
			return true;
		}
		@Override
		public boolean isEnabled(int i)
		{
			return true;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			
			bbAnnouncement announce = items.get(position);
			
			String subject = announce.getSubject();
			String date = announce.getFormattedDate();
			String body = announce.getFormattedBody();
			
			ViewGroup lin = (ViewGroup) convertView;

			if (lin == null)
				lin = (LinearLayout) li.inflate(R.layout.announcement_item_view,null,false);
			
			TextView announcementSubject = (TextView) lin.findViewById(R.id.announcement_header_subject);
			TextView announcementDate = (TextView) lin.findViewById(R.id.announcement_header_date);
			TextView announcementBody = (TextView) lin.findViewById(R.id.announcement_body);
			
			announcementSubject.setText(subject);
			announcementDate.setText(date);
			announcementBody.setText(body);
	
			return (View)lin;
		}
	}
	
	class bbAnnouncement
	{
		String subject, date, body;
		
		public bbAnnouncement(String subject, String date, String body)
		{
			 this.subject = subject;
			 this.date = date;
			 this.body = body;
		}
		public String getSubject()
		{
			return subject;
		}
		public String getFormattedDate()
		{
			return date.substring(0,date.indexOf('T'));
		}
		public String getFormattedBody()
		{
			return Html.fromHtml(Html.fromHtml(body).toString()).toString();
		}
	}
	
}

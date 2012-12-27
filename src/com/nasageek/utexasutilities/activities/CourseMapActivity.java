package com.nasageek.utexasutilities.activities;

import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.nasageek.utexasutilities.BBClass;
import com.nasageek.utexasutilities.ConnectionHelper;
import com.nasageek.utexasutilities.CourseMapSaxHandler;
import com.nasageek.utexasutilities.Pair;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.CourseMapItem;
import com.nasageek.utexasutilities.adapters.CourseMapAdapter;


public class CourseMapActivity extends SherlockActivity {

	
	private ActionBar actionbar;
	private DefaultHttpClient httpclient;
	private SharedPreferences settings;
	private LinearLayout cm_pb_ll;
	private LinearLayout coursemaplinlay;
	private ListView cmlv;
	private ArrayList<BBClass> classList;
	private ArrayList<Pair<CourseMapItem,ArrayList<BBClass>>> classSectionList;
	private fetchCoursemapTask fetch;
	private String bbid;
	private XMLReader xmlreader;
	private CourseMapSaxHandler courseMapSaxHandler;
	private int itemNumber;
	private ArrayList<Pair<CourseMapItem, ArrayList>> mainList;
	private TextView failure_view;
	
	private TextView absTitle;
	private TextView absSubtitle;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.coursemap_layout);
		actionbar = getSupportActionBar();
		actionbar.setDisplayShowCustomEnabled(true);
		actionbar.setDisplayShowTitleEnabled(false);
		actionbar.setHomeButtonEnabled(true);
		actionbar.setDisplayHomeAsUpEnabled(true);
		
		TextView titleView = new TextView(this);
		titleView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
		titleView.setLines(1);
		titleView.setTextSize(18);
		titleView.setPadding(0, 0, 7, 0);
		titleView.setSingleLine(true);
		titleView.setTextColor(Color.BLACK);
		titleView.setTypeface(Typeface.DEFAULT);
		titleView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
		
		//actionbar.setCustomView(titleView);
		actionbar.setCustomView(getLayoutInflater().inflate(R.layout.action_bar_title_subtitle, null));
		absTitle = (TextView) actionbar.getCustomView().findViewById(R.id.abs__action_bar_title);
		absSubtitle = (TextView) actionbar.getCustomView().findViewById(R.id.abs__action_bar_subtitle);
		
		itemNumber=-1;
		bbid="";
		if(getString(R.string.coursemap_intent).equals(getIntent().getAction()))
		{
			bbid = getIntent().getDataString();
		}
		else if(getString(R.string.coursemap_nest_intent).equals(getIntent().getAction()))
		{
			mainList = (ArrayList<Pair<CourseMapItem, ArrayList>>) getIntent().getSerializableExtra("mainList");
			itemNumber = Integer.parseInt(getIntent().getDataString());		
		}
		
		absSubtitle.setText(getIntent().getStringExtra("folderName"));
		
		if(getIntent().getStringExtra("folderName")!=null)
		{	
			absTitle.setText(getIntent().getStringExtra("coursename"));
		}
		
		cm_pb_ll = (LinearLayout) findViewById(R.id.coursemap_progressbar_ll);
		cmlv = (ListView) findViewById(R.id.coursemap_listview);
		coursemaplinlay = (LinearLayout) findViewById(R.id.coursemap_linlay);
		failure_view = (TextView) findViewById(R.id.coursemap_error);
		
		cmlv.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				String linkType = mainList.get(position).first.getLinkType();
				String url = mainList.get(position).first.getViewUrl();
				
				if(mainList.get(position).second.size() != 0)
				{	
					Intent courseMapLaunch = new Intent(getString(R.string.coursemap_nest_intent), Uri.parse(position+""), CourseMapActivity.this, CourseMapActivity.class);
					courseMapLaunch.putExtra("mainList", mainList.get(position).second);
					if(itemNumber == -1 )//top-level, don't copy "Course Map"
						courseMapLaunch.putExtra("folderName", mainList.get(position).first.getName());
					else //chain onto the current folder name for "breadcrumbs"
						courseMapLaunch.putExtra("folderName", absSubtitle.getText() + "/" + mainList.get(position).first.getName());
					courseMapLaunch.putExtra("viewUri", mainList.get(position).first.getViewUrl());
					courseMapLaunch.putExtra("courseid", getIntent().getStringExtra("courseid"));
					courseMapLaunch.putExtra("coursename", getIntent().getStringExtra("coursename"));
					startActivity(courseMapLaunch);
				}
				else if(linkType.equals("resource/x-bb-file") || linkType.equals("resource/x-bb-document"))
				{
					String contentid = mainList.get(position).first.getContentId();
					Intent bbItemLaunch = new Intent(null, null, CourseMapActivity.this, BlackboardDownloadableItemActivity.class);
					bbItemLaunch.putExtra("contentid", contentid);
					if(itemNumber == -1 )//top-level, don't copy "Course Map"
						bbItemLaunch.putExtra("itemName", mainList.get(position).first.getName()); //will be used as Subtitle
					else
						bbItemLaunch.putExtra("itemName", absSubtitle.getText() + "/" + mainList.get(position).first.getName()); //Subtitle
					bbItemLaunch.putExtra("viewUri", url);
					bbItemLaunch.putExtra("courseid", getIntent().getStringExtra("courseid"));
					bbItemLaunch.putExtra("coursename", getIntent().getStringExtra("coursename"));
					startActivity(bbItemLaunch);
				}
				else if(linkType.equals("resource/x-bb-externallink"))
				{
					//((TextView)(actionbar.getCustomView())).setText((((TextView) actionbar.getCustomView()).getText()) + "/" + mainList.get(position).first.split("\\^")[0]);	
					Intent exItemLaunch = new Intent(Intent.ACTION_VIEW,Uri.parse(url));

					exItemLaunch.putExtra("courseid", getIntent().getStringExtra("courseid"));
					exItemLaunch.putExtra("coursename", getIntent().getStringExtra("coursename"));
					startActivity(exItemLaunch);
					//actionbar.setTitle(actionbar.getTitle()+"/"+mainList.get(position).first.split("\\^")[0]);
				}
				else if(linkType.equals("student_gradebook"))
				{
					Intent gradesLaunch = new Intent(null, null, CourseMapActivity.this, BlackboardGradesActivity.class);
					gradesLaunch.putExtra("viewUri", url);
					gradesLaunch.putExtra("courseid", getIntent().getStringExtra("courseid"));
					gradesLaunch.putExtra("coursename", getIntent().getStringExtra("coursename"));
					startActivity(gradesLaunch);
				}
				else if(linkType.equals("announcements"))
				{
					Intent announcementsLaunch = new Intent(null, null, CourseMapActivity.this, BlackboardAnnouncementsActivity.class);
					announcementsLaunch.putExtra("viewUri", url);
					announcementsLaunch.putExtra("courseid", getIntent().getStringExtra("courseid"));
					announcementsLaunch.putExtra("coursename", getIntent().getStringExtra("coursename"));
					startActivity(announcementsLaunch);
				}
				else //default to webview
				{
					Intent bbItemLaunch = new Intent(null, Uri.parse(url), CourseMapActivity.this, BlackboardExternalItemActivity.class);
					bbItemLaunch.putExtra("mainList", mainList.get(position).second);
					if(itemNumber == -1 )//top-level, don't copy "Course Map"
						bbItemLaunch.putExtra("itemName", mainList.get(position).first.getName()); //will be used as Subtitle
					else
						bbItemLaunch.putExtra("itemName", absSubtitle.getText() + "/" + mainList.get(position).first.getName()); //Subtitle
					bbItemLaunch.putExtra("courseid", getIntent().getStringExtra("courseid"));
					bbItemLaunch.putExtra("coursename", getIntent().getStringExtra("coursename")); //will be used at Title
					startActivity(bbItemLaunch);
				}
			}
		});
		
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		
		httpclient = ConnectionHelper.getThreadSafeClient();
		httpclient.getCookieStore().clear();
		BasicClientCookie cookie = new BasicClientCookie("s_session_id", ConnectionHelper.getBBAuthCookie(this,httpclient));
		cookie.setDomain("courses.utexas.edu");
		httpclient.getCookieStore().addCookie(cookie);
	//ONLY DO IF TOP LEVEL
		if(itemNumber==-1)
		{
			fetch = new fetchCoursemapTask(httpclient);
			fetch.execute();
		}
	//now we've got the whole course tree, navigate as necessary
		else if(mainList!= null && mainList.size() != 0)
		{	
			cmlv.setAdapter(new CourseMapAdapter(this,mainList));
			cm_pb_ll.setVisibility(View.GONE);
	    	cmlv.setVisibility(View.VISIBLE);
		}		
	}
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if(fetch!=null)
			fetch.cancel(true);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = this.getSupportMenuInflater();
        inflater.inflate(R.menu.blackboard_dlable_item_menu, menu);
        //return true only if not top-level
        //there is no "nice" page for the coursemap viewable in a browser
        return itemNumber != -1; 
		 
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
	    		showAreYouSureDlg(CourseMapActivity.this);
	    		break;
    	}
    	return false;
	}
	@Override
	protected void onSaveInstanceState(Bundle icicle) 
	{
		icicle.putString("courseid", getIntent().getStringExtra("courseid"));
		icicle.putString("coursename", getIntent().getStringExtra("coursename"));
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
				
				Intent web = new Intent(null,Uri.parse(getIntent().getStringExtra("viewUri")),CourseMapActivity.this,BlackboardExternalItemActivity.class);
	    		web.putExtra("itemName", getIntent().getStringExtra("folderName")); //will be used as SubTitle
	    		web.putExtra("coursename", getIntent().getStringExtra("coursename")); //will be used as Title
	    		startActivity(web);
			}		
		});
		alertBuilder.setTitle("View on Blackboard");
		alertBuilder.show();
	}
	private class fetchCoursemapTask extends AsyncTask<Object,Void,ArrayList>
	{
		private DefaultHttpClient client;
		private String failureMessage = "";
		
		public fetchCoursemapTask(DefaultHttpClient client)
		{
			this.client = client;
		}
		
		@Override
		protected ArrayList doInBackground(Object... params)
		{
			HttpGet hget = new HttpGet("https://courses.utexas.edu/webapps/Bb-mobile-BBLEARN/courseMap?course_id="+bbid);
	    	String pagedata="";

	    	try
			{
				HttpResponse response = client.execute(hget);
		    	pagedata = EntityUtils.toString(response.getEntity());
			} catch (Exception e)
			{
				failureMessage = "UTilities could not fetch your Blackboard course map";
				e.printStackTrace();
				cancel(true);
				return null;
			}

	    	 try{

	             // create the factory
	             SAXParserFactory factory = SAXParserFactory.newInstance();
	            
	             // create a parser
	             SAXParser parser = factory.newSAXParser();
	             // create the reader (scanner)
	             xmlreader = parser.getXMLReader();
	             // instantiate our handler
	             courseMapSaxHandler = new CourseMapSaxHandler();
	             // assign our handler
	             xmlreader.setContentHandler(courseMapSaxHandler);
	         
	             InputSource is = new InputSource(new StringReader(pagedata));
		        
	             xmlreader.parse(is);

	             mainList = courseMapSaxHandler.getParsedData();
	         }
	         catch(Exception e){
	        	 failureMessage = "UTilities could not parse the downloaded Blackboard data.";
	        	 e.printStackTrace();
	        	 cancel(true);
	        	 return null;	        	 
	         }	          	    

			return mainList;
		}
		@Override
		protected void onPostExecute(ArrayList result)
		{
			if(!this.isCancelled())
	    	{
				cmlv.setAdapter(new CourseMapAdapter(CourseMapActivity.this,result));
				
				cm_pb_ll.setVisibility(View.GONE);
	    		cmlv.setVisibility(View.VISIBLE);
	    		failure_view.setVisibility(View.GONE);
	    	}
		}
		@Override
		protected void onCancelled()
		{
			failure_view.setText(failureMessage);
			cm_pb_ll.setVisibility(View.GONE);
    		cmlv.setVisibility(View.GONE);
			failure_view.setVisibility(View.VISIBLE); 
		}
	}
}

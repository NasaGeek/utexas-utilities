package com.nasageek.utexasutilities.fragments;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
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
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.mapsaurus.paneslayout.FragmentLauncher;
import com.nasageek.utexasutilities.ConnectionHelper;
import com.nasageek.utexasutilities.CourseMapSaxHandler;
import com.nasageek.utexasutilities.ParcelablePair;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.adapters.CourseMapAdapter;
import com.nasageek.utexasutilities.model.BBClass;
import com.nasageek.utexasutilities.model.CourseMapItem;


public class BlackboardCourseMapFragment extends SherlockFragment implements BlackboardFragment {
	
	private DefaultHttpClient httpclient;
	private LinearLayout cm_pb_ll;
	private ListView cmlv;
	private ArrayList<BBClass> classList;
	private ArrayList<ParcelablePair<CourseMapItem,ArrayList<BBClass>>> classSectionList;
	private fetchCoursemapTask fetch;
	private XMLReader xmlreader;
	private CourseMapSaxHandler courseMapSaxHandler;
	private int itemNumber;
	private ArrayList<ParcelablePair<CourseMapItem, ArrayList>> mainList;
	private TextView failure_view;
	private String bbID, courseName, folderName, viewUri;
	
	private TextView absTitle;
	private TextView absSubtitle;
	
	public BlackboardCourseMapFragment() {}
	
	public static BlackboardCourseMapFragment newInstance(String action, ArrayList<ParcelablePair<CourseMapItem, ArrayList>> mainList, 
			String courseID, String courseName, String folderName, String viewUri, int itemNumber, boolean fromDashboard)
	{
		BlackboardCourseMapFragment bcmf = new BlackboardCourseMapFragment();
		
		Bundle args = new Bundle();
		args.putString("action", action);
		args.putSerializable("mainList", mainList);
        args.putString("courseID", courseID);
        args.putString("courseName", courseName);
        args.putString("folderName", folderName);
        args.putString("viewUri", viewUri);
        args.putInt("itemNumber", itemNumber);
        args.putBoolean("fromDashboard", fromDashboard);
        bcmf.setArguments(args);
        
        return bcmf;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		itemNumber=-1;
		courseName = getArguments().getString("courseName");
		bbID = getArguments().getString("courseID");
		folderName = getArguments().getString("folderName");
		viewUri = getArguments().getString("viewUri");

		if(getString(R.string.coursemap_nest_intent).equals(getArguments().getString("action")))
		{
			mainList = (ArrayList<ParcelablePair<CourseMapItem, ArrayList>>) getArguments().getSerializable("mainList");
			itemNumber = getArguments().getInt("itemNumber");		
		}

//		settings = PreferenceManager.getDefaultSharedPreferences(this);
		
		httpclient = ConnectionHelper.getThreadSafeClient();
		httpclient.getCookieStore().clear();
		BasicClientCookie cookie = new BasicClientCookie("s_session_id", ConnectionHelper.getBBAuthCookie(getSherlockActivity(),httpclient));
		cookie.setDomain("courses.utexas.edu");
		httpclient.getCookieStore().addCookie(cookie);
	
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{	
		final ActionBar actionbar = getSherlockActivity().getSupportActionBar();
		actionbar.setDisplayShowCustomEnabled(true);
		actionbar.setDisplayShowTitleEnabled(false);
		actionbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_TITLE);
		actionbar.setCustomView(inflater.inflate(R.layout.action_bar_title_subtitle, null));
		
		absTitle = (TextView) actionbar.getCustomView().findViewById(R.id.abs__action_bar_title);
		absSubtitle = (TextView) actionbar.getCustomView().findViewById(R.id.abs__action_bar_subtitle);
		
		
		
		final View vg = inflater.inflate(R.layout.coursemap_layout, container, false);

		cm_pb_ll = (LinearLayout) vg.findViewById(R.id.coursemap_progressbar_ll);
		cmlv = (ListView) vg.findViewById(R.id.coursemap_listview);
//		coursemaplinlay = (LinearLayout) vg.findViewById(R.id.coursemap_linlay);
		failure_view = (TextView) vg.findViewById(R.id.coursemap_error);
		
		absSubtitle.setText(folderName);	
		if(folderName != null)
		{	
			absTitle.setText(courseName);
		}
		
		cmlv.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				final String linkType = mainList.get(position).first.getLinkType();
				final String url = mainList.get(position).first.getViewUrl();
				final SherlockFragmentActivity act = getSherlockActivity();
				
				if(mainList.get(position).second.size() != 0) //a folder was clicked
				{	
	/*				Intent courseMapLaunch = new Intent(getString(R.string.coursemap_nest_intent), Uri.parse(position+""), CourseMapActivity.this, CourseMapActivity.class);
					courseMapLaunch.putExtra("mainList", mainList.get(position).second);
					if(itemNumber == -1 )//top-level, don't copy "Course Map"
						courseMapLaunch.putExtra("folderName", mainList.get(position).first.getName());
					else //chain onto the current folder name for "breadcrumbs"
						courseMapLaunch.putExtra("folderName", absSubtitle.getText() + "/" + mainList.get(position).first.getName());
					courseMapLaunch.putExtra("viewUri", mainList.get(position).first.getViewUrl());
					courseMapLaunch.putExtra("courseid", getIntent().getStringExtra("courseid"));
					courseMapLaunch.putExtra("coursename", getIntent().getStringExtra("coursename"));
					courseMapLaunch.putExtra("showViewInWeb", true);
					startActivity(courseMapLaunch);*/
					
					String path = "";
					if(itemNumber == -1 )//top-level, don't copy "Course Map"
						path = mainList.get(position).first.getName();
					else //chain onto the current folder name for "breadcrumbs"
						path = absSubtitle.getText() + "/" + mainList.get(position).first.getName();
					
					((FragmentLauncher)act).addFragment(BlackboardCourseMapFragment.this, 
							BlackboardCourseMapFragment.newInstance(getString(R.string.coursemap_nest_intent), 
									mainList.get(position).second, bbID, courseName, path, url, position, false));
					
					
				}
				else if(linkType.equals("resource/x-bb-file") || linkType.equals("resource/x-bb-document"))
				{
					String contentid = mainList.get(position).first.getContentId();
			/*		Intent bbItemLaunch = new Intent(null, null, CourseMapActivity.this, BlackboardDownloadableItemActivity.class);
					bbItemLaunch.putExtra("contentid", contentid);
					if(itemNumber == -1 )//top-level, don't copy "Course Map"
						bbItemLaunch.putExtra("itemName", mainList.get(position).first.getName()); //will be used as Subtitle
					else
						bbItemLaunch.putExtra("itemName", absSubtitle.getText() + "/" + mainList.get(position).first.getName()); //Subtitle
					bbItemLaunch.putExtra("viewUri", url);
					bbItemLaunch.putExtra("courseid", getIntent().getStringExtra("courseid"));
					bbItemLaunch.putExtra("coursename", getIntent().getStringExtra("coursename"));
					bbItemLaunch.putExtra("showViewInWeb", true);
					startActivity(bbItemLaunch);*/
					
					String itemName = "";
					if(itemNumber == -1 )//top-level, don't copy "Course Map"
						itemName = mainList.get(position).first.getName(); //will be used as Subtitle
					else
						itemName = absSubtitle.getText() + "/" + mainList.get(position).first.getName(); //Subtitle
					
					((FragmentLauncher)act).addFragment(BlackboardCourseMapFragment.this, 
							BlackboardDownloadableItemFragment.newInstance(contentid, bbID, courseName, itemName, url, false));
					
				}
				else if(linkType.equals("resource/x-bb-externallink"))
				{
					//((TextView)(actionbar.getCustomView())).setText((((TextView) actionbar.getCustomView()).getText()) + "/" + mainList.get(position).first.split("\\^")[0]);	
					Intent exItemLaunch = new Intent(Intent.ACTION_VIEW,Uri.parse(url));

					exItemLaunch.putExtra("courseid", bbID);
					exItemLaunch.putExtra("coursename", courseName);
					startActivity(exItemLaunch);
					//actionbar.setTitle(actionbar.getTitle()+"/"+mainList.get(position).first.split("\\^")[0]);
			/*	((FragmentLauncher)act).addFragment(BlackboardCourseMapFragment.this, 
							BlackboardExternalItemFragment.newInstance(Uri.parse(url), courseID, courseName)); */
					
				}
				else if(linkType.equals("student_gradebook"))
				{
			/*		Intent gradesLaunch = new Intent(null, null, CourseMapActivity.this, BlackboardGradesActivity.class);
					gradesLaunch.putExtra("viewUri", url);
					gradesLaunch.putExtra("courseid", getIntent().getStringExtra("courseid"));
					gradesLaunch.putExtra("coursename", getIntent().getStringExtra("coursename"));
					gradesLaunch.putExtra("showViewInWeb", true);
					startActivity(gradesLaunch);*/
					
					((FragmentLauncher)act).addFragment(BlackboardCourseMapFragment.this, 
							BlackboardGradesFragment.newInstance(bbID, courseName, url, false));
					
				}
				else if(linkType.equals("announcements"))
				{
		/*			Intent announcementsLaunch = new Intent(null, null, CourseMapActivity.this, BlackboardAnnouncementsActivity.class);
					announcementsLaunch.putExtra("viewUri", url);
					announcementsLaunch.putExtra("courseid", getIntent().getStringExtra("courseid"));
					announcementsLaunch.putExtra("coursename", getIntent().getStringExtra("coursename"));
					announcementsLaunch.putExtra("showViewInWeb", true);
					startActivity(announcementsLaunch);*/
					
					((FragmentLauncher)act).addFragment(BlackboardCourseMapFragment.this, 
							BlackboardAnnouncementsFragment.newInstance(bbID, courseName, url, false));
					
				}
				else //default to webview
				{
		/*			Intent bbItemLaunch = new Intent(null, Uri.parse(url), CourseMapActivity.this, BlackboardExternalItemActivity.class);
					bbItemLaunch.putExtra("mainList", mainList.get(position).second);
					if(itemNumber == -1 )//top-level, don't copy "Course Map"
						bbItemLaunch.putExtra("itemName", mainList.get(position).first.getName()); //will be used as Subtitle
					else
						bbItemLaunch.putExtra("itemName", absSubtitle.getText() + "/" + mainList.get(position).first.getName()); //Subtitle
					bbItemLaunch.putExtra("courseid", getIntent().getStringExtra("courseid"));
					bbItemLaunch.putExtra("coursename", getIntent().getStringExtra("coursename")); //will be used as Title
					startActivity(bbItemLaunch);*/
					
					String itemName = "";
					if(itemNumber == -1 )//top-level, don't copy "Course Map"
						itemName = mainList.get(position).first.getName(); //will be used as Subtitle
					else
						itemName = absSubtitle.getText() + "/" + mainList.get(position).first.getName(); //Subtitle
					
					((FragmentLauncher)act).addFragment(BlackboardCourseMapFragment.this, 
							BlackboardExternalItemFragment.newInstance(url, bbID, courseName, itemName, false));
					
				}
			}
		});
		
		//ONLY DO IF TOP LEVEL
		if(itemNumber==-1 && mainList == null)
		{
			fetch = new fetchCoursemapTask(httpclient);
			fetch.execute();
		}
		//now we've got the whole course tree, navigate as necessary
		else if(mainList!= null && mainList.size() != 0)
		{	
			cmlv.setAdapter(new CourseMapAdapter(getSherlockActivity(),mainList));
			cm_pb_ll.setVisibility(View.GONE);
	    	cmlv.setVisibility(View.VISIBLE);
		}		
		
		return vg;
		
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if(fetch!=null)
			fetch.cancel(true);
	}
	
	@Override
	public String getBbid()
	{
		return bbID;
	}
/*	@Override
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
	}*/
	@Override
	public void onSaveInstanceState(Bundle icicle) 
	{
		super.onSaveInstanceState(icicle);
//		icicle.putString("courseid", getIntent().getStringExtra("courseid"));
//		icicle.putString("coursename", getIntent().getStringExtra("coursename"));
	}
/*	private void showAreYouSureDlg(Context con)
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
	}*/
	@Override
	public String getCourseName() {
		return getArguments().getString("courseName");
	}
	
	@Override
	public boolean isFromDashboard() {
		return getArguments().getBoolean("fromDashboard");
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
		protected void onPreExecute()
		{
			cm_pb_ll.setVisibility(View.VISIBLE);
    		cmlv.setVisibility(View.GONE);
			failure_view.setVisibility(View.GONE);
		}
		
		@Override
		protected ArrayList doInBackground(Object... params)
		{
			HttpGet hget = new HttpGet("https://courses.utexas.edu/webapps/Bb-mobile-BBLEARN/courseMap?course_id="+bbID);
	    	String pagedata="";

	    	try
			{
				HttpResponse response = client.execute(hget);
		    	pagedata = EntityUtils.toString(response.getEntity());
			} catch (Exception e)
			{
				failureMessage = "UTilities could not fetch this course map";
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
				if(getSherlockActivity() != null)
					cmlv.setAdapter(new CourseMapAdapter(getSherlockActivity(),result));
				
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

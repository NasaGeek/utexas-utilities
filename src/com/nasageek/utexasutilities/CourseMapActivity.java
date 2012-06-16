package com.nasageek.utexasutilities;

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

import android.content.Context;
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
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import com.nasageek.utexasutilities.Pair;


public class CourseMapActivity extends SherlockActivity {

	
	private ActionBar actionbar;
	private DefaultHttpClient httpclient;
	private SharedPreferences settings;
	private ConnectionHelper ch;
	private LinearLayout cm_pb_ll;
	private LinearLayout coursemaplinlay;
	private ListView cmlv;
	private ArrayList<BBClass> classList;
	private ArrayList<Pair<String,ArrayList<BBClass>>> classSectionList;
	private fetchCoursemapTask fetch;
	private String bbid;
	private XMLReader xmlreader;
	private CourseMapSaxHandler courseMapSaxHandler;
	private int itemNumber;
	private ArrayList<Pair<String,ArrayList>> mainList;
	private View prior;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
			super.onCreate(savedInstanceState);
			setContentView(R.layout.coursemap_layout);
			actionbar = getSupportActionBar();
			actionbar.setDisplayShowCustomEnabled(true);
			actionbar.setHomeButtonEnabled(true);
			actionbar.setDisplayHomeAsUpEnabled(true);
			
			TextView titleView = new TextView(this);
			titleView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
			titleView.setLines(1);
			titleView.setTextSize(17);
			titleView.setPadding(0, 0, 7, 0);
			titleView.setSingleLine(true);
			titleView.setTextColor(Color.BLACK);
			titleView.setTypeface(Typeface.DEFAULT);
			titleView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
			actionbar.setCustomView(titleView);
			
			if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)	
				actionbar.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.actionbar_bg));
			actionbar.setTitle("");
			itemNumber=-1;
			bbid="";
			if(getString(R.string.coursemap_intent).equals(getIntent().getAction()))
			{
				bbid = getIntent().getDataString();
			}
			else if(getString(R.string.coursemap_nest_intent).equals(getIntent().getAction()))
			{
				mainList = (ArrayList<Pair<String, ArrayList>>) getIntent().getSerializableExtra("mainList");
				itemNumber = Integer.parseInt(getIntent().getDataString());		
			}
			if(getIntent().getStringExtra("folderName")!=null)
		//		actionbar.setTitle(getIntent().getStringExtra("folderName"));
			{	((TextView)(actionbar.getCustomView())).setText(getIntent().getStringExtra("folderName"));}
			
			
			cm_pb_ll = (LinearLayout) findViewById(R.id.coursemap_progressbar_ll);
			cmlv = (ListView) findViewById(R.id.coursemap_listview);
			coursemaplinlay = (LinearLayout) findViewById(R.id.coursemap_linlay);
			
			cmlv.setOnItemClickListener(new OnItemClickListener() {
				
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position,
						long id) {
					// TODO Auto-generated method stub
					if(mainList.get(position).second.size() != 0)
					{	
						Intent courseMapLaunch = new Intent(getString(R.string.coursemap_nest_intent), Uri.parse(position+""), CourseMapActivity.this, CourseMapActivity.class);
						courseMapLaunch.putExtra("mainList", mainList.get(position).second);
						courseMapLaunch.putExtra("folderName", ((TextView)(actionbar.getCustomView())).getText() + "/" + mainList.get(position).first.split("\\^")[0]);
						startActivity(courseMapLaunch);
					}
					else
					{
						//((TextView)(actionbar.getCustomView())).setText((((TextView) actionbar.getCustomView()).getText()) + "/" + mainList.get(position).first.split("\\^")[0]);	
						String url = mainList.get(position).first.split("\\^")[1];
						Intent bbItemLaunch = new Intent(null, Uri.parse(url), CourseMapActivity.this, BlackboardItemActivity.class);
						bbItemLaunch.putExtra("mainList", mainList.get(position).second);
						bbItemLaunch.putExtra("folderName", ((TextView)(actionbar.getCustomView())).getText() + "/" + mainList.get(position).first.split("\\^")[0]);
						startActivity(bbItemLaunch);
						
						
						//actionbar.setTitle(actionbar.getTitle()+"/"+mainList.get(position).first.split("\\^")[0]);
					}
		
				}
			});
			

			ch = new ConnectionHelper(this);
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
		////
			else if(mainList!= null && mainList.size() != 0)
			{
				cmlv.setAdapter(new CourseMapAdapter(this,mainList));
				cm_pb_ll.setVisibility(View.GONE);
	    		cmlv.setVisibility(View.VISIBLE);
			}		
	}
	
	
/*	@Override
	public void onBackPressed()
	{
		if( ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0) instanceof WebView)
		{	
			((ViewGroup)findViewById(android.R.id.content)).removeAllViews();
			setContentView(coursemaplinlay);
			((TextView)(actionbar.getCustomView())).setText((((TextView) actionbar.getCustomView()).getText().toString()).substring(0,(((TextView) actionbar.getCustomView()).getText()).toString().lastIndexOf("/")));	
		}
		else
			super.onBackPressed();
	}*/
	
	private class fetchCoursemapTask extends AsyncTask<Object,Void,ArrayList>
	{
		private DefaultHttpClient client;
		
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	ArrayList data=new ArrayList();
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
	     
	         }	          	    
	         	
			// TODO Auto-generated method stub
	    	
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

package com.nasageek.utexasutilities;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class BlackboardActivity extends SherlockActivity {
	
	private ActionBar actionbar;
	private DefaultHttpClient httpclient;
	private SharedPreferences settings;
	private ConnectionHelper ch;
	private TextView view;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		actionbar = getSupportActionBar();
		actionbar.setTitle("Blackboard");
		actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionbar.setHomeButtonEnabled(true);
		actionbar.setDisplayHomeAsUpEnabled(true);
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)	
    		actionbar.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.actionbar_bg));
		
		ch = new ConnectionHelper(this);
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		
		httpclient = ConnectionHelper.getThreadSafeClient();
		httpclient.getCookieStore().clear();
		BasicClientCookie cookie = new BasicClientCookie("s_session_id", ConnectionHelper.getBBAuthCookie(this,httpclient));
    	cookie.setDomain("courses.utexas.edu");
    	httpclient.getCookieStore().addCookie(cookie);

    	view = new TextView(this);
    	
    	setContentView(view);
    	new fetchClassesTask(httpclient).execute();
		
		
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	
	    /*
	    	
	    	Pattern pattern3 = Pattern.compile("(?<=\"center\">\\s{1,10})\\S.*(?=\\s*<)");
	    	Matcher matcher3 = pattern3.matcher(pagedata);
	    	Pattern pattern4 = Pattern.compile("(?<=\"right\">\\s).*(?=</td>\\s*<td)");
	    	Matcher matcher4 = pattern4.matcher(pagedata);
	    	Pattern datepattern = Pattern.compile("(?<=\"left\">\\s{1,10})\\S+");
	    	Matcher datematcher = datepattern.matcher(pagedata);
	    	Pattern balancepattern = Pattern.compile("(?<=\"right\">\\s).*(?=</td>\\s*</tr)");
	    	Matcher balancematcher = balancepattern.matcher(pagedata);
	    	if(balancematcher.find())
	    	{
	    		if(((Character)params[1]).equals('b'))
	    		{	
	    			bevobalance = balancematcher.group();
	    		}
	    		
	    			
	    	}
	    	while(matcher3.find() && matcher4.find() && datematcher.find())
	    	{
	    		String transaction=datematcher.group()+" ";
	    		transaction+=matcher3.group()+" ";
	    		transaction+=matcher4.group().replaceAll("\\s","");
	    		if(((Character)params[1]).equals('b'))
	    			btransactionlist.add(transaction);
	    		else
	    			Log.d("WTF MAN", "HOW DID YOU SCREW THIS UP");
	    	}
	    	timings.addSplit("parsed page");
			// TODO Auto-generated method stub
	    	*/
			return pagedata;
		}
		@Override
		protected void onPostExecute(String result)
		{
	
	//		Log.d("onPostExecute", "oh lawdz it's  postexecuting");
			if(!this.isCancelled())
	    	{
	    		view.setText(result);
				
	    		//bb_pb_ll.setVisibility(View.GONE);
	    		//bblv.setVisibility(View.VISIBLE);
	    	}
		}	
	}
}

package com.nasageek.UTilities;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TimingLogger;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;


public class DineinFragment extends SherlockFragment
{
	private  DefaultHttpClient httpclient;
	private ProgressBar pb;
	private LinearLayout d_pb_ll;
	private ConnectionHelper ch;
	private LinearLayout dineinlinlay;
	private ListView dlv;
	ArrayList<String> dtransactionlist, balancelist;
	String[] dtransactionarray;
	TimingLogger timings;
	int count;
	private Activity act;
	private boolean dfilled;
	ViewGroup cont;
	TextView tv1, tv2,tv3,tv4;
	View vg;
	
	String  dineinbalance="No Dine In Dollars? What kind of animal are you?";
	private SharedPreferences settings;
	
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		if(vg==null){
		vg =  inflater.inflate(R.layout.dinein_fragment_layout, container, false);
		
		
		dlv = (ListView) vg.findViewById(R.id.dtransactions_listview);
		
		dineinlinlay = (LinearLayout) vg.findViewById(R.id.dineinlinlay);

		d_pb_ll = (LinearLayout) vg.findViewById(R.id.dinein_progressbar_ll);
		

	    dineinlinlay.addView(tv1,0);
		dineinlinlay.addView(tv2,1);
		
		try
		{
			
			parser();
			
			timings.addSplit("parsed");
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			act.finish();
			return null;
		}
		return vg;
		}
		else
		{	((ViewGroup)(vg.getParent())).removeView(vg);
			return vg;
		}
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		
		Activity act = getActivity();
		settings = PreferenceManager.getDefaultSharedPreferences(act);
		
		tv1 = new TextView(act);
		tv2 = new TextView(act);

		
		
		timings = new TimingLogger("Timing", "Balance OnCreate");
		
		ch = new ConnectionHelper(act);

		
		dtransactionlist = new ArrayList<String>();


		
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler(){
			public void uncaughtException(Thread thread, Throwable ex)
			{
				// TODO Auto-generated method stub
				Log.e("UNCAUGHT",ex.getMessage(),ex);
				//finish();
				return;
			}});
	
		try
			{
				
		//		parser();
				
				timings.addSplit("parsed");
				
			}
			catch(Exception e)
			{
				e.printStackTrace();
				act.finish();
				return;
			}
			tv1.setGravity(0x01);		
		  	tv2.setGravity(0x01);
		  			
		 	tv1.setTextSize(20);
		 	 	
		    tv1.setTextColor(Color.DKGRAY);
		    tv2.setTextColor(Color.DKGRAY);

		/*  dineinlinlay.addView(tv1,0);
			dineinlinlay.addView(tv2,1);*/


	}
	
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) 
	{
	        menu.removeItem(R.id.balance_refresh);
			inflater.inflate(R.layout.balance_menu, menu);
	        
	}
	public boolean onOptionsItemSelected(MenuItem item)
	{
	    	int id = item.getItemId();
	    	switch(id)
	    	{
		   
	    		case R.id.balance_refresh:
	    			
	    			
					dlv.setVisibility(View.GONE);
					
					d_pb_ll.setVisibility(View.VISIBLE);
	    		try
				{
	    			dtransactionlist.clear();
					dineinbalance = "No Dine In Dollars? What kind of animal are you?";
					parser();	
					
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
	    			break;
	    	}
	    	return true;
	}
	public void parser() throws Exception
    {
		
		httpclient = ConnectionHelper.getThreadSafeClient();
		httpclient.getCookieStore().clear();
				
		BasicClientCookie screen = new BasicClientCookie("webBrowserSize", "B");
    	screen.setDomain(".utexas.edu");
    	httpclient.getCookieStore().addCookie(screen);
    	BasicClientCookie cookie = new BasicClientCookie("SC", ConnectionHelper.getAuthCookie(act,httpclient));
    	cookie.setDomain(".utexas.edu");
    	httpclient.getCookieStore().addCookie(cookie);
		timings.addSplit("logged in");
		
		
	
    	new fetchTransactionDataTask(httpclient).execute("rRequestSw",'d');
    	
    	timings.addSplit("parsed page");
    }
	private class fetchTransactionDataTask extends AsyncTask<Object,Void,Character>
	{
		private DefaultHttpClient client;
		
		public fetchTransactionDataTask(DefaultHttpClient client)
		{
			this.client = client;
		}
		
		@Override
		protected Character doInBackground(Object... params)
		{
	//		DefaultHttpClient httpclient = ch.getThreadSafeClient();

			HttpPost hpost = new HttpPost("https://utdirect.utexas.edu/hfis/transactions.WBX");
	    	String pagedata="";
	    	
	    	
	    	List<BasicNameValuePair> postdata = new ArrayList<BasicNameValuePair>();
	    	postdata.add(new BasicNameValuePair((String) params[0], "B"));
	    	
	    	try
			{
				hpost.setEntity(new UrlEncodedFormEntity(postdata));
				HttpResponse response = client.execute(hpost);
		    	pagedata = EntityUtils.toString(response.getEntity());
			} catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	
	    	timings.addSplit("got page");
	    	
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
	    		if (((Character)params[1]).equals('d'))
	    		{	
	    			dineinbalance = balancematcher.group();
	    		}
	    			
	    	}
	    	while(matcher3.find() && matcher4.find() && datematcher.find())
	    	{
	    		String transaction=datematcher.group()+" ";
	    		transaction+=matcher3.group()+" ";
	    		transaction+=matcher4.group().replaceAll("\\s","");
	    		if (((Character)params[1]).equals('d'))
	    			dtransactionlist.add(transaction);
	    		else
	    			Log.d("WTF MAN", "HOW DID YOU SCREW THIS UP");
	    	}
	    	timings.addSplit("parsed page");
			// TODO Auto-generated method stub
	    	
			return (Character) params[1];
		}
		@Override
		protected void onPostExecute(Character result)
		{
	
	//		Log.d("onPostExecute", "oh lawdz it's  postexecuting");
			
			if ((result).equals('d'))
	    	{
	    		dfilled = true;
	    		dlv.setAdapter(new TransactionAdapter(DineinFragment.this.getActivity(), dtransactionlist));
	    		
	    		tv1.setText("Dine In Dollars ");
				tv2.setText(dineinbalance);
	    		
	    		d_pb_ll.setVisibility(View.GONE);
				dlv.setVisibility(View.VISIBLE);
	    		
	    	} 	
		}	
	}
}
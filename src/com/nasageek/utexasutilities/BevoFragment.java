package com.nasageek.utexasutilities;

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
import com.actionbarsherlock.app.SherlockFragmentActivity;
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


public class BevoFragment extends SherlockFragment
{
	private  DefaultHttpClient httpclient;
	private ProgressBar pb;
	private LinearLayout b_pb_ll;
	private ConnectionHelper ch;
	private LinearLayout bevolinlay;
	private ListView blv;
	ArrayList<String> btransactionlist;
	String[] btransactionarray;
	TimingLogger timings;
	int count;
	private boolean bfilled;
	TextView tv1, tv2,tv3,tv4;
	private SherlockFragmentActivity parentAct;
	String bevobalance="";
	ViewGroup cont;
	View vg;
	private SharedPreferences settings;
	private fetchTransactionDataTask fetch;	
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
	
		if(vg==null)
		{vg = inflater.inflate(R.layout.bevo_fragment_layout, container, false);
		
		blv = (ListView) vg.findViewById(R.id.btransactions_listview);
		
		bevolinlay = (LinearLayout) vg.findViewById(R.id.bevolinlay);

		b_pb_ll = (LinearLayout) vg.findViewById(R.id.bevo_progressbar_ll);
		

	    bevolinlay.addView(tv3,0);
		bevolinlay.addView(tv4,1);
		
		try
		{
			
			parser();
			
			timings.addSplit("parsed");
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			parentAct.finish();
			return null;
		}
		
		
		return vg;}
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
		parentAct = this.getSherlockActivity();
	//	setContentView(R.layout.balance_layout);
		settings = PreferenceManager.getDefaultSharedPreferences(parentAct);
		
	
		tv3 = new TextView(parentAct);
		tv4 = new TextView(parentAct);
		
		
		timings = new TimingLogger("Timing", "Balance OnCreate");
		
		ch = new ConnectionHelper(parentAct);
	//	blv = (ListView) act.findViewById(R.id.btransactions_listview);
		
		
	//	bevolinlay = (LinearLayout) act.findViewById(R.id.bevolinlay);

		btransactionlist = new ArrayList<String>();
		
	//	b_pb_ll = (LinearLayout) act.findViewById(R.id.bevo_progressbar_ll);
		
		
		
		
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
				parentAct.finish();
				return;
			}
			tv3.setGravity(0x01);
		  	tv4.setGravity(0x01);
		  	
		 	tv3.setTextSize(20);
		 	
		    tv3.setTextColor(Color.DKGRAY);
		    tv4.setTextColor(Color.DKGRAY);
		    		
		

	//	    bevolinlay.addView(tv3,0);
	//		bevolinlay.addView(tv4,1);
	//	    timings.addSplit("views set up");	
	
		
		
	//	timings.addSplit("set both adapters");		
		
	//	timings.addSplit("set up tabs");
	//	timings.dumpToLog();
	}
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) 
	{
		 	menu.removeItem(R.id.balance_refresh);
			inflater.inflate(R.layout.balance_menu, menu);       
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
	    	int id = item.getItemId();
	    	switch(id)
	    	{
	    		case R.id.balance_refresh:
	    			
	    			blv.setVisibility(View.GONE);
					
					b_pb_ll.setVisibility(View.VISIBLE);
					
	    		try
				{
	    			
					btransactionlist.clear();
					bevobalance = "";
					
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
    	BasicClientCookie cookie = new BasicClientCookie("SC", ConnectionHelper.getAuthCookie(parentAct,httpclient));
    	cookie.setDomain(".utexas.edu");
    	httpclient.getCookieStore().addCookie(cookie);
		timings.addSplit("logged in");
		fetch = new fetchTransactionDataTask(httpclient);
		
    	fetch.execute("sRequestSw",'b');
    	
    	timings.addSplit("parsed page");
  	
    }
	
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		fetch.cancel(true);
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
	    	
			return (Character) params[1];
		}
		@Override
		protected void onPostExecute(Character result)
		{
	
	//		Log.d("onPostExecute", "oh lawdz it's  postexecuting");
			if((result).equals('b') && !this.isCancelled())
	    	{
	    		bfilled = true;
	    		blv.setAdapter(new TransactionAdapter(parentAct, btransactionlist));	
	    		
	    		tv3.setText("Bevo Bucks ");
				tv4.setText(bevobalance);
				
	    		b_pb_ll.setVisibility(View.GONE);
	    		blv.setVisibility(View.VISIBLE);
	    	}
		}	
	}
}

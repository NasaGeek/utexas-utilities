package com.nasageek.UTilities;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TimingLogger;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TwoLineListItem;
import android.widget.TabHost.TabContentFactory;


public class BalanceActivity extends Activity {
		
	private  DefaultHttpClient httpclient;
	private ProgressDialog pd;
	private ProgressBar pb;
	private LinearLayout b_pb_ll, d_pb_ll;
	private ConnectionHelper ch;
	private LinearLayout bevolinlay,dineinlinlay;
	private ListView blv,dlv;
	ArrayList<String> dtransactionlist, btransactionlist, balancelist;
	String[] dtransactionarray, btransactionarray;
	private File transfile;
	TimingLogger timings;
	int count;
	private boolean bfilled, dfilled;
	TextView tv1, tv2,tv3,tv4;
	String bevobalance="", dineinbalance="No Dine In Dollars? What kind of animal are you?";
	private SharedPreferences settings;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.balance_layout);
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		
		tv1 = new TextView(getBaseContext());
		tv3 = new TextView(getBaseContext());
		tv2 = new TextView(getBaseContext());
		tv4 = new TextView(getBaseContext());
		
		
		timings = new TimingLogger("Timing", "Balance OnCreate");
		
		ch = new ConnectionHelper(this);
		blv = (ListView) findViewById(R.id.btransactions_listview);
		dlv = (ListView) findViewById(R.id.dtransactions_listview);
		
		bevolinlay = (LinearLayout) findViewById(R.id.bevolinlay);
		dineinlinlay = (LinearLayout) findViewById(R.id.dineinlinlay);
		
		dtransactionlist = new ArrayList<String>();
		btransactionlist = new ArrayList<String>();
		
		b_pb_ll = (LinearLayout) findViewById(R.id.bevo_progressbar_ll);
		d_pb_ll = (LinearLayout) findViewById(R.id.dinein_progressbar_ll);
		
		
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler(){
			public void uncaughtException(Thread thread, Throwable ex)
			{
				// TODO Auto-generated method stub
				Log.e("UNCAUGHT",ex.getMessage(),ex);
				finish();
				return;
			}});
		

/*		try {
			transfile = this.getFileStreamPath("transactions.tmp");
			
	//		Log.d("Create", "BalanceActivity created");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
	//		Log.e("TEMPFILE", "Couldn't make the temp file");
		}
		timings.addSplit("pre-parser");*/
		
		if(true)//!transfile.exists() )
		{
		//	pd = ProgressDialog.show(BalanceActivity.this, "", "Loading...");
			try
			{
				
				parser();
				
				timings.addSplit("parsed");
				
			}
			catch(Exception e)
			{
				e.printStackTrace();
				finish();
				return;
			}
		}
	/*	else
		{	readData();

				tv1.setText("Dine In Dollars ");
				tv2.setText(dineinbalance);
				tv3.setText("Bevo Bucks ");
				tv4.setText(bevobalance);
			
			dlv.setAdapter(new TransactionAdapter(BalanceActivity.this, dtransactionlist));
			blv.setAdapter(new TransactionAdapter(BalanceActivity.this, btransactionlist));
		}*/
		
			
			
			
			tv1.setGravity(0x01);
			tv3.setGravity(0x01);
			
		  	tv2.setGravity(0x01);
		  	tv4.setGravity(0x01);
		  	
			
			
		 	tv1.setTextSize(20);
		 	tv3.setTextSize(20);
		    
		    		
		 
		    dineinlinlay.addView(tv1,0);
			dineinlinlay.addView(tv2,1);

		    bevolinlay.addView(tv3,0);
			bevolinlay.addView(tv4,1);
	//	    timings.addSplit("views set up");	
	
		
		
	//	timings.addSplit("set both adapters");
		
		TabHost tabHost = (TabHost)findViewById(R.id.baltabhost); // The activity TabHost
		tabHost.setup();
		TabHost.TabSpec spec=tabHost.newTabSpec("tag1");
		spec.setIndicator("Dine In Dollars");
		spec.setContent(R.id.dineinlinlay);
		tabHost.addTab(spec);
		spec = tabHost.newTabSpec("tag2");
		spec.setIndicator("Bevo Bucks");
		spec.setContent(R.id.bevolinlay);
		tabHost.addTab(spec);
		
		
		tabHost.setCurrentTab(0);
		
		
	//	timings.addSplit("set up tabs");
	//	timings.dumpToLog();
	}
	public void parser() throws Exception
    {
		
		httpclient = ConnectionHelper.getThreadSafeClient();
		httpclient.getCookieStore().clear();
	/*	if(settings.getBoolean("loginpref", true))
		{
			if(!ch.Login(this, httpclient))	
			{	
				finish();
				return;
			}
		}*/
		
	/*	if(ConnectionHelper.getAuthCookie(httpclient,this).equals(""))
		{
			finish();
			return;
		}*/
		
			
		BasicClientCookie screen = new BasicClientCookie("webBrowserSize", "B");
    	screen.setDomain(".utexas.edu");
    	httpclient.getCookieStore().addCookie(screen);
    	BasicClientCookie cookie = new BasicClientCookie("SC", ConnectionHelper.getAuthCookie(this,httpclient));
    	cookie.setDomain(".utexas.edu");
    	httpclient.getCookieStore().addCookie(cookie);
		timings.addSplit("logged in");
		
		
	//	new fetchBalanceDataTask(httpclient).execute();
    	
    	new fetchTransactionDataTask(httpclient).execute("sRequestSw",'b');
    	
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
	    		if(((Character)params[1]).equals('b'))
	    		{	bevobalance = balancematcher.group();
	    			}
	    		else if (((Character)params[1]).equals('d'))
	    		{	dineinbalance = balancematcher.group();
	    			}
	    			
	    	}
	    	
	    	while(matcher3.find() && matcher4.find() && datematcher.find())
	    	{
	    		
	    		
	    		String transaction=datematcher.group()+" ";
	    		transaction+=matcher3.group()+" ";
	    		transaction+=matcher4.group().replaceAll("\\s","");
	    		if(((Character)params[1]).equals('b'))
	    			btransactionlist.add(transaction);
	    		else if (((Character)params[1]).equals('d'))
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
			if((result).equals('b'))
	    	{
	    		bfilled = true;
	    		blv.setAdapter(new TransactionAdapter(BalanceActivity.this, btransactionlist));	
	    		
	    		tv3.setText("Bevo Bucks ");
				tv4.setText(bevobalance);
				
	    		b_pb_ll.setVisibility(View.GONE);
	    		blv.setVisibility(View.VISIBLE);
	    	}
	    	else if ((result).equals('d'))
	    	{
	    		dfilled = true;
	    		dlv.setAdapter(new TransactionAdapter(BalanceActivity.this, dtransactionlist));
	    		
	    		tv1.setText("Dine In Dollars ");
				tv2.setText(dineinbalance);
	    		
	    		d_pb_ll.setVisibility(View.GONE);
				dlv.setVisibility(View.VISIBLE);
	    		
	    	}
	//    	if(bfilled && dfilled)
	//    	{
	//    		bfilled = dfilled = false;
	    /*		try
				{
					writeData(btransactionlist, dtransactionlist, bevobalance, dineinbalance);
				} catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
				
		    	
				
			//	if(pd.isShowing())
	    	//		pd.dismiss();
				
				
				
				
	//    	}
	    	
	    	
		}	
	}
	
/*	private class fetchBalanceDataTask extends AsyncTask
	{

		private DefaultHttpClient client;
		
		public fetchBalanceDataTask(DefaultHttpClient client)
		{
			this.client = client;
		}
		
		@Override
		protected Object doInBackground(Object... arg0)
		{
	//		DefaultHttpClient httpclient = ch.getThreadSafeClient();
			HttpGet hget = new HttpGet("https://utdirect.utexas.edu/hfis/diningDollars.WBX");
	    	
			HttpResponse response;
			String pagedata="";
			try
			{
				response = client.execute(hget);
				pagedata = EntityUtils.toString(response.getEntity());
			} catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	timings.addSplit("got page");
	    	
	    		
	    	Pattern balancepattern = Pattern.compile("</tr>\\s*<tr.*>\\s*<td(?:\\s)?>(.*)</td>");
	//    	Pattern balancepattern = Pattern.compile("(?<=\"datarow\">\\s{0,10}<td >).*(?=<)");
	    	Matcher balancecategorymatcher = balancepattern.matcher(pagedata);
	    	ArrayList<String> datarows = new ArrayList<String>();
	    	ArrayList<String> balancerows = new ArrayList<String>();
	    	while(balancecategorymatcher.find())
	    	{
	    		datarows.add(balancecategorymatcher.group(1));
	    	}
	    	balancepattern = Pattern.compile("(?<=<td(?:\\s)?>)\\$.*(?=<)");
	    	Matcher balancematcher = balancepattern.matcher(pagedata);
	    	while(balancematcher.find())
	    	{
	    		balancerows.add(balancematcher.group());
	    	}
	    	timings.addSplit("parsed page");
	    	for(int x = 0; x<datarows.size();x++)
	    	{
	    		balancelist.add(datarows.get(x));
	    		balancelist.add(balancerows.get(x));
	    	}
	    	
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		protected void onPostExecute(Object result)
		{
	//		Log.d("onPostExecute", "oh lawdz it's  postexecuting");
			balfilled = true;
	    	if(bfilled && dfilled && balfilled)
	    	{
	    		bfilled = dfilled = balfilled = false;
	    		try
				{
					writeData(btransactionlist, dtransactionlist,  bevobalance, dineinbalance);
				} catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(pd.isShowing())
	    			pd.dismiss();
	    		
	    	}
	    	if(balancelist.size()==4)
	    	{	tv1.setText(balancelist.get(0));
				tv2.setText(balancelist.get(1));
				tv3.setText(balancelist.get(2));
				tv4.setText(balancelist.get(3));}
	    	else if(balancelist.size()==2)
	    	{
	    		tv3.setText(balancelist.get(0));
				tv4.setText(balancelist.get(1));
	    	}
		}
		
	}*/
	
	public void readData()
	{
		try {
			BufferedInputStream  stin = new BufferedInputStream(new FileInputStream(transfile));
			int b;
			count=0;
			do
			{
				StringBuilder data=new StringBuilder();
				do
				{
					b = stin.read();
					data.append((char)b);
				}
				while(b!=0xD && b!=0xFF);
				
				dtransactionlist.add(data.substring(0,data.length()-1));
			}
			while(b!=0xFF);
			do
			{
				StringBuilder data=new StringBuilder();
				do
				{
					b = stin.read();
					data.append((char)b);
				}
				while(b!=0xD && b!=0xFF );
				
				btransactionlist.add(data.substring(0,data.length()-1));
				
			}
			while(b!=0xFF);
			do
			{
				StringBuilder data=new StringBuilder();
				do
				{
					b = stin.read();
					data.append((char)b);
				}
				while(b!=0xD && b!=0xFF );
				
				if(b!=0xFF)bevobalance=(data.substring(0,data.length()-1));
				
			}
			while(b!=0xFF);
			do
			{
				StringBuilder data=new StringBuilder();
				do
				{
					b = stin.read();
					data.append((char)b);
				}
				while(b!=0xD && b!=-1);
				
				if(b!=-1)dineinbalance = (data.substring(0,data.length()-1));
				
			}
			while(b!=-1);
			timings.addSplit("file read");
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();	
		}
		
		dtransactionlist.remove(dtransactionlist.size()-1);
		dtransactionlist.trimToSize();
		btransactionlist.remove(btransactionlist.size()-1);
		btransactionlist.trimToSize();

	}
	
	public void writeData(ArrayList<String> btransactionwritelist, ArrayList<String> dtransactionwritelist, String bevobalancewrite, String dineinbalancewrite) throws IOException
	{
		BufferedOutputStream stout = new BufferedOutputStream(openFileOutput("transactions.tmp",0),1024);
    	
    	for(int k =0; k<dtransactionwritelist.size(); k++)
    	{
    		stout.write((dtransactionwritelist.get(k)+"\r").getBytes());
    	}
    	stout.write(0xFF);
    	
    	for(int k =0; k<btransactionwritelist.size(); k++)
    	{
    		stout.write((btransactionwritelist.get(k)+"\r").getBytes());
    	}
    	stout.write(0xFF);

    		stout.write((bevobalancewrite+"\r").getBytes());
    		stout.write(0xFF);
    		stout.write((dineinbalancewrite+"\r").getBytes());
    	stout.flush();
    	stout.close();
    	
  //  	Log.d("FILES", "transactions.tmp written, size: "+transfile.length());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
	        MenuInflater inflater = getMenuInflater();
	        inflater.inflate(R.layout.balance_menu, menu);
	        return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
	    	int id = item.getItemId();
	    	switch(id)
	    	{
	    		case R.id.balance_refresh:
	    			
	    			blv.setVisibility(View.GONE);
					dlv.setVisibility(View.GONE);
					b_pb_ll.setVisibility(View.VISIBLE);
					d_pb_ll.setVisibility(View.VISIBLE);
	    		try
				{
					
	    			
	    			dtransactionlist.clear();
					btransactionlist.clear();
					bevobalance = "";
					dineinbalance = "No Dine In Dollars? What kind of animal are you?";
					parser();	
					
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
		
	    			//new deleteFileTask().execute();
	    			break;
	    	}
	    	return true;
	}
	
	private class deleteFileTask extends AsyncTask
	{
		protected void onPreExecute()
		{
			blv.setVisibility(View.GONE);
			dlv.setVisibility(View.GONE);
			b_pb_ll.setVisibility(View.VISIBLE);
			d_pb_ll.setVisibility(View.VISIBLE);
			
			//	pd = ProgressDialog.show(BalanceActivity.this, "", "Refreshing...");
		}
		
		
		@Override
		protected Object doInBackground(Object... params) {
			// TODO Auto-generated method stub
			transfile.delete();
			return null;
		}
		
		protected void onPostExecute(Object result)
		{
			
			
			try
			{
				
				dtransactionlist.clear();
				btransactionlist.clear();
				bevobalance = "";
				dineinbalance = "No Dine In Dollars? What kind of animal are you?";
				parser();	
				
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
		}
		
	}
}

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
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.TimingLogger;
import android.view.LayoutInflater;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import android.view.View;

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


public class BalanceActivity extends SherlockActivity implements ActionBar.TabListener{
		
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
	ActionBar actionbar;
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
		
		actionbar = getSupportActionBar();
		actionbar.setTitle("Transactions");
//		actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionbar.setHomeButtonEnabled(true);
		actionbar.setDisplayHomeAsUpEnabled(true);
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)	
    		actionbar.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.actionbar_bg));
		
	//	 actionbar.addTab(actionbar.newTab()
	//	            .setText("Home"));
		   //         .setTabListener(new TabListener<DineinFragment>(
		   //                 this, "home", DineinFragment.class, null)));

		//    actionbar.addTab(actionbar.newTab()
		  //          .setText("Inventory"));
		 //           .setTabListener(new TabListener<InventoryFragment>(
		 //                   this, "inventory", InventoryFragment.class, null)));
		
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler(){
			public void uncaughtException(Thread thread, Throwable ex)
			{
				// TODO Auto-generated method stub
				Log.e("UNCAUGHT",ex.getMessage(),ex);
				finish();
				return;
			}});
	
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
			tv1.setGravity(0x01);
			tv3.setGravity(0x01);
			
		  	tv2.setGravity(0x01);
		  	tv4.setGravity(0x01);
		  	
			
			
		 	tv1.setTextSize(20);
		 	tv3.setTextSize(20);
		 	
		    tv1.setTextColor(Color.DKGRAY);
		    tv2.setTextColor(Color.DKGRAY);
		    tv3.setTextColor(Color.DKGRAY);
		    tv4.setTextColor(Color.DKGRAY);
		    		
		 
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
	    		{	
	    			bevobalance = balancematcher.group();
	    		}
	    		else if (((Character)params[1]).equals('d'))
	    		{	
	    			dineinbalance = balancematcher.group();
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
		}	
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
			MenuInflater inflater = this.getSupportMenuInflater();
	        inflater.inflate(R.layout.balance_menu, menu);
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
		            Intent home = new Intent(this, UTilitiesActivity.class);
		            home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		            startActivity(home);break;
	    	
	    	
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
	public void onTabSelected(Tab tab) {
		// TODO Auto-generated method stub
		
	}
	public void onTabUnselected(Tab tab) {
		// TODO Auto-generated method stub
		
	}
	public void onTabReselected(Tab tab) {
		// TODO Auto-generated method stub
		
	}
}
class DineinFragment extends FragmentActivity
{
	private  DefaultHttpClient httpclient;
	private ProgressBar pb;
	private LinearLayout d_pb_ll;
	private ConnectionHelper ch;
	private LinearLayout dineinlinlay;
	private ListView blv,dlv;
	ArrayList<String> dtransactionlist, balancelist;
	String[] dtransactionarray;
	private File transfile;
	TimingLogger timings;
	int count;
	private boolean dfilled;
	TextView tv1, tv2,tv3,tv4;
	
	String  dineinbalance="No Dine In Dollars? What kind of animal are you?";
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

		dlv = (ListView) findViewById(R.id.dtransactions_listview);
		
		
		dineinlinlay = (LinearLayout) findViewById(R.id.dineinlinlay);
		
		dtransactionlist = new ArrayList<String>();

		d_pb_ll = (LinearLayout) findViewById(R.id.dinein_progressbar_ll);
		
		
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler(){
			public void uncaughtException(Thread thread, Throwable ex)
			{
				// TODO Auto-generated method stub
				Log.e("UNCAUGHT",ex.getMessage(),ex);
				finish();
				return;
			}});
	
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
			tv1.setGravity(0x01);
			tv3.setGravity(0x01);
			
		  	tv2.setGravity(0x01);
		  	tv4.setGravity(0x01);
		  	
			
			
		 	tv1.setTextSize(20);
		 	tv3.setTextSize(20);
		 	
		    tv1.setTextColor(Color.DKGRAY);
		    tv2.setTextColor(Color.DKGRAY);
		    tv3.setTextColor(Color.DKGRAY);
		    tv4.setTextColor(Color.DKGRAY);
		    		
		 
		    dineinlinlay.addView(tv1,0);
			dineinlinlay.addView(tv2,1);


	}
	public void parser() throws Exception
    {
		
		httpclient = ConnectionHelper.getThreadSafeClient();
		httpclient.getCookieStore().clear();
				
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
	    		dlv.setAdapter(new TransactionAdapter(DineinFragment.this, dtransactionlist));
	    		
	    		tv1.setText("Dine In Dollars ");
				tv2.setText(dineinbalance);
	    		
	    		d_pb_ll.setVisibility(View.GONE);
				dlv.setVisibility(View.VISIBLE);
	    		
	    	} 	
		}	
	}
}

class TabListener<T extends Fragment> implements ActionBar.TabListener {
    private final FragmentActivity mActivity;
    private final String mTag;
    private final Class mClass;
    private final Bundle mArgs;
    private Fragment mFragment;




    public TabListener(FragmentActivity activity, String tag, Class clz, Bundle args) {
        mActivity = activity;
        mTag = tag;
        mClass = clz;
        mArgs = args;
        FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();


        // Check to see if we already have a fragment for this tab, probably
        // from a previously saved state.  If so, deactivate it, because our
        // initial state is that a tab isn't shown.
        mFragment = mActivity.getSupportFragmentManager().findFragmentByTag(mTag);
        if (mFragment != null && !mFragment.isDetached()) {
            ft.detach(mFragment);
        }
    }

    public void onTabSelected(Tab tab) {
        FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();

        if (mFragment == null) {
            mFragment = Fragment.instantiate(mActivity, mClass.getName(), mArgs);
            ft.add(android.R.id.content, mFragment, mTag);
            ft.commit();
        } else {
            ft.attach(mFragment);
            ft.commit();
        }
    }

    public void onTabUnselected(Tab tab) {
        FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();

        if (mFragment != null) {
            ft.detach(mFragment);
            ft.commitAllowingStateLoss();
        }           
    }

    public void onTabReselected(Tab tab) {

    }

}

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
import android.view.ViewGroup;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;
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
import android.widget.Toast;
import android.widget.TwoLineListItem;
import android.widget.TabHost.TabContentFactory;


public class BalanceActivity extends SherlockFragmentActivity 
{	
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
	
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		

		timings = new TimingLogger("Timing", "Balance OnCreate");
		
		ch = new ConnectionHelper(this);
	
		actionbar = getSupportActionBar();
		actionbar.setTitle("Transactions");
		actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionbar.setHomeButtonEnabled(true);
		actionbar.setDisplayHomeAsUpEnabled(true);
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)	
    		actionbar.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.actionbar_bg));
		
		 actionbar.addTab(actionbar.newTab()
		            .setText("Dinein")
		            .setTabListener(new TabListener<DineinFragment>(
		                    this, "dinein", DineinFragment.class, null)));

		    actionbar.addTab(actionbar.newTab()
		            .setText("Bevo Bucks")
		            .setTabListener(new TabListener<BevoFragment>(
		                    this, "bevo", BevoFragment.class, null)));
		
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
			
				timings.addSplit("parsed");	
			}
			catch(Exception e)
			{
				e.printStackTrace();
				finish();
				return;
			}
	
	//	    timings.addSplit("views set up");	
	
		
		
	//	timings.addSplit("set both adapters");
		
	
		
	//	timings.addSplit("set up tabs");
	//	timings.dumpToLog();
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
	    	return true;
	}

	public static class TabListener<T extends Fragment> implements ActionBar.TabListener 
	{
        private final SherlockFragmentActivity mActivity;
        private final String mTag;
        private final Class<T> mClass;
        private final Bundle mArgs;
        private Fragment mFragment;

        public TabListener(SherlockFragmentActivity activity, String tag, Class<T> clz) {
            this(activity, tag, clz, null);
        }

        public TabListener(SherlockFragmentActivity activity, String tag, Class<T> clz, Bundle args) {
            mActivity = activity;
            mTag = tag;
            mClass = clz;
            mArgs = args;

            // Check to see if we already have a fragment for this tab, probably
            // from a previously saved state.  If so, deactivate it, because our
            // initial state is that a tab isn't shown.
            mFragment = mActivity.getSupportFragmentManager().findFragmentByTag(mTag);
            if (mFragment != null && !mFragment.isDetached()) {
                FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
                ft.detach(mFragment);
                ft.commit();
            }
        }

        public void onTabSelected(Tab tab, FragmentTransaction fta) {
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

        public void onTabUnselected(Tab tab, FragmentTransaction fta) {
        	FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
        	if (mFragment != null) {
                ft.detach(mFragment);
                ft.commit();
            }
        }

        public void onTabReselected(Tab tab, FragmentTransaction fta) {
        	FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
            Toast.makeText(mActivity, "Reselected!", Toast.LENGTH_SHORT).show();
        }
    
	}
}
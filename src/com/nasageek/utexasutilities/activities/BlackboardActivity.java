package com.nasageek.utexasutilities.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.crittercism.app.Crittercism;
import com.foound.widget.AmazingListView;
import com.nasageek.utexasutilities.ConnectionHelper;
import com.nasageek.utexasutilities.Pair;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.Utility;
import com.nasageek.utexasutilities.R.id;
import com.nasageek.utexasutilities.R.layout;
import com.nasageek.utexasutilities.R.string;
import com.nasageek.utexasutilities.adapters.BBClassAdapter;
import com.nasageek.utexasutilities.adapters.MultiPanePagerAdapter;
import com.nasageek.utexasutilities.fragments.BlackboardCourseListFragment;
import com.nasageek.utexasutilities.fragments.BlackboardDashboardFragment;
import com.nasageek.utexasutilities.fragments.MenuFragment;
import com.nasageek.utexasutilities.model.BBClass;
import com.viewpagerindicator.TabPageIndicator;

public class BlackboardActivity extends SherlockFragmentActivity {
	
	private ActionBar actionbar;
	private MultiPanePagerAdapter mPagerAdapter;
	private ViewPager pager;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
    	setContentView(R.layout.blackboard_layout);

    	actionbar = getSupportActionBar();
		actionbar.setTitle("Blackboard");
		actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionbar.setHomeButtonEnabled(true);
		actionbar.setDisplayHomeAsUpEnabled(true);
		
		initialisePaging();
		
    	 Crittercism.leaveBreadcrumb("Loaded BlackboardActivity");
	}
	private void initialisePaging() {
		
        List<SherlockFragment> fragments = new Vector<SherlockFragment>();
        pager = (ViewPager)findViewById(R.id.viewpager);
        
        if(getSupportFragmentManager().findFragmentByTag(Utility.makeFragmentName(pager.getId(), 0)) != null) {
        	
        	fragments.add((SherlockFragment)getSupportFragmentManager().findFragmentByTag(Utility.makeFragmentName(pager.getId(), 0)));
        	fragments.add((SherlockFragment)getSupportFragmentManager().findFragmentByTag(Utility.makeFragmentName(pager.getId(), 1)));
        }
        else {
        	fragments.add(BlackboardCourseListFragment.newInstance("Course List"));
            fragments.add(BlackboardDashboardFragment.newInstance("Dashboard"));
        }
        
        final TabPageIndicator tabIndicator = (TabPageIndicator)findViewById(R.id.titles);
        
        mPagerAdapter = new MultiPanePagerAdapter(getSupportFragmentManager(), fragments);
        mPagerAdapter.setPagesDisplayed(1);
        
        pager.setAdapter(mPagerAdapter);
		tabIndicator.setViewPager(pager);

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
    	}
    	return false;
	}

}

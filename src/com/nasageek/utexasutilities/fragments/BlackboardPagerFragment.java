package com.nasageek.utexasutilities.fragments;

import java.util.List;
import java.util.Vector;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.MenuItem;
import com.crittercism.app.Crittercism;
import com.mapsaurus.paneslayout.PanesActivity;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.Utility;
import com.nasageek.utexasutilities.adapters.MultiPanePagerAdapter;
import com.viewpagerindicator.TabPageIndicator;


public class BlackboardPagerFragment extends SherlockFragment {
	
	private MultiPanePagerAdapter mPagerAdapter;
	private ViewPager pager;
	private TabPageIndicator tabIndicator;
	
	public BlackboardPagerFragment() {}
	
	public static BlackboardPagerFragment newInstance()
	{
		return new BlackboardPagerFragment();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
 /*   	setContentView(R.layout.blackboard_layout);

    	actionbar = getSupportActionBar();
		actionbar.setTitle("Blackboard");
		actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionbar.setHomeButtonEnabled(true);
		actionbar.setDisplayHomeAsUpEnabled(true);*/
		
		
		
    	Crittercism.leaveBreadcrumb("Loaded BlackboardActivity");
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View vg = inflater.inflate(R.layout.blackboard_layout, container, false);
		
		pager = (ViewPager) vg.findViewById(R.id.viewpager);
		tabIndicator = (TabPageIndicator) vg.findViewById(R.id.titles);
		initialisePaging();
		return vg;
	}


	private void initialisePaging() {
		
        List<SherlockFragment> fragments = new Vector<SherlockFragment>();
        
        if(getChildFragmentManager().findFragmentByTag(Utility.makeFragmentName(pager.getId(), 0)) != null) {
        	
        	fragments.add((SherlockFragment)getChildFragmentManager().findFragmentByTag(Utility.makeFragmentName(pager.getId(), 0)));
        	fragments.add((SherlockFragment)getChildFragmentManager().findFragmentByTag(Utility.makeFragmentName(pager.getId(), 1)));
        }
        else {
        	fragments.add(BlackboardCourseListFragment.newInstance("Course List"));
            fragments.add(BlackboardDashboardFragment.newInstance("Dashboard"));
        }

        mPagerAdapter = new MultiPanePagerAdapter(getChildFragmentManager(), fragments);
        mPagerAdapter.setPagesDisplayed(1);
        
        pager.setAdapter(mPagerAdapter);
		tabIndicator.setViewPager(pager);

    }
/*	@Override
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
	}*/


}

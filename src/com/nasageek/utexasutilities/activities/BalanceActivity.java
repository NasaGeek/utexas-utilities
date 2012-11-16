package com.nasageek.utexasutilities.activities;


import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.crittercism.app.Crittercism;
import com.nasageek.utexasutilities.PagerAdapter;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.R.id;
import com.nasageek.utexasutilities.R.layout;
import com.nasageek.utexasutilities.fragments.BevoFragment;
import com.nasageek.utexasutilities.fragments.DineinFragment;
import com.viewpagerindicator.TabPageIndicator;



public class BalanceActivity extends SherlockFragmentActivity
{	
	
	ArrayList<String> dtransactionlist, btransactionlist, balancelist;
	String[] dtransactionarray, btransactionarray;
	int count;
	TextView tv1, tv2,tv3,tv4;
	ActionBar actionbar;
	String bevobalance="", dineinbalance="No Dine In Dollars? What kind of animal are you?";
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.balance_layout);
		this.initialisePaging();
		
	
		actionbar = getSupportActionBar();
		actionbar.setTitle("Transactions");
		actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionbar.setHomeButtonEnabled(true);
		actionbar.setDisplayHomeAsUpEnabled(true);
		
	/*	 actionbar.addTab(actionbar.newTab()
		            .setText("Dinein")
		            .setTabListener(new TabListener<DineinFragment>(
		                    this, "dinein", DineinFragment.class, null)));

		    actionbar.addTab(actionbar.newTab()
		            .setText("Bevo Bucks")
		            .setTabListener(new TabListener<BevoFragment>(
		                    this, "bevo", BevoFragment.class, null)));*/
		
		Crittercism.leaveBreadcrumb("BalanceActivity entered");
	}
	
	 /** maintains the pager adapter*/
	
	    private PagerAdapter mPagerAdapter;
	
	    /* (non-Javadoc)
	
	     * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	
	     */
	    /**
	
	     * Initialise the fragments to be paged
	
	     */
	
	    private void initialisePaging() {

	        List<SherlockFragment> fragments = new Vector<SherlockFragment>();
	        Bundle args = new Bundle(1);
	        args.putString("title", "Dine In");
	        fragments.add((SherlockFragment)SherlockFragment.instantiate(this, DineinFragment.class.getName(), args));
	        args = new Bundle(1);
	        args.putString("title", "Bevo Bucks");
	        fragments.add((SherlockFragment)SherlockFragment.instantiate(this, BevoFragment.class.getName(), args));
	
	      
	        this.mPagerAdapter  = new PagerAdapter(getSupportFragmentManager(), fragments);	
	        ViewPager pager = (ViewPager)findViewById(R.id.viewpager);
	        pager.setPageMargin(2);
	        pager.setAdapter(this.mPagerAdapter);
	        TabPageIndicator tabIndicator = (TabPageIndicator)findViewById(R.id.titles);
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

	public static class TabListener<T extends SherlockFragment> implements ActionBar.TabListener 
	{
        private final SherlockFragmentActivity mActivity;
        private final String mTag;
        private final Class<T> mClass;
        private final Bundle mArgs;
        private SherlockFragment mFragment;

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
            mFragment = (SherlockFragment) mActivity.getSupportFragmentManager().findFragmentByTag(mTag);
            if (mFragment != null && !mFragment.isDetached()) {
                FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
                ft.detach(mFragment);
                ft.commit();
            }
        }

        public void onTabSelected(Tab tab, FragmentTransaction fta) {
        	FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
            if (mFragment == null) {
                mFragment = (SherlockFragment) SherlockFragment.instantiate(mActivity, mClass.getName(), mArgs);
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
        	
        }
    
	}
}
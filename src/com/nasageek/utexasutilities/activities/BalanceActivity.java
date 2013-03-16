package com.nasageek.utexasutilities.activities;


import java.util.List;
import java.util.Vector;

import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.crittercism.app.Crittercism;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.Utility;
import com.nasageek.utexasutilities.adapters.MultiPanePagerAdapter;
import com.nasageek.utexasutilities.fragments.TransactionsFragment;
import com.nasageek.utexasutilities.fragments.TransactionsFragment.TransactionType;
import com.viewpagerindicator.MyTabPageIndicator;
import com.viewpagerindicator.TabPageIndicator;

public class BalanceActivity extends SherlockFragmentActivity
{	

	private ActionBar actionbar;
	private MultiPanePagerAdapter mPagerAdapter;	   
	private ViewPager pager;
	private int pagesDisplayed;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.balance_layout);

		pager = (ViewPager) findViewById(R.id.viewpager);
		pagesDisplayed = getResources().getInteger(R.integer.balance_num_visible_pages);

		this.initialisePaging();

		actionbar = getSupportActionBar();
		actionbar.setTitle("Transactions");
		actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionbar.setHomeButtonEnabled(true);
		actionbar.setDisplayHomeAsUpEnabled(true);
		
		Crittercism.leaveBreadcrumb("BalanceActivity entered");
	}
    private void initialisePaging() 
    {
    	
    	List<SherlockFragment> fragments = new Vector<SherlockFragment>();
        /**
         * this is a bit of a hacky solution for something that should be handled by default.
         * on a rotate, pager caches the old fragments (with setRetainInstance(true)), but the 
         * adapter does not, so I have to add the old fragments back to the adapter manually
        */
        if(getSupportFragmentManager().findFragmentByTag(Utility.makeFragmentName(pager.getId(), 0)) != null) {
        	fragments.add((SherlockFragment)getSupportFragmentManager().findFragmentByTag(Utility.makeFragmentName(pager.getId(), 0)));
        	fragments.add((SherlockFragment)getSupportFragmentManager().findFragmentByTag(Utility.makeFragmentName(pager.getId(), 1)));
        }
        else {
        	fragments.add(TransactionsFragment.newInstance("Dine In", TransactionType.Dinein));
        	fragments.add(TransactionsFragment.newInstance("Bevo Bucks", TransactionType.Bevo));
        }
        
        final MyTabPageIndicator tabIndicator = (MyTabPageIndicator)findViewById(R.id.titles);
        
        mPagerAdapter = new MultiPanePagerAdapter(getSupportFragmentManager(), fragments);
        mPagerAdapter.setPagesDisplayed(pagesDisplayed);
        
        pager.setAdapter(this.mPagerAdapter);
        final double dpiScale = getResources().getDisplayMetrics().density;
        if(pagesDisplayed == 1)
        	pager.setPageMargin((int)(8 * dpiScale + .5));
        else
        	tabIndicator.setSelectAll(true);

		tabIndicator.setViewPager(pager);
    }
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
    	int id = item.getItemId();
    	super.onOptionsItemSelected(item);
    	switch(id)
    	{
	    	case android.R.id.home:
	            // app icon in action bar clicked; go home
	           super.onBackPressed();
	           break;
	        //tightly coupling the activity to the fragments for the sake of graphical consistency
	        //was getting weird disappearing menu buttons when I had them in the fragments 
	        //TODO: should at least do this with an interface
	    	case R.id.balance_refresh:
	    		if(pagesDisplayed > 1)
	    		{	
	    			((TransactionsFragment) ((MultiPanePagerAdapter) pager.getAdapter()).getItem(0)).refresh();
	    			((TransactionsFragment) ((MultiPanePagerAdapter) pager.getAdapter()).getItem(1)).refresh();
	    		}
	    		else //if the viewpager is only showing 1 page at a time only refresh the fragment currently in view
	    			((TransactionsFragment) ((MultiPanePagerAdapter) pager.getAdapter()).getItem(pager.getCurrentItem())).refresh();
	    		break;
    	}
    	return false;
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
	    super.onCreateOptionsMenu(menu);
		getSupportMenuInflater().inflate(R.menu.balance_menu, menu);
	    return true;
	}
}
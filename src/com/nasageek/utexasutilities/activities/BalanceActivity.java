package com.nasageek.utexasutilities.activities;


import java.util.List;
import java.util.Vector;

import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.crittercism.app.Crittercism;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.adapters.PagerAdapter;
import com.nasageek.utexasutilities.fragments.TransactionsFragment;
import com.nasageek.utexasutilities.fragments.TransactionsFragment.TransactionType;
import com.viewpagerindicator.TabPageIndicator;

public class BalanceActivity extends SherlockFragmentActivity
{	

	private ActionBar actionbar;
	private PagerAdapter mPagerAdapter;	    
	
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
		
		Crittercism.leaveBreadcrumb("BalanceActivity entered");
	}
    private void initialisePaging() 
    {

        List<SherlockFragment> fragments = new Vector<SherlockFragment>();
        Bundle args = new Bundle(1);
        args.putString("title", "Dine In");
        args.putSerializable("type", TransactionType.Dinein);
        fragments.add((SherlockFragment)SherlockFragment.instantiate(this, TransactionsFragment.class.getName(), args));
        args = new Bundle(1);
        args.putString("title", "Bevo Bucks");
        args.putSerializable("type", TransactionType.Bevo);
        fragments.add((SherlockFragment)SherlockFragment.instantiate(this, TransactionsFragment.class.getName(), args));

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
}
/*package com.nasageek.utexasutilities;

import java.util.List;
import java.util.Vector;

import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.viewpagerindicator.TabPageIndicator;

public class BlackboardAssignmentActivity extends SherlockFragmentActivity 
{
	
	ViewPager pager;
	ActionBar actionbar;
	private  DefaultHttpClient httpclient;

	private PagerAdapter mPagerAdapter;
	TabPageIndicator tabIndicator;
	MenuFragment details,;
	List<SherlockFragment> fragments;
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu_layout);
		
		details = new MenuFragment();
		lunch = new MenuFragment();
		initialisePaging();
		
		actionbar = getSupportActionBar();
		actionbar.setTitle("");
		actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionbar.setHomeButtonEnabled(true);
		// actionbar.setDisplayHomeAsUpEnabled(true);
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)	
    		actionbar.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.actionbar_bg));

       
        httpclient = ConnectionHelper.getThreadSafeClient();
        
  
	}
	private void initialisePaging() {
	
        fragments = new Vector<SherlockFragment>();
        Bundle args = new Bundle();
        args.putString("title", "Breakfast");
        breakfast.setArguments(args);
        fragments.add(breakfast);
        
        Bundle args1 = new Bundle();
        args1.putString("title", "Lunch");
        lunch.setArguments(args1);
        fragments.add(lunch);
        
       
     
        this.mPagerAdapter  = new PagerAdapter(getSupportFragmentManager(), fragments);	
        pager = (ViewPager)findViewById(R.id.viewpager);
        pager.setOffscreenPageLimit(2);
        pager.setPageMargin(4);
        pager.setAdapter(this.mPagerAdapter);
        tabIndicator = (TabPageIndicator)findViewById(R.id.titles);
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
	            Intent home = new Intent(this, UTilitiesActivity.class);
	            home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(home);break;			
    	}
    	return true;
    }
}

	

}
*/
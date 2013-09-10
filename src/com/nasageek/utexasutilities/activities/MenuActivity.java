package com.nasageek.utexasutilities.activities;

import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.nasageek.utexasutilities.ConnectionHelper;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.Utility;
import com.nasageek.utexasutilities.adapters.MultiPanePagerAdapter;
import com.nasageek.utexasutilities.adapters.MyFragmentPagerAdapter;
import com.nasageek.utexasutilities.fragments.MenuFragment;
import com.nasageek.utexasutilities.fragments.TransactionsFragment;
import com.nasageek.utexasutilities.fragments.TransactionsFragment.TransactionType;
import com.viewpagerindicator.MyTabPageIndicator;

public class MenuActivity extends SherlockFragmentActivity {
	
	
	public enum Restaurant {
		No_Overlay("0","No Restaurant"),
		JesterCityLimits("01","Jester City Limits",new String[][]{{"9am - 11pm"},{"7am - 11pm"},{"7am - 11pm"},{"7am - 11pm"},{"7am - 11pm"},{"7am - 11pm"},{"7am - 9pm"},{"9am - 8pm"}},true),
		JesterCityMarket("05","Jester City Market",new String[][]{{"2pm - 11pm"},{"7am - 12am"},{"7am - 12am"},{"7am - 12am"},{"7am - 12am"},{"7am - 12am"},{"7am - 9pm"},{"2pm - 8pm"}},true),
		J2("12","Jester 2nd Floor Dining",new String[][]{{"","",""},{"","11:30am - 2pm","4:30pm - 7:30pm"},{"","11:30am - 2pm","4:30pm - 7:30pm"},{"","11:30am - 2pm","4:30pm - 7:30pm"},{"","11:30am - 2pm","4:30pm - 7:30pm"},{"","11:30am - 2pm","4:30pm - 7:30pm"},{"","11:30am - 2pm","4:30pm - 7:30pm"},{"","",""}},false),
		Kinsolving("03", "Kinsolving Dining Hall",new String[][]{{"","11am - 2pm",""},{"7am - 9:30am","10:30am - 2pm","4:30pm - 7pm"},{"7am - 9:30am","10:30am - 2pm","4:30pm - 7pm"},{"7am - 9:30am","10:30am - 2pm","4:30pm - 7pm"},{"7am - 9:30am","10:30am - 2pm","4:30pm - 7pm"},{"7am - 9:30am","10:30am - 2pm","4:30pm - 7pm"},{"7am - 9:30am","10:30am - 2pm","4:30pm - 7pm"},{"","11am - 2pm","4:30pm - 7pm"}},false),
		KinsMarket("14","Kin's Market",new String[][]{{"4pm - 11pm"},{"7am - 11pm"},{"7am - 11pm"},{"7am - 11pm"},{"7am - 11pm"},{"7am - 11pm"},{"7am - 3pm"},{"3pm - 7pm"}},true),
		CypressBend("08","Cypress Bend",new String[][]{{"12pm - 7pm"},{"7am - 9pm"},{"7am - 9pm"},{"7am - 9pm"},{"7am - 9pm"},{"7am - 9pm"},{"7am - 2pm"},{"12pm - 7pm"}},true),
		Littlefield("19", "Littlefield Patio Cafe",new String[][]{{"2pm - 8pm"},{"7am - 8pm"},{"7am - 8pm"},{"7am - 8pm"},{"7am - 8pm"},{"7am - 8pm"},{"7am - 4pm"},{""}},true),
		JestAPizza("26","Jest A' Pizza",new String[][]{{"5pm - 12am"},{"11am - 12am"},{"11am - 12am"},{"11am - 12am"},{"11am - 12am"},{"11am - 12am"},{"11am - 2pm"},{""}},true);
		
	    private String code;
	    private String fullName; 
	    private String[][] times;
	    private boolean allDay;

	    private Restaurant(String c, String fullName) {
	      code = c;
	      this.fullName = fullName;
	    }
	    private Restaurant(String c, String fullName, String[][] times, boolean allDay) {
	      code = c;
	      this.fullName = fullName;
	      this.times = times;
	      this.allDay = allDay;
	    }
	    public String getCode() {
	      return code;
	    }
	    public String fullName()
	    {
	    	return fullName;
	    }
	    public String toString()
	    {
	    	return fullName;
	    }
	    public String[] getTimes()
	    {
	    	return times[Calendar.getInstance().get(Calendar.DAY_OF_WEEK)];
	    }
	}

	private ViewPager pager;
	private ActionBar actionbar;
//	private DefaultHttpClient httpclient;
	private SharedPreferences settings;
	
	private MultiPanePagerAdapter mPagerAdapter;
//	private MultiPanePagerAdapter landPagerAdapter;
	
//	private MenuFragment breakfast,lunch,dinner;
	
	private int previousItem;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu_layout);
		
		settings = PreferenceManager.getDefaultSharedPreferences(this);

		actionbar = getSupportActionBar();
		actionbar.setTitle("Menus");
		actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionbar.setHomeButtonEnabled(true);
		actionbar.setDisplayHomeAsUpEnabled(true);
			
        final Spinner spinner = new Spinner(this);
        spinner.setPromptId(R.string.restaurantprompt);
		final ArrayAdapter<CharSequence> adapter = new ArrayAdapter(actionbar.getThemedContext(), android.R.layout.simple_spinner_item, Restaurant.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
       
        if(savedInstanceState != null)
        	previousItem = savedInstanceState.getInt("spinner_selection");
        else
        	previousItem = 0;
        initialisePaging(((Restaurant)spinner.getAdapter().getItem(previousItem)).code+"");	
        
        actionbar.setListNavigationCallbacks(adapter, new OnNavigationListener() {
        	public boolean onNavigationItemSelected(int itemPosition, long itemId) {

        		Restaurant r = (Restaurant)spinner.getAdapter().getItem(itemPosition);
        		
        		String restId = r.getCode();
         		
        		if(!"0".equals(restId)) {	         			
        			String[] times = ((Restaurant)spinner.getAdapter().getItem(itemPosition)).getTimes();
        			if(!r.allDay) {
        				((TextView)findViewById(R.id.breakfast_times)).setText(times[0]);
        				((TextView)findViewById(R.id.lunch_times)).setText(times[1]);
        				((TextView)findViewById(R.id.dinner_times)).setText(times[2]);
        			}
        			else {
        				((TextView)findViewById(R.id.breakfast_times)).setText("");
        				((TextView)findViewById(R.id.lunch_times)).setText(times[0]);
        				((TextView)findViewById(R.id.dinner_times)).setText("");
        			}
       
        			if(itemPosition != previousItem) {
        				
	        			((MenuFragment)mPagerAdapter.getItem(0)).updateView(restId, true);
	         			((MenuFragment)mPagerAdapter.getItem(1)).updateView(restId, true);
	         			((MenuFragment)mPagerAdapter.getItem(2)).updateView(restId, true);
         			
         			previousItem = -1;
        			}
         		}
        		
        		return true;
        	}
        });
        if(savedInstanceState == null)
        	actionbar.setSelectedNavigationItem(Integer.parseInt(settings.getString("default_restaurant", "0")));
        else
        	actionbar.setSelectedNavigationItem(savedInstanceState.getInt("spinner_selection"));
       
	}
	private void initialisePaging(String restId) {
	
        List<SherlockFragment> fragments = new Vector<SherlockFragment>();
        pager = (ViewPager)findViewById(R.id.viewpager);
        
        /**
         * this is a bit of a hacky solution for something that should be handled by default.
         * on a rotate, pager caches the old fragments (with setRetainInstance(true)), but the 
         * adapter does not, so I have to add the old fragments back to the adapter manually
        */
        if(getSupportFragmentManager().findFragmentByTag(Utility.makeFragmentName(pager.getId(), 0)) != null) {
        	
        	fragments.add((SherlockFragment)getSupportFragmentManager().findFragmentByTag(Utility.makeFragmentName(pager.getId(), 0)));
        	fragments.add((SherlockFragment)getSupportFragmentManager().findFragmentByTag(Utility.makeFragmentName(pager.getId(), 1)));
        	fragments.add((SherlockFragment)getSupportFragmentManager().findFragmentByTag(Utility.makeFragmentName(pager.getId(), 2)));
        }
        else {
        	fragments.add(MenuFragment.newInstance("Breakfast", restId));
            fragments.add(MenuFragment.newInstance("Lunch", restId));
            fragments.add(MenuFragment.newInstance("Dinner", restId));
        }
        
        final MyTabPageIndicator tabIndicator = (MyTabPageIndicator)findViewById(R.id.titles);
        
        mPagerAdapter = new MultiPanePagerAdapter(getSupportFragmentManager(), fragments);
        mPagerAdapter.setPagesDisplayed(getResources().getInteger(R.integer.menu_num_visible_pages));
        
        pager.setAdapter(mPagerAdapter);
        if(mPagerAdapter.getPageWidth(0) < 1)
        	tabIndicator.setSelectAll(true);

		tabIndicator.setViewPager(pager);
		
		pager.setOffscreenPageLimit(2);
    }
	@Override
	public void onSaveInstanceState(Bundle out) {
		super.onSaveInstanceState(out);
		out.putInt("spinner_selection", actionbar.getSelectedNavigationIndex());
	}
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	int id = item.getItemId();
    	switch(id)
    	{
	    	case android.R.id.home:
	            // app icon in action bar clicked; go home
	            super.onBackPressed();
	            break;			
    	}
    	return true;
    }
}

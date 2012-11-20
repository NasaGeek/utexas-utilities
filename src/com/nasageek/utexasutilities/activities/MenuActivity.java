package com.nasageek.utexasutilities.activities;

import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.nasageek.utexasutilities.R.id;
import com.nasageek.utexasutilities.R.layout;
import com.nasageek.utexasutilities.R.string;
import com.nasageek.utexasutilities.adapters.PagerAdapter;
import com.nasageek.utexasutilities.fragments.MenuFragment;
import com.viewpagerindicator.TabPageIndicator;

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

	ViewPager pager;
	ActionBar actionbar;
	private  DefaultHttpClient httpclient;
	private SharedPreferences settings;
	
	private PagerAdapter mPagerAdapter;
	TabPageIndicator tabIndicator;
	MenuFragment breakfast,lunch,dinner;
	List<SherlockFragment> fragments;
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu_layout);
		
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		breakfast = new MenuFragment();
		lunch = new MenuFragment();
		dinner=  new MenuFragment();
		initialisePaging("0");
		
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
       
        
        httpclient = ConnectionHelper.getThreadSafeClient();
        
        actionbar.setListNavigationCallbacks(adapter, new OnNavigationListener() 
        {
        	public boolean onNavigationItemSelected(int itemPosition, long itemId) {

        		Restaurant r = (Restaurant)spinner.getAdapter().getItem(itemPosition);
        		
        		String restId = r.getCode();
         		if(!"0".equals(restId))
         		{	
         			String[] times = ((Restaurant)spinner.getAdapter().getItem(itemPosition)).getTimes();
         			
         			if(!r.allDay)
         			{
	         			((TextView)findViewById(R.id.breakfast_times)).setText(times[0]);
	         			((TextView)findViewById(R.id.lunch_times)).setText(times[1]);
	         			((TextView)findViewById(R.id.dinner_times)).setText(times[2]);
         			}
         			else
         			{
         				((TextView)findViewById(R.id.breakfast_times)).setText("");
	         			((TextView)findViewById(R.id.lunch_times)).setText(times[0]);
	         			((TextView)findViewById(R.id.dinner_times)).setText("");
         			}
         			
         			breakfast.updateView(restId,breakfast.getView());
         			lunch.updateView(restId,lunch.getView());
         			dinner.updateView(restId,dinner.getView());
         		}	
        		return false;
        	}
        });
        actionbar.setSelectedNavigationItem(Integer.parseInt(settings.getString("default_restaurant", "0")));
       
	}
	private void initialisePaging(String restId) {
	
        fragments = new Vector<SherlockFragment>();
        Bundle args = new Bundle();
        args.putString("title", "Breakfast");
        args.putString("restId", restId);
        breakfast.setArguments(args);
        fragments.add(breakfast);
        
        Bundle args1 = new Bundle();
        args1.putString("title", "Lunch");
        args1.putString("restId", restId);
        lunch.setArguments(args1);
        fragments.add(lunch);
        
        Bundle args2 = new Bundle();
        args2.putString("title", "Dinner");
        args2.putString("restId", restId);
        dinner.setArguments(args2);
        fragments.add(dinner);
     
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
	            super.onBackPressed();
	            break;			
    	}
    	return true;
    }
}

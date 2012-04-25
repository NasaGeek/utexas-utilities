package com.nasageek.UTilities;

import java.util.ArrayList;

import com.nasageek.UTilities.PagerAdapter;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.foound.widget.AmazingAdapter;
import com.foound.widget.AmazingListView;
import com.nasageek.UTilities.CampusMapActivity.Route;
import com.viewpagerindicator.TabPageIndicator;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

public class MenuActivity extends SherlockFragmentActivity {
	
	
	public enum Restaurant {
		No_Overlay("0","No Restaurant"),
		JesterCityLimits("01","Jester City Limits"),
		JesterCityMarket("05","Jester City Market"),
		J2("12","Jester 2nd Floor Dining"),
		Kinsolving("03", "Kinsolving Dining Hall"),
		KinsMarket("14","Kin's Market"),
		CypressBend("08","Cypress Bend"),
		Littlefield("19", "Littlefield Patio Cafe"),
		FortyAcresBakery("21","40 Acres Bakery"),
		JestAPizza("26","Jest A' Pizza");
		
	    private String code;
	    private String fullName; 

	    private Restaurant(String c, String fullName) {
	      code = c;
	      this.fullName = fullName;
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
	}

	ViewPager pager;
	ActionBar actionbar;
	private  DefaultHttpClient httpclient;

	private PagerAdapter mPagerAdapter;
	TabPageIndicator tabIndicator;
	MenuFragment breakfast,lunch,dinner;
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu_layout);
		
		breakfast = new MenuFragment();
		lunch = new MenuFragment();
		dinner=  new MenuFragment();
		initialisePaging("0");
		
		actionbar = getSupportActionBar();
		actionbar.setTitle("Menus");
		actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionbar.setHomeButtonEnabled(true);
		actionbar.setDisplayHomeAsUpEnabled(true);
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)	
    		actionbar.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.actionbar_bg));
		
        final Spinner spinner = new Spinner(this);
        spinner.setPromptId(R.string.restaurantprompt);
		final ArrayAdapter<CharSequence> adapter = new ArrayAdapter(actionbar.getThemedContext(), android.R.layout.simple_spinner_item, Restaurant.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
       
        httpclient = ConnectionHelper.getThreadSafeClient();
        
       
        
        actionbar.setListNavigationCallbacks(adapter, new OnNavigationListener() 
        {
        	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        		// TODO Auto-generated method stub
        		String restId = ((Restaurant) spinner.getAdapter().getItem(itemPosition)).getCode();
         		if(!"0".equals(restId))
         		{	
         			breakfast.updateView(restId);
         			lunch.updateView(restId);
         			dinner.updateView(restId);
         		}
       
        		
        		return false;
        	}
        });
       
	}
	private void initialisePaging(String restId) {
	
        List<SherlockFragment> fragments = new Vector<SherlockFragment>();
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
	            Intent home = new Intent(this, UTilitiesActivity.class);
	            home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(home);break;			
    	}
    	return true;
    }
}

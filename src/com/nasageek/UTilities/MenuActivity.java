package com.nasageek.UTilities;

import java.util.ArrayList;
import java.util.List;
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
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.view.MenuItem;
import com.foound.widget.AmazingAdapter;
import com.foound.widget.AmazingListView;
import com.nasageek.UTilities.CampusMapActivity.Route;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
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

public class MenuActivity extends SherlockActivity {
	
	
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

	ActionBar actionbar;
	private  DefaultHttpClient httpclient;
	boolean []mealsOffered;
	ArrayList<String> mealList;
	ArrayList<food> foodList;
	AmazingListView blv,llv,dlv;
	LinearLayout b_pb_ll,l_pb_ll,d_pb_ll;
	fetchMenuTask fetchBreakfastTask,fetchLunchTask,fetchDinnerTask,fetchLunchDinnerTask;
	
	
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu_layout);
		
		
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
        
        b_pb_ll = (LinearLayout) findViewById(R.id.breakfast_progressbar_ll);
        l_pb_ll = (LinearLayout) findViewById(R.id.lunch_progressbar_ll);
        d_pb_ll = (LinearLayout) findViewById(R.id.dinner_progressbar_ll);
        
        blv = (AmazingListView) findViewById(R.id.breakfast_listview);
		llv = (AmazingListView) findViewById(R.id.lunch_listview);
		dlv = (AmazingListView) findViewById(R.id.dinner_listview);
        
		
		
		/**
		 * FOR ICS
		 */
		if(Build.VERSION.SDK_INT >= 14)
		{
			TabHost tabHost = (TabHost)findViewById(R.id.menutabhost); // The activity TabHost
	        tabHost.setup();
	        TabHost.TabSpec spec=tabHost.newTabSpec("tag1");
	        
	        spec.setIndicator("Breakfast");
	        spec.setContent(R.id.breakfastlinlay);
	        
	        tabHost.addTab(spec);
	        spec = tabHost.newTabSpec("tag2");
	        
	        spec.setIndicator("Lunch");
	        spec.setContent(R.id.lunchlinlay);
	        tabHost.addTab(spec);
	        spec = tabHost.newTabSpec("tag3");
	        spec.setIndicator("Dinner");
	        spec.setContent(R.id.dinnerlinlay);
	        tabHost.addTab(spec);
	        tabHost.setCurrentTab(0);
		}
		/**
		 * FOR PRE-ICS
		 */
		else
		{
			Resources res = getResources();
            
	        TabHost tabHost = (TabHost)findViewById(R.id.menutabhost); // The activity TabHost
	        tabHost.setup();
	        tabHost.setBackgroundResource(com.actionbarsherlock.R.drawable.abs__tab_indicator_holo);
	        tabHost.getTabWidget().setBackgroundResource(com.actionbarsherlock.R.drawable.abs__tab_indicator_holo);
	        TabHost.TabSpec spec=tabHost.newTabSpec("tag1");
	        TextView tv = new TextView(this);
	        tv.setBackgroundColor(Color.BLACK);
	        tv.setBackgroundResource(com.actionbarsherlock.R.drawable.abs__tab_indicator_holo);
	        tv.setText("Breakfast");
	        tv.setPadding(0, 0, 0, 20);
	        tv.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
	        
	        TextView tv1 = new TextView(this);
	        tv1.setBackgroundResource(com.actionbarsherlock.R.drawable.abs__tab_indicator_holo);
	        tv1.setText("Lunch");
	        tv1.setPadding(0, 0, 0, 20);
	        tv1.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);

	        TextView tv2 = new TextView(this);
	        tv2.setBackgroundResource(com.actionbarsherlock.R.drawable.abs__tab_indicator_holo);
	        tv2.setText("Dinner");
	        tv2.setPadding(0, 0, 0, 20);
	        tv2.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
	        spec.setIndicator(tv);
	        spec.setContent(R.id.breakfastlinlay);
	        
	        tabHost.addTab(spec);
	        spec = tabHost.newTabSpec("tag2");
	        
	        spec.setIndicator(tv1);
	        spec.setContent(R.id.lunchlinlay);
	        tabHost.addTab(spec);
	        spec = tabHost.newTabSpec("tag3");
	        spec.setIndicator(tv2);
	        spec.setContent(R.id.dinnerlinlay);
	        tabHost.addTab(spec);
	        tabHost.getTabWidget().getChildAt(0).getLayoutParams().height = 
	        		65; 
	        tabHost.getTabWidget().getChildAt(1).getLayoutParams().height = 
	        		65; 
	        tabHost.getTabWidget().getChildAt(2).getLayoutParams().height = 
	        		65; 
	        
	        tabHost.setCurrentTab(0);
		}
		
		
		
		
        
        httpclient = ConnectionHelper.getThreadSafeClient();
        
        fetchBreakfastTask = new fetchMenuTask(httpclient);
        fetchLunchTask  = new fetchMenuTask(httpclient);
        fetchDinnerTask  = new fetchMenuTask(httpclient);
        fetchLunchDinnerTask  = new fetchMenuTask(httpclient);
        
        actionbar.setListNavigationCallbacks(adapter, new OnNavigationListener() 
        {
        	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        		// TODO Auto-generated method stub
        		if(!"0".equals(((Restaurant) spinner.getAdapter().getItem(itemPosition)).getCode()))
            		loadRestaurant(((Restaurant) spinner.getAdapter().getItem(itemPosition)).getCode());
        		else
        		{
        			 b_pb_ll.setVisibility(View.GONE);
        		     l_pb_ll.setVisibility(View.GONE);
        		     d_pb_ll.setVisibility(View.GONE);
        		}
        		return false;
        	}
        });
       
	}
	private void loadRestaurant(String restId)
	{
		
		
		 b_pb_ll.setVisibility(View.VISIBLE);
	     l_pb_ll.setVisibility(View.VISIBLE);
	     d_pb_ll.setVisibility(View.VISIBLE);
	     blv.setVisibility(View.GONE);
	     llv.setVisibility(View.GONE);
	     dlv.setVisibility(View.GONE);
	     
	     fetchBreakfastTask.cancel(true);
	     fetchLunchTask.cancel(true);
	     fetchDinnerTask.cancel(true);
	     fetchLunchDinnerTask.cancel(true);
	     
	     fetchBreakfastTask = new fetchMenuTask(httpclient);
	     fetchLunchTask  = new fetchMenuTask(httpclient);
	     fetchDinnerTask  = new fetchMenuTask(httpclient);
	     fetchLunchDinnerTask  = new fetchMenuTask(httpclient);
	     
		fetchBreakfastTask.execute(restId,"Breakfast",blv);
		if(restId.equals("05"))
		{
			fetchLunchDinnerTask.execute(restId,"Lunch/Dinner",dlv);
		}
		else
		{
			fetchLunchTask.execute(restId,"Lunch",llv);
			fetchDinnerTask.execute(restId,"Dinner",dlv);
		}
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
	
	
	
	private class fetchMenuTask extends AsyncTask<Object,Integer,String>
	{
		private AmazingListView lv;
		private ArrayList<Pair<String,ArrayList<food>>> listOfLists;
		private DefaultHttpClient client;
		private String meal;
		
		public fetchMenuTask(DefaultHttpClient client)
		{
			this.client = client;
		}
		
		@Override
		protected String doInBackground(Object... params)
		{
			listOfLists = new ArrayList<Pair<String,ArrayList<food>>>(); 
			ArrayList<String> categories = new ArrayList<String>();
			ArrayList<food> foodList = new ArrayList<food>();
			meal = (String)params[1];
			String location = "http://129.116.62.55/foodpro/pickMenu.asp?locationNum="+params[0]+"&mealName="+meal;
			
			lv = (AmazingListView)params[2];
			
			HttpGet hget = new HttpGet(location);
	    	String pagedata="";
	    	
	    	try
			{
				HttpResponse response = client.execute(hget);
		    	pagedata = EntityUtils.toString(response.getEntity());
			} catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	if(pagedata.contains("No Data Available"))
	    	{
	    		foodList.add(new food("",""));
	    		listOfLists.add(new Pair<String,ArrayList<food>>("No Food Offered at this Time",foodList));
	    		return meal;
	    	}
	    	else
	    	{
		
		    	Pattern catPattern = Pattern.compile("<div class=\'pickmenucolmenucat\'.*?(?=<div class='pickmenucolmenucat'|</html>)", Pattern.DOTALL);
		    	Matcher catMatcher = catPattern.matcher(pagedata);
		    	while(catMatcher.find())
		    	{
		    		foodList = new ArrayList<food>();
		    		
		    		Pattern catNamePattern = Pattern.compile("(?<=>-- ).*?(?= --<)");
		    		Matcher catNameMatcher = catNamePattern.matcher(catMatcher.group());
		    		catNameMatcher.find();
		    		
		    		categories.add(catNameMatcher.group());
		    		
		    		Pattern nutritionLinkPattern = Pattern.compile("(?<=a href=\').*?(?=')");
		    		Matcher nutritionLinkMatcher = nutritionLinkPattern.matcher(catMatcher.group());
		    		
		    		Pattern foodPattern = Pattern.compile("(?<=\">).*?(?=</a)");
		    		Matcher foodMatcher = foodPattern.matcher(catMatcher.group());
		    		
		    		while(foodMatcher.find() && nutritionLinkMatcher.find())
		    		{
		    			String a = foodMatcher.group();
		    			foodList.add(new food(foodMatcher.group(),nutritionLinkMatcher.group()));
		    		}	
		    		listOfLists.add(new Pair<String,ArrayList<food>>(catNameMatcher.group(),foodList));	
		    		if(isCancelled())
		    			return "";
		    	}
	    	}
	    	return meal;
		}
		@Override
		protected void onPostExecute(String result)
		{
			lv.setAdapter(new MenuAdapter(listOfLists));	
			lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub				    	
				String url ="http://129.116.62.55/foodpro/"+((food)(arg0.getItemAtPosition(arg2))).nutritionLink;  
				Intent i = new Intent(Intent.ACTION_VIEW);  
				i.setData(Uri.parse(url));  
				startActivity(i);
		    	
			}
		});
		lv.setPinnedHeaderView(LayoutInflater.from(MenuActivity.this).inflate(R.layout.menu_header_item_view, lv, false));
		
		lv.setVisibility(View.VISIBLE);	
			
			
			
			
			
			
			if(result.equals("Breakfast"))
	    		{
	    	/*		blv.setAdapter(new MenuAdapter(listOfLists));	
	    			blv.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						// TODO Auto-generated method stub				    	
						String url ="http://129.116.62.55/foodpro/"+((food)(arg0.getItemAtPosition(arg2))).nutritionLink;  
						Intent i = new Intent(Intent.ACTION_VIEW);  
						i.setData(Uri.parse(url));  
						startActivity(i);
				    	
					}
				});
	    		blv.setPinnedHeaderView(LayoutInflater.from(MenuActivity.this).inflate(R.layout.menu_header_item_view, blv, false));*/
	    		b_pb_ll.setVisibility(View.GONE);
	  //  		blv.setVisibility(View.VISIBLE);
	    		}
	    		if(result.equals("Lunch"))
	    		{
	    	/*		llv.setAdapter(new MenuAdapter(listOfLists));	
	    			llv.setOnItemClickListener(new OnItemClickListener() {
					
					
					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						// TODO Auto-generated method stub				    	
						String url ="http://129.116.62.55/foodpro/"+((food)(arg0.getItemAtPosition(arg2))).nutritionLink;  
						Intent i = new Intent(Intent.ACTION_VIEW);  
						i.setData(Uri.parse(url));  
						startActivity(i);
				    	
					}
				});
	    		llv.setPinnedHeaderView(LayoutInflater.from(MenuActivity.this).inflate(R.layout.menu_header_item_view, llv, false));*/
	    		l_pb_ll.setVisibility(View.GONE);
	    //		llv.setVisibility(View.VISIBLE);
	    		}	
	    		if(result.equals("Dinner"))
	    		{
	    /*			dlv.setAdapter(new MenuAdapter(listOfLists));	
	    			dlv.setOnItemClickListener(new OnItemClickListener() {
					
					
					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						// TODO Auto-generated method stub				    	
						String url ="http://129.116.62.55/foodpro/"+((food)(arg0.getItemAtPosition(arg2))).nutritionLink;  
						Intent i = new Intent(Intent.ACTION_VIEW);  
						i.setData(Uri.parse(url));  
						startActivity(i);
				    	
					}
				});
	    		dlv.setPinnedHeaderView(LayoutInflater.from(MenuActivity.this).inflate(R.layout.menu_header_item_view,dlv, false));*/
	    		d_pb_ll.setVisibility(View.GONE);
	    	//	dlv.setVisibility(View.VISIBLE);
	    		}
	    		
	    		if(result.equals("Lunch/Dinner"))
	    		{
	    			
		  /*  			llv.setAdapter(new MenuAdapter(listOfLists));	
		    			llv.setOnItemClickListener(new OnItemClickListener() {
						
						
						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1,
								int arg2, long arg3) {
							// TODO Auto-generated method stub				    	
							String url ="http://129.116.62.55/foodpro/"+((food)(arg0.getItemAtPosition(arg2))).nutritionLink;  
							Intent i = new Intent(Intent.ACTION_VIEW);  
							i.setData(Uri.parse(url));  
							startActivity(i);
					    	
						}
					});
		    		llv.setPinnedHeaderView(LayoutInflater.from(MenuActivity.this).inflate(R.layout.menu_header_item_view, llv, false));*/
		    		l_pb_ll.setVisibility(View.GONE);
		    	/*	llv.setVisibility(View.VISIBLE);
		    		
		    			dlv.setAdapter(new MenuAdapter(listOfLists));	
		    			dlv.setOnItemClickListener(new OnItemClickListener() {
						
						
						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1,
								int arg2, long arg3) {
							// TODO Auto-generated method stub				    	
							String url ="http://129.116.62.55/foodpro/"+((food)(arg0.getItemAtPosition(arg2))).nutritionLink;  
							Intent i = new Intent(Intent.ACTION_VIEW);  
							i.setData(Uri.parse(url));  
							startActivity(i);
					    	
						}
					});
		    		dlv.setPinnedHeaderView(LayoutInflater.from(MenuActivity.this).inflate(R.layout.menu_header_item_view, dlv, false));*/
		    		d_pb_ll.setVisibility(View.GONE);
		    	//	dlv.setVisibility(View.VISIBLE);
		    		
		    		
		    		
	    		}
		}	
	
	}
	
	
	class food
	{
		String name;
		String nutritionLink;
		
		public food(String name, String nutritionLink)
		{
			this.name = name;
			this.nutritionLink = nutritionLink;
		}
		public String getName()
		{
			return name;
		}
		public String getLink()
		{
			return nutritionLink;
		}
	}
	
	
	class MenuAdapter extends AmazingAdapter {
		ArrayList<Pair<String, ArrayList<food>>> all;

		
		public MenuAdapter(ArrayList<Pair<String, ArrayList<food>>> all)
		{
			this.all = all;
		}
		
		@Override
		public int getCount() {
			int res = 0;
			for (int i = 0; i < all.size(); i++) {
				res += all.get(i).second.size();
			}
			return res;
		}

		@Override
		public food getItem(int position) {
			int c = 0;
			for (int i = 0; i < all.size(); i++) {
				if (position >= c && position < c + all.get(i).second.size()) {
					return all.get(i).second.get(position - c);
				}
				c += all.get(i).second.size();
			}
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		protected void onNextPageRequested(int page) {
		}

		@Override
		protected void bindSectionHeader(View view, int position, boolean displaySectionHeader) {
			if (displaySectionHeader) {
				view.findViewById(R.id.header).setVisibility(View.VISIBLE);
				TextView lSectionTitle = (TextView) view.findViewById(R.id.header);
				lSectionTitle.setText(getSections()[getSectionForPosition(position)]);
			} else {
				view.findViewById(R.id.header).setVisibility(View.GONE);
			}
		}

		@Override
		public View getAmazingView(int position, View convertView, ViewGroup parent) {
			View res = convertView;
			if (res == null) res = getLayoutInflater().inflate(R.layout.menu_item_view, null);
			
			TextView lName = (TextView) res.findViewById(R.id.lName);
			
			
			food f = getItem(position);
			lName.setText(f.name);
			return res;
		}

		@Override
		public void configurePinnedHeader(View header, int position, int alpha) {
			TextView lSectionHeader = (TextView)header;
			lSectionHeader.setText(getSections()[getSectionForPosition(position)]);
		//	lSectionHeader.getBackground().setAlpha(alpha);
		//	lSectionHeader.setBackgroundColor(alpha << 24 | (0xEAEAEA));
		//	lSectionHeader.setTextColor(alpha << 24 | (0x343434));
		}

		@Override
		public int getPositionForSection(int section) {
			if (section < 0) section = 0;
			if (section >= all.size()) section = all.size() - 1;
			int c = 0;
			for (int i = 0; i < all.size(); i++) {
				if (section == i) { 
					return c;
				}
				c += all.get(i).second.size();
			}
			return 0;
		}

		@Override
		public int getSectionForPosition(int position) {
			int c = 0;
			for (int i = 0; i < all.size(); i++) {
				if (position >= c && position < c + all.get(i).second.size()) {
					return i;
				}
				c += all.get(i).second.size();
			}
			return 0;
		}

		@Override
		public String[] getSections() {
			String[] res = new String[all.size()];
			for (int i = 0; i < all.size(); i++) {
				res[i] = all.get(i).first;
			}
			return res;
		}
		
	}
}

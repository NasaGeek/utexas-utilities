package com.nasageek.utexasutilities.fragments;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.foound.widget.AmazingAdapter;
import com.foound.widget.AmazingListView;
import com.nasageek.utexasutilities.ConnectionHelper;
import com.nasageek.utexasutilities.R;

public class MenuFragment extends SherlockFragment
{
	private DefaultHttpClient httpclient;
//	private ArrayList<String> mealList;
	private ArrayList<Pair<String,ArrayList<food>>> listOfLists;
//	private ArrayList<food> foodList;
	private AmazingListView mlv;
	private LinearLayout m_pb_ll;
	private TextView metv;
	private fetchMenuTask fetchMTask;
	private String restId;
	private MenuAdapter mAdapter;
	

	public MenuFragment() {}
	
	public static MenuFragment newInstance(String title, String restId)
	{
		MenuFragment f = new MenuFragment();
		Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("restId", restId);
        f.setArguments(args);
        
        return f;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{	
		if(restId.equals("0"))
		{	View vg =  inflater.inflate(R.layout.menu_fragment_layout, container, false);
			return vg;
		}

		View vg =  inflater.inflate(R.layout.menu_fragment_layout, container, false);
			
		updateView(restId, vg, false);

		return vg;
	}
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);	
		restId = getArguments().getString("restId");
        httpclient = ConnectionHelper.getThreadSafeClient();
        listOfLists = new ArrayList<Pair<String,ArrayList<food>>>();
        mAdapter = new MenuAdapter(listOfLists);
        setRetainInstance(true);
	}
	@TargetApi(11)
	public void updateView(String restId, View vg, Boolean update)
	{
		this.restId = restId;

		//TODO: vg is null every once in a while, can probably fix by doing fragments the right way
		m_pb_ll = (LinearLayout) vg.findViewById(R.id.menu_progressbar_ll);
        mlv = (AmazingListView) vg.findViewById(R.id.menu_listview);
        metv = (TextView) vg.findViewById(R.id.menu_error);
  
        mlv.setAdapter(mAdapter);
        mlv.setPinnedHeaderView(getSherlockActivity().getLayoutInflater().inflate(R.layout.menu_header_item_view, mlv, false));
        
        if(listOfLists.size() == 0 || update)
        {	
        	listOfLists.clear();
        	fetchMTask = new fetchMenuTask(httpclient);
		
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
				fetchMTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, restId,this.getArguments().getString("title"),mlv);
			else
				fetchMTask.execute(restId,this.getArguments().getString("title"),mlv);
        }
		
	}
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if(fetchMTask!=null)
			fetchMTask.cancel(true);
	}
	private class fetchMenuTask extends AsyncTask<Object,Integer,String>
	{
		private DefaultHttpClient client;
		private String meal;
		private String errorMsg;
		
		public fetchMenuTask(DefaultHttpClient client)
		{
			this.client = client;
		}
		
		@Override
		protected void onPreExecute()
		{
			m_pb_ll.setVisibility(View.VISIBLE);
			mlv.setVisibility(View.GONE);
		}
		@Override
		protected String doInBackground(Object... params)
		{ 
			ArrayList<String> categories = new ArrayList<String>();
			ArrayList<food> foodList = new ArrayList<food>();
			meal = (String)params[1];
			String location = "";
			
			//Special case for JCM, which combines Lunch and Dinner
			if(restId.equals("05") && (meal.equals("Lunch")||meal.equals("Dinner")))
				location = "http://129.116.62.55/foodpro/pickMenu.asp?locationNum="+params[0]+"&mealName=Lunch/Dinner";
			else
				location = "http://129.116.62.55/foodpro/pickMenu.asp?locationNum="+params[0]+"&mealName="+meal;
			

			HttpGet hget = new HttpGet(location);
	    	String pagedata="";
	    	
	    	try
			{
				HttpResponse response = client.execute(hget);
		    	pagedata = EntityUtils.toString(response.getEntity());
			} catch (Exception e)
			{
				errorMsg = "UTilities could not fetch this menu";
				cancel(true);
				e.printStackTrace();
				return null;
			}
	    	if(pagedata.contains("No Data Available"))
	    	{
	    		foodList.add(new food("",""));
	    		listOfLists.add(new Pair<String,ArrayList<food>>("No Food Offered at this Time",foodList));
	    		return meal;
	    	}
	    	else
	    	{
	    		//have to leave in the lookahead so the regex matches don't overlap
		    	Pattern catPattern = Pattern.compile("<div class=\'pickmenucolmenucat\'.*?(?=<div class='pickmenucolmenucat'|</html>)", Pattern.DOTALL);
		    	Matcher catMatcher = catPattern.matcher(pagedata);
		    	while(catMatcher.find())
		    	{
		    		String categoryData = catMatcher.group();
		    		foodList = new ArrayList<food>();
		    		
		    		Pattern catNamePattern = Pattern.compile(">-- (.*?) --<");
		    		Matcher catNameMatcher = catNamePattern.matcher(categoryData);
		    		if(catNameMatcher.find())
		    			categories.add(catNameMatcher.group(1));
		    		else
		    			categories.add("Unknown Category");
		    		
		    		Pattern nutritionLinkPattern = Pattern.compile("a href=\'(.*?)\'");
		    		Matcher nutritionLinkMatcher = nutritionLinkPattern.matcher(categoryData);
		    		
		    		//This pattern is glitchy on a Nexus S 4G running CM10.1 nightly
		    		//Seems to activate Pattern.DOTALL by default. Set flags to 0 to try and mitigate?
		    		Pattern foodPattern = Pattern.compile("<a href=.*?\">(\\w.*?)</a>",0);
		    		Matcher foodMatcher = foodPattern.matcher(categoryData);
		    		
		    		while(foodMatcher.find() && nutritionLinkMatcher.find())
		    		{
		    			foodList.add(new food(foodMatcher.group(1),nutritionLinkMatcher.group(1)));
		    		}	
		    		listOfLists.add(new Pair<String,ArrayList<food>>(catNameMatcher.group(1),foodList));	
		    		if(isCancelled())
		    			return "";
		    	}
	    	}
	    	return meal;
		}
		@Override
		protected void onPostExecute(String result)
		{
			mAdapter.notifyDataSetChanged();
			mlv.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		    	
				String url ="http://129.116.62.55/foodpro/"+((food)(arg0.getItemAtPosition(arg2))).nutritionLink;  
				Intent i = new Intent(Intent.ACTION_VIEW);  
				i.setData(Uri.parse(url));  
				startActivity(i);
				}
			});
			
			if(getSherlockActivity() != null) //was getting a NPE here probably from leaving the activity while the menu was loading
				mlv.setPinnedHeaderView(getSherlockActivity().getLayoutInflater().inflate(R.layout.menu_header_item_view, mlv, false));
			
			mlv.setVisibility(View.VISIBLE);	
			m_pb_ll.setVisibility(View.GONE);
			metv.setVisibility(View.GONE);
		}
		@Override
		protected void onCancelled()
		{
			metv.setText(errorMsg);
			metv.setVisibility(View.VISIBLE);
			mlv.setVisibility(View.GONE);	
			m_pb_ll.setVisibility(View.GONE);
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
		private ArrayList<Pair<String, ArrayList<food>>> all;

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
		protected void onNextPageRequested(int page) {}

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
			if (res == null) res = getActivity().getLayoutInflater().inflate(R.layout.menu_item_view, null);
			
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

		@Override
		protected View getLoadingView(ViewGroup parent) {
			return null;
		}
		
	}
}
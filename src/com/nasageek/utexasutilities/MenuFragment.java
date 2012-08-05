package com.nasageek.utexasutilities;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
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

public class MenuFragment extends SherlockFragment
{
	private DefaultHttpClient httpclient;
	private ArrayList<String> mealList;
	private ArrayList<food> foodList;
	private AmazingListView mlv;
	private LinearLayout m_pb_ll;
	private TextView metv;
	private fetchMenuTask fetchMTask;
	private String restId;
	private String times;
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{	
		if(restId.equals("0"))
		{	View vg =  inflater.inflate(R.layout.menu_fragment_layout, container, false);
			((LinearLayout) vg.findViewById(R.id.menu_progressbar_ll)).setVisibility(View.GONE);
			return vg;
		}

		View vg =  inflater.inflate(R.layout.menu_fragment_layout, container, false);
			
		updateView(restId, vg);

		return vg;
	}
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);	
		restId = getArguments().getString("restId");
        httpclient = ConnectionHelper.getThreadSafeClient();      
	}
	public void updateView(String restId, View vg)
	{
		this.restId = restId;

		m_pb_ll = (LinearLayout) vg.findViewById(R.id.menu_progressbar_ll);
        mlv = (AmazingListView) vg.findViewById(R.id.menu_listview);
        metv = (TextView) vg.findViewById(R.id.menu_error);
  
		m_pb_ll.setVisibility(View.VISIBLE);
		mlv.setVisibility(View.GONE);
		fetchMTask = new fetchMenuTask(httpclient);
		fetchMTask.execute(restId,this.getArguments().getString("title"),mlv);
		
		
	}
	public String getTimes()
	{
		return times;
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
		private AmazingListView lv;
		private ArrayList<Pair<String,ArrayList<food>>> listOfLists;
		private DefaultHttpClient client;
		private String meal;
		private String errorMsg;
		
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
			String location = "";
			if(restId.equals("05") && (meal.equals("Lunch")||meal.equals("Dinner")))
				location = "http://129.116.62.55/foodpro/pickMenu.asp?locationNum="+params[0]+"&mealName=Lunch/Dinner";
			else
				location = "http://129.116.62.55/foodpro/pickMenu.asp?locationNum="+params[0]+"&mealName="+meal;
			
			lv = (AmazingListView)params[2];
			
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
		    	
				String url ="http://129.116.62.55/foodpro/"+((food)(arg0.getItemAtPosition(arg2))).nutritionLink;  
				Intent i = new Intent(Intent.ACTION_VIEW);  
				i.setData(Uri.parse(url));  
				startActivity(i);
		    	
				}
			});
			lv.setPinnedHeaderView(getLayoutInflater(null).inflate(R.layout.menu_header_item_view, lv, false));
			
			lv.setVisibility(View.VISIBLE);	
			m_pb_ll.setVisibility(View.GONE);
			metv.setVisibility(View.GONE);
		}
		@Override
		protected void onCancelled()
		{
			metv.setText(errorMsg);
			metv.setVisibility(View.VISIBLE);
			lv.setVisibility(View.GONE);	
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
			if (res == null) res = getLayoutInflater(null).inflate(R.layout.menu_item_view, null);
			
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
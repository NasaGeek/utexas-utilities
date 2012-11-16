package com.nasageek.utexasutilities.fragments;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.foound.widget.AmazingListView;
import com.nasageek.utexasutilities.ConnectionHelper;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.R.id;
import com.nasageek.utexasutilities.R.layout;
import com.nasageek.utexasutilities.R.menu;
import com.nasageek.utexasutilities.adapters.TransactionAdapter;


public class DineinFragment extends SherlockFragment
{
	private  DefaultHttpClient httpclient;
	private LinearLayout d_pb_ll;
	private LinearLayout dineinlinlay;
	private AmazingListView dlv;
	ArrayList<String> dtransactionlist;
	String[] dtransactionarray;
	int count;
	private TransactionAdapter ta;
	TextView tv1, tv2;
	private TextView etv;
	
	private List<BasicNameValuePair> postdata;
	private SherlockFragmentActivity parentAct;

	ViewGroup cont;
	View vg;
	
	String  dineinbalance="No Dine In Dollars? What kind of animal are you?";
	private fetchTransactionDataTask fetch;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		if(vg==null){
		vg =  inflater.inflate(R.layout.dinein_fragment_layout, container, false);
		
		dlv = (AmazingListView) vg.findViewById(R.id.dtransactions_listview);
		dineinlinlay = (LinearLayout) vg.findViewById(R.id.dineinlinlay);
		d_pb_ll = (LinearLayout) vg.findViewById(R.id.dinein_progressbar_ll);
		etv = (TextView) vg.findViewById(R.id.dinein_error);
		
		dlv.setLoadingView(inflater.inflate(R.layout.loading_content_layout, null));
		dlv.setAdapter(ta);
		
	    dineinlinlay.addView(tv1,0);
		dineinlinlay.addView(tv2,1);
		
		parser(false);

		return vg;
		}
		else
		{	
			((ViewGroup)(vg.getParent())).removeView(vg);
			return vg;
		}
	}
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		
		parentAct = this.getSherlockActivity();
		
		tv1 = new TextView(parentAct);
		tv2 = new TextView(parentAct);

		postdata = new ArrayList<BasicNameValuePair>();
		postdata.add(new BasicNameValuePair("rRequestSw","B"));
		
		dtransactionlist = new ArrayList<String>();
		ta = new TransactionAdapter(parentAct, this, dtransactionlist);
	
			tv1.setGravity(0x01);		
		  	tv2.setGravity(0x01);
		  			
		 	tv1.setTextSize(20);
		 	 	
		    tv1.setTextColor(Color.DKGRAY);
		    tv2.setTextColor(Color.DKGRAY);

	}
	public void parser(boolean refresh)
	{
		
		httpclient = ConnectionHelper.getThreadSafeClient();
		httpclient.getCookieStore().clear();
		
		BasicClientCookie screen = new BasicClientCookie("webBrowserSize", "B");
		screen.setDomain(".utexas.edu");
		httpclient.getCookieStore().addCookie(screen);
		BasicClientCookie cookie = new BasicClientCookie("SC", ConnectionHelper.getAuthCookie(parentAct,httpclient));
		cookie.setDomain(".utexas.edu");
		httpclient.getCookieStore().addCookie(cookie);
		
		fetch = new fetchTransactionDataTask(httpclient, refresh);
		fetch.execute();	
	}
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) 
	{
		menu.removeItem(R.id.balance_refresh);
	    inflater.inflate(R.menu.balance_menu, menu);     
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
	    	int id = item.getItemId();
	    	switch(id)
	    	{
	    		case R.id.balance_refresh:
	    			
					dlv.setVisibility(View.GONE);
					etv.setVisibility(View.GONE);
					d_pb_ll.setVisibility(View.VISIBLE);
					if(fetch!=null)
					{	fetch.cancel(true);
						fetch = null;
					}
	    			dtransactionlist.clear();
					dineinbalance = "No Dine In Dollars? What kind of animal are you?";
					postdata.clear();
					postdata.add(new BasicNameValuePair("rRequestSw","B"));
					parser(true);		

		    		break;
	    	}
	    	ta.resetPage();
    		dlv.setSelectionFromTop(0, 0);
	    	return true;
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(fetch!=null)
			fetch.cancel(true);
	}
	private class fetchTransactionDataTask extends AsyncTask<Object,Void,Character>
	{
		private DefaultHttpClient client;
		private boolean refresh;
		private String errorMsg;
		private ArrayList<String> tempTransactionList;
		
		public fetchTransactionDataTask(DefaultHttpClient client, boolean refresh)
		{
			this.client = client;
			this.refresh = refresh;
		}
		@Override
		protected Character doInBackground(Object... params)
		{
			HttpPost hpost = new HttpPost("https://utdirect.utexas.edu/hfis/transactions.WBX");
	    	String pagedata="";
	    	tempTransactionList = new ArrayList<String>();
	    	try
			{
				hpost.setEntity(new UrlEncodedFormEntity(postdata));
				HttpResponse response = client.execute(hpost);
		    	pagedata = EntityUtils.toString(response.getEntity());
			} catch (Exception e)
			{
				e.printStackTrace();
				errorMsg = "UTilities could not fetch transaction data.  Try refreshing.";
				cancel(true);
				return null;
			}
	    	
	    	Pattern pattern3 = Pattern.compile("(?<=\"center\">\\s{1,10})\\S.*(?=\\s*<)");
	    	Matcher matcher3 = pattern3.matcher(pagedata);
	    	
	    	Pattern pattern4 = Pattern.compile("(?<=\"right\">\\s).*(?=</td>\\s*<td)");
	    	Matcher matcher4 = pattern4.matcher(pagedata);
	    	
	    	Pattern datepattern = Pattern.compile("(?<=\"left\">\\s{1,10})\\S+");
	    	Matcher datematcher = datepattern.matcher(pagedata);
	    	
	    	Pattern balancepattern = Pattern.compile("(?<=\"right\">\\s).*(?=</td>\\s*</tr)");
	    	Matcher balancematcher = balancepattern.matcher(pagedata);
	    	
	    	if(balancematcher.find() && ta.page == 1)
	    	{
	    		dineinbalance = balancematcher.group();	
	    	}
	    	while(matcher3.find() && matcher4.find() && datematcher.find() && !this.isCancelled())
	    	{
	    		String transaction=datematcher.group()+" ";
	    		transaction+=matcher3.group()+" ";
	    		transaction+=matcher4.group().replaceAll("\\s","");
	    		tempTransactionList.add(transaction);
	    	}
	    	if(pagedata.contains("<form name=\"next\"") && !this.isCancelled())
	    	{
	    		Pattern namePattern = Pattern.compile("sNameFL\".*?value=\"(.*?)\"");
	    		Matcher nameMatcher = namePattern.matcher(pagedata);
	    		Pattern nextTransPattern = Pattern.compile("nexttransid\".*?value=\"(.*?)\"");
	    		Matcher nextTransMatcher = nextTransPattern.matcher(pagedata);
	    		Pattern dateTimePattern = Pattern.compile("sStartDateTime\".*?value=\"(.*?)\"");
	    		Matcher dateTimeMatcher = dateTimePattern.matcher(pagedata);
	    		if(nameMatcher.find() && nextTransMatcher.find() && dateTimeMatcher.find() && !this.isCancelled())
	    		{	
	    			postdata.clear();
			    	postdata.add(new BasicNameValuePair("sNameFL",nameMatcher.group(1)));
			    	postdata.add(new BasicNameValuePair("nexttransid",nextTransMatcher.group(1)));
			    	postdata.add(new BasicNameValuePair("rReqeuestSw","B"));
			    	postdata.add(new BasicNameValuePair("sStartDateTime",dateTimeMatcher.group(1)));
			    }
	    		return 'm';
	    	}
	    	else
	    		return 'n';
		}
		@Override
		protected void onPostExecute(Character result)
		{
			if (!this.isCancelled())
	    	{
				dtransactionlist.addAll(tempTransactionList);
				ta.notifyDataSetChanged();
				ta.updateHeaders();
				ta.notifyDataSetChanged();
				int index = dlv.getFirstVisiblePosition();
		    	View v = dlv.getChildAt(0);
		    	int top = (v == null) ? 0 : v.getTop();
	    		if(result == 'm')
	    		{
	    			ta.notifyMayHaveMorePages();
	    		}
	    		if(result == 'n')
	    			ta.notifyNoMorePages();
	    		if(!refresh)
	    			dlv.setSelectionFromTop(index, top);
	    		else
	    			dlv.setSelection(0);
	    		tv1.setText("Dine In Dollars ");
				tv2.setText(dineinbalance);
	    		
	    		d_pb_ll.setVisibility(View.GONE);
	    		etv.setVisibility(View.GONE);
				dlv.setVisibility(View.VISIBLE);
	    		
	    	} 	
		}
		@Override
		protected void onCancelled(Character nullIfError)
		{
			if(nullIfError == null)
			{
				//etv off center, not sure if worth hiding the balance stuff to get it centered
				etv.setText(errorMsg);
				d_pb_ll.setVisibility(View.GONE);
				dlv.setVisibility(View.GONE);
				etv.setVisibility(View.VISIBLE);
			}
		}
	}
}
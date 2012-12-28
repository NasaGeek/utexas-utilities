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
import com.nasageek.utexasutilities.adapters.TransactionAdapter;


public class BevoFragment extends SherlockFragment
{
	private  DefaultHttpClient httpclient;
	private LinearLayout b_pb_ll;
	private LinearLayout bevolinlay;
	private AmazingListView blv;
	ArrayList<String> btransactionlist;
	String[] btransactionarray;
	int count;
	private TransactionAdapter ta;
	TextView tv3,tv4;
	private TextView etv;
	
	private List<BasicNameValuePair> postdata;
	private SherlockFragmentActivity parentAct;
	
	ViewGroup cont;
	View vg;

	String bevobalance="";
	private fetchTransactionDataTask fetch;	
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		if(vg==null)
		{vg = inflater.inflate(R.layout.bevo_fragment_layout, container, false);
		
		blv = (AmazingListView) vg.findViewById(R.id.btransactions_listview);
		bevolinlay = (LinearLayout) vg.findViewById(R.id.bevolinlay);
		b_pb_ll = (LinearLayout) vg.findViewById(R.id.bevo_progressbar_ll);
		etv = (TextView) vg.findViewById(R.id.bevo_error);
		
		
		//must call setLoadingView before setAdapter
		blv.setLoadingView(inflater.inflate(R.layout.loading_content_layout, null));
		blv.setAdapter(ta);
		ta.notifyDataSetChanged();

	    bevolinlay.addView(tv3,0);
		bevolinlay.addView(tv4,1);
		
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
		
		tv3 = new TextView(parentAct);
		tv4 = new TextView(parentAct);
		
		postdata = new ArrayList<BasicNameValuePair>();
		postdata.add(new BasicNameValuePair("sRequestSw","B"));
		
		btransactionlist = new ArrayList<String>();
		ta = new TransactionAdapter(parentAct, this, btransactionlist);
	
			tv3.setGravity(0x01);
		  	tv4.setGravity(0x01);
		  	
		 	tv3.setTextSize(20);
		 	
		    tv3.setTextColor(Color.DKGRAY);
		    tv4.setTextColor(Color.DKGRAY);
		    		
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
	    			
	    			blv.setVisibility(View.GONE);
	    			etv.setVisibility(View.GONE);
					b_pb_ll.setVisibility(View.VISIBLE);
					if(fetch!=null)
					{	fetch.cancel(true);
						fetch = null;
					}
					btransactionlist.clear();
					bevobalance = "";
					postdata.clear();
					postdata.add(new BasicNameValuePair("sRequestSw","B"));
					parser(true);		
					
		    		break;
	    	}
	    	ta.resetPage();
    		blv.setSelectionFromTop(0, 0);
	    	return true;
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
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
	    	if(pagedata.contains("<title>Information Technology Services - UT EID Logon</title>"))
	    	{
				errorMsg = "You've been logged out of UTDirect, back out and log in again.";
				ConnectionHelper.logout(parentAct);
				cancel(true);
				return null;
	    	}
	    	
	    	Pattern pattern3 = Pattern.compile("\"center\">\\s+(.*?)\\s*<");
	    	Matcher matcher3 = pattern3.matcher(pagedata);
	    	
	    	Pattern pattern4 = Pattern.compile("\"right\">\\s*(.*)</td>\\s*<td");
	    	Matcher matcher4 = pattern4.matcher(pagedata);
	    	
	    	Pattern datepattern = Pattern.compile("\"left\">\\s*?(\\S+)");
	    	Matcher datematcher = datepattern.matcher(pagedata);
	    	
	    	Pattern balancepattern = Pattern.compile("\"right\">\\s*(.*)</td>\\s*</tr");
	    	Matcher balancematcher = balancepattern.matcher(pagedata);
	    	
	    	if(balancematcher.find() && ta.page == 1)
	    	{	
	    		bevobalance = balancematcher.group(1);	
	    	}
	    	while(matcher3.find() && matcher4.find() && datematcher.find() && !this.isCancelled())
	    	{
	    		String transaction=datematcher.group(1)+" ";
	    		transaction+=matcher3.group(1).trim()+" ";
	    		transaction+=matcher4.group(1).replaceAll("\\s","");
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
			    	postdata.add(new BasicNameValuePair("sRequestSw","B"));
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
				btransactionlist.addAll(tempTransactionList);
				ta.notifyDataSetChanged();
				ta.updateHeaders();
				ta.notifyDataSetChanged();
				int index = blv.getFirstVisiblePosition();
		    	View v = blv.getChildAt(0);
		    	int top = (v == null) ? 0 : v.getTop();
	    		if(result == 'm')
	    		{
	    			ta.notifyMayHaveMorePages();
	    		}
	    		if(result == 'n')
	    			ta.notifyNoMorePages();
	    		if(!refresh)
	    			blv.setSelectionFromTop(index, top);
	    		else
	    			blv.setSelection(0);
	    		tv3.setText("Bevo Bucks ");
				tv4.setText(bevobalance);
	    		
				b_pb_ll.setVisibility(View.GONE);
				etv.setVisibility(View.GONE);
				blv.setVisibility(View.VISIBLE);
	    		
	    	} 	
		}
		@Override
		protected void onCancelled(Character nullIfError)
		{
			if(nullIfError == null)
			{
				//etv off center, not sure if worth hiding the balance stuff to get it centered
				etv.setText(errorMsg);
				b_pb_ll.setVisibility(View.GONE);
				blv.setVisibility(View.GONE);
				etv.setVisibility(View.VISIBLE);
			}
		}
	}
}
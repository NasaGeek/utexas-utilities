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

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.foound.widget.AmazingListView;
import com.nasageek.utexasutilities.ConnectionHelper;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.adapters.TransactionAdapter;
import com.nasageek.utexasutilities.model.Transaction;


//TODO: last transaction doesn't show when loading dialog is present at the bottom, low priority fix

public class TransactionsFragment extends SherlockFragment
{
	private  DefaultHttpClient httpclient;
	private LinearLayout t_pb_ll;
	private AmazingListView tlv;
	private ArrayList<Transaction> transactionlist;
	private TransactionAdapter ta;
	private TextView balanceLabelView, balanceView;
	private TextView etv;
	
	private List<BasicNameValuePair> postdata;
	private SherlockFragmentActivity parentAct;

	private View vg;
	
	private String balance = "";
	private fetchTransactionDataTask fetch;
	
	public enum TransactionType
	{
		Bevo, Dinein;
	}
	private TransactionType type;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		if(vg==null)
		{
			vg =  inflater.inflate(R.layout.transactions_fragment_layout, container, false);
			
			tlv = (AmazingListView) vg.findViewById(R.id.transactions_listview);
			t_pb_ll = (LinearLayout) vg.findViewById(R.id.trans_progressbar_ll);
			etv = (TextView) vg.findViewById(R.id.trans_error);
			balanceLabelView = (TextView) vg.findViewById(R.id.balance_label_tv);
			balanceView = (TextView) vg.findViewById(R.id.balance_tv);
			
			tlv.setLoadingView(inflater.inflate(R.layout.loading_content_layout, null));
			tlv.setAdapter(ta);
			
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
		
		postdata = new ArrayList<BasicNameValuePair>();
		
		type = (TransactionType) getArguments().getSerializable("type");
		
		if(TransactionType.Bevo.equals(type))
			postdata.add(new BasicNameValuePair("sRequestSw","B"));
		else if(TransactionType.Dinein.equals(type))
			postdata.add(new BasicNameValuePair("rRequestSw","B"));
		
		transactionlist = new ArrayList<Transaction>();
		ta = new TransactionAdapter(parentAct, this, transactionlist);

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
	    			
					tlv.setVisibility(View.GONE);
					etv.setVisibility(View.GONE);
					t_pb_ll.setVisibility(View.VISIBLE);
					if(fetch!=null)
					{	fetch.cancel(true);
						fetch = null;
					}
	    			transactionlist.clear();
					balance = "";
					postdata.clear();

					if(TransactionType.Bevo.equals(type))
						postdata.add(new BasicNameValuePair("sRequestSw","B"));
					else if(TransactionType.Dinein.equals(type))
						postdata.add(new BasicNameValuePair("rRequestSw","B"));
						
					parser(true);		

		    		break;
	    	}
	    	ta.resetPage();
    		tlv.setSelectionFromTop(0, 0);
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
		private ArrayList<Transaction> tempTransactionList;
		
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
	    	tempTransactionList = new ArrayList<Transaction>();
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
	    	
	    	Pattern reasonPattern = Pattern.compile("\"center\">\\s+(.*?)\\s*<");
	    	Matcher reasonMatcher = reasonPattern.matcher(pagedata);
	    	
	    	Pattern costPattern = Pattern.compile("\"right\">\\s*(.*)</td>\\s*<td");
	    	Matcher costMatcher = costPattern.matcher(pagedata);
	    	
	    	Pattern datePattern = Pattern.compile("\"left\">\\s*?(\\S+)");
	    	Matcher dateMatcher = datePattern.matcher(pagedata);
	    	
	    	Pattern balancePattern = Pattern.compile("\"right\">\\s*(.*)</td>\\s*</tr");
	    	Matcher balanceMatcher = balancePattern.matcher(pagedata);
	    	
	    	if(balanceMatcher.find() && ta.page == 1)
	    	{
	    		balance = balanceMatcher.group(1);	
	    	}
	    	while(reasonMatcher.find() && costMatcher.find() && dateMatcher.find() && !this.isCancelled())
	    	{
	    		Transaction tran = new Transaction(reasonMatcher.group(1).trim(), costMatcher.group(1).replaceAll("\\s",""), dateMatcher.group(1));
	    		
	    //		String transaction=datematcher.group(1)+" ";
	    //		transaction+=matcher3.group(1).trim()+" ";
	    //		transaction+=matcher4.group(1).replaceAll("\\s","");
	    		tempTransactionList.add(tran);
	    	}
	    	if(pagedata.contains("<form name=\"next\"") && !this.isCancelled()) //check for additional pages
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
			    	if(TransactionType.Bevo.equals(type))
			    		postdata.add(new BasicNameValuePair("sRequestSw","B"));
			    	else if(TransactionType.Dinein.equals(type))
			    		postdata.add(new BasicNameValuePair("rRequestSw","B"));
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
				transactionlist.addAll(tempTransactionList);
				ta.notifyDataSetChanged();
				ta.updateHeaders();
				ta.notifyDataSetChanged();
				int index = tlv.getFirstVisiblePosition();
		    	View v = tlv.getChildAt(0);
		    	int top = (v == null) ? 0 : v.getTop();
	    		if(result == 'm')
	    		{
	    			ta.notifyMayHaveMorePages();
	    		}
	    		if(result == 'n')
	    			ta.notifyNoMorePages();
	    		if(!refresh)
	    			tlv.setSelectionFromTop(index, top);
	    		else
	    			tlv.setSelection(0);

	    		if(TransactionType.Bevo.equals(type))
	    			balanceLabelView.setText("Bevo Bucks ");
				else if(TransactionType.Dinein.equals(type))
					balanceLabelView.setText("Dine In Dollars ");
				
	    		balanceView.setText(balance);
	    		
	    		t_pb_ll.setVisibility(View.GONE);
	    		etv.setVisibility(View.GONE);
				tlv.setVisibility(View.VISIBLE);
	    		
	    	} 	
		}
		@Override
		protected void onCancelled(Character nullIfError)
		{
			if(nullIfError == null)
			{
				if(ta.page == 1) //if the first page fails just hide everything
				{	
					//etv off center, not sure if worth hiding the balance stuff to get it centered
					etv.setText(errorMsg);
					t_pb_ll.setVisibility(View.GONE);
					tlv.setVisibility(View.GONE);
					etv.setVisibility(View.VISIBLE);
				}
				else //on later pages we should let them see what's already loaded
				{
					Toast.makeText(parentAct, errorMsg, Toast.LENGTH_SHORT).show();
					ta.notifyNoMorePages();
					ta.notifyDataSetChanged();
				}
			}
		}
	}
}
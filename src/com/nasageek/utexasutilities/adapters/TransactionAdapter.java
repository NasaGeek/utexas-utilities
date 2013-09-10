package com.nasageek.utexasutilities.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.foound.widget.AmazingAdapter;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.fragments.TransactionsFragment;
import com.nasageek.utexasutilities.model.Transaction;


public class TransactionAdapter extends AmazingAdapter {
	private Context con;
	private ArrayList<Boolean> areHeaders;
	private ArrayList<Transaction> transactions;
	private LayoutInflater li;
	private String currentDate;
	boolean isSectionHeader;
	private TransactionsFragment frag;
	
	public TransactionAdapter(Context c, TransactionsFragment frag, ArrayList<Transaction> transactions) {
		con = c;
		this.transactions = transactions;
		this.frag = frag;
		areHeaders = new ArrayList<Boolean>();
		li = (LayoutInflater)con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		//just call updateHeaders() here? Did I duplicate this for a reason?
		for(int i = 0; i<transactions.size(); i++) {
			String date = transactions.get(i).getDate();
			if(i == 0) {
				currentDate = date;
				areHeaders.add(true);
			}
			else if(currentDate.equals(date)) {
				areHeaders.add(false);
			}
			else {
				areHeaders.add(true);
				currentDate=date;
			}
		}	
	}
	public int getCount() {
		return transactions.size();
	}

	public Object getItem(int position) {
		return transactions.get(position);
	}

	public long getItemId(int position) {
		return position;
	}
	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}
	@Override
	public boolean isEnabled(int i) {
		return false;
	}
	
	//TODO: fix crash on refresh when in the middle of a scroll
	@Override
	public View getAmazingView(int position, View convertView, ViewGroup parent) {
		
/*		if(position == transactions.size() - 1)
			return new View(con);*/
		Transaction trans = transactions.get(position);
		
		String date = trans.getDate();
		String reason = "\t" + trans.getReason();
		String cost = trans.getCost();
		
		ViewGroup lin = (ViewGroup) convertView;
	
		if (areHeaders.size() == transactions.size() && areHeaders.get(position)) {
			lin =(ViewGroup)li.inflate(R.layout.trans_item_header_view,null,false);
			TextView dateview = (TextView) lin.findViewById(R.id.list_item_section_text);
			dateview.setText(date);
		}
		else {
			lin = (ViewGroup)li.inflate(R.layout.trans_item_view,null,false);
		}
		
		TextView left= (TextView) lin.findViewById(R.id.itemview);
		left.setText(reason);
		TextView right = (TextView) lin.findViewById(R.id.costview);
		right.setText(cost);

		return (View)lin;
	}
	public void updateHeaders() {
		areHeaders.clear();
		for(int i = 0; i<transactions.size(); i++) {
			String date = transactions.get(i).getDate();
			if(i == 0) {
				currentDate = date;
				areHeaders.add(true);
			}
			else if(currentDate.equals(date)) {
				areHeaders.add(false);
			}
			else  {
				areHeaders.add(true);
				currentDate=date;
			}
		}	
	}
	protected View getLoadingView(ViewGroup parent) {
		if (con != null) {
		      LayoutInflater inflater =
		          (LayoutInflater)con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		      return inflater.inflate(R.layout.loading_content_layout, parent, false);
		}
		else
			return new View(con);
	}
	@Override
	protected void onNextPageRequested(int page) {
		
		if(super.automaticNextPageLoading) {	
			nextPage();
		//	Log.d("TransactionAdapter","Page requested!");
			frag.parser(false);
		}
		super.automaticNextPageLoading = false;
	}
	@Override
	protected void bindSectionHeader(View view, int position, boolean displaySectionHeader) {
		return;
	}
	@Override
	public void configurePinnedHeader(View header, int position, int alpha) {
		return;
	}
	@Override
	public int getPositionForSection(int section) {
		return 0;
	}
	@Override
	public int getSectionForPosition(int position) {
		return 0;
	}
	@Override
	public String[] getSections() {
		return null;
	}

}

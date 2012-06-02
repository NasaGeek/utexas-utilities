package com.nasageek.utexasutilities;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TransactionAdapter extends ArrayAdapter
{
	private Context con;
	private ArrayList<Boolean> areHeaders;
	private ArrayList<String> transactions;
	private LayoutInflater li;
	private String currentDate;
	boolean isSectionHeader;
	
	
	public TransactionAdapter(Context c, ArrayList<String> objects)
	{
		super(c,0,objects);
		con = c;
		transactions = objects;
		areHeaders = new ArrayList<Boolean>();
		li = (LayoutInflater)con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		for(int i = 0; i<transactions.size(); i++)
		{
			String dateandplace  = transactions.get(i).substring(0,transactions.get(i).indexOf("$"));
			String date = dateandplace.substring(0,dateandplace.indexOf(" "));
			if(i == 0)
			{
				currentDate = date;
				areHeaders.add(true);
			}
			else if(currentDate.equals(date))
			{
				areHeaders.add(false);
			}
			else 
			{
				areHeaders.add(true);
				currentDate=date;
			}
		}
		
	}
	public int getCount() {
		// TODO Auto-generated method stub
		return transactions.size();
	}

	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return transactions.get(position);
	}

	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public boolean areAllItemsEnabled()
	{
		return true;
	}
	@Override
	public boolean isEnabled(int i)
	{
		return false;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		/*String place = transactions.get(position).split(" ")[0];
		String cost = transactions.get(position).split(" ")[1];
		TextView left= new TextView(con);
		left.setText(place);
		TextView right = new TextView(con);
		right.setText(cost);
		LinearLayout lin = new LinearLayout(con);
		lin.addView(left);lin.addView(right);
		Log.d("GETVIEW", position+"");
		return (View)lin;
		*/
		String trans = transactions.get(position);
		
		String dateplace = trans.substring(0,trans.indexOf("$"));
		String date = dateplace.substring(0,dateplace.indexOf(" "));
		String place = "\t"+dateplace.substring(dateplace.indexOf(" "));
		String cost = trans.substring(trans.indexOf("$"));
		
		
		ViewGroup lin = (ViewGroup) convertView;
	
		if (areHeaders.get(position))
		{
			lin =(ViewGroup)li.inflate(R.layout.trans_item_header_view,null,false);
			TextView dateview = (TextView) lin.findViewById(R.id.list_item_section_text);
			dateview.setText(date);
		}
		else
		{
			lin = (ViewGroup)li.inflate(R.layout.trans_item_view,null,false);
		}
		

		TextView left= (TextView) lin.findViewById(R.id.itemview);
		left.setText(place);
		TextView right = (TextView) lin.findViewById(R.id.costview);
		right.setText(cost);

		return (View)lin;
	}
}

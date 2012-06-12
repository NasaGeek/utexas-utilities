package com.nasageek.utexasutilities;

import java.util.ArrayList;
import java.util.List;

import com.foound.widget.AmazingAdapter;
import com.nasageek.utexasutilities.MenuFragment.food;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BBClassAdapter extends AmazingAdapter
{
	private Context con;
	private ArrayList<BBClass> classes;
	private ArrayList<Pair<String,ArrayList<BBClass>>> all;
	private LayoutInflater li;
	
	public BBClassAdapter(Context con, ArrayList<Pair<String,ArrayList<BBClass>>> objects)
	{
		all = objects;
		li = (LayoutInflater)con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	
		
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
	public BBClass getItem(int position) {
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
	public boolean areAllItemsEnabled()
	{
		return true;
	}
	@Override
	public boolean isEnabled(int i)
	{
		return true;
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
		ViewGroup res = (ViewGroup) convertView;
		if (res == null) res = (ViewGroup) li.inflate(R.layout.bbclass_item_view, null, false);
		
		BBClass bbclass = getItem(position);
		
		String name = bbclass.getName().substring(0,bbclass.getName().indexOf("(")-1);
		String unique = bbclass.getCourseid().split("_")[2];
		String id = bbclass.getCourseid().substring(bbclass.getCourseid().indexOf(unique)+6).replaceAll("_"," ");
		
		TextView nameview= (TextView) res.findViewById(R.id.bb_class_name);
		TextView idview= (TextView) res.findViewById(R.id.bb_class_id);
		
		nameview.setText(name);
		idview.setText(id+" - "+unique);
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

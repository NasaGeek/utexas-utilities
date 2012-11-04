package com.nasageek.utexasutilities;

import java.util.ArrayList;

import android.content.Context;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.foound.widget.AmazingAdapter;

public class BBClassAdapter extends AmazingAdapter
{
	private Context con;
	private ArrayList<BBClass> classes;
	private ArrayList<Pair<String,ArrayList<BBClass>>> all;
	private LayoutInflater li;
	private Boolean longform;
	
	public BBClassAdapter(Context con, ArrayList<Pair<String,ArrayList<BBClass>>> objects)
	{
		all = objects;
		li = (LayoutInflater)con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);	
		longform = PreferenceManager.getDefaultSharedPreferences(con).getBoolean("blackboard_class_longform", false);
		this.con = con;
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
		ViewHolder holder;
		ViewGroup res = (ViewGroup) convertView;

		if (res == null) 
		{	
			res = (ViewGroup) li.inflate(R.layout.bbclass_item_view, null, false);
			holder = new ViewHolder();
			holder.idview = (TextView) res.findViewById(R.id.bb_class_id);
			holder.nameview= (TextView) res.findViewById(R.id.bb_class_name);
			res.setTag(holder);
		}
		else
			holder = (ViewHolder) res.getTag();
		
		BBClass bbclass = getItem(position);
		String name = "", unique = "", id = "";
		
		if(!longform)
		{														
			if(!bbclass.isFullCourseIdTooShort())	
			{	
				if(bbclass.isCourseIdAvailable())
				{
					holder.idview.setText(bbclass.getCourseId()+" - "+bbclass.getUnique());
				}
				else
					holder.idview.setText(bbclass.getUnique());
				
			}
			else
			{
				holder.idview.setText(bbclass.getCourseId());
			}
		}
		else //probably not even necessary anymore, necessary checking is done in the if-statement
		{	
			unique = bbclass.getFullCourseid();
			//id not set because unique will contain ID and Unique number
			holder.idview.setText(unique);		
		}

		holder.nameview.setText(bbclass.getName());
		
		return res;
	}
	
	class ViewHolder
	{
		TextView nameview;
		TextView idview;
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
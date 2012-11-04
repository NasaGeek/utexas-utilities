package com.nasageek.utexasutilities;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.nasageek.utexasutilities.R;

public class cBookBaseAdapter extends BaseAdapter {

	private static ArrayList<cBook> bookArrayList;
	private LayoutInflater mInflater;

	public cBookBaseAdapter(Context context, ArrayList<cBook> results) {
		bookArrayList = results;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return bookArrayList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return bookArrayList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.c_book_layout, null);
			holder = new ViewHolder();
			holder.title = (TextView) convertView
			.findViewById(R.id.title);
			holder.barcode = (TextView) convertView
			.findViewById(R.id.barcode);
			holder.callNo = (TextView) convertView
			.findViewById(R.id.callNo);
			holder.status = (TextView) convertView
			.findViewById(R.id.status);
			holder.renew = (CheckBox) convertView.findViewById(R.id.renewBox);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final cBook b = bookArrayList.get(position);

		holder.title.setText(b.title);
		holder.barcode.setText(b.barcode);
		holder.status.setText(b.status);
		holder.callNo.setText(b.callNumber);
//		holder.renew.setChecked(b.renew);
		
        holder.renew.setOnCheckedChangeListener(new OnCheckedChangeListener(){

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            	b.renew = isChecked;
            }               
        });
		

		return convertView;
	}

	static class ViewHolder {
		TextView title;
		TextView callNo;
		TextView status;
		TextView barcode;
		CheckBox renew;

	}

}

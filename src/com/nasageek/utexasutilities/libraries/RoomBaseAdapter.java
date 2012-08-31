package com.nasageek.utexasutilities.libraries;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

public class RoomBaseAdapter extends BaseAdapter {
	private static ArrayList<Room> allRooms;
	private LayoutInflater mInflater;
	Context context;

	public RoomBaseAdapter(Context con, ArrayList<Room> results) {
		allRooms = results;
		mInflater = LayoutInflater.from(con);
		context = con;

	}

	@Override
	public int getCount() {
		return allRooms.size();
	}

	@Override
	public Object getItem(int position) {
		return allRooms.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.room_layout, null);
			holder = new ViewHolder();

			//retrieve views from layout

			holder.location = (TextView) convertView.findViewById(R.id.location);
			holder.room = (TextView) convertView.findViewById(R.id.roomNumber);
			holder.reqFeatures = (TextView) convertView.findViewById(R.id.reqFeatures);
			holder.seating = (TextView) convertView.findViewById(R.id.seating);
			holder.available = (TextView) convertView.findViewById(R.id.available);
			holder.reserve = (Button) convertView.findViewById(R.id.reserveRoomButton);



			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final Room r = allRooms.get(position);

		holder.location.setText(r.location);
		holder.room.setText(r.room);
		holder.reqFeatures.setText(r.reqFeatures);
		holder.seating.setText(r.seating);
		holder.available.setText(r.available);



		holder.reserve.setFocusable(false);  //needed to make listview clickable
		holder.reserve.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v){

				//create intent, bundling the current room into the event.
				//new activity will visualize the page retrieved from the link
				//and let user finalize room with times

			}
		});

		return convertView;
	}

	static class ViewHolder {

		TextView location;
		TextView room;
		TextView reqFeatures;
		TextView seating;
		TextView available;
		Button reserve;
	}

}

package com.nasageek.utexasutilities.libraries;


import java.util.ArrayList;

import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.IntentAction;

public class displayRoomResults extends Activity {

	Context context;
	ArrayList<ArrayList<Room>> allRooms;
	ProgressDialog dialog;
	DefaultHttpClient client;
	Bundle bundle;
	LinearLayout linLayout;
	LayoutInflater mInflater;
	View view;
	Handler handler;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		bundle = getIntent().getExtras();
		setTitleColor(getResources().getColor(R.color.snow2));

		dialog = new ProgressDialog(this,R.style.CustomDialog);
		dialog.setMessage("Loading. Please wait...");
		dialog.show();
//		dialog = ProgressDialog.show(this, "", "Loading. Please wait...", true);
		setContentView(R.layout.room_results2);
		context = this;
		handler = new Handler();
		mInflater = LayoutInflater.from(context);
		view = mInflater.inflate(R.layout.room_results2,null);
		linLayout = (LinearLayout) view.findViewById(R.id.roomsLinearLayout);
		shared.checkLogInCredentials(this, handler, !shared.displayedLogInCheck, "You are not connected to the internet. Please try again later.");

		// code downloaded from
		// https://github.com/johannilsson/android-actionbar/blob/master/README.md
		ActionBar actionBar = (ActionBar) linLayout.findViewById(R.id.actionbar);
		actionBar.setHomeAction(new IntentAction(this, new Intent(this,WelcomeScreen.class), R.drawable.home)); // go	// home
		actionBar.setTitle("Room Results");
		actionBar.addAction(new IntentAction(this, new Intent(this, settings.class), R.drawable.gear)); //go to settings
		//----------------------

		(new Thread(new getDataThread())).start();
	}

	private class getDataThread implements Runnable{

		@SuppressWarnings("unused")
		@Override
		public void run() {
//			Looper.prepare?();
			//log into UT direct with new client

			client = new DefaultHttpClient();
			shared.logIntoUTDirect(context,client);

			String uri = createURIfromData(bundle);
			String html = shared.retrieveProtectedWebPage(context,client, uri);
			if (shared.LOGGINGLEVEL>0) Log.i("displayRoomResults", "getDataThread html:" + html);
			//parse rooms page
			allRooms = parseRoomResults.extractRooms(html);
			handler.post(new Runnable(){
				public void run() {
					if (allRooms.size()==0)
					{
						//		View view = findViewById(R.id.searchResultsLinearLayout);
						final AlertDialog.Builder builder = new AlertDialog.Builder(context);
						builder.setMessage("Sorry, there are no rooms that match the given criteria. Please modify search parameters and try again.").setCancelable(true)
						.setNeutralButton("OK", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								Intent intent = new Intent(context, reserveStudyRoom.class);
								startActivity(intent);
								dialog.dismiss();
							}
						}
						);
						dialog.dismiss();
						final AlertDialog alert = builder.create();
						alert.show();
					}
					dialog.dismiss();
					displayAllRooms(allRooms, linLayout, mInflater,view);
				}
			});
		}
	}

	@SuppressWarnings("unused")
	public void displayAllRooms (ArrayList<ArrayList<Room>> rooms, LinearLayout linLayout, LayoutInflater mInflater, View view)
	{
		Context context = this;

		try {

			int outer = 0, inner = 0; //setting tags in table rows so that know which button was pressed

			for (ArrayList<Room> byLocation : rooms)
			{
				if (shared.LOGGINGLEVEL>0) Log.i("displayRoomResults","in by Location");
				TextView tv = new TextView(context);
				tv.setText(byLocation.get(0).location);
				tv.setTypeface(Typeface.DEFAULT_BOLD, Typeface.BOLD);
				tv.setTextSize(20);
				linLayout.addView(tv);

				TableLayout table = new TableLayout(context);
				table.setStretchAllColumns(true);
				table.setShrinkAllColumns(true);
				table.setGravity(Gravity.CENTER);

				TableRow header = (TableRow) mInflater.inflate(R.layout.room_row, null);
				header.setGravity(Gravity.CENTER);

				((TextView) header.findViewById(R.id.roomname))
				.setText("Room");
				((TextView) header.findViewById(R.id.hasFeatures))
				.setText("Has Requested Features");
				((TextView) header.findViewById(R.id.roomsize))
				.setText("Capacity");
				((TextView) header.findViewById(R.id.available))
				.setText("Available?");
				header.removeView(header.findViewById(R.id.reserveRoomButton));
				TextView takeupbuttonspace = new TextView(context);
				takeupbuttonspace.setText("");
				header.addView(takeupbuttonspace);

				for (int i=0;i<header.getChildCount();i++)
				{
					View v = header.getChildAt(i);
					if (v instanceof TextView){
						((TextView)v).setGravity(Gravity.CENTER);
					}
				}
				table.addView(header);
				inner = 0;
				for (Room room: byLocation){
					if (shared.LOGGINGLEVEL>0) Log.i("displayRoomResults","in room");

					TableRow roomRow = (TableRow) mInflater.inflate(R.layout.room_row, null);
					roomRow.setGravity(Gravity.CENTER);
					((TextView) roomRow.findViewById(R.id.roomname))
					.setText(room.room);
					((TextView) roomRow.findViewById(R.id.hasFeatures))
					.setText(room.reqFeatures);
					((TextView) roomRow.findViewById(R.id.roomsize))
					.setText(room.seating);
					((TextView) roomRow.findViewById(R.id.available))
					.setText(room.available);

					((Button)roomRow.findViewById(R.id.reserveRoomButton)).setTag(new int[]{outer, inner});

					for (int i=0;i<roomRow.getChildCount();i++)
					{
						View v = roomRow.getChildAt(i);
						if (v instanceof TextView)
							((TextView)v).setGravity(Gravity.CENTER);
					}
					table.addView(roomRow);
					inner++;
				}
				linLayout.addView(table);
				outer++;
			}
		}
		catch (Exception e) {
			if (shared.LOGGINGLEVEL>0) Log.e("displayRoomResults", "Exception in displayRooms",e);
		}
		ScrollView sv = new ScrollView(context);
		sv.addView(linLayout);
		setContentView(sv);
	}

	public void roomSelected(View view)
	{
		int[]tag = (int[])view.getTag();
		Room selected = allRooms.get(tag[0]).get(tag[1]);
		String roomLink = selected.reserveLink;
		roomLink = roomLink.substring(roomLink.indexOf("roomID"));
		String id = roomLink.substring(roomLink.indexOf("=")+1, roomLink.indexOf("&"));

		//just sending bundle from last intent, with room id appended
		Bundle bundle = new Bundle();
		bundle.putString("roomID", id);
		bundle.putString("year", year);
		bundle.putString("month", month);
		bundle.putString("day", day);
		bundle.putString("location", selected.location);
		bundle.putString("room", selected.room);
		bundle.putInt("startHour", startHour);
		bundle.putInt("endHour", endHour);
		bundle.putInt("startMinute", startMinute);
		bundle.putInt("endMinute", endMinute);
		bundle.putString("startPM", startPM);
		bundle.putString("endPM", endPM);

		Intent intent = new Intent(this,finalizeRoomReservation.class);
		intent.putExtras(bundle);
		startActivity(intent);

	}
	//needed for next activity - finalizing reservation
	String year;
	String month;
	String day;
	int startHour;
	int startMinute;
	int endHour;
	int endMinute;
	String startPM;
	String endPM;

	public String createURIfromData(Bundle bundle)
	{
		int [] date = bundle.getIntArray("Date");
		int number = bundle.getInt("number");
		boolean [] building = bundle.getBooleanArray("building");
		boolean [] roomreq = bundle.getBooleanArray("room");
		String [] pm = bundle.getStringArray("PM");
		startPM = pm[0];
		endPM = pm[1];

		try {

			Uri.Builder build = new Uri.Builder();
			build.scheme("http");
			build.path("//www.lib.utexas.edu/studyrooms/index.php");

			build.appendQueryParameter("year", ""+date[0]);
			this.year = ""+date[0];
			date[1]++; //add 1 (month was 0 indexed, need 1 indexed)
			String month =""+ ((date[1]>9)?date[1]:"0"+date[1]); //add 0 to month if less than 10
			build.appendQueryParameter("month", month);
			this.month = month;
			String day =""+ ((date[2]>9)?date[2]:"0"+date[2]); //add 0 to day if less than 10
			build.appendQueryParameter("day", day);
			this.day = day;

			startHour = date[3];

			startMinute = date[4];
			//round startMinute to nearest 15 minutes
			//			startMinute = (int) (Math.round(startMinute/15.0))*15;
			//			if (startMinute==60){
			//				startMinute=0;
			//				startHour = (startHour+1)%24;
			////			}
			//			String startPm = "am";
			//			if (startHour>=12)
			//			{
			//				startPm = "pm";
			//				if (startHour>12)
			//					startHour-=12;
			//			}
			//			else if (startHour==0)
			//				startHour = 12;

			build.appendQueryParameter("startHour", ""+startHour);

			build.appendQueryParameter("startMinute", ""+startMinute);
			build.appendQueryParameter("isStartPM", pm[0]);

			endHour = date[5];

			//round endMinute to nearest 15 minutes
			endMinute = date[6];
			//			endMinute = (int) (Math.round(endMinute/15.0))*15;
			//			if(endMinute==60)
			//			{
			//				endMinute = 0;
			//				endHour = (endHour +1)%24;
			//			}
			//
			//			String endPm = "am";
			//			if (endHour>=12)
			//			{
			//				endPm = "pm";
			//				if (endHour>12)
			//					endHour-=12;
			//			}
			//			else if (endHour==0)
			//				endHour = 12;
			build.appendQueryParameter("endHour", ""+endHour);
			build.appendQueryParameter("endMinute", ""+endMinute);
			build.appendQueryParameter("isEndPM", pm[1]);

			if (building[0])
				build.appendQueryParameter("building", "any");
			else
				for(int i=1;i<building.length;i++)
					if (building[i])
					{
						build.appendQueryParameter("building" + (i-1), "on");
					}

			build.appendQueryParameter("numPeople", ""+(number==0?0:(number+1)));

			for(int i=0;i<roomreq.length;i++)
				if (roomreq[i])
				{
					build.appendQueryParameter("option" + (i), "on");
				}

			build.appendQueryParameter("mode", "search");


			Uri uri = build.build();
			return uri.toString();

		} catch (Exception e) {
			TextView tv = new TextView(this);
			tv.setText(e.toString());
			setContentView(tv);
			return null;
		}
	}
}
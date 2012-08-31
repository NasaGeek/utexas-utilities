package com.nasageek.utexasutilities.libraries;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.IntentAction;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class finalizeRoomReservation extends Activity {

	Context context;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitleColor(getResources().getColor(R.color.snow2));

		setContentView(R.layout.finalize_room_reservation);
		context = this;

		// code downloaded from
		// https://github.com/johannilsson/android-actionbar/blob/master/README.md
		ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setHomeAction(new IntentAction(this, new Intent(this,WelcomeScreen.class), R.drawable.home)); // go	// home
		actionBar.setTitle("Finalize Room Reservation");
		actionBar.addAction(new IntentAction(this, new Intent(this, settings.class), R.drawable.gear)); //go to settings
		//----------------------


		Bundle bundle = getIntent().getExtras();

		// get times, date, room id from previous screen

		roomId = bundle.getString("roomID");
		date = String.format("%s-%s-%s", bundle.getString("year"),
				bundle.getString("month"), bundle.getString("day"));
		location = bundle.getString("location");
		room = bundle.getString("room");

		TextView tv = (TextView) findViewById(R.id.locationText);
		tv.setText("Location: " + location);

		tv = (TextView) findViewById(R.id.roomText);
		tv.setText("Room: " + room);

		Spinner spinner = (Spinner) findViewById(R.id.startHourSpinner);
		spinner.setSelection(bundle.getInt("startHour")-1); //0 index again
		spinner = (Spinner) findViewById(R.id.endHourSpinner);
		spinner.setSelection(bundle.getInt("endHour")-1);
		spinner = (Spinner) findViewById(R.id.startMinuteSpinner);
		spinner.setSelection(bundle.getInt("startMinute")/15);

		spinner = (Spinner) findViewById(R.id.endMinuteSpinner);
		spinner.setSelection(bundle.getInt("endMinute")/15);

		spinner = (Spinner) findViewById(R.id.startPMSpinner);
		spinner.setSelection((bundle.getString("startPM").equals("PM"))?0:1);

		spinner = (Spinner) findViewById(R.id.endPMSpinner);
		spinner.setSelection((bundle.getString("endPM").equals("PM"))?0:1);
	}

	String roomId = "";
	String date = "";
	String room;
	String location;

	@SuppressWarnings("unused")
	public void finalizeReservation(View view) {

		String groupName;
		String startHour;
		String startMinute;
		String startPM;
		String endHour;
		String endMinute;
		String endPM;

		EditText et = (EditText) findViewById(R.id.groupNameEnter);
		groupName = et.getText().toString();
		Spinner spinner = (Spinner) findViewById(R.id.startHourSpinner);
		startHour = 1 + spinner.getSelectedItemPosition() + ""; // time
		// is 0
		// indexed
		spinner = (Spinner) findViewById(R.id.endHourSpinner);
		endHour = 1 + spinner.getSelectedItemPosition() + ""; // time is

		spinner = (Spinner) findViewById(R.id.startMinuteSpinner);
		startMinute = 15 * spinner.getSelectedItemPosition() + ""; // time

		spinner = (Spinner) findViewById(R.id.endMinuteSpinner);
		endMinute = 15 * spinner.getSelectedItemPosition() + ""; // time

		spinner = (Spinner) findViewById(R.id.startPMSpinner);
		startPM = (spinner.getSelectedItemPosition() == 0) ? "PM" : "AM";
		spinner = (Spinner) findViewById(R.id.endPMSpinner);
		endPM = (spinner.getSelectedItemPosition() == 0) ? "PM" : "AM";

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs
				.add(new BasicNameValuePair("reservationName", groupName));
		nameValuePairs.add(new BasicNameValuePair("startHour", startHour));
		nameValuePairs.add(new BasicNameValuePair("startMinute", startMinute));
		nameValuePairs.add(new BasicNameValuePair("isStartPM", startPM));
		nameValuePairs.add(new BasicNameValuePair("endHour", endHour));
		nameValuePairs.add(new BasicNameValuePair("endMinute", endMinute));
		nameValuePairs.add(new BasicNameValuePair("isEndPM", endPM));

		nameValuePairs.add(new BasicNameValuePair("date", date));
		nameValuePairs.add(new BasicNameValuePair("roomID", roomId));

		String url = "http://www.lib.utexas.edu/studyrooms/reservations/submit.php";
		HttpPost httppost = new HttpPost(url);
		DefaultHttpClient client = new DefaultHttpClient();
		shared.logIntoUTDirect(this, client);

		String html = "";

		try {
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,
					HTTP.ASCII));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpResponse response;
		try {
			response = client.execute(httppost);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));
			String next = in.readLine();
			while (next != null) {
				// System.out.println(next);
				html += next;
				next = in.readLine();
			}
			createConfirmationDialog(html, roomId, date, groupName, startHour,
					startMinute, startPM, endHour, endMinute, endPM);
		} catch (Exception e) {
			// e.printStackTrace();
			if (shared.LOGGINGLEVEL>0) Log.e("finalizeRoomReservation", "error in finalizeReservation", e);
		}
		if (shared.LOGGINGLEVEL>0) Log.i("finalizeRoomReservation", html);
	}

	@SuppressWarnings("unused")
	private void createConfirmationDialog(String html, String roomId,
			String date, String groupName, String startHour,
			String startMinute, String startPM, String endHour,
			String endMinute, String endPM) {
		boolean successtemp = false;
		String result = parseRoomResults.parseConfirmation(html);
		if (shared.LOGGINGLEVEL>0) Log.i("finalizeRoomReservation",
				"in createConfirmationDialog. result = " + result);
		String reserveResult = result;
		// showDialog(0);

		if (shared.LOGGINGLEVEL>0) Log.i("finalizeRoomReservation", "inside create dialog");
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		if (reserveResult.contains("Error"))
			builder.setTitle("Error");
		else {
			successtemp = true;
			builder.setTitle("Success");
			reserveResult += String
					.format("\nLocation:%s\nRoom:%s\nGroup Name:%s\nDate:%s\nStart Time:%s:%02d %s\nEnd Time:%s:%02d %s\n",
							location, room, groupName, date, startHour,
							Integer.parseInt(startMinute), startPM, endHour,
							Integer.parseInt(endMinute), endPM);
		}

		final boolean success = successtemp; // had temporary value earlier because this
												// needs to final for method
												// function below

		builder.setMessage(reserveResult).setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
						if (success) {
							Intent intent = new Intent(context,
									WelcomeScreen.class);
							startActivity(intent);
						}
					}
				}).show();
		// .setNegativeButton("No", new DialogInterface.OnClickListener() {
		// public void onClick(DialogInterface dialog, int id) {
		// dialog.cancel();
		// }
		// });

		// TODO: different buttons for success/failure (need to show
		// reservation, maybe

	}

}

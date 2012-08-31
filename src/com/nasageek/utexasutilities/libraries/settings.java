package com.nasageek.utexasutilities.libraries;

import java.io.File;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.widget.EditText;
import android.widget.TextView;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.IntentAction;

public class settings extends PreferenceActivity {

	//	public static SharedPreferences loginPreferences;
	//	public static SharedPreferences.Editor editor;
	Context context;
	Handler handler;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settingslayout);
		addPreferencesFromResource(R.xml.settings2);
		setTitleColor(getResources().getColor(R.color.snow2));



		context = this;
		handler = new Handler();
		// code downloaded from
		// https://github.com/johannilsson/android-actionbar/blob/master/README.md

		ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("Settings");
		actionBar.setHomeAction(new IntentAction(this, new Intent(this,
				WelcomeScreen.class), R.drawable.home));
		//		 ----------------------

		Preference myPref = (Preference) findPreference("About");
		myPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				launchAboutDialog();
				return true;
			}
		});

		myPref = (Preference) findPreference("deletedata");
		myPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				launchDeleteDataDialog();
				return true;
			}
		});

		myPref = (Preference) findPreference("uteid");
		myPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				launchUteidDialog();
				return true;
			}
		});

		myPref = (Preference) findPreference("password");
		myPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				launchPasswordDialog();
				return true;
			}
		});

		//		loginPreferences = this.getSharedPreferences("login", MODE_PRIVATE);
		//		editor = loginPreferences.edit();
	}

	public void launchAboutDialog() {
		AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
		final TextView tv = new TextView(this);
		String about = "This app was developed by Nikhil Garg, an ECE and Plan II student at UT Austin, part of the Class of 2015. " +
		"For any questions, concerns, requests, bug reports, plaudits, insults, or snarky comments, please send an email to " +
		"\n\nNikhil_Garg@utexas.edu";
		tv.setText(about);
		tv.setTextColor(getResources().getColor(R.color.snow2));
		alt_bld.setView(tv);
		alt_bld.setCancelable(true).setTitle("About")
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {

			}
		});

		AlertDialog alert = alt_bld.create();
		alert.show();

	}

	public void launchDeleteDataDialog() {
		AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
		final TextView tv = new TextView(this);
		String about = "Are you sure you want to delete all your data? This includes login data and saved books.";
		tv.setText(about);
		tv.setTextColor(getResources().getColor(R.color.snow2));
		alt_bld.setView(tv);
		alt_bld.setCancelable(true).setTitle("Delete Data")
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				//delete login data
				SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(context);
				Editor editor = prefs.edit();
				editor.clear().commit();
				//delete saved books
				String FILENAME = "Saved_Books";
				File file = context.getFileStreamPath(FILENAME);
				if(file.exists()){
					context.deleteFile(FILENAME);
				}

				Intent intent = new Intent(context, settings.class);
				startActivity(intent);
			}
		})
		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//do nothing upon cancel
			}
		});

		AlertDialog alert = alt_bld.create();
		alert.show();

	}


	public void launchUteidDialog() {
		SharedPreferences loginPreferences =  PreferenceManager.getDefaultSharedPreferences(context);
		final SharedPreferences.Editor editor = loginPreferences.edit();
		String savedUsername = loginPreferences.getString("uteid", "");
		AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
		final EditText input = new EditText(this);
		input.setText(savedUsername);
		alt_bld.setView(input);
		alt_bld.setMessage("Please Enter Your UTEID")
		.setCancelable(true)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// storeUsername();
				String username = input.getText().toString();
				editor.putString("uteid", username);
				editor.commit();
				shared.checkLogInCredentials(context, handler, true, "You are not connected to the internet. Cannot check log in credentials at this time.");

			}
		})
		.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// Action for ‘NO’ Button
				dialog.cancel();
			}
		});

		AlertDialog alert = alt_bld.create();
		// Title for AlertDialog
		// alert.setTitle("AlertDialogExample");
		// Icon for AlertDialog
		// alert.setIcon(R.drawable.ic_launcher);
		alert.show();

	}

	public void launchPasswordDialog() {
		SharedPreferences loginPreferences =  PreferenceManager.getDefaultSharedPreferences(context);
		final SharedPreferences.Editor editor = loginPreferences.edit();
		String savedPassword = loginPreferences.getString("password", "");
		AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_VARIATION_PASSWORD);
		input.setText(savedPassword);
		alt_bld.setView(input);
		alt_bld.setMessage("Please Enter Your Password")
		.setCancelable(true)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// storeUsername();
				String password = input.getText().toString();
				editor.putString("password", password);
				editor.commit();
				shared.checkLogInCredentials(context, handler, true, "You are not connected to the internet. Cannot check log in credentials at this time.");
			}
		})
		.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// Action for ‘NO’ Button
				dialog.cancel();
			}
		});

		AlertDialog alert = alt_bld.create();
		// Title for AlertDialog
		// alert.setTitle("AlertDialogExample");
		// Icon for AlertDialog
		// alert.setIcon(R.drawable.ic_launcher);
		alert.show();

	}

}

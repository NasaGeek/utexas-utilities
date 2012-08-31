package com.nasageek.utexasutilities.libraries;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.IntentAction;

public class WelcomeScreen extends Activity {

	Context context;
	Handler handler;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		handler = new Handler();
		setTitleColor(getResources().getColor(R.color.snow2));

		// setContentView(R.layout.main);

		String[] features = { "Search Catalog", "Checked Out Books", "Saved Books", "Reserve Study Room", "Library Hours", "Library Maps" };

		setContentView(R.layout.main2);

		//code downloaded from https://github.com/johannilsson/android-actionbar/blob/master/README.md
		ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("\t\t\tUTLibraries");
		actionBar.setGravity(Gravity.CENTER);
		actionBar.setHomeLogo(R.drawable.home);
		actionBar.addAction(new IntentAction(this, new Intent(this, settings.class), R.drawable.gear)); //go to settings
		//----------------------

		shared.checkLogInCredentials(this, handler, !shared.displayedLogInCheck, "You are not connected to the internet. App functionality will be limited.");

		ListView listview = (ListView) findViewById(R.id.mainPageListView);
		listview.setAdapter(new ArrayAdapter<String>(this,R.layout.main_list_item, features));

		listview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				launchActivity(position);

			}
		});

	}

	public void launchActivity(int position) {

		Intent intent=null;
		switch (position) {
		case 0:	intent = new Intent(this, searchInputScreen.class);	break;
		case 4: intent = new Intent(this, showLibraryTimings2.class);break;
		//		case 2:	intent = new Intent(this, settings.class); break;
		case 3: intent = new Intent(this,reserveStudyRoom.class); break;
		case 1: intent = new Intent(this, renewBooks.class);break;
		case 2: intent = new Intent(this, saveBooks.class);break;
		case 5: intent = new Intent(this, libraryMaps.class); break;

		}
		if(intent!=null)
			startActivity(intent);

	}
}
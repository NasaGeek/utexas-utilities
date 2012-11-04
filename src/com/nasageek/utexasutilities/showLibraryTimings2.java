package com.nasageek.utexasutilities;

import android.app.Activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.nasageek.utexasutilities.R;

public class showLibraryTimings2 extends SherlockActivity {

	Context context;
	LayoutInflater mInflater;
	int searchMetaType;
	WebView webview;
	Dialog dialog;
	private ActionBar actionbar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		actionbar = getSupportActionBar();
		actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionbar.setTitle("Libraries");
		actionbar.setSubtitle("Hours");
/*		if (!shared.connectedToInternet)
		{
			int duration = Toast.LENGTH_LONG;
			Toast toast = Toast.makeText(this, "This feature requires an internet connection. Please try again later.", duration);
			toast.show();
		}*/


		//code downloaded from https://github.com/johannilsson/android-actionbar/blob/master/README.md
	//	ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
	//	actionBar.setTitle("Library Hours");
//		actionBar.setHomeAction(new IntentAction(this,new Intent(this, WelcomeScreen.class) , R.drawable.icon)); //go home (already there)
//		actionBar.addAction(new IntentAction(this, new Intent(this, settings.class), R.drawable.ic_menu_preferences)); //go to settings
		final Spinner spinner = new Spinner(this);
		 //       spinner.setPromptId();
		final ArrayAdapter<CharSequence> adapter = new ArrayAdapter(actionbar.getThemedContext(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.hoursLocations));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
     
       
        
        actionbar.setListNavigationCallbacks(adapter, new OnNavigationListener() 
        {
        	public boolean onNavigationItemSelected(int itemPosition, long itemId) {

        		searchTypeSpinnerSelected(itemPosition);
        		return true;
        	}
        });
		       
		
		

/*		Spinner searchType = (Spinner) findViewById(R.id.locationSpinner);
		searchType.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				searchTypeSpinnerSelected(position, frame);
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}});*/

		try {
			webview = new WebView(context);

			final Activity activity = this;
			webview.setWebViewClient(new WebViewClient() {

				public void onPageFinished(WebView view, String url)
				{
					if(dialog!=null)dialog.cancel();
					dialog = null;
				}
				public void onReceivedError(WebView view, int errorCode,
						String description, String failingUrl) {
					if(dialog!=null)dialog.cancel();
					dialog=null;
					Toast.makeText(
							activity,
							"Page Could Not Load. Please Try Again Later. "
							+ description, Toast.LENGTH_SHORT).show();
				}
			});

			setContentView(webview);
			searchTypeSpinnerSelected(0);


		} catch (Exception e) {
			if (shared.LOGGINGLEVEL>0) Log.e("showLibraryTimings", "exception in onCreate: " + e.toString(),e);
		}
	}

	public void searchTypeSpinnerSelected(int position)
	{

		if (dialog!=null){
			dialog = new ProgressDialog(this,R.style.CustomDialog);
			((ProgressDialog) dialog).setMessage("Loading. Please wait...");
			dialog.show();
		}

		String url = "";
		String[] urlresources = getResources().getStringArray(R.array.hoursLocationsURLs);
		url = urlresources[position];
		webview.loadUrl(url);
	}
}
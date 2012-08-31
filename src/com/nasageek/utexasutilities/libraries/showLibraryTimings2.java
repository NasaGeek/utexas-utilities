package com.nasageek.utexasutilities.libraries;

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
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.IntentAction;

public class showLibraryTimings2 extends Activity {

	Context context;
	LayoutInflater mInflater;
	int searchMetaType;
	WebView webview;
	Dialog dialog;

	/** Called when the activity is first created. */
	@SuppressWarnings("unused")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		setTitleColor(getResources().getColor(R.color.snow2));

		mInflater = LayoutInflater.from(context);
		if (!shared.connectedToInternet)
		{
			int duration = Toast.LENGTH_LONG;
			Toast toast = Toast.makeText(this, "This feature requires an internet connection. Please try again later.", duration);
			toast.show();
		}

		setContentView(R.layout.library_hours);

		final FrameLayout frame = (FrameLayout) findViewById(R.id.frame);
		frame.bringToFront();

		//code downloaded from https://github.com/johannilsson/android-actionbar/blob/master/README.md
		ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("Library Hours");
		actionBar.setHomeAction(new IntentAction(this,new Intent(this, WelcomeScreen.class) , R.drawable.home)); //go home (already there)
		actionBar.addAction(new IntentAction(this, new Intent(this, settings.class), R.drawable.gear)); //go to settings


		Spinner searchType = (Spinner) findViewById(R.id.locationSpinner);
		searchType.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				searchTypeSpinnerSelected(position, frame);
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}});

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

			frame.addView(webview);
			searchTypeSpinnerSelected(0,frame);


		} catch (Exception e) {
			if (shared.LOGGINGLEVEL>0) Log.e("showLibraryTimings", "exception in onCreate: " + e.toString(),e);
		}
	}

	public void searchTypeSpinnerSelected(int position, FrameLayout frame)
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
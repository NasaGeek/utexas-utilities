package com.nasageek.utexasutilities.activities;


import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

//consider showing this as a dialog instead

public class NutritionInfoActivity extends SherlockActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String url = getIntent().getExtras().getString("url");
		String title = getIntent().getExtras().getString("title");
		final ActionBar actionbar = getSupportActionBar();
		actionbar.setTitle(title);
		actionbar.setHomeButtonEnabled(true);
		actionbar.setDisplayHomeAsUpEnabled(true);
		final WebView wv = new WebView(this);
		wv.getSettings().setJavaScriptEnabled(true);
		wv.getSettings().setBuiltInZoomControls(true);
		wv.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url)
			{
				return false;
			}
		});	

		wv.loadUrl(url);
		setContentView(wv);
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	int id = item.getItemId();
    	switch(id)
    	{
	    	case android.R.id.home:
	            // app icon in action bar clicked; go home
	            super.onBackPressed();
	            break;			
    	}
    	return true;
    }
}

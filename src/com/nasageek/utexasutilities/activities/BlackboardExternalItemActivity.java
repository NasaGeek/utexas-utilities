package com.nasageek.utexasutilities.activities;


import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.nasageek.utexasutilities.ConnectionHelper;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.R.id;
import com.nasageek.utexasutilities.R.layout;

public class BlackboardExternalItemActivity extends SherlockActivity {
	
	private ActionBar actionbar;
	private TextView absTitle;
	private TextView absSubtitle;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
			super.onCreate(savedInstanceState);
			
			actionbar = getSupportActionBar();
			actionbar.setDisplayShowCustomEnabled(true);
			actionbar.setDisplayShowTitleEnabled(false);
			actionbar.setHomeButtonEnabled(true);
			actionbar.setDisplayHomeAsUpEnabled(true);
	//		actionbar.setTitle(BlackboardActivity.currentBBCourseName);
			
			TextView titleView = new TextView(this);
			titleView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
			titleView.setLines(1);
			titleView.setTextSize(18);
			titleView.setPadding(0, 0, 7, 0);
			titleView.setSingleLine(true);
			titleView.setTextColor(Color.BLACK);
			titleView.setTypeface(Typeface.DEFAULT);
			titleView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
			
			titleView.setText(getIntent().getStringExtra("itemName"));
			
			actionbar.setCustomView(getLayoutInflater().inflate(R.layout.action_bar_title_subtitle, null));
			absTitle = (TextView) actionbar.getCustomView().findViewById(R.id.abs__action_bar_title);
			absSubtitle = (TextView) actionbar.getCustomView().findViewById(R.id.abs__action_bar_subtitle);
	//		actionbar.setCustomView(titleView);
			
			absTitle.setText(BlackboardActivity.currentBBCourseName);
			absSubtitle.setText(getIntent().getStringExtra("itemName"));
			
			CookieSyncManager.createInstance(getApplicationContext());
			CookieManager man = CookieManager.getInstance();
			man.setCookie("courses.utexas.edu", "s_session_id="+ConnectionHelper.getBBAuthCookie(this, ConnectionHelper.getThreadSafeClient()));
			
			CookieSyncManager.getInstance().sync();
			
			WebView wv = new WebView(this);
			wv.getSettings().setJavaScriptEnabled(true);
			wv.getSettings().setBuiltInZoomControls(true);
			wv.setWebViewClient(new WebViewClient()
			{
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url)
				{
					return false;
				}
			});
			wv.loadUrl(getIntent().getDataString());
			setContentView(wv);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
	    	int id = item.getItemId();
	    	switch(id)
	    	{
		    	case android.R.id.home:
		            // app icon in action bar clicked; go home
		            super.onBackPressed();
		            break;
	    	}
	    	return false;
	}

}

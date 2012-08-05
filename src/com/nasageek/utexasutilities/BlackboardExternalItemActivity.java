package com.nasageek.utexasutilities;


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

public class BlackboardExternalItemActivity extends SherlockActivity {
	
	ActionBar actionbar;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
			super.onCreate(savedInstanceState);
			
			actionbar = getSupportActionBar();
			actionbar.setDisplayShowCustomEnabled(true);
			actionbar.setHomeButtonEnabled(true);
			// actionbar.setDisplayHomeAsUpEnabled(true);
			actionbar.setTitle("");
			
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
			actionbar.setCustomView(titleView);
			
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
		            Intent home = new Intent(this, UTilitiesActivity.class);
		            home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		            startActivity(home);break;
	    	}
	    	return false;
	}

}

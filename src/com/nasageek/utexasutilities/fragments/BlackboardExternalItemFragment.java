package com.nasageek.utexasutilities.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.nasageek.utexasutilities.ConnectionHelper;
import com.nasageek.utexasutilities.R;

public class BlackboardExternalItemFragment extends BlackboardFragment {
	
	private TextView absTitle;
	private TextView absSubtitle;
//	private WebView wv;
	private View absView;
	
	public BlackboardExternalItemFragment() {}
	
	public static BlackboardExternalItemFragment newInstance(String url, String courseID, String courseName, String itemName, boolean fromDashboard)
	{
		BlackboardExternalItemFragment beif = new BlackboardExternalItemFragment();
		
		Bundle args = new Bundle();
		args.putString("url", url);
        args.putString("courseID", courseID);
        args.putString("courseName", courseName);
        args.putString("itemName", itemName);
        args.putBoolean("fromDashboard", fromDashboard);
        beif.setArguments(args);
        
        return beif;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		setHasOptionsMenu(true);			
		CookieSyncManager.createInstance(getSherlockActivity());
		CookieManager man = CookieManager.getInstance();
		man.setCookie("courses.utexas.edu", "s_session_id="+ConnectionHelper.getBBAuthCookie(getSherlockActivity(), ConnectionHelper.getThreadSafeClient()));
		
		CookieSyncManager.getInstance().sync();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		absView = inflater.inflate(R.layout.action_bar_title_subtitle, null);
		setupActionBar();
		
		//TODO: figure out how to save state of the WebView
		final WebView wv = new WebView(getSherlockActivity());
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

		wv.loadUrl(getArguments().getString("url"));
		return wv;
	}
	private void setupActionBar() {
		final ActionBar actionbar = getSherlockActivity().getSupportActionBar();
		actionbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_TITLE);
		actionbar.setCustomView(absView);
		absTitle = (TextView) actionbar.getCustomView().findViewById(R.id.abs__action_bar_title);
		absSubtitle = (TextView) actionbar.getCustomView().findViewById(R.id.abs__action_bar_subtitle);

		absTitle.setText(getArguments().getString("courseName"));
		absSubtitle.setText(getArguments().getString("itemName"));
	}
	@Override
	public String getBbid() {	
		return getArguments().getString("courseID");
	}

	@Override
	public String getCourseName() {
		return getArguments().getString("courseName");
	}
	
	@Override
	public boolean isFromDashboard() {
		return getArguments().getBoolean("fromDashboard");
	}	
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();     
	}

	@Override
	public void onPanesScrolled() {
		setupActionBar();	
	}


}

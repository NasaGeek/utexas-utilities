package com.nasageek.UTilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class LoginWebViewClient extends WebViewClient {

	private Context context;
	
	public LoginWebViewClient(Context con)
	{
		super();
		context =  con;
	}
	
	public boolean shouldOverrideUrlLoading(WebView view, String url)
	{
		return false;
	}

	public void onPageFinished(WebView view, String url)
	{
		super.onPageFinished(view, url);
		String authCookie = "";
		
		String cookies = CookieManager.getInstance().getCookie("https://utexas.edu");
		if(cookies!=null && !cookies.contains("SC=NONE"))
			authCookie = cookies.substring(cookies.indexOf("SC=")+3,cookies.indexOf(";",cookies.indexOf("SC=")+3));
		if(!authCookie.equals("") && !authCookie.equals("NONE") && url.equals("https://utdirect.utexas.edu/security-443/logon_check.logonform"))
		{
			ConnectionHelper.setAuthCookie(authCookie);
			Toast.makeText(context, "You're now logged in; feel free to access any of the app's features", Toast.LENGTH_LONG).show();
			Intent main_intent = new Intent(context, UTilitiesActivity.class);
	    	main_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			context.startActivity(main_intent);
	  //  	view.destroy();
	    	CookieManager.getInstance().removeAllCookie();
	    	return;
		}

	}
}

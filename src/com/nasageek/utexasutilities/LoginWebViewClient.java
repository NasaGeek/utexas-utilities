package com.nasageek.utexasutilities;

import com.crittercism.app.Crittercism;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class LoginWebViewClient extends WebViewClient {

	private Context context;
	private String nextActivity;
	private char service;
	private String serviceString;
	
	public LoginWebViewClient(Context con, String nextActivity, char service)
	{
		super();
		context =  con;
		this.nextActivity = nextActivity;
		this.service = service;
	}
	
	public boolean shouldOverrideUrlLoading(WebView view, String url)
	{
		return false;
	}
	@Override
	public void onPageFinished(WebView view, String url)
	{
		super.onPageFinished(view, url);
		String authCookie = "";
		String cookies = "";
		switch(service)
		{
			case 'z':((LoginActivity)context).finish();break;
			case 'p':
				cookies = CookieManager.getInstance().getCookie("https://pna.utexas.edu");
				if(cookies != null && cookies.contains("AUTHCOOKIE="))
					//always expecting AUTHCOOKIE to be the last cookie
					authCookie = cookies.substring(cookies.indexOf("AUTHCOOKIE=")+11);
				if(!authCookie.equals(""))
				{
					ConnectionHelper.setPNAAuthCookie(authCookie);
					serviceString = "UT PNA";
					continueToActivity();
					return;
				}
				break;
			case 'b':
				cookies = CookieManager.getInstance().getCookie("https://courses.utexas.edu");
				
				if(url.equals("https://courses.utexas.edu/webapps/portal/frameset.jsp"))
					//always expecting s_session_id to be the last cookie
					authCookie = cookies.substring(cookies.indexOf("s_session_id=")+13);
				if(!authCookie.equals(""))
				{
					ConnectionHelper.setBBAuthCookie(authCookie);
					serviceString = "Blackboard";
					continueToActivity();
			    	return;
				}
				break;
			case 'u':
				cookies = CookieManager.getInstance().getCookie("https://utexas.edu");
				if(cookies!=null && !cookies.contains("SC=NONE"))
					//this will fail if SC is the last cookie
					authCookie = cookies.substring(cookies.indexOf("SC=")+3,cookies.indexOf(";",cookies.indexOf("SC=")+3));
				if(!authCookie.equals("") && !authCookie.equals("NONE") && url.equals("https://utdirect.utexas.edu/security-443/logon_check.logonform"))
				{
					ConnectionHelper.setAuthCookie(authCookie);
					serviceString = "UTDirect";
					continueToActivity();
			    	return;
				}
				break;
		}	
	}
	private void continueToActivity()
	{
		Toast.makeText(context, "You're now logged in to "+serviceString, Toast.LENGTH_SHORT).show();
		Crittercism.leaveBreadcrumb("Logged in (temp)");
		Intent intent=null;
		try {
			intent = new Intent(context, Class.forName(nextActivity));
		} catch (ClassNotFoundException e) {
			
			e.printStackTrace();
			intent = new Intent(context, UTilitiesActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		}
		context.startActivity(intent);
    	CookieManager.getInstance().removeAllCookie();

    	return;
	}
}

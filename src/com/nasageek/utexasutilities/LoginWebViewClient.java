package com.nasageek.utexasutilities;

//import com.crittercism.app.Crittercism;
import com.nasageek.utexasutilities.activities.LoginActivity;
import com.nasageek.utexasutilities.activities.UTilitiesActivity;

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
				if(url.contains("pna.utexas.edu"))
				{
					cookies = CookieManager.getInstance().getCookie("https://pna.utexas.edu");
					if(cookies != null && cookies.contains("AUTHCOOKIE="))
						for(String s : cookies.split("; "))
						{
							if(s.startsWith("AUTHCOOKIE="))
							{	authCookie =  s.substring(11);
								break;
							}
						}
					if(!authCookie.equals(""))
					{
						ConnectionHelper.setPNAAuthCookie(authCookie,context);
						continueToActivity("UT PNA");
						return;
					}
				}
				break;
				
			case 'b':
				if(url.contains("courses.utexas.edu"))
				{	
					cookies = CookieManager.getInstance().getCookie("https://courses.utexas.edu");
					
					if(url.equals("https://courses.utexas.edu/webapps/portal/frameset.jsp") && cookies != null)
					{
						for(String s : cookies.split("; "))
						{
							if(s.startsWith("s_session_id="))
							{	authCookie =  s.substring(13);;
								break;
							}
						}
					}
					if(!authCookie.equals(""))
					{
						ConnectionHelper.setBBAuthCookie(authCookie,context);
						continueToActivity("Blackboard");
				    	return;
					}
				}
				break;
			case 'u':
				if(url.contains("utexas.edu"))
				{	
					cookies = CookieManager.getInstance().getCookie("https://utexas.edu");
					if(cookies!=null && !cookies.contains("SC=NONE"))
					{
						for(String s : cookies.split("; "))
						{
							if(s.startsWith("SC="))
							{	authCookie = s.substring(3);
								break;
							}
						}
					}
					if(!authCookie.equals("") && !authCookie.equals("NONE") && url.equals("https://utdirect.utexas.edu/security-443/logon_check.logonform"))
					{
						ConnectionHelper.setAuthCookie(authCookie,context);
						continueToActivity("UTDirect");
				    	return;
					}
				}
				break;
		}	
	}
	private void continueToActivity(String service)
	{
		Intent intent=null;
		try {
			intent = new Intent(context, Class.forName(nextActivity));
			Toast.makeText(context, "You're now logged in to "+service, Toast.LENGTH_SHORT).show();
			//Crittercism.leaveBreadcrumb("Logged in (temp)");
		} catch (ClassNotFoundException e) {
			
			e.printStackTrace();
			intent = new Intent(context, UTilitiesActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			Toast.makeText(context, "Your attempt to log in went terribly wrong", Toast.LENGTH_SHORT).show();
		}
		context.startActivity(intent);
    	CookieManager.getInstance().removeAllCookie();

    	return;
	}
}
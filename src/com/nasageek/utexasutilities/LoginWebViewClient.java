package com.nasageek.utexasutilities;

import com.nasageek.utexasutilities.activities.LoginActivity;
import com.nasageek.utexasutilities.activities.UTilitiesActivity;

import android.app.Activity;
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

	public LoginWebViewClient(Context con, String nextActivity, char service) {
		super();
		context = con;
		this.nextActivity = nextActivity;
		this.service = service;
	}

	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		return false;
	}

	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) {
		if(service == 'c') {
			if(url.contains("https://utexas.instructure.com/login/oauth2/auth?code=")) {
				// ConnectionHelper.setCanvasAuthToken(url.substring(url.indexOf("code=")),
				// context);
				Intent sendData = new Intent();
				sendData.putExtra("oauth2_code", url.substring(url.indexOf("code=") + 5));
				((Activity) context).setResult(Activity.RESULT_OK, sendData);
				//continueToActivity("Canvas");
				((Activity) context).finish();
			} else if(url.contains("https://utexas.instructure.com/login/oauth2/auth?error=")) {
				// only error that is currently returned is access_denied
				((Activity) context).setResult(Activity.RESULT_CANCELED);
				((Activity) context).finish();
				return;
			}
		}
	}

	@Override
	public void onPageFinished(WebView view, String url) {
		super.onPageFinished(view, url);
		String authCookie = "";
		String cookies = "";
		switch(service) {
			case 'z':
				((Activity) context).finish();
				break;
			case 'p':
				if(url.contains("pna.utexas.edu")) {
					cookies = CookieManager.getInstance().getCookie("https://pna.utexas.edu");
					if(cookies != null && cookies.contains("AUTHCOOKIE="))
						for(String s : cookies.split("; ")) {
							if(s.startsWith("AUTHCOOKIE=")) {
								authCookie = s.substring(11);
								break;
							}
						}
					if(!authCookie.equals("")) {
						ConnectionHelper.setPNAAuthCookie(authCookie, context);
						continueToActivity("UT PNA");
						return;
					}
				}
				break;		
			case 'b':
				if(url.contains(ConnectionHelper.blackboard_domain_noprot)) {	
					cookies = CookieManager.getInstance().getCookie(ConnectionHelper.blackboard_domain);
					
					if(url.equals(ConnectionHelper.blackboard_domain + "/webapps/portal/frameset.jsp") && cookies != null) {
						for(String s : cookies.split("; ")) {
							if(s.startsWith("s_session_id=")) {	
								authCookie =  s.substring(13);;
								break;
							}
						}
					}
				}
				if(!authCookie.equals("")) {
					ConnectionHelper.setBBAuthCookie(authCookie, context);
					continueToActivity("Blackboard");
					return;
				}
				break;
			case 'u':
				if(url.contains("utexas.edu")) {
					cookies = CookieManager.getInstance().getCookie("https://utexas.edu");
					if(cookies != null && !cookies.contains("SC=NONE")) {
						for(String s : cookies.split("; ")) {
							if(s.startsWith("SC=")) {
								authCookie = s.substring(3);
								break;
							}
						}
					}
					if(!authCookie.equals("") && !authCookie.equals("NONE") && url.equals("https://utdirect.utexas.edu/security-443/logon_check.logonform")) {
						ConnectionHelper.setAuthCookie(authCookie, context);
						continueToActivity("UTDirect");
						return;
					}
				}
				break;
		}
	}

	private void continueToActivity(String service) {
		Intent intent = null;
		try {
			intent = new Intent(context, Class.forName(nextActivity));
			// added for case of nextActivity being UTilitiesActivity
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			Toast.makeText(context, "You're now logged in to " + service, Toast.LENGTH_SHORT).show();

		} catch(ClassNotFoundException e) {
			e.printStackTrace();
			intent = new Intent(context, UTilitiesActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			Toast.makeText(context, "Your attempt to log in went terribly wrong", Toast.LENGTH_SHORT).show();
		}
		CookieManager.getInstance().removeAllCookie();
		context.startActivity(intent);
		((Activity) context).finish();
		return;
	}
}
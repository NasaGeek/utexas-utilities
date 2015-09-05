
package com.nasageek.utexasutilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.nasageek.utexasutilities.activities.UTilitiesActivity;

import static com.nasageek.utexasutilities.UTilitiesApplication.PNA_AUTH_COOKIE_KEY;
import static com.nasageek.utexasutilities.UTilitiesApplication.UTD_AUTH_COOKIE_KEY;

public class LoginWebViewClient extends WebViewClient {

    private Context context;
    private String nextActivity;
    private char service;

    public LoginWebViewClient(Context con, String nextActivity, char service) {
        super();
        this.context = con;
        this.nextActivity = nextActivity;
        this.service = service;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return false;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        String authCookie = "";
        String cookies = "";
        switch (service) {
            case 'z':
                ((Activity) context).finish();
                break;
            case 'p':
                if (url.contains("pna.utexas.edu")) {
                    cookies = CookieManager.getInstance().getCookie("https://pna.utexas.edu");
                    if (cookies != null && cookies.contains("AUTHCOOKIE=")) {
                        for (String s : cookies.split("; ")) {
                            if (s.startsWith("AUTHCOOKIE=")) {
                                authCookie = s.substring(11);
                                break;
                            }
                        }
                    }
                    if (!authCookie.equals("")) {
                        AuthCookie pnaAuthCookie = ((UTilitiesApplication) ((Activity) context)
                                .getApplication()).getAuthCookie(PNA_AUTH_COOKIE_KEY);
                        pnaAuthCookie.setAuthCookieVal(authCookie);
                        continueToActivity("UT PNA");
                        return;
                    }
                }
                break;

            case 'u':
                if (url.contains("utexas.edu")) {
                    cookies = CookieManager.getInstance().getCookie("https://login.utexas.edu");
                    if (cookies != null) {
                        for (String s : cookies.split("; ")) {
                            if (s.startsWith("utlogin-prod=")) {
                                authCookie = s.substring(13);
                                break;
                            }
                        }
                    }
                    if (!authCookie.equals("")
                            && url.equals("https://www.utexas.edu/")) {
                        AuthCookie utdAuthCookie = ((UTilitiesApplication) ((Activity) context)
                                .getApplication()).getAuthCookie(UTD_AUTH_COOKIE_KEY);
                        utdAuthCookie.setAuthCookieVal(authCookie);
                        continueToActivity("UTLogin");
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
            Toast.makeText(context, "You're now logged in to " + service, Toast.LENGTH_SHORT)
                    .show();
        } catch (ClassNotFoundException e) {

            e.printStackTrace();
            intent = new Intent(context, UTilitiesActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Toast.makeText(context, "Your attempt to log in went terribly wrong",
                    Toast.LENGTH_SHORT).show();
        }
        context.startActivity(intent);
        CookieManager.getInstance().removeAllCookie();
        return;
    }
}

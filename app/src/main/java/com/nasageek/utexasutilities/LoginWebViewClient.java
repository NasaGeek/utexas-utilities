
package com.nasageek.utexasutilities;

import android.app.Activity;
import android.content.Intent;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.nasageek.utexasutilities.activities.UTilitiesActivity;

import static com.nasageek.utexasutilities.UTilitiesApplication.UTD_AUTH_COOKIE_KEY;

public class LoginWebViewClient extends WebViewClient {

    private Activity activity;
    private String nextActivity;

    public LoginWebViewClient(Activity activity, String nextActivity) {
        super();
        this.activity = activity;
        this.nextActivity = nextActivity;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return false;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        String authCookie = "";
        if (url.contains("utexas.edu")) {
            String cookies = CookieManager.getInstance().getCookie("https://login.utexas.edu");
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
                UTilitiesApplication.getInstance().getAuthCookie(UTD_AUTH_COOKIE_KEY).setAuthCookieVal(authCookie);
                continueToActivity("UTLogin");
            }
        }
    }

    private void continueToActivity(String service) {
        Intent intent;
        try {
            intent = new Intent(activity, Class.forName(nextActivity));
            Toast.makeText(activity, "You're now logged in to " + service, Toast.LENGTH_SHORT)
                    .show();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            intent = new Intent(activity, UTilitiesActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Toast.makeText(activity, "Your attempt to log in went terribly wrong",
                    Toast.LENGTH_SHORT).show();
        }
        activity.startActivity(intent);
        CookieManager.getInstance().removeAllCookie();
        activity.finish();
    }
}

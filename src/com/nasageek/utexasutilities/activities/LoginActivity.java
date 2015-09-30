
package com.nasageek.utexasutilities.activities;

import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;

import com.nasageek.utexasutilities.LoginWebViewClient;

public class LoginActivity extends BaseActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        char service = getIntent().getCharExtra("service", 'z');
        CookieSyncManager.createInstance(this);
        WebView wv = new WebView(this);
        CookieManager.getInstance().removeAllCookie();

        LoginWebViewClient wvlc = new LoginWebViewClient(this, getIntent().getStringExtra(
                "activity"), service);

        wv.setWebViewClient(wvlc);

        switch (service) {
            case 'u':
                wv.getSettings().setJavaScriptEnabled(true);
                wv.loadUrl("https://login.utexas.edu/login/UI/Login");
                actionBar.setSubtitle("UTLogin");
                break;
            case 'p':
                wv.loadUrl("https://management.pna.utexas.edu/server/graph.cgi");
                actionBar.setSubtitle("UT PNA");
                break;
        }
        setContentView(wv);
    }
}

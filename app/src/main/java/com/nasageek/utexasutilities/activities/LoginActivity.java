
package com.nasageek.utexasutilities.activities;

import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;

import com.nasageek.utexasutilities.LoginWebViewClient;

public class LoginActivity extends BaseActivity {
    
    private WebView webView;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        webView = new WebView(this);

        LoginWebViewClient wvlc = new LoginWebViewClient(this, getIntent().getStringExtra(
                "activity"));
        webView.setWebViewClient(wvlc);
        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {
            webView.getSettings().setJavaScriptEnabled(true);
            webView.loadUrl("https://login.utexas.edu/login/UI/Login");
            actionBar.setSubtitle("UTLogin");
        }
        setContentView(webView);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    public void onDestroy() {
        webView.destroy();
        super.onDestroy();
    }
}

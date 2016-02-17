
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
        if (!getIntent().hasExtra("service")) {
            finish();
            return;
        }
        char service = getIntent().getCharExtra("service", 'p');
        CookieSyncManager.createInstance(this);
        CookieManager.getInstance().removeAllCookie();

        LoginWebViewClient wvlc = new LoginWebViewClient(this, getIntent().getStringExtra(
                "activity"), service);
        webView.setWebViewClient(wvlc);
        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {
            switch (service) {
                case 'u':
                    webView.getSettings().setJavaScriptEnabled(true);
                    webView.loadUrl("https://login.utexas.edu/login/UI/Login");
                    actionBar.setSubtitle("UTLogin");
                    break;
                case 'p':
                    webView.loadUrl("https://management.pna.utexas.edu/server/graph.cgi");
                    actionBar.setSubtitle("UT PNA");
                    break;
            }
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

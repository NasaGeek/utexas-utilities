
package com.nasageek.utexasutilities.activities;

import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.nasageek.utexasutilities.ConnectionHelper;
import com.nasageek.utexasutilities.LoginWebViewClient;

public class LoginActivity extends SherlockActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        char service = getIntent().getCharExtra("service", 'z');
        ActionBar actionbar = getSupportActionBar();
        actionbar.setTitle("Login");

        actionbar.setHomeButtonEnabled(true);
        actionbar.setDisplayHomeAsUpEnabled(true);
        CookieSyncManager.createInstance(this);
        WebView wv = new WebView(this);
        CookieManager.getInstance().removeAllCookie();

        LoginWebViewClient wvlc = new LoginWebViewClient(this, getIntent().getStringExtra(
                "activity"), service);

        wv.setWebViewClient(wvlc);

        switch (service) {
            case 'u':
                wv.loadUrl("https://utdirect.utexas.edu");
                actionbar.setSubtitle("UTDirect");
                break;
            case 'b':
                wv.loadUrl(ConnectionHelper.blackboard_domain);
                actionbar.setSubtitle("Blackboard");
                break;
            case 'p':
                wv.loadUrl("https://management.pna.utexas.edu/server/graph.cgi");
                actionbar.setSubtitle("UT PNA");
                break;
            case 'c':
                wv.loadUrl("https://utexas.instructure.com/login/oauth2/auth?client_id=10000000000228&response_type=code&redirect_uri=urn:ietf:wg:oauth:2.0:oob&mobile=1");
                wv.getSettings().setJavaScriptEnabled(true);
                actionbar.setSubtitle("Canvas one-time authorization");
                break;
        }
        setContentView(wv);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                super.onBackPressed();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
}

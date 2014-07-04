
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
    /** Called when the activity is first created. */
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
                wv.loadUrl(ConnectionHelper.BLACKBOARD_DOMAIN);
                actionbar.setSubtitle("Blackboard");
                break;
            case 'p':
                wv.loadUrl("https://management.pna.utexas.edu/server/graph.cgi");
                actionbar.setSubtitle("UT PNA");
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

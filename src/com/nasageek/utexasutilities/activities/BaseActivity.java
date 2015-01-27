package com.nasageek.utexasutilities.activities;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.android.gms.analytics.GoogleAnalytics;

public abstract class BaseActivity extends SherlockFragmentActivity {

    @Override
    public void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    public void onStop() {
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
        super.onStop();
    }

}

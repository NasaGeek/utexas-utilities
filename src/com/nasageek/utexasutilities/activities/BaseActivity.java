package com.nasageek.utexasutilities.activities;

import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.analytics.GoogleAnalytics;

public abstract class BaseActivity extends AppCompatActivity {

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

package com.nasageek.utexasutilities.activities;

import android.os.Bundle;

import com.nasageek.utexasutilities.R;

/**
 * Created by chris on 9/3/15.
 */
public class PreferenceActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}

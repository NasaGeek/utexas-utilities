package com.nasageek.utexasutilities.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;

import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.fragments.UTilitiesPreferenceFragment;

/**
 * Created by chris on 9/3/15.
 */
public class PreferenceActivity extends BaseActivity implements
        PreferenceFragmentCompat.OnPreferenceStartScreenCallback {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);
        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.fragment_container, UTilitiesPreferenceFragment.newInstance("root"),
                UTilitiesPreferenceFragment.class.getSimpleName());
            transaction.commit();
        }
        setupActionBar();
    }

    @Override
    public boolean onPreferenceStartScreen(PreferenceFragmentCompat pfc, PreferenceScreen screen) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, UTilitiesPreferenceFragment.newInstance("experimental"),
                UTilitiesPreferenceFragment.class.getSimpleName());
        transaction.addToBackStack(null);
        transaction.commit();
        return true;
    }
}

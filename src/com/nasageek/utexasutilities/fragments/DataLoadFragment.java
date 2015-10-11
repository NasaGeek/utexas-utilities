package com.nasageek.utexasutilities.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by chris on 10/3/15.
 */
public abstract class DataLoadFragment extends Fragment {
    enum LoadStatus {
        NOT_STARTED, LOADING, SUCCEEDED, FAILED
    }
    protected LoadStatus loadStatus = LoadStatus.NOT_STARTED;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            loadStatus = (LoadStatus) savedInstanceState.getSerializable("loadStatus");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("loadStatus", loadStatus);
    }
}

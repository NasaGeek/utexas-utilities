package com.nasageek.utexasutilities.fragments;

import android.support.v4.app.DialogFragment;

public class DataSourceSelectionFragment extends DialogFragment {

    // stubbed class
    public static DataSourceSelectionFragment newInstance(String path, String webUrl) {
        throw new UnsupportedOperationException("Not implemented in release builds");
    }

    public static class DataSourceSelectedEvent {
        public String url;

        public DataSourceSelectedEvent(String url) {
            throw new UnsupportedOperationException("Not implemented in release builds");
        }
    }
}

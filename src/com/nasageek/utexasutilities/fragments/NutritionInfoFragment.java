
package com.nasageek.utexasutilities.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.actionbarsherlock.app.SherlockFragment;

public class NutritionInfoFragment extends SherlockFragment {

    public NutritionInfoFragment() {
    }

    public static NutritionInfoFragment newInstance(String url) {
        NutritionInfoFragment nif = new NutritionInfoFragment();
        Bundle args = new Bundle();
        args.putString("url", url);
        nif.setArguments(args);
        return nif;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: figure out how to save state of the WebView
        final WebView wv = new WebView(getActivity());
        wv.getSettings().setBuiltInZoomControls(true);
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });

        wv.loadUrl(getArguments().getString("url"));
        return wv;
    }
}

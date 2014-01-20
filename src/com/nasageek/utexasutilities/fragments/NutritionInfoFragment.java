
package com.nasageek.utexasutilities.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.nasageek.utexasutilities.R;

public class NutritionInfoFragment extends SherlockFragment {

    private TextView absTitle;
    private TextView absSubtitle;
    // private WebView wv;
    private View absView;

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
        absView = inflater.inflate(R.layout.action_bar_title_subtitle, null);
        // setupActionBar();

        // TODO: figure out how to save state of the WebView
        final WebView wv = new WebView(getSherlockActivity());
        wv.getSettings().setJavaScriptEnabled(true);
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

    private void setupActionBar() {
        final ActionBar actionbar = getSherlockActivity().getSupportActionBar();
        actionbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM
                | ActionBar.DISPLAY_SHOW_TITLE);
        actionbar.setCustomView(absView);
        absTitle = (TextView) actionbar.getCustomView().findViewById(R.id.abs__action_bar_title);
        absSubtitle = (TextView) actionbar.getCustomView().findViewById(
                R.id.abs__action_bar_subtitle);

        absTitle.setText(getArguments().getString("courseName"));
        absSubtitle.setText(getArguments().getString("itemName"));
    }
}

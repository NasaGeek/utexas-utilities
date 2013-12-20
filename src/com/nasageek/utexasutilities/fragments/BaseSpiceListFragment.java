package com.nasageek.utexasutilities.fragments;

import com.actionbarsherlock.app.SherlockListFragment;
import com.octo.android.robospice.SpiceManager;

import com.nasageek.utexasutilities.CanvasRetrofitSpiceService;

public class BaseSpiceListFragment extends SherlockListFragment {

    private SpiceManager spiceManager = new SpiceManager(CanvasRetrofitSpiceService.class);
    
    @Override
    public void onStart() {
    	super.onStart();
    	spiceManager.start(getSherlockActivity());
    }
    
    @Override
    public void onStop() {
    	spiceManager.shouldStop();
    	super.onStop();
    }
    
    protected SpiceManager getSpiceManager() {
    	return spiceManager;
    }
}

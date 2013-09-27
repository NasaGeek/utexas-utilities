package com.nasageek.utexasutilities.fragments;

import com.actionbarsherlock.app.SherlockFragment;
import com.octo.android.robospice.SpiceManager;

import com.nasageek.utexasutilities.CanvasRetrofitSpiceService;

public class BaseSpiceFragment extends SherlockFragment {

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

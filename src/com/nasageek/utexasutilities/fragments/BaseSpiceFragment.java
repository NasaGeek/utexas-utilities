
package com.nasageek.utexasutilities.fragments;

import com.actionbarsherlock.app.SherlockFragment;
import com.nasageek.utexasutilities.CanvasRetrofitSpiceService;
import com.nasageek.utexasutilities.MySpiceManager;
import com.octo.android.robospice.SpiceManager;

public class BaseSpiceFragment extends SherlockFragment {

    private MySpiceManager spiceManager = new MySpiceManager(CanvasRetrofitSpiceService.class);

    @Override
    public void onStart() {
        super.onStart();
        spiceManager.start(getActivity());
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

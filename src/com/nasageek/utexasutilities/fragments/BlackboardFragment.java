
package com.nasageek.utexasutilities.fragments;

import com.nasageek.utexasutilities.activities.BlackboardPanesActivity.OnPanesScrolledListener;

public abstract class BlackboardFragment extends BaseSpiceFragment implements
        OnPanesScrolledListener {

    abstract String getBbid();

    abstract String getCourseName();

    abstract boolean isFromDashboard();

    /*
     * @Override public void onPanesScrolled() { //woops! turns out I don't need
     * this. We'll hang onto it for the time being just in case I change my
     * mind. // if(isAdded()) //
     * getSherlockActivity().supportInvalidateOptionsMenu(); }
     */

}

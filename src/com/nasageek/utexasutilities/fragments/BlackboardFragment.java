
package com.nasageek.utexasutilities.fragments;

import com.actionbarsherlock.app.SherlockFragment;
import com.nasageek.utexasutilities.activities.BlackboardPanesActivity.OnPanesScrolledListener;

public abstract class BlackboardFragment extends SherlockFragment implements
        OnPanesScrolledListener {

    public static final String BLACKBOARD_DOMAIN_NOPROT = "blackboard.utexas.edu";

    public static final String BLACKBOARD_DOMAIN = "https://" + BLACKBOARD_DOMAIN_NOPROT;

    abstract String getBbid();

    abstract String getCourseName();

    abstract boolean isFromDashboard();

    /*
     * @Override public void onPanesScrolled() { //woops! turns out I don't need
     * this. We'll hang onto it for the time being just in case I change my
     * mind. // if(isAdded()) //
     * getActivity().supportInvalidateOptionsMenu(); }
     */

}

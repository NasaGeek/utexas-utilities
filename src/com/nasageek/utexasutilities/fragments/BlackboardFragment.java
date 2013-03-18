package com.nasageek.utexasutilities.fragments;

import com.actionbarsherlock.app.SherlockFragment;
import com.nasageek.utexasutilities.activities.BlackboardPanesActivity.OnFragmentMenuChangedListener;

public abstract class BlackboardFragment extends SherlockFragment implements OnFragmentMenuChangedListener {

	abstract String getBbid();
	
	abstract String getCourseName();
	
	abstract boolean isFromDashboard();

	
	@Override
	public void onFragmentMenuChanged() {
		//woops! turns out I don't need this.  We'll hang onto it for the time being just in case I change my mind.
//		if(isAdded())
//			getSherlockActivity().supportInvalidateOptionsMenu();
	}
	
}

package com.nasageek.utexasutilities.adapters;

import java.util.List;

import com.actionbarsherlock.app.SherlockFragment;

import android.support.v4.app.FragmentManager;

public class MultiPanePagerAdapter extends MyFragmentPagerAdapter {

	/**
	 * number of pages we want to display at once
	 */
	private int mPanesDisplayed;
	
	public MultiPanePagerAdapter(FragmentManager fm, List<SherlockFragment> fragments) {
		super(fm, fragments);
		mPanesDisplayed = 1;	
	}
	public void setPagesDisplayed(int panes) {
		mPanesDisplayed = panes;
	}
	@Override
	public float getPageWidth(int position) {
		return (float) (1.0/mPanesDisplayed);
	}
}

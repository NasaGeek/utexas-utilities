package com.nasageek.utexasutilities.adapters;

import java.util.List;

import com.actionbarsherlock.app.SherlockFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;

public class MultiPanePagerAdapter extends PagerAdapter {

	public MultiPanePagerAdapter(FragmentManager fm, List<SherlockFragment> fragments) {
		super(fm, fragments);
		
	}
	@Override
	public float getPageWidth(int position) {
		return .5f;
	}
	
	


}

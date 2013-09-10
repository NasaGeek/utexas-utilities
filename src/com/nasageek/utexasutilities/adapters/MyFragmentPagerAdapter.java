
package com.nasageek.utexasutilities.adapters;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * The <code>PagerAdapter</code> serves the fragments when paging.
 * @author mwho
 */
public class MyFragmentPagerAdapter extends FragmentPagerAdapter {

	int pos;
	private List<SherlockFragment> fragments;
	/**
	 * @param fm
	 * @param fragments
	 */
	public MyFragmentPagerAdapter(FragmentManager fm, List<SherlockFragment> fragments) {
		super(fm);
		this.fragments = fragments;
	}
	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentPagerAdapter#getItem(int)
	 */
	@Override
	public Fragment getItem(int position) {
		return this.fragments.get(position);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.view.PagerAdapter#getCount()
	 */
	@Override
	public int getCount() {
		return this.fragments.size();
	}
	@Override
	public String getPageTitle(int position) {
		
		return this.fragments.get(position).getArguments().getString("title");
	}
	
	public void updateFragments(List<SherlockFragment> fragments)
	{
		this.fragments=fragments;
	}
}

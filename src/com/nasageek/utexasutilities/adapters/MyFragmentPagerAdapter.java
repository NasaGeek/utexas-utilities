
package com.nasageek.utexasutilities.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * The <code>PagerAdapter</code> serves the fragments when paging.
 * 
 * @author mwho
 */
public class MyFragmentPagerAdapter extends FragmentPagerAdapter {

    int pos;
    private List<Fragment> fragments;

    /**
     * @param fm
     * @param fragments
     */
    public MyFragmentPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.FragmentPagerAdapter#getItem(int)
     */
    @Override
    public Fragment getItem(int position) {
        return this.fragments.get(position);
    }

    /*
     * (non-Javadoc)
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

    public void updateFragments(List<Fragment> fragments) {
        this.fragments = fragments;
    }
}

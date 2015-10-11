
package com.nasageek.utexasutilities.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.commonsware.cwac.pager.PageDescriptor;
import com.commonsware.cwac.pager.v4.ArrayPagerAdapter;

import java.util.List;

public abstract class MultiPanePagerAdapter<E extends Fragment> extends ArrayPagerAdapter<E> {

    /**
     * number of pages we want to display at once
     */
    private int mPanesDisplayed;

    public MultiPanePagerAdapter(FragmentManager fm, List<PageDescriptor> pages) {
        this(fm, pages, null);
    }

    public MultiPanePagerAdapter(FragmentManager fm, List<PageDescriptor> pages,
                                 RetentionStrategy strategy) {
        super(fm, pages, strategy);
        mPanesDisplayed = 1;
    }

    public void setPagesDisplayed(int panes) {
        mPanesDisplayed = panes;
    }

    @Override
    public float getPageWidth(int position) {
        return (float) (1.0 / mPanesDisplayed);
    }
}

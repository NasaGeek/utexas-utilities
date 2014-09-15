
package com.nasageek.utexasutilities.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.Utility;
import com.nasageek.utexasutilities.activities.BlackboardPanesActivity.OnPanesScrolledListener;
import com.nasageek.utexasutilities.adapters.MultiPanePagerAdapter;
import com.viewpagerindicator.TabPageIndicator;

import java.util.List;
import java.util.Vector;

public class BlackboardPagerFragment extends SherlockFragment implements OnPanesScrolledListener {

    private MultiPanePagerAdapter mPagerAdapter;
    private ViewPager pager;
    private TabPageIndicator tabIndicator;

    public BlackboardPagerFragment() {
    }

    public static BlackboardPagerFragment newInstance() {
        return new BlackboardPagerFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vg = inflater.inflate(R.layout.blackboard_layout, container, false);

        setHasOptionsMenu(true);
        pager = (ViewPager) vg.findViewById(R.id.viewpager);
        tabIndicator = (TabPageIndicator) vg.findViewById(R.id.titles);
        initialisePaging();
        return vg;
    }

    private void initialisePaging() {

        List<SherlockFragment> fragments = new Vector<SherlockFragment>();

        if (getChildFragmentManager().findFragmentByTag(Utility.makeFragmentName(pager.getId(), 0)) != null) {
            fragments.add((SherlockFragment) getChildFragmentManager().findFragmentByTag(
                    Utility.makeFragmentName(pager.getId(), 0)));
            fragments.add((SherlockFragment) getChildFragmentManager().findFragmentByTag(
                    Utility.makeFragmentName(pager.getId(), 1)));
        } else {
            fragments.add(BlackboardCourseListFragment.newInstance("Course List"));
            fragments.add(BlackboardDashboardFragment.newInstance("Dashboard"));
        }

        mPagerAdapter = new MultiPanePagerAdapter(getChildFragmentManager(), fragments);
        mPagerAdapter.setPagesDisplayed(1);
        pager.setPageMargin(1);

        pager.setAdapter(mPagerAdapter);
        tabIndicator.setViewPager(pager);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        pager.setCurrentItem(Integer.parseInt(sp.getString("default_blackboard_tab", "0")));

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
    }

    @Override
    public void onPanesScrolled() {
        final ActionBar actionbar = getSherlockActivity().getSupportActionBar();
        actionbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE, ActionBar.DISPLAY_SHOW_TITLE
                | ActionBar.DISPLAY_SHOW_CUSTOM);
        actionbar.setTitle("Courses");
        actionbar.setSubtitle(null);
    }

    @Override
    public int getPaneWidth() {
        return R.integer.blackboard_pager_width_percentage;
    }
}

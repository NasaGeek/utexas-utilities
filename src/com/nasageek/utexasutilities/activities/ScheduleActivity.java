
package com.nasageek.utexasutilities.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;

import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.adapters.MyFragmentPagerAdapter;
import com.nasageek.utexasutilities.fragments.ActionModeFragment;
import com.nasageek.utexasutilities.fragments.CourseScheduleFragment;
import com.nasageek.utexasutilities.fragments.ExamScheduleFragment;

import java.util.List;
import java.util.Vector;

public class ScheduleActivity extends BaseActivity implements
        ViewPager.OnPageChangeListener {

    protected MyFragmentPagerAdapter mPagerAdapter;
    protected List<Fragment> fragments;
    protected PagerTabStrip titleIndicator;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_layout);
        initialisePaging();
        setupActionBar();
        actionBar.setElevation(0);
    }

    private void initialisePaging() {
        fragments = new Vector<>();
        fragments.add(ExamScheduleFragment.newInstance("Exam Schedule", ""));
        fragments.add(CourseScheduleFragment.newInstance(true, "Current Schedule", ""));

        this.mPagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), fragments);
        ViewPager pager = (ViewPager) findViewById(R.id.viewpager);
        pager.setPageMargin(2);
        pager.setOffscreenPageLimit(2);
        pager.setAdapter(this.mPagerAdapter);
        pager.addOnPageChangeListener(this);

//        titleIndicator.setOnPageChangeListener(this);
//        ((PagerTabStrip) findViewById(R.id.titles)).no
        titleIndicator = (PagerTabStrip) findViewById(R.id.tabs);
        ViewCompat.setElevation(titleIndicator, getResources().getDimensionPixelSize(R.dimen.actionbar_elevation));
        pager.setCurrentItem(1, false);
    }

    public List<Fragment> getFragments() {
        return fragments;
    }

    public MyFragmentPagerAdapter getAdapter() {
        return mPagerAdapter;
    }

    public PagerTabStrip getIndicator() {
        return titleIndicator;
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {

    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {

    }

    @Override
    public void onPageSelected(int location) {
        for (Fragment csf : fragments) {
            if (((ActionModeFragment) csf).getActionMode() != null) {
                ((ActionModeFragment) csf).getActionMode().finish();
            }
        }
    }

}

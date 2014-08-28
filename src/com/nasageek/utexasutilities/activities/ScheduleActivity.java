
package com.nasageek.utexasutilities.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.widget.Spinner;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.adapters.MyFragmentPagerAdapter;
import com.nasageek.utexasutilities.fragments.ActionModeFragment;
import com.nasageek.utexasutilities.fragments.CourseScheduleFragment;
import com.nasageek.utexasutilities.fragments.ExamScheduleFragment;
import com.viewpagerindicator.TitlePageIndicator;

import java.util.Calendar;
import java.util.List;
import java.util.Vector;

public class ScheduleActivity extends SherlockFragmentActivity implements
        ViewPager.OnPageChangeListener {

    protected MyFragmentPagerAdapter mPagerAdapter;
    protected List<SherlockFragment> fragments;
    protected TitlePageIndicator titleIndicator;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_layout);
        initialisePaging();

        ActionBar actionbar = getSupportActionBar();
        actionbar.setTitle("Schedule");
        actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionbar.setHomeButtonEnabled(true);
        actionbar.setDisplayHomeAsUpEnabled(true);
    }

    private void initialisePaging() {
        fragments = new Vector<SherlockFragment>();
        fragments.add(ExamScheduleFragment.newInstance("Exam Schedule", ""));
        fragments.add(CourseScheduleFragment.newInstance(true, "Current Schedule", ""));

        this.mPagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), fragments);
        ViewPager pager = (ViewPager) findViewById(R.id.viewpager);
        pager.setPageMargin(2);
        pager.setOffscreenPageLimit(2);
        pager.setAdapter(this.mPagerAdapter);

        titleIndicator = (TitlePageIndicator) findViewById(R.id.titles);
        titleIndicator.setViewPager(pager);
        titleIndicator.setOnPageChangeListener(this);

        pager.setCurrentItem(1, false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                super.onBackPressed();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public List<SherlockFragment> getFragments() {
        return fragments;
    }

    public MyFragmentPagerAdapter getAdapter() {
        return mPagerAdapter;
    }

    public TitlePageIndicator getIndicator() {
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
        for (SherlockFragment csf : fragments) {
            if (((ActionModeFragment) csf).getActionMode() != null) {
                ((ActionModeFragment) csf).getActionMode().finish();
            }
        }
    }

}

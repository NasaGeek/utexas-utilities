
package com.nasageek.utexasutilities.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import com.commonsware.cwac.pager.PageDescriptor;
import com.commonsware.cwac.pager.v4.ArrayPagerAdapter;
import com.nasageek.utexasutilities.BuildConfig;
import com.nasageek.utexasutilities.MyBus;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.fragments.CourseScheduleFragment;
import com.nasageek.utexasutilities.fragments.DataSourceSelectionFragment;
import com.nasageek.utexasutilities.fragments.ExamScheduleFragment;
import com.nasageek.utexasutilities.fragments.ScheduleFragment;
import com.nasageek.utexasutilities.model.SimplePageDescriptor;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

public class ScheduleActivity extends BaseActivity implements
        ViewPager.OnPageChangeListener, ArrayPagerAdapter.RetentionStrategy {

    private SchedulePagerAdapter mPagerAdapter;
    private ViewPager pager;
    private static final String DEFAULT_COURSE_SCHEDULE_URL =
            "https://utdirect.utexas.edu/registration/classlist.WBX";
    private static final String COURSE_SCHEDULE_URL_WITH_SEMID =
            DEFAULT_COURSE_SCHEDULE_URL + "?sem=%s";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_layout);
        pager = (ViewPager) findViewById(R.id.viewpager);
        initialisePaging();
        setupActionBar();
        actionBar.setElevation(0);
    }

    @Override
    public void onStart() {
        super.onStart();
        MyBus.getInstance().register(this);
    }

    @Override
    public void onStop() {
        MyBus.getInstance().unregister(this);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (BuildConfig.DEBUG) {
            getMenuInflater().inflate(R.menu.data_sources, menu);
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.data_sources) {
            DataSourceSelectionFragment
                    .newInstance("test_html/schedule", DEFAULT_COURSE_SCHEDULE_URL)
                    .show(getSupportFragmentManager(),
                            DataSourceSelectionFragment.class.getSimpleName());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void onDataSourceSelected(DataSourceSelectionFragment.DataSourceSelectedEvent event) {
        while (mPagerAdapter.getCount() > 1) {
            // remove all CourseScheduleFragments
            mPagerAdapter.remove(mPagerAdapter.getCount() - 1);
        }
        mPagerAdapter.add(new CourseSchedulePageDescriptor(true, "Current Schedule", event.url));
        pager.setCurrentItem(1, false);
    }

    private void initialisePaging() {
        List<PageDescriptor> pages = new ArrayList<>();
        pages.add(new SimplePageDescriptor("Exam Schedule", "Exam Schedule"));
        pages.add(new CourseSchedulePageDescriptor(true, "Current Schedule",
                DEFAULT_COURSE_SCHEDULE_URL));
        mPagerAdapter = new SchedulePagerAdapter(getSupportFragmentManager(), pages, this);
        pager.setPageMargin(2);
        pager.setOffscreenPageLimit(2);
        pager.setAdapter(mPagerAdapter);
        pager.addOnPageChangeListener(this);
        pager.setCurrentItem(1, false);

//        titleIndicator.setOnPageChangeListener(this);
//        ((PagerTabStrip) findViewById(R.id.titles)).no
        PagerTabStrip titleIndicator = (PagerTabStrip) findViewById(R.id.tabs);
        ViewCompat.setElevation(titleIndicator, getResources().getDimensionPixelSize(R.dimen.actionbar_elevation));
    }

    public void addCourseSchedule(boolean currentSchedule, String title, String semId) {
        mPagerAdapter.add(new CourseSchedulePageDescriptor(currentSchedule, title,
               String.format(COURSE_SCHEDULE_URL_WITH_SEMID, semId)));
    }

    @Override
    public void onPageScrollStateChanged(int arg0) { }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) { }

    @Override
    public void onPageSelected(int location) {
        for (int i = 0; i < mPagerAdapter.getCount(); i++) {
            if (mPagerAdapter.getExistingFragment(i) != null) {
                ActionMode mode = mPagerAdapter.getExistingFragment(i).getActionMode();
                if (mode != null) {
                    mode.finish();
                }
            }
        }
    }

    @Override
    public void attach(Fragment fragment, FragmentTransaction fragmentTransaction) {
        fragmentTransaction.attach(fragment);
    }

    @Override
    public void detach(Fragment fragment, FragmentTransaction fragmentTransaction) {
        fragmentTransaction.remove(fragment);
    }

    public static class SchedulePagerAdapter extends ArrayPagerAdapter<ScheduleFragment> {

        public SchedulePagerAdapter(FragmentManager fragmentManager,List<PageDescriptor> pages,
                                    RetentionStrategy strategy) {
            super(fragmentManager, pages, strategy);
        }

        @Override
        protected ScheduleFragment createFragment(PageDescriptor pageDescriptor) {
            if (pageDescriptor instanceof CourseSchedulePageDescriptor) {
                CourseSchedulePageDescriptor coursePage =
                        (CourseSchedulePageDescriptor) pageDescriptor;
                return CourseScheduleFragment.newInstance(coursePage.isCurrentSemester(),
                        coursePage.getUrl());
            } else {
                return ExamScheduleFragment.newInstance(pageDescriptor.getTitle());
            }
        }
    }

    public static class CourseSchedulePageDescriptor extends SimplePageDescriptor {

        private boolean currentSemester;
        private String url;

        public CourseSchedulePageDescriptor(boolean currentSemester, String title, String url) {
            super(title, title);
            this.currentSemester = currentSemester;
            this.url = url;
        }

        public boolean isCurrentSemester() {
            return currentSemester;
        }

        public String getUrl() {
            return url;
        }
    }
}

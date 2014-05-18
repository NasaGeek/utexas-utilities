
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

    private ActionBar actionbar;
    protected MyFragmentPagerAdapter mPagerAdapter;
    private ViewPager pager;
    protected List<SherlockFragment> fragments;
    protected TitlePageIndicator titleIndicator;
    String semId = "";
    int selection;
    Spinner spinner;

    public enum Semester {
        No_Overlay(Calendar.getInstance().get(Calendar.YEAR) + "2", "Spring"), JesterCityLimits(
                Calendar.getInstance().get(Calendar.YEAR) + "6", "Summer"), JesterCityMarket(
                Calendar.getInstance().get(Calendar.YEAR) + "9", "Fall");

        private String code;
        private String fullName;

        private Semester(String c, String fullName) {
            code = c;
            this.fullName = fullName;
        }

        public String getCode() {
            return code;
        }

        public String fullName() {
            return fullName;
        }

        @Override
        public String toString() {
            return fullName;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_layout);

        /*
         * Calendar cal = Calendar.getInstance(); int month =
         * cal.get(Calendar.MONTH); switch (month) { case 0: case 1: case 2:
         * case 3: case 4:semId=cal.get(Calendar.YEAR)+"2";selection=0;break;
         * case 5: case 6:semId=cal.get(Calendar.YEAR)+"6";selection=1;break;
         * case 7: case 8: case 9: case 10: case
         * 11:semId=cal.get(Calendar.YEAR)+"9";selection=2;break; }
         */

        initialisePaging();

        actionbar = getSupportActionBar();
        actionbar.setTitle("Schedule");
        actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionbar.setHomeButtonEnabled(true);
        actionbar.setDisplayHomeAsUpEnabled(true);

        /*
         * spinner = new Spinner(this);
         * spinner.setPromptId(R.string.semesterprompt);
         * spinner.setSelection(selection); final ArrayAdapter<String> adapter =
         * new ArrayAdapter<String>(actionbar.getThemedContext(),
         * android.R.layout.simple_spinner_item);
         * adapter.setDropDownViewResource
         * (android.R.layout.simple_spinner_dropdown_item);
         * spinner.setAdapter(adapter);
         * getSupportActionBar().setListNavigationCallbacks(adapter, new
         * OnNavigationListener() { public boolean onNavigationItemSelected(int
         * itemPosition, long itemId) { String semStr = ((String)
         * spinner.getAdapter().getItem(itemPosition)); String semId = "20"
         * +semStr.substring(semStr.length()-2); if(semStr.contains("Summer")) {
         * semId+="6"; } else if(semStr.contains("Spring")) { semId+="2"; } else
         * if(semStr.contains("Fall")) { semId+="9"; }
         * ((CourseScheduleFragment)(
         * fragments.get(0))).updateView(semId,fragments.get(0).getView());
         * ((ExamScheduleFragment
         * )(fragments.get(1))).updateView(semId,fragments.get(1).getView());
         * return true; } });
         */

    }

    private void initialisePaging() {
        fragments = new Vector<SherlockFragment>();

        Bundle args = new Bundle(2);
        args.putString("title", "Exam Schedule");
        args.putString("semId", semId);
        fragments.add((SherlockFragment) Fragment.instantiate(this,
                ExamScheduleFragment.class.getName(), args));

        args = new Bundle(2);
        args.putString("title", "Current Schedule");
        args.putString("semId", semId);
        fragments.add((SherlockFragment) Fragment.instantiate(this,
                CourseScheduleFragment.class.getName(), args));

        this.mPagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), fragments);
        pager = (ViewPager) findViewById(R.id.viewpager);
        pager.setPageMargin(2);
        pager.setOffscreenPageLimit(2);
        pager.setAdapter(this.mPagerAdapter);

        titleIndicator = (TitlePageIndicator) findViewById(R.id.titles);
        titleIndicator.setViewPager(pager);
        titleIndicator.setOnPageChangeListener(this);

        pager.setCurrentItem(1, false);

    }

    @Override
    public void onPause() {
        super.onPause();
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

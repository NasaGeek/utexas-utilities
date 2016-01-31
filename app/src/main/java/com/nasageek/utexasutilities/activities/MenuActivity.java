
package com.nasageek.utexasutilities.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.commonsware.cwac.pager.PageDescriptor;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.ThemedArrayAdapter;
import com.nasageek.utexasutilities.adapters.MultiPanePagerAdapter;
import com.nasageek.utexasutilities.fragments.MenuFragment;
import com.nasageek.utexasutilities.model.SimplePageDescriptor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MenuActivity extends BaseActivity implements ActionBar.OnNavigationListener {

    //@formatter:off
    public enum Restaurant {
        No_Overlay("0", "No Restaurant"), 
        JesterCityLimits("01", "Jester City Limits", new String[][] {
                {
                    "9am - 11pm"
                }, {
                    "7am - 11pm"
                }, {
                    "7am - 11pm"
                }, {
                    "7am - 11pm"
                }, {
                    "7am - 11pm"
                }, {
                    "7am - 11pm"
                }, {
                    "7am - 9pm"
                }, {
                    "9am - 8pm"
                }
            }, true), 
        JesterCityMarket("05", "Jester City Market", new String[][] {
                {
                    "2pm - 11pm"
                }, {
                    "7am - 12am"
                }, {
                    "7am - 12am"
                }, {
                    "7am - 12am"
                }, {
                    "7am - 12am"
                }, {
                    "7am - 12am"
                }, {
                    "7am - 9pm"
                }, {
                    "2pm - 8pm"
                }
            }, true), 
        J2("12", "Jester 2nd Floor Dining", new String[][] {
                {
                        "", "", ""
                }, {
                        "", "11am - 2pm", "5pm - 8pm"
                }, {
                        "", "11am - 2pm", "5pm - 8pm"
                }, {
                        "", "11am - 2pm", "5pm - 8pm"
                }, {
                        "", "11am - 2pm", "5pm - 8pm"
                }, {
                        "", "11am - 2pm", "5pm - 8pm"
                }, {
                        "", "11am - 2pm", "5pm - 8pm"
                }, {
                        "", "", ""
                }
            }, false), 
        Kinsolving("03", "Kinsolving Dining Hall", new String[][] {
                {
                        "", "11am - 2pm", ""
                }, {
                        "7am - 9:30am", "10:30am - 2pm", "4:30pm - 7pm"
                }, {
                        "7am - 9:30am", "10:30am - 2pm", "4:30pm - 7pm"
                }, {
                        "7am - 9:30am", "10:30am - 2pm", "4:30pm - 7pm"
                }, {
                        "7am - 9:30am", "10:30am - 2pm", "4:30pm - 7pm"
                }, {
                        "7am - 9:30am", "10:30am - 2pm", "4:30pm - 7pm"
                }, {
                        "7am - 9:30am", "10:30am - 2pm", "4:30pm - 7pm"
                }, {
                        "", "11am - 2pm", "4:30pm - 7pm"
                }
            }, false), 
        KinsMarket("14", "Kin's Market", new String[][] {
                {
                    "4pm - 11pm"
                }, {
                    "7am - 11pm"
                }, {
                    "7am - 11pm"
                }, {
                    "7am - 11pm"
                }, {
                    "7am - 11pm"
                }, {
                    "7am - 11pm"
                }, {
                    "7am - 3pm"
                }, {
                    "3pm - 7pm"
                }
            }, true), 
        CypressBend("08", "Cypress Bend", new String[][] {
                {
                    "12pm - 7pm"
                }, {
                    "7am - 9pm"
                }, {
                    "7am - 9pm"
                }, {
                    "7am - 9pm"
                }, {
                    "7am - 9pm"
                }, {
                    "7am - 9pm"
                }, {
                    "7am - 2pm"
                }, {
                    "12pm - 7pm"
                }
            }, true), 
        Littlefield("19", "Littlefield Patio Cafe", new String[][] {
                {
                    "2pm - 8pm"
                }, {
                    "7am - 8pm"
                }, {
                    "7am - 8pm"
                }, {
                    "7am - 8pm"
                }, {
                    "7am - 8pm"
                }, {
                    "7am - 8pm"
                }, {
                    "7am - 4pm"
                }, {
                    ""
                }
            }, true), 
        JestAPizza("26", "Jest A' Pizza", new String[][] {
                {
                    "5pm - 12am"
                }, {
                    "11am - 12am"
                }, {
                    "11am - 12am"
                }, {
                    "11am - 12am"
                }, {
                    "11am - 12am"
                }, {
                    "11am - 12am"
                }, {
                    "11am - 2pm"
                }, {
                    ""
                }
            }, true);
        //@formatter:on

        private String code;
        private String fullName;
        private String[][] times;
        private boolean allDay;

        private Restaurant(String c, String fullName) {
            code = c;
            this.fullName = fullName;
        }

        private Restaurant(String c, String fullName, String[][] times, boolean allDay) {
            code = c;
            this.fullName = fullName;
            this.times = times;
            this.allDay = allDay;
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

        public String[] getTimes() {
            return times[Calendar.getInstance().get(Calendar.DAY_OF_WEEK)];
        }
    }

    private MenuPagerAdapter mPagerAdapter;
    private ArrayAdapter<Restaurant> navigationAdapter;
    private int previousItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_layout);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        setupActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setElevation(0);
        navigationAdapter = new ThemedArrayAdapter<>(
                actionBar.getThemedContext(), android.R.layout.simple_spinner_item,
                Restaurant.values());
        navigationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        if (savedInstanceState != null) {
            previousItem = savedInstanceState.getInt("spinner_selection");
        } else {
            previousItem = 0;
        }

        initialisePaging(navigationAdapter.getItem(previousItem).code + "");
        actionBar.setListNavigationCallbacks(navigationAdapter, this);
        if (savedInstanceState == null) {
            actionBar.setSelectedNavigationItem(Integer.parseInt(settings.getString(
                    "default_restaurant", "0")));
        } else {
            actionBar.setSelectedNavigationItem(previousItem);
        }
    }

    private void initialisePaging(String restId) {
        ViewPager pager = (ViewPager) findViewById(R.id.viewpager);
        int pagesDisplayed = getResources().getInteger(R.integer.menu_num_visible_pages);
        List<PageDescriptor> pages = new ArrayList<>();
        pages.add(new MenuPageDescriptor("Breakfast", restId));
        pages.add(new MenuPageDescriptor("Lunch", restId));
        pages.add(new MenuPageDescriptor("Dinner", restId));
        mPagerAdapter = new MenuPagerAdapter(getSupportFragmentManager(), pages);
        mPagerAdapter.setPagesDisplayed(pagesDisplayed);

        pager.setAdapter(mPagerAdapter);
        // TODO: reimplement selectAll hack with design support library
//        if (pagesDisplayed > 1) {
//            tabIndicator.setSelectAll(true);
//        }
        ((TabLayout) findViewById(R.id.tabs)).setupWithViewPager(pager);
        float elevationPx = getResources().getDimensionPixelSize(R.dimen.actionbar_elevation);
        ViewCompat.setElevation(findViewById(R.id.tabs), elevationPx);
        ViewCompat.setElevation(findViewById(R.id.open_times), elevationPx);

        pager.setOffscreenPageLimit(2);
        pager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.pager_margin));
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        Restaurant r = navigationAdapter.getItem(itemPosition);
        String restId = r.getCode();
        if (!"0".equals(restId)) {
            String[] times = r.getTimes();
            if (!r.allDay) {
                ((TextView) findViewById(R.id.breakfast_times)).setText(times[0]);
                ((TextView) findViewById(R.id.lunch_times)).setText(times[1]);
                ((TextView) findViewById(R.id.dinner_times)).setText(times[2]);
            } else {
                ((TextView) findViewById(R.id.breakfast_times)).setText("");
                ((TextView) findViewById(R.id.lunch_times)).setText(times[0]);
                ((TextView) findViewById(R.id.dinner_times)).setText("");
            }
            if (itemPosition != previousItem) {
                mPagerAdapter.getExistingFragment(0).updateView(restId, true);
                mPagerAdapter.getExistingFragment(1).updateView(restId, true);
                mPagerAdapter.getExistingFragment(2).updateView(restId, true);
                previousItem = -1;
            }
        }
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putInt("spinner_selection", actionBar.getSelectedNavigationIndex());
    }

    static class MenuPagerAdapter extends MultiPanePagerAdapter<MenuFragment> {

        public MenuPagerAdapter(FragmentManager fm, List<PageDescriptor> pages) {
            super(fm, pages);
        }

        @Override
        protected MenuFragment createFragment(PageDescriptor pageDescriptor) {
            return MenuFragment.newInstance(pageDescriptor.getTitle(),
                    ((MenuPageDescriptor) pageDescriptor).getRestId());
        }
    }

    static class MenuPageDescriptor extends SimplePageDescriptor {
        private String restId;

        public static final Parcelable.Creator<MenuPageDescriptor> CREATOR =
                new Parcelable.Creator<MenuPageDescriptor>() {
                    public MenuPageDescriptor createFromParcel(Parcel in) {
                        return new MenuPageDescriptor(in);
                    }

                    public MenuPageDescriptor[] newArray(int size) {
                        return new MenuPageDescriptor[size];
                    }
                };

        public MenuPageDescriptor(String title, String restId) {
            // title's unique, can double as tag
            super(title, title);
            this.restId = restId;
        }

        protected MenuPageDescriptor(Parcel in) {
            super(in);
            this.restId = in.readString();
        }

        public String getRestId() {
            return restId;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeString(restId);
        }
    }
}

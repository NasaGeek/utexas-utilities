
package com.nasageek.utexasutilities.activities;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.actionbarsherlock.app.ActionBar;
import com.mapsaurus.paneslayout.PanesActivity;
import com.mapsaurus.paneslayout.PanesLayout;
import com.mapsaurus.paneslayout.PanesLayout.OnIndexChangedListener;
import com.mapsaurus.paneslayout.PanesSizer.PaneSizer;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.fragments.BlackboardAnnouncementsFragment;
import com.nasageek.utexasutilities.fragments.BlackboardCourseMapFragment;
import com.nasageek.utexasutilities.fragments.BlackboardDownloadableItemFragment;
import com.nasageek.utexasutilities.fragments.BlackboardExternalItemFragment;
import com.nasageek.utexasutilities.fragments.BlackboardFragment;
import com.nasageek.utexasutilities.fragments.BlackboardGradesFragment;
import com.nasageek.utexasutilities.fragments.BlackboardPagerFragment;

public class BlackboardPanesActivity extends PanesActivity implements OnIndexChangedListener {
    private ActionBar actionbar;
    private PanesLayout panes;
    private int lastCompleteIndex = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        actionbar = getSupportActionBar();
        actionbar.setTitle("Blackboard");
        actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

        if (getResources().getBoolean(com.mapsaurus.paneslayout.R.bool.largeScreen)) {
            panes = (PanesLayout) findViewById(R.id.panes);
            panes.setOnIndexChangedListener(this);
        }

        setPaneSizer(new BlackboardPaneSizer());

        // this is a requirement, not sure why, nasty crashes otherwise
        if (savedInstanceState == null) {
            setMenuFragment(BlackboardPagerFragment.newInstance());
            showMenu();
        }

        // addFragment(null, BlackboardPagerFragment.newInstance());
        // addFragment(null,
        // BlackboardCourseListFragment.newInstance("Course List"));

    }

    /*
     * @Override public boolean onOptionsItemSelected(MenuItem item) { if
     * (mDelegate.onOptionsItemSelected(item)) return true; return
     * super.onOptionsItemSelected(item); }
     */

    private class BlackboardPaneSizer implements PaneSizer {
        private static final int BLACKBOARD_PAGER_PANE_TYPE = 0;
        private static final int BLACKBOARD_EXTERNAL_ITEM_PANE_TYPE = 1;
        private static final int BLACKBOARD_COURSE_MAP_PANE_TYPE = 2;
        private static final int BLACKBOARD_CONTENT_PANE_TYPE = 3;
        private static final int UNKNOWN_PANE_TYPE = -1;

        // type is the ID to the integer defining the percentage of the width of
        // the screen that a pane should occupy
        @Override
        public int getWidth(int index, int type, int parentWidth, int parentHeight) {
            Resources res = getResources();

            if (type == UNKNOWN_PANE_TYPE) {
                throw new IllegalStateException("Pane has unknown type");
            } else {
                return (int) (((double) res.getInteger(type)) / 100 * parentWidth);
            }

            /*
             * if (type == BLACKBOARD_PAGER_PANE_TYPE && index == 0) return
             * (int)
             * (((double)res.getInteger(R.integer.blackboard_pager_width_percentage
             * ))/100 * parentWidth); else if (type ==
             * BLACKBOARD_EXTERNAL_ITEM_PANE_TYPE) return (int)
             * (((double)res.getInteger
             * (R.integer.blackboard_external_item_width_percentage))/100 *
             * parentWidth); else if (type == BLACKBOARD_COURSE_MAP_PANE_TYPE)
             * return (int)
             * (((double)res.getInteger(R.integer.blackboard_other_width_percentage
             * ))/100 * parentWidth); else if (type == BLACKBOARD_) else throw
             * new IllegalStateException("Pane has unknown type");
             */

            /*
             * if (parentWidth > parentHeight) { if (type ==
             * BLACKBOARD_PAGER_PANE_TYPE && index == 0) return (int) (0.55 *
             * parentWidth); else if (type ==
             * BLACKBOARD_EXTERNAL_ITEM_PANE_TYPE) return (int) (0.55 *
             * parentWidth); else if (type == BLACKBOARD_OTHER_PANE_TYPE) return
             * (int) (0.45 * parentWidth); else throw new
             * IllegalStateException("Pane has unknown type"); } else { if (type
             * == BLACKBOARD_PAGER_PANE_TYPE && index == 0) return (int) (0.75 *
             * parentWidth); else if (type ==
             * BLACKBOARD_EXTERNAL_ITEM_PANE_TYPE) return (int) (0.75 *
             * parentWidth); else if (type == BLACKBOARD_OTHER_PANE_TYPE) return
             * (int) (0.75 * parentWidth); else throw new
             * IllegalStateException("Pane has unknown type"); }
             */
        }

        @Override
        public int getType(Object o) {
            if (o instanceof BlackboardPagerFragment) {
                return R.integer.blackboard_pager_width_percentage;
            } else if (o instanceof BlackboardExternalItemFragment) {
                return R.integer.blackboard_external_item_width_percentage;
            } else if (o instanceof BlackboardCourseMapFragment) {
                return R.integer.blackboard_course_map_width_percentage;
            } else if (o instanceof BlackboardGradesFragment
                    || o instanceof BlackboardAnnouncementsFragment
                    || o instanceof BlackboardDownloadableItemFragment) {
                return R.integer.blackboard_content_width_percentage;
            } else if (o instanceof BlackboardFragment) {
                return R.integer.blackboard_other_width_percentage;
            } else {
                return UNKNOWN_PANE_TYPE;
            }
        }

        @Override
        public boolean getFocused(Object o) {
            if (o instanceof BlackboardPagerFragment || o instanceof BlackboardExternalItemFragment) {
                return true;
            } else {
                return false;
            }
        }
    }

    // getting slightly strange data at times, look into it
    @Override
    public void onIndexChanged(int firstIndex, int lastIndex, int firstCompleteIndex,
            int lastCompleteIndex) {

        Fragment currentFragment = getFragment(lastCompleteIndex);
        if (lastCompleteIndex != this.lastCompleteIndex) {
            if (currentFragment != null) {
                this.lastCompleteIndex = lastCompleteIndex;
                for (int i = 0; i < panes.getNumPanes(); i++) {
                    if (i == lastCompleteIndex) {
                        getFragment(i).setHasOptionsMenu(true);
                    } else {
                        getFragment(i).setHasOptionsMenu(false);
                    }
                }
                if (currentFragment.isAdded()) {
                    ((OnPanesScrolledListener) currentFragment).onPanesScrolled();
                }
            }
        }
    }

    public interface OnPanesScrolledListener {
        public void onPanesScrolled();
    }
}

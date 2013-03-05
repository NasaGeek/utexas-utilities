package com.nasageek.utexasutilities.activities;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.mapsaurus.paneslayout.PanesActivity;
import com.mapsaurus.paneslayout.PanesLayout;
import com.mapsaurus.paneslayout.PanesLayout.OnIndexChangedListener;
import com.mapsaurus.paneslayout.PanesSizer.PaneSizer;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.fragments.BlackboardCourseListFragment;
import com.nasageek.utexasutilities.fragments.BlackboardExternalItemFragment;
import com.nasageek.utexasutilities.fragments.BlackboardFragment;
import com.nasageek.utexasutilities.fragments.BlackboardPagerFragment;

public class BlackboardPanesActivity extends PanesActivity implements OnIndexChangedListener {
	private ActionBar actionbar;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		actionbar = getSupportActionBar();
		actionbar.setTitle("Blackboard");
		actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionbar.setHomeButtonEnabled(true);
		actionbar.setDisplayHomeAsUpEnabled(true);
		//PanesLayout panes = (PanesLayout) findViewById(R.id.panes);
		//setOnIndexChangedListener(this);
		setPaneSizer(new BlackboardPaneSizer());
		
		//this is a requirement, not sure why, nasty crashes otherwise
		if(savedInstanceState == null)
			setMenuFragment(BlackboardPagerFragment.newInstance());
		
		//addFragment(null, BlackboardPagerFragment.newInstance());
//		addFragment(null, BlackboardCourseListFragment.newInstance("Course List"));
		
		
	}
	@Override
	public void updateFragment(Fragment f) {	
		// nothing special happening to fragments
	}
	
	private class BlackboardPaneSizer implements PaneSizer {
		private static final int BLACKBOARD_PAGER_PANE_TYPE = 0;
		private static final int BLACKBOARD_EXTERNAL_ITEM_PANE_TYPE = 1;
		private static final int BLACKBOARD_OTHER_PANE_TYPE = 2;	
		private static final int UNKNOWN_PANE_TYPE = -1;

		@Override
		public int getWidth(int index, int type, int parentWidth, int parentHeight) {
			if (parentWidth > parentHeight) {
				if (type == BLACKBOARD_PAGER_PANE_TYPE && index == 0)
					return (int) (0.55 * parentWidth);
				else if (type == BLACKBOARD_EXTERNAL_ITEM_PANE_TYPE)
					return (int) (0.55 * parentWidth);
				else if (type == BLACKBOARD_OTHER_PANE_TYPE)
					return (int) (0.45 * parentWidth);
				else throw new IllegalStateException("Pane has unknown type");
			} else {
				if (type == BLACKBOARD_PAGER_PANE_TYPE && index == 0)
					return (int) (0.75 * parentWidth);
				else if (type == BLACKBOARD_EXTERNAL_ITEM_PANE_TYPE)
					return (int) (0.75 * parentWidth);
				else if (type == BLACKBOARD_OTHER_PANE_TYPE)
					return (int) (0.75 * parentWidth);
				else throw new IllegalStateException("Pane has unknown type");
			}
		}

		@Override
		public int getType(Object o) {
			if (o instanceof BlackboardPagerFragment)
				return BLACKBOARD_PAGER_PANE_TYPE;
			else if (o instanceof BlackboardExternalItemFragment)
				return BLACKBOARD_EXTERNAL_ITEM_PANE_TYPE;
			else if (o instanceof BlackboardFragment)
				return BLACKBOARD_OTHER_PANE_TYPE;
			else return UNKNOWN_PANE_TYPE;
		}

		@Override
		public boolean getFocused(Object o) {
			if (o instanceof BlackboardPagerFragment || o instanceof BlackboardExternalItemFragment)
				return true;
			else return false;
		}
	}

	@Override
	public void onIndexChanged(int firstIndex, int lastIndex,
			int firstCompleteIndex, int lastCompleteIndex) {
		
		
		
	}

}

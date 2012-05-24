package com.nasageek.utexasutilities;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.ActionMode;

public abstract class ActionModeFragment extends SherlockFragment {

	ActionMode mode;
	
	public ActionMode getActionMode()
	{
		return mode;
	}
}

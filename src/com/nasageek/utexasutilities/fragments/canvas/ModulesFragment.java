
package com.nasageek.utexasutilities.fragments.canvas;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.nasageek.utexasutilities.CanvasRequestListener;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.activities.BlackboardPanesActivity.OnPanesScrolledListener;
import com.nasageek.utexasutilities.adapters.ModuleAdapter;
import com.nasageek.utexasutilities.fragments.BaseSpiceListFragment;
import com.nasageek.utexasutilities.model.canvas.Module;
import com.nasageek.utexasutilities.model.canvas.ModuleItem;
import com.nasageek.utexasutilities.requests.CanvasModulesRequest;
import com.octo.android.robospice.persistence.DurationInMillis;

public class ModulesFragment extends BaseSpiceListFragment implements OnPanesScrolledListener {

    private CanvasModulesRequest canvasModulesRequest;
    private String courseId, courseName, courseCode;

    public static ModulesFragment newInstance(String courseID, String courseName, String courseCode) {
        ModulesFragment mf = new ModulesFragment();

        Bundle args = new Bundle();
        args.putString("courseID", courseID);
        args.putString("courseName", courseName);
        args.putString("courseCode", courseCode);
        mf.setArguments(args);

        return mf;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setBackgroundResource(R.drawable.background_holo_light);
        // this should be free... but it ain't
        if (getListAdapter() == null) {
            setListShown(false);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        courseId = getArguments().getString("courseID");
        courseName = getArguments().getString("courseName");
        courseCode = getArguments().getString("courseCode");
        setupActionBar();

        Module.List mItems = new Module.List();

        // TODO: pinned headers. Doesn't work now because not an AmazingListView
        ModuleAdapter mAdapter = new ModuleAdapter(getActivity(), mItems);

        canvasModulesRequest = new CanvasModulesRequest(
                ConnectionHelper.getCanvasAuthCookie(getActivity()), courseId);
        getSpiceManager().execute(canvasModulesRequest, courseId + "modules",
                DurationInMillis.ONE_MINUTE * 5,
                new CanvasRequestListener<Module.List>(this, mAdapter, mItems, "No modules"));
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        ModuleItem item = (ModuleItem) l.getItemAtPosition(position);

        if (item.type.equals("File")) {
            // do filey stuff
        } else if (item.type.equals("Assignment")) {
            // do assignmenty stuff
        }
    }

    private void setupActionBar() {
        final ActionBar actionbar = getSherlockActivity().getSupportActionBar();
        actionbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE, ActionBar.DISPLAY_SHOW_TITLE
                | ActionBar.DISPLAY_SHOW_CUSTOM);
        actionbar.setTitle(courseCode);
        actionbar.setSubtitle("Modules");
    }

    @Override
    public void onPanesScrolled() {
        setupActionBar();
    }

    @Override
    public int getPaneWidth() {
        return R.integer.blackboard_content_width_percentage;
    }

}

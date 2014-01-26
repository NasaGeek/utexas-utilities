
package com.nasageek.utexasutilities.fragments.canvas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.mapsaurus.paneslayout.FragmentLauncher;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.activities.BlackboardPanesActivity.OnPanesScrolledListener;
import com.nasageek.utexasutilities.fragments.BaseSpiceListFragment;

public class CanvasCourseMapFragment extends BaseSpiceListFragment implements
        OnPanesScrolledListener {

    private String courseId, courseName, courseCode;

    public static CanvasCourseMapFragment newInstance(String courseID, String courseName,
            String courseCode) {
        CanvasCourseMapFragment bcmf = new CanvasCourseMapFragment();

        Bundle args = new Bundle();
        args.putString("courseID", courseID);
        args.putString("courseName", courseName);
        args.putString("courseCode", courseCode);
        bcmf.setArguments(args);

        return bcmf;
    }

    public CanvasCourseMapFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_canvas_list, null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Bundle args = getArguments();
        courseId = args.getString("courseID");
        courseName = args.getString("courseName");
        courseCode = args.getString("courseCode");
        setupActionBar();
        setListAdapter(new ArrayAdapter<String>(getActivity(), R.layout.coursemap_item_view,
                R.id.coursemap_item_name, new String[] {
                        "Assignments", "Files"
                }));

        // canvasAssignmentsRequest = new CanvasAssignmentsRequest(course_id);
    }

    @Override
    public void onListItemClick(ListView lv, View v, int position, long id) {
        String feature = (String) lv.getItemAtPosition(position);
        if (feature.equals("Assignments")) {
            ((FragmentLauncher) getActivity()).addFragment(CanvasCourseMapFragment.this,
                    AssignmentsFragment.newInstance(courseId, courseName, courseCode));
        } else if (feature.equals("Files")) {
            ((FragmentLauncher) getActivity()).addFragment(CanvasCourseMapFragment.this,
                    FileBrowserFragment.newInstance(courseId, courseName, courseCode));
        }
    }

    private void setupActionBar() {
        final ActionBar actionbar = getSherlockActivity().getSupportActionBar();
        actionbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE, ActionBar.DISPLAY_SHOW_TITLE
                | ActionBar.DISPLAY_SHOW_CUSTOM);
        actionbar.setTitle(courseCode);
        actionbar.setSubtitle("Course Map");
    }

    @Override
    public void onPanesScrolled() {
        setupActionBar();
    }
}

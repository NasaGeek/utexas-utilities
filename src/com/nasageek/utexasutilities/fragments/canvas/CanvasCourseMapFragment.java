
package com.nasageek.utexasutilities.fragments.canvas;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.mapsaurus.paneslayout.FragmentLauncher;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.activities.CoursesActivity.OnPanesScrolledListener;
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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setBackgroundResource(R.drawable.background_holo_light);
        setupActionBar();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Bundle args = getArguments();
        courseId = args.getString("courseID");
        courseName = args.getString("courseName");
        courseCode = args.getString("courseCode");
        setListAdapter(new ArrayAdapter<String>(getActivity(), R.layout.coursemap_item_view,
                R.id.coursemap_item_name, new String[] {
                        "Announcements", "Assignments", "Files", "Modules"
                }));

        // canvasAssignmentsRequest = new CanvasAssignmentsRequest(course_id);
    }

    @Override
    public void onListItemClick(ListView lv, View v, int position, long id) {
        String feature = (String) lv.getItemAtPosition(position);
        if (feature.equals("Assignments")) {
            launch(AssignmentsFragment.newInstance(courseId, courseName, courseCode));
        } else if (feature.equals("Files")) {
            launch(FileBrowserFragment.newInstance(courseId, courseName, courseCode));
        } else if (feature.equals("Announcements")) {
            launch(ActivityStreamFragment.newInstance(courseId, courseName, courseCode));
        } else if (feature.equals("Modules")) {
            launch(ModulesFragment.newInstance(courseId, courseName, courseCode));
        }
    }

    private void launch(Fragment frag) {
        ((FragmentLauncher) getActivity()).addFragment(this, frag);
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

    @Override
    public int getPaneWidth() {
        return R.integer.blackboard_course_map_width_percentage;
    }
}

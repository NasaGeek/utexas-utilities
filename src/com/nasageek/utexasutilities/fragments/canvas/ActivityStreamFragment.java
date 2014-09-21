
package com.nasageek.utexasutilities.fragments.canvas;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.UTilitiesApplication;
import com.nasageek.utexasutilities.activities.CoursesActivity.OnPanesScrolledListener;
import com.nasageek.utexasutilities.adapters.ActivityStreamAdapter;
import com.nasageek.utexasutilities.adapters.AssignmentAdapter;
import com.nasageek.utexasutilities.fragments.BaseSpiceListFragment;
import com.nasageek.utexasutilities.model.canvas.ActivityStreamItem;
import com.nasageek.utexasutilities.model.canvas.Assignment;
import com.nasageek.utexasutilities.requests.CanvasActivityStreamRequest;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.ArrayList;
import java.util.List;

public class ActivityStreamFragment extends BaseSpiceListFragment implements
        OnPanesScrolledListener {

    private AssignmentAdapter assignmentsAdapter;
    private CanvasActivityStreamRequest canvasActivityStreamRequest;
    private String courseId, courseName, courseCode;
    private List<Assignment> assignments;

    public static ActivityStreamFragment newInstance(String courseID, String courseName,
            String courseCode) {
        ActivityStreamFragment asf = new ActivityStreamFragment();

        Bundle args = new Bundle();
        args.putString("courseID", courseID);
        args.putString("courseName", courseName);
        args.putString("courseCode", courseCode);
        asf.setArguments(args);

        return asf;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setEmptyText("No announcements available");
        ((TextView) getListView().getEmptyView()).setTextSize(20);
        setupActionBar();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        courseId = getArguments().getString("courseID");
        courseName = getArguments().getString("courseName");
        courseCode = getArguments().getString("courseCode");

        String canvasAuthToken = ((UTilitiesApplication) getActivity().getApplication())
                .getCanvasAuthCookieVal();
        canvasActivityStreamRequest = new CanvasActivityStreamRequest(canvasAuthToken, courseId);
        getSpiceManager().execute(canvasActivityStreamRequest, courseId + "activitystream",
                DurationInMillis.ONE_MINUTE * 5, new CanvasActivityStreamRequestListener());
    }

    public final class CanvasActivityStreamRequestListener implements
            RequestListener<ActivityStreamItem.List> {

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            Toast.makeText(getActivity(), "failure", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRequestSuccess(final ActivityStreamItem.List result) {
            List<ActivityStreamItem> filteredResult = new ArrayList<>();
            for (ActivityStreamItem item : result) {
                if ("Announcement".equals(item.type)) {
                    filteredResult.add(item);
                }
            }
            setListAdapter(new ActivityStreamAdapter(getActivity(), R.layout.announcement_item_view,
                    filteredResult));
        }
    }

    private void setupActionBar() {
        final ActionBar actionbar = getSherlockActivity().getSupportActionBar();
        actionbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE, ActionBar.DISPLAY_SHOW_TITLE
                | ActionBar.DISPLAY_SHOW_CUSTOM);
        actionbar.setTitle(courseCode);
        actionbar.setSubtitle("Announcements");
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

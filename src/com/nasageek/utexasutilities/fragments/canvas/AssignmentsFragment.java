
package com.nasageek.utexasutilities.fragments.canvas;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.nasageek.utexasutilities.CanvasRequestListener;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.UTilitiesApplication;
import com.nasageek.utexasutilities.activities.CoursesActivity.OnPanesScrolledListener;
import com.nasageek.utexasutilities.adapters.AssignmentAdapter;
import com.nasageek.utexasutilities.fragments.BaseSpiceListFragment;
import com.nasageek.utexasutilities.model.canvas.Assignment;
import com.nasageek.utexasutilities.requests.CanvasAssignmentsRequest;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.List;

public class AssignmentsFragment extends BaseSpiceListFragment implements OnPanesScrolledListener {

    private AssignmentAdapter assignmentsAdapter;
    private CanvasAssignmentsRequest canvasAssignmentsRequest;
    private String courseId, courseName, courseCode;
    private List<Assignment> assignments;

    public static AssignmentsFragment newInstance(String courseID, String courseName,
            String courseCode) {
        AssignmentsFragment af = new AssignmentsFragment();

        Bundle args = new Bundle();
        args.putString("courseID", courseID);
        args.putString("courseName", courseName);
        args.putString("courseCode", courseCode);
        af.setArguments(args);

        return af;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setBackgroundResource(R.drawable.background_holo_light);
        // this should be free... but it ain't
        setListShown(false);
        setupActionBar();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        courseId = getArguments().getString("courseID");
        courseName = getArguments().getString("courseName");
        courseCode = getArguments().getString("courseCode");

        Assignment.List mItems = new Assignment.List();
        AssignmentAdapter mAdapter = new AssignmentAdapter(getActivity(), R.layout.grade_item_view,
                mItems);

        String canvasAuthToken = ((UTilitiesApplication) getActivity().getApplication())
                .getCanvasAuthCookieVal();
        canvasAssignmentsRequest = new CanvasAssignmentsRequest(canvasAuthToken, courseId);
        getSpiceManager()
                .execute(
                        canvasAssignmentsRequest,
                        courseId + "assignments",
                        DurationInMillis.ONE_MINUTE * 5,
                        new CanvasRequestListener<Assignment.List>(this, mAdapter, mItems,
                                "No assignments"));
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Assignment assignment = (Assignment) l.getAdapter().getItem(position);

        Dialog dlg = new Dialog(getActivity());
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlg.setContentView(R.layout.grade_info_dialog);
        dlg.setTitle("Assignment Info");

        TextView nameview = (TextView) dlg.findViewById(R.id.grade_info_name);
        TextView scoreview = (TextView) dlg.findViewById(R.id.grade_info_value);
        TextView descriptionview = (TextView) dlg.findViewById(R.id.grade_info_comment);

        String score = "-";
        CharSequence description = "No description";
        if (assignment.description != null) {
            description = trim(Html.fromHtml(assignment.description));
        }
        if (assignment.submission != null && assignment.submission.score != null) {
            score = assignment.submission.score;
        }
        nameview.setText(assignment.name);
        scoreview.setText(score + "/" + assignment.points_possible);
        descriptionview.setText(description);

        dlg.setCanceledOnTouchOutside(true);
        dlg.show();
        // TODO: DialogFragment or showDialog
    }

    private CharSequence trim(CharSequence s) {
        int start = 0;
        int end = s.length();
        while (start < end && Character.isWhitespace(s.charAt(start))) {
            start++;
        }

        while (end > start && Character.isWhitespace(s.charAt(end - 1))) {
            end--;
        }

        return s.subSequence(start, end);
    }

    public final class CanvasAssignmentsRequestListener implements RequestListener<Assignment.List> {

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            Toast.makeText(getActivity(), "failure", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRequestSuccess(final Assignment.List result) {
            setListAdapter(new AssignmentAdapter(getActivity(), R.layout.grade_item_view, result));
        }
    }

    private void setupActionBar() {
        final ActionBar actionbar = getSherlockActivity().getSupportActionBar();
        actionbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE, ActionBar.DISPLAY_SHOW_TITLE
                | ActionBar.DISPLAY_SHOW_CUSTOM);
        actionbar.setTitle(courseCode);
        actionbar.setSubtitle("Assignments");
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

package com.nasageek.utexasutilities.fragments.canvas;

import java.util.List;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nasageek.utexasutilities.ConnectionHelper;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.adapters.AssignmentsAdapter;
import com.nasageek.utexasutilities.fragments.BaseSpiceListFragment;
import com.nasageek.utexasutilities.fragments.canvas.AssignmentsFragment.CanvasAssignmentsRequestListener;
import com.nasageek.utexasutilities.model.canvas.Assignment;
import com.nasageek.utexasutilities.requests.CanvasAssignmentsRequest;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

public class FileBrowserFragment extends BaseSpiceListFragment {
	
	private FilesAdapter filesAdapter;
	private CanvasFilesRequest canvasFilesRequest;
	private String course_id, course_name;
	private List<Assignment> assignments;
	
	public static FileBrowserFragment newInstance(String courseID, String courseName, String courseCode) {
		FileBrowserFragment fbf = new FileBrowserFragment();
		
		Bundle args = new Bundle();
        args.putString("courseID", courseID); 
        args.putString("courseName", courseName);
        args.putString("courseCode", courseCode);
        fbf.setArguments(args);
        
        return fbf;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		course_id = getArguments().getString("courseID");
		course_name = getArguments().getString("courseName");
		
		canvasAssignmentsRequest = new CanvasAssignmentsRequest(ConnectionHelper.getCanvasAuthCookie(getActivity()), course_id);
		getSpiceManager().execute(canvasAssignmentsRequest, course_id + "assignments", DurationInMillis.ONE_MINUTE * 5, new CanvasAssignmentsRequestListener());		
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Assignment assignment = (Assignment)l.getAdapter().getItem(position);
		
		Dialog dlg = new Dialog(getSherlockActivity());
		dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dlg.setContentView(R.layout.grade_info_dialog);
		dlg.setTitle("Assignment Info");
		
		TextView nameview = (TextView) dlg.findViewById(R.id.grade_info_name);
		TextView scoreview = (TextView) dlg.findViewById(R.id.grade_info_value);
		TextView commentview = (TextView) dlg.findViewById(R.id.grade_info_comment);
		
		String score = "-";
		String comment = "";//"No comments"; <-- will put in the future, comments broken atm
		if(assignment.submission != null) {
			score = assignment.submission.score;
			if(assignment.submission.submission_comments != null && assignment.submission.submission_comments.comment != null) {
				comment = assignment.submission.submission_comments.comment;
			}
		}
		nameview.setText(assignment.name);
		scoreview.setText(score + "/" + assignment.points_possible);
		commentview.setText(comment);
		
		dlg.setCanceledOnTouchOutside(true);
		dlg.show();
		//TODO: DialogFragment or showDialog
	}
	
	public final class CanvasAssignmentsRequestListener implements RequestListener<Assignment.List> {

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            Toast.makeText(getSherlockActivity(), "failure", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRequestSuccess(final Assignment.List result) {
    		setListAdapter(new AssignmentsAdapter(getSherlockActivity(), R.layout.grade_item_view, result));
        }
    }
	
	
}

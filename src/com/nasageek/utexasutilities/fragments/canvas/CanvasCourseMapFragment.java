package com.nasageek.utexasutilities.fragments.canvas;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.mapsaurus.paneslayout.FragmentLauncher;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.fragments.BaseSpiceListFragment;
import com.nasageek.utexasutilities.fragments.BlackboardCourseMapFragment;
import com.nasageek.utexasutilities.model.canvas.Assignment;
import com.nasageek.utexasutilities.requests.CanvasAssignmentsRequest;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;


public class CanvasCourseMapFragment extends BaseSpiceListFragment {
	
	private String course_id, course_name, course_code;
	
	public static CanvasCourseMapFragment newInstance(String courseID, String courseName, String courseCode) {
		CanvasCourseMapFragment bcmf = new CanvasCourseMapFragment();
		
		Bundle args = new Bundle();
        args.putString("courseID", courseID);
        args.putString("courseName", courseName);
        args.putString("courseCode", courseCode);
        bcmf.setArguments(args);
        
        return bcmf;
	}
	
	public CanvasCourseMapFragment() {}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		setListAdapter(new ArrayAdapter<String>(getActivity(),
											    R.layout.coursemap_item_view, 
											    R.id.coursemap_item_name, 
											    new String[] {"Assignments", "Files"}));
		Bundle args = getArguments();
		course_id = args.getString("courseID");
		course_name = args.getString("courseName");
		course_code = args.getString("courseCode");
		Log.d("course_id", course_id);
	//	canvasAssignmentsRequest = new CanvasAssignmentsRequest(course_id);
	}
	
	@Override
	public void onListItemClick(ListView lv, View v, int position, long id) {
		String feature = (String) lv.getItemAtPosition(position);
		if(feature.equals("Assignments")) {
			((FragmentLauncher) getActivity()).addFragment(CanvasCourseMapFragment.this, 
					AssignmentsFragment.newInstance(course_id, course_name, course_code));
		} else if(feature.equals("Files")) {
			((FragmentLauncher) getActivity()).addFragment(CanvasCourseMapFragment.this, 
					FileBrowserFragment.newInstance(course_id, course_name, course_code));
		}
	}
}

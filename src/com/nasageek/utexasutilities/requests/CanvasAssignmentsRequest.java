package com.nasageek.utexasutilities.requests;

import com.nasageek.utexasutilities.model.canvas.Assignment;
import com.nasageek.utexasutilities.retrofit.Canvas;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

public class CanvasAssignmentsRequest extends RetrofitSpiceRequest<Assignment.List, Canvas> {
	
	private String course_id;
	private String canvas_auth_token;
	
	public CanvasAssignmentsRequest(String canvas_auth_token, String course_id) {
		super(Assignment.List.class, Canvas.class);
		this.course_id = course_id;
		this.canvas_auth_token = canvas_auth_token;
	}

	@Override
	public Assignment.List loadDataFromNetwork() throws Exception {
		return getService().assignmentsForCourse(canvas_auth_token, course_id);
	}

}
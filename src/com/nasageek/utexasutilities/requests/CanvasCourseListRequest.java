package com.nasageek.utexasutilities.requests;

import com.nasageek.utexasutilities.model.canvas.CanvasCourse;
import com.nasageek.utexasutilities.retrofit.Canvas;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

public class CanvasCourseListRequest extends RetrofitSpiceRequest<CanvasCourse.List, Canvas> {
	
	private	String canvas_auth_token;
	
	public CanvasCourseListRequest(String canvas_auth_token) {
		super(CanvasCourse.List.class, Canvas.class);
		this.canvas_auth_token = canvas_auth_token;
	}

	@Override
	public CanvasCourse.List loadDataFromNetwork() throws Exception {
		return getService().courseList(canvas_auth_token);
	}

}

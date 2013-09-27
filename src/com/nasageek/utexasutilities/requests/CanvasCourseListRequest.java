package com.nasageek.utexasutilities.requests;

import java.util.List;

import com.nasageek.utexasutilities.model.canvas.CanvasCourse;
import com.nasageek.utexasutilities.retrofit.Canvas;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

public class CanvasCourseListRequest extends RetrofitSpiceRequest<CanvasCourse.List, Canvas> {
	
	public CanvasCourseListRequest() {
		super(CanvasCourse.List.class, Canvas.class);
	}

	@Override
	public CanvasCourse.List loadDataFromNetwork() throws Exception {
		return getService().courseList();
	}

}

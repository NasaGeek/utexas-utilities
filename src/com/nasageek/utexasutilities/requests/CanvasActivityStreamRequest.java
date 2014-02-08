
package com.nasageek.utexasutilities.requests;

import com.nasageek.utexasutilities.model.canvas.ActivityStreamItem;
import com.nasageek.utexasutilities.retrofit.Canvas;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

public class CanvasActivityStreamRequest extends
        RetrofitSpiceRequest<ActivityStreamItem.List, Canvas> {

    private String course_id;
    private String canvas_auth_token;

    public CanvasActivityStreamRequest(String canvas_auth_token, String course_id) {
        super(ActivityStreamItem.List.class, Canvas.class);
        this.course_id = course_id;
        this.canvas_auth_token = canvas_auth_token;
    }

    @Override
    public ActivityStreamItem.List loadDataFromNetwork() throws Exception {
        return getService().activityStreamForCourse(canvas_auth_token, course_id);
    }

}

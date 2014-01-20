
package com.nasageek.utexasutilities.requests;

import com.nasageek.utexasutilities.model.canvas.File;
import com.nasageek.utexasutilities.retrofit.Canvas;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

public class CanvasFilesRequest extends RetrofitSpiceRequest<File.List, Canvas> {

    private String course_id;
    private String canvas_auth_token;

    public CanvasFilesRequest(String canvas_auth_token, String course_id) {
        super(File.List.class, Canvas.class);
        this.course_id = course_id;
        this.canvas_auth_token = canvas_auth_token;
    }

    @Override
    public File.List loadDataFromNetwork() throws Exception {
        return getService().filesForCourse(canvas_auth_token, course_id);
    }

}

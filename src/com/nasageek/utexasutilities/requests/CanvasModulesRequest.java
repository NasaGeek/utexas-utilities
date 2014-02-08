
package com.nasageek.utexasutilities.requests;

import com.nasageek.utexasutilities.model.canvas.Module;
import com.nasageek.utexasutilities.retrofit.Canvas;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

public class CanvasModulesRequest extends RetrofitSpiceRequest<Module.List, Canvas> {

    private String course_id;
    private String canvas_auth_token;

    public CanvasModulesRequest(String canvas_auth_token, String course_id) {
        super(Module.List.class, Canvas.class);
        this.course_id = course_id;
        this.canvas_auth_token = canvas_auth_token;
    }

    @Override
    public Module.List loadDataFromNetwork() throws Exception {
        return getService().modulesForCourse(canvas_auth_token, course_id);
    }
}

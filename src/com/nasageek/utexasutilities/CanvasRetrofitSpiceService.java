
package com.nasageek.utexasutilities;

import com.nasageek.utexasutilities.retrofit.Canvas;
import com.octo.android.robospice.retrofit.RetrofitGsonSpiceService;

public class CanvasRetrofitSpiceService extends RetrofitGsonSpiceService {

    private final static String BASE_URL = "https://utexas.instructure.com";

    public CanvasRetrofitSpiceService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        addRetrofitInterface(Canvas.class);
    }

    @Override
    protected String getServerUrl() {
        return BASE_URL;
    }

}

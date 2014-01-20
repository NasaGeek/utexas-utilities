
package com.nasageek.utexasutilities.requests;

import com.nasageek.utexasutilities.model.canvas.OAuthResponse;
import com.nasageek.utexasutilities.retrofit.Canvas;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

public class CanvasPostOAuthCodeRequest extends RetrofitSpiceRequest<OAuthResponse, Canvas> {

    private String oauth2_code;

    public CanvasPostOAuthCodeRequest(String oauth2_code) {
        super(OAuthResponse.class, Canvas.class);
        this.oauth2_code = oauth2_code;

    }

    @Override
    public OAuthResponse loadDataFromNetwork() throws Exception {
        return getService().postAuthCode("10000000000228",
                "6PeIftye8sWqKkpOPMmTgdUCgYyCpHIYbPG9yi4YyTHjRbmXJJfT5amsHnBxzkCU",
                "urn:ietf:wg:oauth:2.0:oob", oauth2_code);
    }

}

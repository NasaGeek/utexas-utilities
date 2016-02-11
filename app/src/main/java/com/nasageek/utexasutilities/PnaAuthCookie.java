package com.nasageek.utexasutilities;

import android.app.Application;

import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import static com.nasageek.utexasutilities.UTilitiesApplication.PNA_AUTH_COOKIE_KEY;

/**
 * Created by Chris on 7/5/2014.
 */
public class PnaAuthCookie extends AuthCookie {


    public PnaAuthCookie(Application mApp) {
        super(PNA_AUTH_COOKIE_KEY,
              "AUTHCOOKIE",
              "https://management.pna.utexas.edu/server/graph.cgi",
              "PNACLOGINusername",
              "PNACLOGINpassword",
              mApp);
    }

    @Override
    protected Request buildLoginRequest() {
        String user = settings.getString("eid", "error").trim();
        String pw = UTilitiesApplication.getInstance().getSecurePreferences()
                .getString("password", "error");

        RequestBody requestBody = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addPart(
                        Headers.of("Content-Disposition", "form-data; name=\"PNACLOGINusername\""),
                        RequestBody.create(null, user))
                .addPart(
                        Headers.of("Content-Disposition", "form-data; name=\"PNACLOGINpassword\""),
                        RequestBody.create(null, pw))
                .addPart(
                        Headers.of("Content-Disposition", "form-data; name=\"PNACLOGINLoginEID\""),
                        RequestBody.create(null, "Log In "))
                .build();
        return new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
    }
}

package com.nasageek.utexasutilities;

import android.app.Application;

import static com.nasageek.utexasutilities.UTilitiesApplication.UTD_AUTH_COOKIE_KEY;

/**
 * Created by chris on 9/16/14.
 */
public class UtdAuthCookie extends AuthCookie {

    public UtdAuthCookie(Application mApp) {
        super(UTD_AUTH_COOKIE_KEY,
                "utlogin-prod",
                "https://login.utexas.edu/openam/UI/Login",
                "IDToken1",
                "IDToken2",
                mApp);
    }

    public void setAuthCookieVal(String authCookie) {
        // hardcode utdirect URL for now until I figure out LARES posting stuff
        super.setAuthCookieVal(authCookie, ".utexas.edu");
    }
}

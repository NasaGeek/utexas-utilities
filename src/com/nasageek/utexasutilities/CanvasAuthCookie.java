package com.nasageek.utexasutilities;

import android.content.Context;

import static com.nasageek.utexasutilities.UTilitiesApplication.CANVAS_AUTH_COOKIE_KEY;

/**
 * Created by chris on 9/16/14.
 * TODO: change this to CanvasAuthToken at some point when it's convenient.
 * That much better represents its functionality, as this is definitely not a cookie.
 */
public class CanvasAuthCookie extends AuthCookie {

    public CanvasAuthCookie(Context con) {
        super(CANVAS_AUTH_COOKIE_KEY, null, null, null, null, con);
    }

    @Override
    protected void resetCookie() {
        // Canvas doesn't deal with cookies at all, so there's nothing to remove
        settings.edit().remove(prefKey).apply();
        authCookie = "";
        cookieHasBeenSet = false;
    }

    @Override
    public void setAuthCookieVal(String authCookie) {
        // Canvas doesn't deal with cookies at all, so there's nothing to set
        this.authCookie = authCookie;
        settings.edit().putString(prefKey, authCookie).apply();
        cookieHasBeenSet = true;
    }
}

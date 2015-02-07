package com.nasageek.utexasutilities;

import android.app.Application;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;

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
        this.authCookie = authCookie;
        settings.edit().putString(prefKey, authCookie).apply();

        /*
        this is technically unnecessary if OkHttp handled the authentication, because it will
        have already set the cookies in the CookieHandler. It doesn't seem to cause any issues
        just to re-add the cookies, though
         */
        HttpCookie httpCookie = new HttpCookie(authCookieKey, authCookie);
        // hardcode utdirect URL for now until I figure out LARES posting stuff
        httpCookie.setDomain("utdirect.utexas.edu");
        try {
            CookieStore cookies = ((CookieManager) CookieHandler.getDefault()).getCookieStore();
            cookies.add(URI.create("utdirect.utexas.edu"), httpCookie);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        cookieHasBeenSet = true;
    }
}

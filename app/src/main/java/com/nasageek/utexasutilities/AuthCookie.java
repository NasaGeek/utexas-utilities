package com.nasageek.utexasutilities;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.webkit.CookieSyncManager;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Chris on 7/4/2014.
 */
public class AuthCookie {

    protected String prefKey;
    protected String authCookie;
    protected String authCookieKey;
    protected String userNameKey;
    protected String passwordKey;
    protected boolean cookieHasBeenSet;
    protected OkHttpClient client;
    protected SharedPreferences settings;
    protected URL url;
    protected Application mApp;


    public AuthCookie(String prefKey, String authCookieKey, String loginUrl, String userNameKey,
                      String passwordKey, Application mApp) {
        this.prefKey = prefKey;
        this.authCookieKey = authCookieKey;
        this.userNameKey = userNameKey;
        this.passwordKey = passwordKey;
        try {
            this.url = new URL(loginUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        this.client = UTilitiesApplication.getInstance().getHttpClient();
        this.client.setConnectTimeout(10, TimeUnit.SECONDS);
        this.settings = PreferenceManager.getDefaultSharedPreferences(mApp);
        this.mApp = mApp;
    }

    public boolean hasCookieBeenSet() {
        return cookieHasBeenSet;
    }

    public String getPrefKey() {
        return prefKey;
    }

    public String getAuthCookieKey() {
        return authCookieKey;
    }

    @Nullable
    public String getAuthCookieVal() {
        if (cookieHasBeenSet) {
            return authCookie;
        } else if (settings.contains(prefKey)) {
            cookieHasBeenSet = true;
            authCookie = settings.getString(prefKey, "");
            return authCookie;
        } else {
           return null;
        }
    }

    private void resetCookie() {
        settings.edit().remove(prefKey).apply();
        authCookie = "";
        try {
            /*
            This step is *required* for PNA, and nicety for other services. PNA won't let you
            log in if you're still holding on to a valid authcookie, so we clear them out.
             */
            URI loginURI = URI.create(url.getHost());
            CookieStore cookies = ((CookieManager) CookieHandler.getDefault()).getCookieStore();
            for (HttpCookie cookie : cookies.get(loginURI)) {
                cookies.remove(loginURI, cookie);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        cookieHasBeenSet = false;
    }

    public void setAuthCookieVal(String authCookie) {
        setAuthCookieVal(authCookie, url.getHost());
    }

    protected void setAuthCookieVal(String authCookie, String domain) {
        this.authCookie = authCookie;
        settings.edit().putString(prefKey, authCookie).apply();

        /*
        this is technically unnecessary if OkHttp handled the authentication, because it will
        have already set the cookies in the CookieHandler. It doesn't seem to cause any issues
        just to re-add the cookies, though
         */
        HttpCookie httpCookie = new HttpCookie(authCookieKey, authCookie);
        httpCookie.setDomain(domain);
        try {
            CookieStore cookies = ((CookieManager) CookieHandler.getDefault()).getCookieStore();
            cookies.add(URI.create(domain), httpCookie);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        cookieHasBeenSet = true;
        android.webkit.CookieManager.getInstance().setCookie(domain, httpCookie.getName() + "=" + httpCookie.getValue());
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            android.webkit.CookieManager.getInstance().flush();
        } else {
            CookieSyncManager.getInstance().sync();
        }
    }

    /**
     * Builds a login request and executes it to produce a valid AuthCookie.
     * @return true if the cookie was set successfully, false if the cookie was not set
     * @throws IOException
     * @throws com.nasageek.utexasutilities.TempLoginException if persistent login is
     * not activated
     */
    public boolean login() throws IOException {
        if (!settings.getBoolean(mApp.getString(R.string.pref_logintype_key), false)) {
            throw new TempLoginException("login() cannot be called when persistent login" +
                    " is turned off.");
        }
        Request request = buildLoginRequest();
        return performLogin(request);
    }

    /**
     * Executes a login request with the AuthCookie's OkHttpClient. This should only be called
     * when persistent login is activated.
     * @param request Request to execute
     * @return true if the cookie was set successfully, false if the cookie was not set
     * @throws IOException
     */
    protected boolean performLogin(Request request) throws IOException {
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Bad response code: " + response + " during login.");
        }
        response.body().close();
        CookieManager cm = (CookieManager) CookieHandler.getDefault();
        List<HttpCookie> cookies = cm.getCookieStore().getCookies();
        for (HttpCookie cookie : cookies) {
            String cookieVal = cookie.getValue();
            if (cookie.getName().equals(authCookieKey)) {
                setAuthCookieVal(cookieVal);
                return true;
            }
        }
        return false;
    }

    protected Request buildLoginRequest() {
        String user = settings.getString("eid", "error");
        String pw = UTilitiesApplication.getInstance().getSecurePreferences()
                .getString("password", "error");

        RequestBody requestBody = new FormEncodingBuilder()
                .add(userNameKey, user)
                .add(passwordKey, pw)
                .build();
        return new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
    }

    public void logout() {
        resetCookie();
    }
}

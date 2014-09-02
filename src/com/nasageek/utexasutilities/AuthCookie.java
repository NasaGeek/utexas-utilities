package com.nasageek.utexasutilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

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
    private OkHttpClient client;
    protected SharedPreferences settings;
    protected SecurePreferences secureSettings;

    protected URL url;


    public AuthCookie(String prefKey, String authCookieKey, String loginUrl, String userNameKey, String passwordKey, Context con) {
        this.prefKey = prefKey;
        this.authCookieKey = authCookieKey;
        this.userNameKey = userNameKey;
        this.passwordKey = passwordKey;
        try {
            this.url = new URL(loginUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        this.client = new OkHttpClient();
        this.secureSettings = new SecurePreferences(con, "com.nasageek.utexasutilities.password", false);
        this.settings = PreferenceManager.getDefaultSharedPreferences(con);
    }

    public boolean hasCookieBeenSet() {
        return cookieHasBeenSet;
    }

    public String getPrefKey() {
        return prefKey;
    }

    public String getAuthCookieVal() {
        if (cookieHasBeenSet) {
            return authCookie;
        } else if (settings.contains(prefKey)) {
            cookieHasBeenSet = true;
            authCookie = settings.getString(prefKey, "");
            return authCookie;
        } else {
            try {
                login();
            } catch (IOException ie) {
                ie.printStackTrace();
            }
            return authCookie;
        }
    }

    private void resetCookie() {
        Utility.commit(settings.edit().remove(prefKey));
        authCookie = "";
        cookieHasBeenSet = false;
    }

    public void setAuthCookieVal(String authCookie) {
        this.authCookie = authCookie;
        Utility.commit(settings.edit().putString(prefKey, authCookie));
        cookieHasBeenSet = true;
    }

    public void login() throws IOException {
        String user = settings.getString("eid", "error");
        String pw = secureSettings.getString("password");

        RequestBody requestBody = new FormEncodingBuilder()
                .add(userNameKey, user)
                .add(passwordKey, pw)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        try {
            Response response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        CookieManager cm = (CookieManager) CookieHandler.getDefault();
        List<HttpCookie> cookies = cm.getCookieStore().getCookies();
        for (HttpCookie cookie : cookies) {
            // special case for UTD login since I'm too lazy to subclass it
            String cookieVal = cookie.getValue();
            if (cookie.getName().equals(authCookieKey) && !cookieVal.equals("NONE")) {
                setAuthCookieVal(cookieVal);
                return;
            }
        }
        // do something otherwise
    }

    public void logout() {
        resetCookie();
    }
}

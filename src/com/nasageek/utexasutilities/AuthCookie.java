package com.nasageek.utexasutilities;

import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

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

    protected URL url;


    public AuthCookie(String prefKey, String authCookieKey, String loginUrl, String userNameKey, String passwordKey) {
        this.prefKey = prefKey;
        this.authCookieKey = authCookieKey;
        this.userNameKey = userNameKey;
        this.passwordKey = passwordKey;
        try {
            this.url = new URL(loginUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public boolean hasCookieBeenSet() {
        return cookieHasBeenSet;
    }

    public String getPrefKey() {
        return prefKey;
    }

    public String getAuthCookie(Context con) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(con);

        if (cookieHasBeenSet) {
            return authCookie;
        } else if (settings.contains(prefKey)) {
            cookieHasBeenSet = true;
            authCookie = settings.getString(prefKey, "");
            return authCookie;
        } else {
            try {
                login(con);
            } catch (IOException ie) {
                ie.printStackTrace();
            }
            return authCookie;
        }
    }

    private void resetCookie(Context con) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(con);
        Utility.commit(settings.edit().remove(prefKey));
        authCookie = "";
        cookieHasBeenSet = false;
    }

    public void setAuthCookie(String authCookie) {
        this.cookieHasBeenSet = true;
        this.authCookie = authCookie;
    }

    public void login(final Context con) throws IOException {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(con);
        SecurePreferences sp = new SecurePreferences(con, "com.nasageek.utexasutilities.password", false);

        String user = settings.getString("eid", "error");
        String pw = sp.getString("password");
        String encodedForm =
                userNameKey + "=" + URLEncoder.encode(user, HTTP.UTF_8) + "&" +
                passwordKey + "=" + URLEncoder.encode(pw, HTTP.UTF_8);

        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setFixedLengthStreamingMode(encodedForm.getBytes().length);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setInstanceFollowRedirects(true);
        BufferedOutputStream bos = new BufferedOutputStream(connection.getOutputStream());
        bos.write(encodedForm.getBytes());
        bos.flush();
        bos.close();

        Map<String, List<String>> headers = connection.getHeaderFields();
        List<String> cookies = headers.get("Set-cookie");
        if (cookies == null) {
            Log.e("login", "no cookies headers for " + prefKey);
            return;
        }
        for (String cookie : headers.get("Set-cookie")) {
            // special case for UTD login since I'm too lazy to subclass it
            if (cookie.startsWith(authCookieKey) && !cookie.equals("SC=NONE")) {
                setAuthCookie(cookie.split(";")[0].substring(cookie.indexOf('=') + 1));
                return;
            }
        }
        // do something otherwise
    }

    public void logout(Context con) {
        resetCookie(con);
    }
}

package com.nasageek.utexasutilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static com.nasageek.utexasutilities.UTilitiesApplication.PNA_AUTH_COOKIE_KEY;

/**
 * Created by Chris on 7/5/2014.
 */
public class PnaAuthCookie extends AuthCookie {


    public PnaAuthCookie() {
        super(PNA_AUTH_COOKIE_KEY,
              "AUTHCOOKIE",
              "https://management.pna.utexas.edu/server/graph.cgi",
              "PNACLOGINusername",
              "PNACLOGINpassword");
    }


    /**
     * We have to do a little extra work here because the PNA website ignores future login requests
     * if you've already got an auth cookie. Explicitly delete all PNA-related cookies from the
     * default CookieStore on logout.
     */
    @Override
    public void logout(Context con) {
        super.logout(con);
        try {
            URI loginURI = url.toURI();
            CookieStore cookies = ((CookieManager) CookieHandler.getDefault()).getCookieStore();
            for (HttpCookie cookie : cookies.get(loginURI)) {
                cookies.remove(loginURI, cookie);
            }
        } catch (URISyntaxException|IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public void login(final Context con) throws IOException {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(con);
        SecurePreferences sp = new SecurePreferences(con, "com.nasageek.utexasutilities.password", false);

        String user = settings.getString("eid", "error").trim();
        String pw = sp.getString("password");

        OkHttpClient client = new OkHttpClient();
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

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Bad response code " + response);
        }

        if (response.priorResponse() != null) {
            List<String> cookies = response.priorResponse().headers("Set-Cookie");
            if (cookies == null || cookies.size() == 0) {
                Log.e("login", "no cookies headers for " + prefKey);
                return;
            }
            for (String cookie : cookies) {
                if (cookie.startsWith(authCookieKey)) {
                    setAuthCookieVal(cookie.split(";")[0].substring(cookie.indexOf('=') + 1));
                    return;
                }
            }
        }
    }
}

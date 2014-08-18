package com.nasageek.utexasutilities;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpCookie;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import static com.nasageek.utexasutilities.UTilitiesApplication.PNA_AUTH_COOKIE_KEY;

/**
 * Created by Chris on 7/5/2014.
 */
public class PnaAuthCookie extends AuthCookie {


    public PnaAuthCookie() {
        super(PNA_AUTH_COOKIE_KEY, "AUTHCOOKIE", "https://management.pna.utexas.edu/server/graph.cgi", "PNACLOGINusername", "PNACLOGINpassword");
    }

    public void login(final Context con) throws IOException {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(con);
        SecurePreferences sp = new SecurePreferences(con, "com.nasageek.utexasutilities.password", false);

        String user = settings.getString("eid", "error").trim();
        String pw = sp.getString("password");

        // sticking with the "tried and true" method because HttpsURLConnection is being difficult
        HttpClient client = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("https://management.pna.utexas.edu/server/graph.cgi");
        try {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("PNACLOGINusername", user));
            nameValuePairs.add(new BasicNameValuePair("PNACLOGINpassword", pw));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));

            // Execute HTTP Post Request
            client.execute(httppost);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        for (Cookie cookie : ((DefaultHttpClient)client).getCookieStore().getCookies()) {
            if (cookie.getName().equals("AUTHCOOKIE")) {
                setAuthCookie(cookie.getValue());
                return;
            }
        }

        // TODO: make this work correctly

        /*
        String boundary = "--------ututilitiesboundary";
        String crlf = "\r\n";

        String multipartForm = "--" + boundary + crlf + "Content-Disposition: form-data; name=\"PNACLOGINusername\"\r\n\r\n" + URLEncoder.encode(user, HTTP.UTF_8) + crlf +
                boundary + crlf + "Content-Disposition: form-data; name=\"PNACLOGINpassword\"\r\n\r\n" + URLEncoder.encode(pw, HTTP.UTF_8) + "\r\n" + boundary + "--" + "\r\n";


        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setFixedLengthStreamingMode(multipartForm.getBytes().length);

        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        connection.setRequestProperty("Connection", "keep-alive");
        connection.setInstanceFollowRedirects(true);
        BufferedOutputStream bos = new BufferedOutputStream(connection.getOutputStream());
        bos.write(multipartForm.getBytes());
        bos.flush();
        bos.close();

        //String pagedata = convertStreamToString(new BufferedInputStream(connection.getInputStream()));

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
        */
    }

}

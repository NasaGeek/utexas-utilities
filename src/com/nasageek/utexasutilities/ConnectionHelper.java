
package com.nasageek.utexasutilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.nasageek.utexasutilities.activities.Preferences;
import com.nasageek.utexasutilities.activities.UTilitiesActivity;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ConnectionHelper {

    public static final String BLACKBOARD_DOMAIN_NOPROT = "blackboard.utexas.edu";
    public static final String BLACKBOARD_DOMAIN = "https://" + BLACKBOARD_DOMAIN_NOPROT;

    private static SharedPreferences settings;

    private static boolean utdCookieHasBeenSet = false;
    private static boolean pnaCookieHasBeenSet = false;
    private static boolean bbCookieHasBeenSet = false;

    public static boolean loggingIn = false;

    public static DefaultHttpClient getThreadSafeClient() {

        DefaultHttpClient client = new DefaultHttpClient();
        ClientConnectionManager mgr = client.getConnectionManager();
        HttpParams params = client.getParams();

        client = new DefaultHttpClient(new ThreadSafeClientConnManager(params,
                mgr.getSchemeRegistry()), params);

        return client;
    }

    public static void logout(Context con) {
        settings = PreferenceManager.getDefaultSharedPreferences(con);
        Editor edit = settings.edit();
        getThreadSafeClient().getCookieStore().clear();
        //resetCookies(con);
        edit.putBoolean("loggedin", false);

        Utility.commit(edit);
        loggingIn = false;
    }

    public static boolean isLoggingIn() {
        return loggingIn;
    }

    public static void setUtdAuthCookie(String cookie, Context con) {
        settings = PreferenceManager.getDefaultSharedPreferences(con);
        Utility.commit(settings.edit().putString("utd_auth_cookie", cookie));
        utdCookieHasBeenSet = true;
    }

    public static void setPnaAuthCookie(String cookie, Context con) {
        settings = PreferenceManager.getDefaultSharedPreferences(con);
        Utility.commit(settings.edit().putString("pna_auth_cookie", cookie));
        pnaCookieHasBeenSet = true;
    }

    public static void setBbAuthCookie(String cookie, Context con) {
        settings = PreferenceManager.getDefaultSharedPreferences(con);
        Utility.commit(settings.edit().putString("bb_auth_cookie", cookie));
        bbCookieHasBeenSet = true;
    }
}

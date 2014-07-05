
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
    private static SecurePreferences sp;

    private static String utdAuthCookie;
    private static String pnaAuthCookie;
    private static String bbAuthCookie;

    private static boolean utdCookieHasBeenSet = false;
    private static boolean pnaCookieHasBeenSet = false;
    private static boolean bbCookieHasBeenSet = false;

    public static boolean utdLoginDone = false;
    public static boolean pnaLoginDone = false;
    public static boolean bbLoginDone = false;

    public static boolean loggingIn = false;

    public static DefaultHttpClient getThreadSafeClient() {

        DefaultHttpClient client = new DefaultHttpClient();
        ClientConnectionManager mgr = client.getConnectionManager();
        HttpParams params = client.getParams();

        client = new DefaultHttpClient(new ThreadSafeClientConnManager(params,
                mgr.getSchemeRegistry()), params);

        return client;
    }

    public boolean bbLogin(Context con, DefaultHttpClient client) {
        settings = PreferenceManager.getDefaultSharedPreferences(con);
        sp = new SecurePreferences(con, "com.nasageek.utexasutilities.password", false);
        HttpPost httppost = new HttpPost(ConnectionHelper.BLACKBOARD_DOMAIN + "/webapps/login/");
        try {

            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

            nameValuePairs.add(new BasicNameValuePair("user_id", settings.getString("eid", "error")
                    .trim()));
            nameValuePairs.add(new BasicNameValuePair("password", sp.getString("password")));

            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.ASCII));

            // Execute HTTP Post Request
            client.execute(httppost);

        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    public boolean utdLogin(Context con, DefaultHttpClient client) {
        settings = PreferenceManager.getDefaultSharedPreferences(con);
        sp = new SecurePreferences(con, "com.nasageek.utexasutilities.password", false);
        HttpPost httppost = new HttpPost(
                "https://utdirect.utexas.edu/security-443/logon_check.logonform");
        try {

            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

            nameValuePairs.add(new BasicNameValuePair("LOGON", settings.getString("eid", "error")
                    .trim()));
            nameValuePairs.add(new BasicNameValuePair("PASSWORDS", sp.getString("password")));

            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.ASCII));

            // Execute HTTP Post Request
            client.execute(httppost);

        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    public boolean pnaLogin(Context con, DefaultHttpClient client) {
        settings = PreferenceManager.getDefaultSharedPreferences(con);
        sp = new SecurePreferences(con, "com.nasageek.utexasutilities.password", false);
        HttpPost httppost = new HttpPost("https://management.pna.utexas.edu/server/graph.cgi");
        try {

            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

            nameValuePairs.add(new BasicNameValuePair("PNACLOGINusername", settings.getString(
                    "eid", "error").trim()));
            nameValuePairs
                    .add(new BasicNameValuePair("PNACLOGINpassword", sp.getString("password")));

            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.ASCII));

            // Execute HTTP Post Request
            client.execute(httppost);

        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    public static void logout(Context con) {
        settings = PreferenceManager.getDefaultSharedPreferences(con);
        Editor edit = settings.edit();
        getThreadSafeClient().getCookieStore().clear();
        resetCookies(con);
        edit.putBoolean("loggedin", false);

        Utility.commit(edit);
        loggingIn = false;
    }

    public static boolean isLoggingIn() {
        return loggingIn;
    }

    public static String getPnaAuthCookie(Context con, DefaultHttpClient client) {
        settings = PreferenceManager.getDefaultSharedPreferences(con);

        if (pnaCookieHasBeenSet) {
            return pnaAuthCookie;
        } else if (settings.contains("pna_auth_cookie")) {
            pnaCookieHasBeenSet = true;
            pnaAuthCookie = settings.getString("pna_auth_cookie", "");
            return pnaAuthCookie;
        } else {
            List<Cookie> cooklist = client.getCookieStore().getCookies();

            for (int i = 0; i < cooklist.size(); i++) {
                if (cooklist.get(i).getName().equals("AUTHCOOKIE")) {
                    pnaAuthCookie = cooklist.get(i).getValue();
                    Utility.commit(settings.edit().putString("pna_auth_cookie", pnaAuthCookie));
                    pnaCookieHasBeenSet = true;
                    return pnaAuthCookie;
                }
            }
            Toast.makeText(
                    con,
                    "Something went wrong during login, try checking your UT EID and Password and try again.",
                    Toast.LENGTH_LONG).show();
            resetPnaCookie(con);
            return "";
        }
    }

    public static String getUtdAuthCookie(Context con, DefaultHttpClient client) {
        settings = PreferenceManager.getDefaultSharedPreferences(con);

        if (utdCookieHasBeenSet) {
            return utdAuthCookie;
        } else if (settings.contains("utd_auth_cookie")) {
            utdCookieHasBeenSet = true;
            utdAuthCookie = settings.getString("utd_auth_cookie", "");
            return utdAuthCookie;
        } else {
            List<Cookie> cooklist = client.getCookieStore().getCookies();

            for (int i = 0; i < cooklist.size(); i++) {
                if (cooklist.get(i).getName().equals("SC")
                        && !cooklist.get(i).getValue().equals("NONE")) {
                    utdAuthCookie = cooklist.get(i).getValue();
                    Utility.commit(settings.edit().putString("utd_auth_cookie", utdAuthCookie));
                    utdCookieHasBeenSet = true;
                    return utdAuthCookie;
                }
            }

            Toast.makeText(
                    con,
                    "Something went wrong during login, try checking your UT EID and Password and try again.",
                    Toast.LENGTH_LONG).show();
            resetUtdCookie(con);
            return "";
        }
    }

    public static String getBbAuthCookie(Context con, DefaultHttpClient client) {
        settings = PreferenceManager.getDefaultSharedPreferences(con);

        if (bbCookieHasBeenSet) {
            return bbAuthCookie;
        } else if (settings.contains("bb_auth_cookie")) {
            bbCookieHasBeenSet = true;
            bbAuthCookie = settings.getString("bb_auth_cookie", "");
            return bbAuthCookie;
        } else {
            List<Cookie> cooklist = client.getCookieStore().getCookies();

            for (int i = 0; i < cooklist.size(); i++) {
                if (cooklist.get(i).getName().equals("s_session_id")) {
                    bbAuthCookie = cooklist.get(i).getValue();
                    Utility.commit(settings.edit().putString("bb_auth_cookie", bbAuthCookie));
                    bbCookieHasBeenSet = true;
                    return bbAuthCookie;
                }
            }

            Toast.makeText(
                    con,
                    "Something went wrong during Blackboard login, try checking your UT EID and Password and try again.",
                    Toast.LENGTH_LONG).show();
            resetBbCookie(con);
            return "";
        }
    }

    public static void resetUtdCookie(Context con) {
        settings = PreferenceManager.getDefaultSharedPreferences(con);
        Utility.commit(settings.edit().remove("utd_auth_cookie"));
        utdAuthCookie = "";
        utdCookieHasBeenSet = false;
    }

    public static void resetPnaCookie(Context con) {
        settings = PreferenceManager.getDefaultSharedPreferences(con);
        Utility.commit(settings.edit().remove("pna_auth_cookie"));
        pnaAuthCookie = "";
        pnaCookieHasBeenSet = false;
    }

    public static void resetBbCookie(Context con) {
        settings = PreferenceManager.getDefaultSharedPreferences(con);
        Utility.commit(settings.edit().remove("bb_auth_cookie"));
        bbAuthCookie = "";
        bbCookieHasBeenSet = false;
    }

    public static void resetCookies(Context con) {
        resetUtdCookie(con);
        resetPnaCookie(con);
        resetBbCookie(con);
    }

    public static void setUtdAuthCookie(String cookie, Context con) {
        settings = PreferenceManager.getDefaultSharedPreferences(con);
        Utility.commit(settings.edit().putString("utd_auth_cookie", cookie));
        utdAuthCookie = cookie;
        utdCookieHasBeenSet = true;
    }

    public static void setPnaAuthCookie(String cookie, Context con) {
        settings = PreferenceManager.getDefaultSharedPreferences(con);
        Utility.commit(settings.edit().putString("pna_auth_cookie", cookie));
        pnaAuthCookie = cookie;
        pnaCookieHasBeenSet = true;
    }

    public static void setBbAuthCookie(String cookie, Context con) {
        settings = PreferenceManager.getDefaultSharedPreferences(con);
        Utility.commit(settings.edit().putString("bb_auth_cookie", cookie));
        bbAuthCookie = cookie;
        bbCookieHasBeenSet = true;
    }

    public static boolean utdCookieHasBeenSet() {
        return utdCookieHasBeenSet;
    }

    public static boolean pnaCookieHasBeenSet() {
        return pnaCookieHasBeenSet;
    }

    public static boolean bbCookieHasBeenSet() {
        return bbCookieHasBeenSet;
    }

    public class utdLoginTask extends AsyncTask<Object, Integer, Boolean> implements
            ChangeableContextTask {
        DefaultHttpClient pnahttpclient;
        DefaultHttpClient httpclient;
        DefaultHttpClient bbhttpclient;
        Editor edit;
        Context context;
        private CountDownLatch loginLatch;

        public utdLoginTask(Context con, DefaultHttpClient httpclient,
                DefaultHttpClient pnahttpclient, DefaultHttpClient bbhttpclient,
                CountDownLatch loginLatch) {
            settings = PreferenceManager.getDefaultSharedPreferences(con);
            edit = settings.edit();
            this.httpclient = httpclient;
            this.pnahttpclient = pnahttpclient;
            this.bbhttpclient = bbhttpclient;
            this.context = con;
            this.loginLatch = loginLatch;
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            loggingIn = true;
            boolean loginStatus = ((ConnectionHelper) params[0]).utdLogin(context, httpclient);
            publishProgress(loginStatus ? 0 : 1);
            return loginStatus;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {

            switch (progress[0]) {
                case 1:
                    Toast.makeText(
                            context,
                            "There was an error while connecting to UT's web services, please check your internet connection and try again",
                            Toast.LENGTH_LONG).show();
                    loggingIn = false;
                    ((SherlockActivity) (context)).invalidateOptionsMenu();
                    cancelProgressBar();
                    break;
                case 0:
                    break;
            }
        }

        @Override
        protected void onPostExecute(Boolean b) {
            loginLatch.countDown();
            return;

            /*utdLoginDone = b;

            if (utdLoginDone && pnaLoginDone && bbLoginDone && !isCancelled()) {
                utdLoginDone = false;
                pnaLoginDone = false;
                bbLoginDone = false;
                loggingIn = false;

                if (!ConnectionHelper.getUtdAuthCookie(context, httpclient).equals("")
                        && !ConnectionHelper.getPnaAuthCookie(context, pnahttpclient).equals("")
                        && !ConnectionHelper.getBbAuthCookie(context, bbhttpclient).equals("")) {
                    Toast.makeText(context,
                            "You're now logged in; feel free to access any of the app's features",
                            Toast.LENGTH_LONG).show();

                    edit.putBoolean("loggedin", true);
                    Utility.commit(edit);
                }
                ((SherlockActivity) (context)).invalidateOptionsMenu();
                cancelProgressBar();
            }*/
        }

        @Override
        public void setContext(Context con) {
            context = con;
        }

        // baaaaaddd
        private void cancelProgressBar() {
            if (context.getClass().equals(UTilitiesActivity.class)) {
                ((SherlockActivity) context).setSupportProgressBarIndeterminateVisibility(false);
            } else if (context.getClass().equals(Preferences.class)) {
                ((SherlockPreferenceActivity) context)
                        .setSupportProgressBarIndeterminateVisibility(false);
            }
        }

    }

    public class pnaLoginTask extends AsyncTask<Object, Integer, Boolean> implements
            ChangeableContextTask {
        private DefaultHttpClient pnahttpclient;
        private DefaultHttpClient httpclient;
        private DefaultHttpClient bbhttpclient;
        private Editor edit;
        private Context context;
        private CountDownLatch loginLatch;

        public pnaLoginTask(Context con, DefaultHttpClient httpclient,
                DefaultHttpClient pnahttpclient, DefaultHttpClient bbhttpclient,
                CountDownLatch loginLatch) {
            settings = PreferenceManager.getDefaultSharedPreferences(con);
            edit = settings.edit();
            this.httpclient = httpclient;
            this.pnahttpclient = pnahttpclient;
            this.bbhttpclient = bbhttpclient;
            this.context = con;
            this.loginLatch = loginLatch;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            switch (progress[0]) {
                case 1:
                    loggingIn = false;
                    ((SherlockActivity) (context)).invalidateOptionsMenu();
                    cancelProgressBar();
                    break;
                case 0:
                    break;
            }
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            loggingIn = true;
            boolean pnaLoginStatus = ((ConnectionHelper) params[0])
                    .pnaLogin(context, pnahttpclient);
            publishProgress(pnaLoginStatus ? 0 : 1);
            return pnaLoginStatus;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            loginLatch.countDown();
            return;

            /*pnaLoginDone = b;

            if (utdLoginDone && pnaLoginDone && bbLoginDone && !isCancelled()) {
                utdLoginDone = false;
                pnaLoginDone = false;
                bbLoginDone = false;
                loggingIn = false;

                if (!ConnectionHelper.getUtdAuthCookie(context, httpclient).equals("")
                        && !ConnectionHelper.getPnaAuthCookie(context, pnahttpclient).equals("")
                        && !ConnectionHelper.getBbAuthCookie(context, bbhttpclient).equals("")) {
                    Toast.makeText(context,
                            "You're now logged in; feel free to access any of the app's features",
                            Toast.LENGTH_LONG).show();

                    edit.putBoolean("loggedin", true);
                    Utility.commit(edit);
                }
                ((SherlockActivity) (context)).invalidateOptionsMenu();
                cancelProgressBar();
            }*/
        }

        @Override
        public void setContext(Context con) {
            context = con;
        }

        private void cancelProgressBar() {
            if (context.getClass().equals(UTilitiesActivity.class)) {
                ((SherlockActivity) context).setSupportProgressBarIndeterminateVisibility(false);
            } else if (context.getClass().equals(Preferences.class)) {
                ((SherlockPreferenceActivity) context)
                        .setSupportProgressBarIndeterminateVisibility(false);
            }
        }
    }

    public class bbLoginTask extends AsyncTask<Object, Integer, Boolean> implements
            ChangeableContextTask {
        private DefaultHttpClient pnahttpclient;
        private DefaultHttpClient httpclient;
        private DefaultHttpClient bbhttpclient;
        private Editor edit;
        private Context context;
        private CountDownLatch loginLatch;

        public bbLoginTask(Context con, DefaultHttpClient httpclient,
                DefaultHttpClient pnahttpclient, DefaultHttpClient bbhttpclient,
                CountDownLatch loginLatch) {
            settings = PreferenceManager.getDefaultSharedPreferences(con);
            edit = settings.edit();
            this.httpclient = httpclient;
            this.pnahttpclient = pnahttpclient;
            this.bbhttpclient = bbhttpclient;
            this.loginLatch = loginLatch;
            this.context = con;
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            loggingIn = true;
            boolean loginStatus = ((ConnectionHelper) params[0]).bbLogin(context, bbhttpclient);
            publishProgress(loginStatus ? 0 : 1);
            return loginStatus;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            switch (progress[0]) {
                case 1:
                    loggingIn = false;
                    ((SherlockActivity) (context)).invalidateOptionsMenu();
                    cancelProgressBar();
                    break;
                case 0:
                    break;
            }
        }

        @Override
        protected void onPostExecute(Boolean b) {
            loginLatch.countDown();
            return;

            /*bbLoginDone = b;



            if (utdLoginDone && pnaLoginDone && bbLoginDone && !isCancelled()) {
                utdLoginDone = false;
                pnaLoginDone = false;
                bbLoginDone = false;
                loggingIn = false;

                if (!ConnectionHelper.getUtdAuthCookie(context, httpclient).equals("")
                        && !ConnectionHelper.getPnaAuthCookie(context, pnahttpclient).equals("")
                        && !ConnectionHelper.getBbAuthCookie(context, bbhttpclient).equals("")) {
                    Toast.makeText(context,
                            "You're now logged in; feel free to access any of the app's features",
                            Toast.LENGTH_LONG).show();

                    edit.putBoolean("loggedin", true);
                    Utility.commit(edit);
                }
                ((SherlockActivity) (context)).invalidateOptionsMenu();
                cancelProgressBar();
            }*/
        }

        @Override
        public void setContext(Context con) {
            context = con;
        }

        private void cancelProgressBar() {
            if (context.getClass().equals(UTilitiesActivity.class)) {
                ((SherlockActivity) context).setSupportProgressBarIndeterminateVisibility(false);
            } else if (context.getClass().equals(Preferences.class)) {
                ((SherlockPreferenceActivity) context)
                        .setSupportProgressBarIndeterminateVisibility(false);
            }
        }
    }
}

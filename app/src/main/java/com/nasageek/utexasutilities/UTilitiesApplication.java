
package com.nasageek.utexasutilities;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.UserManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.webkit.CookieSyncManager;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.nasageek.utexasutilities.fragments.UTilitiesPreferenceFragment;
import com.securepreferences.SecurePreferences;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.okhttp.OkHttpClient;
import com.tozny.crypto.android.AesCbcWithIntegrity;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.annotation.ReportsCrashes;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;

@ReportsCrashes(
customReportContent = {
        ReportField.ANDROID_VERSION, ReportField.APP_VERSION_CODE, ReportField.APP_VERSION_NAME,
        ReportField.BRAND, ReportField.BUILD, ReportField.PACKAGE_NAME,
        ReportField.INSTALLATION_ID, ReportField.PHONE_MODEL, ReportField.PRODUCT,
        ReportField.REPORT_ID, ReportField.STACK_TRACE, ReportField.USER_APP_START_DATE,
        ReportField.USER_CRASH_DATE, ReportField.CUSTOM_DATA
}, httpMethod = org.acra.sender.HttpSender.Method.PUT, reportType = org.acra.sender.HttpSender.Type.JSON, formUri = "http://utexasutilities.cloudant.com/acra-utexasutilities/_design/acra-storage/_update/report", formUriBasicAuthLogin = "spereacedidayestallynner", formUriBasicAuthPassword = "UAIwd5vciiGtWOGqsqYMJxnY"

// formUriBasicAuthLogin = "releasereporter",
// formUriBasicAuthPassword = "raebpcorterpxayszsword"

)
public class UTilitiesApplication extends Application {
    private static UTilitiesApplication sInstance;

    public static final String UTD_AUTH_COOKIE_KEY = "utd_auth_cookie";

    private Map<String, AuthCookie> authCookies = new HashMap<>();
    private OkHttpClient client = new OkHttpClient();
    private Map<String, AsyncTask> asyncTaskCache = new HashMap<>();
    private SharedPreferences securePreferences;

    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            try {
                // Attempt to remedy a system memory leak
                UserManager.class.getMethod("get", Context.class).invoke(null, this);
            } catch (Exception e) {
                // Catch Exception because we need API 19 to do multi-catch for the necessary
                // exceptions.
                // Do nothing on failure.
            }
        }
        LeakCanary.install(this);
        ACRA.init(this);
        try {
            AesCbcWithIntegrity.SecretKeys myKey = AesCbcWithIntegrity.generateKeyFromPassword(
                    Utility.id(this), Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID).getBytes(), 10);
            securePreferences = new SecurePreferences(this, myKey, null);
        } catch (GeneralSecurityException gse) {
            // no clue what to do here
            gse.printStackTrace();
            ACRA.getErrorReporter().handleException(gse, false);
        }
        upgradePasswordEncryption();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // UT's servers only support TLS 1.2 now. TLS 1.2 is supported but not enabled by
            // default in Android 4.1 to 5.0. Here we enable it. Unfortunately, this means we have
            // jto drop support for 4.0.
            try {
                SSLContext sc = SSLContext.getInstance("TLSv1.2");
                sc.init(null, null, null);
                client.setSslSocketFactory(new Tls12SocketFactory(sc.getSocketFactory()));
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                e.printStackTrace();
            }
        }

        authCookies.put(UTD_AUTH_COOKIE_KEY, new UtdAuthCookie(this));

        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
        client.setCookieHandler(cookieManager);
        CookieSyncManager.createInstance(this);

        initGoogleAnalytics();
        AnalyticsHandler.initTrackerIfNeeded(this);
    }

    private void upgradePasswordEncryption() {
        LegacySecurePreferences lsp = new LegacySecurePreferences(this,
                UTilitiesPreferenceFragment.OLD_PASSWORD_PREF_FILE, false);
        if (lsp.containsKey("password")) {
            try {
                String password = lsp.getString("password");
                securePreferences.edit().putString("password", password).apply();
            } catch (LegacySecurePreferences.SecurePreferencesException spe) {
                spe.printStackTrace();
                ACRA.getErrorReporter().handleException(spe, false);
                Toast.makeText(this, "Your saved password has been wiped for security purposes" +
                        " and will need to be re-entered just this once", Toast.LENGTH_LONG).show();
            } finally {
                lsp.clear();
            }
        }
    }

    public void initGoogleAnalytics() {
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        analytics.setDryRun(false);
        analytics.setLocalDispatchPeriod(300);
        analytics.enableAutoActivityReports(this);
        // note the negation
        analytics.setAppOptOut(!PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.pref_analytics_key), false));
    }

    public AuthCookie getAuthCookie(String key) {
        return authCookies.get(key);
    }

    public void putAuthCookie(String key, AuthCookie cookie) {
        authCookies.put(key, cookie);
    }

    public boolean allCookiesSet() {
        for (AuthCookie cookie : authCookies.values()) {
            if (!cookie.hasCookieBeenSet()) {
                return false;
            }
        }
        return !authCookies.isEmpty();
    }

    public boolean anyCookiesSet() {
        for (AuthCookie cookie : authCookies.values()) {
            if (cookie.hasCookieBeenSet()) {
                return true;
            }
        }
        return false;
    }

    public String getUtdAuthCookieVal() {
        return authCookies.get(UTD_AUTH_COOKIE_KEY).getAuthCookieVal();
    }

    public void logoutAll() {
        for (AuthCookie authCookie : authCookies.values()) {
            authCookie.logout();
        }
        CookieStore cookies = ((CookieManager) CookieHandler.getDefault()).getCookieStore();
        cookies.removeAll();
    }

    public OkHttpClient getHttpClient() {
        return client;
    }

    public void cacheTask(String tag, AsyncTask task) {
        if (asyncTaskCache.containsKey(tag)) {
            throw new IllegalStateException("Task already cached");
        }
        asyncTaskCache.put(tag, task);
    }

    public AsyncTask getCachedTask(String tag) {
        return asyncTaskCache.get(tag);
    }

    public void removeCachedTask(String tag) {
        asyncTaskCache.remove(tag);
    }

    public SharedPreferences getSecurePreferences() {
        return securePreferences;
    }

    public static UTilitiesApplication getInstance() {
        return sInstance;
    }

    public static UTilitiesApplication getInstance(Context context) {
        return context != null ? (UTilitiesApplication) context.getApplicationContext() : sInstance;
    }
}


package com.nasageek.utexasutilities;

import android.app.Application;
import android.content.Context;
import android.preference.PreferenceManager;

import com.commonsware.cwac.security.trust.TrustManagerBuilder;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.okhttp.OkHttpClient;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.annotation.ReportsCrashes;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
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

    public static final String UTD_AUTH_COOKIE_KEY = "utd_auth_cookie";
    public static final String PNA_AUTH_COOKIE_KEY = "pna_auth_cookie";

    private Map<String, AuthCookie> authCookies;
    private static UTilitiesApplication sInstance;
    private OkHttpClient client;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        LeakCanary.install(this);

        client = new OkHttpClient();
        try {
            TrustManagerBuilder trustManagerBuilder = new TrustManagerBuilder(this);
            trustManagerBuilder.allowCA(R.raw.pna_cert).or().useDefault();
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerBuilder.buildArray(), null);
            client.setSslSocketFactory(sslContext.getSocketFactory());
        } catch (NoSuchAlgorithmException|KeyManagementException|IOException|CertificateException|
                KeyStoreException nsae) {
            nsae.printStackTrace();
        }

        authCookies = new HashMap<>();
        authCookies.put(UTD_AUTH_COOKIE_KEY, new UtdAuthCookie(this));
        authCookies.put(PNA_AUTH_COOKIE_KEY, new PnaAuthCookie(this));

        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);

        ACRA.init(this);
        initGoogleAnalytics();
        AnalyticsHandler.initTrackerIfNeeded(this);
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

    public String getPnaAuthCookieVal() {
        return authCookies.get(PNA_AUTH_COOKIE_KEY).getAuthCookieVal();
    }

    public void logoutAll() {
        for (AuthCookie authCookie : authCookies.values()) {
            authCookie.logout();
        }
    }

    public OkHttpClient getHttpClient() {
        return client;
    }

    public static UTilitiesApplication getInstance() {
        return sInstance;
    }

    public static UTilitiesApplication getInstance(Context context) {
        return context != null ? (UTilitiesApplication) context.getApplicationContext() : sInstance;
    }
}

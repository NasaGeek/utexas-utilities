
package com.nasageek.utexasutilities;

import android.app.Application;

import com.nasageek.utexasutilities.fragments.BlackboardFragment;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.annotation.ReportsCrashes;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.HashMap;
import java.util.Map;

@ReportsCrashes(formKey = "", // This is required for backward compatibility but
// not used
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
    public static final String BB_AUTH_COOKIE_KEY = "bb_auth_cookie";

    private Map<String, AuthCookie> authCookies;

    @Override
    public void onCreate() {
        super.onCreate();
        authCookies = new HashMap<String, AuthCookie>();
        authCookies.put(UTD_AUTH_COOKIE_KEY, new UtdAuthCookie(this));

        authCookies.put(PNA_AUTH_COOKIE_KEY, new PnaAuthCookie(this));

        authCookies.put(BB_AUTH_COOKIE_KEY, new AuthCookie(BB_AUTH_COOKIE_KEY,
                    "s_session_id",
                    BlackboardFragment.BLACKBOARD_DOMAIN + "/webapps/login/",
                    "user_id",
                    "password",
                    this));

        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);

        // The following line triggers the initialization of ACRA
        ACRA.init(this);
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

    public String getBbAuthCookieVal() {
        return authCookies.get(BB_AUTH_COOKIE_KEY).getAuthCookieVal();
    }

    public void logoutAll() {
        for (AuthCookie authCookie : authCookies.values()) {
            authCookie.logout();
        }
    }
}

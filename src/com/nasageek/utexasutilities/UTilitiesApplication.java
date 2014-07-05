
package com.nasageek.utexasutilities;

import android.app.Application;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.annotation.ReportsCrashes;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private Map<String, AuthCookie> authCookies;

    @Override
    public void onCreate() {
        super.onCreate();
        authCookies = new HashMap<String, AuthCookie>();
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


}

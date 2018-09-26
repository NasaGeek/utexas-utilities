package com.nasageek.utexasutilities;

import android.content.Context;

import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Created by chris on 1/23/15.
 */
public class AnalyticsHandler {

    private static FirebaseAnalytics analyticsTracker;

    private AnalyticsHandler() {
        // do nothing
    }

    public static void initTrackerIfNeeded(Context con, boolean enabled) {
        if (analyticsTracker == null) {
            analyticsTracker = FirebaseAnalytics.getInstance(con);
            analyticsTracker.setAnalyticsCollectionEnabled(enabled);
        }
    }

    public static void trackLogoutEvent() {
        analyticsTracker.logEvent("logout", null);
    }

    public static void trackCalendarExportEvent() {
        analyticsTracker.logEvent("export_calendar", null);
    }

    public static void trackMapAllClassesEvent() {
        analyticsTracker.logEvent("map_all_classes", null);
    }

    public static void trackGetDirectionsEvent() {
        analyticsTracker.logEvent("get_directions", null);
    }

    public static void trackBusRouteEvent() {
        analyticsTracker.logEvent("view_bus_route", null);
    }
}

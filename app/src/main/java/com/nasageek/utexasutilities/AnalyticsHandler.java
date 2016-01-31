package com.nasageek.utexasutilities;

import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.nasageek.utexasutilities.activities.CampusMapActivity;
import com.nasageek.utexasutilities.activities.UTilitiesActivity;
import com.nasageek.utexasutilities.fragments.CourseScheduleFragment;

/**
 * Created by chris on 1/23/15.
 */
public class AnalyticsHandler {

    private static Tracker analyticsTracker;

    private AnalyticsHandler() {
        // do nothing
    }

    public static void initTrackerIfNeeded(Context con) {
        if (analyticsTracker == null) {
            analyticsTracker = GoogleAnalytics.getInstance(con)
                    .newTracker(R.xml.analytics_tracker_config);
        }
    }

    public static void trackLogoutEvent() {
        analyticsTracker.send(new HitBuilders.EventBuilder()
                .setCategory(UTilitiesActivity.class.getSimpleName())
                .setAction("Logout")
                .build());
    }

    public static void trackCalendarExportEvent() {
        analyticsTracker.send(new HitBuilders.EventBuilder()
                .setCategory(CourseScheduleFragment.class.getSimpleName())
                .setAction("Export Calendar")
                .build());
    }

    public static void trackMapAllClassesEvent() {
        analyticsTracker.send(new HitBuilders.EventBuilder()
                .setCategory(CourseScheduleFragment.class.getSimpleName())
                .setAction("Map All Classes")
                .build());
    }

    public static void trackGetDirectionsEvent() {
        analyticsTracker.send(new HitBuilders.EventBuilder()
                .setCategory(CampusMapActivity.class.getSimpleName())
                .setAction("Get Directions")
                .build());
    }

    public static void trackBusRouteEvent() {
        analyticsTracker.send(new HitBuilders.EventBuilder()
                .setCategory(CampusMapActivity.class.getSimpleName())
                .setAction("View Bus Route")
                .build());
    }
}

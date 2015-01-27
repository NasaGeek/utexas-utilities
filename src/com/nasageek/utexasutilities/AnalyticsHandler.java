package com.nasageek.utexasutilities;

import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.nasageek.utexasutilities.activities.UTilitiesActivity;

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
}

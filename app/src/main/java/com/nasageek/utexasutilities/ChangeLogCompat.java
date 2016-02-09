package com.nasageek.utexasutilities;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.webkit.WebView;

import de.cketti.library.changelog.ChangeLog;

/**
 * Created by chris on 9/24/15.
 */
public class ChangeLogCompat extends ChangeLog {

    public ChangeLogCompat(Context context) {
        super(context);
    }


    /**
     * Get a dialog with the full change log.
     *
     * @return An AlertDialog with a full change log displayed.
     */
    public AlertDialog getFullLogDialogCompat() {
        return getDialogCompat(true);
    }

    /**
     * Create a dialog containing (parts of the) change log.
     *
     * @param full
     *         If this is {@code true} the full change log is displayed. Otherwise only changes for
     *         versions newer than the last version are displayed.
     *
     * @return A dialog containing the (partial) change log.
     */
    protected AlertDialog getDialogCompat(boolean full) {
        WebView wv = new WebView(mContext);
//        wv.setBackgroundColor(0); // transparent
        wv.loadDataWithBaseURL(null, getLog(full), "text/html", "UTF-8", null);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(
                mContext.getResources().getString(
                        full ? R.string.changelog_full_title : R.string.changelog_title))
                .setView(wv)
                .setCancelable(false)
                        // OK button
                .setPositiveButton(
                        mContext.getResources().getString(R.string.changelog_ok_button),
                        (dialog, which) -> {
                            // The user clicked "OK" so save the current version code as
                            // "last version code".
                            updateVersionInPreferences();
                        });

        if (!full) {
            // Show "More…" button if we're only displaying a partial change log.
            builder.setNegativeButton(R.string.changelog_show_full, (dialog, id) -> { getFullLogDialog().show(); });
        }

        return builder.create();
    }
}

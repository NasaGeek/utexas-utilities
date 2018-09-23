
package com.nasageek.utexasutilities;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import java.io.IOException;

public class BuildingProvider extends ContentProvider {

    private BuildingDatabase bdb;

    public static final Uri CONTENT_URI = Uri
            .parse("content://" + BuildConfig.APPLICATION_ID + ".buildingprovider");

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        bdb.addBuilding(values);
        return null;
    }

    @Override
    public boolean onCreate() {

        // V1 Initial building list
        // V2 Added all garages, Belo, a few dorms I missed. Should be the
        // entire official building list now
        // V3 CLA - Liberal Arts Building
        // V4 POB, GDC - POB is ACES but renamed, leave them both in there
        // V5 Add INT, ESS, CTR, CS3, CS4, CRB, CCF; remove ESB, RAS, RRN;
        //      rename GIA to GIAC

        if (PreferenceManager.getDefaultSharedPreferences(this.getContext()).getInt(
                "buildingdbversion", 1) < 5) {
            if (this.getContext().deleteDatabase("buildings")) {
               PreferenceManager.getDefaultSharedPreferences(this.getContext())
                        .edit().putInt("buildingdbversion", 5).apply();
            }
        }
        bdb = new BuildingDatabase(this.getContext());
        try {
            bdb.createDataBase(false);
        } catch (IOException e) {

            e.printStackTrace();
        }
        bdb.openDataBase();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        return bdb.query("buildings", projection, selectionArgs[0], null, null, null, sortOrder);

    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}

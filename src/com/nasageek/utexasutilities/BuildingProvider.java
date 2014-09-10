
package com.nasageek.utexasutilities;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.net.Uri;
import android.preference.PreferenceManager;

import java.io.IOException;

public class BuildingProvider extends ContentProvider {

    private BuildingDatabase bdb;

    public static final Uri CONTENT_URI = Uri
            .parse("content://com.nasageek.utexasutilities.buildingprovider");

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
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

        if (PreferenceManager.getDefaultSharedPreferences(this.getContext()).getInt(
                "buildingdbversion", 1) < 4) {
            if (this.getContext().deleteDatabase("buildings")) {
               PreferenceManager.getDefaultSharedPreferences(this.getContext())
                        .edit().putInt("buildingdbversion", 4).apply();
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
        SQLiteCursor sqlc = (SQLiteCursor) bdb.query("buildings", projection, selectionArgs[0],
                null, null, null, sortOrder);
        return sqlc;

    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}

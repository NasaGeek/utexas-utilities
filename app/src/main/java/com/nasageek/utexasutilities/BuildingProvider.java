
package com.nasageek.utexasutilities;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;

public class BuildingProvider extends ContentProvider {

    private BuildingDatabase bdb;

    private static final String TABLE_NAME = "buildings";
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
        return null;
    }

    @Override
    public boolean onCreate() {

        bdb = new BuildingDatabase(this.getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        String mySelection = null;
        if (selectionArgs != null) {
            mySelection = selectionArgs[0];
        }
        return bdb.query(TABLE_NAME, projection, mySelection, null, null, null, sortOrder);

    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}

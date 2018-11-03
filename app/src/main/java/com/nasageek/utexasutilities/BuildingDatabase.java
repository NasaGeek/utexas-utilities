
package com.nasageek.utexasutilities;

import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class BuildingDatabase extends SQLiteAssetHelper {

    private static final String DB_NAME = "buildings.db";
    // V1 Initial building list
    // V2 Added all garages, Belo, a few dorms I missed. Should be the
    // entire official building list now
    // V3 CLA - Liberal Arts Building
    // V4 POB, GDC - POB is ACES but renamed, leave them both in there
    // V5 Add INT, ESS, CTR, CS3, CS4, CRB, CCF; remove ESB, RAS, RRN;
    //      rename GIA to GIAC
    // V6 Re-scrape UT buildings off of maps.utexas.edu (see scripts/ dir for more info)
    //      building location info is also stored in database now instead of kml;
    private static final int DB_VERSION = 6;
    private static final String KEY_SUGGEST_COLUMN_TEXT_1 = SearchManager.SUGGEST_COLUMN_TEXT_1;
    private static final String KEY_SUGGEST_COLUMN_TEXT_2 = SearchManager.SUGGEST_COLUMN_TEXT_2;

    public BuildingDatabase(Context con) {
        super(con, DB_NAME, null, DB_VERSION);
        setForcedUpgrade();
    }

    public Cursor query(String tablename, String[] columns, String selection,
            String[] selectionArgs, String groupBy, String having, String orderBy) {
        if (selection != null) {
            SQLiteCursor c = (SQLiteCursor) getReadableDatabase().query(
                    tablename,
                    columns,
                    KEY_SUGGEST_COLUMN_TEXT_1 + " LIKE '%" + selection + "%' OR "
                            + KEY_SUGGEST_COLUMN_TEXT_2 + " LIKE '%" + selection + "%'", selectionArgs,
                    groupBy, having, orderBy);
            c.moveToFirst();
            return c;
        } else {
            return getReadableDatabase().query(tablename, columns, selection, selectionArgs, groupBy, having, orderBy);
        }
    }
}

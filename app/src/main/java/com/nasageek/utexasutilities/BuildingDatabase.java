
package com.nasageek.utexasutilities;

import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BuildingDatabase extends SQLiteOpenHelper {

    private Context mContext;
    static final String DB_NAME = "buildings";
    static final String TABLE_NAME = "buildings";
    private static final String KEY_SUGGEST_COLUMN_TEXT_1 = SearchManager.SUGGEST_COLUMN_TEXT_1;
    private static final String KEY_SUGGEST_COLUMN_TEXT_2 = SearchManager.SUGGEST_COLUMN_TEXT_2;

    public BuildingDatabase(Context con) {
        super(con, DB_NAME, null, 1);
        mContext = con;
    }

    public void createDataBase() throws IOException {
        if (!databaseExists()) {
            try {
                copyDataBase();
                PreferenceManager.getDefaultSharedPreferences(mContext)
                        .edit().putInt("buildingdbversion", 6).apply();
            } catch (IOException e) {
                e.printStackTrace();
                throw new IOException("Error copying building database");
            }
        }
    }

    private void copyDataBase() throws IOException {
        // Open your local db as the input stream
        InputStream myInput = mContext.getAssets().open(DB_NAME);

        // Path to the just created empty db
        String outFileName = mContext.getDatabasePath(DB_NAME).getPath();

        SQLiteDatabase.openOrCreateDatabase(outFileName, null).close();
        // Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        // transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }

        // Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();

    }

    public SQLiteDatabase openDatabase() throws SQLException {
        String dbPath = mContext.getDatabasePath(DB_NAME).getPath();
        return SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);
    }

    /**
     * Check if the database already exist to avoid re-copying the file each
     * time you open the application.
     * 
     * @return true if it exists, false if it doesn't
     */
    private boolean databaseExists() {
        SQLiteDatabase checkDB = null;
        try {
            checkDB = openDatabase();
        } catch (SQLiteException e) {
            // database does't exist yet.
        }
        if (checkDB != null) {
            checkDB.close();
        }
        return checkDB != null;
    }

    @Override
    public void onCreate(SQLiteDatabase db) { }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }

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

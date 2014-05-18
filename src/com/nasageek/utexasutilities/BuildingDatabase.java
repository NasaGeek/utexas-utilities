
package com.nasageek.utexasutilities;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BuildingDatabase extends SQLiteOpenHelper {

    private static final String DB_NAME = "buildings";
    private String dbPath;
    private Context mContext;
    private SQLiteDatabase sqldb;
    private static final String KEY_SUGGEST_COLUMN_TEXT_1 = SearchManager.SUGGEST_COLUMN_TEXT_1;
    private static final String KEY_SUGGEST_COLUMN_TEXT_2 = SearchManager.SUGGEST_COLUMN_TEXT_2;
    private static final String KEY_SUGGEST_COLUMN_INTENT_DATA = SearchManager.SUGGEST_COLUMN_INTENT_DATA;
    private static final String KEY_BUILDINGID = "buildingid";
    private static final String KEY_NAME = "name";
    private static final String TABLE_NAME = "buildings";

    private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " ("
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_SUGGEST_COLUMN_TEXT_1
            + " TEXT NOT NULL, " + KEY_SUGGEST_COLUMN_TEXT_2 + " TEXT NOT NULL, "
            + KEY_SUGGEST_COLUMN_INTENT_DATA + " TEXT NOT NULL);";

    public BuildingDatabase(Context con) {
        super(con, DB_NAME, null, 1);
        mContext = con;
    }

    public void createDataBase(boolean upgrading) throws IOException {

        boolean dbExist = checkDataBase();

        if (dbExist) {
            Log.d("Building DB", "Building db already exists");
            // do nothing - database already exists
        } else {
            // By calling this method an empty database will be created into the
            // default system path
            // of your application so we are gonna be able to overwrite that
            // database with our database.
            this.getReadableDatabase();
            try {
                copyDataBase();
                Log.d("Building DB", "New building DB copied");
            } catch (IOException e) {
                throw new Error("Error copying building database");
            }
        }
    }

    private void copyDataBase() throws IOException {

        // Open your local db as the input stream
        InputStream myInput = mContext.getAssets().open(DB_NAME);

        // Path to the just created empty db
        String outFileName = mContext.getDatabasePath(DB_NAME).getPath();

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

    public void openDataBase() throws SQLException {

        dbPath = mContext.getDatabasePath(DB_NAME).getPath();
        sqldb = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);

    }

    /**
     * Check if the database already exist to avoid re-copying the file each
     * time you open the application.
     * 
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase() {

        SQLiteDatabase checkDB = null;

        try {
            String myPath = mContext.getDatabasePath(DB_NAME).getPath();
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

        } catch (SQLiteException e) {

            // database does't exist yet.

        }

        if (checkDB != null) {
            checkDB.close();
        }

        return checkDB != null ? true : false;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);

    }

    public void addBuilding(ContentValues cal)// String id, String name, String
                                              // lat, String lon)
    {
        new openDbTask(cal).execute();
    }

    public Cursor query(String tablename, String[] columns, String selection,
            String[] selectionArgs, String groupBy, String having, String orderBy) {
        SQLiteCursor c = (SQLiteCursor) getReadableDatabase().query(
                tablename,
                columns,
                KEY_SUGGEST_COLUMN_TEXT_1 + " LIKE '%" + selection + "%' OR "
                        + KEY_SUGGEST_COLUMN_TEXT_2 + " LIKE '%" + selection + "%'", selectionArgs,
                groupBy, having, orderBy);
        c.moveToFirst();
        return c;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /*
         * if(newVersion > oldVersion) { try { createDataBase(true); } catch
         * (IOException e) { e.printStackTrace(); throw new
         * Error("Error copying building database"); } }
         */

    }

    private class openDbTask extends AsyncTask<Object, Void, Object> {
        private ContentValues cv2;

        public openDbTask(ContentValues c) {
            cv2 = c;
        }

        @Override
        protected Object doInBackground(Object... arg0) {
            try {
                sqldb = getWritableDatabase();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            sqldb.insert(TABLE_NAME, null, cv2);
        }
    }

}

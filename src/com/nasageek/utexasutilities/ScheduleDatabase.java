package com.nasageek.utexasutilities;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Schedule database used for schedule caching feature. This is actually REALLY BAD - it stores the
 * ENTIRE class listing page data. It is really bad because I was really lazy and wanted to
 * implement a quick solution. Properly implementing this would require looking into the data
 * formats for classes and adding code to replace the page parsing with database lookups. Not what
 * I'm willing to do at the moment.
 * 
 * @author Andrew Lin
 */
public class ScheduleDatabase extends SQLiteOpenHelper {

	private static String DB_PATH = "/data/data/com.nasageek.utexasutilities/databases/";
	 
	private static String DB_NAME = "schedule";
	private Context mContext;
	private SQLiteDatabase sqldb;
	private static final String KEY_EID = "eid";
	private static final String KEY_SEMESTER = "semid";
	private static final String KEY_PAGEDATA = "pagedata";
	private static final String TABLE_NAME = "schedule";
	
	private static final String TABLE_CREATE =
	        "CREATE TABLE " + TABLE_NAME + " (" +
	        "_id INTEGER PRIMARY KEY AUTOINCREMENT, "+
	        KEY_EID + " TEXT NOT NULL, " +
	        KEY_SEMESTER + " TEXT, " +
			KEY_PAGEDATA + " TEXT NOT NULL);";
	
	private static final String TABLE_WHERE =
			KEY_EID + " = ? AND " + KEY_SEMESTER + " = ?";
	
	public ScheduleDatabase(Context con) {
		super(con, TABLE_NAME, null, 1);
		mContext = con;
		// TODO Auto-generated constructor stub
	}

	
	public void createDataBase(boolean upgrading) throws IOException{
		 
    	
		boolean dbExist = checkDataBase();
 
    	if(dbExist){
    		Log.d("Schedule DB","Schedule db already exists");
    		//do nothing - database already exist
    	}else{
 
    			//By calling this method and empty database will be created into the default system path
               //of your application so we are gonna be able to overwrite that database with our database.
    			this.getReadableDatabase();
 
        	try {
 
    			copyDataBase();
    			Log.d("Schedule DB","New schedule DB copied");
 
    		} catch (IOException e) {
 
        		throw new Error("Error copying schedule database");
 
        	}
    	}
 
    }
	private void copyDataBase() throws IOException{
		 
    	//Open your local db as the input stream
    	InputStream myInput = mContext.getAssets().open(DB_NAME);
 
    	// Path to the just created empty db
    	String outFileName = DB_PATH + DB_NAME;
 
    	//Open the empty db as the output stream
    	OutputStream myOutput = new FileOutputStream(outFileName);
 
    	//transfer bytes from the inputfile to the outputfile
    	byte[] buffer = new byte[1024];
    	int length;
    	while ((length = myInput.read(buffer))>0){
    		myOutput.write(buffer, 0, length);
    	}
 
    	//Close the streams
    	myOutput.flush();
    	myOutput.close();
    	myInput.close();
 
    }
	 public void openDataBase() throws SQLException{
		 
	    	//Open the database
	        String myPath = DB_PATH + DB_NAME;
	    	sqldb = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
	    	Log.d("SchedDB", "Schedule DB opened");
	 
	    }
	 /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase(){
 
    	SQLiteDatabase checkDB = null;
 
    	try{
    		String myPath = DB_PATH + DB_NAME;
    		checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
 
    	}catch(SQLiteException e){
 
    		//database does't exist yet.
 
    	}
 
    	if(checkDB != null){
 
    		checkDB.close();
 
    	}
 
    	return checkDB != null ? true : false;
    }
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(TABLE_CREATE);
		Log.d("SchedDB", "Schedule DB created");

	}

	public void addSchedule(String eid, String semid, String pagedata)
	{
		SQLiteDatabase db = getWritableDatabase();
		ContentValues val = new ContentValues();
		
		val.put(KEY_EID, eid);
		val.put(KEY_SEMESTER, semid);
		val.put(KEY_PAGEDATA, pagedata);
		
		db.delete(TABLE_NAME, TABLE_WHERE, new String[] {eid, semid});
		db.insert(TABLE_NAME, null, val);
		db.close();
		
		Log.d("SchedDB", "Inserted schedule " + semid + " for " + eid + " into schedule DB");
	}
	
	public String getSchedule(String eid, String semid) {
		SQLiteCursor c = (SQLiteCursor) getReadableDatabase().rawQuery("SELECT " + KEY_PAGEDATA + 
				" FROM " + TABLE_NAME + " WHERE " + TABLE_WHERE, new String[] {eid, semid});
		
		boolean hasRow = c.moveToFirst();
		
		if (hasRow) {
			String ret = c.getString(0);
			c.close();
			
			Log.d("SchedDB", "Retrieved schedule " + semid + " for " + eid + " from schedule DB");
			
			return ret;
		} else {
			Log.d("SchedDB", "Attempted to retrieve schedule " + semid + " for " + eid + " from schedule DB, but no schedule exists");
			return null;
		}
	}
	
	public Cursor query(String tablename, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy)
	{
		SQLiteCursor c =  (SQLiteCursor) getReadableDatabase().query(tablename, columns, selection, selectionArgs, groupBy, having, orderBy);
		c.moveToFirst();
		return c;
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	/*	if(newVersion > oldVersion)
		{	
			try {
				createDataBase(true);
			} catch (IOException e) {
				
				e.printStackTrace();
				throw new Error("Error copying building database");
			}
		}*/

	}
	
	public void deleteTable()
	{
		SQLiteDatabase sqldb = getWritableDatabase();
		try
		{
			sqldb.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
			Log.d("SchedDB", "Schedule DB table deleted");
		}
		catch(SQLException ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			sqldb.close();
		}

	}
	
	public void createTable() {
		SQLiteDatabase sqldb = getWritableDatabase();
		
		try
		{
			sqldb.execSQL(TABLE_CREATE);
			Log.d("SchedDB", "Schedule DB table created");
		}
		catch(SQLException ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			sqldb.close();
		}
	}

}

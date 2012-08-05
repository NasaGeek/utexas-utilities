package com.nasageek.utexasutilities;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;

public class BuildingDatabase extends SQLiteOpenHelper {

	private static String DB_PATH = "/data/data/com.nasageek.utexasutilities/databases/";
	 
	private static String DB_NAME = "buildings";
	private Context mContext;
	private SQLiteDatabase sqldb;
	private static final String KEY_SUGGEST_COLUMN_TEXT_1 = SearchManager.SUGGEST_COLUMN_TEXT_1;
	private static final String KEY_SUGGEST_COLUMN_TEXT_2 = SearchManager.SUGGEST_COLUMN_TEXT_2;
	private static final String KEY_SUGGEST_COLUMN_INTENT_DATA = SearchManager.SUGGEST_COLUMN_INTENT_DATA;
	private static final String KEY_BUILDINGID = "buildingid";
	private static final String KEY_NAME = "name";
	private static final String TABLE_NAME = "buildings";
	
	private static final String TABLE_CREATE =
	        "CREATE TABLE " + TABLE_NAME + " (" +
	        "_id INTEGER PRIMARY KEY AUTOINCREMENT, "+
	        KEY_SUGGEST_COLUMN_TEXT_1 + " TEXT NOT NULL, " +		
	        KEY_SUGGEST_COLUMN_TEXT_2 + " TEXT NOT NULL, " +
	        KEY_SUGGEST_COLUMN_INTENT_DATA + " TEXT NOT NULL);";
	
	public BuildingDatabase(Context con) {
		super(con, TABLE_NAME, null, 1);
		mContext = con;
		// TODO Auto-generated constructor stub
	}

	
	public void createDataBase() throws IOException{
		 
    	boolean dbExist = checkDataBase();
 
    	if(dbExist){
    		//do nothing - database already exist
    	}else{
 
    		//By calling this method and empty database will be created into the default system path
               //of your application so we are gonna be able to overwrite that database with our database.
        	this.getReadableDatabase();
 
        	try {
 
    			copyDataBase();
 
    		} catch (IOException e) {
 
        		throw new Error("Error copying database");
 
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

	}

	public void addBuilding(ContentValues cal)//String id, String name, String lat, String lon)
	{
		new openDbTask(cal).execute();
	
	}
	public Cursor query(String tablename, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy)
	{
		
		SQLiteCursor c =  (SQLiteCursor) getReadableDatabase().query(tablename, columns,KEY_SUGGEST_COLUMN_TEXT_1+" LIKE '%"+selection+"%' OR "+KEY_SUGGEST_COLUMN_TEXT_2+" LIKE '%"+selection+"%'",selectionArgs, groupBy, having,orderBy);
		c.moveToFirst();
		 return c;
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}
	private class openDbTask extends AsyncTask
	{
		private ContentValues cv2;
		public openDbTask(ContentValues c)
		{
			cv2=c;
		}
		@Override
		protected Object doInBackground(Object... arg0)
		{
			// TODO Auto-generated method stub
			try
			{
				sqldb = getWritableDatabase();
			} catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		protected void onPostExecute(Object result)
		{
			sqldb.insert(TABLE_NAME, null, cv2);
		}
	}

}

package com.nasageek.utexasutilities;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;

public class ClassDatabase extends SQLiteOpenHelper
{
	private Context context;
//	private String[] colors = {"00b060","ff4500","ff9200","793a8c","06799f","ff5d40","a6b900"};
	private String[] colors = {"ffe45e","ff866e","b56eb3","488ab0","00b060","94c6ff","81b941"};
	private double oldH=0; 
	private int count;
	private SQLiteDatabase sqldb;
	private static final String KEY_EID = "eid";
	private static final String KEY_UNIQUEID = "uniqueid";
	private static final String KEY_ID = "id";
	private static final String KEY_NAME = "name";
	private static final String KEY_BUILDING = "building";
	private static final String KEY_ROOM = "room";
	private static final String KEY_DAY = "day";
	private static final String KEY_START = "start";
	private static final String KEY_END = "end";
	private static final String KEY_COLOR = "color";
	private static final String KEY_SEMESTER = "semester";
	
	private static final String TABLE_NAME = "classes";
	private static final String TABLE_CREATE =
        "CREATE TABLE " + TABLE_NAME + " (" +
        "_id INTEGER PRIMARY KEY AUTOINCREMENT, "+
        KEY_EID + " TEXT NOT NULL, " +		
        KEY_UNIQUEID + " TEXT NOT NULL, " +
        KEY_ID + " TEXT NOT NULL, " +
        KEY_NAME + " TEXT NOT NULL, " +
		KEY_BUILDING + " TEXT NOT NULL, " +
		KEY_ROOM + " TEXT NOT NULL, " +
		KEY_DAY + " TEXT NOT NULL, " +
		KEY_START + " TEXT NOT NULL, " +
		KEY_END + " TEXT NOT NULL, " +
		KEY_COLOR + " TEXT NOT NULL, " +
		KEY_SEMESTER + " TEXT NOT NULL);";
	
	
	public ClassDatabase(Context con)
	{
		super(con, TABLE_NAME, null, 1);
		context = con;
		count=0;
	}
	@Override
	public void onCreate(SQLiteDatabase db)
	{
		// TODO Auto-generated method stub
		db.execSQL(TABLE_CREATE);
		Log.d("DBCREATE", "new db created");
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		// TODO Auto-generated method stub

	}
	
	public void addClass(UTClass cl)
	{
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		ContentValues val = null;
		SQLiteDatabase sqldb=getWritableDatabase();
		
		String colorhex = colors[count++];
	
		for(int k = 0; k<cl.getClassTimes().size(); k++)
		{
			val = new ContentValues();
			val.put(KEY_EID, sp.getString("eid", "eid_missing"));
			val.put(KEY_UNIQUEID, cl.getUnique());
			val.put(KEY_ID, cl.getId());
			val.put(KEY_NAME, cl.getName());
			val.put(KEY_BUILDING, cl.getClassTimes().get(k).getBuilding().getId());
			val.put(KEY_ROOM, cl.getClassTimes().get(k).getBuilding().getRoom());
			val.put(KEY_DAY, cl.getClassTimes().get(k).getDay()+"");
			val.put(KEY_START, cl.getClassTimes().get(k).getStartTime());
			val.put(KEY_END, cl.getClassTimes().get(k).getEndTime());
			val.put(KEY_COLOR, colorhex);
			val.put(KEY_SEMESTER, cl.getSemId());
			
			sqldb.insert(TABLE_NAME, null, val);
		}
		sqldb.close();
	}
	public void resetColorCount()
	{
		count = 0;
	}
	public String getColor(String unique, String start, String day)
	{
		SQLiteDatabase sqldb = getReadableDatabase();
		Cursor cur = sqldb.rawQuery("SELECT " +KEY_COLOR+" FROM "+TABLE_NAME+" WHERE "+KEY_UNIQUEID+" = "+"\""+unique+"\" AND "+KEY_DAY+" = "+"\""+day+"\" AND "+KEY_START+" = "+"\""+start+"\"", null);
		cur.moveToFirst();
		String color = cur.getString(0);
		cur.close();
		sqldb.close();
		
		return color;
	}

	@Override
	public void close()
	{
		super.close();
	//	sqldb.close();
	}
	public int size()
	{
		
		SQLiteDatabase sqldb = getReadableDatabase();
		Cursor cur = sqldb.query(TABLE_NAME, null, null, null, null, null, null);
		
		
		int size = cur.getCount();
		cur.close();
		sqldb.close();
		
		return size;
	}
	public void deleteDb()
	{
		
		SQLiteDatabase sqldb = getWritableDatabase();

		sqldb.execSQL("DROP TABLE "+TABLE_NAME);
		sqldb.execSQL(TABLE_CREATE);
		sqldb.close();
	}

}

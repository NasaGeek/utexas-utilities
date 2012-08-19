package com.nasageek.utexasutilities;

import java.io.IOException;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.net.Uri;

public class BuildingProvider extends ContentProvider {

	
	private BuildingDatabase bdb;
	
	public static final Uri CONTENT_URI = 
            Uri.parse("content://com.nasageek.utexasutilities.buildingprovider");
	
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		
		bdb.addBuilding(values);
		return null;
	}

	@Override
	public boolean onCreate() {
		
		bdb = new BuildingDatabase(this.getContext());
		try
		{
			bdb.createDataBase(false);
		} catch (IOException e)
		{
			
			e.printStackTrace();
		}
		bdb.openDataBase();
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		SQLiteCursor sqlc = (SQLiteCursor) bdb.query("buildings",projection, selectionArgs[0],null, null, null, sortOrder);
		return sqlc;
		
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}
}

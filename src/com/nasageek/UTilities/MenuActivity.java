package com.nasageek.UTilities;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MenuActivity extends Activity {
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu_layout);
		ListView lv = (ListView) findViewById(R.id.dining_location_list);
		ArrayList dining_locations = new ArrayList();
	//	lv.setAdapter(new ArrayAdapter());
	}
	

}

package com.nasageek.utexasutilities;


import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.markupartist.android.widget.ActionBar.Action;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nasageek.utexasutilities.R;

import com.nasageek.utexasutilities.R;

public class libraryMapsShowImages extends SherlockActivity {

	private ActionBar actionbar;
	private int position;
	private String[] locations, title;
	private int[] images;
	private int pos;
	private TextView textview;
	private ImageView imageview;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitleColor(getResources().getColor(R.color.snow2));
		actionbar = getSupportActionBar();
		Bundle bundle = getIntent().getExtras();
		images = bundle.getIntArray("images");
		title = bundle.getStringArray("title");
		position = bundle.getInt("position");
		locations = bundle.getStringArray("locations");

		setContentView(R.layout.librarymap);

		textview = (TextView) findViewById(R.id.libraryMapHeader);
		imageview = (ImageView) findViewById(R.id.mapimage);
		imageview.setImageResource(images[pos]);
		textview.setText(title[pos]);
//		Button next = (Button) findViewById(R.id.nextMapButton);
//		Button prev = (Button) findViewById(R.id.prevMapButton);

		actionbar.setTitle(locations[position]);
		// code downloaded from
		// https://github.com/johannilsson/android-actionbar/blob/master/README.md
	/*	ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setHomeAction(new IntentAction(this, new Intent(this,WelcomeScreen.class), R.drawable.icon)); // go	// home
		actionBar.addAction(new IntentAction(this, new Intent(this, settings.class), R.drawable.ic_menu_preferences)); //go to settings
		actionBar.addAction(new Action(){
			@Override
			public int getDrawable() {
				return R.drawable.previous;
			}
			@Override
			public void performAction(View view) {
				if (pos[0] > 0)
				pos[0]--;
			imageview.setImageResource(images[pos[0]]);
			textview.setText(title[pos[0]]);
			}
		});
		actionBar.addAction(new Action(){
			@Override
			public int getDrawable() {
				return R.drawable.next;
			}
			@Override
			public void performAction(View view) {
				if (pos[0] < images.length - 1)
					pos[0]++;
				imageview.setImageResource(images[pos[0]]);
				textview.setText(title[pos[0]]);
			}
		});*/
		//----------------------

	}
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = this.getSupportMenuInflater();
        inflater.inflate(R.menu.library_maps_show_images, menu);

        return true;
    }
	
    @Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		
		MenuItem nextItem = menu.findItem(R.id.next_map_btn);
		MenuItem prevItem = menu.findItem(R.id.previous_map_btn);
		
		if(pos == 0)
			prevItem.setIcon(0);
		else
			prevItem.setIcon(R.drawable.ic_previous);
		
		if(pos == images.length - 1)
			nextItem.setIcon(0);
		else
			nextItem.setIcon(R.drawable.ic_next);
    	
		return true;
	}
	@Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	int id = item.getItemId();
    	switch(id)
    	{
    		case R.id.previous_map_btn:
    			if (pos > 0)
    				pos--;

    			imageview.setImageResource(images[pos]);
    			textview.setText(title[pos]);
    			invalidateOptionsMenu();
    			break;
    		case R.id.next_map_btn:
    			if (pos < images.length - 1)
    				pos++;
    				
				imageview.setImageResource(images[pos]);
				textview.setText(title[pos]);
				invalidateOptionsMenu();
				break;
    	}
    	return true;
    }
}

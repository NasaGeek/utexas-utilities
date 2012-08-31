package com.nasageek.utexasutilities.libraries;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.IntentAction;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class libraryMapsShowImages extends Activity {
	/** Called when the activity is first created. */
	@SuppressWarnings("unused")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitleColor(getResources().getColor(R.color.snow2));

		Bundle bundle = getIntent().getExtras();
		final int[] images = bundle.getIntArray("images");
		final String [] title = bundle.getStringArray("title");
		int position = bundle.getInt("position");
		String [] locations = bundle.getStringArray("locations");

	try{
		final int[] pos = new int[1]; // need array because it is a mutable
		// final object. int and Integer are
		// immutable.
		setContentView(R.layout.librarymap);

		final TextView textview = (TextView) findViewById(R.id.libraryMapHeader);
		final ImageView imageview = (ImageView) findViewById(R.id.mapimage);
		imageview.setImageResource(images[pos[0]]);
		textview.setText(title[pos[0]]);
//		Button next = (Button) findViewById(R.id.nextMapButton);
//		Button prev = (Button) findViewById(R.id.prevMapButton);

		// code downloaded from
		// https://github.com/johannilsson/android-actionbar/blob/master/README.md
		ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setHomeAction(new IntentAction(this, new Intent(this,WelcomeScreen.class), R.drawable.home)); // go	// home
		actionBar.setTitle(locations[position]);
		actionBar.addAction(new IntentAction(this, new Intent(this, settings.class), R.drawable.gear)); //go to settings
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
		});
		//----------------------
	}
		catch(Exception e)
		{
			if (shared.LOGGINGLEVEL>0) Log.e("libraryMapsShowImages", "exception in onCreate",e);
		}
	}
}

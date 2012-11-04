package com.nasageek.utexasutilities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;




import com.nasageek.utexasutilities.R;

public class libraryMaps extends Activity {

	String []locations;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitleColor(getResources().getColor(R.color.snow2));

		String[] locations = { "PCL Map", "PCL Locations Guide",
				"Architecture Library Maps",
				"Benson Latin American Collection Map",
				"Benson Latin American Collection Locations Guide",
				"Chemistry (Mallet) Library Map",
				"Chemistry (Mallet) Library Locations Guide",
				"Classics Library Map", "Classics Library Locations Guide",
				"Engineering (McKinney) Library Map",
				"Engineering (McKinney) Library Map Location Guide",
				"Fine Arts Library Map", "Fine Arts Library Location Guide",
				"Geology (Walter) Library Map", "Life Science Library Map",
				"Life Science Library Location Guide",
				"Kuehne Physics Mathematics Astronomy Library (PMA) Map",
				"Kuehne Physics Mathematics Astronomy Library (PMA) Locaton Guide" };
		this.locations = locations;
		setContentView(R.layout.main2);

		// code downloaded from
		// https://github.com/johannilsson/android-actionbar/blob/master/README.md
	/*	ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setHomeAction(new IntentAction(this, new Intent(this,WelcomeScreen.class), R.drawable.icon)); // go	// home
		actionBar.setTitle("Library Maps");
		actionBar.addAction(new IntentAction(this, new Intent(this, settings.class), R.drawable.ic_menu_preferences)); //go to settings*/
		//----------------------

		ListView listview = (ListView) findViewById(R.id.mainPageListView);
		listview.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, locations));

		listview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				launchView(position);

			}
		});
	}

	public void launchView(int position) {

		switch (position) {
		case 0:
			final int[] images = { R.drawable.pcl_01, R.drawable.pcl_02,
					R.drawable.pcl_03, R.drawable.pcl_04, R.drawable.pcl_05,
					R.drawable.pcl_06 };
			final String[] title = { "1st Floor", "2nd Floor", "3rd Floor",
					"4th Floor", "5th Floor", "6th Floor" };
			showImages(images, title, position);
			break;
		case 1:
			final int[] images1 = { R.drawable.pcl_book1, R.drawable.pcl_book2,
					R.drawable.pcl_book3, R.drawable.pcl_book4,
					R.drawable.pcl_book5, R.drawable.pcl_book6,
					R.drawable.pcl_book7 };
			final String[] title1 = { "Call Nos: A-GV", "Call Nos: H-HX",
					"Call Nos: J-PK", "Call Nos: PL-ZA", "Dewey Decimal #s",
					"Oversize Materials", "Special Materials" };
			showImages(images1, title1, position);
			break;
		case 2:
			final int[] images2 = { R.drawable.apl_0 };
			final String[] title2 = { "Archit. Planning Lib. Map" };
			showImages(images2, title2, position);
			break;
		case 3:
			final int[] images3 = { R.drawable.blac_01, R.drawable.blac_02,
					R.drawable.blac_03, R.drawable.blac_04, };
			final String[] title3 = { "1st Floor", "2nd Floor", "3rd Floor",
					"4th Floor" };
			showImages(images3, title3, position);
			break;
		case 4:
			final int[] images4 = { R.drawable.blac_loc1, R.drawable.blac_loc2 };
			final String[] title4 = { "Library of Congress", "Other" };
			showImages(images4, title4, position);
			break;
		case 5:
			final int[] images5 = { R.drawable.chemistry_01 };
			final String[] title5 = { "1st Floor" };
			showImages(images5, title5, position);
			break;
		case 6:
			final int[] images6 = { R.drawable.chemistry_loc1 };
			final String[] title6 = { "Chem. Loc. Guide" };
			showImages(images6, title6, position);
			break;
		case 7:
			final int[] images7 = { R.drawable.classics_01 };
			final String[] title7 = { "1st Floor" };
			showImages(images7, title7, position);
			break;
		case 8:
			final int[] images8 = { R.drawable.classics_loc1,
					R.drawable.classics_loc2 };
			final String[] title8 = { "Locations Guide", "Locations Guide Cont" };
			showImages(images8, title8, position);
			break;
		case 9:
			final int[] images9 = { R.drawable.engineering_01,
					R.drawable.engineering_02 };
			final String[] title9 = { "1st Floor", "Mezzanine Level" };
			showImages(images9, title9, position);
			break;
		case 10:
			final int[] images10 = { R.drawable.engineering_loc1 };
			final String[] title10 = { "Locations Guide" };
			showImages(images10, title10, position);
			break;
		case 11:
			final int[] images11 = { R.drawable.finearts_03,
					R.drawable.finearts_04, R.drawable.finearts_05 };
			final String[] title11 = { "3rd Floor", "4th Floor", "5th Floor" };
			showImages(images11, title11, position);
			break;
		case 12:
			final int[] images12 = { R.drawable.finearts_loc03,
					R.drawable.finearts_loc04, R.drawable.finearts_loc05 };
			final String[] title12 = { "3rd Floor Guide", "4th Floor Guide",
					"5th Floor Guide" };
			showImages(images12, title12, position);
			break;
		case 13:
			final int[] images13 = { R.drawable.geology };
			final String[] title13 = { "Geology 1st Floor Guide" };
			showImages(images13, title13, position);
			break;

		case 14:
			final int[] images16 = { R.drawable.finearts_03,
					R.drawable.finearts_04, R.drawable.finearts_05 };
			final String[] title16 = { "3rd Floor", "4th Floor", "5th Floor" };
			showImages(images16, title16, position);
			break;
		case 15:
			final int[] images17 = { R.drawable.finearts_loc03,
					R.drawable.finearts_loc04, R.drawable.finearts_loc05 };
			final String[] title17 = { "3rd Floor Guide", "4th Floor Guide",
					"5th Floor Guide" };
			showImages(images17, title17, position);
			break;
		case 16:
			final int[] images14 = { R.drawable.pma_01, R.drawable.pma_02 };
			final String[] title14 = { "Main Level", "Upper Level" };
			showImages(images14, title14, position);
			break;
		case 17:
			final int[] images15 = { R.drawable.pma_loc };
			final String[] title15 = { "Stacks Guide" };
			showImages(images15, title15, position);
			break;

		}
	}

	public void showImages(final int[] images, final String[] title, int position) {

		Bundle bundle = new Bundle();
		bundle.putIntArray("images", images);
		bundle.putStringArray("title",title);
		bundle.putInt("position", position);
		bundle.putStringArray("locations", locations);

		Intent intent = new Intent(this, libraryMapsShowImages.class);
		intent.putExtras(bundle);
		startActivity(intent);



	}
}

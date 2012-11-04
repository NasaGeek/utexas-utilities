package com.nasageek.utexasutilities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.nasageek.utexasutilities.R;

public class searchInputScreen_advanced extends Activity {

//	public void onGoButtonClicked(View v) {
//
//		if (!shared.connectedToInternet)
//		{
//			int duration = Toast.LENGTH_SHORT;
//			Toast toast = Toast.makeText(context, "This feature requires an internet connection. Please try again later.", duration);
//			toast.show();
//			return;
//		}
//
////		TextView ttest = new TextView(this);
//		SearchData fieldsData = new SearchData();
//
//		EditText getEditText = (EditText) findViewById(R.id.searchstringInput);
//		fieldsData.searchString = getEditText.getText().toString();
//
//		getEditText = (EditText) findViewById(R.id.publisherInput);
//		CheckBox checkbox = (CheckBox) findViewById(R.id.publisherbox);
//		fieldsData.usePublisher = checkbox.isChecked();
//		fieldsData.publisher= getEditText.getText().toString();
//
//		getEditText = (EditText) findViewById(R.id.yearstartInput);
//		checkbox = (CheckBox) findViewById(R.id.yearstartbox);
//		fieldsData.useYearStart = checkbox.isChecked();
//		if(fieldsData.useYearStart)fieldsData.yearStart= Integer.parseInt(getEditText.getText().toString());
//
//		getEditText = (EditText) findViewById(R.id.yearendInput);
//		checkbox = (CheckBox) findViewById(R.id.yearendbox);
//		fieldsData.useYearEnd = checkbox.isChecked();
//		if (fieldsData.useYearEnd)fieldsData.yearEnd = Integer.parseInt(getEditText.getText().toString());
//
//		//input from Spinners
//		Spinner spinner = (Spinner) findViewById(R.id.fieldtypeInput);
//		fieldsData.fieldType = spinner.getSelectedItemPosition();
//		spinner = (Spinner) findViewById(R.id.locationInput);
//		fieldsData.location = spinner.getSelectedItemPosition();
//		spinner = (Spinner) findViewById(R.id.searchandsortInput);
//		fieldsData.searchAndSort = spinner.getSelectedItemPosition();
//
//
//		fieldsData.language = languageType;
//		fieldsData.materialType = materialType;
//		fieldsData.langLength = languageType.length;
//		fieldsData.matLength = materialType.length;
//
//		checkbox = (CheckBox) findViewById(R.id.onlyavailablebox);
//		fieldsData.limitAvailable = checkbox.isChecked();
//
//		//    	ttest.setText(fieldsData.searchString+"\t"+fieldsData.yearStart+"\t"+fieldsData.yearEnd);
//		//    	setContentView(ttest);
//
//		Intent intent = new Intent(this, displaySearchResults.class);
//		Bundle bundle = new Bundle();
//		bundle.putParcelable("fieldsData", fieldsData);
//		intent.putExtras(bundle);
//		startActivity(intent);
//	}
//
//	public void chooseMaterialType(View view){
//		showDialog(0);
//	}
//	public void chooseLanguage(View view){
//		showDialog(1);
//	}
//
//	private DialogInterface.OnMultiChoiceClickListener MaterialTypeListener = new DialogInterface.OnMultiChoiceClickListener() {
//		public void onClick(DialogInterface dialog, int item, boolean isChecked) {
//			materialType[item] = isChecked;
//		}
//	};
//	private DialogInterface.OnMultiChoiceClickListener LanguageListener = new DialogInterface.OnMultiChoiceClickListener() {
//		public void onClick(DialogInterface dialog, int item, boolean isChecked) {
//			languageType[item] = isChecked;
//		}
//	};

//	@Override
//	protected Dialog onCreateDialog(int id) {
//		switch (id) {
//
//		case 0:
//			AlertDialog.Builder ald = new AlertDialog.Builder(this);
//			ald.setTitle("Please Enter Material Type");
//			ald.setMultiChoiceItems(R.array.MaterialTypeTable, materialType,MaterialTypeListener);
//			ald.setPositiveButton("Done", new OnClickListener(){
//				public void onClick(DialogInterface dialog, int which) {
//					dialog.dismiss();
//				}
//			});
//			return ald.create();
//		case 1:
//			AlertDialog.Builder ald2 = new AlertDialog.Builder(this);
//			ald2.setTitle("Please Enter Language");
//			ald2.setMultiChoiceItems(R.array.LanguageTable,languageType,LanguageListener);
//			ald2.setPositiveButton("Done", new OnClickListener(){
//				public void onClick(DialogInterface dialog, int which) {
//					dialog.dismiss();
//				}
//			});
//			return ald2.create();
//
//		}
//		return null;
//	}

	Context context;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		setTitleColor(getResources().getColor(R.color.snow2));

		if (!shared.connectedToInternet)
		{
			int duration = Toast.LENGTH_LONG;
			Toast toast = Toast.makeText(this, "This feature requires an internet connection. Please try again later.", duration);
			toast.show();
		}

//		Resources res = getResources();
//		materialType = new boolean [res.getStringArray(R.array.MaterialTypeTable).length];
//		languageType = new boolean [res.getStringArray(R.array.LanguageTable).length];
//		materialType[0] = languageType[0] = true; //setting anylanguage and anymaterialtype true by default


		//      TextView ttest = new TextView(this);
		setContentView(R.layout.advanced_search_input);

	}
}


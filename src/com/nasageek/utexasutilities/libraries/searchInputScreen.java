package com.nasageek.utexasutilities.libraries;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.IntentAction;

public class searchInputScreen extends Activity {

	Context context;
	LayoutInflater mInflater;
	int searchMetaType;

	boolean[] materialType;
	boolean[] languageType;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		setTitleColor(getResources().getColor(R.color.snow2));

		mInflater = LayoutInflater.from(context);
//		View current;

		if (!shared.connectedToInternet)
		{
			int duration = Toast.LENGTH_LONG;
			Toast toast = Toast.makeText(this, "This feature requires an internet connection. Please try again later.", duration);
			toast.show();
		}


		//      TextView ttest = new TextView(this);
		setContentView(R.layout.search_input_header);

		final FrameLayout frame = (FrameLayout) findViewById(R.id.frame);
		frame.bringToFront();
		searchTypeSpinnerSelected(0,frame);

		//code downloaded from https://github.com/johannilsson/android-actionbar/blob/master/README.md
		ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("Catalog Search");
		actionBar.setHomeAction(new IntentAction(this,new Intent(this, WelcomeScreen.class) , R.drawable.home)); //go home (already there)
		actionBar.addAction(new IntentAction(this, new Intent(this, settings.class), R.drawable.gear)); //go to settings


		Spinner searchType = (Spinner) findViewById(R.id.searchInputSpinner);
		searchType.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				searchTypeSpinnerSelected(position, frame);
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}});

		Resources res = getResources();
		materialType = new boolean [res.getStringArray(R.array.MaterialTypeTable).length];
		languageType = new boolean [res.getStringArray(R.array.LanguageTable).length];
		materialType[0] = languageType[0] = true; //setting anylanguage and anymaterialtype true by default
	}

	public void searchTypeSpinnerSelected(int position, FrameLayout frame)
	{
		//				if (current!=null) removeView(current);
//		int count = frame.getChildCount();
		frame.removeAllViews();
//		View child;
//		//really bad way to do it, need to do with visibility
//		for (int i=0;i<count;i++)
//		{
//			 child = frame.getChildAt(i);
//			 frame.rem
//			if(child!=null)
//				child.setVisibility(View.GONE);
//		}
		View child = null;
		switch(position){
		case SearchData.Advanced:
			child = mInflater.inflate(R.layout.advanced_search_input, frame, true); break;

		case SearchData.Simple:
			child = mInflater.inflate(R.layout.search_input_simple, frame,true);break;
		}
		searchMetaType = position;
//		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//		imm.showSoftInputFromInputMethod(child.getWindowToken(), 0);
//		child.bringToFront();
//		child.setFocusable(true);
//		EditText input = (EditText) child.findViewById(R.id.searchstringInput);
//		input.forceLayout();
//		input.bringToFront();
//		frame.setFocusable(true);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {

		case 0:
			AlertDialog.Builder ald = new AlertDialog.Builder(this);
			ald.setTitle("Please Enter Material Type");
			ald.setMultiChoiceItems(R.array.MaterialTypeTable, materialType,MaterialTypeListener);
			ald.setPositiveButton("Done", new OnClickListener(){
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			return ald.create();
		case 1:
			AlertDialog.Builder ald2 = new AlertDialog.Builder(this);
			ald2.setTitle("Please Enter Language");
			ald2.setMultiChoiceItems(R.array.LanguageTable,languageType,LanguageListener);
			ald2.setPositiveButton("Done", new OnClickListener(){
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			return ald2.create();

		}
		return null;
	}

	public void onGoButtonClicked(View v) {

		if (!shared.connectedToInternet)
		{
			int duration = Toast.LENGTH_SHORT;
			Toast toast = Toast.makeText(context, "This feature requires an internet connection. Please try again later.", duration);
			toast.show();
			return;
		}

		SearchData fieldsData = new SearchData();
		Spinner spinner;
		CheckBox checkbox;
		int smt = searchMetaType; //for easier typing in if statements

		//fields in all search types:
		fieldsData.metaFieldType = searchMetaType;
		EditText getEditText = (EditText) findViewById(R.id.searchstringInput);
		fieldsData.searchString = getEditText.getText().toString();
		spinner = (Spinner) findViewById(R.id.locationInput);
		fieldsData.location = spinner.getSelectedItemPosition();

			//overwritten in advanced
		fieldsData.language = new boolean[1];
		fieldsData.materialType = new boolean[1];
		fieldsData.language[0] = fieldsData.materialType[0] = true;
		fieldsData.langLength = 1;
		fieldsData.matLength = 1;
		//----------------------

		if (smt==SearchData.Advanced)//inputs for advanced search
		{
			getEditText = (EditText) findViewById(R.id.publisherInput);
			checkbox = (CheckBox) findViewById(R.id.publisherbox);
			fieldsData.usePublisher = checkbox.isChecked();
			fieldsData.publisher= getEditText.getText().toString();

			getEditText = (EditText) findViewById(R.id.yearstartInput);
			checkbox = (CheckBox) findViewById(R.id.yearstartbox);
			fieldsData.useYearStart = checkbox.isChecked();
			if(fieldsData.useYearStart)fieldsData.yearStart= Integer.parseInt(getEditText.getText().toString());

			getEditText = (EditText) findViewById(R.id.yearendInput);
			checkbox = (CheckBox) findViewById(R.id.yearendbox);
			fieldsData.useYearEnd = checkbox.isChecked();
			if (fieldsData.useYearEnd)fieldsData.yearEnd = Integer.parseInt(getEditText.getText().toString());

			spinner = (Spinner) findViewById(R.id.searchandsortInput);
			fieldsData.searchAndSort = spinner.getSelectedItemPosition();

			checkbox = (CheckBox) findViewById(R.id.onlyavailablebox);
			fieldsData.limitAvailable = checkbox.isChecked();

			fieldsData.language = languageType;
			fieldsData.materialType = materialType;
			fieldsData.langLength = languageType.length;
			fieldsData.matLength = materialType.length;

			spinner = (Spinner) findViewById(R.id.fieldtypeInput);
			fieldsData.fieldType = spinner.getSelectedItemPosition();
		}
		else if (smt == SearchData.Numbers)
		{
			spinner = (Spinner) findViewById(R.id.searchandsortInput);
			fieldsData.searchAndSort = spinner.getSelectedItemPosition();

			spinner = (Spinner) findViewById(R.id.numbertypeInput);
			fieldsData.fieldType = spinner.getSelectedItemPosition();
		}
		else if (smt == SearchData.Simple)
		{

		}

		Intent intent = new Intent(this, displaySearchResults.class);
		Bundle bundle = new Bundle();
		bundle.putParcelable("fieldsData", fieldsData);
		intent.putExtras(bundle);
		startActivity(intent);
	}

	public void chooseMaterialType(View view){
		showDialog(0);
	}
	public void chooseLanguage(View view){
		showDialog(1);
	}

	private DialogInterface.OnMultiChoiceClickListener MaterialTypeListener = new DialogInterface.OnMultiChoiceClickListener() {
		public void onClick(DialogInterface dialog, int item, boolean isChecked) {
			materialType[item] = isChecked;
		}
	};
	private DialogInterface.OnMultiChoiceClickListener LanguageListener = new DialogInterface.OnMultiChoiceClickListener() {
		public void onClick(DialogInterface dialog, int item, boolean isChecked) {
			languageType[item] = isChecked;
		}
	};
}
package com.nasageek.utexasutilities.libraries;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ListView;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.IntentAction;

public class saveBooks extends Activity {

	ArrayList<Book> saved = new ArrayList<Book>();
	sBookBaseAdapter adapter;
	int oldSize = 0; //size of saved, enables monitoring of book deletions
	Context context;
	boolean activityRunning = false; //to break out of threads.
	Handler handler;

	// retrieve books from file
	//returns true if there are books saved, false otherwise
	@SuppressWarnings({ "unchecked", "unused" })
	public boolean retrieveSavedBooks(){
		try{
			FileInputStream fileIn = context.openFileInput("Saved_Books");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			//			if (fileIn.read()!=-1) //if file has at least 1 object
			//			{
			Object nextObject = in.readObject();
			if (nextObject!=null && nextObject instanceof ArrayList<?>){
				if (shared.LOGGINGLEVEL>0) Log.i("saveBooks", nextObject.toString() + "\n" + nextObject.getClass());
				saved = (ArrayList<Book>)nextObject;
				if (shared.LOGGINGLEVEL>0) Log.i("saveBooks", saved.toString());

				oldSize = saved.size();
			}
			else
			{
				//TODO fix this, have some sort of view for when no books saved, display then
				//				TextView ttest = new TextView(context);
				//				ttest.setText("Sorry. You have not saved any books yet");
				//				setContentView(ttest);
				return false;
			}
			in.close();
			fileIn.close();
			//add an else to display "no saved books"
		}
		catch(java.io.FileNotFoundException e)
		{
//			TextView ttest = new TextView(context);
//			ttest.setText("Sorry. You have not saved any books yet");
//			setContentView(ttest);
			return false;
		}
		catch(Exception e)
		{
			if (shared.LOGGINGLEVEL>0) Log.e("saveBooks", "exception in retrieveSavedBooks", e);
			return false;
		}
		return saved.size()>0;
	}

	private class displaySavedBooks implements Runnable{

		@SuppressWarnings("unused")
		@Override
		// display retrieved books
		public void run() {
			// to do: prettify
			// add display if no results
			String FILENAME = "Saved_Books";
			File file = context.getFileStreamPath(FILENAME);
			boolean booksSaved;
			if(file.exists()){
				 booksSaved = retrieveSavedBooks();
			}
			else {
				 booksSaved = false;
			}
			final boolean finalBooksSaved = booksSaved;
				try {
					handler.post(new Runnable(){
						public void run() {
							if (finalBooksSaved){
								adapter=new sBookBaseAdapter(context, saved);
								ListView listview = (ListView) findViewById(R.id.savedBooksListView);
								listview.setAdapter(adapter);
								dialog.cancel();
							}
							else{
								//TODO: dialog when no saved books
								final AlertDialog.Builder builder = new AlertDialog.Builder(context);
								builder.setMessage("You have no saved books.").setCancelable(true)
								.setNeutralButton("OK", new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										dialog.dismiss();
										Intent intent = new Intent(context,WelcomeScreen.class);
										startActivity(intent);
									}
								}
								);
								final AlertDialog alert = builder.create();
								dialog.dismiss();
								alert.show();

							}
						}
					});

				} catch (Exception e) {
					if (shared.LOGGINGLEVEL>0) Log.e("saveBooks", "Exception in displaySavedBooks", e);
				}
			}
		}



	// rewrite the file (so to remove books that were deleted)
	@SuppressWarnings("unused")
	public void updateSaveFile() {
		try {
			String FILENAME = "Saved_Books";
			File file = context.getFileStreamPath(FILENAME);
			if(file.exists()){
				FileOutputStream fos = null;
				ObjectOutputStream oos = null;
				fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
				oos = new ObjectOutputStream(fos);
				for (Book b : saved) {
					oos.writeObject(b);
					if (shared.LOGGINGLEVEL>0) Log.i("saveBooks", "saved Book title: " + b.title);
				}
				oos.close();
				fos.close();
			}
			else return;
		} catch (Exception e) {
			if (shared.LOGGINGLEVEL>0) Log.e("saveBooks", "exception updateSaveFile: ", e);
		}

	}

	//new thread that continuosly checks sBookBaseAdapter to see if its array has changed. If it has, rewrite the file
	//with new version of book array
	private class checkArrayChanged implements Runnable{

		@SuppressWarnings("unused")
		@Override
		public void run() {
			while(activityRunning)
			{
				if (oldSize!=saved.size())
				{
					if (shared.LOGGINGLEVEL>0) Log.i("saveBooks", "array changed. oldSize: " + oldSize + ". new Array: " + saved.toString());
					//overwrite saved book file
					try{
						String FILENAME = "Saved_Books";
						FileOutputStream fos = null;
						ObjectOutputStream oos = null;

						fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
						oos = new ObjectOutputStream(fos);
						oos.writeObject(saved);
						oldSize = saved.size();
						oos.close();
						fos.close();
					}
					catch(Exception e)
					{
						if (shared.LOGGINGLEVEL>0) Log.e("saveBooks", "exception in checkArrayChanged: ",e);
					}
				}
			}
		}
	}

	public void onPause()
	{
		super.onPause();
		activityRunning = false;
	}
	public void onResume()
	{
		super.onResume();
		activityRunning = true;
	}
	ProgressDialog dialog;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		setTitleColor(getResources().getColor(R.color.snow2));

		dialog = new ProgressDialog(this,R.style.CustomDialog);
		dialog.setMessage("Loading. Please wait...");
		dialog.show();
//		dialog = ProgressDialog.show(this, "", "Loading. Please wait...", true);

		setContentView(R.layout.saved_books);
		handler = new Handler();
		activityRunning = true;

		//code downloaded from https://github.com/johannilsson/android-actionbar/blob/master/README.md
		ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		// You can also assign the title programmatically by passing a CharSequence or resource id.
		actionBar.setTitle("Saved Books");
		actionBar.setHomeAction(new IntentAction(context, new Intent(context,
				WelcomeScreen.class), R.drawable.home)); // go	// home
		actionBar.addAction(new IntentAction(context, new Intent(context, settings.class), R.drawable.gear)); //go to settings
		//----------------------

		//		if(booksSaved){
		(new Thread(new displaySavedBooks())).start();
		(new Thread(new checkArrayChanged())).start();
		//		}
	}
}

package com.nasageek.utexasutilities;

import java.util.ArrayList;

import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ListView;




import com.nasageek.utexasutilities.R;

public class renewBooks extends Activity {

	DefaultHttpClient client;
	ArrayList<cBook> cbooks;
	ProgressDialog dialog;
	Context context;
	Handler handler;

	private class displayCheckedOutBooksThread implements Runnable{
		@SuppressWarnings("unused")
		@Override
		public void run() {
			// log into UT library account
			client = new DefaultHttpClient();
			shared.logIntoCatalog(context,client);
			// get HTML for page
			String html = shared.retrieveProtectedWebPage(context,client,
			"https://catalog.lib.utexas.edu/patroninfo~S29/1160546/items");
			if (shared.LOGGINGLEVEL>0) Log.i("renewBooks", "html: " + html);

			// store HTML into some sort of structure (with links and all)
			cbooks = new ArrayList<cBook>();
			cbooks = parseCheckedOut.parseCheckedOutBooks(html);
			if (shared.LOGGINGLEVEL>0) Log.i("renewBooks", "checked out books: " + cbooks.toString());

//			Looper.prepare();
			handler.post(new Runnable(){
				public void run() {
					if (cbooks.size()==0)
					{

						//		View view = findViewById(R.id.searchResultsLinearLayout);
						final AlertDialog.Builder builder = new AlertDialog.Builder(context);
						builder.setMessage("You have no books checked out. What kind of student are you?").setCancelable(true)
						.setNeutralButton("OK", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.dismiss();
							}
						}
						);
						final AlertDialog alert = builder.create();
						dialog.dismiss();
						alert.show();
					}
					dialog.dismiss();
					// actually display books (use listview);
					ListView listview = (ListView) findViewById(R.id.checkedOutListView);
					listview.setAdapter(new cBookBaseAdapter(context, cbooks));
				}
			});
		}
	}
	@SuppressWarnings("unused")
	public void renewMarkedBooks(View view) {
		try {

			for (int i = 0; i < cbooks.size(); i++)
				if (shared.LOGGINGLEVEL>0) Log.i("renewBooks", "book: " + cbooks.get(i).title
						+ " renew?: " + cbooks.get(i).renew);

			HttpPost httppost = new HttpPost(
			"https://catalog.lib.utexas.edu/patroninfo~S29/1160546/items");
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

			//renew books whose values are marked
			for (int i = 0; i < cbooks.size(); i++) {
				cBook b = cbooks.get(i);
				if (b.renew)
					nameValuePairs.add(new BasicNameValuePair("renew" + i,
							b.renewValue));

			}
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,
					HTTP.ASCII));
			HttpResponse response = client.execute(httppost);
			// update listview after renewing books
			(new Thread(new displayCheckedOutBooksThread())).start();
		} catch (Exception e) {
			if (shared.LOGGINGLEVEL>0) Log.e("renewBooks",
					"exception in renewMarkedBooks: ",e);
		}
	}
	public void renewAllBooks(View view)
	{
		for (int i = 0; i < cbooks.size(); i++) {
			cBook b = cbooks.get(i);
			b.renew = true;
		}
		renewMarkedBooks(view);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// set Content view
		setContentView(R.layout.renew_books);
		context = this;
		handler = new Handler();
		setTitleColor(getResources().getColor(R.color.snow2));

		// code downloaded from
		// https://github.com/johannilsson/android-actionbar/blob/master/README.md
	/*	ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("Checked Out Books");
		actionBar.setHomeAction(new IntentAction(context, new Intent(context,
				WelcomeScreen.class), R.drawable.icon)); // go	// home
		actionBar.addAction(new IntentAction(context, new Intent(context,
				settings.class), R.drawable.ic_menu_preferences)); // go to	// settings*/

		// ----------------------
		
			dialog = new ProgressDialog(this,R.style.CustomDialog);
			dialog.setMessage("Loading. Please wait...");
			dialog.show();
//			dialog = ProgressDialog.show(this, "", "Loading. Please wait...", true);
			(new Thread(new displayCheckedOutBooksThread())).start();
		
	}

}

package com.nasageek.utexasutilities.libraries;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import org.apache.http.client.methods.HttpGet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.IntentAction;

public class displaySearchResults extends Activity {

	int qSize = 51;
	String[] entries = new String[qSize];
	int entriesBeg = 0;
	int entriesEnd = 0;
	boolean allResultsQed = false;
	boolean allResultsParsed = false;
	boolean pageLoading = false;
	int resultsPerPage = 5;
	TextView header;

	@SuppressWarnings("unused")
	private Uri buildURIfromData(SearchData data) {
		try {

			//TODO: also fix sorting for numbers

			Uri.Builder build = new Uri.Builder();
			build.scheme("http");
			String buildpath = "//catalog.lib.utexas.edu/search/";
			// Search String and Field Type
			String toSearch = data.searchString;
			String searchKey="";

			if (data.metaFieldType == SearchData.Advanced)
			{
				buildpath +="X";
				if (data.fieldType != 0)
					toSearch = getResources().getStringArray(
							R.array.fieldtypeValues)[data.fieldType]
							                         + "(" + toSearch + ")";
				searchKey = "SEARCH";
				build.appendQueryParameter(searchKey, toSearch);

				if (!data.materialType[0]){
					for (int i=1;i<data.materialType.length;i++)
					{
						if (data.materialType[i])
							build.appendQueryParameter("m", getResources().getStringArray(R.array.materialtypeValues)[i]);
					}
				}
				if (!data.language[0]){
					for (int i=1;i<data.language.length;i++)
					{
						if (data.language[i])
							build.appendQueryParameter("l", getResources().getStringArray(R.array.langValues)[i]);
					}
				}
				build.appendQueryParameter("SORT", getResources().getStringArray(R.array.searchandsortvalues)[data.searchAndSort]);
			}
			else if (data.metaFieldType == SearchData.Numbers)
			{

				//TODO for numbers: display search results differently (results either shows list of books in a
				//table or immediately shows book details)

				switch(data.fieldType)
				{
				case 0: buildpath+='c';break;
				case 1: buildpath+='h';break;
				case 2: buildpath+='e';break;
				case 3: buildpath+='g';break;
				case 4: buildpath+='i';break;
				case 5: buildpath+='o';break;
				}
				searchKey = "SEARCH";
				build.appendQueryParameter(searchKey, toSearch);

				// Year Start, Year Start, 2 associated checkboxes
				if (data.useYearStart)
					build.appendQueryParameter("Da", "" + data.yearStart);
				if (data.useYearEnd)
					build.appendQueryParameter("Db", "" + data.yearEnd);
				// limit to available items
				if (data.limitAvailable)
					build.appendQueryParameter("availlim", "1");
				// publisher
				if (data.usePublisher)
					build.appendQueryParameter("p", data.publisher);


			}
			else if (data.metaFieldType == SearchData.Simple)
			{
				//simple -> http://catalog.lib.utexas.edu/search/?searchtype=X&SORT=D&searcharg=test&searchscope=29
				searchKey = "searcharg";
				build.appendQueryParameter("searchtype", "X");
				build.appendQueryParameter("SORT", "D");
				build.appendQueryParameter(searchKey, toSearch);

			}

			build.path(buildpath);
			// Location
			if (data.location != 0)
				build.appendQueryParameter(
						"searchscope",
						getResources()
						.getStringArray(R.array.searchscopeValues)[data.location]);

			Uri uri = build.build();
			if (shared.LOGGINGLEVEL>0) Log.i("displaySearchResults", "uri built from data: " + uri.toString());
			return uri;

		} catch (Exception e) {
			if (shared.LOGGINGLEVEL>0) Log.e("displaySearchResults",
					"Exception in buildURIfromData: " + e.toString(),e);
			return null;
		}
	}

	boolean startedParseThread = false;
	boolean shownFirst = false;

	// to do: implement locks in both runnable classes (or not needed)
	private class getURIdata implements Runnable {

		URL libraryURL;

		public getURIdata(URL url) {
			libraryURL = url;
		}

		@SuppressWarnings("unused")
		public void run() {
			pageLoading = true;
			String catalogHTML = "";
			int numfound = 0;
			while (activityRunning) {

				try {
					if (libraryURL == null) {
						pageLoading = false;
						allResultsQed = true;
						return;
					}

					while (currentViewNumEnd + resultsPerPage * 10 < allBooks
							.size()) {
					}

					BufferedReader in = new BufferedReader(
							new InputStreamReader(libraryURL.openStream()));
					boolean first = true;
					String temp = "";
					while (temp != null) {
						if (!activityRunning)
							finish();
						if (temp.contains("briefcitEntryNum")) {
							if (!first) {
								int oldSize = allBooks.size();
								ArrayList<Book> tempBooks = parseResults4
								.extractBooks(catalogHTML);
								numfound += tempBooks.size();
								for (int i = 0; i < tempBooks.size(); i++)
									tempBooks.get(i).numberinorder = i
									+ oldSize + 1;
								allBooks.addAll(tempBooks);
								if (shared.LOGGINGLEVEL>0) Log.i("displaySearchResults",
										"" + allBooks.size());
								if (allBooks.size() >= resultsPerPage
										&& !shownFirst) {
									displayResults(0);
									shownFirst = true;
								}
							}
							// if first time that seeing "briefcitEntryNum",
							// then it is at the top of the page - need to
							// extract
							// num results and next url from top
							else {
								first = false;
								parseResults4.parsePage(catalogHTML); // parse
								// top

								if (shared.LOGGINGLEVEL>0) Log.i("displaySearchResults",
										"parsed page again, book size currently:"
										+ allBooks.size()
										+ "\n next page url:"
										+ parseResults4.nextPageUrl);
								// of page
								// for
								// number of
								// results,
								// link to
								// next page
							}
							catalogHTML = "";
						}

						catalogHTML += temp + "\n";
						temp = in.readLine();
					}
					// for last entry (there is not briefEntryNum after it so is
					// not extracted in the loop)
					int oldSize = allBooks.size();
					ArrayList<Book> tempBooks = parseResults4
					.extractBooks(catalogHTML);
					numfound += tempBooks.size();
					// now do this in BookBaseAdapter
					for (int i = 0; i < tempBooks.size(); i++)
						tempBooks.get(i).numberinorder = i + oldSize + 1;
					allBooks.addAll(tempBooks);
					catalogHTML = "";

					if (allBooks.size()==0)
					{
						allResultsQed=true;
						pageLoading = false;
						handleZeroResults();
						return;
					}
					else if (!shownFirst) {
						displayResults(0);
						shownFirst = true;
					}

					in.close();
					if (parseResults4.nextPageUrl != null) {
						libraryURL = new URL(parseResults4.nextPageUrl);
						parseResults4.nextPageUrl = null;
					} else
						libraryURL = null;
					if (shared.LOGGINGLEVEL>0) Log.i("displaySearchResults", "next page URL:" + libraryURL);

				} catch (Exception e) {
					handler.post(new Runnable(){
						@Override
						public void run() {
							Toast toast = Toast
							.makeText(
									context,
									"Could not load web data. Please check network connection and try again later.",
									Toast.LENGTH_SHORT);
							toast.show();
							// TODO Auto-generated method stub

						}
					});
					if (shared.LOGGINGLEVEL>0) Log.e("displaySearchResults", "Exception in getURIdata",e);
					return;
				}
			}
		}
	}

	public void handleZeroResults()
	{

		//		Looper.prepare();
		handler.post(new Runnable() {
			@Override
			public void run() {
				//		View view = findViewById(R.id.searchResultsLinearLayout);
				final AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setMessage("Sorry, no results were found. Please modify search parameters and try again.").setCancelable(true)
				.setNeutralButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
						Intent intent = new Intent(context, searchInputScreen.class);
						startActivity(intent);
					}
				}
				);
				final AlertDialog alert = builder.create();
				dialog.cancel(); //loading dialog
				alert.show();
			}
		});
	}
	ArrayList<Book> allBooks = new ArrayList<Book>();
	int currentViewNumStart = 0; // start index of books currently displayed
	int currentViewNumEnd = 0; // end index of books currently displayed

	@SuppressWarnings("unused")
	public void displayResults(int start) {
		// to do: prettify
		// add display if no results

		try {
			if (start < 0)
				start = 0;
			// handler.post(new Runnable(){
			// @Override
			// public void run() {
			// // TODO Auto-generated method stub
			// dialog = ProgressDialog.show(context, "",
			// "Loading. Please wait...", true);
			// }
			// });

			if (shared.LOGGINGLEVEL>0) Log.i("displaySearchResults", "inside display Results");
			if (start < allBooks.size()) {
				while (start + resultsPerPage > allBooks.size()
						&& (!allResultsQed) && shownFirst) {
					if (shared.LOGGINGLEVEL>0) Log.i("displaySearchResult",
							"waiting for all to load. start: " + start + " resultsPerPage: " + resultsPerPage + "allBooks.size: "
							+ allBooks.size() + " allResultsQed: "
							+ allResultsQed + " shown First: "
							+ shownFirst);
					// wait till next page all loaded, or all results have
					// loaded. not for first results
					// because that check occurs elsewhere
				}
				int end = (int) Math.min(start + resultsPerPage,
						allBooks.size());
				currentViewNumStart = start;
				currentViewNumEnd = end;
				if (shared.LOGGINGLEVEL>0) Log.i("displaySearchResults",
				"inside display Results if statement");

				listViewData.clear();
				for (int i = start; i < end; i++)
					listViewData.add(allBooks.get(i));
				//				Looper.prepare();
				handler.post(new Runnable() {
					@Override
					public void run() {
						dialog.cancel();
						actionBar.setTitle("Search Results: "+ String.format("%d-%d/%d",
								currentViewNumStart + 1, currentViewNumEnd,
								parseResults4.numResults));

						// header.setText(String.format("%d-%d/%d",
						// currentViewNumStart + 1, currentViewNumEnd,
						// parseResults4.numResults));
						if (shared.LOGGINGLEVEL>0) Log.i("displaSearchResults", "displayResults: inside handler");
						ListView listview = (ListView) findViewById(R.id.searchResultsListView5);
						listview.setAdapter(new BookBaseAdapter(context,
								listViewData));
					}
				});
				// bookAdapter.notifyDataSetChanged();
				if (shared.LOGGINGLEVEL>0) Log.i("displaySearchResults",
						"set adaptor for display layout with arraylist size"
						+ listViewData.size());

			}

		} catch (Exception e) {
			if (shared.LOGGINGLEVEL>0) Log.e("displaySearchResults", "Exception in displayResults: ", e);
		}
	}

	private class nextPage implements Action {
		@Override
		public int getDrawable() {
			return R.drawable.next; // need to replace with
			// next icon
		}

		@SuppressWarnings("unused")
		@Override
		public void performAction(View view) {
			try {
				if (currentViewNumEnd < allBooks.size()
						&& allBooks.get(currentViewNumEnd).bookDetails.size() != 0) {
					Book b = allBooks.get(currentViewNumEnd);
					if (shared.LOGGINGLEVEL>0) Log.i("displaySearchResults", "Book already has detail:"
							+ b.title + " " + b.bookDetails.toString());
				} else if (currentViewNumEnd < allBooks.size()
						&& allBooks.get(currentViewNumEnd).bookDetails.size() == 0) {
					// new Thread(new fetchBookDetail(currentViewNumEnd, //not
					// fetching details right now - slows down code too much
					// currentViewNumEnd + resultsPerPage)).start();
				} else
					if (shared.LOGGINGLEVEL>0) Log.i("displaySearchResults", "book out of range");

			} catch (Exception e) {
				if (shared.LOGGINGLEVEL>0) Log.i("displaySearchResults",
						"Exception in nextPage:" + e.toString());
			}
			displayResults(currentViewNumEnd);
		}
	}

	private class prevPage implements Action {
		public int getDrawable() {
			return R.drawable.previous; // need to replace with
			// prev icon
		}

		public void performAction(View view) {
			displayResults(currentViewNumStart - resultsPerPage);
		}
	}

	public void toSearchInput(View view) {
		Intent intent = new Intent(this, searchInputScreen.class);
		startActivity(intent);
	}

	private class fetchBookDetail implements Runnable {

		int start;
		int end;

		public fetchBookDetail(int start1, int end1) {
			this.start = start1;
			this.end = end1;
		}

		@SuppressWarnings("unused")
		public void run() {
			// if (shared.LOGGINGLEVEL>0) Log.i("displaySearchResults","inside run method for fetching book details");
			try {
				for (int i = start; i < end; i++) {
					if (!activityRunning)
						finish();
					// if (shared.LOGGINGLEVEL>0) Log.i("displaySearchResults","inside for loop for fetching book details");

					if (i < allBooks.size()) {
						Book b = allBooks.get(i);

						if (b.bookDetails.size() == 0) {

							// URL detailURL = new URL (b.detailURL);
							String HTML = shared.getHTMLfromURL(b.detailURL);
							parseResults4.parseBookDetails(HTML, b);
							if (shared.LOGGINGLEVEL>0) Log.i("displaySearchResults",
									"fetched book details for book:" + b.title);
						}
					}
				}
			} catch (Exception e) {
				if (shared.LOGGINGLEVEL>0) Log.i("displaySearchResults", "Exception in fetchBookDetail"
						+ e.toString());
			}
		}
	}

	@SuppressWarnings("unused")
	public void displayBookDetail(int position) {
		try {
			Book b = allBooks.get(position + currentViewNumStart);
			String HTML = shared.getHTMLfromURL(b.detailURL);
			if (b.detailURL != null && b.detailURL.length() > 0
					&& b.bookDetails.size() == 0)
				parseResults4.parseBookDetails(HTML, b);
			if (b.bookDetails.size() > 0) {
				setContentView(R.layout.book_detail_layout2);
				LayoutInflater mInflater = LayoutInflater.from(this);

				View view = mInflater.inflate(R.layout.book_detail_layout2,
						null);
				// TableLayout detailsTable = (TableLayout)
				// view.findViewById(R.id.detailsTable);
				LinearLayout detailsTable = (LinearLayout) view
				.findViewById(R.id.detailsTable2);
				detailsTable.removeAllViews();
				// setContentView(detailsTable);
				for (String key : b.bookDetails.keySet()) {
					// TableRow row =
					// (TableRow)mInflater.inflate(R.layout.book_detail_row,
					// null);
					RelativeLayout row = (RelativeLayout) mInflater.inflate(
							R.layout.book_detail_row2, null);

					((TextView) row.findViewById(R.id.detailLabel2))
					.setText(key + ": ");
					((TextView) row.findViewById(R.id.detailValue2))
					.setText(b.bookDetails.get(key));
					detailsTable.addView(row);
				}
				dialog.cancel();
				setContentView(detailsTable);
				// setContentView(R.layout.book_detail_layout2);
			} else
				if (shared.LOGGINGLEVEL>0) Log.i("displaySearchResults", "book detail not fetched yet:"
						+ b.title);
		}

		catch (Exception e) {
			if (shared.LOGGINGLEVEL>0) Log.i("displaySearchResults", "Exception in displayBookDetail");
			TextView tv = new TextView(this);
			tv.setText(e.toString());
			setContentView(tv);
		}
	}

	ArrayList<Book> listViewData = new ArrayList<Book>();
	BookBaseAdapter bookAdapter;
	Handler handler;
	Context context;
	boolean activityRunning;

	public void onPause() {
		super.onPause();
		activityRunning = false;
	}

	public void onResume() {
		super.onResume();
		activityRunning = true;
		parseResults4.numResults = -1; // static field, need to reset. - should
		// probably rewrite this field to be
		// nonstatic
		// and localized to each searchResults activity instance.
	}

	ProgressDialog dialog;
	ActionBar actionBar;

	/** Called when the activity is first created. */
	@SuppressWarnings("unused")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitleColor(getResources().getColor(R.color.snow2));

		setContentView(R.layout.search_results5);
		dialog = new ProgressDialog(this,R.style.CustomDialog);
		dialog.setMessage("Loading. Please wait...");
		dialog.show();
		//		dialog = ProgressDialog.show(this, "", "Loading. Please wait...", true);		activityRunning = true;
		// header = (TextView) findViewById(R.id.searchResultsHeader);
		context = this;
		ListView listview = (ListView) findViewById(R.id.searchResultsListView5);

		bookAdapter = new BookBaseAdapter(this, listViewData);
		listview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				//TODO: disabled onclick for beta test until can figure out how to fix this
				//				if (shared.LOGGINGLEVEL>0) Log.i("displaySearchResult", "listview item clicked: "
				//						+ position);
				//				dialog = new ProgressDialog(context,R.style.CustomDialog);
				//				dialog.setMessage("Loading. Please wait...");
				//				dialog.show();
				////				dialog = ProgressDialog.show(this, "", "Loading. Please wait...", true);
				//				(new fetchBookDetail(position, position)).run(); // in UI
				//				// thread.
				//				// new Thread(new fetchBookDetail(currentViewNumEnd, //not
				//				// fetching details right now - slows down code too much
				//				// currentViewNumEnd + resultsPerPage)).start();
				//				displayBookDetail(position);
			}
		});

		listview.setAdapter(bookAdapter);

		// code downloaded from
		// https://github.com/johannilsson/android-actionbar/blob/master/README.md
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("Search Results");
		actionBar.setHomeAction(new IntentAction(this, new Intent(this,WelcomeScreen.class), R.drawable.home)); // go	// home

		actionBar.addAction(new IntentAction(this, new Intent(this,
				settings.class), R.drawable.gear)); // go to
		// settings
		actionBar.addAction(new prevPage());
		actionBar.addAction(new nextPage());
		// ----------------------

		Bundle bundle = getIntent().getExtras();
		SearchData data = bundle.getParcelable("fieldsData");

		Uri uri = buildURIfromData(data);
		handler = new Handler();

		try {
			HttpGet httpget = new HttpGet(uri.toString());
			URL libraryURL = new URL(httpget.getURI().toString());
			// if (!pageLoading)
			new Thread(new getURIdata(libraryURL)).start();

			if (allResultsQed && !shownFirst) {
				displayResults(0);
				shownFirst = true;
			}
			// new Thread(new fetchBookDetail(0, resultsPerPage)).start(); //
			// fetch
			// book // details // for // first // page
		} catch (Exception e) {
			if (shared.LOGGINGLEVEL>0) Log.i("displaySearchResults",
					"Exception in getResultsPage:" + e.toString());
		}

	}
}

package com.nasageek.utexasutilities.fragments;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import com.nasageek.utexasutilities.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
//import com.crittercism.app.Crittercism;
import com.mapsaurus.paneslayout.FragmentLauncher;
import com.nasageek.utexasutilities.AttachmentDownloadService;
import com.nasageek.utexasutilities.ConnectionHelper;
import com.nasageek.utexasutilities.MyScrollView;
import com.nasageek.utexasutilities.ParcelablePair;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.model.CourseMapItem;

public class BlackboardDownloadableItemFragment extends  BlackboardFragment {
	
	private ListView dlableItems;
	private TextView contentDescription;
	private LinearLayout dlil_pb_ll;
	private TextView dlil_etv;
	private LinearLayout dlil_ell;
	private DefaultHttpClient client;
	private BroadcastReceiver onNotificationClick;
	private ArrayList<bbFile> attachments;
	private dlableItemAdapter attachmentAdapter;
	private String content;
	private String courseID, courseName, viewUri, itemName;
	
	private MyScrollView msv;
	
	public BlackboardDownloadableItemFragment() {}
	
	public static BlackboardDownloadableItemFragment newInstance(String contentID,
			String courseID, String courseName, String itemName, String viewUri, Boolean fromDashboard) {
		BlackboardDownloadableItemFragment bmif = new BlackboardDownloadableItemFragment();
		
		Bundle args = new Bundle();
		args.putString("contentID", contentID);
        args.putString("courseID", courseID);
        args.putString("courseName", courseName);
        args.putString("itemName", itemName);
        args.putString("viewUri", viewUri);
        args.putBoolean("fromDashboard", fromDashboard);
        bmif.setArguments(args);
        
        return bmif;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		
		courseID = getArguments().getString("courseID");
		courseName = getArguments().getString("courseName");
		viewUri = getArguments().getString("viewUri");
		itemName = getArguments().getString("itemName");
		
		setHasOptionsMenu(true);
		
		attachments = new ArrayList<bbFile>();
		attachmentAdapter = new dlableItemAdapter(getSherlockActivity(), attachments);
		content = "";
		
		client = ConnectionHelper.getThreadSafeClient();
		BasicClientCookie cookie = new BasicClientCookie("s_session_id", ConnectionHelper.getBBAuthCookie(getSherlockActivity(),client));
    	cookie.setDomain("courses.utexas.edu");
    	client.getCookieStore().addCookie(cookie);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)  {
		final View vg = inflater.inflate(R.layout.blackboard_dlable_item_layout, container, false);
		
		setupActionBar();
		
		dlableItems = (ListView) vg.findViewById(R.id.dlable_item_list);
		dlil_pb_ll = (LinearLayout) vg.findViewById(R.id.blackboard_dl_items_progressbar_ll);
		dlil_ell = (LinearLayout) vg.findViewById(R.id.blackboard_dl_error);
		dlil_etv = (TextView) vg.findViewById(R.id.tv_failure);
		contentDescription = (TextView) vg.findViewById(R.id.content_description);
		msv = (MyScrollView) vg.findViewById(R.id.scroll_content_description);
		
		contentDescription.setText(content);
		dlableItems.setAdapter(attachmentAdapter);
		
		dlableItems.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> parent, View arg1, int position, long id) {
				final bbFile item = (bbFile)(parent.getAdapter().getItem(position));
				
				AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getSherlockActivity());
				alertBuilder.setMessage("Would you like to download this attached file?")
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				})
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					@Override
					@TargetApi(Build.VERSION_CODES.HONEYCOMB)
					public void onClick(DialogInterface dialog, int which) {
						if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {	 
							Intent downloadAttachment = new Intent(getSherlockActivity(), AttachmentDownloadService.class);
							downloadAttachment.putExtra("fileName", item.getFileName());
							downloadAttachment.putExtra("url", item.getDlUri());
							getSherlockActivity().startService(downloadAttachment);			
						}	
						else if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) { 	
							final DownloadManager manager = (DownloadManager) getSherlockActivity().getSystemService(Service.DOWNLOAD_SERVICE);
	
							onNotificationClick = new BroadcastReceiver() {
							    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
								public void onReceive(Context con, Intent intent) {
							    	final String action = intent.getAction();
							    	DownloadManager notifmanager = (DownloadManager) con.getSystemService(Service.DOWNLOAD_SERVICE);
							    	if(DownloadManager.ACTION_NOTIFICATION_CLICKED.equals(action)) {
							    		long[] dlIDs = intent.getLongArrayExtra(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS);
							    		Uri downloadedFile = notifmanager.getUriForDownloadedFile(dlIDs[0]);
							    		//not sure when dlIDs will ever have >1 member, so let's just assume only 1 member
							    		//TODO: need to confirm when dlIDs might be >1
							    		if(downloadedFile != null) { //make sure file isn't still downloading	
							    			try {
							 	    			con.startActivity(new Intent(Intent.ACTION_VIEW, downloadedFile));
							    			} catch(ActivityNotFoundException ex) {
							 	    				ex.printStackTrace();
							 	    				//TODO: let the user know something went wrong?
							 	    		}
							    		}
							    		else
							    			Toast.makeText(con, "Download could not be opened at this time.", Toast.LENGTH_SHORT).show();
							    }
							}
						};
							getSherlockActivity().registerReceiver(onNotificationClick, new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));
							  
							Uri uri = Uri.parse("https://courses.utexas.edu" + Uri.decode(item.getDlUri()));
							 
							Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdirs();
							DownloadManager.Request request = new DownloadManager.Request(uri);
							 
							//fix stupid Honeycomb bug
							if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR2)
								request.setShowRunningNotification(true);
							else	
								request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
					    	request.setDescription("Downloading to the Download folder.")
					    			.setTitle(item.getFileName())
					    			.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, item.getFileName())
					    			.addRequestHeader("Cookie", "s_session_id="+ConnectionHelper.getBBAuthCookie(getSherlockActivity(), client));
					    	
					    	final long dlID = manager.enqueue(request);
					    	Toast.makeText(getSherlockActivity(), "Download started, item should appear in the \"Download\" folder on your external storage.", Toast.LENGTH_LONG).show();
						 }	 
						 else {
							 AlertDialog.Builder build = new AlertDialog.Builder(getSherlockActivity());
							 build.setNeutralButton("Okay", new DialogInterface.OnClickListener() {	
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();	
								}
							 })
							 .setTitle("No External Media")
							 .setMessage("Your external storage media (such as a microSD Card) is currently unavailable; "+
							 "the download cannot start.")
							 .show();	 
						}	
					}
				})
				.setTitle("Download Attachment").
				show();
			}
		});

		if(content.equals(""))
			new fetchData(client).execute(getArguments().getString("contentID"));
		else
			completeUISetup();
		return vg;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(onNotificationClick != null)
			try {
				getSherlockActivity().unregisterReceiver(onNotificationClick);
				onNotificationClick = null;
			} catch(IllegalArgumentException e) { //if it's already been unregistered
				e.printStackTrace();
			}
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
		
//        if(!getIntent().getBooleanExtra("showViewInWeb", false))
        if(viewUri != null && !viewUri.equals(""))
        	inflater.inflate(R.menu.blackboard_dlable_item_menu, menu);
       // 	menu.removeItem(R.id.blackboard_d);
		 
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	int id = item.getItemId();
    	switch(id) {
	    	case R.id.dlable_item_view_in_web:
	    		showAreYouSureDlg(getSherlockActivity());
	    		break;
    	}
    	return false;
	}
	private void showAreYouSureDlg(final Context con) {
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(con);
		alertBuilder.setMessage("Would you like to view this item on the Blackboard website? (you might need to log in again if" +
				" you have disabled the embedded browser)");
		alertBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		
		alertBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				
				SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(con);
				if(sp.getBoolean("embedded_browser", true)) {
					((FragmentLauncher)getSherlockActivity()).addFragment(BlackboardDownloadableItemFragment.this, 
							BlackboardExternalItemFragment.newInstance(viewUri, courseID, courseName, itemName, false));
				}
				else {
					Intent i = new Intent(Intent.ACTION_VIEW);  
					i.setData(Uri.parse(viewUri));  
					startActivity(i);
				}
				
				
		/*		Intent web = new Intent(null,Uri.parse(getIntent().getStringExtra("viewUri")),BlackboardDownloadableItemActivity.this,BlackboardExternalItemActivity.class);
	    		web.putExtra("itemName", getIntent().getStringExtra("itemName"));
	    		web.putExtra("coursename", getIntent().getStringExtra("coursename"));
	    		startActivity(web);*/
			}		
		});
		alertBuilder.setTitle("View on Blackboard");
		alertBuilder.show();
	}
	
	private void setupActionBar() {
		final ActionBar actionbar = getSherlockActivity().getSupportActionBar();
		actionbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE, ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_CUSTOM);
		actionbar.setTitle(getArguments().getString("courseName"));
		actionbar.setSubtitle(getArguments().getString("itemName"));
	}
	
	private void completeUISetup() {
		if(msv.getViewTreeObserver().isAlive()) {
			msv.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

				@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
				@Override
				public void onGlobalLayout() {
					
					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) 
						msv.getViewTreeObserver().removeGlobalOnLayoutListener(this);
					else 
						msv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
					
					if(!msv.canScroll())
						msv.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0));
					else if(msv.canScroll())
						msv.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 6));

				}
			});
		}
		if(("".equals(content) || "No description".equals(content)) && getSherlockActivity() != null) {	
			content = "No description";
			TypedValue tv = new TypedValue();
			if(getSherlockActivity().getTheme().resolveAttribute(android.R.attr.textColorTertiary, tv, true)) {	
				TypedArray arr = getSherlockActivity().obtainStyledAttributes(tv.resourceId, new int[] {android.R.attr.textColorTertiary});
				contentDescription.setTextColor(arr.getColor(0, Color.BLACK));
			}			
		}
	}
	private class fetchData extends AsyncTask<String, Object, Object[]> {
		private DefaultHttpClient client;
		private String errorMsg;
		
		
		public fetchData(DefaultHttpClient client) {
			this.client = client;
		}
		
		@Override
		protected void onPreExecute() {
			dlil_pb_ll.setVisibility(View.VISIBLE);
			dlil_ell.setVisibility(View.GONE);
			contentDescription.setVisibility(View.GONE);
    		dlableItems.setVisibility(View.GONE);
		}
		
		@Override
		protected Object[] doInBackground(String... params) {
			String contentid = params[0];
			
			HttpGet hget = new HttpGet("https://courses.utexas.edu/webapps/Bb-mobile-BBLEARN/contentDetail?content_id="+contentid+"&course_id="+getArguments().getString("courseID"));
	    	String pagedata="";

	    	try {
				HttpResponse response = client.execute(hget);
		    	pagedata = EntityUtils.toString(response.getEntity());
			} catch (Exception e) {
				errorMsg = "UTilities could not fetch this download";
				cancel(true);
				e.printStackTrace();
				return null;	
			}
	    	Object[] result = new Object[2];
	    	ArrayList<bbFile> data=new ArrayList<bbFile>();
	    	
	    	Pattern contentPattern = Pattern.compile("<body>(.*?)</body>",Pattern.DOTALL);
	    	Matcher contentMatcher = contentPattern.matcher(pagedata);
	    	if(contentMatcher.find())
	    		content = contentMatcher.group(1);
	    	else
	    		content = "No description";
	    	
	       	Pattern attachmentPattern = Pattern.compile("<attachment.*?/>");
	    	Matcher attachmentMatcher = attachmentPattern.matcher(pagedata);
	    	
	    	while(attachmentMatcher.find()) {
	    		String attachData = attachmentMatcher.group();
	    		Pattern namePattern = Pattern.compile("linkLabel=\"(.*?)\"");
		    	Matcher nameMatcher = namePattern.matcher(attachData);
		    	Pattern fileNamePattern = Pattern.compile("name=\"(.*?)\"");
		    	Matcher fileNameMatcher = fileNamePattern.matcher(attachData);
		    	Pattern uriPattern = Pattern.compile("uri=\"(.*?)\"");
		    	Matcher uriMatcher = uriPattern.matcher(attachData);
		    	Pattern sizePattern = Pattern.compile("filesize=\"(.*?)\"");
		    	Matcher sizeMatcher = sizePattern.matcher(attachData);
	
		    	if(sizeMatcher.find() && nameMatcher.find() && fileNameMatcher.find() && uriMatcher.find())
		    		data.add(new bbFile(nameMatcher.group(1).replace("&amp;", "&"),fileNameMatcher.group(1),
		    				sizeMatcher.group(1),uriMatcher.group(1).replace("&amp;", "&"),getArguments().getString("itemName")));
	    	}
	    	
	    	result[0] = content;
	    	result[1] = data;
	    	
			return result;
		}
		@Override
		protected void onPostExecute(Object... result) {
			if(!this.isCancelled()) {				
				content = Html.fromHtml(Html.fromHtml(((String) result[0]).replaceAll("<!--.*?-->", "")).toString()).toString().trim();
				completeUISetup();

				contentDescription.setText(content);	
				attachments.addAll((ArrayList<bbFile>) result[1]);
				attachmentAdapter.notifyDataSetChanged();
				
				
				dlil_pb_ll.setVisibility(View.GONE);
				contentDescription.setVisibility(View.VISIBLE);
	    		dlableItems.setVisibility(View.VISIBLE);
	    	}
		}		
		@Override
		protected void onCancelled(Object... o) {
			dlil_etv.setText(errorMsg);
			
			dlil_ell.setVisibility(View.VISIBLE);
			dlil_pb_ll.setVisibility(View.GONE);
			contentDescription.setVisibility(View.GONE);
    		dlableItems.setVisibility(View.GONE);
		}
		
		
	}
	private class dlableItemAdapter extends ArrayAdapter<bbFile> {
		private Context con;
		private ArrayList<bbFile> items;
		LayoutInflater li;
		
		
		public dlableItemAdapter(Context c, ArrayList<bbFile> items) {
			super(c,0,items);
			con = c;
			this.items=items;
			li = (LayoutInflater)con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		}
		@Override
		public int getCount() {
			return items.size();
		}
		@Override
		public bbFile getItem(int position) {
			return items.get(position);
		}
		@Override
		public long getItemId(int position) {
			return 0;
		}
		@Override
		public boolean areAllItemsEnabled() {
			return true;
		}
		@Override
		public boolean isEnabled(int i) {
			return true;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			bbFile item = items.get(position);
			ViewGroup lin = (ViewGroup) convertView;
			
			if (lin == null)
				lin = (ViewGroup) li.inflate(R.layout.attachment_view,null,false);
		/*	if(position%2==0)
				lin.setBackgroundColor(Color.LTGRAY);   Don't do this until you figure out the state drawable
			else
				lin.setBackgroundDrawable(null);*/
			TextView nameView = (TextView) lin.findViewById(R.id.attachment_name);
			TextView filesizeView = (TextView) lin.findViewById(R.id.attachment_size);
			
			nameView.setText(item.getName());
			filesizeView.setText("Filesize: " + String.format("%,.1f", Double.parseDouble(item.getSize())/1000)+" KB");
		
			return (View)lin;
		}
	}
	
	private class bbFile {
		private String name;
		private String size;
		private String dlUri;
		private String viewUri;
		private String fileName;
		
		
		public bbFile(String name, String fileName, String size, String dlUri, String viewUri) {
			this.name=name;
			this.fileName=fileName;
			this.size=size;
			this.dlUri=dlUri;
			this.viewUri=viewUri;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getSize() {
			return size;
		}
		public void setSize(String size) {
			this.size = size;
		}
		public String getDlUri() {
			return dlUri;
		}
		public void setDlUri(String dlUri) {
			this.dlUri = dlUri;
		}
		public String getViewUri() {
			return viewUri;
		}
		public String getFileName() {
			return fileName;
		}
	}

	@Override
	public String getBbid() {	
		return getArguments().getString("courseID");
	}

	@Override
	public String getCourseName() {
		return getArguments().getString("courseName");
	}
	
	@Override
	public boolean isFromDashboard() {
		return getArguments().getBoolean("fromDashboard");
	}

	@Override
	public void onPanesScrolled() {
		setupActionBar();		
	}

}

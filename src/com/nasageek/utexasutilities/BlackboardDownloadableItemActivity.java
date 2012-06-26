package com.nasageek.utexasutilities;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class BlackboardDownloadableItemActivity extends SherlockActivity {
	
	private DownloadManager manager;
	private long dlID;
	private ListView dlableItems;
	private TextView contentDescription;
	private LinearLayout dlil_pb_ll;
	private ActionBar actionbar;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.blackboard_dlable_item_layout);
		
		actionbar = getSupportActionBar();
		actionbar.setHomeButtonEnabled(true);
		// actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setTitle(getIntent().getStringExtra("itemName")+" - details");
		
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)	
			actionbar.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.actionbar_bg));
		
		
		dlableItems = (ListView) findViewById(R.id.dlable_item_list);
		dlil_pb_ll = (LinearLayout) findViewById(R.id.blackboard_dl_items_progressbar_ll);
		contentDescription = (TextView) findViewById(R.id.content_description);
		
		DefaultHttpClient client = ConnectionHelper.getThreadSafeClient();
		BasicClientCookie cookie = new BasicClientCookie("s_session_id", ConnectionHelper.getBBAuthCookie(this,client));
    	cookie.setDomain("courses.utexas.edu");
    	client.getCookieStore().addCookie(cookie);
		
		new fetchData(client).execute(getIntent().getStringExtra("contentid"));
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater = this.getSupportMenuInflater();
        inflater.inflate(R.layout.blackboard_dlable_item_menu, menu);
		return true;
		 
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
	    	int id = item.getItemId();
	    	switch(id)
	    	{
		    	case android.R.id.home:
		            // app icon in action bar clicked; go home
		            Intent home = new Intent(this, UTilitiesActivity.class);
		            home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		            startActivity(home);break;
		    	case R.id.viewInWeb:
		    		showAreYouSureDlg(BlackboardDownloadableItemActivity.this);
		    		break;
	    	}
	    	return false;
	}
	private void showAreYouSureDlg(Context con)
	{
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(con);
		alertBuilder.setMessage("Would you like to view this item on the Blackboard website?");
		alertBuilder.setNegativeButton("No", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				
			}
		});
		
		alertBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() 
		{
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				Intent web = new Intent(null,Uri.parse(getIntent().getStringExtra("viewUri")),BlackboardDownloadableItemActivity.this,BlackboardExternalItemActivity.class);
	    		web.putExtra("itemName", getIntent().getStringExtra("itemName"));
	    		startActivity(web);
			}		
		});
		alertBuilder.setTitle("View on Blackboard");
		alertBuilder.show();
	}
	
	private class fetchData extends AsyncTask<String, Void, Object[]>
	{
			private DefaultHttpClient client;
			
			public fetchData(DefaultHttpClient client)
			{
				this.client = client;
			}
			
			@Override
			protected Object[] doInBackground(String... params)
			{
				String contentid = params[0];
				
				HttpGet hget = new HttpGet("https://courses.utexas.edu/webapps/Bb-mobile-BBLEARN/contentDetail?content_id="+contentid+"&course_id="+BlackboardActivity.currentBBCourseId);
		    	String pagedata="";

		    	try
				{
					HttpResponse response = client.execute(hget);
			    	pagedata = EntityUtils.toString(response.getEntity());
				} catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    	Object[] result = new Object[2];
		    	ArrayList<bbFile> data=new ArrayList<bbFile>();
		    	String content;
		    	
		    	Pattern contentPattern = Pattern.compile("<body>(.*?)</body>",Pattern.DOTALL);
		    	Matcher contentMatcher = contentPattern.matcher(pagedata);
		    	if(contentMatcher.find())
		    	{
		    		content = contentMatcher.group(1);
		    	}
		    	else
		    		content = "No description";
		    	
		       	Pattern attachmentPattern = Pattern.compile("<attachment.*?/>");
		    	Matcher attachmentMatcher = attachmentPattern.matcher(pagedata);
		    	
		    	while(attachmentMatcher.find())
		    	{
		    		String attachData = attachmentMatcher.group();
		    		Pattern namePattern = Pattern.compile("linkLabel=\"(.*?)\"");
			    	Matcher nameMatcher = namePattern.matcher(attachData);
			    	Pattern uriPattern = Pattern.compile("uri=\"(.*?)\"");
			    	Matcher uriMatcher = uriPattern.matcher(attachData);
			    	Pattern sizePattern = Pattern.compile("filesize=\"(.*?)\"");
			    	Matcher sizeMatcher = sizePattern.matcher(attachData);
			    	
			    	if(sizeMatcher.find() && nameMatcher.find() && uriMatcher.find())
			    		data.add(new bbFile(nameMatcher.group(1),sizeMatcher.group(1),uriMatcher.group(1).replace("&amp;", "&"),getIntent().getStringExtra("itemName")));
		    	}
		    	
		    	result[0] = content;
		    	result[1] = data;
		    	
				return result;
			}
			@Override
			protected void onPostExecute(Object... result)
			{
				if(!this.isCancelled())
		    	{
					String content = Html.fromHtml(Html.fromHtml(((String) result[0]).replaceAll("<!--.*?-->", "")).toString()).toString();
					ArrayList<bbFile> data = (ArrayList<bbFile>) result[1];
					
					
					contentDescription.setText(content);
					
					
					dlableItems.setAdapter(new dlableItemAdapter(BlackboardDownloadableItemActivity.this,data));
					dlableItems.setOnItemClickListener(new OnItemClickListener() {
						
						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
						{
							final bbFile item = (bbFile)(arg0.getAdapter().getItem(arg2));
							
							AlertDialog.Builder alertBuilder = new AlertDialog.Builder(BlackboardDownloadableItemActivity.this);
							alertBuilder.setMessage("Would you like to download this attached file?");
							alertBuilder.setNegativeButton("No", new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
									dialog.dismiss();
									
								}
							});
							
							alertBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub

									if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) 
									{	 
										showNotSupportedDlg(BlackboardDownloadableItemActivity.this);
									}	
									 else
								     { 
								    	 Uri uri = Uri.parse("https://courses.utexas.edu"+Uri.decode(item.getDlUri()));
										 DownloadManager.Request request = new DownloadManager.Request(uri);
										 
										 request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
								    	 request.setDescription("Downloading to the UTilities folder.");
								    	 request.setTitle(item.getName());
								    	 request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, item.getName());
								    	 request.addRequestHeader("Cookie", "s_session_id="+ConnectionHelper.getBBAuthCookie(BlackboardDownloadableItemActivity.this, client));
								    	 manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
								    	 dlID = manager.enqueue(request);
								    	 
								    	 if(Build.VERSION.SDK_INT<Build.VERSION_CODES.HONEYCOMB)
								    	 {
								    		 BroadcastReceiver receiver = new BroadcastReceiver() {
									             @Override
									             public void onReceive(Context context, Intent intent) {
									                 String action = intent.getAction();
									                 if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
									                     long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
									                     Query query = new Query();
									                     query.setFilterById(dlID);
									                     Cursor c = manager.query(query);
									                     if (c.moveToFirst()) {
									                         int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
									                         if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
									                        	 Notification n;
								                        		 Notification.Builder notbuild = new Notification.Builder(BlackboardDownloadableItemActivity.this);
									                        	 notbuild.setSmallIcon(android.R.drawable.stat_sys_download_done);
									                             notbuild.setContentTitle(item.getName());
									                             notbuild.setContentText("Download complete");
									                             notbuild.setTicker("UTilities download completed.");
									                             notbuild.setContentIntent(PendingIntent.getActivity(BlackboardDownloadableItemActivity.this, 0, new Intent(Intent.ACTION_VIEW,manager.getUriForDownloadedFile(dlID)),0));
									                             n = notbuild.getNotification();
									                             ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(item.getName(),0,n);
									                         }
									                     }
									                 }
									             }
									         };
									         registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
								    	 }
								    	 Toast.makeText(BlackboardDownloadableItemActivity.this, "Download started, item should appear in the \"Download\" folder on your external storage.", Toast.LENGTH_LONG).show();
								     }
									
								}
							});
							alertBuilder.setTitle("Download Attachment");
							alertBuilder.show();
						}
					});
					dlil_pb_ll.setVisibility(View.GONE);
					contentDescription.setVisibility(View.VISIBLE);
		    		dlableItems.setVisibility(View.VISIBLE);
		    	}
			}
			private void showNotSupportedDlg(Context con)
			{
				AlertDialog.Builder build = new AlertDialog.Builder(con);
				build.setMessage("Sorry! Downloading attachments is currently only supported on Android 3 and above. " +
						"Your version should receive support in the near future.");
				build.setNeutralButton("Okay", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}	
				});
				build.show();
			}
		
	}
	
	private class dlableItemAdapter extends ArrayAdapter<bbFile>
	{
		private Context con;
		private ArrayList<bbFile> items;
		LayoutInflater li;
		
		
		public dlableItemAdapter(Context c, ArrayList<bbFile> items)
		{
			super(c,0,items);
			con = c;
			this.items=items;
			li = (LayoutInflater)con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		}
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return items.size();
		}
		@Override
		public bbFile getItem(int position) {
			// TODO Auto-generated method stub
			return items.get(position);
		}
		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}
		@Override
		public boolean areAllItemsEnabled()
		{
			return true;
		}
		@Override
		public boolean isEnabled(int i)
		{
			return true;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
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
	
	private class bbFile
	{
		private String name;
		private String size;
		private String dlUri;
		private String viewUri;
		
		
		public bbFile(String name, String size, String dlUri, String viewUri)
		{
			this.name=name;
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


		public void setdlUri(String dlUri) {
			this.dlUri = dlUri;
		}
		public String getViewUri()
		{
			return viewUri;
		}
	}

}

package com.nasageek.utexasutilities.fragments;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.acra.ACRA;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;
import org.xmlpull.v1.XmlPullParserException;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import com.nasageek.utexasutilities.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.preference.PreferenceManager;
import android.util.TimingLogger;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.foound.widget.AmazingAdapter;
import com.foound.widget.AmazingListView;
import com.mapsaurus.paneslayout.FragmentLauncher;
import com.nasageek.utexasutilities.BlackboardDashboardXmlParser;
import com.nasageek.utexasutilities.ConnectionHelper;
import com.nasageek.utexasutilities.ParcelablePair;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.SectionedParcelableList;
import com.nasageek.utexasutilities.model.BBClass;
import com.nasageek.utexasutilities.model.FeedItem;

public class BlackboardDashboardFragment extends SherlockFragment {

	private DefaultHttpClient httpclient;
	private LinearLayout d_pb_ll;
	private AmazingListView dlv;
	private TextView etv;
	private LinearLayout ell;
	private Button eb;
	private TimingLogger tl;
	private fetchDashboardTask fetch;
	private boolean longform;

	private HashMap<String, BBClass> courses;
	private List<ParcelablePair<String, List<FeedItem>>> feedList;
	private BlackboardDashboardAdapter bda;
				
		
	public BlackboardDashboardFragment() { }
	
	public static BlackboardDashboardFragment newInstance(String title) {
		BlackboardDashboardFragment f = new BlackboardDashboardFragment();

        Bundle args = new Bundle();
        args.putString("title", title);
        f.setArguments(args);

        return f;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		tl = new TimingLogger("Dashboard", "loadTime");
				
		longform = PreferenceManager.getDefaultSharedPreferences(getSherlockActivity()).getBoolean("blackboard_class_longform", false);
		if(savedInstanceState == null)
			feedList = new ArrayList<ParcelablePair<String,List<FeedItem>>>();
		else {
			feedList = (List<ParcelablePair<String, List<FeedItem>>>) ((SectionedParcelableList) savedInstanceState.getParcelable("feedList")).getList();
			courses = (HashMap<String, BBClass>) savedInstanceState.getSerializable("courses");
		}
		bda = new BlackboardDashboardAdapter(feedList);
		
			
	}
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View vg =  inflater.inflate(R.layout.blackboard_dashboard_fragment, container, false);
		
		httpclient = ConnectionHelper.getThreadSafeClient();
		String bbAuthCookie = ConnectionHelper.getBBAuthCookie(getSherlockActivity(),httpclient);
		httpclient.getCookieStore().clear();
		BasicClientCookie cookie = new BasicClientCookie("s_session_id", bbAuthCookie);
    	cookie.setDomain(ConnectionHelper.blackboard_domain_noprot);
    	httpclient.getCookieStore().addCookie(cookie);
		
		dlv = (AmazingListView) vg.findViewById(R.id.dash_listview);
		d_pb_ll = (LinearLayout) vg.findViewById(R.id.dash_progressbar_ll);
		ell = (LinearLayout) vg.findViewById(R.id.dash_error);
		etv = (TextView) vg.findViewById(R.id.tv_failure);
		eb = (Button) vg.findViewById(R.id.button_send_data);
		
		dlv.setAdapter(bda);
		dlv.setPinnedHeaderView(inflater.inflate(R.layout.menu_header_item_view, dlv, false));
		dlv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
				SherlockFragmentActivity act = getSherlockActivity();
				
				FeedItem fi = (FeedItem) parent.getAdapter().getItem(position);
				String courseid = fi.getBbId();
				String contentid = fi.getContentId();
				String coursename = courses.get(fi.getBbId()).getCourseId();
				String message = fi.getMessage();
				
				if("Grades".equals(fi.getType())) {		
			/*		final Intent gradesLaunch = new Intent(null, null, getSherlockActivity(), BlackboardGradesActivity.class);
		//			gradesLaunch.putExtra("viewUri", url);
				//TODO: fetch coursemap for viewurl
					gradesLaunch.putExtra("courseid", courseid);
					gradesLaunch.putExtra("coursename", coursename);
					gradesLaunch.putExtra("showViewInWeb", false);
					startActivity(gradesLaunch);*/
					
					//TODO: reconsider passing blank string as viewUri
					
					((FragmentLauncher)act).addFragment(BlackboardDashboardFragment.this.getParentFragment(), 
							BlackboardGradesFragment.newInstance(courseid, coursename, "", true));
				}
				else if("Content".equals(fi.getType())) {
				/*	final Intent bbItemLaunch = new Intent(null, null, getSherlockActivity(), BlackboardDownloadableItemActivity.class);
					bbItemLaunch.putExtra("contentid", contentid);
					
					bbItemLaunch.putExtra("itemName", message); //TODO: not sure if I want to keep this
					
				//	bbItemLaunch.putExtra("viewUri", url); TODO
					bbItemLaunch.putExtra("courseid", courseid);
					bbItemLaunch.putExtra("coursename", coursename);
					bbItemLaunch.putExtra("showViewInWeb", false);
					startActivity(bbItemLaunch);*/
					
					((FragmentLauncher)act).addFragment(BlackboardDashboardFragment.this.getParentFragment(), 
							BlackboardDownloadableItemFragment.newInstance(contentid, courseid, coursename, message, "", true));
				}
				else if("Announcement".equals(fi.getType())) {
					//TODO: figure out how to seek to a specific announcement
			/*		final Intent announcementsLaunch = new Intent(null, null, getSherlockActivity(), BlackboardAnnouncementsActivity.class);
			//		announcementsLaunch.putExtra("viewUri", url); TODO
					announcementsLaunch.putExtra("courseid", courseid);
					announcementsLaunch.putExtra("coursename", coursename);
					announcementsLaunch.putExtra("showViewInWeb", false);
					startActivity(announcementsLaunch);*/
					
					((FragmentLauncher)act).addFragment(BlackboardDashboardFragment.this.getParentFragment(), 
							BlackboardAnnouncementsFragment.newInstance(courseid, coursename, "", true));
				}
				else if("Courses".equals(fi.getType())) {
		/*			final Intent classLaunch = new Intent(getString(R.string.coursemap_intent), null, getSherlockActivity(), CourseMapActivity.class);
					classLaunch.putExtra("courseid", fi.getBbId());
					classLaunch.setData(Uri.parse(fi.getBbId()));
					classLaunch.putExtra("folderName", "Course Map");
					classLaunch.putExtra("coursename", fi.getCourseId());
		//			classLaunch.putExtra("showViewInWeb", false);
					startActivity(classLaunch);*/
					
					((FragmentLauncher)act).addFragment(BlackboardDashboardFragment.this.getParentFragment(), 
							BlackboardCourseMapFragment.newInstance(getString(R.string.coursemap_intent), null, courseid, coursename, "Course Map", "", -1, false));
				}
			}
		});
		
		if(feedList.size() == 0) {	
			fetch = new fetchDashboardTask(httpclient);
			
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
				fetch.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, bbAuthCookie);
			else
				fetch.execute(bbAuthCookie);
		}
		
		return vg;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(fetch!=null)
			fetch.cancel(true);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable("feedList", new SectionedParcelableList<FeedItem>(feedList, FeedItem.class));
		outState.putSerializable("courses", courses);
	}
	
	public void refresh()
	{
	//	tlv.setVisibility(View.GONE);
	//	etv.setVisibility(View.GONE);
	//	t_pb_ll.setVisibility(View.VISIBLE);
		if(fetch!=null)
		{	fetch.cancel(true);
			fetch = null;
		}
//		transactionlist.clear();
		
//		parser(true);
//		ta.resetPage();
//		tlv.setSelectionFromTop(0, 0);
	}
	private class fetchDashboardTask extends AsyncTask<String,Void,List<ParcelablePair<String, List<FeedItem>>>>
	{
		private DefaultHttpClient client;
		private String errorMsg = "";
		private List<ParcelablePair<String, List<FeedItem>>> tempFeedList;
		private Exception ex;
		private String pagedata;
		private Boolean showButton = false;
		
		public fetchDashboardTask(DefaultHttpClient client) {
			this.client = client;
		}
		
		@Override
		protected void onPreExecute() {
			d_pb_ll.setVisibility(View.VISIBLE);
    		dlv.setVisibility(View.GONE);
			ell.setVisibility(View.GONE); 
		}
		
		@Override
		protected List<ParcelablePair<String, List<FeedItem>>> doInBackground(String... params) {
	    	String pagedata="";
	    	String bbAuthCookie = params[0];		
	    	tempFeedList = new ArrayList<ParcelablePair<String, List<FeedItem>>>();
	    	
	    	if(Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
		    	URL location;
		    	HttpsURLConnection conn = null;
				try {
					location = new URL(ConnectionHelper.blackboard_domain + "/webapps/Bb-mobile-BBLEARN/dashboard?course_type=COURSE&with_notifications=true");
					conn = (HttpsURLConnection) location.openConnection();
					conn.setRequestProperty("Cookie", "s_session_id="+bbAuthCookie);
					conn.setRequestMethod("GET");
					conn.setDoInput(true);
					conn.connect();
					InputStream in = conn.getInputStream();
					BlackboardDashboardXmlParser parser = new BlackboardDashboardXmlParser();
					tempFeedList = parser.parse(in);
					courses = parser.getCourses();
					in.close();
					tl.addSplit("XML downloaded");
					
				} catch (IOException e) {
					errorMsg = "UTilities could not fetch your Blackboard Dashboard";
					e.printStackTrace();
					cancel(true);
					return null;
				} catch (XmlPullParserException e) {
					errorMsg = "UTilities could not parse the downloaded Dashboard data.";
					showButton = true;
					this.pagedata = pagedata;
					ex = e;
		        	e.printStackTrace();
		        	cancel(true);
		        	return null;	
					
				} finally {
					if(conn != null)
						conn.disconnect();
				}
	    	} else {
		    	try {
		    		HttpGet hget = new HttpGet(ConnectionHelper.blackboard_domain + "/webapps/Bb-mobile-BBLEARN/dashboard?course_type=COURSE&with_notifications=true");
		    		HttpResponse response = client.execute(hget);
			    	pagedata = EntityUtils.toString(response.getEntity());
			    	BlackboardDashboardXmlParser parser = new BlackboardDashboardXmlParser();
			    	tempFeedList = parser.parse(new StringReader(pagedata));
			    	courses = parser.getCourses();
			    	
				} catch (IOException e) {
					errorMsg = "UTilities could not fetch your Blackboard Dashboard";
					e.printStackTrace();
					cancel(true);
					return null;
				} catch(XmlPullParserException e) {
		    		errorMsg = "UTilities could not parse the downloaded Dashboard data.";
		    		showButton = true;
		    		this.pagedata = pagedata;
		    		ex = e;
		    		e.printStackTrace();
		    		cancel(true);
		    		return null;	        	 
		    	}	
	    	}
	    	tl.addSplit("XML parsed");
	   // 	tl.dumpToLog();
			return feedList;
		}
		@Override
		protected void onPostExecute(List<ParcelablePair<String, List<FeedItem>>> result) {
			//	dlv.setAdapter(new BlackboardDashboardAdapter(result));
			feedList.addAll(tempFeedList);
			bda.notifyDataSetChanged();
			tl.addSplit("Adapter created");
			d_pb_ll.setVisibility(View.GONE);
			dlv.setVisibility(View.VISIBLE);
			ell.setVisibility(View.GONE);	
		}
		@Override
		protected void onCancelled(List<ParcelablePair<String, List<FeedItem>>> result) {
			etv.setText(errorMsg);
			if(showButton) {
				eb.setText("Send anonymous information about the dashboard to the developer to help improve UTilities.");
				eb.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(pagedata != null && ex != null) {
							SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getSherlockActivity().getBaseContext()); 
							if(!sp.getBoolean("acra.enable", true))
								ACRA.getErrorReporter().setEnabled(true);
							ACRA.getErrorReporter().putCustomData("xmldata", pagedata);
				        	ACRA.getErrorReporter().handleException(ex);
				        	ACRA.getErrorReporter().removeCustomData("xmldata");
				        	if(!sp.getBoolean("acra.enable", true))
								ACRA.getErrorReporter().setEnabled(false);
				        	Toast.makeText(getSherlockActivity(), "Data is being sent, thanks for helping out!", Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(getSherlockActivity(), "Couldn't send the dashboard data for some reason :(", Toast.LENGTH_SHORT).show();
						}
						v.setVisibility(View.INVISIBLE);
						
					}
				});
				eb.setVisibility(View.VISIBLE);
			}
			d_pb_ll.setVisibility(View.GONE);
    		dlv.setVisibility(View.GONE);
			ell.setVisibility(View.VISIBLE); 
		}
	}
	
	//TODO: figure out fast scroll, maybe the min-sdk is just too low...
	class BlackboardDashboardAdapter extends AmazingAdapter {
		
		private List<ParcelablePair<String, List<FeedItem>>> items;
		
		public BlackboardDashboardAdapter(List<ParcelablePair<String, List<FeedItem>>> items) {
			this.items = items;
		}
		
		@Override
		public int getCount() {
			int res = 0;
			for (int i = 0; i < items.size(); i++) {
				res += items.get(i).second.size();
			}
			return res;
		}
		
		@Override
		public boolean isEnabled(int position) {
			return !("Unknown".equals(getItem(position).getType()) || "Notification".equals(getItem(position).getType())); 
		}

		@Override
		public FeedItem getItem(int position) {
			int c = 0;
			for (int i = 0; i < items.size(); i++) {
				if (position >= c && position < c + items.get(i).second.size()) {
					return items.get(i).second.get(position - c);
				}
				c += items.get(i).second.size();
			}
			return null;
		}
		
		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		protected void onNextPageRequested(int page) {}
		
		@Override
		protected void bindSectionHeader(View view, int position, boolean displaySectionHeader) {
			if (displaySectionHeader) {
				view.findViewById(R.id.header).setVisibility(View.VISIBLE);
				TextView lSectionTitle = (TextView) view.findViewById(R.id.header);
				lSectionTitle.setText(getSections()[getSectionForPosition(position)]);
			} else {
				view.findViewById(R.id.header).setVisibility(View.GONE);
			}
		}
		
		//TODO
		@Override
		public View getAmazingView(int position, View convertView, ViewGroup parent) {
			View res = convertView;
			if (res == null) res = getSherlockActivity().getLayoutInflater().inflate(R.layout.dashboard_item_view, null);
			FeedItem fi = getItem(position);
			BBClass course = courses.get(fi.getBbId());
			
			TextView courseName = (TextView) res.findViewById(R.id.d_course_name);
			TextView contentType = (TextView) res.findViewById(R.id.d_content_type);
			TextView message = (TextView) res.findViewById(R.id.d_message);

			if(!longform) {
				//TODO: make this look nice for malformed classes
				if(!"".equals(course.getCourseId()))
					courseName.setText(course.getCourseId()+ " - " + course.getName()+ " ("+course.getUnique()+")");
				else
					courseName.setText(course.getName()+ " ("+course.getUnique()+")");
			} else {
				courseName.setText(course.getFullName());
			}
			
			contentType.setText(fi.getType());
			message.setText(fi.getMessage());
				
			if(!isEnabled(position)) {	
				TypedValue tv = new TypedValue();
				if(getSherlockActivity().getTheme().resolveAttribute(android.R.attr.textColorTertiary, tv, true)) {	
					TypedArray arr = getSherlockActivity().obtainStyledAttributes(tv.resourceId, new int[] {android.R.attr.textColorTertiary});
					message.setTextColor(arr.getColor(0, Color.BLACK));
					courseName.setTextColor(arr.getColor(0, Color.BLACK));
				}
			} else {	
				message.setTextColor(Color.BLACK);
				courseName.setTextColor(Color.BLACK);
			}
			return res;
		}
		
		@Override
		public void configurePinnedHeader(View header, int position, int alpha) {
			TextView lSectionHeader = (TextView)header;
			lSectionHeader.setText(getSections()[getSectionForPosition(position)]);
			//	lSectionHeader.getBackground().setAlpha(alpha);
			//	lSectionHeader.setBackgroundColor(alpha << 24 | (0xEAEAEA));
			//	lSectionHeader.setTextColor(alpha << 24 | (0x343434));
		}
		
		@Override
		public int getPositionForSection(int section) {
			if (section < 0) section = 0;
			if (section >= items.size()) section = items.size() - 1;
			int c = 0;
			for (int i = 0; i < items.size(); i++) {
				if (section == i) { 
					return c;
				}
				c += items.get(i).second.size();
			}
			return 0;
		}
		
		@Override
		public int getSectionForPosition(int position) {
			int c = 0;
			for (int i = 0; i < items.size(); i++) {
				if (position >= c && position < c + items.get(i).second.size()) {
					return i;
				}
				c += items.get(i).second.size();
			}
			return 0;
		}
		
		@Override
		public String[] getSections() {
			String[] res = new String[items.size()];
			for (int i = 0; i < items.size(); i++) {
				res[i] = items.get(i).first;
			}
			return res;
		}
		
		@Override
		protected View getLoadingView(ViewGroup parent) {
			return null;
		}
	}
}

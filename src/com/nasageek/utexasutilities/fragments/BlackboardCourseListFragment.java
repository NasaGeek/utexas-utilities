package com.nasageek.utexasutilities.fragments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;

import retrofit.RetrofitError;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.foound.widget.AmazingListView;
import com.mapsaurus.paneslayout.FragmentLauncher;
import com.mapsaurus.paneslayout.PanesActivity;
import com.nasageek.utexasutilities.AsyncTask;
import com.nasageek.utexasutilities.ConnectionHelper;
import com.nasageek.utexasutilities.ParcelablePair;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.SectionedParcelableList;
import com.nasageek.utexasutilities.adapters.BBClassAdapter;
import com.nasageek.utexasutilities.fragments.canvas.CanvasCourseMapFragment;
import com.nasageek.utexasutilities.model.BBCourse;
import com.nasageek.utexasutilities.model.Course;
import com.nasageek.utexasutilities.model.canvas.CanvasCourse;
import com.nasageek.utexasutilities.requests.CanvasCourseListRequest;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

public class BlackboardCourseListFragment extends BaseSpiceFragment {
	
	private DefaultHttpClient httpclient;
	private LinearLayout bb_pb_ll;
	private TextView bbetv;
	private LinearLayout bbell;
	private Button bbeb;
	
	private AmazingListView bblv;
	private ArrayList<Course> classList;
	private List<ParcelablePair<String, List<Course>>> classSectionList;
	private fetchClassesTask fetch;	
//	private ArrayList<ParcelablePair<String, ArrayList<BBClass>>> classes;
	private BBClassAdapter classAdapter;
	private CanvasCourseListRequest canvasCourseListRequest;
	
	public BlackboardCourseListFragment() {}
	
	public static BlackboardCourseListFragment newInstance(String title) {
		BlackboardCourseListFragment f = new BlackboardCourseListFragment();
		Bundle args = new Bundle();
		args.putString("title", title);
		f.setArguments(args);
		
		return f;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
    	classList = new ArrayList<Course>();
    	
    	canvasCourseListRequest = new CanvasCourseListRequest(ConnectionHelper.getCanvasAuthCookie(getActivity()));
    	if(savedInstanceState == null)
    		classSectionList = new ArrayList<ParcelablePair<String, List<Course>>>();
    	else
    		classSectionList = (ArrayList<ParcelablePair<String, List<Course>>>) ((SectionedParcelableList) savedInstanceState.getParcelable("classSectionList")).getList();
		
		httpclient = ConnectionHelper.getThreadSafeClient();
		httpclient.getCookieStore().clear();
		BasicClientCookie cookie = new BasicClientCookie("s_session_id", ConnectionHelper.getBBAuthCookie(getSherlockActivity(), httpclient));
    	cookie.setDomain(ConnectionHelper.blackboard_domain_noprot);
    	httpclient.getCookieStore().addCookie(cookie);
    	
    	classAdapter = new BBClassAdapter(getSherlockActivity(), classSectionList);
	}

	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {	
		View vg = inflater.inflate(R.layout.blackboard_courselist_fragment, container, false);

    	bb_pb_ll = (LinearLayout) vg.findViewById(R.id.blackboard_progressbar_ll);
    	bblv = (AmazingListView) vg.findViewById(R.id.blackboard_class_listview);
    	
    	bbell = (LinearLayout) vg.findViewById (R.id.blackboard_error);
    	bbetv = (TextView) vg.findViewById(R.id.tv_failure);
    	bbeb = (Button) vg.findViewById(R.id.button_send_data);
    			
		bblv.setAdapter(classAdapter);
		bblv.setPinnedHeaderView(getSherlockActivity().getLayoutInflater().inflate(R.layout.menu_header_item_view, bblv, false));		
		bblv.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				//TODO: figure out course id stuff here
				Course course = (Course)(parent.getItemAtPosition(position));
				String type = course.getType();
		
				SherlockFragmentActivity act = getSherlockActivity();
				Fragment topFragment = null;
				if(act != null && act instanceof PanesActivity) {	
					topFragment = ((PanesActivity)act).getTopFragment();
					//we're on a tablet, PanesActivity acts a bit odd with them
					if(((PanesActivity)act).getMenuFragment() == topFragment)
						topFragment = null;
				}
				//don't re-add the current displayed course, instead just show it
				if(act != null && act instanceof FragmentLauncher) {	
					if(type.equals("blackboard")) {
						BBCourse bbclass = (BBCourse)course;
						/*if(topFragment == null || 
						  (topFragment != null && topFragment instanceof BlackboardFragment && 
						  (!((BlackboardFragment)topFragment).getBbid().equals(bbclass.getId())) || ((BlackboardFragment)topFragment).isFromDashboard()))
						{*/	((FragmentLauncher)act).addFragment(BlackboardCourseListFragment.this.getParentFragment(), 					
									BlackboardCourseMapFragment.newInstance(getString(R.string.coursemap_intent), null, bbclass.getId(), bbclass.getCourseCode(), "Course Map", "", -1, false));
															
					/*	}
						else if(act instanceof PanesActivity)
							((PanesActivity) act).showContent();*/
					} else if(type.equals("canvas")) {
						CanvasCourse ccourse = (CanvasCourse)course;
						//launch canvas coursemap
						((FragmentLauncher)act).addFragment(BlackboardCourseListFragment.this.getParentFragment(), 					
								CanvasCourseMapFragment.newInstance(ccourse.getId(), ccourse.getName(), ccourse.getCourseCode()));
							
					}
				}		
			}
		});
		
		//TODO: where to callll, also, helper?  -  helper for what? shit I don't remember writing this...
		if(classSectionList.size() == 0) {
	    	fetch = new fetchClassesTask(httpclient);
	    	
	    	if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
				fetch.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			else
				fetch.execute();  	
    	}
		return vg;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable("classSectionList", new SectionedParcelableList<Course>(classSectionList, Course.class));
	}

	public final class CanvasCourseListRequestListener implements RequestListener<CanvasCourse.List> {

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            Toast.makeText(getSherlockActivity(), "failure", Toast.LENGTH_SHORT).show();
            //if request was unauthorized, token's probably bad
            if(((RetrofitError)spiceException.getCause()).getResponse().getStatus() == 401) {
            	ConnectionHelper.resetCanvasAuthToken(getActivity());
            }
        }

        @Override
        public void onRequestSuccess(final CanvasCourse.List result) {
            int i = 0;
            //classSectionList guaranteed to be populated, this supposes the two lists are ordered the same
            //in this case most recent to least
            for(int j = 0; j < classSectionList.size(); j++) {
	        	while(i < result.size() && result.get(i).getTermName().equals(classSectionList.get(j).first)) {
	        		classSectionList.get(j).second.add(result.get(i));
	        		i++;
	        	}
	        	if(j == classSectionList.size() - 1) {
	        		classSectionList.get(j).second.addAll(result.subList(i, result.size()));
	        	}
            }
            classAdapter.notifyDataSetChanged();
        }
    }
	
	private class fetchClassesTask extends AsyncTask<Object, Void, ArrayList<ParcelablePair<String, List<Course>>>> {
		private DefaultHttpClient client;
		private String errorMsg;
		private Exception ex;
		private String pagedata;
		
		public fetchClassesTask(DefaultHttpClient client) {
			this.client = client;
		}
		
		@Override
		protected void onPreExecute() {
			bb_pb_ll.setVisibility(View.VISIBLE);
			bbell.setVisibility(View.GONE);
    		bblv.setVisibility(View.GONE);
		}
		
		@Override
		protected ArrayList<ParcelablePair<String, List<Course>>> doInBackground(Object... params) {
			HttpGet hget = new HttpGet(ConnectionHelper.blackboard_domain + "/webapps/Bb-mobile-BBLEARN/enrollments?course_type=COURSE");
	    	String pagedata="";

	    	try {
				HttpResponse response = client.execute(hget);
		    	pagedata = EntityUtils.toString(response.getEntity());
			} catch (Exception e) {
				errorMsg = "UTilities could not fetch the Blackboard course list";
				e.printStackTrace();
				cancel(true);
				return null;
			}

	    	Pattern class_pattern = Pattern.compile("bbid=\"(.*?)\" name=\"(.*?)\" courseid=\"(.*?)\"");
	    	Matcher class_matcher = class_pattern.matcher(pagedata);
	    	
	    	while(class_matcher.find()) {
	    		classList.add(new BBCourse(class_matcher.group(2).replace("&amp;","&"),class_matcher.group(1).replace("&amp;","&"),class_matcher.group(3)));	
	    	}	    	
	    	//build the sectioned list now
	    	String currentCategory = "";
    		ArrayList<Course> sectionList = null;
			ArrayList<ParcelablePair<String, List<Course>>> tempClassSectionList = new ArrayList<ParcelablePair<String, List<Course>>>();

			for(int i = 0; i < classList.size(); i++) {
    			//first course is always in a new category (the first category)
				if(i == 0) {	
    				currentCategory = classList.get(i).getTermName();
    				sectionList = new ArrayList<Course>();
    				sectionList.add(classList.get(i));
    			}
				//if the current course is not part of the current category or we're on the last course
				//weird stuff going on here depending on if we're at the end of the course list
    			else if(!classList.get(i).getTermName().equals(currentCategory) || i == classList.size() - 1) {
    				
    				if(i == classList.size() - 1)
    					sectionList.add(classList.get(i));
    					
    				tempClassSectionList.add(new ParcelablePair<String, List<Course>>(currentCategory, sectionList));
    				
    				currentCategory = classList.get(i).getTermName();
    				sectionList= new ArrayList<Course>();
    				
    				if(i != classList.size() - 1)
    					sectionList.add(classList.get(i));
    			}
				//otherwise just add to the current category
    			else {
    				sectionList.add(classList.get(i));
    			}  			
    		}
    		Collections.reverse(tempClassSectionList);
    		/*Collections.sort(tempClassSectionList, new Comparator<ParcelablePair<String, List<BBClass>>>() {

				@Override
				public int compare(ParcelablePair<String, List<BBClass>> lhs,
						ParcelablePair<String, List<BBClass>> rhs) {
					return -lhs.first.compareTo(rhs.first);
				}
			});*/
			return tempClassSectionList;
		}
		@Override
		protected void onPostExecute(ArrayList<ParcelablePair<String, List<Course>>> result) {	    		
			classSectionList.addAll(result);
			classAdapter.notifyDataSetChanged();
			//TODO: learn to thread properly :(
			getSpiceManager().execute(canvasCourseListRequest, "courses", DurationInMillis.ONE_MINUTE * 5, new CanvasCourseListRequestListener());
			
			
			bb_pb_ll.setVisibility(View.GONE);
			bbell.setVisibility(View.GONE);
    		bblv.setVisibility(View.VISIBLE);	
		}
		@Override
		protected void onCancelled() {
			bbetv.setText(errorMsg);
			bb_pb_ll.setVisibility(View.GONE);
			bbell.setVisibility(View.VISIBLE);
    		bblv.setVisibility(View.GONE);
		}
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(fetch != null)
			fetch.cancel(true);
	}
}
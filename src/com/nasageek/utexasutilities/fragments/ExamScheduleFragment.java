package com.nasageek.utexasutilities.fragments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.crittercism.app.Crittercism;
import com.nasageek.utexasutilities.ConnectionHelper;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.activities.CampusMapActivity;

public class ExamScheduleFragment extends SherlockFragment implements ActionModeFragment{

	private boolean noExams;
	private TextView login_first;
	private DefaultHttpClient httpclient;
	private ArrayList<String> examlist;
	private ListView examlistview;
	private ExamAdapter ea;
	private LinearLayout pb_ll;
	private SherlockFragmentActivity parentAct;
//	private View vg;
	public ActionMode mode;
	private TextView netv;
	private TextView eetv;
	String semId;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{			
		View vg = inflater.inflate(R.layout.exam_schedule_fragment_layout, container, false);
		
		updateView(semId, vg);
		
		return vg;
	}
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		parentAct = this.getSherlockActivity();
		semId = getArguments().getString("semdId");
	}
	public void updateView(String semId, View vg)
	{
		this.semId = semId;
		
		examlist=  new ArrayList<String>();
		login_first = (TextView) vg.findViewById(R.id.login_first_tv);
		pb_ll = (LinearLayout) vg.findViewById(R.id.examschedule_progressbar_ll);
		examlistview = (ListView) vg.findViewById(R.id.examschedule_listview);
		netv = (TextView) vg.findViewById(R.id.no_exams);
		eetv = (TextView) vg.findViewById(R.id.examschedule_error);
		
		if(!ConnectionHelper.cookieHasBeenSet())
		{	
			pb_ll.setVisibility(View.GONE);
			login_first.setVisibility(View.VISIBLE);
		}
		else
		{
			parser();
		}
		
	}
	public void parser()
	{

		httpclient = ConnectionHelper.getThreadSafeClient();
		httpclient.getCookieStore().clear();
		
		BasicClientCookie cookie = new BasicClientCookie("SC", ConnectionHelper.getAuthCookie(parentAct,httpclient));
    	cookie.setDomain(".utexas.edu");
    	httpclient.getCookieStore().addCookie(cookie);
    	
    	new fetchExamDataTask(httpclient).execute();
	}
	public ActionMode getActionMode()
	{
		return mode;
	}
	private class fetchExamDataTask extends AsyncTask<Object,Void,Character>
	{
		private DefaultHttpClient client;
		private String errorMsg;
		
		public fetchExamDataTask(DefaultHttpClient client)
		{
			this.client = client;
		}
		@Override
		protected void onPreExecute()
		{
			pb_ll.setVisibility(View.VISIBLE);
			examlistview.setVisibility(View.GONE);
			netv.setVisibility(View.GONE);
		}
		@Override
		protected Character doInBackground(Object... params)
		{
			HttpGet hget = new HttpGet("https://utdirect.utexas.edu/registrar/exam_schedule.WBX");
	    	String pagedata="";

	    	try
			{
				HttpResponse response = client.execute(hget);
		    	pagedata = EntityUtils.toString(response.getEntity());
			} catch (Exception e)
			{
				errorMsg = "UTilities could not fetch your exam schedule";
				cancel(true);
				e.printStackTrace();
				return null;
			}
	    	if(pagedata.contains("<title>Information Technology Services - UT EID Logon</title>"))
	    	{
				errorMsg = "You've been logged out of UTDirect, back out and log in again.";
				if(getActivity() != null)
					ConnectionHelper.logout(getActivity());
				cancel(true);
				return null;
	    	}

	    	if(pagedata.contains("will be available approximately three weeks"))// || !tempId.equals(semId))
	    	{	
	    		noExams = true;
	    		return 'c';
	    	}
	    	else if(pagedata.contains("Our records indicate that you are not enrolled for the current semester."))
	    	{
	    		noExams = true;
	    		return 'b';
	    	}
	    	else
	    		noExams = false;
	    	Pattern rowpattern = Pattern.compile("<tr >.*?</tr>",Pattern.DOTALL);
	    	Matcher rowmatcher = rowpattern.matcher(pagedata);
	    	
	    	while(rowmatcher.find())
	    	{
	    		String rowstring = "";
	    		String row = rowmatcher.group();
	    		if(row.contains("Unique") || row.contains("Home Page"))
	    			continue;
	    		
	    		Pattern fieldpattern = Pattern.compile("<td.*?>(.*?)</td>",Pattern.DOTALL);
	    		Matcher fieldmatcher = fieldpattern.matcher(row);
	    		while(fieldmatcher.find())
	    		{
	    			String field = fieldmatcher.group(1).replace("&nbsp;"," ").trim().replace("\t","");
	    			Spanned span = Html.fromHtml(field);
	    			String out = span.toString();
	    			rowstring +=out+"^";
	    			
	    		}
	    	
	    		examlist.add(rowstring);
	    		
	    	}
	    	
	    	return ' ';
		}
		@Override
		protected void onPostExecute(Character result)
		{
			if(!noExams)
			{
				ea = new ExamAdapter(parentAct, examlist);
				examlistview.setAdapter(ea);
				examlistview.setOnItemClickListener(ea);
				examlistview.setVisibility(View.VISIBLE);
			}
			else
			{
				switch(result)
				{
					case 'c':netv.setText("'Tis not the season for exams.\nTry back later!\n(about 3 weeks before they begin)");break;
					case 'b':netv.setText("You aren't enrolled for the current semester.");break;
				}
	    		
	    		netv.setVisibility(View.VISIBLE);
			}
			pb_ll.setVisibility(View.GONE);
			
		}
		@Override
		protected void onCancelled()
		{
			eetv.setText(errorMsg);
			
			netv.setVisibility(View.GONE);
			pb_ll.setVisibility(View.GONE);
			examlistview.setVisibility(View.GONE);
			login_first.setVisibility(View.GONE);
			eetv.setVisibility(View.VISIBLE);
			
		}
	}
	
	private class ExamAdapter extends ArrayAdapter<String> implements AdapterView.OnItemClickListener
	{
			private Context con;
			private ArrayList<String> exams;
			private LayoutInflater li;

			public ExamAdapter(Context c, ArrayList<String> objects)
			{
				super(c,0,objects);
				con = c;
				exams = objects;
				li = (LayoutInflater)con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			}
			public int getCount() {
				return exams.size();
			}

			public String getItem(int position) {
				return exams.get(position);
			}

			public long getItemId(int position) {
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
				String[] examdata = exams.get(position).split("\\^");
				boolean examRequested = false, summerSession = false;
				String id="", name="", date="", location="", unique="";
				
				
				//TODO: I hate doing these try/catches, find a better solution so I know when stuff goes wrong? ACRA?
				try
				{
					examRequested = !examdata[2].contains("The department");
					summerSession = examdata[2].contains("Information on final exams is available for Nine-Week Summer Session(s) only.");	
				
					unique = examdata[0]; 
					id = examdata[1];
					name = examdata[2];
					date = "";
					location = "";
					if(examRequested && !summerSession && examdata.length >= 5)
					{	date = examdata[3];
						location = examdata[4];
					}
				}
				catch(ArrayIndexOutOfBoundsException ex)
				{
					ex.printStackTrace();
					Crittercism.leaveBreadcrumb("Exam parse error "+Arrays.toString(examdata));
					
				}
				String course = "";
				ViewGroup vg = (ViewGroup)convertView;
				vg =(ViewGroup)li.inflate(R.layout.exam_item_view,null,false);
				TextView courseview = (TextView) vg.findViewById(R.id.exam_item_header_text);
				
				if(!examRequested || summerSession)
				{
					course = id + " - " + unique;
					TextView left= (TextView) vg.findViewById(R.id.examdateview);
					left.setText(name);
				}
				else
				{
					course = id+" "+name;
					TextView left= (TextView) vg.findViewById(R.id.examdateview);
					left.setText(date);
					TextView right = (TextView) vg.findViewById(R.id.examlocview);
					right.setText(location);
				}
				
				courseview.setText(course);
				
				return (View)vg;
				
			}
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				mode = ExamScheduleFragment.this.parentAct.startActionMode(new ScheduleActionMode(position));
			}
			final class ScheduleActionMode implements ActionMode.Callback {
		        
				int position;
				
				public ScheduleActionMode(int pos)
				{
					position = pos;
				}
				
				@Override
		        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		            mode.setTitle("Exam Info");
		            MenuInflater inflater = getSherlockActivity().getSupportMenuInflater();
		            String[] elements = exams.get(position).split("\\^");
		            if(elements.length >= 3) //TODO: check this?
		            {
		            	if(elements[2].contains("The department") || elements[2].contains("Information on final exams is available for Nine-Week Summer Session(s) only."))
	    				{
	    					return true;
	    				}
		            }
		            else
		            	return true;
		            inflater.inflate(R.menu.schedule_action_mode, menu);
		            return true;
		        }

		        @Override
		        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		            return false;
		        }

		        @Override
		        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		            switch(item.getItemId())
		            {
		            	case R.id.locate_class:
		            		ArrayList<String> building = new ArrayList<String>();
		            		Intent map = new Intent(con.getString(R.string.building_intent), null, con, CampusMapActivity.class);
		            		
		            		String[] elements = exams.get(position).split("\\^");
		            		if(elements.length >= 5)
		    				{	
		    					building.add(elements[4].split(" ")[0]);
		    					map.putStringArrayListExtra("buildings",building);
		            	//		map.setData(Uri.parse(elements[4].split(" ")[0]));
		    					con.startActivity(map);
		    					return true;
		    				}
		            		else
		            		{
		            			Toast.makeText(con, "Your exam's location could not be found", Toast.LENGTH_SHORT).show();
		            		}
		            }
		            return true;
		        }

		        @Override
		        public void onDestroyActionMode(ActionMode mode) {
		        }
		    }
			
			
		}
}

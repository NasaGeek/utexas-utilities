package com.nasageek.UTilities;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;



import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ExamScheduleActivity extends Activity
{
	private boolean noExams;
	private ProgressDialog pd;
	private ConnectionHelper ch;
	private DefaultHttpClient httpclient;
	private SharedPreferences settings;
	private ArrayList<String> examlist;
	private ListView examlistview;
	private LinearLayout linlay;
	private ExamAdapter ea;
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.examschedule_layout);
		
		pd = ProgressDialog.show(ExamScheduleActivity.this, "", "Loading...");
		examlistview = (ListView) findViewById(R.id.examschedule_listview);
		linlay = (LinearLayout) findViewById(R.id.examschedule_linlay);
		
		examlist=  new ArrayList<String>();
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		ch = new ConnectionHelper(this);
		try
		{
			parser();
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void parser() throws Exception
	{

		httpclient = ConnectionHelper.getThreadSafeClient();
		httpclient.getCookieStore().clear();
		/*if(settings.getBoolean("loginpref", true))
		{
			if(!ch.Login(this, httpclient))	
			{	
				finish();
				return;
			}
		}
		
		if(ConnectionHelper.getAuthCookie(this,httpclient).equals(""))
		{
			finish();
			return;
		}*/
		BasicClientCookie cookie = new BasicClientCookie("SC", ConnectionHelper.getAuthCookie(this,httpclient));
    	cookie.setDomain(".utexas.edu");
    	httpclient.getCookieStore().addCookie(cookie);
    	
    	new fetchExamDataTask(httpclient).execute();
	}
	
	private class fetchExamDataTask extends AsyncTask<Object,Void,Character>
	{
		private DefaultHttpClient client;
		
		public fetchExamDataTask(DefaultHttpClient client)
		{
			this.client = client;
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	    	if(pagedata.contains("will be available approximately three weeks"))
	    	{	
	    		noExams = true;
	    		return ' ';
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
				ea = new ExamAdapter(ExamScheduleActivity.this, examlist);
				examlistview.setAdapter(ea);
				examlistview.setOnItemLongClickListener(ea);
			}
			else
			{
				TextView tv = new TextView(ExamScheduleActivity.this);
	    		tv.setText("      'Tis not the season for exams.\n                    Try back later!\n  (about 3 weeks before they begin)");
	    		tv.setTextSize(19);
	    		linlay.removeAllViews();
	    		linlay.addView(tv);
			}
			
			if(pd.isShowing())
			{
				pd.cancel();
			}
		}
	}
	
	private class ExamAdapter extends ArrayAdapter implements AdapterView.OnItemLongClickListener
	{
		
			private Context con;
			private ArrayList<String> exams;
			private LayoutInflater li;
			private String currentDate;
			boolean isSectionHeader;
			
			@SuppressWarnings("unchecked")
			public ExamAdapter(Context c, ArrayList<String> objects)
			{
				super(c,0,objects);
				con = c;
				exams = objects;
				li = (LayoutInflater)con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			}
			public int getCount() {
				// TODO Auto-generated method stub
				return exams.size();
			}

			public Object getItem(int position) {
				// TODO Auto-generated method stub
				return exams.get(position);
			}

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
						
				String[] elements = exams.get(position).split("\\^");
				String course = "";
				ViewGroup vg = (ViewGroup)convertView;
				vg =(ViewGroup)li.inflate(R.layout.exam_item_view,null,false);
				TextView courseview = (TextView) vg.findViewById(R.id.exam_item_header_text);
				
			/*	if(exams.get(position).contains("not the season for exam"))
				{
					TextView tv = new TextView(ExamScheduleActivity.this);
					tv.setTextSize(19);
					tv.setText(exams.get(position));
					vg.removeAllViews();
					vg.addView(tv);
					
					return (View)vg;
				}*/
				
				if(elements[2].contains("The department"))
				{
					course = elements[1];
					TextView left= (TextView) vg.findViewById(R.id.examdateview);
					left.setText(elements[2]);
				}
				else
				{
					course = elements[1]+" "+elements[2];
					TextView left= (TextView) vg.findViewById(R.id.examdateview);
					left.setText(elements[3]);
					TextView right = (TextView) vg.findViewById(R.id.examlocview);
					right.setText(elements[4]);
				}
				
				courseview.setText(course);
				
				return (View)vg;
				
			}
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
			{
				
				Intent map = new Intent(con.getString(R.string.building_intent), null, con, CampusMapActivity.class);
				
				String[] elements = exams.get(position).split("\\^");
				if(elements[2].contains("The department"))
				{
					return true;
				}
				else
				{	
					map.setData(Uri.parse(elements[4].split(" ")[0]));
					con.startActivity(map);
					return true;
				}
			}
		}

	
}

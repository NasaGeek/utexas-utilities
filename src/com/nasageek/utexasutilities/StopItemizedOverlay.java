package com.nasageek.utexasutilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class StopItemizedOverlay extends ItemizedOverlay{
    private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
    private Context mContext;
    private ArrayList<String> stops;
    private String routeid;
    public Toast stoptimes;
 //   private String stopid;

    public StopItemizedOverlay(Drawable defaultMarker, String routeid, Context context) {
    	super(boundCenterBottom(defaultMarker));
    	this.routeid = routeid;
    	stops = new ArrayList<String>();
          mContext = context;
          stoptimes = Toast.makeText(mContext, "", Toast.LENGTH_LONG);
          
    }

    public void addOverlay(OverlayItem overlay, String stopid) {
        mOverlays.add(overlay);
        stops.add(stopid);
        populate();
    }

    @Override
    protected OverlayItem createItem(int i) {
      return mOverlays.get(i);
    }

    @Override
    public int size() {
      return mOverlays.size();
    }
    @Override
    protected boolean onTap(int i)
    {
    	
		stoptimes.setText("Loading times...");
		stoptimes.show();
    	new checkStopTask().execute(i);

    	return true;

    }
    private class checkStopTask extends AsyncTask<Integer,Void,String>
	{
		@Override
		protected String doInBackground(Integer... params)
		{
			int i = (int)params[0];
			String times="Oops! There are no specified times for this stop on capmetro.org ";
			DefaultHttpClient httpclient = ConnectionHelper.getThreadSafeClient();
			String data="";
			
			HttpResponse response=null;
			try
			{
				response = httpclient.execute(new HttpGet("http://capmetro.org/planner/s_service.asp?tool=NB&stopid="+stops.get(i)));
				data = EntityUtils.toString(response.getEntity());
			} catch (ClientProtocolException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Pattern pattern = Pattern.compile("<b>.*?(?=</p>)", Pattern.DOTALL);
			Matcher matcher = pattern.matcher(data);
		//	ArrayList<String> times = new ArrayList();
			while(matcher.find())
			{
				Pattern pattern2 = Pattern.compile("(?<=<b>)\\d+(?=</b>)");
			//	Log.d("ROUTE", matcher.group());
				Matcher matcher2 = pattern2.matcher(matcher.group());

				if(matcher2.find())
				{	
					String a = matcher2.group();
					if(matcher2.group().equals(routeid))
					{
						times = mOverlays.get(i).getTitle()+":\n";
						
						Pattern pattern3 = Pattern.compile("<span.*?>(.*?)(?=</span>)");
						Matcher matcher3 = pattern3.matcher(matcher.group());
						while(matcher3.find())
						{
							times+=matcher3.group(1)+"\n";
						}
						break;
					}
				}
				
			}
			return times;
		}
		protected void onPostExecute( String times)
		{
		
			stoptimes.setText(times.substring(0,times.length()-1));
			stoptimes.show();
		}
	}
}
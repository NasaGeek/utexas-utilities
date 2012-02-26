package com.nasageek.UTilities;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.series.XYSeries;
import com.androidplot.xy.*;

public class DataUsageActivity extends Activity implements OnTouchListener
{
	
	private DefaultHttpClient httpclient;
	private SharedPreferences settings;
	private ConnectionHelper ch;
	private Float[] updata,downdata,totaldata; 
	private Long[] labels;
	private XYPlot graph;
	private ProgressBar mProgress;
	private TextView dataUsedText;
	private String usedText;
	private ProgressDialog pd;
	private LinearLayout d_pb_ll;
	
	private PointD minXY;
	private PointD maxXY;
	private double absMinX;
	private double absMaxX;
	private double minNoError;
	private double maxNoError;
	private double minDif;
	private double maxDif;
	
	private double percentused;
 
	final private double difPadding = 0.1;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	//	pd = ProgressDialog.show(DataUsageActivity.this, "", "Loading...");
		
		setContentView(R.layout.data_layout);

		d_pb_ll = (LinearLayout) findViewById(R.id.data_progressbar_ll);
		dataUsedText = (TextView) findViewById(R.id.dataUsedText);
		mProgress = (ProgressBar) findViewById(R.id.percentDataUsed);
		
		graph = (XYPlot) findViewById(R.id.mySimpleXYPlot);
		graph.setOnTouchListener(this);
		graph.disableAllMarkup();

		
		ch = new ConnectionHelper(this);
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		
		labels = new Long[288];//2017];
		downdata = new Float[288];//2017];
	//	updata = new Float[2017];
		totaldata = new Float[288];
		
		
		
	/*	if(ConnectionHelper.PNACookieHasBeenSet())
		{
			ConnectionHelper.resetPNACookie();
		}
		
		
		
		if(settings.getBoolean("loginpref", true))
		{
			if(!ch.PNALogin(this, httpclient))	
			{	
				finish();
				return;
			}
		}
		
		if(ConnectionHelper.getPNAAuthCookie(httpclient,this).equals(""))
		{
			finish();
			return;
		}*/
		httpclient = ConnectionHelper.getThreadSafeClient();
		httpclient.getCookieStore().clear();
		BasicClientCookie cookie = new BasicClientCookie("AUTHCOOKIE", ConnectionHelper.getPNAAuthCookie(this,httpclient));
    	cookie.setDomain(".pna.utexas.edu");
    	httpclient.getCookieStore().addCookie(cookie);
    	new fetchDataTask(httpclient).execute();
		new fetchProgressTask(httpclient).execute();	
	}
	
	
	// Definition of the touch states
	static final private int NONE = 0;
	static final private int ONE_FINGER_DRAG = 1;
	static final private int TWO_FINGERS_DRAG = 2;
	private int mode = NONE;
 
	private PointF firstFinger;
	private float lastScrolling;
	private float distBetweenFingers;
	private float lastZooming;
 
	
	public boolean onTouch(View arg0, MotionEvent event) {
		switch(event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN: // Start gesture
				firstFinger = new PointF(event.getX(), event.getY());
				mode = ONE_FINGER_DRAG;
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
				//When the gesture ends, a thread is created to give inertia to the scrolling and zoom 
				final Timer t = new Timer();
				t.schedule(new TimerTask() {
					@Override
					public void run() {
						while(Math.abs(lastScrolling) > 1f || Math.abs(lastZooming - 1) > 1.01) {
							lastScrolling *= .8;	//speed of scrolling damping
							scroll(lastScrolling);
							lastZooming += (1 - lastZooming) * .2;	//speed of zooming damping
							zoom(lastZooming);
							
							try {
								graph.postRedraw();
							} catch (final InterruptedException e) {
								e.printStackTrace();
							}
							// the thread lives until the scrolling and zooming are imperceptible
						}
					}
				}, 0);break;
 
			case MotionEvent.ACTION_POINTER_DOWN: // second finger
				distBetweenFingers = spacing(event);
				// the distance check is done to avoid false alarms
				if (distBetweenFingers > 5f)
					mode = TWO_FINGERS_DRAG;
				break;
			case MotionEvent.ACTION_MOVE:
				if (mode == ONE_FINGER_DRAG) {
					final PointF oldFirstFinger = firstFinger;
					firstFinger = new PointF(event.getX(), event.getY());
					lastScrolling = oldFirstFinger.x - firstFinger.x;
					scroll(lastScrolling);
					lastZooming = (firstFinger.y - oldFirstFinger.y) / graph.getHeight();
					if (lastZooming < 0)
						lastZooming = 1 / (1 - lastZooming);
					else
						lastZooming += 1;
					zoom(lastZooming);
					
					graph.redraw();
 
				}else if (mode == TWO_FINGERS_DRAG) {
					final float oldDist = distBetweenFingers;
					distBetweenFingers = spacing(event);
					if(distBetweenFingers > 0)
					{	lastZooming = oldDist / distBetweenFingers;
						zoom(lastZooming);
						checkBoundaries();
						graph.redraw();}
					else break;
				}
				break;
		}
		return true;
	}
 
	private void zoom(float scale) {
		final double domainSpan = maxXY.x - minXY.x;
		final double domainMidPoint = maxXY.x - domainSpan / 2.0f;
		final double offset = domainSpan * scale / 2.0f;
		minXY.x = domainMidPoint - offset;
		maxXY.x = domainMidPoint + offset;
		checkBoundaries();
		
	}
 
	private void scroll(float pan) {
		final double domainSpan = maxXY.x - minXY.x;
		final double step = domainSpan / graph.getWidth();
		final double offset = pan * step;
		minXY.x += offset;
		maxXY.x += offset;


		if (minXY.x < absMinX)
		{	minXY.x = absMinX;
			maxXY.x -= offset;//(absMinX-minXY.x);
			}
		else if (minXY.x > maxNoError)
			minXY.x = maxNoError;
		if (maxXY.x > absMaxX)
		{	maxXY.x = absMaxX;//(maxXY.x-absMaxX);
			minXY.x -= offset;//(maxXY.x-absMaxX);
			}
		else if (maxXY.x < minNoError)
			maxXY.x = minNoError;
		if (maxXY.x - minXY.x < minDif)
			maxXY.x = maxXY.x + (double) (minDif - (maxXY.x - minXY.x));
		
		graph.setDomainBoundaries(minXY.x, maxXY.x, BoundaryMode.FIXED);
		
	}
 
	private float spacing(MotionEvent event) {
		if(event.getPointerCount()>=2)
		{	final float x = event.getX(0) - event.getX(1);
			final float y = event.getY(0) - event.getY(1);
			return FloatMath.sqrt(x * x + y * y);
		}
		
		else return 1f;
	}
 
	private void checkBoundaries() {
		//Make sure the proposed domain boundaries will not cause plotting issues
		if (minXY.x < absMinX  || ((Double)minXY.x).equals(Double.NaN))
			minXY.x = absMinX;
		else if (minXY.x > maxNoError)
			minXY.x = maxNoError;
		if (maxXY.x > absMaxX ||  ((Double)maxXY.x).equals(Double.NaN))
			maxXY.x = absMaxX;
		else if (maxXY.x < minNoError)
			maxXY.x = minNoError;
		if (maxXY.x - minXY.x < minDif)
			maxXY.x = maxXY.x + (double) (minDif - (maxXY.x - minXY.x));
		else if (maxXY.x - minXY.x > maxDif)
			maxXY.x = maxXY.x + (double) (maxDif - (maxXY.x - minXY.x));
		graph.setDomainBoundaries(minXY.x, maxXY.x, BoundaryMode.FIXED);
	}
	private class fetchProgressTask extends AsyncTask<Object,Void,Void>
	{
		private DefaultHttpClient client;
		
		public fetchProgressTask(DefaultHttpClient client)
		{
			this.client = client;
		}
		
		
		protected Void doInBackground(Object... params)
		{
			HttpGet hget = new HttpGet("https://management.pna.utexas.edu/server/graph.cgi");
			
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
	    	
	    	

	        // Execute HTTP Post Request
	        
	    	
	    	
	    	
			Pattern percentpattern = Pattern.compile("\\((.+?)%\\)");
	    	Matcher percentmatcher = percentpattern.matcher(pagedata);
			percentmatcher.find();
			String found = percentmatcher.group(1);
			percentused = Double.parseDouble(found);
			
			Pattern usedpattern = Pattern.compile("<b>(.*?)</b>");
	    	Matcher usedmatcher = usedpattern.matcher(pagedata);
			usedmatcher.find();
			usedText = usedmatcher.group(1);
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Void v)
		{
			dataUsedText.setText(usedText);
			mProgress.setProgress((int)(percentused*10));
			
		}
		
	}

	private class fetchDataTask extends AsyncTask<Object,Void,Character>
	{
		private DefaultHttpClient client;
		
		public fetchDataTask(DefaultHttpClient client)
		{
			this.client = client;
		}
		
		@Override
		protected Character doInBackground(Object... params)
		{
	//		DefaultHttpClient httpclient = ch.getThreadSafeClient();

			Pattern authidpattern = Pattern.compile("(?<=%20)\\d+");
	    	Matcher authidmatcher = authidpattern.matcher(client.getCookieStore().getCookies().get(0).getValue());
			authidmatcher.find();
			HttpGet hget = new HttpGet("https://management.pna.utexas.edu/server/get-bw-graph-data.cgi?authid="+authidmatcher.group());
			
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
	    	
	    	
	    	String[] lines = pagedata.split("\n");
	    	Calendar date = Calendar.getInstance();
	    	
	    	
	    	
	    	for(int i = lines.length-288, x=0; i<lines.length; i++,x++)
	    	{
	    		String[] entry = lines[i].split(",");
	    		date.clear();
	    		
	    		date.set(Integer.parseInt(entry[0].split("/")[0]),Integer.parseInt(entry[0].split("/")[1])-1,Integer.parseInt(entry[0].split("/| ")[2]),Integer.parseInt(entry[0].split(" |:")[1]),Integer.parseInt(entry[0].split(" |:")[2]));
	    		labels[x]=date.getTimeInMillis();
	    	
	    		downdata[x]=(Float.valueOf(entry[1])); 
	    		
	    //		updata[x]=(Float.valueOf(entry[2]));
	    		totaldata[x]=(Float.valueOf(entry[3]));
	    	}
	    	
	    	
	   
			// TODO Auto-generated method stub
	    	
			return ' ';
		}
		@Override
		protected void onPostExecute(Character result)
		{
			XYSeries series = new SimpleXYSeries(Arrays.asList(labels), Arrays.asList(downdata),  "Downloaded");
			XYSeries seriestotal = new SimpleXYSeries(Arrays.asList(labels), Arrays.asList(totaldata),  "Uploaded");
			
			BarFormatter barformatter = new BarFormatter(Color.GREEN, Color.BLACK);
			BarFormatter barformatter2 = new BarFormatter(Color.CYAN, Color.BLACK);
			
			graph.setDomainLabel("Date");
			graph.getDomainLabelWidget().setHeight(30.0f);
			graph.getDomainLabelWidget().pack();
			
			graph.setRangeLabel("Data (MB)");
			graph.getRangeLabelWidget().setHeight(20.0f);
			graph.getRangeLabelWidget().pack();
			
			graph.addSeries(seriestotal, barformatter2);
			graph.addSeries(series, barformatter);
			
			
			graph.setTicksPerDomainLabel(4);
			
			graph.setDomainStep(XYStepMode.INCREMENT_BY_VAL,1800000);
			graph.setRangeStep(XYStepMode.INCREMENT_BY_VAL,30);
			
			
			
			
			graph.calculateMinMaxVals();
			minXY = new PointD(graph.getCalculatedMinX().doubleValue(),
					graph.getCalculatedMinY().doubleValue()); //initial minimum data point
			absMinX = minXY.x; //absolute minimum data point
			//absolute minimum value for the domain boundary maximum
			minNoError = Math.round(series.getX(1).doubleValue() + 2);
			maxXY = new PointD(graph.getCalculatedMaxX().doubleValue(),
					graph.getCalculatedMaxY().doubleValue()); //initial maximum data point
			
			graph.setTicksPerRangeLabel(maxXY.y>61?2:1);
			
			absMaxX = maxXY.x; //absolute maximum data point
			//absolute maximum value for the domain boundary minimum
			maxNoError = (double) Math.round(series.getX(series.size() - 1).doubleValue()) - 2;
	 
			//Check x data to find the minimum difference between two neighboring domain values
			//Will use to prevent zooming further in than this distance
			double temp1 = series.getX(0).doubleValue();
			double temp2 = series.getX(1).doubleValue();
			double temp3;
			double thisDif;
		/*	minDif = 1000000;	//increase if necessary for domain values
			for (int i = 2; i < series.size(); i++) {
				temp3 = series.getX(i).doubleValue();
				thisDif = Math.abs(temp1 - temp2);
				if (thisDif < minDif)
					minDif = thisDif;
				temp1 = temp2;
				temp2 = temp3;
			}*/
			minDif=3000000;
			maxDif=33000000;
			
			graph.setRangeUpperBoundary(maxXY.y>31?maxXY.y:31, BoundaryMode.FIXED);
			graph.setRangeLowerBoundary(0, BoundaryMode.FIXED);
			graph.setDomainUpperBoundary(maxXY.x, BoundaryMode.FIXED);
			graph.setDomainLowerBoundary(minXY.x, BoundaryMode.FIXED);
			checkBoundaries();
			 
		    graph.setDomainValueFormat(new TimeFormat());
			
	    	graph.redraw();
	    	
	    	graph.setVisibility(View.VISIBLE);
	    	d_pb_ll.setVisibility(View.GONE);
	    	
	  //  	if(pd.isShowing())
    //			pd.dismiss();
		}	
	}
	
	 private class TimeFormat extends Format 
	 {

		private static final long serialVersionUID = 1L;
		// create a simple date format that draws on the year portion of our timestamp.
         // see http://download.oracle.com/javase/1.4.2/docs/api/java/text/SimpleDateFormat.html
         // for a full description of SimpleDateFormat.
         private SimpleDateFormat dateFormat = new SimpleDateFormat("E - HH:mm");


         @Override
         public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
 
        	 long timestamp = ((Number) obj).longValue();
             Date date = new Date(timestamp);
             return dateFormat.format(date, toAppendTo, pos);
         }
         
        
         @Override
         public Object parseObject(String source, ParsePosition pos) {
             return null;

         }

         
 }
	 private class PointD
	 {
		 double x,y;
		 public PointD(double x, double y)
		 {
			 this.x=x;
			 this.y=y;
		 }
	 }
	
}

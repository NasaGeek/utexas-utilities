
package com.nasageek.utexasutilities.activities;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.androidplot.xy.BarFormatter;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;
import com.nasageek.utexasutilities.AsyncTask;
import com.nasageek.utexasutilities.ConnectionHelper;
import com.nasageek.utexasutilities.R;

import org.acra.ACRA;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataUsageActivity extends SherlockActivity implements OnTouchListener {

    private DefaultHttpClient httpclient;
    private Float[] downdata, totaldata;
    private Long[] labels;
    private XYPlot graph;
    private ProgressBar mProgress;
    private TextView dataUsedText;
    private TextView detv;
    private LinearLayout dell;
    private Button deb;
    private String usedText;
    private LinearLayout d_pb_ll;
    private ActionBar actionbar;

    private PointD minXY;
    private PointD maxXY;
    private double absMinX;
    private double absMaxX;
    private double minNoError;
    private double maxNoError;
    private double minDif;
    private double maxDif;

    private double percentused;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_layout);

        d_pb_ll = (LinearLayout) findViewById(R.id.data_progressbar_ll);
        dataUsedText = (TextView) findViewById(R.id.dataUsedText);
        mProgress = (ProgressBar) findViewById(R.id.percentDataUsed);
        dell = (LinearLayout) findViewById(R.id.data_error);
        detv = (TextView) findViewById(R.id.tv_failure);
        deb = (Button) findViewById(R.id.button_send_data);
        actionbar = getSupportActionBar();
        actionbar.setTitle("Data Usage");
        actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionbar.setHomeButtonEnabled(true);
        actionbar.setDisplayHomeAsUpEnabled(true);

        graph = (XYPlot) findViewById(R.id.mySimpleXYPlot);
        graph.setOnTouchListener(this);
        graph.setMarkupEnabled(false);

        labels = new Long[288];
        downdata = new Float[288];
        totaldata = new Float[288];

        httpclient = ConnectionHelper.getThreadSafeClient();
        httpclient.getCookieStore().clear();
        BasicClientCookie cookie = new BasicClientCookie("AUTHCOOKIE",
                ConnectionHelper.getPNAAuthCookie(this, httpclient));
        cookie.setDomain(".pna.utexas.edu");
        httpclient.getCookieStore().addCookie(cookie);
        new fetchDataTask(httpclient).execute();
        new fetchProgressTask(httpclient).execute();

    }

    // Definition of the touch states
    static final private int NONE = 0;
    static final private int ONE_FINGER_DRAG = 1;
    private int mode = NONE;

    private PointF firstFinger;
    private float lastScrolling;
    private float lastZooming;

    @Override
    public boolean onTouch(View arg0, MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: // Start gesture
                firstFinger = new PointF(event.getX(), event.getY());
                mode = ONE_FINGER_DRAG;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                // When the gesture ends, a thread is created to give inertia to
                // the scrolling and zoom
                final Timer t = new Timer();
                t.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        while (Math.abs(lastScrolling) > 1f || Math.abs(lastZooming - 1) > 1.01) {
                            lastScrolling *= .8; // speed of scrolling damping
                            scroll(lastScrolling);
                            lastZooming += (1 - lastZooming) * .2; // speed of
                                                                   // zooming
                                                                   // damping
                            zoom(lastZooming);
                            graph.redraw();
                        }
                    }
                }, 0);
                break;

            case MotionEvent.ACTION_MOVE:
                if (mode == ONE_FINGER_DRAG) {
                    final PointF oldFirstFinger = firstFinger;
                    firstFinger = new PointF(event.getX(), event.getY());
                    lastScrolling = oldFirstFinger.x - firstFinger.x;
                    scroll(lastScrolling);
                    lastZooming = (firstFinger.y - oldFirstFinger.y) / graph.getHeight();
                    if (lastZooming < 0) {
                        lastZooming = 1 / (1 - lastZooming);
                    } else {
                        lastZooming += 1;
                    }
                    zoom(lastZooming);

                    graph.redraw();

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

        if (minXY.x < absMinX) {
            minXY.x = absMinX;
            maxXY.x -= offset;// (absMinX-minXY.x);
        } else if (minXY.x > maxNoError) {
            minXY.x = maxNoError;
        }
        if (maxXY.x > absMaxX) {
            maxXY.x = absMaxX;// (maxXY.x-absMaxX);
            minXY.x -= offset;// (maxXY.x-absMaxX);
        } else if (maxXY.x < minNoError) {
            maxXY.x = minNoError;
        }
        if (maxXY.x - minXY.x < minDif) {
            maxXY.x = maxXY.x + (minDif - (maxXY.x - minXY.x));
        }

        graph.setDomainBoundaries(minXY.x, maxXY.x, BoundaryMode.FIXED);
    }

    private void checkBoundaries() {
        // Make sure the proposed domain boundaries will not cause plotting
        // issues
        if (minXY.x < absMinX || ((Double) minXY.x).equals(Double.NaN)) {
            minXY.x = absMinX;
        } else if (minXY.x > maxNoError) {
            minXY.x = maxNoError;
        }
        if (maxXY.x > absMaxX || ((Double) maxXY.x).equals(Double.NaN)) {
            maxXY.x = absMaxX;
        } else if (maxXY.x < minNoError) {
            maxXY.x = minNoError;
        }
        if (maxXY.x - minXY.x < minDif) {
            maxXY.x = maxXY.x + (minDif - (maxXY.x - minXY.x));
        } else if (maxXY.x - minXY.x > maxDif) {
            maxXY.x = maxXY.x + (maxDif - (maxXY.x - minXY.x));
        }
        graph.setDomainBoundaries(minXY.x, maxXY.x, BoundaryMode.FIXED);
    }

    private class fetchProgressTask extends AsyncTask<Object, Void, Void> {
        private DefaultHttpClient client;

        public fetchProgressTask(DefaultHttpClient client) {
            this.client = client;
        }

        @Override
        protected Void doInBackground(Object... params) {
            HttpGet hget = new HttpGet("https://management.pna.utexas.edu/server/graph.cgi");

            String pagedata = "";

            try {
                HttpResponse response = client.execute(hget);
                pagedata = EntityUtils.toString(response.getEntity());

            } catch (Exception e) {
                cancel(true);
                e.printStackTrace();
            }

            Pattern percentpattern = Pattern.compile("\\((.+?)%\\)");
            Matcher percentmatcher = percentpattern.matcher(pagedata);
            String found = "0.00";
            if (percentmatcher.find()) {
                found = percentmatcher.group(1);
            }
            percentused = Double.parseDouble(found);

            Pattern usedpattern = Pattern.compile("<b>(.*?)</b>");
            Matcher usedmatcher = usedpattern.matcher(pagedata);
            usedText = "UTilities could not find your % data usage";
            if (usedmatcher.find()) {
                usedText = usedmatcher.group(1);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            dataUsedText.setText(usedText);
            mProgress.setProgress((int) (percentused * 10));
        }

        @Override
        protected void onCancelled() {
            dataUsedText.setText("UTilities could not fetch your % data usage");
        }
    }

    private class fetchDataTask extends AsyncTask<Object, Void, Character> {
        private DefaultHttpClient client;
        private String errorMsg;
        private Exception ex;
        private Boolean showButton = false;
        private String errorData;

        public fetchDataTask(DefaultHttpClient client) {
            this.client = client;
        }

        @Override
        protected Character doInBackground(Object... params) {
            HttpGet hget = null;
            Pattern authidpattern = Pattern.compile("(?<=%20)\\d+");
            Matcher authidmatcher = authidpattern.matcher(client.getCookieStore().getCookies()
                    .get(0).getValue());
            if (authidmatcher.find()) {
                hget = new HttpGet(
                        "https://management.pna.utexas.edu/server/get-bw-graph-data.cgi?authid="
                                + authidmatcher.group());
            } else {
                cancel(true);
                errorMsg = "UTilities could not fetch your data usage";
                Log.d(DataUsageActivity.class.getSimpleName(), "No authid found");
                return ' ';
            }

            String pagedata = "";

            try {
                HttpResponse response = client.execute(hget);
                pagedata = EntityUtils.toString(response.getEntity());
            } catch (Exception e) {
                cancel(true);
                errorMsg = "UTilities could not fetch your data usage";
                e.printStackTrace();
                return ' ';
            }

            String[] lines = pagedata.split("\n");
            if (!lines[0].equals("Date,MB In,MB Out,MB Total")) {
                cancel(true);
                errorMsg = "UTilities could not fetch your data usage";
                return ' ';
            }
            Calendar date = Calendar.getInstance();

            // if there's more than a week of data, show just the last week,
            // otherwise show it all
            for (int i = lines.length >= 288 ? lines.length - 288 : 0, x = 0; i < lines.length; i++, x++) {
                String[] entry = lines[i].split(",");
                date.clear();
                try {
                    // TODO: this crashes sometimes on the [2] access, not sure
                    // why
                    date.set(Integer.parseInt(entry[0].split("/")[0]),
                            Integer.parseInt(entry[0].split("/")[1]) - 1,
                            Integer.parseInt(entry[0].split("/| ")[2]),
                            Integer.parseInt(entry[0].split(" |:")[1]),
                            Integer.parseInt(entry[0].split(" |:")[2]));
                } catch (NumberFormatException nfe) {
                    cancel(true);
                    errorMsg = "UTilities could not fetch your data usage";
                    nfe.printStackTrace();
                    return ' ';
                } catch (ArrayIndexOutOfBoundsException aibe) {
                    errorMsg = "UTilities could not parse your data usage";
                    ex = aibe;
                    StringBuilder data = new StringBuilder();
                    for (String line : lines) {
                        data.append(line).append("\n");
                    }
                    errorData = data.toString();
                    showButton = true;
                    aibe.printStackTrace();
                    cancel(true);
                    return ' ';
                }
                labels[x] = date.getTimeInMillis();
                downdata[x] = (Float.valueOf(entry[1]));

                // psh who needs updata when you can just overlay downdata on
                // top of totaldata?
                // updata[x]=(Float.valueOf(entry[2]));
                totaldata[x] = (Float.valueOf(entry[3]));
            }
            return ' ';
        }

        @Override
        protected void onPostExecute(Character result) {
            XYSeries series = new SimpleXYSeries(Arrays.asList(labels), Arrays.asList(downdata),
                    "Downloaded");
            XYSeries seriestotal = new SimpleXYSeries(Arrays.asList(labels),
                    Arrays.asList(totaldata), "Uploaded");

            BarFormatter downbarformatter = new BarFormatter(0xFF42D692, Color.BLACK);
            BarFormatter upbarformatter = new BarFormatter(0xFF388DFF, Color.BLACK);

            downbarformatter.getBorderPaint().setStrokeWidth(0);
            upbarformatter.getBorderPaint().setStrokeWidth(0);

            Paint gridpaint = new Paint();
            gridpaint.setColor(Color.DKGRAY);
            gridpaint.setAntiAlias(true);
            gridpaint.setStyle(Paint.Style.STROKE);

            graph.getGraphWidget().setDomainGridLinePaint(gridpaint);
            graph.getGraphWidget().setRangeGridLinePaint(gridpaint);
            graph.setTicksPerDomainLabel(4);

            graph.addSeries(seriestotal, upbarformatter);
            graph.addSeries(series, downbarformatter);

            graph.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 1800000);
            graph.setRangeStep(XYStepMode.INCREMENT_BY_VAL, 30);

            graph.calculateMinMaxVals();
            minXY = new PointD(graph.getCalculatedMinX().doubleValue(), graph.getCalculatedMinY()
                    .doubleValue()); // initial minimum data point
            absMinX = minXY.x; // absolute minimum data point
            // TODO: this crashes with a NPE sometimes. ??
            // absolute minimum value for the domain boundary maximum
            Number temp = series.getX(1);
            if (temp == null) {
                errorMsg = "There was an error fetching or displaying your data usage.";
                detv.setText(errorMsg);
                d_pb_ll.setVisibility(View.GONE);
                dell.setVisibility(View.VISIBLE);
                return;
            }
            minNoError = Math.round(temp.doubleValue() + 2);
            maxXY = new PointD(graph.getCalculatedMaxX().doubleValue(), graph.getCalculatedMaxY()
                    .doubleValue()); // initial maximum data point

            graph.setTicksPerRangeLabel(maxXY.y > 61 ? 2 : 1);

            absMaxX = maxXY.x; // absolute maximum data point
            // absolute maximum value for the domain boundary minimum
            maxNoError = (double) Math.round(series.getX(series.size() - 1).doubleValue()) - 2;
            minDif = 3000000;
            maxDif = 33000000;

            graph.setRangeUpperBoundary(maxXY.y > 31 ? maxXY.y : 31, BoundaryMode.FIXED);
            graph.setRangeLowerBoundary(0, BoundaryMode.FIXED);
            graph.setDomainUpperBoundary(maxXY.x, BoundaryMode.FIXED);
            graph.setDomainLowerBoundary(minXY.x, BoundaryMode.FIXED);
            checkBoundaries();

            graph.setDomainValueFormat(new TimeFormat());
            graph.redraw();
            graph.setVisibility(View.VISIBLE);
            d_pb_ll.setVisibility(View.GONE);

            Toast.makeText(DataUsageActivity.this, "Swipe up and down to zoom in and out",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onCancelled() {
            detv.setText(errorMsg);
            deb.setText("Send anonymous information about your data usage to help improve UTilities.");
            deb.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (errorData != null && ex != null) {
                        SharedPreferences sp = PreferenceManager
                                .getDefaultSharedPreferences(DataUsageActivity.this
                                        .getBaseContext());
                        if (!sp.getBoolean("acra.enable", true)) {
                            ACRA.getErrorReporter().setEnabled(true);
                        }
                        ACRA.getErrorReporter().putCustomData("xmldata", errorData);
                        ACRA.getErrorReporter().handleException(ex);
                        ACRA.getErrorReporter().removeCustomData("xmldata");
                        if (!sp.getBoolean("acra.enable", true)) {
                            ACRA.getErrorReporter().setEnabled(false);
                        }
                        Toast.makeText(DataUsageActivity.this,
                                "Data is being sent, thanks for helping out!", Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        Toast.makeText(DataUsageActivity.this,
                                "Couldn't send the course data for some reason :(",
                                Toast.LENGTH_SHORT).show();
                    }
                    v.setVisibility(View.INVISIBLE);
                }
            });
            d_pb_ll.setVisibility(View.GONE);
            dell.setVisibility(View.VISIBLE);
            if (showButton) {
                deb.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                super.onBackPressed();
                break;
        }
        return true;
    }

    private class TimeFormat extends Format {

        private static final long serialVersionUID = 1L;
        // create a simple date format that draws on the year portion of our
        // timestamp.
        // see
        // http://download.oracle.com/javase/1.4.2/docs/api/java/text/SimpleDateFormat.html
        // for a full description of SimpleDateFormat.
        private SimpleDateFormat dateFormat = new SimpleDateFormat("E - HH:mm", Locale.US);

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

    private class PointD {
        double x, y;

        public PointD(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}

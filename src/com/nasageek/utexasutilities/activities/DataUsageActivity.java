
package com.nasageek.utexasutilities.activities;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.xy.BarFormatter;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;
import com.nasageek.utexasutilities.AuthCookie;
import com.nasageek.utexasutilities.MyBus;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.TaggedAsyncTask;
import com.nasageek.utexasutilities.UTilitiesApplication;
import com.nasageek.utexasutilities.fragments.DataLoadFragment.LoadStatus;
import com.nasageek.utexasutilities.model.LoadFailedEvent;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.nasageek.utexasutilities.UTilitiesApplication.PNA_AUTH_COOKIE_KEY;

public class DataUsageActivity extends BaseActivity implements OnTouchListener {

    private float downdata[] = new float[288];
    private float totaldata[] = new float[288];
    private long labels[] = new long[288];
    private XYPlot graph;
    private ProgressBar percentDataUsedView;
    private TextView dataUsedText;
    private TextView errorTextView;
    private LinearLayout errorLayout;
    private LinearLayout progressLayout;

    private PointD minXY;
    private PointD maxXY;
    private double absMinX;
    private double absMaxX;
    private double minNoError;
    private double maxNoError;
    private double minDif;
    private double maxDif;

    LoadStatus percentLoadStatus = LoadStatus.NOT_STARTED;
    LoadStatus dataLoadStatus = LoadStatus.NOT_STARTED;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_layout);
        setupActionBar();

        String TASK_TAG = getClass().getSimpleName();
        progressLayout = (LinearLayout) findViewById(R.id.data_progressbar_ll);
        dataUsedText = (TextView) findViewById(R.id.dataUsedText);
        percentDataUsedView = (ProgressBar) findViewById(R.id.percentDataUsed);
        errorLayout = (LinearLayout) findViewById(R.id.data_error);
        errorTextView = (TextView) findViewById(R.id.tv_failure);

        graph = (XYPlot) findViewById(R.id.mySimpleXYPlot);
        graph.setOnTouchListener(this);
        graph.setMarkupEnabled(false);

        if (savedInstanceState != null) {
            labels = savedInstanceState.getLongArray("labels");
            downdata = savedInstanceState.getFloatArray("downdata");
            totaldata = savedInstanceState.getFloatArray("totaldata");
            percentLoadStatus = (LoadStatus) savedInstanceState
                    .getSerializable("percentLoadStatus");
            dataLoadStatus = (LoadStatus) savedInstanceState.getSerializable("dataLoadStatus");
            switch (dataLoadStatus) {
                case NOT_STARTED:
                    // defaults should suffice
                    break;
                case LOADING:
                    progressLayout.setVisibility(View.VISIBLE);
                    errorLayout.setVisibility(View.GONE);
                    graph.setVisibility(View.GONE);
                    break;
                case SUCCEEDED:
                    List<Long> labelsList = new ArrayList<>(288);
                    for (long l : labels) {
                        labelsList.add(l);
                    }
                    List<Float> downdataList = new ArrayList<>(288);
                    for (float f : downdata) {
                        downdataList.add(f);
                    }
                    List<Float> totaldataList = new ArrayList<>(288);
                    for (float f : totaldata) {
                        totaldataList.add(f);
                    }
                    errorLayout.setVisibility(View.GONE);
                    setupGraph(labelsList, downdataList, totaldataList);
                    break;
                case FAILED:
                    dataLoadFailed(new DataLoadFailedEvent("", errorTextView.getText()));
                    break;
            }
        }

        FetchDataTask dataTask = new FetchDataTask(TASK_TAG + FetchDataTask.class.getSimpleName());
        FetchPercentTask percentTask =
                new FetchPercentTask(TASK_TAG + FetchPercentTask.class.getSimpleName());
        UTilitiesApplication mApp = UTilitiesApplication.getInstance();
        if (dataLoadStatus == LoadStatus.NOT_STARTED &&
                mApp.getCachedTask(dataTask.getTag()) == null) {
            dataLoadStatus = LoadStatus.LOADING;
            mApp.cacheTask(dataTask.getTag(), dataTask);
            dataTask.execute();
        }
        if (percentLoadStatus == LoadStatus.NOT_STARTED &&
                mApp.getCachedTask(percentTask.getTag()) == null) {
            percentLoadStatus = LoadStatus.LOADING;
            mApp.cacheTask(percentTask.getTag(), percentTask);
            percentTask.execute();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        MyBus.getInstance().register(this);
    }

    @Override
    public void onStop() {
        MyBus.getInstance().unregister(this);
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLongArray("labels", labels);
        outState.putFloatArray("downdata", downdata);
        outState.putFloatArray("totaldata", totaldata);
        outState.putSerializable("percentLoadStatus", percentLoadStatus);
        outState.putSerializable("dataLoadStatus", dataLoadStatus);
    }

    private void setupGraph(List<Long> labels, List<Float> downdata, List<Float> totaldata) {
        XYSeries series = new SimpleXYSeries(labels, downdata, "Downloaded");
        XYSeries seriestotal = new SimpleXYSeries(labels, totaldata, "Uploaded");

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
            dataLoadFailed(new DataLoadFailedEvent("",
                    "There was an error fetching or displaying your data usage."));
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
        progressLayout.setVisibility(View.GONE);
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

    static class FetchPercentTask extends TaggedAsyncTask<Object, Void, Void> {
        private String errorMsg;
        private double percentUsed;
        private String totalUsed;

        public FetchPercentTask(String tag) {
            super(tag);
        }

        @Override
        protected Void doInBackground(Object... params) {
            String reqUrl = "https://management.pna.utexas.edu/server/graph.cgi";
            Request request = new Request.Builder()
                    .url(reqUrl)
                    .build();
            String pagedata = "";
            OkHttpClient client = UTilitiesApplication.getInstance().getHttpClient();

            try {
                Response response = client.newCall(request).execute();
                pagedata = response.body().string();
            } catch (IOException e) {
                errorMsg = "UTilities could not fetch your % data usage";
                e.printStackTrace();
                cancel(true);
                return null;
            }

            Pattern percentpattern = Pattern.compile("\\((.+?)%\\)");
            Matcher percentmatcher = percentpattern.matcher(pagedata);
            String found = "0.00";
            if (percentmatcher.find()) {
                found = percentmatcher.group(1);
            }
            percentUsed = Double.parseDouble(found);

            Pattern usedpattern = Pattern.compile("<b>(.*?)</b>");
            Matcher usedmatcher = usedpattern.matcher(pagedata);
            if (usedmatcher.find()) {
                totalUsed = usedmatcher.group(1);
            } else {
                errorMsg = "UTilities could not find your % data usage";
                cancel(true);
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            MyBus.getInstance().post(new PercentLoadSucceededEvent(percentUsed, totalUsed));
            UTilitiesApplication.getInstance().removeCachedTask(getTag());
        }

        @Override
        protected void onCancelled() {
            MyBus.getInstance().post(new PercentLoadFailedEvent(getTag(), errorMsg));
            UTilitiesApplication.getInstance().removeCachedTask(getTag());
        }
    }

    @Subscribe
    public void percentLoadSucceeded(PercentLoadSucceededEvent event) {
        percentLoadStatus = LoadStatus.SUCCEEDED;
        dataUsedText.setText(event.totalDataUsed);
        percentDataUsedView.setProgress((int) (event.percentDataUsed * 10));
    }

    @Subscribe
    public void percentLoadFailed(LoadFailedEvent event) {
        percentLoadStatus = LoadStatus.FAILED;
        dataUsedText.setText(event.errorMessage);
        percentDataUsedView.setProgress(0);
    }

    static class PercentLoadFailedEvent extends LoadFailedEvent {
        public PercentLoadFailedEvent(String tag, String errorMsg) {
            super(tag, errorMsg);
        }
    }

    static class PercentLoadSucceededEvent {
        public double percentDataUsed;
        public String totalDataUsed;

        public PercentLoadSucceededEvent(double percentDataUsed, String totalDataUsed) {
            this.totalDataUsed = totalDataUsed;
            this.percentDataUsed = percentDataUsed;
        }
    }

    static class FetchDataTask extends TaggedAsyncTask<Object, Void, Character> {
        private String errorMsg;
        private List<Long> labels = new ArrayList<>(288);
        private List<Float> downdata = new ArrayList<>(288);
        private List<Float> totaldata = new ArrayList<>(288);

        public FetchDataTask(String tag) {
            super(tag);
        }

        @Override
        protected Character doInBackground(Object... params) {
            AuthCookie pnaCookie = UTilitiesApplication.getInstance()
                    .getAuthCookie(PNA_AUTH_COOKIE_KEY);
            Pattern authidpattern = Pattern.compile("(?<=%20)\\d+");
            String cookie = pnaCookie.getAuthCookieVal();
            Matcher authidmatcher = authidpattern.matcher(cookie == null ? "" : cookie);
            OkHttpClient client = UTilitiesApplication.getInstance().getHttpClient();
            String reqUrl;
            if (authidmatcher.find()) {
                reqUrl = "https://management.pna.utexas.edu/server/get-bw-graph-data.cgi?authid="
                                + authidmatcher.group();
            } else {
                cancel(true);
                errorMsg = "UTilities could not fetch your data usage";
                return null;
            }

            Request request = new Request.Builder()
                    .url(reqUrl)
                    .build();
            String pagedata = "";

            try {
                Response response = client.newCall(request).execute();
                pagedata = response.body().string();
            } catch (IOException e) {
                errorMsg = "UTilities could not fetch your data usage";
                e.printStackTrace();
                cancel(true);
                return null;
            }

            String[] lines = pagedata.split("\n");
            if (!lines[0].equals("Date,MB In,MB Out,MB Total")) {
                cancel(true);
                errorMsg = "UTilities could not fetch your data usage";
                return null;
            }
            Calendar date = Calendar.getInstance();

            // if there's more than a week of data, show just the last week, otherwise show it all
            for (int i = lines.length >= 288 ? lines.length - 288 : 0, x = 0; i < lines.length; i++, x++) {
                String[] entry = lines[i].split(",");
                date.clear();
                try {
                    // TODO: this crashes sometimes on the [2] access, not sure why
                    date.set(Integer.parseInt(entry[0].split("/")[0]),
                            Integer.parseInt(entry[0].split("/")[1]) - 1,
                            Integer.parseInt(entry[0].split("/| ")[2]),
                            Integer.parseInt(entry[0].split(" |:")[1]),
                            Integer.parseInt(entry[0].split(" |:")[2]));
                } catch (NumberFormatException nfe) {
                    errorMsg = "UTilities could not fetch your data usage";
                    nfe.printStackTrace();
                    cancel(true);
                    return null;
                } catch (ArrayIndexOutOfBoundsException aibe) {
                    errorMsg = "UTilities could not parse your data usage";
                    aibe.printStackTrace();
                    cancel(true);
                    return null;
                }
                labels.add(date.getTimeInMillis());
                downdata.add(Float.valueOf(entry[1]));
                totaldata.add(Float.valueOf(entry[3]));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Character result) {
            MyBus.getInstance().post(new DataLoadSucceededEvent(labels, downdata, totaldata));
            UTilitiesApplication.getInstance().removeCachedTask(getTag());
        }

        @Override
        protected void onCancelled() {
            MyBus.getInstance().post(new DataLoadFailedEvent(getTag(), errorMsg));
            UTilitiesApplication.getInstance().removeCachedTask(getTag());
        }
    }

    @Subscribe
    public void dataLoadSucceeded(DataLoadSucceededEvent event) {
        dataLoadStatus = LoadStatus.SUCCEEDED;
        int i = 0;
        for (Long l : event.labels) {
            labels[i] = l;
            i++;
        }
        i = 0;
        for (Float f : event.downdata) {
            downdata[i] = f;
            i++;
        }
        i = 0;
        for (Float f : event.totaldata) {
            totaldata[i] = f;
            i++;
        }
        setupGraph(event.labels, event.downdata, event.totaldata);
        Toast.makeText(this, "Swipe up and down to zoom in and out", Toast.LENGTH_SHORT).show();
    }

    @Subscribe
    public void dataLoadFailed(DataLoadFailedEvent event) {
        dataLoadStatus = LoadStatus.FAILED;
        errorTextView.setText(event.errorMessage);
        progressLayout.setVisibility(View.GONE);
        errorLayout.setVisibility(View.VISIBLE);
    }

    static class DataLoadSucceededEvent {
        private List<Long> labels;
        private List<Float> downdata;
        private List<Float> totaldata;

        public DataLoadSucceededEvent(List<Long> labels, List<Float> downdata,
                                      List<Float> totaldata) {
            this.labels = labels;
            this.downdata = downdata;
            this.totaldata = totaldata;
        }
    }

    static class DataLoadFailedEvent extends LoadFailedEvent {
        public DataLoadFailedEvent(String tag, CharSequence errorMsg) {
            super(tag, errorMsg);
        }
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
        public StringBuffer format(Object obj, @NonNull StringBuffer toAppendTo,
                                   @NonNull FieldPosition pos) {
            long timestamp = ((Number) obj).longValue();
            Date date = new Date(timestamp);
            return dateFormat.format(date, toAppendTo, pos);
        }

        @Override
        public Object parseObject(String source, @NonNull ParsePosition pos) {
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

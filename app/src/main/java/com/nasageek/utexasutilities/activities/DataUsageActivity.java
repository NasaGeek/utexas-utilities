
package com.nasageek.utexasutilities.activities;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.nasageek.utexasutilities.BuildConfig;
import com.nasageek.utexasutilities.MyBus;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.TaggedAsyncTask;
import com.nasageek.utexasutilities.UTilitiesApplication;
import com.nasageek.utexasutilities.fragments.DataLoadFragment.LoadStatus;
import com.nasageek.utexasutilities.fragments.DataSourceSelectionFragment;
import com.nasageek.utexasutilities.model.LoadFailedEvent;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataUsageActivity extends BaseActivity {

    private static final int DATA_POINTS = 2016; // one week of 5-minute intervals
    private ArrayList<Entry> downData = new ArrayList<>(DATA_POINTS);
    private ArrayList<Entry> totalData = new ArrayList<>(DATA_POINTS);
    private ArrayList<String> labels = new ArrayList<>(DATA_POINTS);
    private LineChart chart;
    private ProgressBar percentDataUsedView;
    private TextView dataUsedText;
    private TextView errorTextView;
    private View errorLayout;
    private LinearLayout progressLayout;

    private String TASK_TAG;
    LoadStatus percentLoadStatus = LoadStatus.NOT_STARTED;
    LoadStatus dataLoadStatus = LoadStatus.NOT_STARTED;
    private UTilitiesApplication mApp = UTilitiesApplication.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_layout);
        setupActionBar();

        TASK_TAG = getClass().getSimpleName();
        progressLayout = (LinearLayout) findViewById(R.id.data_progressbar_ll);
        dataUsedText = (TextView) findViewById(R.id.dataUsedText);
        percentDataUsedView = (ProgressBar) findViewById(R.id.percentDataUsed);
        errorLayout = findViewById(R.id.data_error);
        errorTextView = (TextView) findViewById(R.id.tv_failure);
        chart = (LineChart) findViewById(R.id.mp_usage_chart);

        if (savedInstanceState != null) {
            labels = savedInstanceState.getStringArrayList("labels");
            downData = savedInstanceState.getParcelableArrayList("downData");
            totalData = savedInstanceState.getParcelableArrayList("totalData");
            percentLoadStatus = (LoadStatus) savedInstanceState
                    .getSerializable("percentLoadStatus");
            dataLoadStatus = (LoadStatus) savedInstanceState.getSerializable("dataLoadStatus");
            switch (dataLoadStatus) {
                case NOT_STARTED:
                    // defaults should suffice
                    break;
                case LOADING:
                    prepareToLoad();
                    break;
                case SUCCEEDED:
                    errorLayout.setVisibility(View.GONE);
                    dataLoadSucceeded(new DataLoadSucceededEvent(labels, downData, totalData));
                    break;
                case FAILED:
                    dataLoadFailed(new DataLoadFailedEvent("", errorTextView.getText()));
                    break;
            }
        }
        loadData(DataUsageActivity.getDataUrl(), false);
    }

    private void prepareToLoad() {
        progressLayout.setVisibility(View.VISIBLE);
        errorLayout.setVisibility(View.GONE);
        chart.setVisibility(View.GONE);
    }

    private void loadData(String url, boolean forceReload) {
        FetchDataTask dataTask = new FetchDataTask(TASK_TAG + FetchDataTask.class.getSimpleName());
        if ((dataLoadStatus == LoadStatus.NOT_STARTED &&
                mApp.getCachedTask(dataTask.getTag()) == null) || forceReload) {
            prepareToLoad();
            dataLoadStatus = LoadStatus.LOADING;
            dataTask.execute(url);
        }
    }

    @Subscribe
    public void openWebView(OpenWebViewEvent ev) {
        WebView wv = new WebView(this);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.setWebViewClient(new DataUsageWebViewClient());
        wv.addJavascriptInterface(this, "ututilities");
        wv.loadData(ev.html, "text/html; charset=UTF-8", null);
    }

    class DataUsageWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            String jsScript = "window.ututilities.setUsageData(data.bytes_in, data.bytes_total, data.timestamp)";
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                view.evaluateJavascript(jsScript, null);
            } else {
                // exec js
                if (!url.contains("javascript:")) {
                    view.loadUrl("javascript:" + jsScript);
                }
            }
        }
    }

    @JavascriptInterface
    public void setUsageData(long[] bytes_in, long[] bytes_total, long[] timestamps) {
        ArrayList<Entry> downdata = new ArrayList<>(DATA_POINTS);
        ArrayList<Entry> totaldata = new ArrayList<>(DATA_POINTS);
        ArrayList<String> labels = new ArrayList<>(DATA_POINTS);

        DateFormat outFormat = new SimpleDateFormat("E - hh:mm a", Locale.US);

        // if there's more than a week of data, show just the last week, otherwise show it all
        for (int i = Math.max(timestamps.length - DATA_POINTS, 0), x = 0; i < timestamps.length; i++, x++) {
            labels.add(outFormat.format(new Date(timestamps[i])));
            downdata.add(new Entry(bytes_in[i] / 1024 / 1024, x));
            totaldata.add(new Entry(bytes_total[i] / 1024 / 1024, x));
        }
        new Handler(getMainLooper()).post(
                () -> MyBus.getInstance().post(new DataLoadSucceededEvent(labels, downdata, totaldata)));
    }

    @NonNull
    private static String getDataUrl() {
        return "https://management.pna.utexas.edu/restricted/cgi/bwdetails.cgi";
    }

    @Override
    public void onStart() {
        super.onStart();
        MyBus.getInstance().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        CookieSyncManager.getInstance().startSync();
    }

    @Override
    public void onPause() {
        CookieSyncManager.getInstance().stopSync();
        super.onPause();
    }

    @Override
    public void onStop() {
        MyBus.getInstance().unregister(this);
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("labels", labels);
        outState.putParcelableArrayList("downData", downData);
        outState.putParcelableArrayList("totalData", totalData);
        outState.putSerializable("percentLoadStatus", percentLoadStatus);
        outState.putSerializable("dataLoadStatus", dataLoadStatus);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (BuildConfig.DEBUG) {
            getMenuInflater().inflate(R.menu.data_sources, menu);
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.data_sources:
                DataSourceSelectionFragment.newInstance("test_html/datausage", getDataUrl())
                        .show(getSupportFragmentManager(),
                                DataSourceSelectionFragment.class.getSimpleName());
                break;
            default: return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Subscribe
    public void onDataSourceSelected(DataSourceSelectionFragment.DataSourceSelectedEvent event) {
        chart.clear();
        if (event.url != null) {
            loadData(event.url, true);
        } else {
            dataLoadFailed(new DataLoadFailedEvent("",
                    "UTilities could not fetch your data usage"));
        }
    }

    private void setupChart(List<String> labels, List<Entry> downData, List<Entry> totalData) {
        LineDataSet totalLineDataSet = new LineDataSet(totalData, "Uploaded");
        totalLineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        totalLineDataSet.setDrawCircles(false);
        totalLineDataSet.setCubicIntensity(0.1f);
        totalLineDataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        totalLineDataSet.setDrawFilled(true);
        totalLineDataSet.setDrawValues(false);
        totalLineDataSet.setColor(ContextCompat.getColor(this, R.color.data_usage_chart_upload));
        totalLineDataSet.setFillColor(ContextCompat.getColor(this, R.color.data_usage_chart_upload));
        totalLineDataSet.setFillAlpha(255);

        LineDataSet downLineDataSet = new LineDataSet(downData, "Downloaded");
        downLineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        downLineDataSet.setDrawCircles(false);
        downLineDataSet.setCubicIntensity(0.1f);
        downLineDataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        downLineDataSet.setDrawFilled(true);
        downLineDataSet.setDrawValues(false);
        downLineDataSet.setColor(ContextCompat.getColor(this, R.color.data_usage_chart_download));
        downLineDataSet.setFillColor(ContextCompat.getColor(this, R.color.data_usage_chart_download));
        downLineDataSet.setFillAlpha(255);

        List<ILineDataSet> downAndUp = new ArrayList<>();
        downAndUp.add(totalLineDataSet);
        downAndUp.add(downLineDataSet);

        LineData dataUsageLineData = new LineData(labels, downAndUp);
        chart.getAxisRight().setEnabled(true);
        chart.getAxisRight().setDrawAxisLine(true);
        chart.getAxisRight().setDrawGridLines(false);
        chart.getAxisRight().setDrawLabels(false);
        chart.getAxisLeft().setStartAtZero(true);
        chart.setData(dataUsageLineData);
        chart.setDescription("");
        chart.setScaleXEnabled(true);
        chart.setScaleYEnabled(false);
        chart.setPinchZoom(true);
        chart.setDoubleTapToZoomEnabled(true);
        chart.getData().setHighlightEnabled(false);
        chart.getAxisLeft().setValueFormatter((value, axis) -> value + " MB");
        // maximum viewable area is one day
        chart.setScaleMinima(downData.size() / 288f, 1f);
        // initially show the most recent 24 hours
        chart.centerViewTo(Math.max(downData.size() - 144, 0), chart.getYChartMax() / 2, YAxis.AxisDependency.LEFT);
        chart.setVisibility(View.VISIBLE);
        progressLayout.setVisibility(View.GONE);
    }

    static class FetchDataTask extends TaggedAsyncTask<String, String, Void> {
        private String errorMsg;
        private double percentUsed;
        private String totalUsed;

        public FetchDataTask(String tag) {
            super(tag);
        }

        @Override
        protected Void doInBackground(String... params) {
            String reqUrl = params[0];
            Request request = new Request.Builder()
                    .url(reqUrl)
                    .build();
            String pagedata;
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
            publishProgress(pagedata);

            String regex = "<td>Total / Remaining </td>\\s+<td class=\"qty\">([0-9]+) MB</td>\\s+<td class=\"qty.*?\">([0-9]+) MB</td>";
            Pattern dataUsedPattern = Pattern.compile(regex, Pattern.MULTILINE);
            Matcher dataUsedMatcher = dataUsedPattern.matcher(pagedata);
            int total, remaining;
            if (dataUsedMatcher.find()) {
                total = Integer.parseInt(dataUsedMatcher.group(1));
                remaining = Integer.parseInt(dataUsedMatcher.group(2));
                totalUsed = Integer.toString(total - remaining);
                percentUsed = (total - remaining) / (double) total;
                return null;
            } else {
                errorMsg = "UTilities could not find your % data usage";
                cancel(true);
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            MyBus.getInstance().post(new OpenWebViewEvent(values[0]));
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            MyBus.getInstance().post(new PercentLoadSucceededEvent(percentUsed, totalUsed));
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            MyBus.getInstance().post(new PercentLoadFailedEvent(getTag(), errorMsg));
//            MyBus.getInstance().post(new DataLoadFailedEvent(getTag(), errorMsg));
        }
    }

    @Subscribe
    public void percentLoadSucceeded(PercentLoadSucceededEvent event) {
        percentLoadStatus = LoadStatus.SUCCEEDED;
        dataUsedText.setText("You've used " + event.totalDataUsed + "MB of data this week");
        percentDataUsedView.setProgress((int) (event.percentDataUsed * 10));
    }

    @Subscribe
    public void percentLoadFailed(PercentLoadFailedEvent event) {
        percentLoadStatus = LoadStatus.FAILED;
        dataUsedText.setText(event.errorMessage);
        percentDataUsedView.setProgress(0);
    }

    static class OpenWebViewEvent {
        public String html;

        public OpenWebViewEvent(String html) {
            this.html = html;
        }
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

    @Subscribe
    public void dataLoadSucceeded(DataLoadSucceededEvent event) {
        dataLoadStatus = LoadStatus.SUCCEEDED;
        labels = event.labels;
        downData = event.downdata;
        totalData = event.totaldata;
        setupChart(labels, downData, totalData);
    }

    @Subscribe
    public void dataLoadFailed(DataLoadFailedEvent event) {
        dataLoadStatus = LoadStatus.FAILED;
        errorTextView.setText(event.errorMessage);
        progressLayout.setVisibility(View.GONE);
        errorLayout.setVisibility(View.VISIBLE);
    }

    static class DataLoadSucceededEvent {
        private ArrayList<String> labels;
        private ArrayList<Entry> downdata;
        private ArrayList<Entry> totaldata;

        public DataLoadSucceededEvent(ArrayList<String> labels, ArrayList<Entry> downdata,
                                      ArrayList<Entry> totaldata) {
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
}

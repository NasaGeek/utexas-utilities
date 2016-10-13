
package com.nasageek.utexasutilities.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.nasageek.utexasutilities.AuthCookie;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.nasageek.utexasutilities.UTilitiesApplication.PNA_AUTH_COOKIE_KEY;

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
        String reqUrl = createDataUrl();
        if (reqUrl != null) {
            loadData(reqUrl, false);
        } else if (dataLoadStatus != LoadStatus.SUCCEEDED) {
            dataLoadFailed(new DataLoadFailedEvent("",
                    "UTilities could not fetch your data usage"));
        }
        FetchPercentTask percentTask =
                new FetchPercentTask(TASK_TAG + FetchPercentTask.class.getSimpleName());
        if (percentLoadStatus == LoadStatus.NOT_STARTED &&
                mApp.getCachedTask(percentTask.getTag()) == null) {
            percentLoadStatus = LoadStatus.LOADING;
            percentTask.execute();
        }
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

    @Nullable
    private String createDataUrl() {
        return "https://management.pna.utexas.edu/restricted/cgi/bwdetails.cgi";
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
                DataSourceSelectionFragment.newInstance("test_data/datausage", createDataUrl())
                        .show(getSupportFragmentManager(),
                                DataSourceSelectionFragment.class.getSimpleName());
                break;
            default: return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Subscribe
    public void onDataSourceSelected(DataSourceSelectionFragment.DataSourceSelectedEvent event) {
        percentDataUsedView.setProgress(700);
        dataUsedText.setText("70% of your data's been used");
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
        totalLineDataSet.setDrawCubic(true);
        totalLineDataSet.setDrawFilled(true);
        totalLineDataSet.setDrawValues(false);
        totalLineDataSet.setColor(ContextCompat.getColor(this, R.color.data_usage_chart_upload));
        totalLineDataSet.setFillColor(ContextCompat.getColor(this, R.color.data_usage_chart_upload));
        totalLineDataSet.setFillAlpha(255);

        LineDataSet downLineDataSet = new LineDataSet(downData, "Downloaded");
        downLineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        downLineDataSet.setDrawCircles(false);
        downLineDataSet.setDrawCubic(true);
        downLineDataSet.setDrawFilled(true);
        downLineDataSet.setDrawValues(false);
        downLineDataSet.setColor(ContextCompat.getColor(this, R.color.data_usage_chart_download));
        downLineDataSet.setFillColor(ContextCompat.getColor(this, R.color.data_usage_chart_download));
        downLineDataSet.setFillAlpha(255);

        List<ILineDataSet> downAndUp = new ArrayList<>();
        downAndUp.add(totalLineDataSet);
        downAndUp.add(downLineDataSet);

        LineData dataUsageLineData = new LineData(labels, downAndUp);
        chart.setData(dataUsageLineData);
        chart.setDescription("");
        chart.setScaleXEnabled(true);
        chart.setScaleYEnabled(false);
        chart.setPinchZoom(true);
        chart.setDoubleTapToZoomEnabled(true);
        chart.getData().setHighlightEnabled(false);
        chart.getAxisRight().setDrawLabels(false);
        chart.getAxisLeft().setValueFormatter((value, yAxis) -> value + " MB");
        // maximum viewable area is one day
        chart.setScaleMinima(downData.size() / 288f, 1f);
        // initially show the most recent 24 hours
        chart.centerViewTo(Math.max(downData.size() - 144, 0), chart.getYChartMax() / 2, YAxis.AxisDependency.LEFT);
        chart.setVisibility(View.VISIBLE);
        progressLayout.setVisibility(View.GONE);
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
            super.onPostExecute(v);
            MyBus.getInstance().post(new PercentLoadSucceededEvent(percentUsed, totalUsed));
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            MyBus.getInstance().post(new PercentLoadFailedEvent(getTag(), errorMsg));
        }
    }

    @Subscribe
    public void percentLoadSucceeded(PercentLoadSucceededEvent event) {
        percentLoadStatus = LoadStatus.SUCCEEDED;
        dataUsedText.setText(event.totalDataUsed);
        percentDataUsedView.setProgress((int) (event.percentDataUsed * 10));
    }

    @Subscribe
    public void percentLoadFailed(PercentLoadFailedEvent event) {
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

    static class FetchDataTask extends TaggedAsyncTask<String, Void, Character> {
        private String errorMsg;
        private ArrayList<String> labels = new ArrayList<>(DATA_POINTS);
        private ArrayList<Entry> downdata = new ArrayList<>(DATA_POINTS);
        private ArrayList<Entry> totaldata = new ArrayList<>(DATA_POINTS);

        public FetchDataTask(String tag) {
            super(tag);
        }

        @Override
        protected Character doInBackground(String... params) {
            OkHttpClient client = UTilitiesApplication.getInstance().getHttpClient();
            String reqUrl = params[0];
            Request request = new Request.Builder()
                    .url(reqUrl)
                    .build();
            String pagedata;

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

            DateFormat inFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US);
            DateFormat outFormat = new SimpleDateFormat("E - hh:mm a", Locale.US);
            // if there's more than a week of data, show just the last week, otherwise show it all
            for (int i = Math.max(lines.length - DATA_POINTS, 0), x = 0; i < lines.length; i++, x++) {
                String[] entry = lines[i].split(",");
                date.clear();
                try {
                    labels.add(outFormat.format(inFormat.parse(entry[0])));
                } catch (ParseException e) {
                    errorMsg = "UTilities could not parse your data usage";
                    e.printStackTrace();
                    cancel(true);
                    return null;
                }
                downdata.add(new Entry(Float.parseFloat(entry[1]), x));
                totaldata.add(new Entry(Float.parseFloat(entry[3]), x));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Character result) {
            super.onPostExecute(result);
            MyBus.getInstance().post(new DataLoadSucceededEvent(labels, downdata, totaldata));
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            MyBus.getInstance().post(new DataLoadFailedEvent(getTag(), errorMsg));
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

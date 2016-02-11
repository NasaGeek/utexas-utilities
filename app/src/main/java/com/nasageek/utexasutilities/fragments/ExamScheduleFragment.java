
package com.nasageek.utexasutilities.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nasageek.utexasutilities.AuthCookie;
import com.nasageek.utexasutilities.MyBus;
import com.nasageek.utexasutilities.NotAuthenticatedException;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.UTLoginTask;
import com.nasageek.utexasutilities.UTilitiesApplication;
import com.nasageek.utexasutilities.activities.CampusMapActivity;
import com.nasageek.utexasutilities.model.LoadFailedEvent;
import com.squareup.okhttp.Request;
import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.nasageek.utexasutilities.UTilitiesApplication.UTD_AUTH_COOKIE_KEY;

public class ExamScheduleFragment extends ScheduleFragment implements ActionModeFragment,
        ActionMode.Callback, AdapterView.OnItemClickListener {

    private ArrayList<String> exams = new ArrayList<>();
    private ListView examListview;
    private LinearLayout progressLayout;
    private TextView errorTextView;
    private LinearLayout errorLayout;
    private ActionMode mode;
    private AuthCookie utdAuthCookie;
    private String TASK_TAG;
    private String selectedExam;
    private UTilitiesApplication mApp = UTilitiesApplication.getInstance();

    public static ExamScheduleFragment newInstance() {
        return new ExamScheduleFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View vg = inflater.inflate(R.layout.exam_schedule_fragment_layout, container, false);
        progressLayout = (LinearLayout) vg.findViewById(R.id.examschedule_progressbar_ll);
        examListview = (ListView) vg.findViewById(R.id.examschedule_listview);
        errorLayout = (LinearLayout) vg.findViewById(R.id.examschedule_error);
        errorTextView = (TextView) vg.findViewById(R.id.tv_failure);

        if (savedInstanceState != null) {
            switch (loadStatus) {
                case NOT_STARTED:
                    // defaults should suffice
                    break;
                case LOADING:
                    progressLayout.setVisibility(View.VISIBLE);
                    errorLayout.setVisibility(View.GONE);
                    examListview.setVisibility(View.GONE);
                    break;
                case SUCCEEDED:
                    progressLayout.setVisibility(View.GONE);
                    errorLayout.setVisibility(View.GONE);
                    examListview.setVisibility(View.VISIBLE);
                    break;
                case FAILED:
                    progressLayout.setVisibility(View.GONE);
                    errorLayout.setVisibility(View.VISIBLE);
                    examListview.setVisibility(View.GONE);
                    break;
            }
        }

        if (!utdAuthCookie.hasCookieBeenSet()) {
            progressLayout.setVisibility(View.GONE);
            errorTextView.setText(getString(R.string.login_first));
            errorTextView.setVisibility(View.VISIBLE);
        } else if (loadStatus == LoadStatus.NOT_STARTED && mApp.getCachedTask(TASK_TAG) == null){
            loadStatus = LoadStatus.LOADING;
            prepareToLoad();
            FetchExamDataTask task = new FetchExamDataTask(TASK_TAG);
            task.execute(false);
        } else {
            setupAdapter();
        }
        return vg;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            exams = savedInstanceState.getStringArrayList("exams");
        }
        utdAuthCookie = mApp.getAuthCookie(UTD_AUTH_COOKIE_KEY);
        TASK_TAG = getClass().getSimpleName();
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
        outState.putStringArrayList("exams", exams);
    }

    private void setupAdapter() {
        ExamAdapter adapter = new ExamAdapter(getActivity(), exams);
        examListview.setAdapter(adapter);
    }

    @Override
    public ActionMode getActionMode() {
        return mode;
    }

    private void prepareToLoad() {
        progressLayout.setVisibility(View.VISIBLE);
        examListview.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.setTitle("Exam Info");
        MenuInflater inflater = getActivity().getMenuInflater();
        String[] elements = selectedExam.split("\\^");
        if (elements.length >= 3) { // TODO: check this?
            if (elements[2].contains("The department")
                    || elements[2]
                    .contains("Information on final exams is available for Nine-Week Summer Session(s) only.")
                    || elements.length <= 4) {
                return true;
            }
        } else {
            return true;
        }
        inflater.inflate(R.menu.schedule_action_mode, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.locate_class:
                ArrayList<String> building = new ArrayList<>();
                Intent map = new Intent(getActivity().getString(R.string.building_intent), null,
                        getActivity(), CampusMapActivity.class);

                String[] elements = selectedExam.split("\\^");
                if (elements.length >= 5) {
                    building.add(elements[4].split(" ")[0]);
                    map.putStringArrayListExtra("buildings", building);
                    // map.setData(Uri.parse(elements[4].split(" ")[0]));
                    getActivity().startActivity(map);
                    return true;
                } else {
                    Toast.makeText(getActivity(), "Your exam's location could not be found",
                            Toast.LENGTH_SHORT).show();
                }
        }
        return true;

    }

    @Override
    public void onDestroyActionMode(ActionMode mode) { }

    static class FetchExamDataTask extends UTLoginTask<Boolean, Void, List<String>> {

        public FetchExamDataTask(String tag) {
            super(tag, "https://utdirect.utexas.edu/registrar/exam_schedule.WBX");
        }

        @Override
        protected List<String> doInBackground(Boolean... params) {
            Boolean recursing = params[0];
            List<String> examlist = new ArrayList<>();

            Request request = new Request.Builder()
                    .url(reqUrl)
                    .build();
            String pagedata;

            try {
                pagedata = fetchData(request);
            } catch (IOException e) {
                errorMsg = "UTilities could not fetch your exam schedule";
                e.printStackTrace();
                cancel(true);
                return null;
            } catch (NotAuthenticatedException e) {
                e.printStackTrace();
                cancel(true);
                return null;
            }

            if (pagedata.contains("will be available approximately three weeks")) {
                cancel(true);
                errorMsg = "'Tis not the season for final exams.\nTry back later!" +
                            "\n(about 3 weeks before they begin)";
                return null;
            } else if (pagedata.contains("Our records indicate that you are not enrolled" +
                    " for the current semester.")) {
                cancel(true);
                errorMsg = "You aren't enrolled for the current semester.";
                return null;
            }

            Pattern rowpattern = Pattern.compile("<tr >.*?</tr>", Pattern.DOTALL);
            Matcher rowmatcher = rowpattern.matcher(pagedata);

            while (rowmatcher.find()) {
                String rowstring = "";
                String row = rowmatcher.group();
                if (row.contains("Unique") || row.contains("Home Page")) {
                    continue;
                }

                Pattern fieldpattern = Pattern.compile("<td.*?>(.*?)</td>", Pattern.DOTALL);
                Matcher fieldmatcher = fieldpattern.matcher(row);
                while (fieldmatcher.find()) {
                    String field = fieldmatcher.group(1).replace("&nbsp;", " ").trim()
                            .replace("\t", "");
                    Spanned span = Html.fromHtml(field);
                    String out = span.toString();
                    rowstring += out + "^";
                }
                examlist.add(rowstring);
            }
            return examlist;
        }

        @Override
        protected void onPostExecute(List<String> result) {
            super.onPostExecute(result);
            MyBus.getInstance().post(new LoadSucceededEvent(getTag(), result));
        }
    }

    @Subscribe
    public void loadFailed(LoadFailedEvent event) {
        if (event.tag.equals(TASK_TAG)) {
            loadStatus = LoadStatus.FAILED;
            progressLayout.setVisibility(View.GONE);
            examListview.setVisibility(View.GONE);
            errorTextView.setText(event.errorMessage);
            errorLayout.setVisibility(View.VISIBLE);
        }
    }

    @Subscribe
    public void loadSucceeded(LoadSucceededEvent event) {
        if (event.tag.equals(TASK_TAG)) {
            loadStatus = LoadStatus.SUCCEEDED;
            exams.clear();
            exams.addAll(event.exams);
            progressLayout.setVisibility(View.GONE);
            setupAdapter();
            examListview.setOnItemClickListener(this);
            examListview.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectedExam = exams.get(position);
        mode = ((AppCompatActivity)getActivity()).startSupportActionMode(this);
    }

    static class ExamAdapter extends ArrayAdapter<String> {

        public ExamAdapter(Context c, ArrayList<String> objects) {
            super(c, 0, objects);
        }

        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }

        @Override
        public boolean isEnabled(int i) {
            return true;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String[] examdata = getItem(position).split("\\^");
            boolean examRequested = false, summerSession = false;
            String id = "", name = "", date = "", location = "", unique = "";

            // TODO: I hate doing these try/catches, find a better solution so I
            // know when stuff goes wrong? ACRA?
            try {
                examRequested = !examdata[2].contains("The department");
                summerSession = examdata[2]
                        .contains("Information on final exams is available for Nine-Week Summer Session(s) only.");

                unique = examdata[0];
                id = examdata[1];
                name = examdata[2];
                date = "";
                location = "";
                if (examRequested && !summerSession) {
                    if (examdata.length >= 4) {
                        date = examdata[3];
                    }
                    if (examdata.length >= 5) {
                        location = examdata[4];
                    }
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                ex.printStackTrace();
            }
            String course;

            View vg = convertView;
            if (vg == null) {
                vg = LayoutInflater.from(getContext())
                        .inflate(R.layout.exam_item_view, parent, false);
            }
            TextView courseview = (TextView) vg.findViewById(R.id.exam_item_header_text);
            TextView left = (TextView) vg.findViewById(R.id.examdateview);
            TextView right = (TextView) vg.findViewById(R.id.examlocview);

            if (!examRequested || summerSession) {
                course = id + " - " + unique;
                left.setText(name);
                right.setVisibility(View.INVISIBLE);
            } else {
                course = id + " " + name;
                left.setText(date);
                right.setVisibility(View.VISIBLE);
                right.setText(location);
            }
            courseview.setText(course);
            return vg;
        }
    }

    static class LoadSucceededEvent {
        public List<String> exams;
        public String tag;

        public LoadSucceededEvent(String tag, List<String> exams) {
            this.tag = tag;
            this.exams = exams;
        }
    }
}

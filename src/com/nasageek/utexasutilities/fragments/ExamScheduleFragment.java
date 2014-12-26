
package com.nasageek.utexasutilities.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
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

import com.nasageek.utexasutilities.AsyncTask;
import com.nasageek.utexasutilities.AuthCookie;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.TempLoginException;
import com.nasageek.utexasutilities.UTilitiesApplication;
import com.nasageek.utexasutilities.activities.CampusMapActivity;
import com.nasageek.utexasutilities.activities.LoginActivity;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.nasageek.utexasutilities.UTilitiesApplication.UTD_AUTH_COOKIE_KEY;

public class ExamScheduleFragment extends Fragment implements ActionModeFragment {

    private TextView login_first;
    private OkHttpClient httpclient;
    private ArrayList<String> examlist;
    private ListView examlistview;
    private ExamAdapter ea;
    private LinearLayout pb_ll;
    private FragmentActivity parentAct;
    // private View vg;
    public ActionMode mode;
    private TextView netv;
    private TextView eetv;
    private LinearLayout ell;
    String semId;
    private AuthCookie utdAuthCookie;

    public static ExamScheduleFragment newInstance(String title, String id) {
        ExamScheduleFragment esf = new ExamScheduleFragment();

        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("semId", id);
        esf.setArguments(args);

        return esf;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View vg = inflater.inflate(R.layout.exam_schedule_fragment_layout, container, false);
        updateView(semId, vg);
        return vg;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentAct = this.getActivity();
        semId = getArguments().getString("semId");
        utdAuthCookie = ((UTilitiesApplication) getActivity().getApplication())
                .getAuthCookie(UTD_AUTH_COOKIE_KEY);
    }

    public void updateView(String semId, View vg) {
        this.semId = semId;
        examlist = new ArrayList<>();
        login_first = (TextView) vg.findViewById(R.id.login_first_tv);
        pb_ll = (LinearLayout) vg.findViewById(R.id.examschedule_progressbar_ll);
        examlistview = (ListView) vg.findViewById(R.id.examschedule_listview);
        netv = (TextView) vg.findViewById(R.id.no_exams);
        ell = (LinearLayout) vg.findViewById(R.id.examschedule_error);
        eetv = (TextView) vg.findViewById(R.id.tv_failure);

        if (!utdAuthCookie.hasCookieBeenSet()) {
            pb_ll.setVisibility(View.GONE);
            login_first.setVisibility(View.VISIBLE);
        } else {
            parser();
        }
    }

    public void parser() {
        httpclient = new OkHttpClient();
        new fetchExamDataTask(httpclient).execute(false);
    }

    @Override
    public ActionMode getActionMode() {
        return mode;
    }

    private class fetchExamDataTask extends AsyncTask<Boolean, Void, Integer> {
        private OkHttpClient client;
        private String errorMsg;

        private final static int RESULT_SUCCESS = 0;
        private final static int RESULT_FAIL_NOT_ENROLLED = 1;
        private final static int RESULT_FAIL_TOO_EARLY = 2;


        public fetchExamDataTask(OkHttpClient client) {
            this.client = client;
        }

        @Override
        protected void onPreExecute() {
            pb_ll.setVisibility(View.VISIBLE);
            examlistview.setVisibility(View.GONE);
            netv.setVisibility(View.GONE);
            ell.setVisibility(View.GONE);
        }

        @Override
        protected Integer doInBackground(Boolean... params) {
            Boolean recursing = params[0];

            String reqUrl = "https://utdirect.utexas.edu/registrar/exam_schedule.WBX";
            Request request = new Request.Builder()
                    .url(reqUrl)
                    .build();
            String pagedata = "";

            try {
                Response response = client.newCall(request).execute();
                pagedata = response.body().string();
            } catch (IOException e) {
                errorMsg = "UTilities could not fetch your exam schedule";
                e.printStackTrace();
                cancel(true);
                return null;
            }

            if (pagedata.contains("<title>UT EID Login</title>")) {
                errorMsg = "You've been logged out of UTDirect, back out and log in again.";
                if (parentAct != null) {
                    UTilitiesApplication mApp = (UTilitiesApplication) parentAct.getApplication();
                    if (!recursing) {
                        try {
                            mApp.getAuthCookie(UTD_AUTH_COOKIE_KEY).logout();
                            mApp.getAuthCookie(UTD_AUTH_COOKIE_KEY).login();
                        } catch (IOException e) {
                            errorMsg = "UTilities could not fetch your exam schedule";
                            cancel(true);
                            e.printStackTrace();
                            return null;
                        } catch (TempLoginException tle) {
                            /*
                            ooooh boy is this lazy. I'd rather not init SharedPreferences here
                            to check if persistent login is on, so we'll just catch the exception
                             */
                            Intent login = new Intent(parentAct, LoginActivity.class);
                            login.putExtra("activity", parentAct.getIntent().getComponent()
                                    .getClassName());
                            login.putExtra("service", 'u');
                            parentAct.startActivity(login);
                            parentAct.finish();
                            errorMsg = "Session expired, please log in again";
                            cancel(true);
                            return null;
                        }
                        return doInBackground(true);
                    } else {
                        mApp.logoutAll();
                    }
                }
                cancel(true);
                return null;
            }
            if (pagedata.contains("will be available approximately three weeks")) {
                return RESULT_FAIL_TOO_EARLY;
            } else if (pagedata.contains("Our records indicate that you are not enrolled" +
                    " for the current semester.")) {
                return RESULT_FAIL_NOT_ENROLLED;
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
            return RESULT_SUCCESS;
        }

        @Override
        protected void onPostExecute(Integer result) {
            switch (result) {
                case RESULT_SUCCESS:
                    ea = new ExamAdapter(parentAct, examlist);
                    examlistview.setAdapter(ea);
                    examlistview.setOnItemClickListener(ea);
                    examlistview.setVisibility(View.VISIBLE);
                    break;
                case RESULT_FAIL_TOO_EARLY:
                    netv.setText("'Tis not the season for final exams.\nTry back later!" +
                            "\n(about 3 weeks before they begin)");
                    netv.setVisibility(View.VISIBLE);
                    break;
                case RESULT_FAIL_NOT_ENROLLED:
                    netv.setText("You aren't enrolled for the current semester.");
                    netv.setVisibility(View.VISIBLE);
                    break;
            }
            pb_ll.setVisibility(View.GONE);
        }

        @Override
        protected void onCancelled() {
            eetv.setText(errorMsg);
            netv.setVisibility(View.GONE);
            pb_ll.setVisibility(View.GONE);
            examlistview.setVisibility(View.GONE);
            login_first.setVisibility(View.GONE);
            ell.setVisibility(View.VISIBLE);
        }
    }

    private class ExamAdapter extends ArrayAdapter<String> implements
            AdapterView.OnItemClickListener {
        private Context con;
        private ArrayList<String> exams;
        private LayoutInflater li;

        public ExamAdapter(Context c, ArrayList<String> objects) {
            super(c, 0, objects);
            con = c;
            exams = objects;
            li = (LayoutInflater) con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return exams.size();
        }

        @Override
        public String getItem(int position) {
            return exams.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
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
            String[] examdata = exams.get(position).split("\\^");
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
            String course = "";
            ViewGroup vg = (ViewGroup) convertView;
            if (vg == null) {
                vg = (ViewGroup) li.inflate(R.layout.exam_item_view, parent, false);
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

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mode = ((AppCompatActivity)getActivity())
                    .startSupportActionMode(new ScheduleActionMode(position));
        }

        final class ScheduleActionMode implements ActionMode.Callback {

            int position;

            public ScheduleActionMode(int pos) {
                position = pos;
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.setTitle("Exam Info");
                MenuInflater inflater = getActivity().getMenuInflater();
                String[] elements = exams.get(position).split("\\^");
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
                        Intent map = new Intent(con.getString(R.string.building_intent), null, con,
                                CampusMapActivity.class);

                        String[] elements = exams.get(position).split("\\^");
                        if (elements.length >= 5) {
                            building.add(elements[4].split(" ")[0]);
                            map.putStringArrayListExtra("buildings", building);
                            // map.setData(Uri.parse(elements[4].split(" ")[0]));
                            con.startActivity(map);
                            return true;
                        } else {
                            Toast.makeText(con, "Your exam's location could not be found",
                                    Toast.LENGTH_SHORT).show();
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

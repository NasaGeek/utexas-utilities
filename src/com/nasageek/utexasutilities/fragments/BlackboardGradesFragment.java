
package com.nasageek.utexasutilities.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.mapsaurus.paneslayout.FragmentLauncher;
import com.nasageek.utexasutilities.AsyncTask;
import com.nasageek.utexasutilities.ConnectionHelper;
import com.nasageek.utexasutilities.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlackboardGradesFragment extends BlackboardFragment {

    private LinearLayout g_pb_ll;
    private ListView glv;

    private LinearLayout gell;
    private TextView getv;

    private String courseID, courseName, viewUri;
    private DefaultHttpClient httpclient;
    private fetchGradesTask fetch;

    private ArrayList<bbGrade> grades;
    private GradesAdapter gradeAdapter;

    public BlackboardGradesFragment() {
    }

    public static BlackboardGradesFragment newInstance(String courseID, String courseName,
            String viewUri, boolean fromDashboard) {
        BlackboardGradesFragment bgf = new BlackboardGradesFragment();

        Bundle args = new Bundle();
        args.putString("courseID", courseID);
        args.putString("courseName", courseName);
        args.putString("viewUri", viewUri);
        args.putBoolean("fromDashboard", fromDashboard);
        bgf.setArguments(args);

        return bgf;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        courseID = getArguments().getString("courseID");
        courseName = getArguments().getString("courseName");
        viewUri = getArguments().getString("viewUri");
        setHasOptionsMenu(true);

        grades = new ArrayList<bbGrade>();
        gradeAdapter = new GradesAdapter(getActivity(), grades);

        httpclient = ConnectionHelper.getThreadSafeClient();
        httpclient.getCookieStore().clear();
        BasicClientCookie cookie = new BasicClientCookie("s_session_id",
                ConnectionHelper.getBbAuthCookie(getActivity(), httpclient));
        cookie.setDomain(ConnectionHelper.BLACKBOARD_DOMAIN_NOPROT);
        httpclient.getCookieStore().addCookie(cookie);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: check type of container, should we get the current index from
        // container or parent activity?

        final View vg = inflater.inflate(R.layout.blackboard_grades_layout, container, false);

        setupActionBar();
        g_pb_ll = (LinearLayout) vg.findViewById(R.id.grades_progressbar_ll);
        glv = (ListView) vg.findViewById(R.id.gradesListView);

        gell = (LinearLayout) vg.findViewById(R.id.grades_error);
        getv = (TextView) vg.findViewById(R.id.tv_failure);

        glv.setAdapter(gradeAdapter);
        glv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                bbGrade grade = (bbGrade) arg0.getAdapter().getItem(arg2);

                Dialog dlg = new Dialog(getActivity());// ,R.style.Theme_Sherlock_Light_Dialog);
                dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dlg.setContentView(R.layout.grade_info_dialog);
                dlg.setTitle("Grade Info");

                TextView name = (TextView) dlg.findViewById(R.id.grade_info_name);
                TextView value = (TextView) dlg.findViewById(R.id.grade_info_value);
                TextView comment = (TextView) dlg.findViewById(R.id.grade_info_comment);

                name.setText(grade.getName());

                String valueString = null;
                if (grade.getNumGrade().equals(-1)) {
                    valueString = "-";
                } else if (grade.getNumGrade().equals(-2)) {
                    valueString = grade.getGrade();
                } else {
                    valueString = grade.getNumGrade() + "/" + grade.getNumPointsPossible();
                }
                value.setText(valueString);
                comment.setText(grade.getComment());

                dlg.setCanceledOnTouchOutside(true);
                dlg.show();
                // TODO: DialogFragment or showDialog
            }

        });

        // there should always be at least 2 grades, so checking for 0 is a
        // valid way to see if it couldn't load last time
        if (grades.size() == 0) {
            fetch = new fetchGradesTask(httpclient);
            fetch.execute();
        }

        return vg;

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        menu.clear();
        // if(!getIntent().getBooleanExtra("showViewInWeb", false))
        if (viewUri != null && !viewUri.equals("")) {
            inflater.inflate(R.menu.blackboard_grades_menu, menu);
            // menu.removeItem(R.id.viewInWeb);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.grades_view_in_web:
                showAreYouSureDlg(getActivity());
                break;
        }
        return false;
    }

    private void showAreYouSureDlg(final Context con) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(con);
        alertBuilder
                .setMessage("Would you like to view this item on the Blackboard website? (you might need to log in again if"
                        + " you have disabled the embedded browser)");
        alertBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

            }
        });

        alertBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {

                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(con);
                if (sp.getBoolean("embedded_browser", true)) {
                    ((FragmentLauncher) getActivity()).addFragment(
                            BlackboardGradesFragment.this, BlackboardExternalItemFragment
                                    .newInstance(viewUri, courseID, courseName, "Grades", false));
                } else {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(viewUri));
                    startActivity(i);
                }

                /*
                 * Intent web = new
                 * Intent(null,Uri.parse(getIntent().getStringExtra
                 * ("viewUri")),BlackboardGradesActivity
                 * .this,BlackboardExternalItemActivity.class);
                 * web.putExtra("itemName", "Grades");
                 * web.putExtra("coursename",
                 * getIntent().getStringExtra("coursename"));
                 * startActivity(web);
                 */
            }
        });
        alertBuilder.setTitle("View on Blackboard");
        alertBuilder.show();
    }

    private void setupActionBar() {
        final ActionBar actionbar = getSherlockActivity().getSupportActionBar();
        actionbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE, ActionBar.DISPLAY_SHOW_TITLE
                | ActionBar.DISPLAY_SHOW_CUSTOM);
        actionbar.setTitle(courseName);
        actionbar.setSubtitle("Grades");
    }

    @Override
    public String getBbid() {
        return getArguments().getString("courseID");
    }

    @Override
    public String getCourseName() {
        return getArguments().getString("courseName");
    }

    @Override
    public boolean isFromDashboard() {
        return getArguments().getBoolean("fromDashboard");
    }

    private class fetchGradesTask extends AsyncTask<Object, Void, ArrayList<bbGrade>> {
        private DefaultHttpClient client;
        private String errorMsg;

        public fetchGradesTask(DefaultHttpClient client) {
            this.client = client;
        }

        @Override
        protected void onPreExecute() {
            g_pb_ll.setVisibility(View.VISIBLE);
            glv.setVisibility(View.GONE);
            gell.setVisibility(View.GONE);
        }

        @Override
        protected ArrayList<bbGrade> doInBackground(Object... params) {

            HttpGet hget = new HttpGet(ConnectionHelper.BLACKBOARD_DOMAIN
                    + "/webapps/Bb-mobile-BBLEARN/courseData?course_section=GRADES&course_id="
                    + courseID);
            String pagedata = "";

            try {
                HttpResponse response = client.execute(hget);
                pagedata = EntityUtils.toString(response.getEntity());
            } catch (Exception e) {
                errorMsg = "UTilities could not fetch this course's grades";
                cancel(true);
                e.printStackTrace();
                return null;
            }
            ArrayList<bbGrade> data = new ArrayList<bbGrade>();

            Pattern gradeItemPattern = Pattern.compile("<grade-item.*?/>", Pattern.DOTALL);
            Matcher gradeItemMatcher = gradeItemPattern.matcher(pagedata);

            while (gradeItemMatcher.find()) {
                String gradeData = gradeItemMatcher.group();
                Pattern namePattern = Pattern.compile("name=\"(.*?)\"");
                Matcher nameMatcher = namePattern.matcher(gradeData);
                Pattern pointsPattern = Pattern.compile("pointspossible=\"(.*?)\"");
                Matcher pointsMatcher = pointsPattern.matcher(gradeData);
                Pattern gradePattern = Pattern.compile("grade=\"(.*?)\"");
                Matcher gradeMatcher = gradePattern.matcher(gradeData);
                Pattern commentPattern = Pattern.compile("comments=\"(.*?)\"", Pattern.DOTALL);
                Matcher commentMatcher = commentPattern.matcher(gradeData);

                if (nameMatcher.find() && pointsMatcher.find() && gradeMatcher.find()) {
                    data.add(new bbGrade(nameMatcher.group(1).replace("&amp;", "&"), gradeMatcher
                            .group(1), pointsMatcher.group(1), commentMatcher.find() ? Html
                            .fromHtml(Html.fromHtml(commentMatcher.group(1)).toString()).toString()
                            : "No comments"));
                }
            }
            return data;
        }

        @Override
        protected void onPostExecute(ArrayList<bbGrade> result) {
            if (!this.isCancelled()) {
                grades.addAll(result);
                gradeAdapter.notifyDataSetChanged();

                g_pb_ll.setVisibility(View.GONE);
                gell.setVisibility(View.GONE);
                glv.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected void onCancelled() {
            getv.setText(errorMsg);

            g_pb_ll.setVisibility(View.GONE);
            glv.setVisibility(View.GONE);
            gell.setVisibility(View.VISIBLE);
        }
    }

    class GradesAdapter extends ArrayAdapter<bbGrade> {

        private Context con;
        private ArrayList<bbGrade> items;
        private LayoutInflater li;

        public GradesAdapter(Context c, ArrayList<bbGrade> items) {
            super(c, 0, items);
            con = c;
            this.items = items;
            li = (LayoutInflater) con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }

        @Override
        public int getCount() {

            return items.size();
        }

        @Override
        public bbGrade getItem(int position) {

            return items.get(position);
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
            bbGrade grade = items.get(position);

            String title = grade.getName();
            String value = null;
            if (grade.getNumGrade().equals(-1)) {
                value = "-";
            } else if (grade.getNumGrade().equals(-2)) {
                value = grade.getGrade();
            } else {
                value = grade.getNumGrade() + "/" + grade.getNumPointsPossible();
            }
            ViewGroup lin = (ViewGroup) convertView;

            if (lin == null) {
                lin = (RelativeLayout) li.inflate(R.layout.grade_item_view, null, false);
            }

            TextView gradeName = (TextView) lin.findViewById(R.id.grade_name);
            TextView gradeValue = (TextView) lin.findViewById(R.id.grade_value);
            ImageView commentImg = (ImageView) lin.findViewById(R.id.comment_available_img);

            if (grade.getComment().equals("No comments")) {
                commentImg.setVisibility(View.INVISIBLE);
            } else {
                commentImg.setVisibility(View.VISIBLE);
            }
            gradeName.setText(title);
            gradeValue.setText(value);

            return lin;
        }
    }

    class bbGrade {
        String name, grade, pointsPossible, comment;

        public bbGrade(String name, String grade, String pointsPossible, String comment) {
            this.name = name;
            this.grade = grade;
            this.pointsPossible = pointsPossible;
            this.comment = comment;
        }

        public String getName() {
            return name;
        }

        public String getComment() {
            return comment;
        }

        public Number getNumGrade() {
            if (!grade.equals("-")) {
                String temp = grade.replaceAll("[^\\d\\.]*", "");
                if (temp.equals("")) {
                    return -2;
                }
                double d = Double.parseDouble(temp);
                if (d == Math.floor(d)) {
                    return (int) d;
                } else {
                    return d;
                }
            } else {
                return -1;
            }
        }

        public String getGrade() {
            return grade;
        }

        public String getPointsPossible() {
            return pointsPossible;
        }

        public Number getNumPointsPossible() {
            String temp = pointsPossible.replaceAll("[^\\d\\.]*", "");
            double d = Double.parseDouble(temp);
            if (d == Math.floor(d)) {
                return (int) d;
            } else {
                return d;
            }
        }

    }

    @Override
    public void onPanesScrolled() {
        setupActionBar();
    }

}


package com.nasageek.utexasutilities.fragments;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.mapsaurus.paneslayout.FragmentLauncher;
import com.nasageek.utexasutilities.AsyncTask;
import com.nasageek.utexasutilities.R;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import android.app.AlertDialog;
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
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlackboardAnnouncementsFragment extends BlackboardFragment {

    private LinearLayout a_pb_ll;
    private ListView alv;
    private TextView atv;
    private TextView etv;
    private LinearLayout ell;
    private OkHttpClient httpclient;
    private fetchAnnouncementsTask fetch;
    private AnnouncementsAdapter announceAdapter;
    private ArrayList<bbAnnouncement> announcements;
    private boolean noAnnouncements = false;
    private String courseID, courseName, viewUri;
    private String selection;
    private boolean fromDashboard;

    public BlackboardAnnouncementsFragment() {
    }

    public static BlackboardAnnouncementsFragment newInstance(String courseID, String courseName,
            String viewUri, Boolean fromDashboard, String selection) {
        BlackboardAnnouncementsFragment baf = new BlackboardAnnouncementsFragment();

        Bundle args = new Bundle();
        args.putString("courseID", courseID);
        args.putString("courseName", courseName);
        args.putString("viewUri", viewUri);
        args.putBoolean("fromDashboard", fromDashboard);
        args.putString("selection", selection);
        baf.setArguments(args);

        return baf;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        courseID = getArguments().getString("courseID");
        courseName = getArguments().getString("courseName");
        viewUri = getArguments().getString("viewUri");
        fromDashboard = getArguments().getBoolean("fromDashboard");
        selection = getArguments().getString("selection");
        setHasOptionsMenu(true);
        announcements = new ArrayList<>();
        announceAdapter = new AnnouncementsAdapter(getActivity(), announcements);
        httpclient = new OkHttpClient();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View vg = inflater
                .inflate(R.layout.blackboard_announcements_layout, container, false);

        setupActionBar();
        a_pb_ll = (LinearLayout) vg.findViewById(R.id.announcements_progressbar_ll);
        alv = (ListView) vg.findViewById(R.id.announcementsListView);
        atv = (TextView) vg.findViewById(R.id.no_announcements_textview);
        etv = (TextView) vg.findViewById(R.id.tv_failure);
        ell = (LinearLayout) vg.findViewById(R.id.announcements_error);

        alv.setEmptyView(atv);
        alv.setAdapter(announceAdapter);

        if (announcements.size() == 0 && !noAnnouncements) {
            fetch = new fetchAnnouncementsTask(httpclient);
            fetch.execute();
        }

        return vg;
    }

    private void setupActionBar() {
        final ActionBar actionbar = getSherlockActivity().getSupportActionBar();
        actionbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE, ActionBar.DISPLAY_SHOW_TITLE
                | ActionBar.DISPLAY_SHOW_CUSTOM);
        actionbar.setTitle(getArguments().getString("courseName"));
        actionbar.setSubtitle("Announcements");
    }

    @Override
    public String getBbid() {
        return courseID;
    }

    @Override
    public String getCourseName() {
        return courseName;
    }

    @Override
    public boolean isFromDashboard() {
        return fromDashboard;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        menu.clear();

        // if(!getIntent().getBooleanExtra("showViewInWeb", false))
        if (viewUri != null && !viewUri.equals("")) {
            inflater.inflate(R.menu.blackboard_announcements_menu, menu);
            // menu.removeItem(R.id.announcements_view_in_web);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.announcements_view_in_web:
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
                            BlackboardAnnouncementsFragment.this, BlackboardExternalItemFragment
                                    .newInstance(viewUri, courseID, courseName, "Announcements",
                                            false));
                } else {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(viewUri));
                    startActivity(i);
                }

                /*
                 * Intent web = new
                 * Intent(null,Uri.parse(getIntent().getStringExtra
                 * ("viewUri")),BlackboardAnnouncementsActivity
                 * .this,BlackboardExternalItemActivity.class);
                 * web.putExtra("itemName", "Announcements");
                 * web.putExtra("coursename",
                 * getIntent().getStringExtra("coursename"));
                 * startActivity(web);
                 */
            }
        });
        alertBuilder.setTitle("View on Blackboard");
        alertBuilder.show();
    }

    private class fetchAnnouncementsTask extends AsyncTask<Object, Void, ArrayList<bbAnnouncement>> {
        private OkHttpClient client;
        private String errorMsg;
        private int selectIndex = 0;

        public fetchAnnouncementsTask(OkHttpClient client) {
            this.client = client;
        }

        @Override
        protected void onPreExecute() {
            a_pb_ll.setVisibility(View.VISIBLE);
            alv.setVisibility(View.GONE);
            atv.setVisibility(View.GONE);
            ell.setVisibility(View.GONE);
        }

        @Override
        protected ArrayList<bbAnnouncement> doInBackground(Object... params) {
            String reqUrl = BLACKBOARD_DOMAIN
                    + "/webapps/Bb-mobile-BBLEARN/courseData?course_section=ANNOUNCEMENTS&course_id="
                    + getArguments().getString("courseID");
            Request request = new Request.Builder()
                    .url(reqUrl)
                    .build();
            String pagedata = "";

            try {
                Response response = client.newCall(request).execute();
                pagedata = response.body().string();
            } catch (IOException e) {
                errorMsg = "UTilities could not fetch this course's announcements";
                e.printStackTrace();
                cancel(true);
                return null;
            }

            ArrayList<bbAnnouncement> data = new ArrayList<>();
            // pagedata = pagedata.replaceAll("comments=\".*?\"", ""); //might
            // include later, need to strip for now for grade recognition

            Pattern announcementPattern = Pattern
                    .compile(
                            "<announcement .*?subject=\"(.*?)\".*?startdate=\"(.*?)\".*?>(.*?)</announcement>",
                            Pattern.DOTALL);
            Matcher announcementMatcher = announcementPattern.matcher(pagedata);

            while (announcementMatcher.find()) {
                data.add(new bbAnnouncement(announcementMatcher.group(1), announcementMatcher
                        .group(2), announcementMatcher.group(3)));
            }

            if (!selection.equals("")) {
                for (int i = 0; i < data.size(); i++) {
                    if (data.get(i).getSubject().equals(selection)) {
                        selectIndex = i;
                    }
                }
            }
            return data;
        }

        @Override
        protected void onPostExecute(ArrayList<bbAnnouncement> result) {
            if (!this.isCancelled()) {
                a_pb_ll.setVisibility(View.GONE);
                ell.setVisibility(View.GONE);
                // if(!result.isEmpty())
                // {
                announcements.addAll(result);
                if (announcements.size() == 0) {
                    noAnnouncements = true;
                }
                announceAdapter.notifyDataSetChanged();
                alv.setSelection(selectIndex);
                // alv.setVisibility(View.VISIBLE);
                // atv.setVisibility(View.GONE);
                /*
                 * } else { // atv.setVisibility(View.VISIBLE); //
                 * alv.setVisibility(View.GONE); }
                 */
            }
        }

        @Override
        protected void onCancelled() {
            etv.setText(errorMsg);
            a_pb_ll.setVisibility(View.GONE);
            alv.setVisibility(View.GONE);
            atv.setVisibility(View.GONE);
            ell.setVisibility(View.VISIBLE);
        }
    }

    class AnnouncementsAdapter extends ArrayAdapter<bbAnnouncement> {

        private Context con;
        private ArrayList<bbAnnouncement> items;
        private LayoutInflater li;

        public AnnouncementsAdapter(Context c, ArrayList<bbAnnouncement> items) {
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
        public bbAnnouncement getItem(int position) {

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
            return false;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            bbAnnouncement announce = items.get(position);

            String subject = announce.getSubject();
            String date = announce.getFormattedDate();
            String body = announce.getFormattedBody();

            ViewGroup lin = (ViewGroup) convertView;

            if (lin == null) {
                lin = (LinearLayout) li.inflate(R.layout.announcement_item_view, parent, false);
            }

            TextView announcementSubject = (TextView) lin
                    .findViewById(R.id.announcement_header_subject);
            TextView announcementDate = (TextView) lin.findViewById(R.id.announcement_header_date);
            TextView announcementBody = (TextView) lin.findViewById(R.id.announcement_body);

            announcementSubject.setText(subject);
            announcementDate.setText(date);
            announcementBody.setText(body);

            return lin;
        }
    }

    class bbAnnouncement {
        private String subject, date, body;

        public bbAnnouncement(String subject, String date, String body) {
            this.subject = subject;
            this.date = date;
            this.body = body;
        }

        public String getSubject() {
            return subject;
        }

        public String getFormattedDate() {
            return date.substring(0, date.indexOf('T'));
        }

        public String getFormattedBody() {
            return Html.fromHtml(Html.fromHtml(body).toString()).toString();
        }
    }

    @Override
    public void onPanesScrolled() {
        setupActionBar();
    }

}

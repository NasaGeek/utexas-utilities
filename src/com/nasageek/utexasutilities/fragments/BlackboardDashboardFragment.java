
package com.nasageek.utexasutilities.fragments;

import com.actionbarsherlock.app.SherlockFragment;
import com.foound.widget.AmazingAdapter;
import com.foound.widget.AmazingListView;
import com.mapsaurus.paneslayout.FragmentLauncher;
import com.nasageek.utexasutilities.AsyncTask;
import com.nasageek.utexasutilities.BlackboardDashboardXmlParser;
import com.nasageek.utexasutilities.MyPair;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.Utility;
import com.nasageek.utexasutilities.model.BBCourse;
import com.nasageek.utexasutilities.model.FeedItem;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.acra.ACRA;
import org.xmlpull.v1.XmlPullParserException;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Fragment for displaying the user's Blackboard notification dashboard, a list of
 * all course updates arranged in reverse chronological order.
 */
public class BlackboardDashboardFragment extends SherlockFragment {

    private LinearLayout d_pb_ll;
    private AmazingListView dlv;
    private TextView etv;
    private LinearLayout ell;
    private Button eb;
    private FetchDashboardTask fetch;
    private boolean longform;
    private HashMap<String, BBCourse> courses;
    private List<MyPair<String, List<FeedItem>>> feedList;
    private BlackboardDashboardAdapter bda;

    public BlackboardDashboardFragment() {
    }

    public static BlackboardDashboardFragment newInstance(String title) {
        BlackboardDashboardFragment f = new BlackboardDashboardFragment();

        Bundle args = new Bundle();
        args.putString("title", title);
        f.setArguments(args);

        return f;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        longform = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(
                "blackboard_class_longform", false);
        if (savedInstanceState == null) {
            feedList = new ArrayList<>();
        } else {
            feedList = (List<MyPair<String, List<FeedItem>>>) savedInstanceState
                    .getSerializable("feedList");
            courses = (HashMap<String, BBCourse>) savedInstanceState.getSerializable("courses");
        }
        bda = new BlackboardDashboardAdapter(feedList);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vg = inflater.inflate(R.layout.blackboard_dashboard_fragment, container, false);

        dlv = (AmazingListView) vg.findViewById(R.id.dash_listview);
        d_pb_ll = (LinearLayout) vg.findViewById(R.id.dash_progressbar_ll);
        ell = (LinearLayout) vg.findViewById(R.id.dash_error);
        etv = (TextView) vg.findViewById(R.id.tv_failure);
        eb = (Button) vg.findViewById(R.id.button_send_data);

        dlv.setAdapter(bda);
        dlv.setPinnedHeaderView(inflater.inflate(R.layout.menu_header_item_view, dlv, false));
        dlv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FeedItem fi = (FeedItem) parent.getAdapter().getItem(position);
                String courseid = fi.getBbId();
                String contentid = fi.getContentId();
                String coursename = courses.get(fi.getBbId()).getCourseCode();
                String message = fi.getMessage();

                FragmentLauncher launcher = (FragmentLauncher) getActivity();
                Fragment currentFragment = BlackboardDashboardFragment.this.getParentFragment();
                switch (fi.getType()) {
                    case "Grades":
                        launcher.addFragment(currentFragment,
                                BlackboardGradesFragment.newInstance(courseid, coursename, "", true,
                                        fi.getMessage()));
                        break;
                    case "Content":
                        launcher.addFragment(currentFragment,
                                BlackboardDownloadableItemFragment.newInstance(contentid, courseid,
                                        coursename, message, "", true));
                        break;
                    case "Announcement":
                        launcher.addFragment(currentFragment,
                                BlackboardAnnouncementsFragment.newInstance(courseid, coursename,
                                        "", true, fi.getMessage()));
                        break;
                    case "Courses":
                        launcher.addFragment(currentFragment,
                                BlackboardCourseMapFragment.newInstance(
                                        getString(R.string.coursemap_intent), null, courseid,
                                        coursename, "Course Map", "", -1, false));
                        break;
                }
            }
        });

        if (feedList.size() == 0) {
            OkHttpClient httpclient = new OkHttpClient();
            fetch = new FetchDashboardTask(httpclient);
            Utility.parallelExecute(fetch);
        }
        return vg;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fetch != null) {
            fetch.cancel(true);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("feedList", (Serializable) feedList);
        outState.putSerializable("courses", courses);
    }

    private class FetchDashboardTask extends
            AsyncTask<String, Void, List<MyPair<String, List<FeedItem>>>> {
        private OkHttpClient client;
        private String errorMsg = "";
        private List<MyPair<String, List<FeedItem>>> tempFeedList;
        private Exception ex;
        private String pagedata;
        private Boolean showButton = false;

        public FetchDashboardTask(OkHttpClient client) {
            this.client = client;
        }

        @Override
        protected void onPreExecute() {
            d_pb_ll.setVisibility(View.VISIBLE);
            dlv.setVisibility(View.GONE);
            ell.setVisibility(View.GONE);
        }

        @Override
        protected List<MyPair<String, List<FeedItem>>> doInBackground(String... params) {
            tempFeedList = new ArrayList<>();

            String reqUrl = BlackboardFragment.BLACKBOARD_DOMAIN
                    + "/webapps/Bb-mobile-BBLEARN/dashboard?course_type=COURSE&" +
                    "with_notifications=true";
            Request request = new Request.Builder()
                    .url(reqUrl)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                pagedata = response.body().string();
            } catch (IOException e) {
                errorMsg = "UTilities could not fetch your Blackboard Dashboard";
                e.printStackTrace();
                cancel(true);
                return null;
            }

            try {
                BlackboardDashboardXmlParser parser = new BlackboardDashboardXmlParser();
                tempFeedList = parser.parse(new StringReader(pagedata));
                courses = parser.getCourses();
            } catch (IOException|XmlPullParserException e) {
                errorMsg = "UTilities could not parse the downloaded Dashboard data.";
                // only show the button if setting pagedata didn't cause the exception
                if (pagedata != null) {
                    showButton = true;
                }
                ex = e;
                e.printStackTrace();
                cancel(true);
                return null;
            }
            return feedList;
        }

        @Override
        protected void onPostExecute(List<MyPair<String, List<FeedItem>>> result) {
            feedList.addAll(tempFeedList);
            bda.notifyDataSetChanged();
            d_pb_ll.setVisibility(View.GONE);
            dlv.setVisibility(View.VISIBLE);
            ell.setVisibility(View.GONE);
        }

        @Override
        protected void onCancelled(List<MyPair<String, List<FeedItem>>> result) {
            etv.setText(errorMsg);
            if (showButton) {
                eb.setText("Send anonymous information about the dashboard to the developer to help improve UTilities.");
                eb.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (ex != null) {
                            SharedPreferences sp = PreferenceManager
                                    .getDefaultSharedPreferences(getActivity().getBaseContext());
                            if (!sp.getBoolean("acra.enable", true)) {
                                ACRA.getErrorReporter().setEnabled(true);
                            }
                            ACRA.getErrorReporter().putCustomData("xmldata", pagedata);
                            ACRA.getErrorReporter().handleException(ex);
                            ACRA.getErrorReporter().removeCustomData("xmldata");
                            if (!sp.getBoolean("acra.enable", true)) {
                                ACRA.getErrorReporter().setEnabled(false);
                            }
                            Toast.makeText(getActivity(),
                                    "Data is being sent, thanks for helping out!",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(),
                                    "Couldn't send the dashboard data for some reason :(",
                                    Toast.LENGTH_SHORT).show();
                        }
                        v.setVisibility(View.INVISIBLE);

                    }
                });
                eb.setVisibility(View.VISIBLE);
            }
            d_pb_ll.setVisibility(View.GONE);
            dlv.setVisibility(View.GONE);
            ell.setVisibility(View.VISIBLE);
        }
    }

    class BlackboardDashboardAdapter extends AmazingAdapter {

        private List<MyPair<String, List<FeedItem>>> items;

        public BlackboardDashboardAdapter(List<MyPair<String, List<FeedItem>>> items) {
            this.items = items;
        }

        @Override
        public int getCount() {
            int res = 0;
            for (MyPair<String, List<FeedItem>> pair : items) {
                res += pair.second.size();
            }
            return res;
        }

        @Override
        public boolean isEnabled(int position) {
            return !("Unknown".equals(getItem(position).getType()) ||
                    "Notification".equals(getItem(position).getType()));
        }

        @Override
        public FeedItem getItem(int position) {
            int c = 0;
            for (MyPair<String, List<FeedItem>> pair : items) {
                if (position >= c && position < c + pair.second.size()) {
                    return pair.second.get(position - c);
                }
                c += pair.second.size();
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        protected void onNextPageRequested(int page) {
        }

        @Override
        protected void bindSectionHeader(View view, int position, boolean displaySectionHeader) {
            if (displaySectionHeader) {
                view.findViewById(R.id.header).setVisibility(View.VISIBLE);
                TextView lSectionTitle = (TextView) view.findViewById(R.id.header);
                lSectionTitle.setText(getSections()[getSectionForPosition(position)]);
            } else {
                view.findViewById(R.id.header).setVisibility(View.GONE);
            }
        }

        @Override
        public View getAmazingView(int position, View convertView, ViewGroup parent) {
            View res = convertView;
            if (res == null) {
                res = getActivity().getLayoutInflater().inflate(R.layout.dashboard_item_view, null);
            }
            FeedItem fi = getItem(position);
            BBCourse course = courses.get(fi.getBbId());

            TextView courseName = (TextView) res.findViewById(R.id.d_course_name);
            TextView contentType = (TextView) res.findViewById(R.id.d_content_type);
            TextView message = (TextView) res.findViewById(R.id.d_message);

            if (!longform) {
                // TODO: make this look nice for malformed classes
                if (!"".equals(course.getCourseCode())) {
                    courseName.setText(course.getCourseCode() + " - " + course.getName() + " ("
                            + course.getUnique() + ")");
                } else {
                    courseName.setText(course.getName() + " (" + course.getUnique() + ")");
                }
            } else {
                courseName.setText(course.getFullName());
            }

            contentType.setText(fi.getType());
            message.setText(fi.getMessage());

            if (!isEnabled(position)) {
                TypedValue tv = new TypedValue();
                if (getActivity().getTheme().resolveAttribute(android.R.attr.textColorTertiary, tv,
                        true)) {
                    TypedArray arr = getActivity().obtainStyledAttributes(tv.resourceId, new int[] {
                        android.R.attr.textColorTertiary
                    });
                    message.setTextColor(arr.getColor(0, Color.BLACK));
                    courseName.setTextColor(arr.getColor(0, Color.BLACK));
                }
            } else {
                message.setTextColor(Color.BLACK);
                courseName.setTextColor(Color.BLACK);
            }
            return res;
        }

        @Override
        public void configurePinnedHeader(View header, int position, int alpha) {
            TextView lSectionHeader = (TextView) header;
            lSectionHeader.setText(getSections()[getSectionForPosition(position)]);
        }

        @Override
        public int getPositionForSection(int section) {
            if (section < 0) {
                section = 0;
            }
            if (section >= items.size()) {
                section = items.size() - 1;
            }
            int c = 0;
            for (int i = 0; i < items.size(); i++) {
                if (section == i) {
                    return c;
                }
                c += items.get(i).second.size();
            }
            return 0;
        }

        @Override
        public int getSectionForPosition(int position) {
            int c = 0;
            for (int i = 0; i < items.size(); i++) {
                if (position >= c && position < c + items.get(i).second.size()) {
                    return i;
                }
                c += items.get(i).second.size();
            }
            return 0;
        }

        @Override
        public String[] getSections() {
            String[] res = new String[items.size()];
            for (int i = 0; i < items.size(); i++) {
                res[i] = items.get(i).first;
            }
            return res;
        }

        @Override
        protected View getLoadingView(ViewGroup parent) {
            return null;
        }
    }
}


package com.nasageek.utexasutilities.fragments;

import com.actionbarsherlock.app.SherlockFragment;
import com.foound.widget.AmazingListView;
import com.mapsaurus.paneslayout.FragmentLauncher;
import com.mapsaurus.paneslayout.PanesActivity;
import com.nasageek.utexasutilities.AsyncTask;
import com.nasageek.utexasutilities.MyPair;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.Utility;
import com.nasageek.utexasutilities.adapters.BBClassAdapter;
import com.nasageek.utexasutilities.model.BBClass;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlackboardCourseListFragment extends SherlockFragment {

    private OkHttpClient httpclient;
    private LinearLayout bb_pb_ll;
    private TextView bbetv;
    private LinearLayout bbell;

    private AmazingListView bblv;
    private ArrayList<BBClass> classList;
    private List<MyPair<String, List<BBClass>>> classSectionList;
    private fetchClassesTask fetch;
    // private ArrayList<ParcelableMyPair<String, ArrayList<BBClass>>> classes;
    private BBClassAdapter classAdapter;

    public BlackboardCourseListFragment() {
    }

    public static BlackboardCourseListFragment newInstance(String title) {
        BlackboardCourseListFragment f = new BlackboardCourseListFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        f.setArguments(args);

        return f;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        classList = new ArrayList<BBClass>();
        if (savedInstanceState == null) {
            classSectionList = new ArrayList<MyPair<String, List<BBClass>>>();
        } else {
            classSectionList = (ArrayList<MyPair<String, List<BBClass>>>) savedInstanceState
                    .getSerializable("classSectionList");
        }

        httpclient = new OkHttpClient();
        classAdapter = new BBClassAdapter(getActivity(), classSectionList);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vg = inflater.inflate(R.layout.blackboard_courselist_fragment, container, false);

        bb_pb_ll = (LinearLayout) vg.findViewById(R.id.blackboard_progressbar_ll);
        bblv = (AmazingListView) vg.findViewById(R.id.blackboard_class_listview);

        bbell = (LinearLayout) vg.findViewById(R.id.blackboard_error);
        bbetv = (TextView) vg.findViewById(R.id.tv_failure);

        bblv.setAdapter(classAdapter);
        bblv.setPinnedHeaderView(getActivity().getLayoutInflater().inflate(
                R.layout.menu_header_item_view, bblv, false));
        bblv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO: figure out course id stuff here
                // Intent classLaunch = new
                // Intent(getString(R.string.coursemap_intent), null,
                // getActivity(), CourseMapActivity.class);
                BBClass bbclass = (BBClass) (parent.getItemAtPosition(position));
                /*
                 * classLaunch.putExtra("courseid", bbclass.getBbid());
                 * classLaunch.setData(Uri.parse((bbclass).getBbid()));
                 * classLaunch.putExtra("folderName", "Course Map");
                 * classLaunch.putExtra("coursename", bbclass.getCourseId());
                 * classLaunch.putExtra("showViewInWeb", true);
                 * startActivity(classLaunch);
                 */
                FragmentActivity act = getActivity();
                Fragment topFragment = null;
                if (act != null && act instanceof PanesActivity) {
                    topFragment = ((PanesActivity) act).getTopFragment();
                    // we're on a tablet, PanesActivity acts a bit odd with them
                    if (((PanesActivity) act).getMenuFragment() == topFragment) {
                        topFragment = null;
                    }
                }
                // don't re-add the current displayed course, instead just show
                // it
                if (act != null && act instanceof FragmentLauncher) {
                    if (topFragment == null
                            || (topFragment != null
                                    && topFragment instanceof BlackboardFragment
                                    && (!((BlackboardFragment) topFragment).getBbid().equals(
                                            bbclass.getBbid())) || ((BlackboardFragment) topFragment)
                                        .isFromDashboard())) {
                        ((FragmentLauncher) act).addFragment(BlackboardCourseListFragment.this
                                .getParentFragment(), BlackboardCourseMapFragment.newInstance(
                                getString(R.string.coursemap_intent), null, bbclass.getBbid(),
                                bbclass.getCourseId(), "Course Map", "", -1, false));

                    } else if (act instanceof PanesActivity) {
                        ((PanesActivity) act).showContent();
                    }
                }
            }
        });

        // where to callll, also, helper? - helper for what? shit I don't
        // remember writing this...
        if (classSectionList.size() == 0) {
            fetch = new fetchClassesTask(httpclient);
            Utility.parallelExecute(fetch);
        }
        return vg;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("classSectionList", (Serializable) classSectionList);
    }

    private class fetchClassesTask extends
            AsyncTask<Object, Void, ArrayList<MyPair<String, List<BBClass>>>> {
        private OkHttpClient client;
        private String errorMsg;

        public fetchClassesTask(OkHttpClient client) {
            this.client = client;
        }

        @Override
        protected void onPreExecute() {
            bb_pb_ll.setVisibility(View.VISIBLE);
            bbell.setVisibility(View.GONE);
            bblv.setVisibility(View.GONE);
        }

        @Override
        protected ArrayList<MyPair<String, List<BBClass>>> doInBackground(Object... params) {
            String reqUrl = BlackboardFragment.BLACKBOARD_DOMAIN
                    + "/webapps/Bb-mobile-BBLEARN/enrollments?course_type=COURSE";
            Request request = new Request.Builder()
                    .url(reqUrl)
                    .build();
            String pagedata = "";

            try {
                Response response = client.newCall(request).execute();
                pagedata = response.body().string();
            } catch (IOException e) {
                errorMsg = "UTilities could not fetch the Blackboard course list";
                e.printStackTrace();
                cancel(true);
                return null;
            }

            Pattern class_pattern = Pattern
                    .compile("bbid=\"(.*?)\" name=\"(.*?)\" courseid=\"(.*?)\"");
            Matcher class_matcher = class_pattern.matcher(pagedata);

            while (class_matcher.find()) {
                classList.add(new BBClass(class_matcher.group(2).replace("&amp;", "&"),
                        class_matcher.group(1).replace("&amp;", "&"), class_matcher.group(3)));
            }
            // section the class list by semester
            String currentSemester = "";
            ArrayList<BBClass> currentSemesterList = null;
            ArrayList<MyPair<String, List<BBClass>>> sectionedClassList =
                    new ArrayList<MyPair<String, List<BBClass>>>();
            for (int i = 0; i < classList.size(); i++) {
                // first course always starts a new semester
                if (i == 0) {
                    currentSemester = classList.get(i).getSemester();
                    currentSemesterList = new ArrayList<BBClass>();
                    currentSemesterList.add(classList.get(i));
                }
                // hit a new semester, finalize current semester and init the new one
                else if (!classList.get(i).getSemester().equals(currentSemester)) {
                    sectionedClassList.add(new MyPair<String, List<BBClass>>(currentSemester,
                            currentSemesterList));

                    currentSemester = classList.get(i).getSemester();
                    currentSemesterList = new ArrayList<BBClass>();
                    currentSemesterList.add(classList.get(i));
                }
                // otherwise just add to the current semester
                else {
                    currentSemesterList.add(classList.get(i));
                }
                // add final semester once we're through
                if (i == classList.size() - 1) {
                    sectionedClassList.add(new MyPair<String, List<BBClass>>(currentSemester,
                            currentSemesterList));
                }
            }
            Collections.reverse(sectionedClassList);
            return sectionedClassList;
        }

        @Override
        protected void onPostExecute(ArrayList<MyPair<String, List<BBClass>>> result) {
            classSectionList.addAll(result);
            classAdapter.notifyDataSetChanged();

            bb_pb_ll.setVisibility(View.GONE);
            bbell.setVisibility(View.GONE);
            bblv.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onCancelled() {
            bbetv.setText(errorMsg);
            bb_pb_ll.setVisibility(View.GONE);
            bbell.setVisibility(View.VISIBLE);
            bblv.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fetch != null) {
            fetch.cancel(true);
        }
    }
}

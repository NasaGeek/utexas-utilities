
package com.nasageek.utexasutilities.fragments;

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
import android.widget.Toast;

import com.foound.widget.AmazingListView;
import com.mapsaurus.paneslayout.FragmentLauncher;
import com.mapsaurus.paneslayout.PanesActivity;
import com.nasageek.utexasutilities.AsyncTask;
import com.nasageek.utexasutilities.MyPair;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.UTilitiesApplication;
import com.nasageek.utexasutilities.Utility;
import com.nasageek.utexasutilities.adapters.BBClassAdapter;
import com.nasageek.utexasutilities.fragments.canvas.CanvasCourseMapFragment;
import com.nasageek.utexasutilities.model.BBCourse;
import com.nasageek.utexasutilities.model.Course;
import com.nasageek.utexasutilities.model.canvas.CanvasCourse;
import com.nasageek.utexasutilities.requests.CanvasCourseListRequest;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit.RetrofitError;

import static com.nasageek.utexasutilities.UTilitiesApplication.CANVAS_AUTH_COOKIE_KEY;

public class BlackboardCourseListFragment extends BaseSpiceFragment {

    private OkHttpClient httpclient;
    private LinearLayout bb_pb_ll;
    private TextView bbetv;
    private LinearLayout bbell;

    private AmazingListView bblv;
    private ArrayList<Course> classList;
    private List<MyPair<String, List<Course>>> classSectionList;
    private fetchClassesTask fetch;
    // private ArrayList<ParcelableMyPair<String, ArrayList<BBClass>>> classes;
    private BBClassAdapter classAdapter;
    private CanvasCourseListRequest canvasCourseListRequest;

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
        
        classList = new ArrayList<Course>();
        canvasCourseListRequest = new CanvasCourseListRequest(
                ((UTilitiesApplication) getActivity().getApplication()).getCanvasAuthCookieVal());
        if (savedInstanceState == null) {
            classSectionList = new ArrayList<MyPair<String, List<Course>>>();
        } else {
            classSectionList = (ArrayList<MyPair<String, List<Course>>>) savedInstanceState
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
                Course course = (Course) (parent.getItemAtPosition(position));
                String type = course.getType();
                FragmentActivity act = getActivity();
                Fragment topFragment = null;
                if (act != null && act instanceof PanesActivity) {
                    topFragment = ((PanesActivity) act).getTopFragment();
                    // we're on a tablet, PanesActivity acts a bit odd with them
                    if (((PanesActivity) act).getMenuFragment() == topFragment) {
                        topFragment = null;
                    }
                }
                
                if (act != null && act instanceof FragmentLauncher) {
                    if (type.equals("blackboard")) {
                        BBCourse bbclass = (BBCourse) course;

                        ((FragmentLauncher) act).addFragment(BlackboardCourseListFragment.this
                                .getParentFragment(), BlackboardCourseMapFragment.newInstance(
                                getString(R.string.coursemap_intent), null, bbclass.getId(),
                                bbclass.getCourseCode(), "Course Map", "", -1, false));
                    } else if (type.equals("canvas")) {
                        CanvasCourse ccourse = (CanvasCourse) course;
                        // Launch canvas coursemap
                        ((FragmentLauncher) act).addFragment(
                                BlackboardCourseListFragment.this.getParentFragment(),
                                CanvasCourseMapFragment.newInstance(ccourse.getId(),
                                        ccourse.getName(), ccourse.getCourseCode()));
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

    public final class CanvasCourseListRequestListener implements
            RequestListener<CanvasCourse.List> {

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            Toast.makeText(getSherlockActivity(), "failure", Toast.LENGTH_SHORT).show();
            // if request was unauthorized, token's probably bad
            if (((RetrofitError) spiceException.getCause()).getResponse().getStatus() == 401) {
                ((UTilitiesApplication) getActivity().getApplication())
                        .getAuthCookie(CANVAS_AUTH_COOKIE_KEY).logout();
            }
            bb_pb_ll.setVisibility(View.GONE);
            bbell.setVisibility(View.GONE);
            bblv.setVisibility(View.VISIBLE);
        }

        @Override
        public void onRequestSuccess(final CanvasCourse.List result) {
            int i = 0;

            Collections.reverse(result);
            // classSectionList guaranteed to be populated, this supposes the
            // two lists are ordered the same
            // in this case most recent to least
            for (int j = 0; j < classSectionList.size(); j++) {
                while (i < result.size()
                        && result.get(i).getTermName().equals(classSectionList.get(j).first)) {
                    classSectionList.get(j).second.add(result.get(i));
                    i++;
                }
                if (j == classSectionList.size() - 1) {
                    classSectionList.get(j).second.addAll(result.subList(i, result.size()));
                }
            }
            classAdapter.notifyDataSetChanged();
            bb_pb_ll.setVisibility(View.GONE);
            bbell.setVisibility(View.GONE);
            bblv.setVisibility(View.VISIBLE);
        }
    }

    private class fetchClassesTask extends
            AsyncTask<Object, Void, ArrayList<MyPair<String, List<Course>>>> {
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
        protected ArrayList<MyPair<String, List<Course>>> doInBackground(Object... params) {
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
                classList.add(new BBCourse(class_matcher.group(2).replace("&amp;", "&"),
                        class_matcher.group(1).replace("&amp;", "&"), class_matcher.group(3)));
            }
            // section the class list by semester
            String currentSemester = "";
            ArrayList<Course> currentSemesterList = null;
            ArrayList<MyPair<String, List<Course>>> sectionedClassList =
                    new ArrayList<MyPair<String, List<Course>>>();
            for (int i = 0; i < classList.size(); i++) {
                // first course always starts a new semester
                if (i == 0) {
                    currentSemester = classList.get(i).getTermName();
                    currentSemesterList = new ArrayList<Course>();
                    currentSemesterList.add(classList.get(i));
                }
                // hit a new semester, finalize current semester and init the new one
                else if (!classList.get(i).getTermName().equals(currentSemester)) {
                    sectionedClassList.add(new MyPair<String, List<Course>>(currentSemester,
                            currentSemesterList));

                    currentSemester = classList.get(i).getTermName();
                    currentSemesterList = new ArrayList<Course>();
                    currentSemesterList.add(classList.get(i));
                }
                // otherwise just add to the current semester
                else {
                    currentSemesterList.add(classList.get(i));
                }
                // add final semester once we're through
                if (i == classList.size() - 1) {
                    sectionedClassList.add(new MyPair<String, List<Course>>(currentSemester,
                            currentSemesterList));
                }
            }
            Collections.reverse(sectionedClassList);
            return sectionedClassList;
        }

        @Override
        protected void onPostExecute(ArrayList<MyPair<String, List<Course>>> result) {
            classSectionList.addAll(result);
            classAdapter.notifyDataSetChanged();
            // TODO: learn to thread properly :(
            getSpiceManager().execute(canvasCourseListRequest, "courses",
                    DurationInMillis.ONE_MINUTE * 5, new CanvasCourseListRequestListener());

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

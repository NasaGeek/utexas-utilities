
package com.nasageek.utexasutilities.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

import com.commonsware.cwac.security.RuntimePermissionUtils;
import com.nasageek.utexasutilities.AnalyticsHandler;
import com.nasageek.utexasutilities.MyBus;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.TaggedAsyncTask;
import com.nasageek.utexasutilities.UTilitiesApplication;
import com.nasageek.utexasutilities.Utility;
import com.nasageek.utexasutilities.WrappingSlidingDrawer;
import com.nasageek.utexasutilities.activities.CampusMapActivity;
import com.nasageek.utexasutilities.activities.ScheduleActivity;
import com.nasageek.utexasutilities.adapters.ScheduleClassAdapter;
import com.nasageek.utexasutilities.model.Classtime;
import com.nasageek.utexasutilities.model.LoadFailedEvent;
import com.nasageek.utexasutilities.model.UTClass;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("deprecation")
public class CourseScheduleFragment extends ScheduleFragment implements ActionModeFragment,
        SlidingDrawer.OnDrawerCloseListener, SlidingDrawer.OnDrawerOpenListener,
        AdapterView.OnItemClickListener, ActionMode.Callback {

    public static final int TRANSLUCENT_GRAY = 0x99F0F0F0;

    private static final int REQUEST_CALENDAR_PERMISSION = 1;
    private RuntimePermissionUtils runtimePermissions;

    private GridView scheduleGridView;
    private WrappingSlidingDrawer slidingDrawer;
    private ScheduleClassAdapter adapter;

    private Menu mMenu;
    private LinearLayout progressLayout;
    private ImageView classInfoImageView;
    private TextView classInfoTextView;
    private TextView errorTextView;
    private LinearLayout errorLayout;
    private LinearLayout scheduleLayout;

    private ArrayList<UTClass> classList = new ArrayList<>();
    private Classtime currentClasstime;
    private int lastClickedPosition = -1;
    private ParseTask fetch;

    private ActionMode mode;

    private ScheduleActivity parentAct;
    private String reqUrl;
    private Boolean initialFragment;
    private String TASK_TAG;
    private final UTilitiesApplication mApp = UTilitiesApplication.getInstance();

    public static CourseScheduleFragment newInstance(boolean initialFragment, String url) {
        CourseScheduleFragment csf = new CourseScheduleFragment();

        Bundle args = new Bundle();
        args.putBoolean("initialFragment", initialFragment);
        args.putString("url", url);
        csf.setArguments(args);

        return csf;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vg = inflater.inflate(R.layout.course_schedule_fragment_layout, container, false);
        slidingDrawer = (WrappingSlidingDrawer) vg.findViewById(R.id.drawer);
        classInfoImageView = (ImageView) vg.findViewById(R.id.class_info_color);
        classInfoTextView = (TextView) vg.findViewById(R.id.class_info_text);
        errorLayout = (LinearLayout) vg.findViewById(R.id.schedule_error);
        errorTextView = (TextView) vg.findViewById(R.id.tv_failure);
        progressLayout = (LinearLayout) vg.findViewById(R.id.schedule_progressbar_ll);
        scheduleLayout = (LinearLayout) vg.findViewById(R.id.schedule_ll);
        scheduleGridView = (GridView) vg.findViewById(R.id.scheduleview);
        if (savedInstanceState != null) {
            switch (loadStatus) {
                case NOT_STARTED:
                    // defaults should suffice
                    break;
                case LOADING:
                    prepareToLoad();
                    break;
                case SUCCEEDED:
                    progressLayout.setVisibility(View.GONE);
                    errorLayout.setVisibility(View.GONE);
                    scheduleLayout.setVisibility(View.VISIBLE);
                    break;
                case FAILED:
                    loadFailed(new LoadFailedEvent(TASK_TAG, errorTextView.getText()));
                    break;
            }
        }
        slidingDrawer.setOnDrawerCloseListener(this);
        slidingDrawer.setOnDrawerOpenListener(this);
        scheduleGridView.setOnItemClickListener(this);

        if (loadStatus == LoadStatus.NOT_STARTED && mApp.getCachedTask(TASK_TAG) == null) {
            loadData();
        } else {
            setupAdapter();
            if (lastClickedPosition >= 0) {
                scheduleGridView.performItemClick(null, lastClickedPosition, 0);
                //noinspection ResourceType
                slidingDrawer.setVisibility(savedInstanceState.getInt("drawerVisibility"));
                boolean slidingDrawerOpened = savedInstanceState.getBoolean("drawerOpened");
                if (!slidingDrawerOpened) {
                    slidingDrawer.close();
                }
                boolean actionModeOpen = savedInstanceState.getBoolean("actionModeOpen");
                if (!actionModeOpen) {
                    if (mode != null) {
                        mode.finish();
                    }
                }
            }
        }
        return vg;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        parentAct = (ScheduleActivity) getActivity();
        runtimePermissions = new RuntimePermissionUtils(getActivity());
        reqUrl = getArguments().getString("url");
        initialFragment = getArguments().getBoolean("initialFragment");
        TASK_TAG = getClass().getSimpleName() + reqUrl;
        if (savedInstanceState != null) {
            classList = savedInstanceState.getParcelableArrayList("classList");
            lastClickedPosition = savedInstanceState.getInt("lastClickedPosition");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        MyBus.getInstance().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.updateTime();
        }
        if (scheduleGridView != null) {
            scheduleGridView.invalidateViews();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.mMenu = menu;
        inflater.inflate(R.menu.schedule_menu, menu);
    }

    @Override
    public void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putParcelableArrayList("classList", classList);
        out.putInt("lastClickedPosition", lastClickedPosition);

        out.putInt("drawerVisibility", slidingDrawer.getVisibility());
        out.putBoolean("drawerOpened", slidingDrawer.isOpened());
        out.putBoolean("actionModeOpen", mode != null);
    }

    @Override
    public void onStop() {
        MyBus.getInstance().unregister(this);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (fetch != null) {
            fetch.cancel(true);
        }
        super.onDestroy();
    }

    private void setupAdapter() {
        adapter = new ScheduleClassAdapter(parentAct, classList);
        scheduleGridView.setAdapter(adapter);
    }

    private void loadData() {
        loadStatus = LoadStatus.LOADING;
        prepareToLoad();
        fetch = new ParseTask(TASK_TAG, reqUrl, initialFragment);
        mApp.cacheTask(TASK_TAG, fetch);
        Utility.parallelExecute(fetch, false);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        boolean scheduleHasClasses = (classList != null && classList.size() > 0);
        menu.findItem(R.id.map_all_classes).setEnabled(scheduleHasClasses);
        menu.findItem(R.id.export_schedule).setEnabled(scheduleHasClasses);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.map_all_classes:
                // check to see if we're done loading the schedules (the
                // ScheduleClassAdapter is initialized in onPostExecute)
                if (adapter != null) {
                    // populate an array with the buildings IDs of all of the user's classtimes
                    ArrayList<String> buildings = new ArrayList<>();

                    for (UTClass clz : classList) {
                        for (Classtime clt : clz.getClassTimes()) {
                            if (!buildings.contains(clt.getBuilding().getId())) {
                                buildings.add(clt.getBuilding().getId());
                            }
                        }
                    }
                    AnalyticsHandler.trackMapAllClassesEvent();
                    Intent map = new Intent(getString(R.string.building_intent), null, parentAct, CampusMapActivity.class);
                    map.putStringArrayListExtra("buildings", buildings);
                    startActivity(map);
                    break;
                }
            case R.id.export_schedule:
                // check to see if we're done loading the schedules (the
                // ScheduleClassAdapter is initialized in onPostExecute)
                if (adapter != null) {
                    if (runtimePermissions.hasPermission(Manifest.permission.READ_CALENDAR)) {
                        DoubleDatePickerDialogFragment.newInstance(classList)
                                .show(getActivity().getSupportFragmentManager(), "double_date_picker");
                    } else {
                        requestPermissions(new String[]{Manifest.permission.READ_CALENDAR},
                                REQUEST_CALENDAR_PERMISSION);
                    }
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CALENDAR_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // work around crash when showing fragment in onRequestPermissionsResult
                    // https://code.google.com/p/android/issues/detail?id=190966
                    new Handler().postDelayed(() -> DoubleDatePickerDialogFragment.newInstance(classList)
                            .show(getActivity().getSupportFragmentManager(), "double_date_picker"), 200);
                }
                break;
        }
    }

    @Override
    public ActionMode getActionMode() {
        return mode;
    }

    @Override
    public void onDrawerClosed() {
        ((ImageView) (slidingDrawer.getHandle())).setImageResource(R.drawable.ic_expand_half);
    }

    @Override
    public void onDrawerOpened() {
        ((ImageView) (slidingDrawer.getHandle())).setImageResource(R.drawable.ic_collapse_half);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        slidingDrawer.close();
        currentClasstime = (Classtime) parent.getItemAtPosition(position);
        lastClickedPosition = position;

        if (currentClasstime != null) {
            mode = parentAct.startSupportActionMode(this);

            String text = " ";
            text += currentClasstime.getCourseId() + " - " + currentClasstime.getName() + " ";

            String daytext = "\n\t";
            String building = currentClasstime.getBuilding().getId() + " "
                    + currentClasstime.getBuilding().getRoom();
            String unique = currentClasstime.getUnique();

            String time = currentClasstime.getStartTime();
            String end = currentClasstime.getEndTime();

            if (currentClasstime.getDay() == 'H') {
                daytext += "TH";
            } else {
                daytext += currentClasstime.getDay();
            }

            // TODO: stringbuilder
            text += (daytext + " from " + time + "-" + end + " in " + building) + "\n";

            text += "\tUnique: " + unique + "\n";

            classInfoImageView.setBackgroundColor(Color.parseColor("#" + currentClasstime.getColor()));
            classInfoImageView.setMinimumHeight(10);
            classInfoImageView.setMinimumWidth(10);

            classInfoTextView.setTextColor(Color.BLACK);
            classInfoTextView.setTextSize(14f);
            classInfoTextView.setBackgroundColor(TRANSLUCENT_GRAY);
            classInfoTextView.setText(text);

            slidingDrawer.setVisibility(View.VISIBLE);
            slidingDrawer.open();
        }
        // they clicked an empty cell
        else {
            if (mode != null) {
                mode.finish();
            }
            scheduleGridView.clearChoices();
            slidingDrawer.setVisibility(View.INVISIBLE);
        }
    }

    static class ParseTask extends TaggedAsyncTask<Boolean, String, List<UTClass>> {
        private OkHttpClient client = UTilitiesApplication.getInstance().getHttpClient();
        private String errorMsg;
        private String reqUrl;
        private boolean classParseIssue = false;
        private boolean initialFragment;
        private final String[] colors = {
                "488ab0", "00b060", "b56eb3", "94c6ff", "81b941", "ff866e", "ffad46", "ffe45e"
        };

        public ParseTask(String tag, String reqUrl, boolean initialFragment) {
            super(tag);
            this.reqUrl = reqUrl;
            this.initialFragment = initialFragment;
        }

        @Override
        protected void onProgressUpdate(String... params) {
            MyBus.getInstance().post(new NewSemesterEvent(getTag(), params[0].trim(), params[1]));
        }

        @Override
        protected List<UTClass> doInBackground(Boolean... params) {
            Boolean recursing = params[0];
            List<UTClass> classes = new ArrayList<>();
            Request request = new Request.Builder()
                    .url(reqUrl)
                    .build();
            String pagedata = "";

            try {
                Response response = client.newCall(request).execute();
                pagedata = response.body().string();
            } catch (IOException e) {
                errorMsg = "UTilities could not fetch your class listing";
                e.printStackTrace();
                cancel(true);
                return null;
            }

            // now parse the Class Listing data
//
//            // did we hit the login screen?
            if (pagedata.contains("<title>UT EID Login</title>")) {
                errorMsg = "You've been logged out of UTDirect, back out and log in again.";
                UTilitiesApplication mApp = UTilitiesApplication.getInstance();
//                if (!recursing) {
//                    try {
//                        mApp.getAuthCookie(UTD_AUTH_COOKIE_KEY).logout();
//                        mApp.getAuthCookie(UTD_AUTH_COOKIE_KEY).login();
//                    } catch (IOException e) {
//                        errorMsg = "UTilities could not fetch your class listing";
//                        cancel(true);
//                        e.printStackTrace();
//                        return null;
//                    } catch (TempLoginException tle) {
//                        /*
//                        ooooh boy is this lazy. I'd rather not init SharedPreferences here
//                        to check if persistent login is on, so we'll just catch the exception
//                         */
//                        Intent login = new Intent(mApp, LoginActivity.class);
//                        login.putExtra("activity", parentAct.getIntent().getComponent()
//                                .getClassName());
//                        login.putExtra("service", 'u');
//                        mApp.startActivity(login);
//                        parentAct.finish();
//                        errorMsg = "Session expired, please log in again";
//                        cancel(true);
//                        return null;
//                    }
//                    return doInBackground(true);
//                } else {
                    mApp.logoutAll();
//                }
                cancel(true);
                return null;
            }
            Pattern semSelectPattern = Pattern.compile("<select  name=\"sem\">.*</select>",
                    Pattern.DOTALL);
            Matcher semSelectMatcher = semSelectPattern.matcher(pagedata);

            if (semSelectMatcher.find() && initialFragment) {
                Pattern semesterPattern = Pattern.compile(
                        "<option.*?value=\"(\\d*)\"\\s*>([\\w\\s]*?)</option>", Pattern.DOTALL);
                Matcher semesterMatcher = semesterPattern.matcher(semSelectMatcher.group());
                while (semesterMatcher.find()) {
                    // the "current" semester that has been downloaded is the one with the
                    // "selected" attribute, so we don't want to load it again
                    if (!semesterMatcher.group(0).contains("selected=\"selected\"")) {
                        publishProgress(semesterMatcher.group(2), semesterMatcher.group(1));
                    }
                }
            }

            Pattern scheduleTablePattern = Pattern.compile("<table.*</table>", Pattern.DOTALL);
            Matcher scheduletableMatcher = scheduleTablePattern.matcher(pagedata);

            if (scheduletableMatcher.find()) {
                pagedata = scheduletableMatcher.group(0);
            } else {
                // if no <table>, user probably isn't enrolled for semester
                errorMsg = "You aren't enrolled for this semester.";
                cancel(true);
                return null;
            }
            Pattern classPattern = Pattern.compile("<tr  ?v.*?</tr>", Pattern.DOTALL);
            Matcher classMatcher = classPattern.matcher(pagedata);
            int colorIndex = 0;

            while (classMatcher.find()) {
                String classContent = classMatcher.group();

                String uniqueid = "", classid = "", classname = "";
                String[] buildings, rooms, days, times;
                boolean dropped = false;

                Pattern classAttPattern = Pattern.compile("<td ?>(.*?)</td>", Pattern.DOTALL);
                Matcher classAttMatcher = classAttPattern.matcher(classContent);
                if (classAttMatcher.find()) {
                    uniqueid = classAttMatcher.group(1);
                } else {
                    classParseIssue = true;
                    continue;
                }
                if (classAttMatcher.find()) {
                    classid = classAttMatcher.group(1);
                } else {
                    classParseIssue = true;
                    continue;
                }
                if (classAttMatcher.find()) {
                    classname = classAttMatcher.group(1);
                } else {
                    classParseIssue = true;
                    continue;
                }
                if (classAttMatcher.find()) {
                    buildings = classAttMatcher.group(1).split("<br( /)?>");
                    for (int i = 0; i < buildings.length; i++) {
                        buildings[i] = buildings[i].replaceAll("<.*?>", "").trim();
                    }
                } else {
                    classParseIssue = true;
                    continue;
                }
                if (classAttMatcher.find()) {
                    rooms = classAttMatcher.group(1).split("<br( /)?>");
                    for (int i = 0; i < rooms.length; i++) {
                        rooms[i] = rooms[i].trim();
                    }
                } else {
                    classParseIssue = true;
                    continue;
                }
                if (classAttMatcher.find()) {
                    days = classAttMatcher.group(1).split("<br( /)?>");
                    // Thursday represented by H so I can treat all days as single characters
                    for (int a = 0; a < days.length; a++) {
                        days[a] = days[a].replaceAll("TH", "H").trim();
                    }
                } else {
                    classParseIssue = true;
                    continue;
                }
                if (classAttMatcher.find()) {
                    times = classAttMatcher.group(1).replaceAll("- ", "-").split("<br( /)?>");
                    for (int i = 0; i < times.length; i++) {
                        times[i] = times[i].trim();
                    }
                } else {
                    classParseIssue = true;
                    continue;
                }
                if (classAttMatcher.find()) {
                    String remark = classAttMatcher.group(1);
                    if (remark.contains("Dropped")) {
                        dropped = true;
                    }
                } else {
                    classParseIssue = true;
                    continue;
                }
                if (!dropped) {
                    classes.add(new UTClass(uniqueid, classid, classname, buildings, rooms, days,
                            times, colors[colorIndex]));
                    colorIndex = (colorIndex == colors.length - 1) ? 0 : colorIndex + 1;
                }
            }
            return classes;
        }

        @Override
        protected void onPostExecute(List<UTClass> classes) {
            MyBus.getInstance().post(new LoadSucceededEvent(getTag(), classes, classParseIssue));
            UTilitiesApplication.getInstance().removeCachedTask(getTag());
        }

        @Override
        protected void onCancelled() {
            MyBus.getInstance().post(new LoadFailedEvent(getTag(), errorMsg));
            UTilitiesApplication.getInstance().removeCachedTask(getTag());
        }
    }

    private void prepareToLoad() {
        progressLayout.setVisibility(View.VISIBLE);
        scheduleLayout.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);
    }

    @Subscribe
    public void onSemesterFound(NewSemesterEvent event) {
        if (TASK_TAG.equals(event.tag)) {
            parentAct.addCourseSchedule(false, event.name, event.semId);
        }
    }

    @Subscribe
    public void loadSucceeded(LoadSucceededEvent event) {
        if (TASK_TAG.equals(event.tag)) {
            loadStatus = LoadStatus.SUCCEEDED;
            progressLayout.setVisibility(View.GONE);
            classList.addAll(event.classes);

            if (event.classes.size() == 0) {
                loadFailed(new LoadFailedEvent(event.tag,
                        "UTilities could not find any of your classes"));
                return;
            } else {
                setupAdapter();
                // scrolls down to the user's earliest class
                scheduleGridView.setSelection(adapter.getEarliestClassPos());
                scheduleLayout.setVisibility(View.VISIBLE);
                setMenuItemsEnabled(true);
                if (!parentAct.isFinishing()) {
                    Toast.makeText(parentAct, "Tap a class to see its information.",
                            Toast.LENGTH_SHORT).show();
                }
            }
            if (event.parseIssue) {
                Toast.makeText(
                        parentAct,
                        "One or more classes could not be parsed correctly, try emailing the dev ;)",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Subscribe
    public void loadFailed(LoadFailedEvent event) {
        if (TASK_TAG.equals(event.tag)) {
            loadStatus = LoadStatus.FAILED;
            errorTextView.setText(event.errorMessage);
            errorLayout.setVisibility(View.VISIBLE);
            progressLayout.setVisibility(View.GONE);
            scheduleLayout.setVisibility(View.GONE);
            setMenuItemsEnabled(false);
        }
    }

    private void setMenuItemsEnabled(boolean enable) {
        if (mMenu != null) {
            if (mMenu.findItem(R.id.map_all_classes) != null) {
                mMenu.findItem(R.id.map_all_classes).setEnabled(enable);
            }
            if (mMenu.findItem(R.id.export_schedule) != null) {
                mMenu.findItem(R.id.export_schedule).setEnabled(enable);
            }
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.setTitle("Class Info");
        mode.setSubtitle(currentClasstime.getCourseId());
        MenuInflater inflater = parentAct.getMenuInflater();
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
                Intent map = new Intent(getString(R.string.building_intent), null, parentAct,
                        CampusMapActivity.class);
                building.add(currentClasstime.getBuilding().getId());
                map.putStringArrayListExtra("buildings", building);
                startActivity(map);
                break;
        }
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        this.mode = null;
    }

    static class NewSemesterEvent {
        public String tag;
        public String name;
        public String semId;

        public NewSemesterEvent(String tag, String name, String semId) {
            this.tag = tag;
            this.name = name;
            this.semId = semId;
        }
    }

    static class LoadSucceededEvent {
        public String tag;
        public List<UTClass> classes;
        public boolean parseIssue;

        public LoadSucceededEvent(String tag, List<UTClass> classes, boolean parseIssue) {
            this.tag = tag;
            this.classes = classes;
            this.parseIssue = parseIssue;
        }
    }
}

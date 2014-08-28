
package com.nasageek.utexasutilities.fragments;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.nasageek.utexasutilities.AsyncTask;
import com.nasageek.utexasutilities.ConnectionHelper;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.WrappingSlidingDrawer;
import com.nasageek.utexasutilities.activities.CampusMapActivity;
import com.nasageek.utexasutilities.activities.ScheduleActivity;
import com.nasageek.utexasutilities.adapters.ScheduleClassAdapter;
import com.nasageek.utexasutilities.model.Classtime;
import com.nasageek.utexasutilities.model.UTClass;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

@SuppressWarnings("deprecation")
public class CourseScheduleFragment extends SherlockFragment implements ActionModeFragment,
        SlidingDrawer.OnDrawerCloseListener, SlidingDrawer.OnDrawerOpenListener,
        AdapterView.OnItemClickListener {

    public static final int TRANSLUCENT_GRAY = 0x99F0F0F0;

    private GridView scheduleGridView;
    private WrappingSlidingDrawer slidingDrawer;
    private ScheduleClassAdapter adapter;

    private final String[] colors = {
            "488ab0", "00b060", "b56eb3", "94c6ff", "81b941", "ff866e", "ffad46", "ffe45e"
    };

    private Menu mMenu;
    private LinearLayout progress;
    private LinearLayout dayList;
    private ImageView classInfoImageView;
    private TextView classInfoTextView;
    private TextView noCoursesTextView;
    private TextView errorTextView;
    private LinearLayout errorLayout;

    private ArrayList<UTClass> classList;
    private Classtime currentClasstime;
    private parseTask fetch;

    private ActionMode mode;

    private SherlockFragmentActivity parentAct;
    private String semId;

    public static CourseScheduleFragment newInstance(boolean initialFragment, String title, String id) {
        CourseScheduleFragment csf = new CourseScheduleFragment();

        Bundle args = new Bundle();
        args.putBoolean("initialFragment", initialFragment);
        args.putString("title", title);
        args.putString("semId", id);
        csf.setArguments(args);

        return csf;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vg = inflater.inflate(R.layout.course_schedule_fragment_layout, container, false);

        slidingDrawer = (WrappingSlidingDrawer) vg.findViewById(R.id.drawer);

        classInfoImageView = (ImageView) vg.findViewById(R.id.class_info_color);
        classInfoTextView = (TextView) vg.findViewById(R.id.class_info_text);
        errorLayout = (LinearLayout) vg.findViewById(R.id.schedule_error);
        errorTextView = (TextView) vg.findViewById(R.id.tv_failure);

        progress = (LinearLayout) vg.findViewById(R.id.schedule_progressbar_ll);
        noCoursesTextView = (TextView) vg.findViewById(R.id.no_courses);
        scheduleGridView = (GridView) vg.findViewById(R.id.scheduleview);
        dayList = (LinearLayout) vg.findViewById(R.id.daylist);

        /*
         * if(savedInstanceState != null) classList =
         * savedInstanceState.getParcelableArrayList("classList");
         */

        slidingDrawer.setOnDrawerCloseListener(this);
        slidingDrawer.setOnDrawerOpenListener(this);
        slidingDrawer.setVisibility(View.INVISIBLE);

        DefaultHttpClient client = ConnectionHelper.getThreadSafeClient();
        fetch = new parseTask(client);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            fetch.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            fetch.execute();
        }

        return vg;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        parentAct = this.getSherlockActivity();
        semId = getArguments().getString("semId");
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
        menu.removeItem(R.id.map_all_classes);
        inflater.inflate(R.menu.schedule_menu, menu);
    }

    /*
     * @Override public void onSaveInstanceState(Bundle out) {
     * super.onSaveInstanceState(out); out.putParcelableArrayList("classList",
     * classList); }
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fetch != null) {
            fetch.cancel(true);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (classList == null || classList.size() == 0) {
            menu.findItem(R.id.map_all_classes).setEnabled(false);
            MenuItem exportSchedule;
            // <4.0 can't export schedule, so MenuItem will be null
            if ((exportSchedule = menu.findItem(R.id.export_schedule)) != null) {
                exportSchedule.setEnabled(false);
            }
        } else {
            menu.findItem(R.id.map_all_classes).setEnabled(true);
            MenuItem exportSchedule;
            if ((exportSchedule = menu.findItem(R.id.export_schedule)) != null) {
                exportSchedule.setEnabled(true);
            }
        }
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
                    ArrayList<String> buildings = new ArrayList<String>();

                    for (UTClass clz : classList) {
                        for (Classtime clt : clz.getClassTimes()) {
                            if (!buildings.contains(clt.getBuilding().getId())) {
                                buildings.add(clt.getBuilding().getId());
                            }
                        }
                    }

                    Intent map = new Intent(getString(R.string.building_intent), null, parentAct, CampusMapActivity.class);
                    map.putStringArrayListExtra("buildings", buildings);
                    startActivity(map);
                    break;
                }
            case R.id.export_schedule:
                // version-gate handled by xml, but just to make sure...
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    // check to see if we're done loading the schedules (the
                    // ScheduleClassAdapter is initialized in onPostExecute)
                    if (adapter != null) {
                        FragmentManager fm = parentAct.getSupportFragmentManager();
                        DoubleDatePickerDialogFragment ddpDlg = DoubleDatePickerDialogFragment
                                .newInstance(classList);
                        ddpDlg.show(fm, "fragment_double_date_picker");
                    }
                } else {
                    Toast.makeText(parentAct,
                            "Export to calendar is not supported on this version of Android",
                            Toast.LENGTH_SHORT).show();
                }

                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
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

        if (currentClasstime != null) {
            mode = parentAct.startActionMode(new ScheduleActionMode());
            slidingDrawer.setVisibility(View.VISIBLE);

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

            slidingDrawer.open();
        }
        // they clicked an empty cell
        else {
            if (mode != null) {
                mode.finish();
            }
            slidingDrawer.setVisibility(View.INVISIBLE);
        }
    }

    private class parseTask extends AsyncTask<Object, String, Integer> {
        private DefaultHttpClient client;
        private String errorMsg;
        private boolean classParseIssue = false;
        private String authCookie;

        public parseTask(DefaultHttpClient client) {
            this.client = client;
        }

        @Override
        protected void onPreExecute() {
            progress.setVisibility(View.VISIBLE);
            scheduleGridView.setVisibility(View.GONE);
            errorLayout.setVisibility(View.GONE);

            client.getCookieStore().clear();
            authCookie = ConnectionHelper.getUtdAuthCookie(parentAct, client);

            BasicClientCookie cookie = new BasicClientCookie("SC", authCookie);
            cookie.setDomain(".utexas.edu");
            client.getCookieStore().addCookie(cookie);

        }

        @Override
        protected void onProgressUpdate(String... params) {
            ((ScheduleActivity) parentAct).getFragments().add(
                    CourseScheduleFragment.newInstance(false, params[0].trim(), params[1]));
            ((ScheduleActivity) parentAct).getAdapter().notifyDataSetChanged();
            ((ScheduleActivity) parentAct).getIndicator().notifyDataSetChanged();
        }

        private String convertStreamToString(InputStream is) {
            @SuppressWarnings("resource")
            Scanner s = new Scanner(is, "iso-8859-1").useDelimiter("\\A");
            String out = s.hasNext() ? s.next() : "";
            s.close();
            return out;
        }

        @Override
        protected Integer doInBackground(Object... params) {
            // "stateful" stuff, I'll get it figured out in the next release
            // if(classList == null)
            classList = new ArrayList<UTClass>();
            // else
            // return classList.size();

            String pagedata = "";

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
                URL location;
                HttpsURLConnection conn = null;
                try {
                    location = new URL(
                            "https://utdirect.utexas.edu/registration/classlist.WBX?sem=" + semId);
                    conn = (HttpsURLConnection) location.openConnection();

                    if (getActivity() == null) {
                        cancel(true);
                        errorMsg = "";
                        return -1;
                    }
                    // TODO: why not just do this in onPreExecute?
                    conn.setRequestProperty("Cookie", "SC=" + authCookie);

                    // conn.setUseCaches(true);
                    // conn.setRequestProperty("Cache-Control",
                    // "only-if-cached");
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    /*
                     * if(HttpResponseCache.getInstalled().get(new URI(
                     * "https://utdirect.utexas.edu/registration/classlist.WBX?sem="
                     * +semId), "GET", conn.getHeaderFields()) != null) {
                     * pagedata =
                     * convertStreamToString(HttpResponseCache.getInstalled
                     * ().get(new URI(
                     * "https://utdirect.utexas.edu/registration/classlist.WBX?sem="
                     * +semId), "GET", conn.getHeaderFields()).getBody()); }
                     * else {
                     */
                    conn.connect();
                    pagedata = convertStreamToString(conn.getInputStream());
                    // }

                } catch (IOException e) {
                    e.printStackTrace();
                    errorMsg = "UTilities could not fetch your class listing";
                    cancel(true);
                    return -1;
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
            } else {
                HttpGet hget = new HttpGet(
                        "https://utdirect.utexas.edu/registration/classlist.WBX?sem=" + semId);
                try {
                    HttpResponse res = client.execute(hget);
                    pagedata = EntityUtils.toString(res.getEntity());
                } catch (Exception e) {
                    e.printStackTrace();
                    errorMsg = "UTilities could not fetch your class listing";
                    cancel(true);
                    return -1;
                }
            }

            // now parse the Class Listing data

            // did we hit the login screen?
            if (pagedata.contains("<title>Information Technology Services - UT EID Logon</title>")) {
                errorMsg = "You've been logged out of UTDirect, back out and log in again.";
                if (parentAct != null) {
                    ConnectionHelper.logout(parentAct);
                }
                cancel(true);
                return -1;
            }
            Pattern semSelectPattern = Pattern.compile("<select  name=\"sem\">.*</select>",
                    Pattern.DOTALL);
            Matcher semSelectMatcher = semSelectPattern.matcher(pagedata);

            // TODO: un-hardcode this eventually! Shouldn't be too hard to
            // figure out the dropdown size
            if (semSelectMatcher.find() && parentAct != null
                    && ((ScheduleActivity) parentAct).getFragments().size() < 3) {
                Pattern semesterPattern = Pattern.compile(
                        "<option.*?value=\"(\\d*)\"\\s*>([\\w\\s]*?)</option>", Pattern.DOTALL);
                Matcher semesterMatcher = semesterPattern.matcher(semSelectMatcher.group());
                while (semesterMatcher.find()) {
                    if (semesterMatcher.group(0).contains("selected=\"selected\"")) {
                        continue;
                    } else {
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
                return 0;
            }
            Pattern classPattern = Pattern.compile("<tr  .*?</tr>", Pattern.DOTALL);
            Matcher classMatcher = classPattern.matcher(pagedata);
            int classCount = 0, colorCount = 0;

            while (classMatcher.find()) {
                String classContent = classMatcher.group();

                String uniqueid = "", classid = "", classname = "";
                String[] buildings, rooms, days, times;
                boolean dropped = false;

                Pattern classAttPattern = Pattern.compile("<td >(.*?)</td>", Pattern.DOTALL);
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
                    buildings = classAttMatcher.group(1).split("<br />");
                    for (int i = 0; i < buildings.length; i++) {
                        buildings[i] = buildings[i].replaceAll("<.*?>", "").trim();
                    }
                } else {
                    classParseIssue = true;
                    continue;
                }
                if (classAttMatcher.find()) {
                    rooms = classAttMatcher.group(1).split("<br />");
                    for (int i = 0; i < rooms.length; i++) {
                        rooms[i] = rooms[i].trim();
                    }
                } else {
                    classParseIssue = true;
                    continue;
                }
                if (classAttMatcher.find()) {
                    days = classAttMatcher.group(1).split("<br />");
                    // Thursday represented by H so I can treat all days as single characters
                    for (int a = 0; a < days.length; a++) {
                        days[a] = days[a].replaceAll("TH", "H").trim();
                    }
                } else {
                    classParseIssue = true;
                    continue;
                }
                if (classAttMatcher.find()) {
                    times = classAttMatcher.group(1).replaceAll("- ", "-").split("<br />");
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
                    classList.add(new UTClass(uniqueid, classid, classname, buildings, rooms, days,
                            times, semId, colors[colorCount]));
                    colorCount = (colorCount == colors.length - 1) ? 0 : colorCount + 1;
                    classCount++;
                }
            }
            return classCount;

        }

        // TODO: nullchecks everywhere, figure out what the real problem is --
        // pretty sure it was because of stupid broken AsyncTask on <2.3
        @Override
        protected void onPostExecute(Integer result) {
            progress.setVisibility(View.GONE);

            if (result != null && result >= 0) {
                if (result == 0) {
                    dayList.setVisibility(View.GONE);
                    noCoursesTextView.setText("You aren't enrolled for this semester.");
                    noCoursesTextView.setVisibility(View.VISIBLE);

                    // if they're not enrolled for the semester, disable the
                    // calendar-specific options
                    setMenuItemsEnabled(false);
                    return;
                } else {
                    adapter = new ScheduleClassAdapter(parentAct, classList);
                    scheduleGridView.setOnItemClickListener(CourseScheduleFragment.this);
                    scheduleGridView.setAdapter(adapter);

                    // scrolls down to the user's earliest class
                    scheduleGridView.setSelection(adapter.getEarliestClassPos());

                    scheduleGridView.setVisibility(View.VISIBLE);
                    dayList.setVisibility(View.VISIBLE);

                    setMenuItemsEnabled(true);
                    if (!parentAct.isFinishing()) {
                        Toast.makeText(parentAct, "Tap a class to see its information.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                errorMsg = "UTilities could not fetch your class listing";
                errorTextView.setText(errorMsg);
                errorTextView.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
                dayList.setVisibility(View.GONE);
                noCoursesTextView.setVisibility(View.GONE);
                scheduleGridView.setVisibility(View.GONE);

                setMenuItemsEnabled(false);
            }
            if (classParseIssue) {
                Toast.makeText(
                        parentAct,
                        "One or more classes could not be parsed correctly, try emailing the dev ;)",
                        Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            errorTextView.setText(errorMsg);
            errorLayout.setVisibility(View.VISIBLE);
            progress.setVisibility(View.GONE);
            dayList.setVisibility(View.GONE);
            noCoursesTextView.setVisibility(View.GONE);
            scheduleGridView.setVisibility(View.GONE);

            setMenuItemsEnabled(false);
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
    }

    private final class ScheduleActionMode implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.setTitle("Class Info");
            MenuInflater inflater = parentAct.getSupportMenuInflater();
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
                    ArrayList<String> building = new ArrayList<String>();
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
        }
    }

}

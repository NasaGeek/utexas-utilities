
package com.nasageek.utexasutilities.fragments;

import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.acra.ACRA;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.mapsaurus.paneslayout.FragmentLauncher;
import com.nasageek.utexasutilities.AsyncTask;
import com.nasageek.utexasutilities.ConnectionHelper;
import com.nasageek.utexasutilities.CourseMapSaxHandler;
import com.nasageek.utexasutilities.MyPair;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.adapters.CourseMapAdapter;
import com.nasageek.utexasutilities.model.BBCourse;
import com.nasageek.utexasutilities.model.CourseMapItem;

public class BlackboardCourseMapFragment extends BlackboardFragment {

    private DefaultHttpClient httpclient;
    private LinearLayout cm_pb_ll;
    private ListView cmlv;
    private ArrayList<BBCourse> classList;
    private ArrayList<MyPair<CourseMapItem, ArrayList<BBCourse>>> classSectionList;
    private fetchCoursemapTask fetch;
    private XMLReader xmlreader;
    private CourseMapSaxHandler courseMapSaxHandler;
    private int itemNumber;
    private ArrayList<MyPair<CourseMapItem, ArrayList>> mainList;
    private LinearLayout failure_ll;
    private TextView failure_tv;
    private Button failure_button;
    private String bbID, courseName, folderName, viewUri;

    private TextView absTitle;
    private TextView absSubtitle;
    private View absView;

    public BlackboardCourseMapFragment() {
    }

    public static BlackboardCourseMapFragment newInstance(String action,
            ArrayList<MyPair<CourseMapItem, ArrayList>> mainList, String courseID,
            String courseName, String folderName, String viewUri, int itemNumber,
            boolean fromDashboard) {
        BlackboardCourseMapFragment bcmf = new BlackboardCourseMapFragment();

        Bundle args = new Bundle();
        args.putString("action", action);
        args.putSerializable("mainList", mainList);
        args.putString("courseID", courseID);
        args.putString("courseName", courseName);
        args.putString("folderName", folderName);
        args.putString("viewUri", viewUri);
        args.putInt("itemNumber", itemNumber);
        args.putBoolean("fromDashboard", fromDashboard);
        bcmf.setArguments(args);

        return bcmf;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        itemNumber = -1;
        courseName = getArguments().getString("courseName");
        bbID = getArguments().getString("courseID");
        folderName = getArguments().getString("folderName");
        viewUri = getArguments().getString("viewUri");
        setHasOptionsMenu(true);

        if (getString(R.string.coursemap_nest_intent).equals(getArguments().getString("action"))) {
            mainList = (ArrayList<MyPair<CourseMapItem, ArrayList>>) getArguments()
                    .getSerializable("mainList");
            itemNumber = getArguments().getInt("itemNumber");
        }

        // settings = PreferenceManager.getDefaultSharedPreferences(this);

        httpclient = ConnectionHelper.getThreadSafeClient();
        httpclient.getCookieStore().clear();
        BasicClientCookie cookie = new BasicClientCookie("s_session_id",
                ConnectionHelper.getBBAuthCookie(getSherlockActivity(), httpclient));
        cookie.setDomain(ConnectionHelper.blackboard_domain_noprot);
        httpclient.getCookieStore().addCookie(cookie);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        absView = inflater.inflate(R.layout.action_bar_title_subtitle, null);
        setupActionBar();
        final View vg = inflater.inflate(R.layout.coursemap_layout, container, false);

        cm_pb_ll = (LinearLayout) vg.findViewById(R.id.coursemap_progressbar_ll);
        cmlv = (ListView) vg.findViewById(R.id.coursemap_listview);
        failure_ll = (LinearLayout) vg.findViewById(R.id.coursemap_error);
        failure_button = (Button) vg.findViewById(R.id.button_send_data);
        failure_tv = (TextView) vg.findViewById(R.id.tv_failure);

        cmlv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final String linkType = mainList.get(position).first.getLinkType();
                final String url = mainList.get(position).first.getViewUrl();
                final SherlockFragmentActivity act = getSherlockActivity();

                if (mainList.get(position).second.size() != 0) // a folder was
                                                               // clicked
                {
                    String path = "";
                    if (itemNumber == -1) {
                        path = mainList.get(position).first.getName();
                    } else {
                        // chain onto the current folder name for "breadcrumbs"
                        path = absSubtitle.getText() + "/" + mainList.get(position).first.getName();
                    }

                    ((FragmentLauncher) act).addFragment(BlackboardCourseMapFragment.this,
                            BlackboardCourseMapFragment.newInstance(
                                    getString(R.string.coursemap_nest_intent),
                                    mainList.get(position).second, bbID, courseName, path, url,
                                    position, false));
                } else if (linkType.equals("resource/x-bb-file")
                        || linkType.equals("resource/x-bb-document")) {
                    String contentid = mainList.get(position).first.getContentId();
                    String itemName = "";
                    if (itemNumber == -1) {
                        itemName = mainList.get(position).first.getName(); // will
                                                                           // be
                                                                           // used
                                                                           // as
                                                                           // Subtitle
                    } else {
                        itemName = absSubtitle.getText() + "/"
                                + mainList.get(position).first.getName(); // Subtitle
                    }

                    ((FragmentLauncher) act).addFragment(BlackboardCourseMapFragment.this,
                            BlackboardDownloadableItemFragment.newInstance(contentid, bbID,
                                    courseName, itemName, url, false));

                } else if (linkType.equals("resource/x-bb-externallink")) {
                    Intent exItemLaunch = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

                    exItemLaunch.putExtra("courseid", bbID);
                    exItemLaunch.putExtra("coursename", courseName);
                    startActivity(exItemLaunch);
                } else if (linkType.equals("student_gradebook")) {
                    ((FragmentLauncher) act).addFragment(BlackboardCourseMapFragment.this,
                            BlackboardGradesFragment.newInstance(bbID, courseName, url, false));
                } else if (linkType.equals("announcements")) {
                    ((FragmentLauncher) act).addFragment(BlackboardCourseMapFragment.this,
                            BlackboardAnnouncementsFragment.newInstance(bbID, courseName, url,
                                    false));
                } else // default to webview
                {
                    String itemName = "";
                    if (itemNumber == -1) {
                        itemName = mainList.get(position).first.getName(); // will
                                                                           // be
                                                                           // used
                                                                           // as
                                                                           // Subtitle
                    } else {
                        itemName = absSubtitle.getText() + "/"
                                + mainList.get(position).first.getName(); // Subtitle
                    }

                    ((FragmentLauncher) act).addFragment(BlackboardCourseMapFragment.this,
                            BlackboardExternalItemFragment.newInstance(url, bbID, courseName,
                                    itemName, false));
                }
            }
        });

        // ONLY DO IF TOP LEVEL
        if (itemNumber == -1 && mainList == null) {
            fetch = new fetchCoursemapTask(httpclient);
            fetch.execute();
        }
        // now we've got the whole course tree, navigate as necessary
        else if (mainList != null && mainList.size() != 0) {
            cmlv.setAdapter(new CourseMapAdapter(getSherlockActivity(), mainList));
            cm_pb_ll.setVisibility(View.GONE);
            cmlv.setVisibility(View.VISIBLE);
        }

        return vg;
    }

    private void setupActionBar() {
        final ActionBar actionbar = getSherlockActivity().getSupportActionBar();
        // actionbar.setDisplayShowCustomEnabled(true);
        // actionbar.setDisplayShowTitleEnabled(false);
        actionbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM
                | ActionBar.DISPLAY_SHOW_TITLE);
        actionbar.setCustomView(absView);

        absTitle = (TextView) actionbar.getCustomView().findViewById(R.id.abs__action_bar_title);
        absSubtitle = (TextView) actionbar.getCustomView().findViewById(
                R.id.abs__action_bar_subtitle);

        absSubtitle.setText(folderName);
        if (folderName != null) {
            absTitle.setText(courseName);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fetch != null) {
            fetch.cancel(true);
        }
    }

    @Override
    public String getBbid() {
        return bbID;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        // show the menu only if not top-level
        // there is no "nice" page for the top-level coursemap viewable in a
        // browser
        if (itemNumber != -1) {
            inflater.inflate(R.menu.blackboard_course_map_menu, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.course_map_view_in_web:
                showAreYouSureDlg(getSherlockActivity());
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
                    ((FragmentLauncher) getSherlockActivity()).addFragment(
                            BlackboardCourseMapFragment.this, BlackboardExternalItemFragment
                                    .newInstance(viewUri, bbID, courseName, folderName, false));
                } else {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(viewUri));
                    startActivity(i);
                }
                /*
                 * Intent web = new
                 * Intent(null,Uri.parse(getIntent().getStringExtra
                 * ("viewUri")),CourseMapActivity
                 * .this,BlackboardExternalItemActivity.class);
                 * web.putExtra("itemName",
                 * getIntent().getStringExtra("folderName")); //will be used as
                 * SubTitle web.putExtra("coursename",
                 * getIntent().getStringExtra("coursename")); //will be used as
                 * Title startActivity(web);
                 */
            }
        });
        alertBuilder.setTitle("View on Blackboard");
        alertBuilder.show();
    }

    @Override
    public String getCourseName() {
        return getArguments().getString("courseName");
    }

    @Override
    public boolean isFromDashboard() {
        return getArguments().getBoolean("fromDashboard");
    }

    private class fetchCoursemapTask extends AsyncTask<Object, Void, ArrayList> {
        private DefaultHttpClient client;
        private String failureMessage = "";
        private String pagedata;
        private Exception ex;
        private Boolean showButton = false;

        public fetchCoursemapTask(DefaultHttpClient client) {
            this.client = client;
        }

        @Override
        protected void onPreExecute() {
            cm_pb_ll.setVisibility(View.VISIBLE);
            cmlv.setVisibility(View.GONE);
            failure_ll.setVisibility(View.GONE);
        }

        @Override
        protected ArrayList doInBackground(Object... params) {
            HttpGet hget = new HttpGet(ConnectionHelper.blackboard_domain
                    + "/webapps/Bb-mobile-BBLEARN/courseMap?course_id=" + bbID);
            String pagedata = "";

            try {
                HttpResponse response = client.execute(hget);
                pagedata = EntityUtils.toString(response.getEntity());
            } catch (Exception e) {
                failureMessage = "UTilities could not fetch this course map";
                e.printStackTrace();
                cancel(true);
                return null;
            }

            try {

                // create the factory
                SAXParserFactory factory = SAXParserFactory.newInstance();

                // create a parser
                SAXParser parser = factory.newSAXParser();
                // create the reader (scanner)
                xmlreader = parser.getXMLReader();
                // instantiate our handler
                courseMapSaxHandler = new CourseMapSaxHandler();
                // assign our handler
                xmlreader.setContentHandler(courseMapSaxHandler);

                InputSource is = new InputSource(new StringReader(pagedata));

                xmlreader.parse(is);

                mainList = courseMapSaxHandler.getParsedData();
            } catch (Exception e) {
                failureMessage = "UTilities could not parse the downloaded Blackboard data.";
                ex = e;
                this.pagedata = pagedata;
                showButton = true;
                e.printStackTrace();
                cancel(true);
                return null;
            }

            return mainList;
        }

        @Override
        protected void onPostExecute(ArrayList result) {
            if (!this.isCancelled()) {
                if (getSherlockActivity() != null) {
                    cmlv.setAdapter(new CourseMapAdapter(getSherlockActivity(), result));
                }

                cm_pb_ll.setVisibility(View.GONE);
                cmlv.setVisibility(View.VISIBLE);
                failure_ll.setVisibility(View.GONE);
            }
        }

        @Override
        protected void onCancelled() {
            failure_tv.setText(failureMessage);
            failure_button
                    .setText("Send anonymous information about this course to help improve UTilities.");
            failure_button.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (pagedata != null && ex != null) {
                        SharedPreferences sp = PreferenceManager
                                .getDefaultSharedPreferences(getSherlockActivity().getBaseContext());
                        if (!sp.getBoolean("acra.enable", true)) {
                            ACRA.getErrorReporter().setEnabled(true);
                        }
                        ACRA.getErrorReporter().putCustomData("xmldata", pagedata);
                        ACRA.getErrorReporter().handleException(ex);
                        ACRA.getErrorReporter().removeCustomData("xmldata");
                        if (!sp.getBoolean("acra.enable", true)) {
                            ACRA.getErrorReporter().setEnabled(false);
                        }
                        Toast.makeText(getSherlockActivity(),
                                "Data is being sent, thanks for helping out!", Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        Toast.makeText(getSherlockActivity(),
                                "Couldn't send the course data for some reason :(",
                                Toast.LENGTH_SHORT).show();
                    }
                    v.setVisibility(View.INVISIBLE);

                }
            });
            cm_pb_ll.setVisibility(View.GONE);
            cmlv.setVisibility(View.GONE);
            if (showButton) {
                failure_button.setVisibility(View.VISIBLE);
            }
            failure_ll.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPanesScrolled() {
        setupActionBar();
    }
}

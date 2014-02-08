
package com.nasageek.utexasutilities.fragments.canvas;

import java.util.ArrayList;

import retrofit.RetrofitError;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.nasageek.utexasutilities.AttachmentDownloadService;
import com.nasageek.utexasutilities.ConnectionHelper;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.activities.BlackboardPanesActivity.OnPanesScrolledListener;
import com.nasageek.utexasutilities.adapters.FileAdapter;
import com.nasageek.utexasutilities.fragments.BaseSpiceListFragment;
import com.nasageek.utexasutilities.model.canvas.File;
import com.nasageek.utexasutilities.requests.CanvasFilesRequest;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

public class FileBrowserFragment extends BaseSpiceListFragment implements OnPanesScrolledListener {

    private FileAdapter mFileAdapter;
    private CanvasFilesRequest canvasFilesRequest;
    private String courseId, courseName, courseCode;
    private BroadcastReceiver onNotificationClick;

    public static FileBrowserFragment newInstance(String courseID, String courseName,
            String courseCode) {
        FileBrowserFragment fbf = new FileBrowserFragment();

        Bundle args = new Bundle();
        args.putString("courseID", courseID);
        args.putString("courseName", courseName);
        args.putString("courseCode", courseCode);
        fbf.setArguments(args);

        return fbf;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setBackgroundResource(R.drawable.background_holo_light);
        // this should be free... but it ain't
        if (getListAdapter() == null) {
            setListShown(false);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        courseId = getArguments().getString("courseID");
        courseName = getArguments().getString("courseName");
        courseCode = getArguments().getString("courseCode");
        setupActionBar();

        canvasFilesRequest = new CanvasFilesRequest(
                ConnectionHelper.getCanvasAuthCookie(getActivity()), courseId);
        getSpiceManager().execute(canvasFilesRequest, courseId + "files",
                DurationInMillis.ONE_MINUTE * 5, new CanvasFilesRequestListener());
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        final File item = (File) (l.getItemAtPosition(position));

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
        alertBuilder.setMessage("Would you like to download this file?")
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                    public void onClick(DialogInterface dialog, int which) {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                                && Environment.getExternalStorageState().equals(
                                        Environment.MEDIA_MOUNTED)) {
                            Intent downloadAttachment = new Intent(getActivity(),
                                    AttachmentDownloadService.class);
                            downloadAttachment.putExtra("fileName", item.display_name);
                            downloadAttachment.putExtra("url", item.url);
                            downloadAttachment.putExtra("service", "canvas");
                            getActivity().startService(downloadAttachment);
                        } else if (Environment.getExternalStorageState().equals(
                                Environment.MEDIA_MOUNTED)
                                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            final DownloadManager manager = (DownloadManager) getSherlockActivity()
                                    .getSystemService(Context.DOWNLOAD_SERVICE);

                            onNotificationClick = new BroadcastReceiver() {
                                @Override
                                @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                                public void onReceive(Context con, Intent intent) {
                                    final String action = intent.getAction();
                                    DownloadManager notifmanager = (DownloadManager) con
                                            .getSystemService(Context.DOWNLOAD_SERVICE);
                                    if (DownloadManager.ACTION_NOTIFICATION_CLICKED.equals(action)) {
                                        long[] dlIDs = intent
                                                .getLongArrayExtra(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS);
                                        Uri downloadedFile = notifmanager
                                                .getUriForDownloadedFile(dlIDs[0]);
                                        // not sure when dlIDs will ever have >1
                                        // member, so let's just assume only 1
                                        // member
                                        // TODO: need to confirm when dlIDs
                                        // might be >1
                                        if (downloadedFile != null) { // make
                                                                      // sure
                                                                      // file
                                                                      // isn't
                                                                      // still
                                                                      // downloading
                                            try {
                                                con.startActivity(new Intent(Intent.ACTION_VIEW,
                                                        downloadedFile));
                                            } catch (ActivityNotFoundException ex) {
                                                ex.printStackTrace();
                                                // TODO: let the user know
                                                // something went wrong?
                                            }
                                        } else {
                                            Toast.makeText(con,
                                                    "Download could not be opened at this time.",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            };
                            getSherlockActivity().registerReceiver(onNotificationClick,
                                    new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));

                            Uri uri = Uri.parse(Uri.decode(item.url));

                            Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_DOWNLOADS).mkdirs();
                            DownloadManager.Request request = new DownloadManager.Request(uri);

                            // fix stupid Honeycomb bug
                            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR2) {
                                request.setShowRunningNotification(true);
                            } else {
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                            }
                            request.setDescription("Downloading to the Download folder.")
                                    .setTitle(item.display_name)
                                    .setDestinationInExternalPublicDir(
                                            Environment.DIRECTORY_DOWNLOADS, item.display_name);
                            // need auth header?

                            try {
                                final long dlID = manager.enqueue(request);
                            } catch (IllegalArgumentException iae) {
                                // fallback for people with messed up Downloads
                                // provider
                                Intent downloadAttachment = new Intent(getSherlockActivity(),
                                        AttachmentDownloadService.class);
                                downloadAttachment.putExtra("fileName", item.display_name);
                                downloadAttachment.putExtra("url", item.url);
                                downloadAttachment.putExtra("service", "canvas");
                                getSherlockActivity().startService(downloadAttachment);
                            }
                            Toast.makeText(
                                    getSherlockActivity(),
                                    "Download started, item should appear in the \"Download\" folder on your external storage.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            AlertDialog.Builder build = new AlertDialog.Builder(
                                    getSherlockActivity());
                            build.setNeutralButton("Okay", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                                    .setTitle("No External Media")
                                    .setMessage(
                                            "Your external storage media (such as a microSD Card) is currently unavailable; "
                                                    + "the download cannot start.").show();
                        }
                    }
                }).setTitle("Download File").show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (onNotificationClick != null) {
            try {
                getActivity().unregisterReceiver(onNotificationClick);
                onNotificationClick = null;
            } catch (IllegalArgumentException e) { // if it's already been
                                                   // unregistered
                e.printStackTrace();
            }
        }
    }

    public final class CanvasFilesRequestListener implements RequestListener<File.List> {

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            spiceException.printStackTrace();
            // it looks like this can happen if the user isn't authorized (but
            // when is that the case?)
            // either it occurs after the course has completed, it occurs
            // because there are no files,
            // or it is a specific setting activated by the instructor.
            if (((RetrofitError) spiceException.getCause()).getResponse().getStatus() == 401) {
                Toast.makeText(getSherlockActivity(), "No files available", Toast.LENGTH_SHORT)
                        .show();
                setListAdapter(new FileAdapter(getActivity(), R.layout.file_view,
                        new ArrayList<File>()));
                setEmptyText("No files available/Not authorized");
            }
        }

        @Override
        public void onRequestSuccess(final File.List result) {
            setListAdapter(new FileAdapter(getActivity(), R.layout.file_view, result));
            setEmptyText("No files available/Not authorized");
        }
    }

    private void setupActionBar() {
        final ActionBar actionbar = getSherlockActivity().getSupportActionBar();
        actionbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE, ActionBar.DISPLAY_SHOW_TITLE
                | ActionBar.DISPLAY_SHOW_CUSTOM);
        actionbar.setTitle(courseCode);
        actionbar.setSubtitle("Files");
    }

    @Override
    public void onPanesScrolled() {
        setupActionBar();
    }

    @Override
    public int getPaneWidth() {
        return R.integer.blackboard_content_width_percentage;
    }
}

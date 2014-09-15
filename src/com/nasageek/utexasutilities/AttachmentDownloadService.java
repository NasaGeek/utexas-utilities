
package com.nasageek.utexasutilities;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.nasageek.utexasutilities.fragments.BlackboardFragment;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@SuppressLint("NewApi")
public class AttachmentDownloadService extends IntentService {

    private Handler handler;

    public AttachmentDownloadService() {
        super("AttachmentDownload");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        handler = new Handler();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String urlToDownload = BlackboardFragment.BLACKBOARD_DOMAIN + intent.getStringExtra("url");
        String fileName = intent.getStringExtra("fileName");

        NotificationCompat.Builder notbuild = new NotificationCompat.Builder(
                AttachmentDownloadService.this);
        Notification n = notbuild
                .setContentIntent(PendingIntent.getActivity(this, 010, new Intent(), 0))
                .setOngoing(true).setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle(fileName).setContentText("Download in progress")
                .setTicker("UTilities download started.")
                // TODO: .build(); really should do this, but don't want to
                // break anything
                .getNotification();
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(fileName,
                1123, n);

        Uri dlLocation = null;
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(urlToDownload)
                    .build();
            Response response = client.newCall(request).execute();

            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    .mkdirs();
            dlLocation = Uri.withAppendedPath(Uri.fromFile(Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)),
                    fileName);
            // download the file
            InputStream input = new BufferedInputStream(response.body().byteStream());

            OutputStream output = new FileOutputStream(dlLocation.getPath());
            byte data[] = new byte[2048];
            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
            n = notbuild.setOngoing(false).setSmallIcon(android.R.drawable.stat_sys_download_done)
                    .setContentText("Download failed").setTicker("Download failed.")
                    // TODO: .build(); really should do this, but don't want to
                    // break anything
                    .getNotification();
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(fileName,
                    1123, n);
            return;
        }

        if (new Intent(Intent.ACTION_VIEW, dlLocation).resolveActivity(getPackageManager()) == null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(
                            getApplicationContext(),
                            "You do not have any apps that can open this file; download one from the Play Store.",
                            Toast.LENGTH_LONG).show();
                }
            });
        }
        n = notbuild
                .setContentIntent(
                        PendingIntent.getActivity(this, 0, new Intent(Intent.ACTION_VIEW,
                                dlLocation), 0)).setOngoing(false)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentText("Download complete").setTicker("Download complete.")
                // TODO: .build(); really should do this, but don't want to
                // break anything
                .getNotification();
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(fileName,
                1123, n);
    }
}

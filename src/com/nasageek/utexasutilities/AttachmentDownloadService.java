package com.nasageek.utexasutilities;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import com.crittercism.app.Crittercism;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.v4.app.NotificationCompat;
import android.widget.AbsListView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class AttachmentDownloadService extends IntentService {

	private Handler handler;
	
	public AttachmentDownloadService() {
		super("AttachmentDownload");
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		handler = new Handler();
		return super.onStartCommand(intent, flags, startId);
	}
	@Override
	protected void onHandleIntent(Intent intent) {

		String urlToDownload = "https://courses.utexas.edu" + intent.getStringExtra("url");
		String fileName = intent.getStringExtra("fileName");
		
		NotificationCompat.Builder notbuild = new NotificationCompat.Builder(AttachmentDownloadService.this);
		Notification n = notbuild.setContentIntent(PendingIntent.getActivity(this, 010, new Intent(), 0))
		 .setOngoing(true)
   	 	.setSmallIcon(android.R.drawable.stat_sys_download)
        .setContentTitle(fileName)
        .setContentText("Download in progress")
        .setTicker("UTilities download started.")
        .getNotification();
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(fileName,1123,n);
		
        Uri dlLocation = null;
        try {
            URL url = new URL(urlToDownload);
            
            URLConnection connection = url.openConnection();
            connection.addRequestProperty("Cookie", "s_session_id="+ConnectionHelper.getBBAuthCookie(AttachmentDownloadService.this, ConnectionHelper.getThreadSafeClient()));
            
            if(Build.VERSION.SDK_INT<=Build.VERSION_CODES.ECLAIR_MR1)
            {	(new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Download")).mkdirs();
            	dlLocation = Uri.withAppendedPath(Uri.fromFile(Environment.getExternalStorageDirectory()), "Download/"+fileName);
            }
            else
            {	Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdirs();
            	dlLocation = Uri.withAppendedPath(Uri.fromFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)),fileName);
            }
            // download the file
            InputStream input = new BufferedInputStream(url.openStream());

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
            n = notbuild.setOngoing(false)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentText("Download failed")
            .setTicker("Download failed.")
            .getNotification();
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(fileName, 1123, n);
            return;
        }

        if(new Intent(Intent.ACTION_VIEW, dlLocation).resolveActivity(getPackageManager()) == null)
        {	
        	handler.post(new Runnable()
        	{
				@Override
				public void run() {
					Toast.makeText(getApplicationContext(), "You do not have any apps that can open this file; download one from the Play Store.", Toast.LENGTH_LONG).show();	
				}	
        	});
        }
        n = notbuild.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(Intent.ACTION_VIEW,dlLocation), 0))
       	.setOngoing(false)
       	.setSmallIcon(android.R.drawable.stat_sys_download_done)
       	.setContentText("Download complete")
        .setTicker("Download complete.")
        .getNotification();
       	((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(fileName, 1123, n);
       	Crittercism.leaveBreadcrumb("Attachment Downloaded (<3.0)");
    }
}

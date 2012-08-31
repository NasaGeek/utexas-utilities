package com.nasageek.utexasutilities.libraries;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

//class for shared data and content, including Log in method, and client for connection
public class shared {

	public static final int LOGGINGLEVEL = 0;

	public static String getHTMLfromURL(String urlstring) throws Exception { //should probably catch exception instead of throwing it
		URL url = new URL(urlstring);
		BufferedReader in = new BufferedReader(new InputStreamReader(
				url.openStream()));

		String HTML = "";
		String add = in.readLine();
		while (add != null) {
			HTML += add;
			add = in.readLine();
		}

		return HTML;
	}


	public static boolean loggedIntoCatalog = false;
	public static boolean loggedIntoUTDirect = false;

	public static boolean connectedToInternet = false;
	public static boolean logInWorks = false;
	public static boolean displayedLogInCheck = false;
	public static boolean displayedInternetCheck = false;

	@SuppressWarnings("unused")
	public static String retrieveProtectedWebPage (Context context,DefaultHttpClient client, String uri)
	{
		String html = "";

		try{
			boolean success = logIntoUTDirect(context, client);
			if (shared.LOGGINGLEVEL>0) Log.i("shared", "retrieveprotectedwebpage log in success: " + success);
			HttpGet httpget = new HttpGet();
			httpget.setURI(new URI(uri));
			HttpResponse response = client.execute(httpget);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));

			String next = in.readLine();
			while(next!=null){
				//	System.out.println(next);
				html+=next;
				next = in.readLine();
			}
			//			response.getEntity().getContent().close();
			return html;

		}
		catch(Exception e)
		{
			//			Toast toast = Toast.makeText(context, "Could not retrive web page. Please check network connection and try again later.", Toast.LENGTH_SHORT);
			//			toast.show();
			if (shared.LOGGINGLEVEL>0) Log.e("shared",
					"exception in retrieveProtectedWebPage: ",e);
			return null;
		}
	}

	@SuppressWarnings("unused")
	public static boolean logIntoUTDirect(Context context, DefaultHttpClient client)
	{
		try{
			SharedPreferences loginPreferences = PreferenceManager.getDefaultSharedPreferences(context);
//			SharedPreferences loginPreferences = context.getSharedPreferences("login", Context.MODE_PRIVATE);
			String username = loginPreferences.getString("uteid", "");
			String password = loginPreferences.getString("password","");


			HttpPost httppost = new HttpPost ("https://utdirect.utexas.edu/security-443/logon_check.logonform");
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("LOGON", username));  			//use my info for testing
			nameValuePairs.add(new BasicNameValuePair("PASSWORDS", password));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.ASCII));
						HttpResponse response = client.execute(httppost);
									response.getEntity().getContent().close();
		}
		catch(Exception e)
		{
			if (shared.LOGGINGLEVEL>0) Log.e("shared",
					"exception in logIntoUTDirect: ",e);
			loggedIntoUTDirect = false;
			//			Toast toast = Toast.makeText(context, "Could not Log in. Please check UTEID/Password and network connection.", Toast.LENGTH_SHORT);
			//			toast.show();
			return false;
		}
		loggedIntoUTDirect =  true;
		return true;

	}

	@SuppressWarnings("unused")
	public static boolean logIntoCatalog (Context context,DefaultHttpClient client)
	{
		try{
			SharedPreferences loginPreferences = PreferenceManager.getDefaultSharedPreferences(context);
//			SharedPreferences loginPreferences = context.getSharedPreferences("login", Context.MODE_PRIVATE);
			String username = loginPreferences.getString("uteid", "");
			String password = loginPreferences.getString("password","");


			HttpPost httppost = new HttpPost ("https://catalog.lib.utexas.edu/patroninfo/");
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("extpatid", username));
			nameValuePairs.add(new BasicNameValuePair("extpatpw", password));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.ASCII));
			HttpResponse response = client.execute(httppost);
			response.getEntity().getContent().close();

		}
		catch(Exception e)
		{
			if (shared.LOGGINGLEVEL>0) Log.i("shared",
					"exception in logIntoCatalog: " + e.toString());
			loggedIntoCatalog = false;
			//			Toast toast = Toast.makeText(context, "Could not Log in. Please check UTEID/Password and network connection.", Toast.LENGTH_SHORT);
			//			toast.show();
			return false;
		}
		loggedIntoCatalog =  true;
		return true;
	}

	public static void checkLogInCredentials(Context contextvar, Handler handlervar, boolean showToastvar, String textvar)
	{

		final Context context = contextvar;
		final Handler handler = handlervar;
		final boolean showToast = showToastvar;
		final String text = textvar;

		(new Thread(new Runnable(){
			@SuppressWarnings("unused")
			@Override
			public void run() {
				boolean correcttemp = false;
				try{
					checkInternetConnection(context, handler, showToast, text);

					DefaultHttpClient client = new DefaultHttpClient();
					String html="";

					SharedPreferences loginPreferences = PreferenceManager.getDefaultSharedPreferences(context);
//					SharedPreferences loginPreferences = context.getSharedPreferences("login", Context.MODE_PRIVATE);
					String username = loginPreferences.getString("uteid", "");
					String password = loginPreferences.getString("password","");

					if (shared.LOGGINGLEVEL>0) Log.i("shared", "uteid:" + username + " password: " + password);

					HttpPost httppost = new HttpPost ("https://utdirect.utexas.edu/security-443/logon_check.logonform");
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
					nameValuePairs.add(new BasicNameValuePair("LOGON", username));
					nameValuePairs.add(new BasicNameValuePair("PASSWORDS", password));
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.ASCII));
					HttpResponse response = client.execute(httppost);


					HttpGet httpget = new HttpGet();
					httpget.setURI(new URI("https://utdirect.utexas.edu/utdirect/index.WBX"));
					response = client.execute(httpget);
					BufferedReader in = new BufferedReader(new InputStreamReader(
							response.getEntity().getContent()));

					String next = in.readLine();
					while(next!=null){
						//	System.out.println(next);
						html+=next;
						next = in.readLine();
						correcttemp |=  (html.contains("UT Direct - My Home"));
						if (correcttemp)break;
					}
					final boolean correct = correcttemp;
					handler.post(new Runnable(){

						@Override
						public void run() {
							if (!correct && showToast){
								int duration = Toast.LENGTH_LONG;
								Toast toast = Toast.makeText(context, "Could not log in. Please check UTEID/Password combination.", duration);
								toast.show();
								displayedLogInCheck = false;

							}
							else if (correct && showToast)
							{
								displayedLogInCheck = true;
								int duration = Toast.LENGTH_LONG;
								Toast toast = Toast.makeText(context, "Log in successful. Please feel free to access any of the app's features.", duration);
								toast.show();
							}
					}
				});
					logInWorks = correct;
				}
				catch(Exception e)
				{
					if (shared.LOGGINGLEVEL>0) Log.e("shared", "checkLogInCredentials",e);
					logInWorks = false;
				}
//				return correcttemp;
			}
		})).start();
	}
	public static boolean checkInternetConnection (Context context, Handler handler, boolean showToast, String text)
	{
		//code copied from http://stackoverflow.com/questions/1560788/how-to-check-internet-access-on-android-inetaddress-never-timeouts
		ConnectivityManager cm =
			(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		boolean connected =  cm.getActiveNetworkInfo() != null &&
		cm.getActiveNetworkInfo().isConnectedOrConnecting();

		if (!connected && showToast){
			int duration = Toast.LENGTH_LONG;
			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}
		connectedToInternet = connected;
		return connected;
	}
	public static boolean checkLogInandInternetHeader(Context context){
		if (!shared.connectedToInternet)
		{
			int duration = Toast.LENGTH_LONG;
			Toast toast = Toast.makeText(context, "This feature requires an internet connection. Please try again later.", duration);
			toast.show();
			return false;
		}
		else if (!shared.logInWorks)
		{
			int duration = Toast.LENGTH_LONG;
			Toast toast = Toast.makeText(context, "This feature requires a working UT Login. Please login and try again later.", duration);
			toast.show();
			return false;
		}
		return true;
	}
}

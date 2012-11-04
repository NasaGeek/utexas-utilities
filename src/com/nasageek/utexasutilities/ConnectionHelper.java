package com.nasageek.utexasutilities;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.crittercism.app.Crittercism;

public class ConnectionHelper {
	
	private HttpPost httppost;
	private DefaultHttpClient httpclient;
	private static SharedPreferences settings;
	private static SecurePreferences sp;
	public static String authCookie;
	public static String PNAAuthCookie;
	public static String bbAuthCookie;
	private static boolean bbCookieHasBeenSet = false;
	private static boolean PNACookieHasBeenSet = false;
	private static boolean cookieHasBeenSet = false;
	private static boolean bbLoggedIn = false;
	private static boolean PNALoggedIn = false;
	private static boolean loggedIn = false;
	public static boolean logindone=false, pnalogindone=false, bbLoginDone=false;
	public static boolean loggingIn=false;

	public static DefaultHttpClient getThreadSafeClient() {

	    DefaultHttpClient client = new DefaultHttpClient();
	    ClientConnectionManager mgr = client.getConnectionManager();
	    HttpParams params = client.getParams();

	    client = new DefaultHttpClient(
	        new ThreadSafeClientConnManager(params,
	            mgr.getSchemeRegistry()), params);
	    
	    return client;
	}
	public boolean bbLogin(Context con, DefaultHttpClient client)
	{
		settings = PreferenceManager.getDefaultSharedPreferences(con);
		sp = new SecurePreferences(con, "com.nasageek.utexasutilities.password","lalalawhatanicekey",false);
		HttpPost httppost = new HttpPost("https://courses.utexas.edu/webapps/login/");
		try {
		       
			 // Add your data
		    	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		
		        nameValuePairs.add(new BasicNameValuePair("user_id", settings.getString("eid","error").trim()));
		        nameValuePairs.add(new BasicNameValuePair("password", sp.getString("password")));
		        
		        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,HTTP.ASCII));

		        // Execute HTTP Post Request
		        HttpResponse response = client.execute(httppost);

		   
		    } catch (Exception ex) {
		    	
		    	bbLoggedIn = false;
		        return false;
		    }
		    
		   bbLoggedIn = true; 
		   return true;
	}
	public boolean Login(Context con, DefaultHttpClient client)
	{
		settings = PreferenceManager.getDefaultSharedPreferences(con);
		sp = new SecurePreferences(con, "com.nasageek.utexasutilities.password","lalalawhatanicekey",false);
		HttpPost httppost = new HttpPost("https://utdirect.utexas.edu/security-443/logon_check.logonform");
		try {
		       
			 // Add your data
		    	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		
		        nameValuePairs.add(new BasicNameValuePair("LOGON", settings.getString("eid","error").trim()));
		        nameValuePairs.add(new BasicNameValuePair("PASSWORDS", sp.getString("password")));
		        
		        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,HTTP.ASCII));

		        // Execute HTTP Post Request
		        HttpResponse response = client.execute(httppost);

		   
		    } catch (Exception ex) {
		    	
		    	loggedIn = false;
		        return false;
		    }
		    
		   loggedIn = true; 
		   return true;
		
	}
	public static void logout(Context con)
	{
		settings = PreferenceManager.getDefaultSharedPreferences(con);
		Editor edit = settings.edit();
		getThreadSafeClient().getCookieStore().clear();
		resetCookies();
		edit.putBoolean("loggedin", false);
		
		Utility.commit(edit);
		loggingIn = false;
		Crittercism.leaveBreadcrumb("Logged out");
		
	}
	public static boolean isLoggingIn()
	{
		return loggingIn;
	}
	public boolean PNALogin(Context con, DefaultHttpClient client)
	{
		settings = PreferenceManager.getDefaultSharedPreferences(con);
		sp = new SecurePreferences(con, "com.nasageek.utexasutilities.password","lalalawhatanicekey",false);
		HttpPost httppost = new HttpPost("https://management.pna.utexas.edu/server/graph.cgi");
		try {
		       
			 // Add your data
		    	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		
		        nameValuePairs.add(new BasicNameValuePair("PNACLOGINusername", settings.getString("eid","error").trim()));
		        nameValuePairs.add(new BasicNameValuePair("PNACLOGINpassword", sp.getString("password")));
		        
		        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,HTTP.ASCII));

		        // Execute HTTP Post Request
		        HttpResponse response = client.execute(httppost);
		        
		    } catch (Exception ex) {
		    	
		    	PNALoggedIn = false;
		//    	Toast.makeText(con, "There was an error while connecting to UT PNA, please check your internet connection and try again", Toast.LENGTH_LONG).show();
		//        Log.e("Error connecting to utexas.edu", ex.toString());	
		        return false;
		    }
		   PNALoggedIn = true; 
		   return true;
	}
	public static String getPNAAuthCookie(Context con, DefaultHttpClient client)
	{
		settings = PreferenceManager.getDefaultSharedPreferences(con);
		

		if(!PNACookieHasBeenSet)
		{
			List<Cookie> cooklist = client.getCookieStore().getCookies();
		    	
		    	for(int i = 0; i<cooklist.size(); i++)
		    	{
		    	//	Log.d("COOKIE", cooklist.get(i).getValue()+"");
		    		if(cooklist.get(i).getName().equals("AUTHCOOKIE"))
		    		{	
		    			PNAAuthCookie = cooklist.get(i).getValue();
		    			PNACookieHasBeenSet = true;
		    			return PNAAuthCookie;
		    		}
		    	}
		    	
	//	    	Log.e("CREDS", "Error authenticating credentials");
				Toast.makeText(con, "Something went wrong during login, try checking your UT EID and Password and try again.", Toast.LENGTH_LONG).show();
				PNALoggedIn = false;
				Log.d("PNACOOKIE", "Login failed");
				return "";
		}
		else
		{
			return PNAAuthCookie;
		}
	}
	public static void PNASetAuthCookie(String cookie)
	{
		PNAAuthCookie = cookie;
		PNACookieHasBeenSet = true;
	}
	public static void bbSetAuthCookie(String cookie)
	{
		bbAuthCookie = cookie;
		bbCookieHasBeenSet = true;
	}
	public static String getAuthCookie(Context con, DefaultHttpClient client )
	{
		//settings = PreferenceManager.getDefaultSharedPreferences(con);
	

		if(!cookieHasBeenSet)
		{
			List<Cookie> cooklist = client.getCookieStore().getCookies();
		    	
		    	for(int i = 0; i<cooklist.size(); i++)
		    	{
		    	//	Log.d("COOKIE", cooklist.get(i).getValue()+"");
		    		if(cooklist.get(i).getName().equals("SC") && !cooklist.get(i).getValue().equals("NONE"))
		    		{	
		    			authCookie = cooklist.get(i).getValue();
		    			cookieHasBeenSet = true;
		    			return authCookie;
		    		}
		    	}
		    	
	//	    	Log.e("CREDS", "Error authenticating credentials");
				Toast.makeText(con, "Something went wrong during login, try checking your UT EID and Password and try again.", Toast.LENGTH_LONG).show();
				loggedIn = false;
				Log.d("COOKIE", "Login failed");
				return "";
		}
		else
		{
			return authCookie;
		}
	}
	public static String getBBAuthCookie(Context con, DefaultHttpClient client )
	{
		if(!bbCookieHasBeenSet)
		{
			List<Cookie> cooklist = client.getCookieStore().getCookies();
		    	
		    	for(int i = 0; i<cooklist.size(); i++)
		    	{
		    	//	Log.d("COOKIE", cooklist.get(i).getValue()+"");
		    		if(cooklist.get(i).getName().equals("s_session_id"))
		    		{	
		    			bbAuthCookie = cooklist.get(i).getValue();
		    			bbCookieHasBeenSet = true;
		    			return bbAuthCookie;
		    		}
		    	}
		    	
	//	    	Log.e("CREDS", "Error authenticating credentials");
				Toast.makeText(con, "Something went wrong during Blackboard login, try checking your UT EID and Password and try again.", Toast.LENGTH_LONG).show();
				bbLoggedIn = false;
				Log.d("BBCOOKIE", "Login failed");
				return "";
		}
		else
		{
			return bbAuthCookie;
		}
	}
	public static void resetCookie()
	{
		authCookie = "";
		loggedIn = false;
		cookieHasBeenSet = false;
	}
	public static void resetBBCookie()
	{
		bbAuthCookie = "";
		bbLoggedIn = false;
		bbCookieHasBeenSet = false;
	}
	public static void setAuthCookie(String cookie)
	{
		authCookie = cookie;
		cookieHasBeenSet = true;
	}
	public static void setPNAAuthCookie(String cookie)
	{
		PNAAuthCookie = cookie;
		PNACookieHasBeenSet = true;
	}
	public static void setBBAuthCookie(String cookie)
	{
		bbAuthCookie = cookie;
		bbCookieHasBeenSet = true;
	}
	public static void resetPNACookie()
	{
		PNAAuthCookie = "";
		PNACookieHasBeenSet = false;
		PNALoggedIn = false;
	}
	public static void resetCookies()
	{
		authCookie = "";
		PNAAuthCookie = "";
		bbAuthCookie="";
		bbCookieHasBeenSet = false;
		PNACookieHasBeenSet = false;
		cookieHasBeenSet = false;
		PNALoggedIn = false;
		loggedIn = false;
		bbLoggedIn = false;
		
	}
	public static boolean cookieHasBeenSet()
	{
		return cookieHasBeenSet;
	}
	public static boolean PNACookieHasBeenSet()
	{
		return PNACookieHasBeenSet;
	}
	public static boolean bbCookieHasBeenSet()
	{
		return bbCookieHasBeenSet;
	}
	public class loginTask extends AsyncTask<Object,Integer,Boolean>
	{
		
		DefaultHttpClient pnahttpclient;
		DefaultHttpClient httpclient;
		Editor edit;
		Context context;
    	
    	public loginTask(Context con, DefaultHttpClient httpclient, DefaultHttpClient pnahttpclient)
		{
    		settings = PreferenceManager.getDefaultSharedPreferences(con);
    		this.httpclient = httpclient;
			this.pnahttpclient = pnahttpclient;
			edit = settings.edit();
			this.context = con;
			
		}
    	@Override
    	protected Boolean doInBackground(Object... params)
		{
			loggingIn=true;
    		boolean loginStatus = ((ConnectionHelper)params[0]).Login(context, (DefaultHttpClient)httpclient);
			publishProgress(loginStatus?0:1);
			return loginStatus;		
		}
    	@Override
		protected void onProgressUpdate(Integer... progress)
		{
			
    		switch(progress[0])
			{
    		case 1:
    			Toast.makeText(context, "There was an error while connecting to UT's web services, please check your internet connection and try again", Toast.LENGTH_LONG).show();
    			loggingIn=false;
    			((SherlockActivity)(context)).invalidateOptionsMenu();
    			cancelProgressBar();
    			
				break;
    		case 0:break;
			}
		}
		@Override
		protected void onPostExecute(Boolean b)
		{
			logindone = b;
			
			if(logindone && pnalogindone && bbLoginDone && !isCancelled())  
			{
				logindone = false;pnalogindone = false;bbLoginDone=false;
				loggingIn=false;
				
				if(!ConnectionHelper.getAuthCookie(context, httpclient).equals("") && !ConnectionHelper.getPNAAuthCookie(context, pnahttpclient).equals("") && !ConnectionHelper.getBBAuthCookie(context, httpclient).equals(""))
				 {
					Toast.makeText(context, "You're now logged in; feel free to access any of the app's features", Toast.LENGTH_LONG).show();
					
					edit.putBoolean("loggedin", true);
					Utility.commit(edit);
					
				 }
				((SherlockActivity)(context)).invalidateOptionsMenu();
				cancelProgressBar();
				Crittercism.leaveBreadcrumb("Logged in (persistent)");
			}
		}
		private void cancelProgressBar()
		{
			if(context.getClass().equals(UTilitiesActivity.class))
				((SherlockActivity)context).setSupportProgressBarIndeterminateVisibility(false);
			else if(context.getClass().equals(Preferences.class))
				((SherlockPreferenceActivity)context).setSupportProgressBarIndeterminateVisibility(false);
		}
		
	}
    public class PNALoginTask extends AsyncTask<Object,Integer,Boolean>
	{
    	
		DefaultHttpClient pnahttpclient;
		DefaultHttpClient httpclient;
		Editor edit;
		Context context;
    	
    	public PNALoginTask(Context con, DefaultHttpClient httpclient, DefaultHttpClient pnahttpclient)
		{
    		settings = PreferenceManager.getDefaultSharedPreferences(con);
    		this.httpclient = httpclient;
			this.pnahttpclient = pnahttpclient;
			this.context = con;
			edit = settings.edit();
		}
		@Override
		protected void onProgressUpdate(Integer... progress)
		{
    		switch(progress[0])
			{
    			case 1:
    //				Toast.makeText(context, "There was an error while connecting to UT PNA, please check your internet connection and try again", Toast.LENGTH_LONG).show();
    				loggingIn=false;
    				((SherlockActivity)(context)).invalidateOptionsMenu();
    				cancelProgressBar();
    				
    				break;
    			case 0:break;
			}
		}	
		protected Boolean doInBackground(Object... params)
		{
			loggingIn = true;
			boolean pnaLoginStatus = ((ConnectionHelper)params[0]).PNALogin(context, (DefaultHttpClient)pnahttpclient);
			publishProgress(pnaLoginStatus?0:1);
			return pnaLoginStatus;
		}
		@Override
		protected void onPostExecute(Boolean b)
		{
			pnalogindone = b;
			
			if(logindone && pnalogindone && bbLoginDone && !isCancelled())
			{
				logindone = false;pnalogindone = false;bbLoginDone=false;
				loggingIn=false;
				
				if(!ConnectionHelper.getAuthCookie(context, httpclient).equals("") && !ConnectionHelper.getPNAAuthCookie(context, pnahttpclient).equals("") && !ConnectionHelper.getBBAuthCookie(context, httpclient).equals(""))
				 {
					Toast.makeText(context, "You're now logged in; feel free to access any of the app's features", Toast.LENGTH_LONG).show();
					
					edit.putBoolean("loggedin", true);
					Utility.commit(edit);
					
				 }
				((SherlockActivity)(context)).invalidateOptionsMenu();
				cancelProgressBar();
				Crittercism.leaveBreadcrumb("Logged in (persistent)");
			}
		}
		private void cancelProgressBar()
		{
			if(context.getClass().equals(UTilitiesActivity.class))
				((SherlockActivity)context).setSupportProgressBarIndeterminateVisibility(false);
			else if(context.getClass().equals(Preferences.class))
				((SherlockPreferenceActivity)context).setSupportProgressBarIndeterminateVisibility(false);
		}	
	}
    public class bbLoginTask extends AsyncTask<Object,Integer,Boolean>
	{
		
		DefaultHttpClient pnahttpclient;
		DefaultHttpClient httpclient;
		Editor edit;
		Context context;
    	
    	public bbLoginTask(Context con, DefaultHttpClient httpclient, DefaultHttpClient pnahttpclient)
		{
    		settings = PreferenceManager.getDefaultSharedPreferences(con);
    		this.httpclient = httpclient;
			this.pnahttpclient = pnahttpclient;
			edit = settings.edit();
			this.context = con;
			
		}
    	@Override
    	protected Boolean doInBackground(Object... params)
		{
			loggingIn=true;
    		boolean loginStatus = ((ConnectionHelper)params[0]).bbLogin(context, (DefaultHttpClient)httpclient);
			publishProgress(loginStatus?0:1);
			return loginStatus;		
		}
    	@Override
		protected void onProgressUpdate(Integer... progress)
		{			
    		switch(progress[0])
			{
    		case 1:
 //   			Toast.makeText(context, "There was an error while connecting to Blackboard, please check your internet connection and try again", Toast.LENGTH_LONG).show();
    			loggingIn=false;
    			((SherlockActivity)(context)).invalidateOptionsMenu();
    			cancelProgressBar();
				break;
				
    		case 0:break;
			}
		}
		@Override
		protected void onPostExecute(Boolean b)
		{
			bbLoginDone = b;
			
			if(logindone && pnalogindone && bbLoginDone && !isCancelled())
			{
				logindone = false;pnalogindone = false;bbLoginDone=false;
				loggingIn=false;
				
				if(!ConnectionHelper.getAuthCookie(context, httpclient).equals("") && !ConnectionHelper.getPNAAuthCookie(context, pnahttpclient).equals("") && !ConnectionHelper.getBBAuthCookie(context, httpclient).equals(""))
				 {
					Toast.makeText(context, "You're now logged in; feel free to access any of the app's features", Toast.LENGTH_LONG).show();
					
					edit.putBoolean("loggedin", true);
					Utility.commit(edit);
					
				 }
				((SherlockActivity)(context)).invalidateOptionsMenu();
				cancelProgressBar();
				Crittercism.leaveBreadcrumb("Logged in (persistent)");
			}
		}
		private void cancelProgressBar()
		{
			if(context.getClass().equals(UTilitiesActivity.class))
				((SherlockActivity)context).setSupportProgressBarIndeterminateVisibility(false);
			else if(context.getClass().equals(Preferences.class))
				((SherlockPreferenceActivity)context).setSupportProgressBarIndeterminateVisibility(false);
		}
		
	}
}

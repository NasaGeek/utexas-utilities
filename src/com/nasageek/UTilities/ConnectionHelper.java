package com.nasageek.UTilities;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class ConnectionHelper {
	
	private HttpPost httppost;
	private DefaultHttpClient httpclient;
	private static SharedPreferences settings;
	private static Context mContext;
	public static String authCookie;
	public static String PNAAuthCookie;
	private static boolean PNACookieHasBeenSet = false;
	private static boolean cookieHasBeenSet = false;
	private static boolean PNALoggedIn = false;
	private static boolean loggedIn = false;
	
	public ConnectionHelper(Context c)
	{
		mContext = c;
		
	}
	public static DefaultHttpClient getThreadSafeClient() {

	    DefaultHttpClient client = new DefaultHttpClient();
	    ClientConnectionManager mgr = client.getConnectionManager();
	    HttpParams params = client.getParams();

	    client = new DefaultHttpClient(
	        new ThreadSafeClientConnManager(params,
	            mgr.getSchemeRegistry()), params);
	    
	    return client;
	}
	public boolean Login(Context con, DefaultHttpClient client)
	{
		settings = PreferenceManager.getDefaultSharedPreferences(con);
		client.getCookieStore().clear();
		httppost = new HttpPost("https://utdirect.utexas.edu/security-443/logon_check.logonform");
		try {
		       
			 // Add your data
		    	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		
		        nameValuePairs.add(new BasicNameValuePair("LOGON", settings.getString("eid","error").trim()));
		        nameValuePairs.add(new BasicNameValuePair("PASSWORDS", settings.getString("password","error")));
		        
		        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,HTTP.ASCII));

		        // Execute HTTP Post Request
		        HttpResponse response = client.execute(httppost);

		   
		    } catch (IOException ex) {
		    	
		    	loggedIn = false;
		    	Toast.makeText(mContext, "There was an error while connecting to UTDirect, please check your internet connection and try again", Toast.LENGTH_LONG).show();
		//        Log.e("Error connecting to utexas.edu", ex.toString());// TODO Auto-generated catch block	
		        return false;
		    }
		    
		   loggedIn = true; 
		   return true;
		
	}
	public static void logout(Context con)
	{
		getThreadSafeClient().getCookieStore().clear();
		resetCookies();
		new ClassDatabase(con).deleteDb();
		
	}
	public boolean PNALogin(Context con, DefaultHttpClient client)
	{
		settings = PreferenceManager.getDefaultSharedPreferences(con);
		client.getCookieStore().clear();
		httppost = new HttpPost("https://management.pna.utexas.edu/server/graph.cgi");
		try {
		       
			 // Add your data
		    	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		
		        nameValuePairs.add(new BasicNameValuePair("PNACLOGINusername", settings.getString("eid","error").trim()));
		        nameValuePairs.add(new BasicNameValuePair("PNACLOGINpassword", settings.getString("password","error")));
		        
		        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,HTTP.ASCII));

		        // Execute HTTP Post Request
		        HttpResponse response = client.execute(httppost);
		        
		    } catch (Exception ex) {
		    	
		    	PNALoggedIn = false;
		    	Toast.makeText(mContext, "There was an error while connecting to UT PNA, please check your internet connection and try again", Toast.LENGTH_LONG).show();
		//        Log.e("Error connecting to utexas.edu", ex.toString());// TODO Auto-generated catch block	
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
				return "";
		}
		else
		{
			return authCookie;
		}
	}
	public static void resetCookie()
	{
		authCookie = "";
		loggedIn = false;
		cookieHasBeenSet = false;
	}
	public static void setAuthCookie(String cookie)
	{
		authCookie = cookie;
		cookieHasBeenSet = true;
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
		PNACookieHasBeenSet = false;
		PNALoggedIn = false;
		loggedIn = false;
		cookieHasBeenSet = false;
	}
	public static boolean cookieHasBeenSet()
	{
		return cookieHasBeenSet;
	}
	public static boolean PNACookieHasBeenSet()
	{
		return PNACookieHasBeenSet;
	}
}

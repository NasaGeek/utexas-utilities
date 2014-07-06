
package com.nasageek.utexasutilities;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpParams;

import android.content.Context;
public class ConnectionHelper {

    public static final String BLACKBOARD_DOMAIN_NOPROT = "blackboard.utexas.edu";
    public static final String BLACKBOARD_DOMAIN = "https://" + BLACKBOARD_DOMAIN_NOPROT;

    public static DefaultHttpClient getThreadSafeClient() {

        DefaultHttpClient client = new DefaultHttpClient();
        ClientConnectionManager mgr = client.getConnectionManager();
        HttpParams params = client.getParams();

        client = new DefaultHttpClient(new ThreadSafeClientConnManager(params,
                mgr.getSchemeRegistry()), params);

        return client;
    }

    public static void logout(Context con) {
    }
}

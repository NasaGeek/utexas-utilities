package com.nasageek.utexasutilities;

import com.nasageek.utexasutilities.model.LoadFailedEvent;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * Created by chris on 2/7/16.
 */
public abstract class UTLoginTask<Params, Progress, Result> extends
        TaggedAsyncTask<Params, Progress, Result> {

    protected final OkHttpClient client = UTilitiesApplication.getInstance().getHttpClient();
    protected String reqUrl;
    protected String errorMsg;

    public UTLoginTask(String tag, String reqUrl) {
        super(tag);
        this.reqUrl = reqUrl;
    }

    protected String fetchData(Request request) throws IOException, NotAuthenticatedException {
        Response response = client.newCall(request).execute();
        String pagedata = response.body().string();

        // did we hit the login screen?
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
            throw new NotAuthenticatedException();
        }
        return pagedata;
    }

    @Override
    protected void onCancelled(Result result) {
        super.onCancelled(result);
        MyBus.getInstance().post(new LoadFailedEvent(getTag(), errorMsg));
    }
}


package com.nasageek.utexasutilities.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.nasageek.utexasutilities.AsyncTask;
import com.nasageek.utexasutilities.AuthCookie;
import com.nasageek.utexasutilities.ChangeLog;
import com.nasageek.utexasutilities.ChangeableContextTask;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.SecurePreferences;
import com.nasageek.utexasutilities.UTilitiesApplication;
import com.nasageek.utexasutilities.Utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static com.nasageek.utexasutilities.UTilitiesApplication.BB_AUTH_COOKIE_KEY;
import static com.nasageek.utexasutilities.UTilitiesApplication.PNA_AUTH_COOKIE_KEY;
import static com.nasageek.utexasutilities.UTilitiesApplication.UTD_AUTH_COOKIE_KEY;

/**
 * Main entry point for UTilities. Allows the user to log in and contains a dashboard
 * of buttons to launch UT web services.
 */
public class UTilitiesActivity extends SherlockActivity {

    private final static int BUTTON_ANIMATION_DURATION = 90;

    private final static String SECURE_PREF_PW_KEY = "com.nasageek.utexasutilities.password";

    private SharedPreferences settings;
    private Toast message;
    private ImageView scheduleCheck, balanceCheck, dataCheck, blackboardCheck;
    private AlertDialog nologin;

    private AuthCookie authCookies[];
    private List<AsyncTask> loginTasks;
    private UpdateUiTask updateUiTask;
    private AuthCookie utdAuthCookie;
    private AuthCookie pnaAuthCookie;
    private AuthCookie bbAuthCookie;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UTilitiesApplication mApp = (UTilitiesApplication) getApplication();
        utdAuthCookie = mApp.getAuthCookie(UTD_AUTH_COOKIE_KEY);
        pnaAuthCookie = mApp.getAuthCookie(PNA_AUTH_COOKIE_KEY);
        bbAuthCookie = mApp.getAuthCookie(BB_AUTH_COOKIE_KEY);
        authCookies = new AuthCookie[]{utdAuthCookie, pnaAuthCookie, bbAuthCookie};

        loginTasks = (List<AsyncTask>) getLastNonConfigurationInstance();
        if (loginTasks != null) {
            updateUiTask = (UpdateUiTask) loginTasks.get(0);
            if (updateUiTask != null) {
                updateUiTask.setContext(this);
            }
        }

        settings = PreferenceManager.getDefaultSharedPreferences(this.getBaseContext());
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.main);
        setSupportProgressBarIndeterminateVisibility(isLoggingIn());

        // use one Activity-wide Toast so they don't stack up
        message = Toast.makeText(this, R.string.login_first, Toast.LENGTH_SHORT);

        handleUnencryptedPassword();
        handleFirstLaunch();
        if (settings.getBoolean("autologin", false) && !isLoggingIn() && !mApp.allCookiesSet()) {
            login();
        }
        setupDashBoardButtons();
    }

    /**
     * Set up the dashboard of buttons to launch the various services of the
     * app. This covers things like touch/focus listeners, authentication,
     * intent to launch, etc.
     */
    private void setupDashBoardButtons() {
        scheduleCheck = (ImageView) findViewById(R.id.scheduleCheck);
        balanceCheck = (ImageView) findViewById(R.id.balanceCheck);
        dataCheck = (ImageView) findViewById(R.id.dataCheck);
        blackboardCheck = (ImageView) findViewById(R.id.blackboardCheck);

        final Intent schedule = new Intent(this, ScheduleActivity.class);
        final Intent balance = new Intent(this, BalanceActivity.class);
        final Intent map = new Intent(this, CampusMapActivity.class);
        final Intent data = new Intent(this, DataUsageActivity.class);
        final Intent menu = new Intent(this, MenuActivity.class);
        final Intent blackboard = new Intent(this, BlackboardPanesActivity.class);

        // simple struct-like class to help handle related data
        class DashboardButtonData {
            public Intent intent;
            public int imageButtonId;
            public AuthCookie authCookie;
            public Character serviceChar;
            public ImageView checkOverlay;

            // for authenticated services
            public DashboardButtonData(Intent intent, int id, AuthCookie authCookie,
                                       Character service, ImageView check) {
                this.intent = intent;
                this.imageButtonId = id;
                this.authCookie = authCookie;
                this.serviceChar = service;
                this.checkOverlay = check;
            }

            // for unauthenticated services
            public DashboardButtonData(Intent intent, int id) {
                this(intent, id, null, null, null);
            }
        }

        DashboardButtonData buttonData[] = new DashboardButtonData[6];
        buttonData[0] = new DashboardButtonData(schedule, R.id.schedule_button, utdAuthCookie, 'u',
                scheduleCheck);
        buttonData[1] = new DashboardButtonData(balance, R.id.balance_button, utdAuthCookie, 'u',
                balanceCheck);
        buttonData[2] = new DashboardButtonData(blackboard, R.id.blackboard_button, bbAuthCookie,
                'b', blackboardCheck);
        buttonData[3] = new DashboardButtonData(data, R.id.data_button, pnaAuthCookie, 'p',
                dataCheck);
        buttonData[4] = new DashboardButtonData(map, R.id.map_button);
        buttonData[5] = new DashboardButtonData(menu, R.id.menu_button);

        for (int i = 0; i < 6; i++) {
            ImageButton ib = (ImageButton) findViewById(buttonData[i].imageButtonId);
            ib.setOnTouchListener(new ImageButtonTouchListener(
                    (TransitionDrawable) ib.getDrawable()));
            ib.setOnFocusChangeListener(new ImageButtonFocusListener());
            ib.setTag(buttonData[i]);
            ib.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DashboardButtonData data = (DashboardButtonData) v.getTag();
                    // null cookie means the service doesn't need an EID
                    if (data.authCookie == null) {
                        startActivity(data.intent);
                    } else {
                        if (settings.getBoolean("loginpref", false)) {
                            // persistent login
                            if (!data.authCookie.hasCookieBeenSet() || isLoggingIn()) {
                                showLoginFirstToast();
                            } else {
                                startActivity(data.intent);
                            }
                        } else {
                            // temp login
                            if (!data.authCookie.hasCookieBeenSet()) {
                                Intent login = new Intent(UTilitiesActivity.this,
                                        LoginActivity.class);
                                login.putExtra("activity", data.intent.getComponent()
                                        .getClassName());
                                login.putExtra("service", data.serviceChar);
                                startActivity(login);
                            } else {
                                startActivity(data.intent);
                            }
                        }
                    }
                }
            });
        }
    }

    /**
     * FocusListener that triggers a TransitionDrawable when the item goes
     * in and out of focus.
     */
    private class ImageButtonFocusListener implements OnFocusChangeListener {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                ((TransitionDrawable) ((ImageButton) v).getDrawable())
                        .startTransition(BUTTON_ANIMATION_DURATION);
            } else {
                ((TransitionDrawable) ((ImageButton) v).getDrawable())
                        .reverseTransition(BUTTON_ANIMATION_DURATION);
            }

        }
    }

    /**
     * TouchListener that triggers a TransitionDrawable on touch and release.
     */
    private class ImageButtonTouchListener implements OnTouchListener {
        private boolean buttonPressed = false;
        private TransitionDrawable crossfade;

        public ImageButtonTouchListener(TransitionDrawable transDrawable) {
            crossfade = transDrawable;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    if (!buttonPressed) {
                        buttonPressed = true;
                        crossfade.startTransition(BUTTON_ANIMATION_DURATION);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (buttonPressed) {
                        buttonPressed = false;
                        crossfade.reverseTransition(BUTTON_ANIMATION_DURATION);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    final int[] states = v.getDrawableState();
                    boolean pressedStateFound = false;
                    for (int state : states) {
                        if (state == android.R.attr.state_pressed) {
                            pressedStateFound = true;
                            if (!buttonPressed) {
                                buttonPressed = true;
                                crossfade.startTransition(BUTTON_ANIMATION_DURATION);
                            }
                            break;
                        }
                    }
                    if (!pressedStateFound && buttonPressed) {
                        buttonPressed = false;
                        crossfade.reverseTransition(BUTTON_ANIMATION_DURATION);
                    }
                    break;
            }
            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = this.getSupportMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        boolean allLoggedIn = true;
        boolean anyLoggedIn = false;
        for (AuthCookie cookie : authCookies) {
            if (!cookie.hasCookieBeenSet()) {
                allLoggedIn = false;
            } else {
                anyLoggedIn = true;
            }
        }

        // update the displayed login state
        if (settings.getBoolean("loginpref", false)) {
            if (!isLoggingIn()) {
                if (allLoggedIn) {
                    replaceLoginButton(menu, R.id.logout_button, "Log out");
                } else {
                    replaceLoginButton(menu, R.id.login_button, "Log in");
                }
            } else {
                replaceLoginButton(menu, R.id.cancel_button, "Cancel");
            }
        } else {
            if (anyLoggedIn) {
                replaceLoginButton(menu, R.id.logout_button, "Log out");
            } else {
                menu.removeGroup(R.id.login_menu_group);
            }
        }
        return true;
    }

    /**
     * Replace current login button with another (typically "Log-in", "Cancel", or "Log out")
     * @param menu menu to add to
     * @param id id of button being added
     * @param text text of the button being added
     */
    private void replaceLoginButton(Menu menu, int id, String text) {
        menu.removeGroup(R.id.login_menu_group);
        menu.add(R.id.login_menu_group, id, Menu.NONE, text);
        MenuItem item = menu.findItem(id);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.settings:
                loadSettings();
                break;
            case R.id.login_button:
                login();
                invalidateOptionsMenu();
                break;
            case R.id.logout_button:
                logout();
                invalidateOptionsMenu();
                break;
            case R.id.cancel_button:
                cancelLogin();
                invalidateOptionsMenu();
                break;
        }
        return true;
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return loginTasks;
    }

    /**
     * Show "first launch" dialogs. Either the changelog for the first launch after an update
     * or the actual first-launch dialog letting the user known they should save their
     * credentials to get full use of the app.
     */
    private void handleFirstLaunch() {
        if (!settings.contains("firstRun")) {
            // first launch ever
            AlertDialog.Builder nologin_builder = new AlertDialog.Builder(this);
            nologin_builder
                    .setMessage(
                            "This is your first time running UTilities; why don't you try" +
                                    " logging in to get the most use out of the app?")
                    .setCancelable(false)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            loadSettings();
                        }
                    })
                    .setNegativeButton("No thanks", null);
            nologin = nologin_builder.create();
            nologin.show();
            Utility.commit(settings.edit().putBoolean("firstRun", false));
            Utility.id(this);
        } else {
            ChangeLog cl = new ChangeLog(this);
            if (cl.isFirstRun()) {
                // first launch after an update
                // this will actually show after the second launch if it's a fresh install
                cl.getFullLogDialog().show();
            }
        }
    }

    /**
     * Legacy method, probably not used anymore.
     * Removes any unencrypted password and stores a boolean so this doesn't happen again
     * and shows the user a dialog informing them of what's happened.
     */
    private void handleUnencryptedPassword() {
        if (!settings.contains("encryptedpassword") && settings.contains("firstRun")
                && settings.contains("password")) {
            Utility.commit(settings.edit().remove("password"));
            Utility.commit(settings.edit().putBoolean("encryptedpassword", true));
            // turn off autologin, they can re-activate it when they enter their password
            Utility.commit(settings.edit().putBoolean("autologin", false));
            showUnencryptedPasswordDialog();
        }
    }


    /**
     * Show dialog informing the user that their password has been reset.
     */
    private void showUnencryptedPasswordDialog() {
        AlertDialog.Builder passwordcleared_builder = new AlertDialog.Builder(this);
        passwordcleared_builder
                .setMessage(
                        "With this update to UTilities, your stored password will be " +
                                "encrypted. Your currently stored password has been wiped " +
                                "for security purposes and you will need to re-enter it.")
                .setCancelable(true)
                .setPositiveButton("Ok", null);
        AlertDialog passwordcleared = passwordcleared_builder.create();
        passwordcleared.show();
    }

    private void loadSettings() {
        final Intent pref_intent = new Intent(this, Preferences.class);
        startActivity(pref_intent);
    }

    private boolean isLoggingIn() {
        // updateUiTask is null before login has begun
        return updateUiTask != null && updateUiTask.getStatus() == AsyncTask.Status.RUNNING &&
                !updateUiTask.isCancelled();
    }

    /**
     * Call login on the given AuthCookie and decrement the given CountDownLatch afterwards.
     * This doesn't really need to be an AsyncTask, that's just what I'm most familiar with.
     */
    static class LoginTask extends AsyncTask<AuthCookie, Void, Void> {

        private CountDownLatch loginLatch;

        public LoginTask(CountDownLatch loginLatch) {
            this.loginLatch = loginLatch;
        }

        @Override
        protected Void doInBackground(AuthCookie... params) {
            try {
                /*
                We can ignore the return value of login() because UpdateUITask ensures all of the
                cookies have been set before completing the login.
                  */
                params[0].login();
            } catch (IOException e) {
                /*
                TODO: Inform the user that the login request failed due to a network error.
                This will require a change to AuthCookie or some other sort of shared state
                between LoginTask and UpdateUITask.
                 */
                e.printStackTrace();
            }
            loginLatch.countDown();
            return null;
        }
    }

    /**
     * AsyncTask that waits on the given CountDownLatch to deplete. It then checks to see if all
     * of the Activity's AuthCookies have been set and changes the UI accordingly.
     */
    static class UpdateUiTask extends AsyncTask<CountDownLatch, Void, Void> implements
                                                                            ChangeableContextTask {

        private UTilitiesActivity mActivity;

        public UpdateUiTask(UTilitiesActivity act) {
            mActivity = act;
        }

        @Override
        protected Void doInBackground(CountDownLatch... params) {
            CountDownLatch loginLatch = params[0];
            try {
                loginLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            for (AuthCookie cookie : mActivity.authCookies) {
                if (!cookie.hasCookieBeenSet()) {
                    ((UTilitiesApplication) mActivity.getApplication()).logoutAll();
                    Toast.makeText(mActivity, "Login failed", Toast.LENGTH_SHORT).show();
                    break;
                }
            }
            mActivity.invalidateOptionsMenu();
            mActivity.setSupportProgressBarIndeterminateVisibility(false);
        }

        @Override
        public void setContext(Context con) {
            mActivity = (UTilitiesActivity) con;
        }
    }

    /**
     * Perform a login with the user's saved credentials.
     */
    private void login() {
        SecurePreferences sp = new SecurePreferences(UTilitiesActivity.this, SECURE_PREF_PW_KEY, false);
        if (settings.getBoolean("loginpref", false)) {
            if (!settings.contains("eid") || !sp.containsKey("password")
                    || settings.getString("eid", "error").equals("")
                    || sp.getString("password").equals("")) {
                message.setText("Please enter your credentials to log in");
                message.setDuration(Toast.LENGTH_LONG);
                message.show();
            } else {
                setSupportProgressBarIndeterminateVisibility(true);
                loginTasks = new ArrayList<AsyncTask>();
                CountDownLatch loginLatch = new CountDownLatch(authCookies.length);
                updateUiTask = new UpdateUiTask(this);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    updateUiTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, loginLatch);
                } else {
                    updateUiTask.execute(loginLatch);
                }
                loginTasks.add(updateUiTask);

                for (AuthCookie cookie : authCookies) {
                    LoginTask loginTask = new LoginTask(loginLatch);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        loginTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, cookie);
                    } else {
                        loginTask.execute(cookie);
                    }
                    loginTasks.add(loginTask);
                }
            }
        } else {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
        }
    }

    /**
     * Cancel the login process.
     */
    private void cancelLogin() {
        for (AsyncTask task : loginTasks) {
            task.cancel(true);
        }
        logout();
        setSupportProgressBarIndeterminateVisibility(false);
    }

    /**
     * Log the user out of all UT web services.
     */
    private void logout() {
        for (AuthCookie cookie : authCookies) {
            cookie.logout();
        }
        resetChecks();
    }

    private void showLoginFirstToast() {
        message.setText(R.string.login_first);
        message.setDuration(Toast.LENGTH_SHORT);
        message.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        invalidateOptionsMenu();
        resetChecks();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (nologin != null) {
            if (nologin.isShowing()) {
                nologin.dismiss();
            }
        }
    }

    /**
     * Reset the checkmark overlays that act as temp login indicators to
     * ensure they show the correct login status.
     */
    private void resetChecks() {
        if (settings.getBoolean("loginpref", false)) {
            scheduleCheck.setVisibility(View.GONE);
            balanceCheck.setVisibility(View.GONE);
            dataCheck.setVisibility(View.GONE);
            blackboardCheck.setVisibility(View.GONE);
        } else {
            if (!utdAuthCookie.hasCookieBeenSet()) {
                scheduleCheck.setImageResource(R.drawable.ic_done_translucent);
                balanceCheck.setImageResource(R.drawable.ic_done_translucent);
            } else {
                scheduleCheck.setImageResource(R.drawable.ic_done);
                balanceCheck.setImageResource(R.drawable.ic_done);
            }
            if (!bbAuthCookie.hasCookieBeenSet()) {
                blackboardCheck.setImageResource(R.drawable.ic_done_translucent);
            } else {
                blackboardCheck.setImageResource(R.drawable.ic_done);
            }
            if (!pnaAuthCookie.hasCookieBeenSet()) {
                dataCheck.setImageResource(R.drawable.ic_done_translucent);
            } else {
                dataCheck.setImageResource(R.drawable.ic_done);
            }
            scheduleCheck.setVisibility(View.VISIBLE);
            balanceCheck.setVisibility(View.VISIBLE);
            dataCheck.setVisibility(View.VISIBLE);
            blackboardCheck.setVisibility(View.VISIBLE);
        }
    }
}

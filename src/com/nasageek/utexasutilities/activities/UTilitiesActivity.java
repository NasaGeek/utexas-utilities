
package com.nasageek.utexasutilities.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.nasageek.utexasutilities.AnalyticsHandler;
import com.nasageek.utexasutilities.AsyncTask;
import com.nasageek.utexasutilities.AuthCookie;
import com.nasageek.utexasutilities.ChangeLogCompat;
import com.nasageek.utexasutilities.ChangeableContextTask;
import com.nasageek.utexasutilities.MyBus;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.UTilitiesApplication;
import com.nasageek.utexasutilities.Utility;
import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static com.nasageek.utexasutilities.UTilitiesApplication.PNA_AUTH_COOKIE_KEY;
import static com.nasageek.utexasutilities.UTilitiesApplication.UTD_AUTH_COOKIE_KEY;

/**
 * Main entry point for UTilities. Allows the user to log in and contains a dashboard
 * of buttons to launch UT web services.
 */
public class UTilitiesActivity extends BaseActivity {

    private static final float CHECK_TRANSLUCENT_OPACITY = 0.3f;
    private final static int BUTTON_ANIMATION_DURATION = 90;

    private SharedPreferences settings;
    private Toast message;
    private ImageView scheduleCheck, balanceCheck, dataCheck;
    private ProgressBar scheduleProgress, balanceProgress, dataProgress;
    private AlertDialog nologin;

    private AuthCookie authCookies[];
    private List<AsyncTask> loginTasks;
    private UpdateUiTask updateUiTask;
    private AuthCookie utdAuthCookie;
    private AuthCookie pnaAuthCookie;

    private HashMap<String, ImageView[]> cookiesToFeatures;
    private HashMap<String, Boolean> serviceLoggedIn;
    private ImageView[] featureButtons;
    private View.OnClickListener enabledFeatureButtonListener;
    private View.OnClickListener disabledFeatureButtonListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UTilitiesApplication mApp = (UTilitiesApplication) getApplication();
        utdAuthCookie = mApp.getAuthCookie(UTD_AUTH_COOKIE_KEY);
        pnaAuthCookie = mApp.getAuthCookie(PNA_AUTH_COOKIE_KEY);
        authCookies = new AuthCookie[]{utdAuthCookie, pnaAuthCookie};

        loginTasks = (List<AsyncTask>) getLastCustomNonConfigurationInstance();
        if (loginTasks != null) {
            updateUiTask = (UpdateUiTask) loginTasks.get(0);
            if (updateUiTask != null) {
                updateUiTask.setContext(this);
            }
        }
        if (savedInstanceState != null) {
            serviceLoggedIn = (HashMap) savedInstanceState.getSerializable("serviceLoggedIn");
        } else {
            serviceLoggedIn = new HashMap<>();
            serviceLoggedIn.put(UTD_AUTH_COOKIE_KEY, true);
            serviceLoggedIn.put(PNA_AUTH_COOKIE_KEY, true);
        }

        settings = PreferenceManager.getDefaultSharedPreferences(this.getBaseContext());
        setContentView(R.layout.main);
        enabledFeatureButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DashboardButtonData data = (DashboardButtonData) v.getTag();
                // null cookie means the service doesn't need an EID
                if (data.authCookie == null) {
                    startActivity(data.intent);
                } else {
                    if (settings.getBoolean(getString(R.string.pref_logintype_key), false)) {
                        // persistent login
                        if ((!data.authCookie.hasCookieBeenSet() || isLoggingIn())
                                && isLoginRequired()) {
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
        };
        disabledFeatureButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                AlertDialog.Builder featureDisabledBuilder =
                        new AlertDialog.Builder(UTilitiesActivity.this);
                featureDisabledBuilder
                        .setMessage("This feature has been disabled due to a failed login, would " +
                                "you like to try logging in again?")
                        .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                ((DashboardButtonData) v.getTag()).loginProgress
                                        .setVisibility(View.VISIBLE);
                                loginTasks = new ArrayList<>();
                                CountDownLatch loginLatch = new CountDownLatch(1);
                                updateUiTask = new UpdateUiTask(UTilitiesActivity.this);
                                Utility.parallelExecute(updateUiTask, loginLatch);
                                loginTasks.add(updateUiTask);

                                AuthCookie cookie =
                                        ((DashboardButtonData) v.getTag()).authCookie;
                                LoginTask loginTask = new LoginTask(loginLatch);
                                Utility.parallelExecute(loginTask, cookie);
                                loginTasks.add(loginTask);
                            }
                        })
                        .setNegativeButton("Cancel", null);
                AlertDialog featureDisabled = featureDisabledBuilder.create();
                featureDisabled.show();
            }
        };
        setupDashBoardButtons();
        setLoginProgressBarVisiblity(isLoggingIn());

        // use one Activity-wide Toast so they don't stack up
        message = Toast.makeText(this, R.string.login_first, Toast.LENGTH_SHORT);

        handleUnencryptedPassword();
        handleFirstLaunch();
        if (settings.getBoolean("autologin", false) && !isLoggingIn() && !mApp.anyCookiesSet()) {
            login();
        }

        MyBus.getInstance().register(this);
    }

    // simple struct-like class to help handle related data
    class DashboardButtonData {
        public Intent intent;
        public int imageButtonId;
        public AuthCookie authCookie;
        public Character serviceChar;
        public ImageView checkOverlay;
        public ProgressBar loginProgress;
        public boolean loggedIn;

        // for authenticated services
        public DashboardButtonData(Intent intent, int id, AuthCookie authCookie,
                                   Character service, ImageView check, ProgressBar progress,
                                   boolean loggedIn) {
            this.intent = intent;
            this.imageButtonId = id;
            this.authCookie = authCookie;
            this.serviceChar = service;
            this.checkOverlay = check;
            this.loginProgress = progress;
            this.loggedIn = loggedIn;
        }

        // for unauthenticated services
        public DashboardButtonData(Intent intent, int id) {
            this(intent, id, null, null, null, null, true);
        }
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

        scheduleProgress = (ProgressBar) findViewById(R.id.scheduleProgress);
        balanceProgress = (ProgressBar) findViewById(R.id.balanceProgress);
        dataProgress = (ProgressBar) findViewById(R.id.dataProgress);

        final Intent schedule = new Intent(this, ScheduleActivity.class);
        final Intent balance = new Intent(this, BalanceActivity.class);
        final Intent map = new Intent(this, CampusMapActivity.class);
        final Intent data = new Intent(this, DataUsageActivity.class);
        final Intent menu = new Intent(this, MenuActivity.class);



        DashboardButtonData buttonData[] = new DashboardButtonData[6];
        buttonData[0] = new DashboardButtonData(schedule, R.id.schedule_button, utdAuthCookie, 'u',
                scheduleCheck, scheduleProgress, serviceLoggedIn.get(UTD_AUTH_COOKIE_KEY));
        buttonData[1] = new DashboardButtonData(balance, R.id.balance_button, utdAuthCookie, 'u',
                balanceCheck, balanceProgress, serviceLoggedIn.get(UTD_AUTH_COOKIE_KEY));
        buttonData[2] = new DashboardButtonData(data, R.id.data_button, pnaAuthCookie, 'p',
                dataCheck, dataProgress, serviceLoggedIn.get(PNA_AUTH_COOKIE_KEY));
        buttonData[3] = new DashboardButtonData(map, R.id.map_button);
        buttonData[4] = new DashboardButtonData(menu, R.id.menu_button);

        featureButtons = new ImageView[5];
        for (int i = 0; i < 5; i++) {
            ImageView ib = (ImageView) findViewById(buttonData[i].imageButtonId);
            ib.setOnTouchListener(new ImageButtonTouchListener(
                    (TransitionDrawable) ib.getDrawable()));
            ib.setOnFocusChangeListener(new ImageButtonFocusListener());
            ib.setTag(buttonData[i]);
            if (buttonData[i].loggedIn) {
                enableFeature(ib);
            } else {
                disableFeature(ib);
            }
            featureButtons[i] = ib;
        }
        cookiesToFeatures = new HashMap<>();
        cookiesToFeatures.put(UTD_AUTH_COOKIE_KEY,
                new ImageView[]{featureButtons[0], featureButtons[1]});
        cookiesToFeatures.put(PNA_AUTH_COOKIE_KEY, new ImageView[]{featureButtons[2]});
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
        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        boolean anyLoggedIn = false;
        for (AuthCookie cookie : authCookies) {
            if (cookie.hasCookieBeenSet()) {
                anyLoggedIn = true;
                break;
            }
        }

        // update the displayed login state
        if (settings.getBoolean(getString(R.string.pref_logintype_key), false)) {
            if (!isLoggingIn()) {
                if (anyLoggedIn) {
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
        MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.settings:
                loadSettings();
                return true;
            case R.id.login_button:
                login();
                invalidateOptionsMenu();
                return true;
            case R.id.logout_button:
                AnalyticsHandler.trackLogoutEvent();
                logout();
                invalidateOptionsMenu();
                return true;
            case R.id.cancel_button:
                cancelLogin();
                invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
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
            settings.edit().putBoolean("firstRun", false).apply();
            Utility.id(this);
        } else {
            ChangeLogCompat cl = new ChangeLogCompat(this);
            if (cl.isFirstRun()) {
                // first launch after an update
                // this will actually show after the second launch if it's a fresh install
                cl.getFullLogDialogCompat().show();
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
            settings.edit().remove("password").apply();
            settings.edit().putBoolean("encryptedpassword", true).apply();
            // turn off autologin, they can re-activate it when they enter their password
            settings.edit().putBoolean("autologin", false).apply();
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
        final Intent pref_intent = new Intent(this, PreferenceActivity.class);
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
    static class LoginTask extends AsyncTask<AuthCookie, Void, Boolean> {

        private CountDownLatch loginLatch;
        private AuthCookie cookie;

        public LoginTask(CountDownLatch loginLatch) {
            this.loginLatch = loginLatch;
        }

        @Override
        protected Boolean doInBackground(AuthCookie... params) {
            cookie = params[0];
            Boolean result = false;
            try {
                /*
                We can ignore the return value of login() because UpdateUITask ensures all of the
                cookies have been set before completing the login.
                  */
                result = cookie.login();
            } catch (IOException e) {
                /*
                TODO: Inform the user that the login request failed due to a network error.
                This will require a change to AuthCookie or some other sort of shared state
                between LoginTask and UpdateUITask.
                 */
                e.printStackTrace();
            }
            loginLatch.countDown();
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            MyBus.getInstance().post(new LoginFinishedEvent(cookie.getPrefKey(), result));
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
                    Toast.makeText(mActivity, "One or more services could not log in and have been disabled", Toast.LENGTH_SHORT).show();
                    break;
                }
            }
            /*
             trick to make sure that the login is seen as "done" before onCreateOptionsMenu()
             is called.
              */
            cancel(false);
            mActivity.invalidateOptionsMenu();
            mActivity.setLoginProgressBarVisiblity(false);
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
        SharedPreferences sp = ((UTilitiesApplication) getApplication()).getSecurePreferences();
        if (settings.getBoolean(getString(R.string.pref_logintype_key), false)) {
            if (!settings.contains("eid") || !sp.contains("password")
                    || settings.getString("eid", "").equals("")
                    || sp.getString("password", "").equals("")) {
                message.setText("Please enter your credentials to log in");
                message.setDuration(Toast.LENGTH_LONG);
                message.show();
            } else {
                setLoginProgressBarVisiblity(true);
                loginTasks = new ArrayList<>();
                CountDownLatch loginLatch = new CountDownLatch(authCookies.length);
                updateUiTask = new UpdateUiTask(this);
                Utility.parallelExecute(updateUiTask, loginLatch);
                loginTasks.add(updateUiTask);

                for (AuthCookie cookie : authCookies) {
                    LoginTask loginTask = new LoginTask(loginLatch);
                    Utility.parallelExecute(loginTask, cookie);
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
        setLoginProgressBarVisiblity(false);
    }

    /**
     * Log the user out of all UT web services.
     */
    private void logout() {
        for (AuthCookie cookie : authCookies) {
            cookie.logout();
        }
        for (ImageView ib : featureButtons) {
            enableFeature(ib);
            if (((DashboardButtonData) ib.getTag()).loginProgress != null) {
                ((DashboardButtonData) ib.getTag()).loginProgress.setVisibility(View.GONE);
            }
        }
        resetChecks();
    }

    private void disableFeature(final ImageView featureButton) {
        // Some sort of bug in Android 4.x causes the ImageView to disappear if a ColorMatrix
        // is applied, so only do it in 2.3/5.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
            featureButton.setColorFilter(filter);
        }
        Utility.setImageAlpha(featureButton, 75);
        featureButton.setOnClickListener(disabledFeatureButtonListener);
    }

    private void enableFeature(final ImageView featureButton) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            featureButton.clearColorFilter();
        }
        Utility.setImageAlpha(featureButton, 255);
        featureButton.setOnClickListener(enabledFeatureButtonListener);
    }

    @Subscribe
    public void loginFinished(final LoginFinishedEvent lfe) {
        boolean successful = lfe.loginSuccessful() || !isLoginRequired();
        serviceLoggedIn.put(lfe.getService(), successful);
        for (ImageView iv : cookiesToFeatures.get(lfe.getService())) {
            if (successful) {
                enableFeature(iv);
            } else {
                disableFeature(iv);
            }
            ((DashboardButtonData) iv.getTag()).loggedIn = successful;
            ((DashboardButtonData) iv.getTag()).loginProgress.setVisibility(View.GONE);
        }
    }

    private void showLoginFirstToast() {
        message.setText(R.string.login_first);
        message.setDuration(Toast.LENGTH_SHORT);
        message.show();
    }

    private boolean isLoginRequired() {
        return !settings.getBoolean("dont_require_login", false);
    }

    @Override
    public void onResume() {
        super.onResume();
        invalidateOptionsMenu();
        if (!settings.getBoolean(getString(R.string.pref_logintype_key), false)) {
            for (ImageView iv : featureButtons) {
                enableFeature(iv);
            }
        }
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("serviceLoggedIn", serviceLoggedIn);
    }

    @Override
    protected void onDestroy() {
        MyBus.getInstance().unregister(this);
        super.onDestroy();
    }

    /**
     * Reset the checkmark overlays that act as temp login indicators to
     * ensure they show the correct login status.
     */
    private void resetChecks() {
        if (settings.getBoolean(getString(R.string.pref_logintype_key), false)) {
            scheduleCheck.setVisibility(View.GONE);
            balanceCheck.setVisibility(View.GONE);
            dataCheck.setVisibility(View.GONE);
        } else {
            if (!utdAuthCookie.hasCookieBeenSet()) {
                scheduleCheck.setAlpha(CHECK_TRANSLUCENT_OPACITY);
                balanceCheck.setAlpha(CHECK_TRANSLUCENT_OPACITY);
            } else {
                scheduleCheck.setAlpha(1f);
                balanceCheck.setAlpha(1f);
            }
            if (!pnaAuthCookie.hasCookieBeenSet()) {
                dataCheck.setAlpha(CHECK_TRANSLUCENT_OPACITY);
            } else {
                dataCheck.setAlpha(1f);
            }
            scheduleCheck.setVisibility(View.VISIBLE);
            balanceCheck.setVisibility(View.VISIBLE);
            dataCheck.setVisibility(View.VISIBLE);
        }
    }

    private void setLoginProgressBarVisiblity(Boolean showOrHide) {
       for (ImageView featureButton : featureButtons) {
           DashboardButtonData dbd = (DashboardButtonData) featureButton.getTag();
           if (dbd.loginProgress != null) {
                dbd.loginProgress. setVisibility(showOrHide ? View.VISIBLE : View.GONE);
           }
       }
    }

    static class LoginFinishedEvent {
        private String service;
        private boolean successful;

        public LoginFinishedEvent(String service, boolean successful) {
            this.service = service;
            this.successful = successful;
        }

        public String getService() {
            return service;
        }

        public boolean loginSuccessful() {
            return successful;
        }
    }
}

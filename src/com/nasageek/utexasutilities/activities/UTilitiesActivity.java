
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
import android.view.View.OnClickListener;
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

public class UTilitiesActivity extends SherlockActivity {

    private final static int LOGOUT_MENU_ID = 11;
    private final static int CANCEL_LOGIN_MENU_ID = 12;
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
            for (AsyncTask task : loginTasks) {
                if (task != null && task instanceof ChangeableContextTask) {
                    ((ChangeableContextTask) task).setContext(this);
                }
            }
        }

        settings = PreferenceManager.getDefaultSharedPreferences(this.getBaseContext());
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.main);
        if (isLoggingIn()) {
            setSupportProgressBarIndeterminateVisibility(true);
        } else {
            setSupportProgressBarIndeterminateVisibility(false);
        }

        final Intent schedule = new Intent(this, ScheduleActivity.class);
        final Intent balance = new Intent(this, BalanceActivity.class);
        final Intent map = new Intent(this, CampusMapActivity.class);
        final Intent data = new Intent(this, DataUsageActivity.class);
        final Intent menu = new Intent(this, MenuActivity.class);
        final Intent blackboard = new Intent(this, BlackboardPanesActivity.class);

        message = Toast.makeText(this, R.string.login_first, Toast.LENGTH_SHORT);

        scheduleCheck = (ImageView) findViewById(R.id.scheduleCheck);
        balanceCheck = (ImageView) findViewById(R.id.balanceCheck);
        dataCheck = (ImageView) findViewById(R.id.dataCheck);
        blackboardCheck = (ImageView) findViewById(R.id.blackboardCheck);

        if (!settings.contains("encryptedpassword") && settings.contains("firstRun")
                && settings.contains("password")) {
            Utility.commit(settings.edit().remove("password"));
            Utility.commit(settings.edit().putBoolean("encryptedpassword", true));
            Utility.commit(settings.edit().putBoolean("autologin", false));
            AlertDialog.Builder passwordcleared_builder = new AlertDialog.Builder(this);
            passwordcleared_builder
                    .setMessage(
                            "With this update to UTilities, your stored password will be encrypted. Your currently stored password "
                                    + "has been wiped for security purposes and you will need to re-enter it.")
                    .setCancelable(true).setPositiveButton("Ok", null);
            AlertDialog passwordcleared = passwordcleared_builder.create();
            passwordcleared.show();
        }

        if (!settings.contains("firstRun")) {
            AlertDialog.Builder nologin_builder = new AlertDialog.Builder(this);
            nologin_builder
                    .setMessage(
                            "This is your first time running UTilities; why don't you try logging in to get the most use out of the app?")
                    .setCancelable(false)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            loadSettings();
                        }
                    }).setNegativeButton("No thanks", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            nologin = nologin_builder.create();
            nologin.show();
            Utility.commit(settings.edit().putBoolean("firstRun", false));
            Utility.id(this);
        } else {
            ChangeLog cl = new ChangeLog(this);

            if (cl.isFirstRun()) {
                cl.getFullLogDialog().show();
            }
        }

        if (settings.getBoolean("autologin", false) && !isLoggingIn() && !mApp.allCookiesSet()) {
            login();
        }

        final ImageButton schedulebutton = (ImageButton) findViewById(R.id.schedule_button);
        schedulebutton.setOnTouchListener(new imageButtonTouchListener(
                (TransitionDrawable) schedulebutton.getDrawable()));
        schedulebutton.setOnFocusChangeListener(new imageButtonFocusListener());
        schedulebutton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (settings.getBoolean("loginpref", false)) {
                    if (!utdAuthCookie.hasCookieBeenSet() || isLoggingIn()) {
                        showLoginFirstToast();
                    } else {
                        startActivity(schedule);
                    }
                } else {
                    if (!utdAuthCookie.hasCookieBeenSet()) {
                        Intent login_intent = new Intent(UTilitiesActivity.this,
                                LoginActivity.class);
                        login_intent.putExtra("activity", schedule.getComponent().getClassName());
                        login_intent.putExtra("service", 'u');
                        startActivity(login_intent);
                    } else {
                        startActivity(schedule);
                    }
                }
            }
        });

        final ImageButton balancebutton = (ImageButton) findViewById(R.id.balance_button);
        balancebutton.setOnTouchListener(new imageButtonTouchListener(
                (TransitionDrawable) balancebutton.getDrawable()));
        balancebutton.setOnFocusChangeListener(new imageButtonFocusListener());
        balancebutton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (settings.getBoolean("loginpref", false)) {
                    if (!utdAuthCookie.hasCookieBeenSet() || isLoggingIn()) {
                        showLoginFirstToast();
                    } else {
                        startActivity(balance);
                    }
                } else {
                    if (!utdAuthCookie.hasCookieBeenSet()) {
                        Intent login_intent = new Intent(UTilitiesActivity.this,
                                LoginActivity.class);
                        login_intent.putExtra("activity", balance.getComponent().getClassName());
                        login_intent.putExtra("service", 'u');
                        startActivity(login_intent);
                    } else {
                        startActivity(balance);
                    }
                }
            }
        });

        final ImageButton mapbutton = (ImageButton) findViewById(R.id.map_button);
        mapbutton.setOnTouchListener(new imageButtonTouchListener((TransitionDrawable) mapbutton
                .getDrawable()));
        mapbutton.setOnFocusChangeListener(new imageButtonFocusListener());
        mapbutton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(map);
            }
        });

        final ImageButton databutton = (ImageButton) findViewById(R.id.data_button);
        databutton.setOnTouchListener(new imageButtonTouchListener((TransitionDrawable) databutton
                .getDrawable()));
        databutton.setOnFocusChangeListener(new imageButtonFocusListener());
        databutton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (settings.getBoolean("loginpref", false)) {
                    if (!pnaAuthCookie.hasCookieBeenSet() || isLoggingIn()) {
                        showLoginFirstToast();
                    } else {
                        startActivity(data);
                    }
                } else {
                    if (!pnaAuthCookie.hasCookieBeenSet()) {
                        Intent login_intent = new Intent(UTilitiesActivity.this,
                                LoginActivity.class);
                        login_intent.putExtra("activity", data.getComponent().getClassName());
                        login_intent.putExtra("service", 'p');
                        startActivity(login_intent);
                    } else {
                        startActivity(data);
                    }
                }
            }
        });

        final ImageButton menubutton = (ImageButton) findViewById(R.id.menu_button);
        menubutton.setOnTouchListener(new imageButtonTouchListener((TransitionDrawable) menubutton
                .getDrawable()));
        menubutton.setOnFocusChangeListener(new imageButtonFocusListener());
        menubutton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(menu);
            }

        });

        final ImageButton blackboardbutton = (ImageButton) findViewById(R.id.blackboard_button);
        blackboardbutton.setOnTouchListener(new imageButtonTouchListener(
                (TransitionDrawable) blackboardbutton.getDrawable()));
        blackboardbutton.setOnFocusChangeListener(new imageButtonFocusListener());
        blackboardbutton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (settings.getBoolean("loginpref", false)) {
                    if (!bbAuthCookie.hasCookieBeenSet() || isLoggingIn()) {
                        showLoginFirstToast();
                    } else {
                        startActivity(blackboard);
                    }
                } else {
                    if (!bbAuthCookie.hasCookieBeenSet()) {
                        Intent login_intent = new Intent(UTilitiesActivity.this,
                                LoginActivity.class);
                        login_intent.putExtra("activity", blackboard.getComponent().getClassName());
                        login_intent.putExtra("service", 'b');
                        startActivity(login_intent);
                    } else {
                        startActivity(blackboard);
                    }
                }
            }

        });
    }

    private class imageButtonFocusListener implements OnFocusChangeListener {
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

    private class imageButtonTouchListener implements OnTouchListener {
        private boolean buttonPressed = false;
        private TransitionDrawable crossfade;

        public imageButtonTouchListener(TransitionDrawable transDrawable) {
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

        if (settings.getBoolean("loginpref", false)) {
            if (!isLoggingIn()) {
                if (allLoggedIn) {
                    menu.removeGroup(R.id.log);
                    menu.add(R.id.log, LOGOUT_MENU_ID, Menu.NONE, "Log out");
                    MenuItem item = menu.findItem(LOGOUT_MENU_ID);
                    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                } else {
                    menu.removeGroup(R.id.log);
                    menu.add(R.id.log, R.id.login, Menu.NONE, "Log in");
                    MenuItem item = menu.findItem(R.id.login);
                    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                }
            } else {
                menu.removeGroup(R.id.log);
                menu.add(R.id.log, CANCEL_LOGIN_MENU_ID, Menu.NONE, "Cancel");
                MenuItem item = menu.findItem(CANCEL_LOGIN_MENU_ID);
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            }
        } else {
            if (anyLoggedIn) {
                menu.removeGroup(R.id.log);
                menu.add(R.id.log, LOGOUT_MENU_ID, Menu.NONE, "Log out");
                MenuItem item = menu.findItem(LOGOUT_MENU_ID);
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            } else {
                menu.removeGroup(R.id.log);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.login:
                login();
                invalidateOptionsMenu();
                break;
            case R.id.settings:
                loadSettings();
                break;
            case LOGOUT_MENU_ID:
                logout();
                invalidateOptionsMenu();
                break;
            case CANCEL_LOGIN_MENU_ID:
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

    private void loadSettings() {
        final Intent pref_intent = new Intent(this, Preferences.class);
        startActivity(pref_intent);
    }

    private boolean isLoggingIn() {
        // updateUiTask is null before login has begun
        return updateUiTask != null && updateUiTask.getStatus() == AsyncTask.Status.RUNNING &&
                !updateUiTask.isCancelled();
    }

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

    private void cancelLogin() {
        for (AsyncTask task : loginTasks) {
            task.cancel(true);
        }
        logout();
        setSupportProgressBarIndeterminateVisibility(false);
    }

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

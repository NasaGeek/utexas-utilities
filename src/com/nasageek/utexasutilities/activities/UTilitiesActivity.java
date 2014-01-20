
package com.nasageek.utexasutilities.activities;

import org.apache.http.impl.client.DefaultHttpClient;

import android.app.AlertDialog;
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
import com.nasageek.utexasutilities.ChangeLog;
import com.nasageek.utexasutilities.ChangeableContextTask;
import com.nasageek.utexasutilities.ConnectionHelper;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.SecurePreferences;
import com.nasageek.utexasutilities.Utility;

public class UTilitiesActivity extends SherlockActivity {

    private final static int LOGOUT_MENU_ID = 11;
    private final static int CANCEL_LOGIN_MENU_ID = 12;
    private final static int BUTTON_ANIMATION_DURATION = 90;

    private SharedPreferences settings;
    private Toast message;
    private ImageView scheduleCheck, balanceCheck, dataCheck, blackboardCheck;
    private ConnectionHelper.loginTask lt;
    private ConnectionHelper.PNALoginTask plt;
    private ConnectionHelper.bbLoginTask bblt;
    private AlertDialog nologin;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
         * StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
         * .detectAll() .penaltyLog() .build()); StrictMode.setVmPolicy(new
         * StrictMode.VmPolicy.Builder() .detectAll() .penaltyLog() .build()); }
         */

        /*
         * try { File httpCacheDir = new File(getCacheDir(), "http"); long
         * httpCacheSize = 1024*1024; HttpResponseCache.install(httpCacheDir,
         * httpCacheSize); } catch (IOException e) { e.printStackTrace(); }
         */

        final ChangeableContextTask[] loginTasks = (ChangeableContextTask[]) getLastNonConfigurationInstance();
        if (loginTasks != null) {
            for (ChangeableContextTask at : loginTasks) {
                if (at != null) {
                    at.setContext(this);
                }
            }
        }

        settings = PreferenceManager.getDefaultSharedPreferences(this.getBaseContext());
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.main);
        if (ConnectionHelper.isLoggingIn()) {
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

        if (settings.getBoolean("autologin", false) && !ConnectionHelper.cookieHasBeenSet()
                && !ConnectionHelper.isLoggingIn()) {
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
                    if (!ConnectionHelper.cookieHasBeenSet() || ConnectionHelper.isLoggingIn()) {
                        message.setText(R.string.login_first);
                        message.setDuration(Toast.LENGTH_SHORT);
                        message.show();
                    } else {
                        startActivity(schedule);
                    }
                } else {
                    if (!ConnectionHelper.cookieHasBeenSet()) {
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
                    if (!ConnectionHelper.cookieHasBeenSet() || ConnectionHelper.isLoggingIn()) {
                        message.setText(R.string.login_first);
                        message.setDuration(Toast.LENGTH_SHORT);
                        message.show();
                    } else {
                        startActivity(balance);
                    }
                } else {
                    if (!ConnectionHelper.cookieHasBeenSet()) {
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
                    if (!ConnectionHelper.PNACookieHasBeenSet() || ConnectionHelper.isLoggingIn()) {
                        message.setText(R.string.login_pna_first);
                        message.setDuration(Toast.LENGTH_SHORT);
                        message.show();
                    } else {
                        startActivity(data);
                    }
                } else {
                    if (!ConnectionHelper.PNACookieHasBeenSet()) {
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
                    if (!ConnectionHelper.bbCookieHasBeenSet() || ConnectionHelper.isLoggingIn()) {
                        message.setText(R.string.login_bb_first);
                        message.setDuration(Toast.LENGTH_SHORT);
                        message.show();
                    } else {
                        startActivity(blackboard);
                    }
                } else {
                    if (!ConnectionHelper.bbCookieHasBeenSet()) {
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

    /*
     * @Override protected void onStop() { super.onStop(); HttpResponseCache
     * cache = HttpResponseCache.getInstalled(); if(cache != null)
     * cache.flush(); }
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = this.getSupportMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        if (settings.getBoolean("loginpref", false)) {
            if (!ConnectionHelper.isLoggingIn()) {
                if (ConnectionHelper.cookieHasBeenSet() && ConnectionHelper.bbCookieHasBeenSet()
                        && ConnectionHelper.PNACookieHasBeenSet()) {
                    menu.removeGroup(R.id.log);
                    menu.add(R.id.log, 11, Menu.NONE, "Log out");
                    MenuItem item = menu.findItem(11);
                    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                } else if (!ConnectionHelper.cookieHasBeenSet()
                        || !ConnectionHelper.bbCookieHasBeenSet()
                        || !ConnectionHelper.PNACookieHasBeenSet()) {
                    menu.removeGroup(R.id.log);
                    menu.add(R.id.log, R.id.login, Menu.NONE, "Log in");
                    MenuItem item = menu.findItem(R.id.login);
                    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                }
            } else if (ConnectionHelper.isLoggingIn()) {
                menu.removeGroup(R.id.log);
                menu.add(R.id.log, CANCEL_LOGIN_MENU_ID, Menu.NONE, "Cancel");
                MenuItem item = menu.findItem(CANCEL_LOGIN_MENU_ID);
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            }
        } else {
            if (ConnectionHelper.cookieHasBeenSet() || ConnectionHelper.bbCookieHasBeenSet()
                    || ConnectionHelper.PNACookieHasBeenSet()) {
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
        return new ChangeableContextTask[] {
                bblt, lt, plt
        };
    }

    public void loadSettings() {
        final Intent pref_intent = new Intent(this, Preferences.class);
        startActivity(pref_intent);
    }

    public void login() {
        SecurePreferences sp = new SecurePreferences(UTilitiesActivity.this,
                "com.nasageek.utexasutilities.password", false);
        if (settings.getBoolean("loginpref", false)) {
            if (!settings.contains("eid") || !sp.containsKey("password")
                    || settings.getString("eid", "error").equals("")
                    || sp.getString("password").equals("")) {
                message.setText("Please enter your credentials to log in");
                message.setDuration(Toast.LENGTH_LONG);
                message.show();
            } else {
                message.setText("Logging in...");
                message.setDuration(Toast.LENGTH_SHORT);
                message.show();
                ConnectionHelper.loggingIn = true;

                setSupportProgressBarIndeterminateVisibility(true);

                ConnectionHelper ch = new ConnectionHelper();
                DefaultHttpClient httpclient = ConnectionHelper.getThreadSafeClient();
                DefaultHttpClient pnahttpclient = ConnectionHelper.getThreadSafeClient();
                DefaultHttpClient bbhttpclient = ConnectionHelper.getThreadSafeClient();

                ConnectionHelper.resetCookies(this);

                bblt = ch.new bbLoginTask(this, httpclient, pnahttpclient, bbhttpclient);
                lt = ch.new loginTask(this, httpclient, pnahttpclient, bbhttpclient);
                plt = ch.new PNALoginTask(this, httpclient, pnahttpclient, bbhttpclient);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    bblt.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ch);
                    lt.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ch);
                    plt.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ch);
                } else {
                    bblt.execute(ch);
                    lt.execute(ch);
                    plt.execute(ch);
                }
            }
        } else {
            Intent login_intent = new Intent(this, LoginActivity.class);
            startActivity(login_intent);
        }
    }

    public void cancelLogin() {
        if (lt != null) {
            lt.cancel(true);
        }
        if (plt != null) {
            plt.cancel(true);
        }
        if (bblt != null) {
            bblt.cancel(true);
        }
        message.setText("Cancelled");
        ConnectionHelper.logout(this);
        setSupportProgressBarIndeterminateVisibility(false);
    }

    public void logout() {
        ConnectionHelper.logout(this);
        resetChecks();
        message.setText("You have been successfully logged out");
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
            if (!ConnectionHelper.cookieHasBeenSet()) {
                scheduleCheck.setImageResource(R.drawable.ic_done_translucent);
                balanceCheck.setImageResource(R.drawable.ic_done_translucent);
            } else {
                scheduleCheck.setImageResource(R.drawable.ic_done);
                balanceCheck.setImageResource(R.drawable.ic_done);
            }
            if (!ConnectionHelper.bbCookieHasBeenSet()) {
                blackboardCheck.setImageResource(R.drawable.ic_done_translucent);
            } else {
                blackboardCheck.setImageResource(R.drawable.ic_done);
            }
            if (!ConnectionHelper.PNACookieHasBeenSet()) {
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

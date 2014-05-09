
package com.nasageek.utexasutilities.fragments;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.WrappedViewPager;
import com.nasageek.utexasutilities.model.Classtime;
import com.nasageek.utexasutilities.model.UTClass;
import com.viewpagerindicator.TabPageIndicator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class DoubleDatePickerDialogFragment extends SherlockDialogFragment {

    private List<View> datePickers;
    private DatePicker startDatePicker, endDatePicker;

    public static final String[] EVENT_PROJECTION = new String[] {
            Calendars._ID, // 0
            Calendars.ACCOUNT_NAME, // 1
            Calendars.CALENDAR_DISPLAY_NAME, // 2
            Calendars.OWNER_ACCOUNT, // 3
            Calendars.VISIBLE, // 4
            Calendars.CALENDAR_ACCESS_LEVEL
    // 5
    };

    // The indices for the projection array above.
    private static final int PROJECTION_ID_INDEX = 0;
    private static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
    private static final int PROJECTION_DISPLAY_NAME_INDEX = 2;
    private static final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;
    private static final int PROJECTION_DISPLAY_INDEX = 4;
    private static final int PROJECTION_ACCESS_LEVEL_INDEX = 5;

    public DoubleDatePickerDialogFragment() {
    }

    public static DoubleDatePickerDialogFragment newInstance(ArrayList<UTClass> classList) {
        DoubleDatePickerDialogFragment ddpdf = new DoubleDatePickerDialogFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList("classList", classList);
        ddpdf.setArguments(args);
        return ddpdf;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
    }

    /*
     * @Override public Dialog onCreateDialog(Bundle savedInstanceState) { View
     * view = getActivity().getLayoutInflater().inflate(R.layout.
     * double_date_picker_dialog_fragment_layout, null); AlertDialog.Builder
     * build = new AlertDialog.Builder(getActivity());
     * build.setView(view). setPositiveButton("Okay", null).
     * setNegativeButton("Cancel", null).
     * setTitle("Choose start and end dates"); initialisePaging(view); return
     * build.create(); }
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.double_date_picker_dialog_fragment_layout, container);
        initialisePaging(view);
        ((Button) view.findViewById(R.id.calendar_button_ok))
                .setOnClickListener(new OnClickListener() {

                    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                    @Override
                    public void onClick(View v) {
                        Calendar startDate = new GregorianCalendar(startDatePicker.getYear(),
                                startDatePicker.getMonth(), startDatePicker.getDayOfMonth());
                        Calendar endDate = new GregorianCalendar(endDatePicker.getYear(),
                                endDatePicker.getMonth(), endDatePicker.getDayOfMonth());
                        if (startDate.after(endDate)) {
                            Toast.makeText(getActivity(),
                                    "Start date must be before end date.", Toast.LENGTH_SHORT)
                                    .show();
                            return;
                        }
                        ContentResolver cr = getActivity().getContentResolver();

                        SimpleDateFormat formatter = new SimpleDateFormat("hh:mmaa", Locale.US);
                        ArrayList<UTClass> classList = getArguments().getParcelableArrayList(
                                "classList");
                        SimpleDateFormat endDateFormatter = new SimpleDateFormat(
                                "yyyyMMdd'T000000Z'", Locale.US);
                        // roll forward one because RRULE will not place events
                        // on the specified end date
                        endDate.roll(Calendar.DATE, true);

                        String endDateString = endDateFormatter.format(endDate.getTime());
                        ArrayList<ContentValues> valuesList = new ArrayList<ContentValues>();

                        // copying our original selected start date for
                        // comparison to each class
                        // start date later
                        Calendar selectedStartDate = (Calendar) startDate.clone();
                        for (UTClass clz : classList) {
                            for (Classtime clt : clz.getClassTimes()) {
                                Date classStartTime = null, classEndTime = null;

                                try {
                                    classStartTime = formatter.parse(clt.getStartTime());
                                    classEndTime = formatter.parse(clt.getEndTime());
                                } catch (ParseException e1) {
                                    Toast.makeText(getActivity(),
                                            "Error parsing " + clt.getCourseId()
                                                    + " start/end time. Export canceled.",
                                            Toast.LENGTH_LONG);
                                    e1.printStackTrace();
                                    DoubleDatePickerDialogFragment.this.dismiss();
                                    return;
                                }

                                // resetting startdate to the selected start
                                // date
                                startDate = (Calendar) selectedStartDate.clone();
                                startDate.clear(Calendar.DAY_OF_MONTH);
                                startDate.set(Calendar.DAY_OF_WEEK,
                                        getDayConstantFromChar(clt.getDay()));

                                // forces us to start the schedule when the user
                                // asked us to,
                                // and not just on the week of the day they
                                // selected
                                if (startDate.before(selectedStartDate)) {
                                    startDate.roll(Calendar.WEEK_OF_YEAR, true);
                                }

                                // if for some strange reason their end date is
                                // <1 week after start date
                                // don't add that class
                                if (startDate.after(endDate)) {
                                    continue;
                                }

                                startDate.set(Calendar.HOUR_OF_DAY, classStartTime.getHours());
                                startDate.set(Calendar.MINUTE, classStartTime.getMinutes());

                                ContentValues values = new ContentValues();
                                values.put(CalendarContract.Events.TITLE,
                                        clz.getId() + " - " + clz.getName());
                                values.put(CalendarContract.Events.EVENT_LOCATION, clt
                                        .getBuilding().getId() + " " + clt.getBuilding().getRoom());
                                values.put(CalendarContract.Events.EVENT_COLOR,
                                        Integer.parseInt(clt.getColor(), 16));
                                values.put(CalendarContract.Events.RRULE, "FREQ=WEEKLY;UNTIL="
                                        + endDateString);
                                values.put(CalendarContract.Events.DURATION,
                                        startEndToDuration(classStartTime, classEndTime));
                                values.put(CalendarContract.Events.DTSTART,
                                        startDate.getTimeInMillis());
                                values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone
                                        .getTimeZone("US/Central").getID());
                                valuesList.add(values);
                            }
                        }

                        Cursor cur = null;
                        Uri uri = Calendars.CONTENT_URI;

                        // show them Google Calendars where they are either:
                        // owner, editor, contributor, or domain admin
                        // (700, 600, 500, 800 respectively)
                        cur = cr.query(uri, EVENT_PROJECTION, "((" + Calendars.ACCOUNT_TYPE
                                + " = ?) AND ((" + Calendars.CALENDAR_ACCESS_LEVEL + " = ?) OR "
                                + "(" + Calendars.CALENDAR_ACCESS_LEVEL + " = ?) OR " + "("
                                + Calendars.CALENDAR_ACCESS_LEVEL + " = ?) OR " + "("
                                + Calendars.CALENDAR_ACCESS_LEVEL + " = ?)))", new String[] {
                                "com.google", "800", "700", "600", "500"
                        }, null);
                        ArrayList<String> calendars = new ArrayList<String>();
                        ArrayList<Integer> indices = new ArrayList<Integer>();

                        // If no calendars are available, let them know
                        if (cur == null) {
                            Toast.makeText(getActivity(),
                                    "There are no available calendars to export to.",
                                    Toast.LENGTH_LONG).show();
                            DoubleDatePickerDialogFragment.this.dismiss();
                            return;
                        }
                        while (cur.moveToNext()) {
                            long calID = 0;
                            String displayName = null;
                            String accountName = null;
                            calID = cur.getLong(PROJECTION_ID_INDEX);
                            displayName = cur.getString(PROJECTION_DISPLAY_NAME_INDEX);
                            accountName = cur.getString(PROJECTION_ACCOUNT_NAME_INDEX);
                            calendars.add(displayName + " ^^ " + accountName);

                            // going to hope that they don't have so many
                            // calendars that I actually need a long
                            indices.add((int) calID);
                        }
                        FragmentManager fm = getActivity().getSupportFragmentManager();
                        PickCalendarDialogFragment pcdf = PickCalendarDialogFragment.newInstance(
                                indices, calendars, valuesList);
                        DoubleDatePickerDialogFragment.this.dismiss();
                        pcdf.show(fm, "fragment_pick_calendar");
                        return;
                    }
                });
        ((Button) view.findViewById(R.id.calendar_button_cancel))
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getDialog().dismiss();
                    }
                });
        return view;
    }

    private int getDayConstantFromChar(char day) {
        switch (day) {
            case 'M':
                return Calendar.MONDAY;
            case 'T':
                return Calendar.TUESDAY;
            case 'W':
                return Calendar.WEDNESDAY;
            case 'H':
                return Calendar.THURSDAY;
            case 'F':
                return Calendar.FRIDAY;
            default:
                return Calendar.MONDAY;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void initialisePaging(View view) {
        datePickers = new Vector<View>();
        FrameLayout fl1 = new FrameLayout(getActivity());
        FrameLayout fl2 = new FrameLayout(getActivity());
        startDatePicker = new DatePicker(getActivity());
        startDatePicker.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        endDatePicker = new DatePicker(getActivity());
        endDatePicker.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, Gravity.CENTER));

        // not entirely necessary since this feature will never be supported
        // below Honeycomb
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            startDatePicker.setCalendarViewShown(false);
            endDatePicker.setCalendarViewShown(false);
        }
        fl1.addView(startDatePicker);
        fl1.setTag("Start Date");
        fl2.addView(endDatePicker);
        fl2.setTag("End Date");

        datePickers.add(fl1);
        datePickers.add(fl2);

        ViewPagerAdapter adapter = new ViewPagerAdapter(datePickers);

        WrappedViewPager pager = (WrappedViewPager) view.findViewById(R.id.wrappedviewpager);
        pager.setPageMargin(2);
        pager.setAdapter(adapter);

        TabPageIndicator tabIndicator = (TabPageIndicator) view.findViewById(R.id.titles);
        tabIndicator.setViewPager(pager);
    }

    /*
     * Converts a start and end time to a duration in the RFC2445 format
     */
    private String startEndToDuration(Date startTime, Date endTime) {
        int minutesDur = 0;
        minutesDur = (int) ((endTime.getTime() - startTime.getTime()) / (1000 * 60));
        String duration = minutesDur + "M";
        return "P" + duration;
    }
}

class ViewPagerAdapter extends android.support.v4.view.PagerAdapter {
    private List<View> views;

    public ViewPagerAdapter(List<View> views) {
        super();
        this.views = views;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0.equals(arg1);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(views.get(position));
        return views.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
        views.remove(position);
    }

    @Override
    public String getPageTitle(int position) {
        return (String) views.get(position).getTag();
    }
}


package com.nasageek.utexasutilities.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.Shape;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nasageek.utexasutilities.model.Classtime;
import com.nasageek.utexasutilities.model.UTClass;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScheduleClassAdapter extends BaseAdapter {

    // contains the Classtimes that have been ordered/grouped into a fake "grid"
    private List<Classtime> orderedClasstimes = new ArrayList<>(180);
    // element is true if it's the first cell of a class in the schedule (cells are a half hour)
    private List<Boolean> firstlist = new ArrayList<>(180);
    private Context mContext;
    private int currentTimePos = -1;
    private int currMinutes;
    private String emptyCellBackgroundPref;

    private static final int DARKGRAY = 0xFFCECECE;
    private static final int LIGHTGRAY = 0xFFDCDCDC;

    public ScheduleClassAdapter(Context c, List<UTClass> classList) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        emptyCellBackgroundPref = sp.getString("schedule_background_style", "checkhour");
        mContext = c;
        ArrayList<Classtime> allClasstimes = new ArrayList<>(50);
        updateTime();

        // grab all ClassTimes out of classList
        for (UTClass clz : classList) {
            for (Classtime classtime : clz.getClassTimes()) {
                allClasstimes.add(classtime);
            }
        }

        for (int x = 0; x < 180; x++) {
            orderedClasstimes.add(null);
            firstlist.add(false);
        }

        Map<Character, Integer> dayCharMap = new HashMap<>();
        // set up day map for manipulating orderedClasstimes as if it were a grid
        dayCharMap.put('M', 0);
        dayCharMap.put('T', 1);
        dayCharMap.put('W', 2);
        dayCharMap.put('H', 3);
        dayCharMap.put('F', 4);

        for (Classtime classTime : allClasstimes) {
            int startPos = timeToPos(classTime.getStartTime());
            int endPos = timeToPos(classTime.getEndTime());
            int dayOffset = dayCharMap.get(classTime.getDay());

            for (int a = 0; a < (endPos - startPos); a++) {
                int dayIndex = dayOffset + 5 * startPos + a * 5;
                orderedClasstimes.set(dayIndex, classTime);
                if (a == 0) {
                    firstlist.set(dayIndex, true);
                }
            }
        }
    }

    public void updateTime() {
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_WEEK) - 2;
        String time = cal.get(Calendar.HOUR) + (cal.get(Calendar.MINUTE) >= 30 ? ":30" : ":00")
                + (cal.get(Calendar.AM_PM) == Calendar.PM ? "pm" : "");

        if (day < 5 && day >= 0 && cal.get(Calendar.HOUR_OF_DAY) <= 22
                && cal.get(Calendar.HOUR_OF_DAY) >= 6) {
            currentTimePos = day + 5 * timeToPos(time);
            currMinutes = cal.get(Calendar.MINUTE) % 30;
        }
    }

    // 6am is at position 0
    private int timeToPos(String time) {
        String[] temp = time.split(":");
        int pos = Integer.parseInt(temp[0]) * 2 - 12;
        if (temp[1].contains("pm") && pos != 12) {
            pos += 24;
        }
        if (temp[1].charAt(0) == '3') {
            pos++;
        }
        return pos;
    }

    public int getEarliestClassPos() {
        for (int i = 0; i < firstlist.size(); i++) {
            if (firstlist.get(i)) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public int getCount() {
        return orderedClasstimes.size();
    }

    @Override
    public Object getItem(int position) {
        return orderedClasstimes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressWarnings("deprecation")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        TextView textView;

        if (convertView == null) {
            textView = new TextView(mContext);
        } else {
            textView = (TextView) convertView;
        }

        textView.setTextColor(Color.BLACK);
        textView.setTextSize(13f); // 11.75 to fill cell space

        // is there class at this time?
        if (orderedClasstimes.get(position) == null) {
            if (position == currentTimePos) {
                final int colorInt = getEmptyCellColor(position);
                Drawable currentMinutesLine = new ShapeDrawable(new Shape() {

                    @Override
                    public void draw(Canvas canvas, Paint paint) {
                        drawCurrentMinutesLine(this, canvas, paint, colorInt);
                    }
                });
                textView.setBackgroundDrawable(currentMinutesLine);
            } else {
                textView.setBackgroundColor(getEmptyCellColor(position));
            }
            textView.setText("");
        } else {
            final Classtime cl = orderedClasstimes.get(position);
            final String color = "#" + cl.getColor();

            if (position == currentTimePos) {
                final int colorInt = Color.parseColor(color);
                Drawable currentMinutesLine = new ShapeDrawable(new Shape() {

                    @Override
                    public void draw(Canvas canvas, Paint paint) {
                        drawCurrentMinutesLine(this, canvas, paint, colorInt);
                    }
                });
                textView.setBackgroundDrawable(currentMinutesLine);
            } else {
                textView.setBackgroundColor(Color.parseColor(color));
            }

            // is this the first Classtime in a "block"?
            if (firstlist.get(position)) {
                // if so, label it with the start time of the class
                textView.setText(orderedClasstimes.get(position).getStartTime());
                textView.setGravity(Gravity.CENTER_HORIZONTAL);
            } else {
                textView.setText("");
            }
        }
        return textView;
    }

    private void drawCurrentMinutesLine(Shape shape, Canvas canvas, Paint paint, int bgColor) {
        paint.setStrokeWidth(3f);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawColor(bgColor);

        Paint blur = new Paint(paint);
        blur.setStrokeWidth(3f);
        blur.setMaskFilter(new BlurMaskFilter(3, BlurMaskFilter.Blur.SOLID));
        canvas.drawLine(0, (int) ((currMinutes / 30.0) * shape.getHeight() + .5),
                shape.getWidth(), (int) ((currMinutes / 30.0) * shape.getHeight() + .5), paint);
    }

    private int getEmptyCellColor(int position) {
        if (emptyCellBackgroundPref.equals("checkhour")) {
            if ((position / 10) % 2 == 0) {
                if ((position / 5) % 2 == 0) {
                    return position % 2 == 0 ? LIGHTGRAY : DARKGRAY;
                } else {
                    return position % 2 == 0 ? DARKGRAY : LIGHTGRAY;
                }
            } else {
                if ((position / 5) % 2 == 0) {
                    return position % 2 == 0 ? DARKGRAY : LIGHTGRAY;
                } else {
                    return position % 2 == 0 ? LIGHTGRAY : DARKGRAY;
                }
            }
        } else if (emptyCellBackgroundPref.equals("checkhalf")) {
            return position % 2 == 0 && (position % 10) % 2 == 0 ? LIGHTGRAY : DARKGRAY;
        } else if (emptyCellBackgroundPref.equals("stripehour")) {
            return position / 10 % 2 == 0 ? LIGHTGRAY : DARKGRAY;
        } else if (emptyCellBackgroundPref.equals("stripehalf")) {
            return position / 5 % 2 == 0 ? LIGHTGRAY : DARKGRAY;
        } else {
            return Color.BLACK;
        }
    }
}

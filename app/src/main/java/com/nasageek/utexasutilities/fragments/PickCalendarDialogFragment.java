
package com.nasageek.utexasutilities.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.nasageek.utexasutilities.AnalyticsHandler;
import com.nasageek.utexasutilities.R;

import java.util.ArrayList;

public class PickCalendarDialogFragment extends ExportScheduleDialogFragment {

    private FragmentActivity parentAct;

    public PickCalendarDialogFragment() {
    }

    public static PickCalendarDialogFragment newInstance(ArrayList<Integer> indices,
            ArrayList<String> calendars, ArrayList<ContentValues> valuesList) {
        PickCalendarDialogFragment pcdf = new PickCalendarDialogFragment();
        Bundle args = new Bundle();
        args.putIntegerArrayList("indices", indices);
        args.putStringArrayList("calendars", calendars);
        args.putParcelableArrayList("valuesList", valuesList);
        pcdf.setArguments(args);
        return pcdf;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        parentAct = getActivity();
        ArrayList<String> calendars = getArguments().getStringArrayList("calendars");
        return new AlertDialog.Builder(getActivity())
                .setAdapter(new CalendarAdapter(getActivity(), calendars), (dialog, which) -> {
                    CalendarInsertHandler cih = new CalendarInsertHandler(getActivity()
                            .getContentResolver());
                    ArrayList<ContentValues> cvList = getArguments().getParcelableArrayList(
                            "valuesList");
                    ArrayList<Integer> indices = getArguments().getIntegerArrayList("indices");
                    int selection = indices.get(which);

                    for (int i = 0; i < cvList.size(); i++) {
                        cvList.get(i).put(CalendarContract.Events.CALENDAR_ID, selection);
                        cih.startInsert(i, null, CalendarContract.Events.CONTENT_URI, cvList.get(i));
                    }
                })
                .setTitle("Select calendar")
                .create();
    }

    // Suppress because handler is very shortlived
    @SuppressLint("HandlerLeak")
    class CalendarInsertHandler extends AsyncQueryHandler {

        public CalendarInsertHandler(ContentResolver cr) {
            super(cr);
        }

        @Override
        public void onInsertComplete(int token, Object cookie, Uri uri) {
            if (token == getArguments().getParcelableArrayList("valuesList").size() - 1) {
                Log.d("CalendarStuff", "insert finished");
                Toast.makeText(
                        parentAct,
                        "This schedule has been exported successfully. It may take a minute or two "
                                + "for it to show up on your calendar.", Toast.LENGTH_LONG).show();
                AnalyticsHandler.trackCalendarExportEvent();
            }
        }
    }

    class CalendarAdapter extends BaseAdapter {
        private ArrayList<String> elements;
        private LayoutInflater inflater;

        public CalendarAdapter(Context con, ArrayList<String> elements) {
            super();
            this.elements = elements;
            inflater = (LayoutInflater) con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return elements.size();
        }

        @Override
        public Object getItem(int arg0) {
            return elements.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.calendar_item_view, parent, false);
            }
            String calName = elements.get(position).split(" \\^\\^ ")[0];
            String accName = elements.get(position).split(" \\^\\^ ")[1];
            ((TextView) convertView.findViewById(R.id.calendar_name)).setText(calName);
            ((TextView) convertView.findViewById(R.id.calendar_account)).setText(accName);
            return convertView;
        }
    }
}


package com.nasageek.utexasutilities.adapters;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.model.canvas.ActivityStreamItem;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ActivityStreamAdapter extends ArrayAdapter<ActivityStreamItem> {

    private int mResource;
    private LayoutInflater mInflater;
    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    public ActivityStreamAdapter(Context context, int resource, List<ActivityStreamItem> objects) {
        super(context, resource, objects);
        for (ActivityStreamItem item : objects) {
            item.formattedMessage = Html.fromHtml(item.message);
            item.formattedDate = formatter.format(item.updated_at);
        }
        mResource = resource;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ActivityStreamItem item = getItem(position);
        View view;

        String title = item.title;
        CharSequence content = item.formattedMessage;
        String date = item.formattedDate;

        if (convertView == null) {
            view = mInflater.inflate(mResource, parent, false);
        } else {
            view = convertView;
        }

        TextView titleView = (TextView) view.findViewById(R.id.announcement_header_subject);
        TextView dateView = (TextView) view.findViewById(R.id.announcement_header_date);
        TextView contentView = (TextView) view.findViewById(R.id.announcement_body);

        titleView.setText(title);
        dateView.setText(date);
        contentView.setText(content);
        return view;
    }
}

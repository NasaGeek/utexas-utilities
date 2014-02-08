
package com.nasageek.utexasutilities.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.nasageek.utexasutilities.model.canvas.ActivityStreamItem;

public class ActivityStreamAdapter extends ArrayAdapter<ActivityStreamItem> {

    private int mResource;
    private LayoutInflater mInflater;

    public ActivityStreamAdapter(Context context, int resource, List<ActivityStreamItem> objects) {
        super(context, resource, objects);
        mResource = resource;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ActivityStreamItem item = getItem(position);
        View view;

        if (convertView == null) {
            view = mInflater.inflate(mResource, parent, false);
        } else {
            view = convertView;
        }

        return view;
    }

}

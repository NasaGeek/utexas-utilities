
package com.nasageek.utexasutilities.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.model.canvas.File;

public class FileAdapter extends ArrayAdapter<File> {

    private int mResource;
    private LayoutInflater mInflater;

    public FileAdapter(Context context, int resource, List<File> objects) {
        super(context, resource, objects);
        mResource = resource;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        File file = getItem(position);
        View view;

        if (convertView == null) {
            view = mInflater.inflate(R.layout.file_view, null, false);
        } else {
            view = convertView;
        }

        TextView nameView = (TextView) view.findViewById(R.id.file_name);
        TextView filesizeView = (TextView) view.findViewById(R.id.file_size);

        nameView.setText(file.display_name);
        filesizeView.setText("Filesize: " + String.format("%,.1f", (file.size / 1000.0)) + " KB");

        return view;
    }
}

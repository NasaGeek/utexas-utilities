
package com.nasageek.utexasutilities.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nasageek.utexasutilities.MyPair;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.model.CourseMapItem;

import java.util.ArrayList;

public class CourseMapAdapter extends ArrayAdapter<MyPair<CourseMapItem, ArrayList>> {

    private Context con;
    private ArrayList<MyPair<CourseMapItem, ArrayList>> items;
    private LayoutInflater li;

    public CourseMapAdapter(Context c, ArrayList<MyPair<CourseMapItem, ArrayList>> items) {
        super(c, 0, items);
        con = c;
        this.items = items;
        li = (LayoutInflater) con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {

        return items.size();
    }

    @Override
    public MyPair<CourseMapItem, ArrayList> getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int i) {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Boolean isFolder = true;
        MyPair<CourseMapItem, ArrayList> item = items.get(position);

        String title = item.first.getName();
        if (item.second.size() == 0) {
            isFolder = false;
        }

        ViewGroup lin = (ViewGroup) convertView;

        if (lin == null) {
            lin = (ViewGroup) li.inflate(R.layout.coursemap_item_view, null, false);
        }

        TextView itemName = (TextView) lin.findViewById(R.id.coursemap_item_name);
        ImageView folder = (ImageView) lin.findViewById(R.id.coursemap_folder);

        folder.setVisibility(isFolder ? View.VISIBLE : View.INVISIBLE);
        itemName.setText(title);
        return lin;
    }
}


package com.nasageek.utexasutilities.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.model.BBGrade;

public class GradesAdapter extends ArrayAdapter<BBGrade> {

    private Context con;
    private ArrayList<BBGrade> items;
    private LayoutInflater li;

    public GradesAdapter(Context c, ArrayList<BBGrade> items) {
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
    public BBGrade getItem(int position) {

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
        BBGrade grade = items.get(position);

        String title = grade.getName();
        String value = null;
        if (grade.getNumGrade().equals(-1)) {
            value = "-";
        } else if (grade.getNumGrade().equals(-2)) {
            value = grade.getGrade();
        } else {
            value = grade.getNumGrade() + "/" + grade.getNumPointsPossible();
        }
        ViewGroup lin = (ViewGroup) convertView;

        if (lin == null) {
            lin = (RelativeLayout) li.inflate(R.layout.grade_item_view, null, false);
        }

        TextView gradeName = (TextView) lin.findViewById(R.id.grade_name);
        TextView gradeValue = (TextView) lin.findViewById(R.id.grade_value);
        ImageView commentImg = (ImageView) lin.findViewById(R.id.comment_available_img);

        if (grade.getComment().equals("No comments")) {
            commentImg.setVisibility(View.INVISIBLE);
        } else {
            commentImg.setVisibility(View.VISIBLE);
        }
        gradeName.setText(title);
        gradeValue.setText(value);

        return lin;
    }
}


package com.nasageek.utexasutilities.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.model.canvas.Assignment;

import java.util.List;

public class AssignmentAdapter extends ArrayAdapter<Assignment> {

    private int mResource;
    private LayoutInflater mInflater;

    public AssignmentAdapter(Context context, int resource, List<Assignment> objects) {
        super(context, resource, objects);
        mResource = resource;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Assignment assignment = getItem(position);
        View view;

        if (convertView == null) {
            view = mInflater.inflate(mResource, parent, false);
        } else {
            view = convertView;
        }
        view.findViewById(R.id.comment_available_img).setVisibility(View.INVISIBLE);

        String score = "-";
        // might check grading_type here, too. Need some sample data for
        // letter_grade grade_type
        if (assignment.submission != null && assignment.submission.score != null) {
            score = assignment.submission.score;
        }

        ((TextView) view.findViewById(R.id.grade_name)).setText(assignment.name);
        ((TextView) view.findViewById(R.id.grade_value)).setText(score + "/"
                + assignment.points_possible);

        return view;
    }

}

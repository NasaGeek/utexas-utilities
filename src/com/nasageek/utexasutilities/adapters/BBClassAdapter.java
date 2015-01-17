
package com.nasageek.utexasutilities.adapters;

import android.content.Context;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nasageek.utexasutilities.MyPair;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.model.BBClass;

import java.util.List;

public class BBClassAdapter extends StickyHeaderAdapter<BBClass> {
    private LayoutInflater li;
    private Boolean longform;

    public BBClassAdapter(Context con, List<MyPair<String, List<BBClass>>> objects) {
        super(objects);
        li = (LayoutInflater) con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        longform = PreferenceManager.getDefaultSharedPreferences(con).getBoolean(
                "blackboard_class_longform", false);
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
    public View getAmazingView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        ViewGroup res = (ViewGroup) convertView;

        if (res == null) {
            res = (ViewGroup) li.inflate(R.layout.bbclass_item_view, parent, false);
            holder = new ViewHolder();
            holder.idview = (TextView) res.findViewById(R.id.bb_class_id);
            holder.nameview = (TextView) res.findViewById(R.id.bb_class_name);
            res.setTag(holder);
        } else {
            holder = (ViewHolder) res.getTag();
        }

        BBClass bbclass = getItem(position);
        String unique = "";

        if (!longform) {
            if (!bbclass.isFullCourseIdTooShort()) {
                if (bbclass.isCourseIdAvailable()) {
                    holder.idview
                            .setText(bbclass.getCourseId() + " - " + bbclass.getUnique() + " ");
                } else {
                    holder.idview.setText(bbclass.getUnique());
                }

            } else {
                holder.idview.setText(bbclass.getCourseId());
            }
        } else // probably not even necessary anymore, necessary checking is
               // done in the if-statement
        {
            unique = bbclass.getFullCourseid();
            // id not set because unique will contain ID and Unique number
            holder.idview.setText(unique);
        }

        holder.nameview.setText(bbclass.getName());

        return res;
    }

    class ViewHolder {
        TextView nameview;
        TextView idview;
    }
}

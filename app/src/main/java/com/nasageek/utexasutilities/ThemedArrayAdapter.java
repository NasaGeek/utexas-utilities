package com.nasageek.utexasutilities;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by chris on 9/13/15.
 */
public class ThemedArrayAdapter<T> extends ArrayAdapter<T> implements ThemedSpinnerAdapter {

    private final Helper mDropDownHelper;
    private int dropdownResId;

    public ThemedArrayAdapter(Context context, int resource, T[] items) {
        super(context, resource, items);
        mDropDownHelper = new Helper(context);
    }

    @Override
    public void setDropDownViewResource(@LayoutRes int resId) {
        super.setDropDownViewResource(resId);
        dropdownResId = resId;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView == null) {
            // Inflate the drop down using the helper's LayoutInflater
            LayoutInflater inflater = mDropDownHelper.getDropDownViewInflater();
            view = inflater.inflate(dropdownResId, parent, false);
        } else {
            view = convertView;
        }

        if (getItem(position) instanceof CharSequence) {
            ((TextView) view).setText((CharSequence) getItem(position));
        } else {
            ((TextView) view).setText(getItem(position).toString());
        }
        return view;
    }

    @Override
    public void setDropDownViewTheme(Resources.Theme theme) {
        mDropDownHelper.setDropDownViewTheme(theme);
    }

    @Override
    public Resources.Theme getDropDownViewTheme() {
        return mDropDownHelper.getDropDownViewTheme();
    }
}

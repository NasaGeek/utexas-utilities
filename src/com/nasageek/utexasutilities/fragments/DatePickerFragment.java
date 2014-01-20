
package com.nasageek.utexasutilities.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.DatePicker;

import com.actionbarsherlock.app.SherlockFragment;

public class DatePickerFragment extends SherlockFragment {
    public DatePickerFragment() {
    }

    public static DatePickerFragment newInstance(String title) {
        Bundle args = new Bundle();
        args.putString("title", title);
        DatePickerFragment dpf = new DatePickerFragment();
        dpf.setArguments(args);

        return dpf;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // View view = new View(getActivity());
        // view.
        // FrameLayout layout = new FrameLayout(getSherlockActivity())
        DatePicker picker = new DatePicker(getSherlockActivity());
        picker.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));
        return picker;
    }
}

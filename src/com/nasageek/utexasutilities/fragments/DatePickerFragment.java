
package com.nasageek.utexasutilities.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.DatePicker;


public class DatePickerFragment extends Fragment {
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
        // FrameLayout layout = new FrameLayout(getActivity())
        DatePicker picker = new DatePicker(getActivity());
        picker.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));
        return picker;
    }
}

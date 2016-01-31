package com.nasageek.utexasutilities;

import android.os.Bundle;
import android.support.v7.preference.EditTextPreferenceDialogFragmentCompat;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;

/**
 * Created by chris on 9/9/15.
 */
public class PasswordEditTextPreferenceDialogFragmentCompat extends EditTextPreferenceDialogFragmentCompat {

    public static PasswordEditTextPreferenceDialogFragmentCompat newInstance(String key) {
        final PasswordEditTextPreferenceDialogFragmentCompat
                fragment = new PasswordEditTextPreferenceDialogFragmentCompat();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        ((EditText)view.findViewById(android.R.id.edit)).setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    }
}

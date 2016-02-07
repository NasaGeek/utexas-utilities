package com.nasageek.utexasutilities.fragments;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;

import com.commonsware.cwac.security.RuntimePermissionUtils;
import com.nasageek.utexasutilities.activities.ScheduleActivity;

/**
 * Created by chris on 2/6/16.
 */
public abstract class ExportScheduleDialogFragment extends AppCompatDialogFragment {

    protected RuntimePermissionUtils permissionUtils;

    @Override
    public void onResume() {
        super.onResume();
        // User must grant permission to launch the Dialog, but make sure they haven't
        // tried anything sneaky like revoking permissions while the Dialog is still open
        if (!permissionUtils.hasPermission(Manifest.permission.READ_CALENDAR)) {
            ((ScheduleActivity) getActivity())
                    .showSnackbar("Calendar permission revoked, please grant it again");
            getDialog().dismiss();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissionUtils = new RuntimePermissionUtils(getContext());
    }
}

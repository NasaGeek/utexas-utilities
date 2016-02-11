
package com.nasageek.utexasutilities.activities;

import android.app.Dialog;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.widget.TextView;

import com.google.android.gms.common.GoogleApiAvailability;
import com.nasageek.utexasutilities.R;

public class AboutMeActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aboutme_layout);
        setupActionBar();

        // force the License Dialog link to be underlined so it looks "linky"
        TextView licenseView = (TextView) findViewById(R.id.library_license_link);
        SpannableString underlinedLicenseLink = new SpannableString(
                getString(R.string.library_license_link));
        underlinedLicenseLink.setSpan(new UnderlineSpan(), 0, underlinedLicenseLink.length(), 0);
        licenseView.setText(underlinedLicenseLink);
        licenseView.setOnClickListener(v -> {
            FragmentManager fm = getSupportFragmentManager();
            LibraryLicenseDialog libraryLicenseDlg = new LibraryLicenseDialog();
            libraryLicenseDlg.show(fm, "fragment_license");
        });
        // do the same thing with the Privacy Policy link
        TextView policyView = (TextView) findViewById(R.id.privacy_policy_link);
        SpannableString underlinedPolicyLink = new SpannableString(
                getString(R.string.privacy_policy_link));
        underlinedPolicyLink.setSpan(new UnderlineSpan(), 0, underlinedPolicyLink.length(), 0);
        policyView.setText(underlinedPolicyLink);
        policyView.setOnClickListener(v -> {
            FragmentManager fm = getSupportFragmentManager();
            PrivacyPolicyDialog privacyPolicyDlg = new PrivacyPolicyDialog();
            privacyPolicyDlg.show(fm, "fragment_privacy_policy");
        });

        TextView versionNumberView = (TextView) findViewById(R.id.version);
        String versionName = "";
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            // of course UTilities is installed...
            e.printStackTrace();
        }
        versionNumberView.setText(versionName);
    }

    public static class PrivacyPolicyDialog extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setMessage(getString(R.string.privacy_policy))
                    .setNeutralButton("Okay", null)
                    .setTitle("Privacy Policy")
                    .create();
        }
    }

    public static class LibraryLicenseDialog extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setTitle("Licenses and Legal Notices")
                    .setMessage(getString(R.string.licenses) + "\n\n" + "Legal Notices:" + "\n\n"
                        + GoogleApiAvailability.getInstance().getOpenSourceSoftwareLicenseInfo(getActivity()))
                    .setNeutralButton("Okay", null)
                    .create();
        }
    }
}

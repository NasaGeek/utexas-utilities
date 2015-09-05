
package com.nasageek.utexasutilities.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.nasageek.utexasutilities.R;

public class AboutMeActivity extends BaseActivity {
    private ActionBar actionbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aboutme_layout);

        actionbar = getSupportActionBar();
        actionbar.setTitle("About");
        actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionbar.setHomeButtonEnabled(true);
        actionbar.setDisplayHomeAsUpEnabled(true);

        // force the License Dialog link to be underlined so it looks "linky"
        TextView licenseView = (TextView) findViewById(R.id.library_license_link);
        SpannableString underlinedLicenseLink = new SpannableString(
                getString(R.string.library_license_link));
        underlinedLicenseLink.setSpan(new UnderlineSpan(), 0, underlinedLicenseLink.length(), 0);
        licenseView.setText(underlinedLicenseLink);
        licenseView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                FragmentManager fm = getSupportFragmentManager();
                LibraryLicenseDialog libraryLicenseDlg = new LibraryLicenseDialog();
                libraryLicenseDlg.show(fm, "fragment_license");
            }
        });
        // do the same thing with the Privacy Policy link
        TextView policyView = (TextView) findViewById(R.id.privacy_policy_link);
        SpannableString underlinedPolicyLink = new SpannableString(
                getString(R.string.privacy_policy_link));
        underlinedPolicyLink.setSpan(new UnderlineSpan(), 0, underlinedPolicyLink.length(), 0);
        policyView.setText(underlinedPolicyLink);
        policyView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                FragmentManager fm = getSupportFragmentManager();
                PrivacyPolicyDialog privacyPolicyDlg = new PrivacyPolicyDialog();
                privacyPolicyDlg.show(fm, "fragment_privacy_policy");
            }
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

        public PrivacyPolicyDialog() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder build = new AlertDialog.Builder(getActivity());
            build.setMessage(getString(R.string.privacy_policy)).setNeutralButton("Okay", null)
                    .setTitle("Privacy Policy");
            return build.create();
        }
    }

    public static class LibraryLicenseDialog extends DialogFragment {

        public LibraryLicenseDialog() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder build = new AlertDialog.Builder(getActivity());
            String licenseText = getString(R.string.licenses) + "\n\n" + "Legal Notices:" + "\n\n"
                    + GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(getActivity());

            // licenseTextView = (TextView)
            // view.findViewById(R.id.license_text);
            // licenseTextView.setText(licenseText.getText()+"\n\n"+"Legal Notices:"+"\n\n"+
            // GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(AboutMeActivity.this));
            build.setMessage(licenseText).setNeutralButton("Okay", null)
                    .setTitle("Licenses and Legal Notices");
            return build.create();
        }
    }
}

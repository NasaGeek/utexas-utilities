package com.nasageek.utexasutilities.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.R.layout;

public class AboutMeActivity extends SherlockFragmentActivity 
{
	private ActionBar actionbar;
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aboutme_layout);
		
		actionbar = getSupportActionBar();
		actionbar.setTitle("About");
		actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionbar.setHomeButtonEnabled(true);
		actionbar.setDisplayHomeAsUpEnabled(true);
		
		//force the License Dialog link to be underlined so it looks "linky"
		TextView licenseView = (TextView) findViewById(R.id.library_license_link);
		SpannableString underlinedLicenseLink = new SpannableString(getString(R.string.library_license_link));
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
		//do the same thing with the Privacy Policy link
		TextView policyView = (TextView) findViewById(R.id.privacy_policy_link);
		SpannableString underlinedPolicyLink = new SpannableString(getString(R.string.privacy_policy_link));
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
			//come on like I need to put anything here, of course UTilities is installed
			e.printStackTrace();
		}
		versionNumberView.setText(versionName);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	int id = item.getItemId();
    	switch(id) {
	    	case android.R.id.home:
	            // app icon in action bar clicked; go home
	            super.onBackPressed();
	            break;
    	}
    	return false;
	}

	public static class PrivacyPolicyDialog extends SherlockDialogFragment {
	//	private TextView policyText;
		private Button dismissButton;
		
		public PrivacyPolicyDialog() {}
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			View view = getActivity().getLayoutInflater().inflate(R.layout.privacy_policy_dialog_fragment, null);
			AlertDialog.Builder build = new AlertDialog.Builder(getActivity());
			build.setMessage(getString(R.string.privacy_policy)).
			setNeutralButton("Okay", null).
			setTitle("Privacy Policy");
		    return build.create();		
		}
	}
	
	public static class LibraryLicenseDialog extends SherlockDialogFragment {
		private TextView licenseTextView;
		private Button dismissButton;
		
		public LibraryLicenseDialog() {}
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			View view = getActivity().getLayoutInflater().inflate(R.layout.license_dialog_fragment, null);
			AlertDialog.Builder build = new AlertDialog.Builder(getActivity());
			String licenseText = getString(R.string.licenses) + "\n\n" + "Legal Notices:" + "\n\n" + 
					GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(getActivity());
			
			
	//		licenseTextView = (TextView) view.findViewById(R.id.license_text);
	//		licenseTextView.setText(licenseText.getText()+"\n\n"+"Legal Notices:"+"\n\n"+ 
	//					GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(AboutMeActivity.this));
			build.setMessage(licenseText).
			setNeutralButton("Okay", null).
			setTitle("Licenses and Legal Notices");
		    return build.create();		
		}
	}
}

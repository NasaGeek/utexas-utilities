package com.nasageek.utexasutilities.activities;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
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
		}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
	    	int id = item.getItemId();
	    	switch(id)
	    	{
		    	case android.R.id.home:
		            // app icon in action bar clicked; go home
		            super.onBackPressed();
		            break;
	    	}
	    	return false;
	}

	class LibraryLicenseDialog extends SherlockDialogFragment
	{
		private TextView licenseText;
		private Button dismissButton;
		
		public LibraryLicenseDialog() {}
		
		@Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			 View view = inflater.inflate(R.layout.license_dialog_fragment, container);
			 
			
			licenseText = (TextView) view.findViewById(R.id.license_text);
			licenseText.setText(licenseText.getText()+"\n\n"+"Legal Notices:"+"\n\n"+ 
						GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(AboutMeActivity.this));
	        dismissButton = (Button) view.findViewById(R.id.license_dismiss_button);
	        dismissButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					LibraryLicenseDialog.this.dismiss();
				}
			});
	        getDialog().setTitle("Licenses and Legal Notices");

	        return view;
	    }
	}
}

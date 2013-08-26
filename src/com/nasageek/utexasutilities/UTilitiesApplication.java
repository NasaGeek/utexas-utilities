package com.nasageek.utexasutilities;

import android.app.Application;
import org.acra.*;
import org.acra.annotation.*;
import org.acra.ReportField;

@ReportsCrashes(
	      formKey = "", // This is required for backward compatibility but not used
	      customReportContent = { ReportField.ANDROID_VERSION, ReportField.APP_VERSION_CODE, ReportField.APP_VERSION_NAME,
	    		  				  ReportField.BRAND, ReportField.BUILD, ReportField.PACKAGE_NAME, ReportField.INSTALLATION_ID, 
	    		  				  ReportField.PHONE_MODEL, ReportField.PRODUCT, ReportField.REPORT_ID, ReportField.STACK_TRACE, 
	    		  				  ReportField.USER_APP_START_DATE, ReportField.USER_CRASH_DATE, ReportField.CUSTOM_DATA},
	      httpMethod = org.acra.sender.HttpSender.Method.PUT,
	      reportType = org.acra.sender.HttpSender.Type.JSON,
	      formUri = "http://utexasutilities.iriscouch.com/acra-utexasutilities/_design/acra-storage/_update/report",
	      formUriBasicAuthLogin = "releasereporter",
	      formUriBasicAuthPassword = "raebpcorterpxayszsword"
	  )

public class UTilitiesApplication extends Application {

	 @Override
     public void onCreate() {
         super.onCreate();

         // The following line triggers the initialization of ACRA
         ACRA.init(this);
     }
}

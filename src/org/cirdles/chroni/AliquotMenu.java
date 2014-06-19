package org.cirdles.chroni;

import java.io.File;
import java.util.ArrayList;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class AliquotMenu extends Activity{
	
	private Button aliquotFileSelectButton, aliquotFileSubmitButton, aliquotIGSNSubmitButton, aliquotURLButton;
	private EditText aliquotFileSelectText, aliquotIGSNText, aliquotURLText;
	private String selectedAliquot, aliquotIGSN, aliquotURL, aliquotLocation, aliquot;	// the Aliquot values
	public static boolean aliquotFound;
	private boolean invalidFile = false; // true if file attempted to be downloaded is invalid

	// Base URLs for IGSN downloads
	public static String BASE_ALIQUOT_URI = "http://www.geochronportal.org/getxml.php?igsn=";
//	public static String BASE_ALIQUOT_URI = "http://picasso.kgs.ku.edu/geochron/getxml.php?igsn=";
	public static String BASE_SAMPLE_URI = "http://www.geosamples.org/display.php?igsn=";
		
	private static final int REQUEST_PICK_FILE = 1;	
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(android.R.style.Theme_Holo);
		setContentView(R.layout.aliquot_select);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
     // Information about Aliquot File
     		aliquotFileSelectButton = (Button) findViewById(R.id.aliquotFileSelectButton);
     		aliquotFileSelectButton.setOnClickListener(new View.OnClickListener() {
     			public void onClick(View v) {
     				Intent openFilePicker = new Intent("android.intent.action.FILEPICKER");
     				openFilePicker.putExtra("Default_Directory", "Aliquot");
     				startActivity(openFilePicker);
     		    	}
     			});	

     		aliquotFileSubmitButton = (Button) findViewById(R.id.aliquotFileSubmitButton);
     		aliquotFileSubmitButton.setOnClickListener(new View.OnClickListener() {
     			public void onClick(View v) {
     		    	Intent openMainMenu = new Intent("android.intent.action.DISPLAY");
     		    	startActivity(openMainMenu);		    		
     		    	}
     			});	
     		
     		aliquotFileSelectText = (EditText) findViewById(R.id.aliquotFileSelectText);		
     		if(getIntent().hasExtra("AliquotXMLFileName")){
     			selectedAliquot = getIntent().getStringExtra("AliquotXMLFileName");
    			String[] absoluteFileName = selectedAliquot.split("/");
    			String fileName = absoluteFileName[absoluteFileName.length - 1];
     			aliquotFileSelectText.setText(fileName);
     		}

     		// Information about Aliquot IGSN
     		aliquotIGSNText = (EditText) findViewById(R.id.aliquotIGSNText);
     		
     		aliquotIGSNSubmitButton = (Button) findViewById(R.id.aliquotIGSNSubmitButton);
     		aliquotIGSNSubmitButton.setOnClickListener(new View.OnClickListener() {
     			public void onClick(View v) {				
     				aliquotIGSN = aliquotIGSNText.getText().toString().toUpperCase();

     				// Downloads Aliquot file
     				URLFileReader downloader = new URLFileReader(AliquotMenu.this, "AliquotMenu", makeURI(BASE_ALIQUOT_URI, aliquotIGSN), "igsn");	
     		    	Intent openMainMenu = new Intent("android.intent.action.DISPLAY");
     		    	startActivity(openMainMenu);		    		
     		    	}
     			});	
     		
     		// Information about Aliquot URL
     		aliquotURLText = (EditText) findViewById(R.id.aliquotURLText);
     		
     		aliquotURLButton = (Button) findViewById(R.id.aliquotURLButton);
     		aliquotURLButton.setOnClickListener(new View.OnClickListener() {
     			public void onClick(View v) {
     				aliquotURL = aliquotURLText.getText().toString();

     				// Downloads Aliquot file from URL
     				URLFileReader downloader = new URLFileReader(AliquotMenu.this, "AliquotMenu", aliquotURL, "url");	
     				Intent openMainMenu = new Intent("android.intent.action.DISPLAY");
     				openMainMenu.putExtra("Url", aliquotURL);
     		    	startActivity(openMainMenu);	
     		    	}
     			});
     		
     }
		
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == RESULT_OK) {
			switch(requestCode) {
			case REQUEST_PICK_FILE:
				if(data.hasExtra(FilePickerActivity.EXTRA_FILE_PATH)) {
					// Get the file path
					File f = new File(data.getStringExtra(FilePickerActivity.EXTRA_FILE_PATH));
					
					aliquotLocation = f.getPath();
					aliquotFound = true;
					
					String[] aliquotName = aliquotLocation.toString().split("/");
					aliquot = aliquotName[aliquotName.length-1];
					
					// Set the file path text view
					aliquotFileSelectText.setText(aliquot);
				}
			}
		}
	}
	
	/*
	 * Creates URL from the constant geochron URL and IGSN
	 */
	public final static String makeURI(String baseURL, String IGSN){
		String URI = baseURL + IGSN;
		// Will create unique URL and password if credentials required
//		if(ViewUtil.privateIGSN){
//			URI += "&username="+
//			ViewUtil.USERNAME+
//			"&password="+
//			ViewUtil.PASSWORD;
//		}
		return URI;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	public boolean isInvalidFile() {
		return invalidFile;
	}

	public static void setInvalidFile(boolean invalidFile) {
		invalidFile = invalidFile;
	}

	
	 @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	        // Handle item selection
	        switch (item.getItemId()) {
	        	case R.id.returnToMenu:
	        		Intent openMainMenu = new Intent("android.intent.action.MAINMENU");
	        		startActivity(openMainMenu);
	        		return true;
	            case R.id.editProfileMenu:
		        	Intent openUserProfile = new Intent("android.intent.action.USERPROFILE");
		        	startActivity(openUserProfile);
		            return true;
		        case R.id.helpMenu:
					Intent openHelpBlog = new Intent(Intent.ACTION_VIEW, Uri.parse("http://joyenettles.blogspot.com"));
					startActivity(openHelpBlog);
		        	return true;
		        case R.id.exitProgram:
	                finish();
	                System.exit(0);
	                
//	            case R.id.deleteFileMenu:
//	            	Intent openReportSettingsMenu = new Intent("android.intent.action.REPORTSETTINGSMENU");
//					startActivity(openReportSettingsMenu);
//	                return true;
//	            case R.id.renameFileMenu:
//	            	Intent openAliquotMenu = new Intent("android.intent.action.ALIQUOTMENU");
//					startActivity(openAliquotMenu);
//	                return true;
//	            case R.id.defaultFileMenu:
//	            	Intent openAliquotMenu = new Intent("android.intent.action.ALIQUOTMENU");
//					startActivity(openAliquotMenu);
//	                return true;

//	            case R.id.selectAliquotMenu:
//	            	Intent openAliquotMenu = new Intent("android.intent.action.ALIQUOTMENU");
//					startActivity(openAliquotMenu);
//	                return true;
//	            case R.id.selectReportSettingsMenu:
//	            	Intent openReportSettingsMenu = new Intent("android.intent.action.REPORTSETTINGSMENU");
//					startActivity(openReportSettingsMenu);
//	                return true;
	                
	             default:
	                return super.onOptionsItemSelected(item);
	        }
	    }

}

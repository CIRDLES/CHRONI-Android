package com.example.cirdles;

import java.io.File;
import java.util.ArrayList;

import android.os.Bundle;
import android.os.Environment;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class AliquotMenu extends Activity implements OnClickListener{
	
	private Button aliquotFileSelectButton, aliquotFileSubmitButton, aliquotIGSNSubmitButton, aliquotURLButton;
	private EditText aliquotFileSelectText, aliquotIGSNText, aliquotURLText;
	private String selectedAliquot, aliquotIGSN, aliquotURL, aliquotLocation, aliquot;	// the Aliquot values
	public static boolean aliquotFound;

	// Base URLs for IGSN downloads
	public static String BASE_ALIQUOT_URI = "http://www.geochronportal.org/getxml.php?igsn=";
//	public static String BASE_ALIQUOT_URI = "http://picasso.kgs.ku.edu/geochron/getxml.php?igsn=";
	public static String BASE_SAMPLE_URI = "http://www.geosamples.org/display.php?igsn=";
		
	private static final int REQUEST_PICK_FILE = 1;	
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen);
		setContentView(R.layout.aliquot_select);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        // set option menu if has no hardware menu key
        boolean hasMenu = ViewConfiguration.get(this).hasPermanentMenuKey();
        if(!hasMenu){
            //getWindow().setFlags(0x08000000, 0x08000000);
            try {
                getWindow().addFlags(WindowManager.LayoutParams.class.getField("FLAG_NEEDS_MENU_KEY").getInt(null));
              }
              catch (NoSuchFieldException e) {
                // Ignore since this field won't exist in most versions of Android
              }
              catch (IllegalAccessException e) {
                Log.w("Optionmenus", "Could not access FLAG_NEEDS_MENU_KEY in addLegacyOverflowButton()", e);
              }
        }
        
		// Information about Aliquot File
		aliquotFileSelectButton = (Button) findViewById(R.id.aliquotFileSelectButton);
		aliquotFileSelectButton.setOnClickListener(this);

		aliquotFileSubmitButton = (Button) findViewById(R.id.aliquotFileSubmitButton);
		aliquotFileSubmitButton.setOnClickListener(this);
		
		aliquotFileSelectText = (EditText) findViewById(R.id.aliquotFileSelectText);		
		if(getIntent().hasExtra("AliquotXMLFileName")){
			selectedAliquot = getIntent().getStringExtra("AliquotXMLFileName");
			aliquotFileSelectText.setText(selectedAliquot);
		}

		// Information about Aliquot IGSN
		aliquotIGSNText = (EditText) findViewById(R.id.aliquotIGSNText);
		aliquotIGSNSubmitButton = (Button) findViewById(R.id.aliquotIGSNSubmitButton);
		aliquotIGSNSubmitButton.setOnClickListener(this);
		
		// Information about Aliquot URL
		aliquotURLText = (EditText) findViewById(R.id.aliquotURLText);
		aliquotURLButton = (Button) findViewById(R.id.aliquotURLButton);
		aliquotURLButton.setOnClickListener(this);
		
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.aliquotFileSelectButton:
			// Create a new Intent for the file picker activity
			Intent intent = new Intent(this, FilePickerActivity.class);
		
			// Show hidden files
			//intent.putExtra(FilePickerActivity.EXTRA_SHOW_HIDDEN_FILES, true);
			
			// Only make .xml files visible
			ArrayList<String> extensions = new ArrayList<String>();
			extensions.add(".xml");
			intent.putExtra(FilePickerActivity.EXTRA_ACCEPTED_FILE_EXTENSIONS, extensions);
			
			// Sets the Initial Directory
			intent.putExtra("Default_Directory", "Aliquot");
			
			// Start the activity
			startActivityForResult(intent, REQUEST_PICK_FILE);
			break;
			
		case R.id.aliquotFileSubmitButton:
			Intent openMainMenu = new Intent("android.intent.action.DISPLAY");
	    	startActivity(openMainMenu);
	    	break;
	    	
		case R.id.aliquotIGSNSubmitButton:
			if(aliquotIGSNText.getText() != null){
				aliquotIGSN = aliquotIGSNText.getText().toString().toUpperCase();
				// Downloads Aliquot file
				URLFileReader IGSNdownloader = new URLFileReader(AliquotMenu.this, "AliquotMenu", makeURI(BASE_ALIQUOT_URI, aliquotIGSN), "igsn");	
				Intent openMainMenu1 = new Intent("android.intent.action.DISPLAY");
				startActivity(openMainMenu1);				
				break;
			}
			
		case R.id.aliquotURLButton:
			if(aliquotURLText.getText() != null){
			aliquotURL = aliquotURLText.getText().toString();
			// Downloads Aliquot file from URL
			URLFileReader URLDownloader = new URLFileReader(AliquotMenu.this, "AliquotMenu", aliquotURL, "url");	
			Intent openMainMenu2 = new Intent("android.intent.action.DISPLAY");
			openMainMenu2.putExtra("Url", aliquotURL);
	    	startActivity(openMainMenu2);	
	    	break;
			}
		}
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

	
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//	    // Handle item selection
//	    switch (item.getItemId()) {
//	        case R.id.new_game:
//	            newGame();
//	            return true;
//	        case R.id.help:
//	            showHelp();
//	            return true;
//	        default:
//	            return super.onOptionsItemSelected(item);
//	    }
//	}

}

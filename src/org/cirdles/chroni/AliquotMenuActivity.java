package org.cirdles.chroni;

import java.io.File;
import java.util.ArrayList;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ToggleButton;

public class AliquotMenuActivity extends Activity {

    private Button aliquotFileSelectButton, aliquotFileSubmitButton,
	    aliquotIGSNSubmitButton, aliquotURLButton;
    private ToggleButton privateAliquotButton;
    private EditText aliquotFileSelectText, aliquotIGSNText, aliquotURLText;
    private String selectedAliquot, aliquotIGSN, aliquotURL, aliquotLocation,
	    aliquot; // the Aliquot values
    public static boolean aliquotFound;
    private boolean invalidFile = false; // true if file attempted to be downloaded is invalid
    private static boolean privateFile = false;
    private static String geochronUsername, geochronPassword; // the geochron information on file for the user
    public static final String USER_PREFS = "My CIRDLES Settings"; // code to access stored preferences

    // Base URLs for IGSN downloads
    public static String BASE_ALIQUOT_URI = "http://www.geochronportal.org/getxml.php?igsn=";
    // public static String BASE_ALIQUOT_URI = "http://picasso.kgs.ku.edu/geochron/getxml.php?igsn=";
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
		Intent openFilePicker = new Intent(
			"android.intent.action.FILEPICKER");
		openFilePicker.putExtra("Default_Directory", "Aliquot");
		startActivity(openFilePicker);
	    }
	});

	aliquotFileSelectText = (EditText) findViewById(R.id.aliquotFileSelectText);
	if (getIntent().hasExtra("AliquotXMLFileName")) {
	    selectedAliquot = getIntent().getStringExtra("AliquotXMLFileName");
	    String[] absoluteFileName = selectedAliquot.split("/");
	    String fileName = absoluteFileName[absoluteFileName.length - 1];
	    aliquotFileSelectText.setText(fileName);
	}

	aliquotFileSubmitButton = (Button) findViewById(R.id.aliquotFileSubmitButton);
	aliquotFileSubmitButton.setOnClickListener(new View.OnClickListener() {
	    public void onClick(View v) {
		if (aliquotFileSelectText.getText().length() != 0) {
		    Intent openMainMenu = new Intent(
			    "android.intent.action.DISPLAY");
		    openMainMenu.putExtra("AliquotXML", getIntent()
			    .getStringExtra("AliquotXMLFileName"));
		    // TableBuilder.setAliquotPath(getIntent().getStringExtra("AliquotXMLFileName"));
		    // // Sends Aliquot XML path for file parsing
		    // TableBuilder.buildTable();
		    startActivity(openMainMenu);
		}
	    }
	});

	// Information about Aliquot IGSN
	aliquotIGSNText = (EditText) findViewById(R.id.aliquotIGSNText);
    // Checks to see if user profile information has been authenticated for private file access
    retrieveCredentials();
	if (!getGeochronUsername().contentEquals("None")&& !getGeochronPassword().contentEquals("None")) {
		aliquotIGSNText.setHint("Profile information stored. Private files enabled!");
	}	   
	
	privateAliquotButton = (ToggleButton) findViewById(R.id.privateAliquotButton);
//	privateAliquotButton.setOnClickListener(new View.OnClickListener() {
//	    public void onClick(View v) { 
//	    	privateFile = privateAliquotButton.isChecked();
//	    }
//	   	});
	    
	aliquotIGSNSubmitButton = (Button) findViewById(R.id.aliquotIGSNSubmitButton);
	aliquotIGSNSubmitButton.setOnClickListener(new View.OnClickListener() {
	    public void onClick(View v) { 	
		if (aliquotIGSNText.getText().length() != 0) {
		    aliquotIGSN = aliquotIGSNText.getText().toString()
			    .toUpperCase().trim();
		    // Downloads Aliquot file
		    URLFileReader downloader = new URLFileReader(
			    AliquotMenuActivity.this, "AliquotMenu", makeURI(
				    BASE_ALIQUOT_URI, aliquotIGSN), "igsn");
		    Intent openMainMenu = new Intent(
			    "android.intent.action.DISPLAY");
		    startActivity(openMainMenu);
		}
	    }
	});

	// Information about Aliquot URL
	aliquotURLText = (EditText) findViewById(R.id.aliquotURLText);

	aliquotURLButton = (Button) findViewById(R.id.aliquotURLButton);
	aliquotURLButton.setOnClickListener(new View.OnClickListener() {
	    public void onClick(View v) {
		if (aliquotURLText.getText().length() != 0) {
		    aliquotURL = aliquotURLText.getText().toString().trim();
		    // Downloads Aliquot file from URL
		    URLFileReader downloader = new URLFileReader(
			    AliquotMenuActivity.this, "AliquotMenu",
			    aliquotURL, "url");
		    Intent openMainMenu = new Intent(
			    "android.intent.action.DISPLAY");
		    openMainMenu.putExtra("Url", aliquotURL);
		    startActivity(openMainMenu);
		}
	    }
	});
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	if (resultCode == RESULT_OK) {
	    switch (requestCode) {
	    case REQUEST_PICK_FILE:
		if (data.hasExtra(FilePickerActivity.EXTRA_FILE_PATH)) {
		    // Get the file path
		    File f = new File(
			    data.getStringExtra(FilePickerActivity.EXTRA_FILE_PATH));

		    aliquotLocation = f.getPath();
		    aliquotFound = true;

		    String[] aliquotName = aliquotLocation.toString()
			    .split("/");
		    aliquot = aliquotName[aliquotName.length - 1];

		    // Set the file path text view
		    aliquotFileSelectText.setText(aliquot);
		}
	    }
	}
    }
    
    /*
     * Retrieves the currently stored username and password
     * If none present, returns None
     */
    private void retrieveCredentials() {
	SharedPreferences settings = getSharedPreferences(USER_PREFS, 0);
	setGeochronUsername(settings.getString("Geochron Username", "None"));
	setGeochronPassword(settings.getString("Geochron Password", "None"));
    }

    /*
     * Creates URL from the constant geochron URL and IGSN
     */
    public final static String makeURI(String baseURL, String IGSN) {
	String URI = baseURL + IGSN;
	// Will create unique URL and password if credentials required
	 if(privateFile){
		 URI += "&username="+ getGeochronUsername()+"&password="+ getGeochronPassword();
	 }
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

    public static String getGeochronUsername() {
		return geochronUsername;
	}

	public void setGeochronUsername(String geochronUsername) {
		this.geochronUsername = geochronUsername;
	}

	public static String getGeochronPassword() {
		return geochronPassword;
	}

	public void setGeochronPassword(String geochronPassword) {
		this.geochronPassword = geochronPassword;
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
	    Intent openUserProfile = new Intent(
		    "android.intent.action.USERPROFILE");
	    startActivity(openUserProfile);
	    return true;
	case R.id.helpMenu:
	    // Intent openHelpBlog = new Intent(Intent.ACTION_VIEW,
	    // Uri.parse("http://joyenettles.blogspot.com"));
	    // startActivity(openHelpBlog);
	    // return true;
	case R.id.exitProgram:
	    finish();
	    System.exit(0);

	default:
	    return super.onOptionsItemSelected(item);
	}
    }

}

package org.cirdles.chroni;

import java.io.File;

import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ReportSettingsMenuActivity extends Activity {
    private Button reportSettingsFileSelectButton, reportSettingsOpenButton,
	    reportSettingsUrlButton;
    private EditText reportSettingsFileSelectText, reportSettingsUrlText;
    private TextView currentReportSettingsFile; // The Name of the current report settings
    private String selectedReportSettings; // name of Report Settings file that
					   // has been chosen for viewing
    private String reportSettingsUrl; // name of Report Settings URL
    private String absoluteFileName; // path of selected Report Settings file

    private static final String PREF_REPORT_SETTINGS = "Current Report Settings";     // Path of the current report settungs file

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setTheme(android.R.style.Theme_Holo);
	setContentView(R.layout.report_settings_select);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

    // Sets up background
    RelativeLayout layout =(RelativeLayout)findViewById(R.id.reportSettingsBackground);
    layout.setBackground(getResources().getDrawable(R.drawable.background));

	// Directories needed for file locations
	final File chroniDirectory = getDir("CHRONI", Context.MODE_PRIVATE); 
	final File reportSettingsDirectory = new File(chroniDirectory, "Report Settings");

    // Provides a label of the name of the current report settings file
        currentReportSettingsFile = (TextView) findViewById(R.id.currentReportSettingsLabel);
//        String[] reportSettingLabelContents = retrieveReportSettingsFileName().split("/");
//        String reportSettingsLabel = reportSettingLabelContents[reportSettingLabelContents.length-1];
//        currentReportSettingsFile.setText("Current Report Settings: " + reportSettingsLabel);
        currentReportSettingsFile.setText("Current Report Settings: " + splitReportSettingsName(retrieveReportSettingsFileName()));

	// Information about Report Settings file
	reportSettingsFileSelectButton = (Button) findViewById(R.id.reportSettingsFileSelectButton);
	reportSettingsFileSelectButton
		.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
			Intent openFilePicker = new Intent(
				"android.intent.action.FILEPICKER");
			openFilePicker.putExtra("Default_Directory",
				"Report Settings");
			startActivity(openFilePicker);
		    }
		});

	reportSettingsFileSelectText = (EditText) findViewById(R.id.reportSettingsFileSelectText);
	if (getIntent().hasExtra("ReportSettingsXMLFileName")) {
	    selectedReportSettings = getIntent().getStringExtra(
		    "ReportSettingsXMLFileName");
	    String[] absoluteFileName = selectedReportSettings.split("/");
	    String fileName = absoluteFileName[absoluteFileName.length - 1];
	    reportSettingsFileSelectText.setText(fileName);
	}

	reportSettingsOpenButton = (Button) findViewById(R.id.reportSettingsFileOpenButton);
	reportSettingsOpenButton.setOnClickListener(new View.OnClickListener() {
	    public void onClick(View v) {
		if (reportSettingsFileSelectText.getText().length() != 0) {
		    Intent openMainMenu = new Intent(
			    "android.intent.action.DISPLAY");
		    openMainMenu.putExtra("ReportSettingsXML", getIntent()
			    .getStringExtra("ReportSettingsXMLFileName")); // Sends
									   // Report
									   // Setting
									   // XML
									   // path
									   // for
									   // file
									   // parsing
		    // TableBuilder.setReportSettingsPath(getIntent().getStringExtra("ReportSettingsXMLFileName"));
		    // // Sends Aliquot XML path for file parsing
		    // TableBuilder.buildTable();
            saveCurrentReportSettings();
		    startActivity(openMainMenu);
		}
	    }
	});

	// Information about Report Settings URL
	reportSettingsUrlText = (EditText) findViewById(R.id.reportSettingsUrlText);

	reportSettingsUrlButton = (Button) findViewById(R.id.reportSettingsUrlButton);
	reportSettingsUrlButton.setText("Download");
	reportSettingsUrlButton.setOnClickListener(new View.OnClickListener() {
	    public void onClick(View v) {
            // Checks internet connection before downloading files
            ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if (mWifi.isConnected()) {
		if (reportSettingsUrlText.getText().length() != 0) {
		    reportSettingsUrl = reportSettingsUrlText.getText()
			    .toString().trim();

		    // Downloads Report Settings file from URL
		    URLFileReader downloader = new URLFileReader(ReportSettingsMenuActivity.this,   "ReportSettingsMenu", reportSettingsUrl, "url");


            Thread timer = new Thread() {
                public void run() {
                    try {
                        sleep(2500); // gives file download three seconds to complete
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {

            Intent openMainMenu = new Intent("android.intent.action.DISPLAY");
		    setAbsoluteFileName(reportSettingsDirectory + "/" + createFileName("url", reportSettingsUrl) + ".xml");
		    openMainMenu.putExtra("ReportSettingsXML", getAbsoluteFileName());
                        saveCurrentReportSettings();
                        startActivity(openMainMenu);
                    }
                }
            };
            timer.start();
        	}
            }else{
                //Handles lack of wifi connection
                Toast.makeText(ReportSettingsMenuActivity.this, "Please check your internet connection before performing this action.", Toast.LENGTH_LONG).show();
            }
	    }
	});
    }

    /*
	 * Creates file name based on the file's type and URL
	 */
	protected String createFileName(String downloadMethod, String fileUrl) {
		String name = null;			
			//makes name from ending of URL
				String[] URL = fileUrl.split("/");
				name = URL[URL.length-1];
				if (name.contains(".xml")){
					// Removes the file name ending from XML files
					String [] newName = name.split(".xml");
					name = newName[0];
				}
		return name;
	}
    
    public String getAbsoluteFileName() {
		return absoluteFileName;
	}

	public void setAbsoluteFileName(String absoluteFileName) {
		this.absoluteFileName = absoluteFileName;
	}

 /*
 * Accesses current report settings file
 */
    private String retrieveReportSettingsFileName() {
        SharedPreferences settings = getSharedPreferences(PREF_REPORT_SETTINGS, 0);
        return settings.getString("Current Report Settings", "Default Report Settings.xml"); // Gets current RS and if no file there, returns default as the current file
    }

    /*
    Splits report settings file name
     */
    private String splitReportSettingsName(String fileName){
        String[] fileNameParts = fileName.split("/");
        String reportSettingsName = fileNameParts[fileNameParts.length-1];
        return reportSettingsName;
    }

  /*
  * Stores Current Report Settings
  */
    protected void saveCurrentReportSettings() {
        SharedPreferences settings = getSharedPreferences(PREF_REPORT_SETTINGS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("Current Report Settings", getIntent().getStringExtra("ReportSettingsXMLFileName")); // gets chosen file from file browser and stores
        editor.commit(); // Commiting changes
    }

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
	// Inflate the menu; this adds items to the action bar if it is present.
	getMenuInflater().inflate(R.menu.menu, menu);
	return true;
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
	    Intent openHelpBlog = new Intent(Intent.ACTION_VIEW,
		    Uri.parse("http://joyenettles.blogspot.com"));
	    startActivity(openHelpBlog);
	    return true;
	case R.id.exitProgram:
	    finish();
	    System.exit(0);

	default:
	    return super.onOptionsItemSelected(item);
	}
    }
}

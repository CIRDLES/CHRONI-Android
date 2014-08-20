package org.cirdles.chroni;

import java.io.File;

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

public class ReportSettingsMenuActivity extends Activity {
    private Button reportSettingsFileSelectButton, reportSettingsOpenButton,
	    reportSettingsUrlButton;
    private EditText reportSettingsFileSelectText, reportSettingsUrlText;
    private String selectedReportSettings; // name of Report Settings file that
					   // has been chosen for viewing
    private String reportSettingsUrl; // name of Report Settings URL
    private String absoluteFileName; // path of selected Report Settings file
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setTheme(android.R.style.Theme_Holo);
	setContentView(R.layout.report_settings_select);
	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    // Sets up background
    RelativeLayout layout =(RelativeLayout)findViewById(R.id.reportSettingsBackground);
    layout.setBackground(getResources().getDrawable(R.drawable.background));

	// Directories needed for file locations
	final File chroniDirectory = getDir("CHRONI", Context.MODE_PRIVATE); 
	final File reportSettingsDirectory = new File(chroniDirectory, "Report Settings");
	
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
		if (reportSettingsUrlText.getText().length() != 0) {
		    reportSettingsUrl = reportSettingsUrlText.getText()
			    .toString().trim();

		    // Downloads Report Settings file from URL
		    URLFileReader downloader = new URLFileReader(ReportSettingsMenuActivity.this,   "ReportSettingsMenu", reportSettingsUrl, "url");
		    Intent openMainMenu = new Intent("android.intent.action.DISPLAY");
		    setAbsoluteFileName(reportSettingsDirectory + "/" + createFileName("url", reportSettingsUrl) + ".xml");
		    openMainMenu.putExtra("ReportSettingsXML", getAbsoluteFileName());
		    startActivity(openMainMenu);
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

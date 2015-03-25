/* This activity provides the user with the Report Settings file selection menu actions and setup */

package org.cirdles.chroni;

import java.io.File;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
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
    //layout variables
    private Button reportSettingsSelectedFileButton; // file browser button for report settings
    private Button reportSettingsApplyButton;// open button to the display table
    private EditText reportSettingsSelectedFileText; // contains name of the report settings file for viewing

    private String selectedReportSettings; // name of Report Settings file that has been chosen for viewing
    private String reportSettingsUrl; // name of Report Settings URL
    private String absoluteFilePath; // path of selected Report Settings file
    private String finalReportSettingsFileName; //name of the final report settings
    private static final String PREF_REPORT_SETTINGS = "Current Report Settings";// Path of the current report settings file
    private static final String PREF_ALIQUOT = "Current Aliquot";// Path of the current aliquot file

    String aliquotPath; // the path containing the Aliquot file

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // sets up activity layout
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_Holo);
        setContentView(R.layout.report_settings_select);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        // Sets up background
        RelativeLayout layout =(RelativeLayout)findViewById(R.id.reportSettingsBackground);
        layout.setBackground(getResources().getDrawable(R.drawable.background));

        // Provides a label of the name of the current report settings file
        TextView currentReportSettingsFile = (TextView) findViewById(R.id.currentReportSettingsLabel);
        currentReportSettingsFile.setText("Current Report Settings: " + splitReportSettingsName(retrieveReportSettingsFileName()));

        reportSettingsSelectedFileButton = (Button) findViewById(R.id.reportSettingsFileSelectButton);
        reportSettingsSelectedFileButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        // Opens file picker activity to main menu
                        Intent openFilePicker = new Intent("android.intent.action.FILEPICKER");
                        openFilePicker.putExtra("Default_Directory", "Report_Settings_Directory");
                        startActivity(openFilePicker);
                        saveCurrentReportSettings();
                    }
                });

        // Displays the selected report settings on the report settings menu
        reportSettingsSelectedFileText = (EditText) findViewById(R.id.reportSettingsFileSelectText);
        if (getIntent().hasExtra("ReportSettingsXMLFileName")) {
            selectedReportSettings = getIntent().getStringExtra(
                    "ReportSettingsXMLFileName");
            String[] absoluteFileName = selectedReportSettings.split("/");
            String fileName = absoluteFileName[absoluteFileName.length - 1];
            reportSettingsSelectedFileText.setText(fileName);
        }

        // Opens the display screen with the selected report settings file
        reportSettingsApplyButton = (Button) findViewById(R.id.reportSettingsFileOpenButton);
        //Changes button color back to blue if it is not already
        reportSettingsApplyButton.setBackgroundColor(getResources().getColor(R.color.button_blue));
        reportSettingsApplyButton.setTextColor(Color.WHITE);
        reportSettingsApplyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (reportSettingsSelectedFileText.getText().length() != 0) {
                    Intent openDisplayTable = new Intent(
                            "android.intent.action.DISPLAY");
                    openDisplayTable.putExtra("ReportSettingsXML", getIntent().getStringExtra("ReportSettingsXMLFileName")); // Sends selected report settings file to display activity

                    // Changes button color to indicate it has been opened
                    reportSettingsApplyButton.setBackgroundColor(Color.LTGRAY);
                    reportSettingsApplyButton.setTextColor(Color.BLACK);
                    saveCurrentReportSettings();
                    startActivity(openDisplayTable);
                }
            }
        });

        // Returns the user to the display table!
        Button reportSettingsCancelButton = (Button) findViewById(R.id.reportSettingsMenuCancelButton);
        reportSettingsCancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                    Intent openDisplayTable = new Intent(
                            "android.intent.action.DISPLAY");
                    openDisplayTable.putExtra("ReportSettingsXML", getIntent().getStringExtra("ReportSettingsXMLFileName")); // Sends selected report settings file to display activity
                    saveCurrentReportSettings();
                    startActivity(openDisplayTable);
            }
        });
    }

    /*
Requests file name from user and  proceeds to download based on input
 */
    public void requestFileName(){
        AlertDialog.Builder userFileNameAlert = new AlertDialog.Builder(ReportSettingsMenuActivity.this);

        userFileNameAlert.setTitle("Choose a file name");
        userFileNameAlert.setMessage("Enter the desired name of your Report Settings URL file:");

        // Set an EditText view to get user input
        final EditText input = new EditText(ReportSettingsMenuActivity.this);
        userFileNameAlert.setView(input);

        userFileNameAlert.setPositiveButton("Start Download!", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (input.getText().toString().length() != 0) {
                    if(input.getText().toString().contains(".xml")) { // Removes user added .xml
                        String[] inputText = input.getText().toString().split(".xml");
                        String userFileName = inputText[0];
                        setFinalReportSettingsFileName(userFileName); // sets the user file name in the class
                    }else {
                        setFinalReportSettingsFileName(input.getText().toString()); // sets the user file name in the class
                    }
                    URLFileReader downloader = new URLFileReader(
                            ReportSettingsMenuActivity.this, "ReportSettingsMenu",
                            reportSettingsUrl, "url", getFinalReportSettingsFileName()); // Downloads the file and sets user name
                    Toast.makeText(ReportSettingsMenuActivity.this, "Downloading Report Settings...", Toast.LENGTH_LONG).show();
                }
            }
        });

        userFileNameAlert.setNegativeButton("Cancel Download", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        userFileNameAlert.show();
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

    public String getAbsoluteFilePath() {
        return absoluteFilePath;
    }

    public void setAbsoluteFilePath(String absoluteFilePath) {
        this.absoluteFilePath = absoluteFilePath;
    }

    /*
Splits report settings file name returning a displayable version without the entire path
 */
    private String splitReportSettingsName(String fileName){
        String[] fileNameParts = fileName.split("/");
        String reportSettingsName = fileNameParts[fileNameParts.length-1];
        return reportSettingsName;
    }

    /*
    * Accesses current report settings file
    */
    private String retrieveReportSettingsFileName() {
        SharedPreferences settings = getSharedPreferences(PREF_REPORT_SETTINGS, 0);
        return settings.getString("Current Report Settings", "Default Report Settings.xml"); // Gets current RS and if no file there, returns default as the current file
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

    public String getFinalReportSettingsFileName() {
        return finalReportSettingsFileName;
    }

    public void setFinalReportSettingsFileName(String finalReportSettingsFileName) {
        this.finalReportSettingsFileName = finalReportSettingsFileName;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handles menu item selection
        switch (item.getItemId()) {
            case R.id.returnToMenu: // Takes user to main menu
                Intent openMainMenu = new Intent("android.intent.action.MAINMENU");
                startActivity(openMainMenu);
                return true;
            case R.id.editProfileMenu: //Takes user to credentials screen
                Intent openUserProfile = new Intent(
                        "android.intent.action.USERPROFILE");
                startActivity(openUserProfile);
                return true;
            case R.id.historyMenu: //Takes user to credentials screen
                Intent openHistoryTable = new Intent(
                        "android.intent.action.HISTORY");
                startActivity(openHistoryTable);
                return true;
            case R.id.viewAliquotsMenu: // Takes user to aliquot menu
                Intent openAliquotFiles = new Intent(
                        "android.intent.action.FILEPICKER");
                openAliquotFiles.putExtra("Default_Directory",
                        "Aliquot_CHRONI_Directory");
                startActivity(openAliquotFiles);
                return true;
            case R.id.viewReportSettingsMenu: // Takes user to report settings menu
                Intent openReportSettingsFiles = new Intent(
                        "android.intent.action.FILEPICKER");
                openReportSettingsFiles.putExtra("Default_Directory",
                        "Report_Settings_CHRONI_Directory");
                startActivity(openReportSettingsFiles);
                return true;
            case R.id.aboutScreen: // Takes user to about screen
                Intent openAboutScreen = new Intent(
                        "android.intent.action.ABOUT");
                startActivity(openAboutScreen);
                return true;
            case R.id.helpMenu: // Takes user to help blog
                Intent openHelpBlog = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://chronihelpblog.wordpress.com"));
                startActivity(openHelpBlog);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
package org.cirdles.chroni;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Document;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * This activity provides the user with the Report Settings file selection menu actions and setup
 */
public class ReportSettingsMenuActivity extends Activity {
    //layout variables
    private EditText reportSettingsSelectedFileText; // contains name of the report settings file for viewing

    private String selectedReportSettings; // name of Report Settings file that has been chosen for viewing
    private static final String PREF_REPORT_SETTINGS = "Current Report Settings";// Path of the current report settings file

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // sets up activity layout
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_Holo);
        setContentView(R.layout.report_settings_select);

        // Sets up background
        RelativeLayout layout =(RelativeLayout)findViewById(R.id.reportSettingsBackground);
        layout.setBackground(getResources().getDrawable(R.drawable.background));

        // Provides a label of the name of the current report settings file
        TextView currentReportSettingsFile = (TextView) findViewById(R.id.currentReportSettingsLabel);
        currentReportSettingsFile.setText("Current Settings:\n" + splitReportSettingsName(retrieveReportSettingsFileName()));

        Button reportSettingsSelectedFileButton = (Button) findViewById(R.id.reportSettingsFileSelectButton);
        reportSettingsSelectedFileButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        // Opens file picker activity to main menu
                        Intent openFilePicker = new Intent("android.intent.action.FILEPICKER");
                        openFilePicker.putExtra("Default_Directory", "From_Report_Directory");
                        startActivityForResult(openFilePicker, 1);  // Open FilePicker to get back a new Report Settings file
                    }
                });

        // Displays the selected report settings on the report settings menu
        reportSettingsSelectedFileText = (EditText) findViewById(R.id.reportSettingsFileSelectText);

        if (getIntent().hasExtra("ReportSettingsXMLFileName")) {
            selectedReportSettings = getIntent().getStringExtra("ReportSettingsXMLFileName");
            String[] absoluteFileName = selectedReportSettings.split("/");
            String fileName = absoluteFileName[absoluteFileName.length - 1];
            reportSettingsSelectedFileText.setText(fileName);
        }

        // Opens the display screen with the selected report settings file
        Button reportSettingsApplyButton = (Button) findViewById(R.id.reportSettingsFileOpenButton);
        reportSettingsApplyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (reportSettingsSelectedFileText.getText().length() != 0) {

                    if (getIntent().hasExtra("From_Table")) {   // when coming from an Aliquot Display Table
                        if (getIntent().getStringExtra("From_Table").equals("true")) {

                            // if the Report Settings file selected is valid, return to the new table
                            if (validateFile(selectedReportSettings)) {
                                Toast.makeText(ReportSettingsMenuActivity.this, "Changing Report Settings...", Toast.LENGTH_LONG).show();
                                Intent returnReportSettings = new Intent("android.intent.action.DISPLAY");
                                returnReportSettings.putExtra("newReportSettings", "true"); // tells if new report settings have been chosen

                                // tells Intent that it is from a previous table that was opened via the History table
                                if (getIntent().hasExtra("fromHistory")) {
                                    returnReportSettings.putExtra("fromHistory", getIntent().getStringExtra("fromHistory"));
                                    returnReportSettings.putExtra("historyReportSettings", selectedReportSettings);

                                    if (!getIntent().getStringExtra("fromHistory").equals("true"))
                                        // saves RS if Intent was NOT originally from the History table
                                        saveCurrentReportSettings(selectedReportSettings);
                                }

                                else    // saves RS if Intent was NOT originally from the History table (and doesn't have extra)
                                    saveCurrentReportSettings(selectedReportSettings);

                                setResult(RESULT_OK, returnReportSettings);
                                finish();

                            } else  // if it is no valid, display a message
                                Toast.makeText(ReportSettingsMenuActivity.this, "ERROR: Invalid Report Settings XML file.", Toast.LENGTH_LONG).show();

                        }

                    } else {

                        // if the Report Settings file selected is valid, return to the new table
                        if (validateFile(selectedReportSettings)) {
                            Intent openDisplayTable = new Intent("android.intent.action.DISPLAY");
                            openDisplayTable.putExtra("fromHistory", "false");  // tells Intent that it is not from History table

                            saveCurrentReportSettings(selectedReportSettings);
                            startActivity(openDisplayTable);
                        } else  // if it is no valid, display a message
                            Toast.makeText(ReportSettingsMenuActivity.this, "ERROR: Invalid Report Settings XML file.", Toast.LENGTH_LONG).show();

                    }
                }
            }
        });

        // Returns the user to the display table!
        Button reportSettingsCancelButton = (Button) findViewById(R.id.reportSettingsMenuCancelButton);
        reportSettingsCancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                    finish();
            }
        });
    }

    /**
     * Stores Current Report Settings
     *
     * @param filePath the path to the Report Settings file
     */
    protected void saveCurrentReportSettings(String filePath) {
        SharedPreferences settings = getSharedPreferences(PREF_REPORT_SETTINGS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("Current Report Settings", filePath);
        editor.apply(); // Committing changes
    }

    // Gets the result from a FilePicker Activity initiated from a Report Settings Menu (by pressing the + button)
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                if (data.hasExtra("NewReportSettingsXMLFileName")) {
                    String result = data.getStringExtra("NewReportSettingsXMLFileName");
                    getIntent().putExtra("ReportSettingsXMLFileName", result);
                    selectedReportSettings = result;
                    String[] absoluteFileName = selectedReportSettings.split("/");
                    String fileName = absoluteFileName[absoluteFileName.length - 1];
                    reportSettingsSelectedFileText.setText(fileName);
                }
            }
        }
    }


    /**
     * Splits report settings file name returning a displayable version without the entire path
     */
    private String splitReportSettingsName(String fileName){
        String[] fileNameParts = fileName.split("/");
        String name = fileNameParts[fileNameParts.length-1];
        if (name.contains(".xml")) {    // removes '.xml' from end of name
            String[] newParts = name.split(".xml");
            name = newParts[0];
        }
        return name;
    }

    /**
     * Checks an XML file at the specified file path to see if it is a ReportSettings file
     *
     * @param filePath the path to the XML file
     * @return a boolean stating whether it is valid or not
     */
    private boolean validateFile(String filePath) {
        // initializes the end result
        boolean result = false;
        String[] splitPathAtPeriod = filePath.split("\\.");

        if (splitPathAtPeriod.length > 0) { // makes sure there is something to index
            // then makes sure that the file is an XML file
            if (splitPathAtPeriod[splitPathAtPeriod.length - 1].equals("xml")) {
                try {
                    // builds the XML file to parse and checks for validity
                    File xmlFile = new File(filePath);
                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    Document doc = dBuilder.parse(xmlFile);

                    // returns true if the first node is ReportSettings
                    result = doc.getDocumentElement().getNodeName().equals("ReportSettings");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // returns false if there was a different error
        return result;
    }

    /**
     * Accesses current report settings file
     */
    private String retrieveReportSettingsFileName() {
        SharedPreferences settings = getSharedPreferences(PREF_REPORT_SETTINGS, 0);
        return settings.getString("Current Report Settings", "Default Report Settings.xml"); // Gets current RS and if no file there, returns default as the current file
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    /**
     * The purpose of overriding this method is to alter/delete some of the menu items from the default
     * menu, as they are not wanted in this Activity. Doing so prevents the unnecessary stacking of
     * Activities by making the user follow the intended flow of Activities in the application.
     *
     * @param menu the menu that has been inflated in the Activity
     * @return true so that the menu is displayed
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // removes the History item from the menu
        MenuItem historyItem = menu.findItem(R.id.historyMenu);
        historyItem.setVisible(false);

        // removes the Edit Credentials item from the menu
        MenuItem credentialsItem = menu.findItem(R.id.editProfileMenu);
        credentialsItem.setVisible(false);

        MenuItem viewFiles = menu.findItem(R.id.viewFilesMenu);
        viewFiles.setVisible(false);

        // if coming from a Table Activity, changes Main Menu item to say "Back to Table"
        if (getIntent().hasExtra("From_Table")) {
            if (getIntent().getStringExtra("From_Table").equals("true")) {
                MenuItem backItem = menu.findItem(R.id.returnToMenu);
                backItem.setTitle("Back to Table");
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handles menu item selection
        switch (item.getItemId()) {
            case R.id.returnToMenu: // Takes user to main menu by finishing the Activity
                finish();
                return true;
            case R.id.viewAliquotsMenu: // Takes user to aliquot menu
                Intent openAliquotFiles = new Intent(
                        "android.intent.action.FILEPICKER");
                openAliquotFiles.putExtra("Default_Directory",
                        "Aliquot_Directory");
                startActivity(openAliquotFiles);
                return true;
            case R.id.viewReportSettingsMenu: // Takes user to report settings menu
                Intent openReportSettingsFiles = new Intent(
                        "android.intent.action.FILEPICKER");
                openReportSettingsFiles.putExtra("Default_Directory",
                        "From_Report_Directory");
                startActivityForResult(openReportSettingsFiles, 1);
                return true;
            case R.id.importFilesMenu:  // Takes user to import files menu
                Intent importFiles = new Intent(
                        "android.intent.action.IMPORTFILES");
                startActivity(importFiles);
                return true;
            case R.id.viewRootMenu:
                Intent openRootDirectory = new Intent(
                        "android.intent.action.FILEPICKER");
                openRootDirectory.putExtra("Default_Directory",
                        "Root_Directory");
                startActivity(openRootDirectory);
                return true;
            case R.id.aboutScreen: // Takes user to about screen
                Intent openAboutScreen = new Intent(
                        "android.intent.action.ABOUT");
                startActivity(openAboutScreen);
                return true;
            case R.id.helpMenu: // Takes user to help blog
                // Checks internet connection before downloading files
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mobileWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                if (mobileWifi.isConnected()) {
                    Intent openHelpBlog = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(getString(R.string.chroni_help_address)));
                    startActivity(openHelpBlog);

                } else {
                    new AlertDialog.Builder(this).setMessage("You are not connected to WiFi, mobile data rates may apply. " +
                            "Do you wish to continue?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent openHelpBlog = new Intent(Intent.ACTION_VIEW,
                                            Uri.parse(getString(R.string.chroni_help_address)));
                                    startActivity(openHelpBlog);
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
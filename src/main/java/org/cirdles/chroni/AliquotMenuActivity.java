/* This activity provides the user with the aliquot file selection menu actions and setup */

package org.cirdles.chroni;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.w3c.dom.Document;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class AliquotMenuActivity extends Activity {

    // Layout variables
    private Button aliquotFileSubmitButton; // button submits aliquot file for viewing
    private Button igsnDownloadButton; // button submits current inputted IGSN for downloading
    private EditText aliquotSelectedFileText; // holds the currently selected file name
    private EditText igsnText; // holds user-inputted IGSN

    // Global functionality  variables
    private static String geochronUsername, geochronPassword; // the GeoChron information on file for the user
    public static final String USER_PREFS = "My CIRDLES Settings"; // code to access stored preferences

    private static final String PREF_ALIQUOT = "Current Aliquot";   // Path of the current aliquot file

    // Base URLs for IGSN downloads
    public static String BASE_ALIQUOT_URI = "http://www.geochronportal.org/getxml.php?igsn=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Sets up activity layout
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_Holo);
        setContentView(R.layout.aliquot_select);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        RelativeLayout aliquotMenuLayout =(RelativeLayout)findViewById(R.id.aliquotSelectBackground);

        //Places background image on layout after theme overriding
        aliquotMenuLayout.setBackground(getResources().getDrawable(R.drawable.background));

        Button aliquotFileSelectButton = (Button) findViewById(R.id.aliquotFileSelectButton);
        aliquotFileSelectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Opens file picker activity to main menu
                Intent openFilePicker = new Intent("android.intent.action.FILEPICKER");
                openFilePicker.putExtra("Default_Directory", "Aliquot_Directory");
                startActivityForResult(openFilePicker, 1);  // Opens FilePicker and waits for it to return an Extra (SEE onActivityResult())
            }
        });


        aliquotSelectedFileText = (EditText) findViewById(R.id.aliquotFileSelectText); // Contains selected aliquot file name

        aliquotFileSubmitButton = (Button) findViewById(R.id.aliquotFileSubmitButton);
        aliquotFileSubmitButton.setOnClickListener(new View.OnClickListener() {
            // Submits aliquot file to display activity for parsing and displaying in table
            public void onClick(View v) {

                if (aliquotSelectedFileText.getText().length() != 0) {

                    // if coming from a previously created table, change the aliquot
                    if (getIntent().hasExtra("From_Table")) {
                        if (getIntent().getStringExtra("From_Table").equals("true")) {

                            // if the Aliquot selected is valid, return to the new table
                            if (validateFile(getIntent().getStringExtra("AliquotXMLFileName"))) {
                                Toast.makeText(AliquotMenuActivity.this, "Changing Aliquot...", Toast.LENGTH_LONG).show(); // lets user know table is opening
                                Intent returnAliquot = new Intent("android.intent.action.DISPLAY");
                                returnAliquot.putExtra("newAliquot", "true");   // tells if a new Aliquot has been chosen

                                // tells Intent that it is from a previous table that was opened via the History table
                                if (getIntent().hasExtra("fromHistory")) {
                                    returnAliquot.putExtra("fromHistory", getIntent().getStringExtra("fromHistory"));
                                    returnAliquot.putExtra("historyAliquot", getIntent().getStringExtra("AliquotXMLFileName"));

                                    if (!getIntent().getStringExtra("fromHistory").equals("true"))
                                        saveCurrentAliquot();   // saves Aliquot if Intent was NOT originally from the History table
                                }

                                else    // saves Aliquot if Intent was NOT originally from the History table (and doesn't have extra)
                                    saveCurrentAliquot();

                                setResult(RESULT_OK, returnAliquot);
                                finish();

                            } else  // if it is not valid, display a message
                                Toast.makeText(AliquotMenuActivity.this, "ERROR: Invalid Aliquot XML file.", Toast.LENGTH_LONG).show();

                        }

                    } else {

                        // if the Aliquot selected is valid, return to the new table
                        if (validateFile(getIntent().getStringExtra("AliquotXMLFileName"))) {
                            // Makes sure there is a file selected
                            Toast.makeText(AliquotMenuActivity.this, "Opening table...", Toast.LENGTH_LONG).show(); // lets user know table is opening
                            Intent openDisplayTable = new Intent("android.intent.action.DISPLAY"); // Opens display table
                            openDisplayTable.putExtra("fromHistory", "false");  // tells Intent that it is not from History table

                            saveCurrentAliquot();

                            startActivity(openDisplayTable); // Starts display activity

                        } else  // if it is not valid, display a message
                            Toast.makeText(AliquotMenuActivity.this, "ERROR: Invalid Aliquot XML file.", Toast.LENGTH_LONG).show();

                    }

                } else
                    // Tells user to select a file for viewing
                    Toast.makeText(AliquotMenuActivity.this, "Please select an aliquot file to view.", Toast.LENGTH_LONG).show(); // lets user know table is opening

            }
        });

        igsnText = (EditText) findViewById(R.id.aliquotIGSNText);
        // Checks to see if user profile information has been authenticated for private file access
        // Sets appropriate hint based on if credentials are stored or not
        retrieveCredentials();
        if (!getGeochronUsername().contentEquals("None")&& !getGeochronPassword().contentEquals("None")) {
            igsnText.setHint("Profile information stored. Private files enabled!");
        } else {
            igsnText.setHint("No profile information stored. Private files disabled.");
        }

        igsnDownloadButton = (Button) findViewById(R.id.aliquotIGSNSubmitButton);
        igsnDownloadButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // Hides SoftKeyboard When Download Button is Pressed
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(igsnText.getWindowToken(),0);

                // Checks internet connection before downloading files
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mobileWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                if (mobileWifi.isConnected()) {
                    if (igsnText.getText().length() != 0) {
                        downloadAliquot();
                        igsnText.setText("");
                    }
                } else {//Handles lack of wifi connection
                    new AlertDialog.Builder(AliquotMenuActivity.this).setMessage("You are not connected to WiFi, mobile data rates may apply. " +
                            "Do you wish to continue?")
                            // if user selects yes, continue with validation
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    downloadAliquot();
                                    igsnText.setText("");
                                }
                            })
                                    // if user selects no, just go back
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .show();
                }
            }

        });

    }

    /**
     * Downloads an aliquot based on the text entered into the aliquot field
     */
    public void downloadAliquot() {
        Toast.makeText(AliquotMenuActivity.this, "Downloading Aliquot...", Toast.LENGTH_SHORT).show(); // Reports that aliquot is being downloaded

        // Captures igsn from user input
        String aliquotIGSN = igsnText.getText().toString().toUpperCase().trim();

        // creates URLFileReader class to download the file
        URLFileReader downloader = new URLFileReader(AliquotMenuActivity.this, "AliquotMenu",
                makeURI(BASE_ALIQUOT_URI, aliquotIGSN), "igsn");

        downloader.startFileDownload(); // begins actual download

        // Note: Setting above is useful for download-then-open functionality
    }


    /**
     * Stores Current Aliquot
     */
    protected void saveCurrentAliquot() {
        SharedPreferences settings = getSharedPreferences(PREF_ALIQUOT, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("Current Aliquot", getIntent().getStringExtra("AliquotXMLFileName")); // gets chosen file from file browser and stores
        editor.apply(); // Committing changes
    }


    @Override
    // Gets the filename that the FilePicker returns and puts it into this Intent's Extra
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                if (data.hasExtra("AliquotXMLFileName")) {
                    String result = data.getStringExtra("AliquotXMLFileName");  // Specified file name from file browser
                    getIntent().putExtra("AliquotXMLFileName", result);     // put the DATA into THIS INTENT
                    String[] selectedAliquotFilePath = result.split("/"); // Splits selected file name into relevant parts
                    String fileName = selectedAliquotFilePath[selectedAliquotFilePath.length - 1]; // Creates displayable file name from split file path
                    aliquotSelectedFileText.setText(fileName); // Sets file name for displaying on aliquot file select line
                }
            }
        }
    }

    /**
     * Checks an XML file at the specified file path to see if it is a Aliquot file
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

                    // returns true if the first node is Aliquot
                    result = doc.getDocumentElement().getNodeName().equals("Aliquot");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // returns false if there was a different error
        return result;
    }

    /**
     * Retrieves the currently stored username and password
     * If none present, returns None
     */
    private void retrieveCredentials() {
        SharedPreferences settings = getSharedPreferences(USER_PREFS, 0);
        setGeochronUsername(settings.getString("Geochron Username", "None"));
        setGeochronPassword(settings.getString("Geochron Password", "None"));
    }

    /**
     * Creates URL from the constant GeoChron URL and IGSN
     */
    public static String makeURI(String baseURL, String IGSN) {
        String URI = baseURL + IGSN;
        if (!getGeochronUsername().contentEquals("None")&& !getGeochronPassword().contentEquals("None")) {
            // Create unique URL and password if credentials stored
            URI += "&username="+getGeochronUsername()+"&password="+getGeochronPassword();
        }
        return URI;
    }

    public static String getGeochronUsername() {
        return geochronUsername;
    }

    public void setGeochronUsername(String geochronUser) {
        geochronUsername = geochronUser;
    }

    public static String getGeochronPassword() {
        return geochronPassword;
    }

    public void setGeochronPassword(String geochronPass) {
        geochronPassword = geochronPass;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
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
                        "Aliquot_Directory");
                startActivity(openAliquotFiles);
                return true;
            case R.id.viewReportSettingsMenu: // Takes user to report settings menu
                Intent openReportSettingsFiles = new Intent(
                        "android.intent.action.FILEPICKER");
                openReportSettingsFiles.putExtra("Default_Directory",
                        "Report_Settings_Directory");
                startActivity(openReportSettingsFiles);
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

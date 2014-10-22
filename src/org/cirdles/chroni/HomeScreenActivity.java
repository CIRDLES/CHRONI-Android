package org.cirdles.chroni;

import java.io.File;
import java.io.FileNotFoundException;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.Menu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class HomeScreenActivity extends Activity  {

    // Maintains whether app is initalizing for the first time or not
    private static final String PREF_FIRST_LAUNCH = "First Launch";
    private static final String PREF_REPORT_SETTINGS = "Current Report Settings";     // Path of the current report settungs file

    // Version number
    private TextView versionNumber;

    private CHRONIDatabaseHelper trialDatabaseHelper; // Database

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_Holo);
        setContentView(R.layout.home_screen);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        //Places background image on layout due to theme overriding
        RelativeLayout layout =(RelativeLayout)findViewById(R.id.homeScreenBackground);
        layout.setBackground(getResources().getDrawable(R.drawable.background));

        // Checks to see if this is launch
        // checkCredentials();

        try {
            // Puts the versioning information on the App
            Context context = this;
            int versionCode = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionCode;
            String versionName = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionName;

            versionNumber = (TextView) findViewById(R.id.versionNumber);
            versionNumber.setText("Version " + versionCode + "." + versionName);
            versionNumber.setTextColor(getResources().getColor(
                    R.color.button_blue));

            // Puts demo items in the history database if first launch
//            trialDatabaseHelper = new CHRONIDatabaseHelper(this);
//            if (isInitialLaunch()) {
//                trialDatabaseHelper.createEntry("01/11/1111", "Demo Aliquot");
//            }

            // Creates the necessary application directories
            createDirectories();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Thread timer = new Thread() {
            public void run() {
                try {
                    sleep(3500); // The home screen is shown for two seconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    // Tests to see whether the user has completed a profile
                    // yet.
                    // If not, proceeds to the User Profile login screen, else
                    // moves to Main Menu.
                    // SharedPreferences appSettings =
                    // getSharedPreferences(USER_REGISTERED, 0);
                    // if(!appSettings.getBoolean("User Registered", false)){
                    // Intent openLoginScreen = new
                    // Intent("android.intent.action.LOGINSCREEN");
                    // startActivity(openLoginScreen);
                    // }else{
                    Intent openMainMenu = new Intent(
                            "android.intent.action.MAINMENU");
                    openMainMenu.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(openMainMenu);
                    // }
                }
            }
        };
        timer.start();
    }

    /*
     * Creates the necessary application directories: CIRDLES, Aliquot and
     * Report Settings folders
     */
    protected void createDirectories() throws FileNotFoundException {
        boolean defaultReportSettingsPresent = false; // detemines whether the report settings is present or not

        // Establishes the CHRONI folders
        File chroniDirectory = getDir("CHRONI", Context.MODE_PRIVATE); //Creating an internal directory for CHRONI files
        File aliquotDirectory = new File(chroniDirectory, "Aliquot");
        File reportSettingsDirectory = new File(chroniDirectory, "Report Settings");
        File defaultReportSettingsDirectory = new File(reportSettingsDirectory, "Default Report Settings");

        // Creates the directories if they are not there
        if (!chroniDirectory.exists()) {
            chroniDirectory.mkdirs();
        }
        if (!aliquotDirectory.exists()) {
            aliquotDirectory.mkdirs();
        }
        if (!reportSettingsDirectory.exists()) {
            reportSettingsDirectory.mkdirs();
        }

        // Checks internet connection before downloading files
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        // Checks to see if the default report settings is present
        File[] files = reportSettingsDirectory.listFiles(); // Lists files in CHRONI directory
        for (File f : files) {
            if (f.getName().contentEquals("Default Report Settings.xml")) {
                defaultReportSettingsPresent = true;
            }
        }
        if (!defaultReportSettingsPresent) {

            if (mWifi.isConnected()) {
                // Downloads the default report setting file if absent
                URLFileReader downloader = new URLFileReader(
                        HomeScreenActivity.this,
                        "HomeScreen",
                        "http://cirdles.org/sites/default/files/Downloads/CIRDLESDefaultReportSettings.xml",
                        "url");
                saveInitialLaunch();
                saveCurrentReportSettings();
            }
            // Notes that files have been downloaded and application has been
            // properly initialized
         else {
            Toast.makeText(HomeScreenActivity.this, "Please connect to your local wifi network to download your Default Report Settings file.", Toast.LENGTH_LONG).show();
        }
        }
    }

    /*
     * Storing in Shared Preferences
     */
    protected void saveInitialLaunch() {
        SharedPreferences settings = getSharedPreferences(PREF_FIRST_LAUNCH, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("Initial Launch", false);
        editor.commit(); // Commiting changes
    }

    /*
    * Stores Current Report Settings
    */
    protected void saveCurrentReportSettings() {
        // Establishes the CHRONI folders
        File chroniDirectory = getDir("CHRONI", Context.MODE_PRIVATE); //Creating an internal directory for CHRONI files
        File reportSettingsDirectory = new File(chroniDirectory, "Report Settings");
        File defaultReportSettingsDirectory = new File(reportSettingsDirectory, "Default Report Settings");

        SharedPreferences settings = getSharedPreferences(PREF_REPORT_SETTINGS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("Current Report Settings", defaultReportSettingsDirectory.getPath()); // gets chosen file from file browser and stores
        editor.commit(); // Commiting changes
    }

    /*
     * Checking Shared Preferences if the user had pressed the remember me
     * button last time he logged in
     */
    private boolean isInitialLaunch() {
        SharedPreferences settings = getSharedPreferences(PREF_FIRST_LAUNCH, 0);
        return settings.getBoolean("Initial Launch", true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
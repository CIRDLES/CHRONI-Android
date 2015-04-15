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
import android.os.Environment;
import android.view.Menu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

/*
Defines the home screen activity of the application
 */
public class HomeScreenActivity extends Activity  {

    // Maintains whether app is initializing for the first time or not
    private static final String PREF_FIRST_LAUNCH = "First Launch";
    private static final String PREF_REPORT_SETTINGS = "Current Report Settings";     // Path of the current report settings file
    private static final String PREF_ALIQUOT = "Current Aliquot";// Path of the current aliquot file

    private TextView versionNumber; // version number
    private CHRONIDatabaseHelper trialDatabaseHelper; // Database

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // sets up layout screen
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        //Places background image on layout  due to theme overriding
        RelativeLayout layout =(RelativeLayout)findViewById(R.id.homeScreenBackground);

        try {
            // Puts the versioning information on the layout screen
            // TODO make this a method accessible by the entire app so that every screen can have the version info
            Context context = this;
            int versionCode = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionCode;
            String versionName = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionName;


            versionNumber = (TextView) findViewById(R.id.versionNumber);
            versionNumber.setText("Version " + versionCode + "." + versionName);
            versionNumber.setTextColor(getResources().getColor(R.color.button_blue));

            // Puts demo items in the history database if first launch
//            trialDatabaseHelper = new CHRONIDatabaseHelper(this);
//            if (isInitialLaunch()) {
//                trialDatabaseHelper.createEntry("01/11/1111", "Demo Aliquot");
//            }

            // Creates the necessary CHRONI directories
            createDirectories();


        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Waits 3.5 seconds before moving to the main menu
        Thread timer = new Thread() {
            public void run() {
                try {
                    sleep(3500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    Intent openMainMenu = new Intent(
                            "android.intent.action.MAINMENU");
                    openMainMenu.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(openMainMenu);
                }
            }
        };
        timer.start();
    }

    /*
 * Creates the necessary application directories: CIRDLES, Aliquot and Report Settings folders
 */
    protected void createDirectories() throws FileNotFoundException {
        // Establishes the CIRDLES directories
        File chroniDirectory = new File(Environment.getExternalStorageDirectory()+ "/CHRONI/");
        File aliquotDirectory = new File(Environment.getExternalStorageDirectory()+ "/CHRONI/Aliquot");
        File reportSettingsDirectory = new File(Environment.getExternalStorageDirectory()+ "/CHRONI/Report Settings");

        // Gives default report settings a path
        File defaultReportSettingsDirectory = new File(reportSettingsDirectory, "Default Report Settings");
        File defaultReportSettings2Directory = new File(reportSettingsDirectory, "Default Report Settings 2");

        // Gives default aliquot a path
        File defaultAliquotDirectory = new File(reportSettingsDirectory, "Default Aliquot");

        boolean defaultReportSettingsPresent = false; // determines whether the default report settings is present or not
        boolean defaultReportSettings2Present = false; // determines whether the default report settings 2 is present or not
        boolean defaultAliquotPresent = false; // determines whether the aliquot is present or not

        //Creates the directories if they are not there
            chroniDirectory.mkdirs();
            aliquotDirectory.mkdirs();
            reportSettingsDirectory.mkdirs();

        // Checks internet connection before downloading files
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobileWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        // Checks to see if the default report settings is present
        File[] files = reportSettingsDirectory.listFiles(); // Lists files in CHRONI directory
        for (File f : files) {
            if (f.getName().contentEquals("Default Report Settings.xml")) {
                defaultReportSettingsPresent = true;
            }if (f.getName().contentEquals("Default Report Settings 2.xml")) {
                defaultReportSettings2Present = true;
            }if(f.getName().contentEquals("Default Aliquot.xml")){
                defaultAliquotPresent = true;
            }
        }

        if (mobileWifi.isConnected()) {
            // Downloads default report settings 1 if not present
            if (!defaultReportSettingsPresent) {
                // Downloads the default report setting file if absent
                URLFileReader downloader = new URLFileReader(
                        HomeScreenActivity.this,
                        "HomeScreen",
                        "http://cirdles.org/sites/default/files/Downloads/CIRDLESDefaultReportSettings.xml",
                        "url");
                saveInitialLaunch();
                saveCurrentReportSettings();         // Notes that files have been downloaded and application has been properly initialized
            }

            if (!defaultReportSettings2Present) {
                // Downloads the default report setting file if absent
                URLFileReader downloader2 = new URLFileReader(
                        HomeScreenActivity.this,
                        "HomeScreen",
                        "http://cirdles.org/sites/default/files/Downloads/Default%20Report%20Settings%202.xml",
                        "url");
                saveInitialLaunch();
                saveCurrentReportSettings();         // Notes that files have been downloaded and application has been properly initialized
            }

//            if (!defaultAliquotPresent) {
//                // Downloads the default report setting file if absent
//                URLFileReader downloader3 = new URLFileReader(
//                        HomeScreenActivity.this,
//                        "HomeScreen",
//                        "http://cirdles.org/sites/default/files/Downloads/TempAliquot.xml",
//                        "url");
//                saveInitialLaunch();
//                saveCurrentAliquot();         // Notes that files have been downloaded and application has been properly initialized
//            }

        }else {
            Toast.makeText(HomeScreenActivity.this, "Please connect to your local wifi network to download your Default Report Settings files.", Toast.LENGTH_LONG).show();
        }

    }


    /*
     * Creates the necessary application directories: CIRDLES, Aliquot and
     * Report Settings folders
     */
    protected void oldCreateDirectories() throws FileNotFoundException {
        boolean defaultReportSettingsPresent = false; // determines whether the report settings is present or not
        boolean defaultReportSettings2Present = false; // determines whether the report settings is present or not

        // Establishes the CHRONI folders
        File chroniDirectory = getDir("CHRONI", Context.MODE_PRIVATE); //Creating an internal directory for CHRONI files
        File aliquotDirectory = new File(chroniDirectory, "Aliquot");
        File reportSettingsDirectory = new File(chroniDirectory, "Report Settings");
        File defaultReportSettingsDirectory = new File(reportSettingsDirectory, "Default Report Settings");
        File defaultReportSettings2Directory = new File(reportSettingsDirectory, "Default Report Settings 2");

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
        NetworkInfo mobileWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        // Checks to see if the default report settings is present
        File[] files = reportSettingsDirectory.listFiles(); // Lists files in CHRONI directory
        for (File f : files) {
            if (f.getName().contentEquals("Default Report Settings.xml")) {
                defaultReportSettingsPresent = true;
            }if (f.getName().contentEquals("Default Report Settings 2.xml")) {
                defaultReportSettings2Present = true;
            }
        }

            if (mobileWifi.isConnected()) {
                // Downloads default report settings 1 if not present
                if (!defaultReportSettingsPresent) {
                    // Downloads the default report setting file if absent
                    URLFileReader downloader = new URLFileReader(
                            HomeScreenActivity.this,
                            "HomeScreen",
                            "http://cirdles.org/sites/default/files/Downloads/CIRDLESDefaultReportSettings.xml",
                            "url");
                    saveInitialLaunch();
                    saveCurrentReportSettings();         // Notes that files have been downloaded and application has been properly initialized
                }

                if (!defaultReportSettings2Present) {
                    // Downloads the default report setting file if absent
                    URLFileReader downloader2 = new URLFileReader(
                            HomeScreenActivity.this,
                            "HomeScreen",
                            "http://cirdles.org/sites/default/files/Downloads/Default%20Report%20Settings%202.xml",
                            "url");
                    saveInitialLaunch();
                    saveCurrentReportSettings();         // Notes that files have been downloaded and application has been properly initialized
                }

            }else {
                Toast.makeText(HomeScreenActivity.this, "Please connect to your local wifi network to download your Default Report Settings files.", Toast.LENGTH_LONG).show();
            }

    }

    /*
* Stores Current Aliquot
*/
    protected void saveCurrentAliquot() {
        SharedPreferences settings = getSharedPreferences(PREF_ALIQUOT, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("Current Aliquot", getIntent().getStringExtra("AliquotXMLFileName")); // gets chosen file from file browser and stores
        editor.commit(); // Commiting changes
    }

    /*
     * Stores initial launch in Shared Preferences
     */
    protected void saveInitialLaunch() {
        SharedPreferences settings = getSharedPreferences(PREF_FIRST_LAUNCH, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("Initial Launch", false);
        editor.commit(); // Commiting changes
    }

    /*
    * Stores Current Report Settings
    * TODO clean up this process
    */
    protected void saveCurrentReportSettings() {
        // Establishes the CHRONI folders
        File chroniDirectory = getDir("CHRONI", Context.MODE_PRIVATE); //Creating an internal directory for CHRONI files
        File reportSettingsDirectory = new File(chroniDirectory, "Report Settings");
        File defaultReportSettingsDirectory = new File(reportSettingsDirectory, "Default Report Settings");

        SharedPreferences settings = getSharedPreferences(PREF_REPORT_SETTINGS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("Current Report Settings", defaultReportSettingsDirectory.getPath()); // makes the Default Report Settings the current report settings
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
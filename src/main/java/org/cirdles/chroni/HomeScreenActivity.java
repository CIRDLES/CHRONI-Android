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

/**
 * Defines the home screen activity of the application
 */
public class HomeScreenActivity extends Activity  {

    // Maintains whether app is initializing for the first time or not
    private static final String PREF_FIRST_LAUNCH = "First Launch";
    private static final String PREF_REPORT_SETTINGS = "Current Report Settings";     // Path of the current report settings file
    private static final String PREF_ALIQUOT = "Current Aliquot"; // Path of the current aliquot file

    private boolean defaultReportSettingsPresent = true;
    private boolean defaultReportSettings2Present = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // sets up layout screen
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_Holo);

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


            TextView versionNumber = (TextView) findViewById(R.id.versionNumber);
            versionNumber.setText("Version " + versionCode + "." + versionName);
            versionNumber.setTextColor(getResources().getColor(R.color.button_blue));

            // Creates the necessary CHRONI directories
            createDirectories();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        // Waits 3.5 seconds before moving to the main menu
        Thread timer = new Thread() {
            public void run() {
                try {
                    sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    Intent openMainMenu = new Intent(
                            "android.intent.action.MAINMENU");
                    openMainMenu.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    openMainMenu.putExtra("hasDefault1", defaultReportSettingsPresent);
                    openMainMenu.putExtra("hasDefault2", defaultReportSettings2Present);
                    startActivity(openMainMenu);
                }
            }
        };
        timer.start();
    }

    /**
     * Creates the necessary application directories: CIRDLES, Aliquot and Report Settings folders
     */
    protected void createDirectories() throws FileNotFoundException {
        // Establishes the CIRDLES directories
        File chroniDirectory = new File(Environment.getExternalStorageDirectory()+ "/CHRONI/");
        File aliquotDirectory = new File(Environment.getExternalStorageDirectory()+ "/CHRONI/Aliquot");
        File reportSettingsDirectory = new File(Environment.getExternalStorageDirectory()+ "/CHRONI/Report Settings");

        // Gives default aliquot a path
        File defaultAliquotDirectory = new File(reportSettingsDirectory, "Default Aliquot");

        defaultReportSettingsPresent = false; // determines whether the default report settings is present or not
        defaultReportSettings2Present = false; // determines whether the default report settings 2 is present or not
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
            }if(f.getName().contentEquals("Default Aliquot.xml")) {
                defaultAliquotPresent = true;
            }
        }

        if (mobileWifi.isConnected()) {
            // Downloads default report settings 1 if not present
            if (!defaultReportSettingsPresent) {
                // Downloads the default report settings file if absent
                URLFileReader downloader = new URLFileReader(
                        HomeScreenActivity.this,
                        "HomeScreen",
                        "https://raw.githubusercontent.com/CIRDLES/cirdles.github.com/master/assets/Default%20Report%20Settings%20XML/Default%20Report%20Settings.xml",
                        "url");
                downloader.startFileDownload();     // begins download
                saveInitialLaunch();
                saveCurrentReportSettings();    // Notes that files have been downloaded and application has been properly initialized
            }

            if (!defaultReportSettings2Present) {
                // Downloads the second default report settings file if absent
                URLFileReader downloader2 = new URLFileReader(
                        HomeScreenActivity.this,
                        "HomeScreen",
                        "https://raw.githubusercontent.com/CIRDLES/cirdles.github.com/master/assets/Default%20Report%20Settings%20XML/Default%20Report%20Settings%202.xml",
                        "url");
                downloader2.startFileDownload();    // begins download
                saveInitialLaunch();
                saveCurrentReportSettings();    // Notes that files have been downloaded and application has been properly initialized
            }

        }

    }

    //Stores initial launch in Shared Preferences
    protected void saveInitialLaunch() {
        SharedPreferences settings = getSharedPreferences(PREF_FIRST_LAUNCH, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("Initial Launch", false);
        editor.apply(); // Committing changes
    }

    /**
     * Stores Current Report Settings
     * TODO clean up this process
     */
    protected void saveCurrentReportSettings() {
        // Establishes the CHRONI folders
        File reportSettingsDirectory = new File(Environment.getExternalStorageDirectory() + "/CHRONI/Report Settings"); //Creating an internal directory for CHRONI files

        SharedPreferences settings = getSharedPreferences(PREF_REPORT_SETTINGS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("Current Report Settings", reportSettingsDirectory.getPath() + "/Default Report Settings.xml"); // makes the Default Report Settings the current report settings
        editor.apply(); // Committing changes
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
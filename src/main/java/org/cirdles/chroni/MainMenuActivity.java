/*
 * Copyright 2016 CIRDLES.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cirdles.chroni;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;

/**
 * This activity is used for structuring the main menu layout of the application.
 */
public class MainMenuActivity extends Activity {

    // path of the current Report Settings and Aliquot files
    private static final String PREF_ALIQUOT = "Current Aliquot"; // Path of the current aliquot file
    private static final String PREF_REPORT_SETTINGS = "Current Report Settings";

    private boolean hasDefaultReportSettings1 = true;
    private boolean hasDefaultReportSettings2 = true;
    private boolean hasDefaultAliquot = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Sets up layout
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_Holo);
        setContentView(R.layout.main_menu);

         // Places background image on layout due to theme overriding
         RelativeLayout layout =(RelativeLayout)findViewById(R.id.mainMenuBackground);
         layout.setBackground(getResources().getDrawable(R.drawable.background));

        try {
            // Puts the versioning information on the App
            Context context = this;
            int versionCode = context.getPackageManager().getPackageInfo(
                context.getPackageName(), 0).versionCode;
            String versionName = context.getPackageManager().getPackageInfo(
                context.getPackageName(), 0).versionName;

            TextView versionNumber = (TextView) findViewById(R.id.versionNumberMainMenu);
            versionNumber.setText("Version " + versionCode + "." + versionName);
            versionNumber.setTextColor(getResources().getColor(
                R.color.button_blue));

        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        // Allows user to select an aliquot file for viewing in the display table
        Button openButton = (Button) findViewById(R.id.menuOpenButton);
        openButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent openDisplay = new Intent(
                        "android.intent.action.ALIQUOTMENU");
                startActivity(openDisplay);
            }
        });

        // Allows user to open the history activity
        Button historyButton = (Button) findViewById(R.id.menuHistoryButton);
        historyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            Intent openHistoryTable = new Intent(
                "android.intent.action.HISTORY");
            startActivity(openHistoryTable);
            }
        });

        // Allows user to open the report settings activity
        Button reportSettingsButton = (Button) findViewById(R.id.reportSettingsButton);
        reportSettingsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // TODO Change this
                Intent openReportSettingsMenu = new Intent("android.intent.action.REPORTSETTINGSMENU");
                startActivity(openReportSettingsMenu);
            }
        });

        // Allows user to access the profile management feature
        Button credentialsButton = (Button) findViewById(R.id.menuProfileButton);
        credentialsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent openUserProfile = new Intent(
                        "android.intent.action.USERPROFILE");
                startActivity(openUserProfile);
            }
        });

        hasDefaultReportSettings1 = getIntent().getBooleanExtra("hasDefault1", true);
        hasDefaultReportSettings2 = getIntent().getBooleanExtra("hasDefault2", true);
        hasDefaultAliquot = getIntent().getBooleanExtra("hasDefaultAliquot", true);

        // alerts user/downloads default files if ANY are missing
        if (!(hasDefaultReportSettings1 && hasDefaultReportSettings2 && hasDefaultAliquot)) {
            new AlertDialog.Builder(this).setMessage("You do not have all of the default files and are not connected to WiFi, mobile data rates may apply. " +
                    "Do you wish to download the default files?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            downloadDefaultReportSettings();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).show();
        }

    }

    public void downloadDefaultReportSettings() {

        if (!hasDefaultReportSettings1) {
            // Downloads the default report settings file if absent
            URLFileReader downloader = new URLFileReader(
                    MainMenuActivity.this,
                    "HomeScreen",
                    "https://raw.githubusercontent.com/CIRDLES/cirdles.github.com/master/assets/Default%20Report%20Settings%20XML/Default%20Report%20Settings.xml",
                    "url");
            downloader.startFileDownload();     // begins download
            saveCurrentReportSettings();    // Notes that files have been downloaded and application has been properly initialized
            hasDefaultReportSettings1 = true;
            getIntent().putExtra("hasDefault1", true);
        }

        if (!hasDefaultReportSettings2) {
            // Downloads the second default report settings file if absent
            URLFileReader downloader2 = new URLFileReader(
                    MainMenuActivity.this,
                    "HomeScreen",
                    "https://raw.githubusercontent.com/CIRDLES/cirdles.github.com/master/assets/Default%20Report%20Settings%20XML/Default%20Report%20Settings%202.xml",
                    "url");
            downloader2.startFileDownload();    // begins download
            saveCurrentReportSettings();    // Notes that files have been downloaded and application has been properly initialized
            hasDefaultReportSettings2 = true;
            getIntent().putExtra("hasDefault2", true);
        }

        if (!hasDefaultAliquot) {
            URLFileReader downloader3 = new URLFileReader(
                    MainMenuActivity.this,
                    "HomeScreenAliquot",
                    "https://raw.githubusercontent.com/CIRDLES/cirdles.github.com/master/assets/Default-Aliquot-XML/Default%20Aliquot.xml",
                    "url");
            downloader3.startFileDownload();
            saveCurrentAliquot();
            hasDefaultAliquot = true;
            getIntent().putExtra("hasDefaultAliquot", true);

        }
    }


    /**
     * Stores the current Report Settings as default in the Shared Preferences.
     */
    protected void saveCurrentReportSettings() {
        // Establishes the CHRONI folders
        File reportSettingsDirectory = new File(Environment.getExternalStorageDirectory() + "/CHRONI/Report Settings"); //Creating an internal directory for CHRONI files

        SharedPreferences settings = getSharedPreferences(PREF_REPORT_SETTINGS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("Current Report Settings", reportSettingsDirectory.getPath() + "/Default Report Settings.xml"); // makes the Default Report Settings the current report settings
        editor.apply(); // Committing changes
    }

    /**
     * Stores the current Aliquot as default in the Shared Preferences.
     */
    protected  void saveCurrentAliquot() {
        // Establishes the CHRONI folders
        File reportSettingsDirectory = new File(Environment.getExternalStorageDirectory() + "/CHRONI/Aliquot");
        SharedPreferences settings = getSharedPreferences(PREF_ALIQUOT, 0);
        SharedPreferences.Editor editor = settings.edit();

        // makes the Default Report Settings the current report settings
        editor.putString("Current Aliquot", reportSettingsDirectory.getPath() + "/Default Aliquot.xml");
        editor.apply(); // Committing changes
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
            case R.id.returnToMenu: // Already on the main menu, so just return true
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
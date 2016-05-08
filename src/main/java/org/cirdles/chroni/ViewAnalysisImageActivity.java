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

/**
 * Able to get ths code from http://www.learn2crack.com/2014/06/android-load-image-from-internet.html
 */

package org.cirdles.chroni;

import java.io.InputStream;
import java.net.URL;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class ViewAnalysisImageActivity extends Activity {
    Button saveAnalysisImage;
    ImageView viewAnalysisImage;
    TextView analysisImageText;
    Bitmap bitmap;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_analysis_image);
        setTheme(android.R.style.Theme_Holo);

        viewAnalysisImage = (ImageView)findViewById(R.id.viewAnalysisImage);
        analysisImageText = (TextView) findViewById(R.id.analysisImageText);
        new LoadImage().execute("http://www.geochronportal.org/uploadimages/16747-concordia-tempConcordia.svg");


        if(getIntent().getStringExtra("ProbabilityDensityImage") != null)
            analysisImageText.setText("Probability Density");

        if(getIntent().getStringExtra("ConcordiaImage") != null)
            analysisImageText.setText("Concordia");

        saveAnalysisImage = (Button)findViewById(R.id.saveAnalysisImageButton);
        saveAnalysisImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
            }
        });
    }

    private class LoadImage extends AsyncTask<String, String, Bitmap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(ViewAnalysisImageActivity.this);
            progressDialog.setMessage("Loading Image ....");
            progressDialog.show();
        }
        protected Bitmap doInBackground(String... args) {
            try {
                bitmap = BitmapFactory.decodeStream((InputStream)new URL(args[0]).getContent());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }
        protected void onPostExecute(Bitmap image) {
            if(image != null){
                viewAnalysisImage.setImageBitmap(image);
                progressDialog.dismiss();
            }else{
                progressDialog.dismiss();
                Toast.makeText(ViewAnalysisImageActivity.this, "Image Does Not exist or Network Error",
                        Toast.LENGTH_SHORT).show();
            }
        }
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
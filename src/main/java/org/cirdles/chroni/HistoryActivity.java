package org.cirdles.chroni;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Sets up the History database table.
 */
public class HistoryActivity extends Activity {

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // sets up the layout
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_Holo);
        setContentView(R.layout.history);

        //Places background image on layout due to theme overriding
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.historyBackground);
        layout.setBackground(getResources().getDrawable(R.drawable.background));

        // Sets up the home button
        Button homeButton = (Button) findViewById(R.id.historyFinishButton);
        homeButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                finish();   // Exits out of History
            }
        });

        final CHRONIDatabaseHelper myAliquots = new CHRONIDatabaseHelper(this); // used to access the stored CHRONI database

        if (!myAliquots.isEmpty()) {
            // Collects information from the database if it isn't empty
            final String[][] database = myAliquots.fillTableData(); // completes 2D array of aliquot table for history
            int ROWS = 11; // rows for last five MRV entries plus an extra row reserved for header
            if (myAliquots.getTotalEntryCount() < 10){
                ROWS = (int) myAliquots.getTotalEntryCount() + 1; // Sizes the array based on how many entries are in database
            }
            final long COLUMNS = 4;

            // sets up the table to display the database
            TableLayout table = (TableLayout) findViewById(R.id.historyDatabaseTable);

            // stores the header TextViews for later
            TextView[] headerCells = new TextView[4];
            headerCells[0] = (TextView) findViewById(R.id.historyHeaderOne);
            headerCells[1] = (TextView) findViewById(R.id.historyHeaderTwo);
            headerCells[2] = (TextView) findViewById(R.id.historyHeaderThree);
            headerCells[3] = (TextView) findViewById(R.id.historyHeaderFour);

            // obtains the desired height and width to be used for each View
            TextView tempText = headerCells[0];
            tempText.measure(0, 0);
            int height = tempText.getMaxHeight();
            int width = tempText.getMeasuredWidth();
            float textSize = this.getResources().getDisplayMetrics().scaledDensity;
            textSize = tempText.getTextSize() / textSize;   // converts textSize from px to sp

            // Table Layout Printing
            for (int currentRow = 0; currentRow < ROWS; currentRow++) {
                // Creates current row to the table
                TableRow row = new TableRow(this);
                row.setGravity(Gravity.CENTER);
                table.addView(row);

                for (int currentColumn = 0; currentColumn < COLUMNS; currentColumn++) {

                    // Adds text to the history cells if not a button column or is header row
                    if (currentColumn != 3 || currentRow == 0) {
                        // if it is a header view, simply make it visible
                        if (currentRow == 0) {
                            TextView cell = headerCells[currentColumn];
                            cell.setVisibility(View.VISIBLE);

                        } else {
                            TextView textCell = new TextView(this);

                            // Formats the file names correctly for history table
                            if (database[currentRow][currentColumn].contains("/")) {
                                String[] fileNameText = database[currentRow][currentColumn].split("/");
                                String fileName = fileNameText[fileNameText.length - 1];
                                if (fileName.contains(".xml")) { // Removes the extension from the Aliquot name
                                    String fileNameWithExtension[] = fileName.split(".xml");
                                    fileName = fileNameWithExtension[fileNameWithExtension.length - 1];
                                }
                                textCell.setText(fileName); // Sets correctly formatted file name to middle column
                            } else {
                                textCell.setText(database[currentRow][currentColumn]); // Sets date in first column
                            }

                            // Properly formats all text cells
                            textCell.setPadding(4, 4, 4, 4);
                            textCell.setTextSize(textSize - 2);
                            textCell.setGravity(Gravity.CENTER);
                            textCell.setWidth(width);
                            textCell.setHeight(height);
                            textCell.setTypeface(Typeface.DEFAULT_BOLD);

                            if (currentRow % 2 == 1) {
                                // Colors table's odd rows
                                textCell.setTextColor(Color.WHITE);
                                textCell.setBackgroundResource(R.drawable.dark_blue_background);
                            } else {
                                // Colors even rows
                                textCell.setTextColor(Color.BLACK);
                                textCell.setBackgroundResource(R.drawable.white_background);
                            }

                            row.addView(textCell); // adds text cell to the row
                        }

                    } // ends the formatting of the text cells

                    // adds the open button to the last column
                    else {
                        final Button openButton = new Button(this);
                        openButton.setText("OPEN");
                        openButton.setTextSize(textSize - 2);
                        openButton.setPadding(5, 5, 5, 5);
                        openButton.setTypeface(Typeface.DEFAULT_BOLD);
                        openButton.setGravity(Gravity.CENTER);
                        openButton.setLayoutParams(new TableRow.LayoutParams(width-15, height-15));
                        row.addView(openButton);

                        // Gets the current aliquot info for sending to the display table
                        final int currentAliquotRow = currentRow;
                        final int currentAliquotColumn = currentColumn - 2;

                        //Changes button color back to blue if it is not already
                        openButton.setBackgroundResource(R.drawable.light_gray_button);
                        openButton.setTextColor(Color.BLACK);

                        // adds open button functionality
                        openButton.setOnClickListener(new View.OnClickListener() {
                            // When view/edit is clicked, the review screen is opened
                            public void onClick(View v) {

                                Toast.makeText(HistoryActivity.this, "Opening table...", Toast.LENGTH_LONG).show();
                                Intent openTableScreen = new Intent("android.intent.action.DISPLAY");

                                // put the aliquot file path into the Table Intent
                                openTableScreen.putExtra("fromHistory", "true");
                                openTableScreen.putExtra("historyAliquot",
                                        database[currentAliquotRow][currentAliquotColumn]); // gives Aliquot file path
                                openTableScreen.putExtra("historyReportSettings",
                                        database[currentAliquotRow][currentAliquotColumn + 1]); // gives Report Settings file path

                                startActivity(openTableScreen);
                            }
                        });

                    }
                }
            }
        }
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
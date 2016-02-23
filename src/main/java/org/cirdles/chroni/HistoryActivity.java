package org.cirdles.chroni;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
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
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

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
                ROWS = (int)myAliquots.getTotalEntryCount() + 1; // Sizes the array based on how many entries are in database
            }
            final long COLUMNS = 3;

            // sets up the table to display the database
            TableLayout table = (TableLayout) findViewById(R.id.historyDatabaseTable);

            // Table Layout Printing
            for (int currentRow = 0; currentRow < ROWS; currentRow++) {
                // Adds current row to the table
                TableRow row = new TableRow(this);
                row.setGravity(Gravity.CENTER);
                table.addView(row);

                for (int currentColumn = 0; currentColumn < COLUMNS; currentColumn++) {

                    // Adds text to the history cells if not a button column or is header row
                    if (currentColumn != 2 || currentRow == 0) {
                        TextView textCell = new TextView(this);

                        // Formats the file names correctly for history table
                        if (database[currentRow][currentColumn].contains("/")) {
                            String[] fileNameText = database[currentRow][currentColumn].split("/");
                            String fileName = fileNameText[fileNameText.length - 1];
                            if (fileName.contains(".xml")){ // Removes the extension from the Aliquot name
                               String fileNameWithExtension[] = fileName.split(".xml");
                               fileName = fileNameWithExtension[fileNameWithExtension.length-1];
                            }
                            textCell.setText(fileName); // Sets correctly formatted file name to middle column
                        } else {
                            textCell.setText(database[currentRow][currentColumn]); // Sets date in first column
                        }

                        // Properly formats all text cells
                        textCell.setPadding(2,2,2,2);
                        textCell.setTextSize((float) 15);
                        textCell.setGravity(Gravity.CENTER);
                        textCell.setWidth(240);
                        textCell.setHeight(70);
                        textCell.setTypeface(Typeface.DEFAULT_BOLD);

                        if (currentRow == 0) {
                            // Colors header row
                            textCell.setBackgroundResource(R.drawable.dark_grey_background);
                            textCell.setTextColor(Color.BLACK);
                        } else if (currentRow % 2 == 1) {
                            // colors table's odd rows
                            textCell.setTextColor(Color.WHITE);
                            textCell.setBackgroundResource(R.drawable.dark_blue_background);
                        } else {
                            // Colors even rows
                            textCell.setTextColor(Color.BLACK);
                            textCell.setBackgroundResource(R.drawable.white_background);
                        }

                        row.addView(textCell); // adds text cell to the row

                    } // ends the formatting of the text cells

                    // adds the open button to the last column
                    else {
                        final Button openButton = new Button(this);
                        openButton.setText("OPEN");
                        openButton.setTextSize((float) 15);
                        openButton.setTypeface(Typeface.DEFAULT_BOLD);
                        openButton.setGravity(Gravity.CENTER);
                        row.addView(openButton);

                        // Gets the current aliquot info for sending to the display table
                        final int currentAliquotRow = currentRow;
                        final int currentAliquotColumn = currentColumn-1;

                        //Changes button color back to blue if it is not already
                        openButton.setBackgroundColor(getResources().getColor(R.color.button_blue));
                        openButton.setTextColor(Color.WHITE);

                        // adds open button functionality
                        openButton.setOnClickListener(new View.OnClickListener() {
                            // When view/edit is clicked, the review screen is opened
                            public void onClick(View v) {
                                // Changes button color to indicate it has been opened
                                openButton.setBackgroundColor(Color.LTGRAY);
                                openButton.setTextColor(Color.BLACK);

                                Toast.makeText(HistoryActivity.this, "Opening table...", Toast.LENGTH_LONG).show();
                                Intent openTableScreen = new Intent("android.intent.action.DISPLAY");
                                openTableScreen.putExtra("AliquotXML", database[currentAliquotRow][currentAliquotColumn]); // Opens table to specified aliquot file
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
            case R.id.historyMenu: // Already on the history menu, so just return true
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
                Intent openHelpBlog = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(getString(R.string.chroni_help_address)));
                startActivity(openHelpBlog);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
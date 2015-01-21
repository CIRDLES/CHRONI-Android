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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

/*
 * Sets up the History database table.
 */
public class HistoryActivity extends Activity {

    CHRONIDatabaseHelper preloadedAliquots; // Database helper

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // sets up the layout
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_Holo);
        setContentView(R.layout.history);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        //Places background image on layout due to theme overriding
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.historyBackground);
        layout.setBackground(getResources().getDrawable(R.drawable.background));

        // Sets up the finish button
        Button finishButton = (Button) findViewById(R.id.historyFinishButton);
        finishButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent openMainMenu = new Intent(
                        "android.intent.action.MAINMENU");
                startActivity(openMainMenu);
            }
        });

        final CHRONIDatabaseHelper myAliquots = new CHRONIDatabaseHelper(this);

        if (!myAliquots.isEmpty()) {
            // Collects information from the database if it isn't empty
            final String[][] database = myAliquots.fillTableData(); // completes 2D array of aliquot table
            final long ROWS = myAliquots.getEntryCount() - 1; // TODO why is this -1
            final long COLUMNS = 3;

            // sets up the table to display the database
            TableLayout table = (TableLayout) findViewById(R.id.historyDatabaseTable);
            table.setGravity(Gravity.CENTER);
            table.setPadding(35, 0, 35, 0);

            // Table Layout Printing
            for (int i = 0; i < ROWS; i++) {
                // adds each row to the table
                // i = row number (starting at 1)
                TableRow row = new TableRow(this);
                table.addView(row);
                final int rowNum = i;
                for (int j = 0; j < COLUMNS; j++) {
                    // adds columns to the table
                    // j = column number (starting at 1)
                    if (j != 2 || i == 0) {
                        TextView cell = new TextView(this);
                        // Formats the file names correctly for history table
                        if (database[i][j].contains("/data/")) {
                            String[] fileNameText = database[i][j].split("/");
                            String fileName = fileNameText[fileNameText.length - 1];
                            if (fileName.contains(".xml")){ // Removes the extension from the Aliquot name
                               String fileNameWithExtension[] = fileName.split(".xml");
                               fileName = fileNameWithExtension[fileNameWithExtension.length-1];
                            }
                            cell.setText(fileName);
                        } else {
                            cell.setText(database[i][j]);
                        }
                        cell.setPadding(4, 4, 4, 4);
                        cell.setTextSize((float) 12);
                        cell.setGravity(Gravity.CENTER);
                        cell.setWidth(150);
                        cell.setHeight(100);

                        if (i % 2 == 1) {
                            // colors table's odd rows
                            cell.setBackgroundColor(Color.parseColor("#107AB3"));
                            cell.setTextColor(Color.WHITE);

                        } else {
                            cell.setBackgroundColor(Color.WHITE);
                            cell.setTextColor(Color.BLACK);
                        }

                        cell.setTypeface(Typeface.DEFAULT_BOLD);

//                        if(j != 4){ //TODO: Come back and find a more elegant solution to handle adding multiple buttons
                        row.addView(cell);
//                        }
//                        else if(j==4 && i==0){
//                            row.addView(cell);
//                        }
                    } // ends the formatting of the text cells

                    // adds the open buttons to the last column
                    else if (j == 2 && i != 0) {
                        Button openButton = new Button(this);
                        openButton.setText("OPEN");
                        openButton.setTextColor(Color.WHITE);
                        openButton.setTextSize((float) 12);
                        openButton.setTypeface(Typeface.DEFAULT_BOLD);
                        openButton.setGravity(Gravity.CENTER);
                        openButton.setWidth(150);
                                                openButton.setHeight(100);
                        openButton.setBackgroundColor(Color.GRAY);
                        row.addView(openButton);

                        final int currentRow = i;
                        final int currentColumn = j;

                        // adds open button functionality
                        openButton.setOnClickListener(new View.OnClickListener() {
                            // When view/edit is clicked, the review screen is
                            // opened
                            public void onClick(View v) {
                                Toast.makeText(HistoryActivity.this, "Opening table...", Toast.LENGTH_LONG).show();
                                Intent openTableScreen = new Intent("android.intent.action.DISPLAY");
                                openTableScreen.putExtra("AliquotXML", database[currentRow][currentColumn - 2]);
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
            case R.id.viewAliquotsMenu: // Takes user to aliquot menu
                Intent openAliquotFiles = new Intent(
                        "android.intent.action.FILEPICKER");
                openAliquotFiles.putExtra("Default_Directory",
                        "Aliquot");
                startActivity(openAliquotFiles);
                return true;
            case R.id.viewReportSettingsMenu: // Takes user to report settings menu
                Intent openReportSettingsFiles = new Intent(
                        "android.intent.action.FILEPICKER");
                openReportSettingsFiles.putExtra("Default_Directory",
                        "Report Settings");
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
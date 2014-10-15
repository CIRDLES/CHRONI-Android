package org.cirdles.chroni;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
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

/*
 * Sets up the main Review database table.
 */
public class HistoryActivity extends Activity {

    RelativeLayout background;
    TableLayout table;
    Button finishButton;
    ImageView reviewSubtext;
    CHRONIDatabaseHelper preloadedAliquots; // Database

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

        // Sets up the finish button
        finishButton = (Button) findViewById(R.id.historyFinishButton);
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
            final String[][] database = myAliquots.fillTableData(); // completes
            // 2D array
            // of
            // aliquot
            // data
            final long ROWS = myAliquots.getEntryCount() - 1;
            final long COLUMNS = 5;

            // sets up the table to display the database
            table = (TableLayout) findViewById(R.id.historyDatabaseTable);
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
                    if (j != 3 || i == 0) {
                        TextView cell = new TextView(this);
                        // Formats the file names correctly for history table
                        if (database[i][j].contains("/data/")) {
                            String[] fileNameText = database[i][j].split("/");
                            String fileName = fileNameText[fileNameText.length - 1];
                            cell.setText(fileName);
                        } else {
                            cell.setText(database[i][j]);
                        }
                        cell.setPadding(4, 4, 4, 4);
                        cell.setTextSize((float) 12);
                        cell.setGravity(Gravity.CENTER);
                        cell.setWidth(175);
                        cell.setHeight(120);

                        if (i % 2 == 1) {
                            // colors table's odd rows
                            cell.setBackgroundColor(Color.parseColor("#107AB3"));
                            cell.setTextColor(Color.WHITE);

                        } else {
                            cell.setBackgroundColor(Color.WHITE);
                            cell.setTextColor(Color.BLACK);
                        }

                        cell.setTypeface(Typeface.DEFAULT_BOLD);

                        if(j != 4){ //TODO: Come back and find a more elegant solution to handle adding multiple buttons
                        row.addView(cell);}
                        else if(j==4 && i==0){
                            row.addView(cell);
                        }
                    } // ends the formatting of the text cells

                    // adds the open buttons to the last column
                    else if (j == 3 && i != 0) {
                        Button button = new Button(this);
                        button.setText("OPEN");
                        button.setTextColor(Color.WHITE);
                        button.setTextSize((float) 12);
                        button.setTypeface(Typeface.DEFAULT_BOLD);
                        button.setGravity(Gravity.CENTER);
                        button.setHeight(105);
                        button.setWidth(50);
                        button.setBackgroundColor(Color.GRAY);
                        row.addView(button);

                        final int currentRow = i;
                        final int currentColumn = j;

                        // adds button functionality
                        button.setOnClickListener(new View.OnClickListener() {
                            // When view/edit is clicked, the review screen is
                            // opened
                            public void onClick(View v) {

				Intent openTableScreen = new Intent("android.intent.action.DISPLAY");
                                openTableScreen.putExtra("AliquotXML", database[currentRow][currentColumn-2]);
				startActivity(openTableScreen);
                            }
                        });

                    }

                // adds the delete buttons to the last column
                if (j == 4 && i != 0) {
                    Button button = new Button(this);
                    button.setText("DELETE");
                    button.setTextColor(Color.WHITE);
                    button.setTextSize((float)11);
                    button.setTypeface(Typeface.DEFAULT_BOLD);
                    button.setGravity(Gravity.CENTER);
                    button.setHeight(105);
                    button.setWidth(50);
                    button.setBackgroundColor(Color.GRAY);
                    row.addView(button);

                    final int currentRow = i;

                    // adds button functionality
                    button.setOnClickListener(new View.OnClickListener() {
                        // When button clicked, it deletes entry
                        public void onClick(View v) {
                            myAliquots.deleteEntry(currentRow);
                        }
                    });

                }
            }

            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.returnToMenu:
                Intent openMainMenu = new Intent("android.intent.action.MAINMENU");
                startActivity(openMainMenu);
                return true;
            case R.id.editProfileMenu:
                Intent openUserProfile = new Intent(
                        "android.intent.action.USERPROFILE");
                startActivity(openUserProfile);
                return true;
            case R.id.helpMenu:
                Intent openHelpBlog = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://joyenettles.blogspot.com"));
                startActivity(openHelpBlog);
                return true;
            case R.id.exitProgram:
                finish();
                System.exit(0);

                // case R.id.deleteFileMenu:
                // Intent openReportSettingsMenu = new
                // Intent("android.intent.action.REPORTSETTINGSMENU");
                // startActivity(openReportSettingsMenu);
                // return true;
                // case R.id.renameFileMenu:
                // Intent openAliquotMenu = new
                // Intent("android.intent.action.ALIQUOTMENU");
                // startActivity(openAliquotMenu);
                // return true;
                // case R.id.defaultFileMenu:
                // Intent openAliquotMenu = new
                // Intent("android.intent.action.ALIQUOTMENU");
                // startActivity(openAliquotMenu);
                // return true;

                // case R.id.selectAliquotMenu:
                // Intent openAliquotMenu = new
                // Intent("android.intent.action.ALIQUOTMENU");
                // startActivity(openAliquotMenu);
                // return true;
                // case R.id.selectReportSettingsMenu:
                // Intent openReportSettingsMenu = new
                // Intent("android.intent.action.REPORTSETTINGSMENU");
                // startActivity(openReportSettingsMenu);
                // return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
package org.cirdles.chroni;

import java.util.ArrayList;
import java.util.Random;

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

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

	// sets up the layout
	super.onCreate(savedInstanceState);
	setTheme(android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen);
	setContentView(R.layout.history);

	// Sets up the finish button
	finishButton = (Button) findViewById(R.id.historyFinishButton);
	finishButton.setOnClickListener(new OnClickListener() {
	    public void onClick(View v) {
		Intent openMainMenu = new Intent(
			"android.intent.action.MAINMENU");
		startActivity(openMainMenu);
	    }
	});

	final CirdlesDatabaseHelper myAliquots = new CirdlesDatabaseHelper(this);

	if (!myAliquots.isEmpty()) {
	    // Collects information from the database if it isn't empty
	    final String[][] database = myAliquots.fillTableData(); // completes
								    // 2D array
								    // of
								    // aliquot
								    // data
	    final long ROWS = myAliquots.getEntryCount() - 1;
	    final long COLUMNS = 3;

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

		for (int j = 0; j < COLUMNS; j++) {
		    // adds columns to the table
		    // j = column number (starting at 1)

		    if (j != 2 || i == 0) {
			TextView cell = new TextView(this);
			cell.setText(database[i][j]);
			cell.setPadding(3, 4, 3, 4);
			cell.setTextSize((float) 17);
			cell.setGravity(Gravity.CENTER);
			cell.setWidth(275);
			cell.setHeight(75);

			// sets up width of each column
			if (j == 2) {
			    cell.setWidth(275);
			    cell.setHeight(100);
			}

			if (i % 2 == 1) {
			    // colors even rows
			    cell.setBackgroundColor(Color.parseColor("#107AB3"));
			    cell.setTextColor(Color.WHITE);

			} else {
			    cell.setBackgroundColor(Color.WHITE);
			    cell.setTextColor(Color.BLACK);
			}

			// Sets up the rows of the table
			cell.setGravity(Gravity.CENTER);
			cell.setHeight(100);
			cell.setTypeface(Typeface.DEFAULT_BOLD);

			row.addView(cell);
		    } // ends the formatting of the text cells

		    // adds the button to the last column
		    else if (j == 2 && i != 0) {
			Button cell = new Button(this);
			cell.setTextColor(Color.WHITE);
			cell.setTextSize((float) 15);
			cell.setGravity(Gravity.CENTER);
			cell.setHeight(100);
			row.addView(cell);

			cell.setText("OPEN");

			// adds button functionality
			cell.setOnClickListener(new View.OnClickListener() {
			    // When view/edit is clicked, the review screen is
			    // opened
			    public void onClick(View v) {
				Intent openTableScreen = new Intent(
					"android.intent.action.DISPLAY");
				startActivity(openTableScreen);
			    }
			});

			// Colors buttons
			if (i % 2 == 1) {
			    // colors even rows
			    cell.setBackgroundColor(Color.parseColor("#107AB3"));
			    cell.setTextColor(Color.WHITE);

			} else {
			    cell.setBackgroundColor(Color.WHITE);
			    cell.setTextColor(Color.BLACK);
			}

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
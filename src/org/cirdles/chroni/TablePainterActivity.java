package org.cirdles.chroni;

import java.io.File;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TableRow.LayoutParams;
import android.widget.Toast;


/*
This class creates the display table after parsing the aliquot and report settings files
 */
public class TablePainterActivity extends Activity {

    private static TreeMap<Integer, Category> categoryMap; // map returned from parsing Report Settings
    private static TreeMap<String, Fraction> fractionMap; // map returned from parsing Aliquot
    private static TreeMap<String, Image> imageMap; // map of image data returned from parsing Aliquot
    private static Image[] imageArray; // holds images from parsed Aliquot files
    private static String[][] finalArray; // the completely parsed array for displaying
    private static ArrayList<String> outputVariableName; // output variable names for column work

    private static int columnCount; // maintains a count of the number of columns in the final display table

    private CHRONIDatabaseHelper entryHelper; // used to help with history database

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_Holo);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        setContentView(R.layout.display);

        // Instantiates the Report Settings Parser
        ReportSettingsParser reportSettingsParser = new ReportSettingsParser();
        String reportSettingsPath = Environment.getExternalStorageDirectory()
                + "/CHRONI/Report Settings/Default Report Settings.xml"; // sets default Report Settings XML
        if (getIntent().getStringExtra("ReportSettingsXML") != null) {
            reportSettingsPath = getIntent().getStringExtra("ReportSettingsXML"); // gets the new location of the report settings xml
        }

        // Parses the Report Settings XML file
        categoryMap = (TreeMap<Integer, Category>) reportSettingsParser.runReportSettingsParser(reportSettingsPath);
        ArrayList<String> outputVariableNames = reportSettingsParser.getOutputVariableName();

        // Instantiates the Aliquot Parser
        AliquotParser aliquotParser = new AliquotParser();
        String aliquotPath = "";
        if (getIntent().getStringExtra("AliquotXML") != null) {
            aliquotPath = getIntent().getStringExtra("AliquotXML"); // gets the new location of the aliquot xml
        }

        // Parses aliquot file and retrieves maps
        MapTuple maps = aliquotParser.runAliquotParser(aliquotPath);
        fractionMap = (TreeMap<String, Fraction>) maps.getFractionMap();
        imageMap = (TreeMap<String, Image>) maps.getImageMap();

        final String aliquot = aliquotParser.getAliquotName();

        // Fills the Report Setting and Aliquot arrays
        String[][] reportSettingsArray = fillReportSettingsArray(
                outputVariableNames, categoryMap);
        String[][] fractionArray = fillFractionArray(outputVariableNames,
                categoryMap, fractionMap, aliquot);

        //Sorts the table array
        Arrays.sort(fractionArray, new Comparator<String[]>() {
            @Override
            public int compare(final String[] entry1, final String[] entry2) {
                int retVal = 0;

                final String field1 = entry1[0].trim();
                final String field2 = entry2[0].trim();

                Comparator<String> forNoah = new IntuitiveStringComparator<String>();
                return retVal = forNoah.compare(field1, field2);
            }
        });

        String[][] finalArray = fillArray(outputVariableNames, reportSettingsArray, fractionArray); // Creates the final table array for displaying

        ArrayList<String> contents = new ArrayList<String>();

        // Creates database entry from current entry
        entryHelper = new CHRONIDatabaseHelper(this);
        entryHelper.createEntry(getCurrentTime(), aliquotPath, reportSettingsPath);
        Toast.makeText(TablePainterActivity.this, "Your current table info has been stored!", Toast.LENGTH_LONG).show();

        // Creates the row for the buttons
        TableRow labelRow = (TableRow) findViewById(R.id.labelRow);
        labelRow.setGravity(Gravity.CENTER);

        // Adds a label with the current report settings
        TextView reportSettingsCell = new TextView(this);
        String[] reportSettingsPathParts = reportSettingsPath.split("/");
        String currentReportSettingsFileName = reportSettingsPathParts[reportSettingsPathParts.length -1];
        reportSettingsCell.setText("Current Report Settings File: " + currentReportSettingsFileName);
        reportSettingsCell.setTextColor(Color.BLACK);
        reportSettingsCell.setTypeface(Typeface.DEFAULT_BOLD);
        reportSettingsCell.setBackgroundResource(R.drawable.background_blue_background);
        reportSettingsCell.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        reportSettingsCell.setTextSize((float) 20);
        reportSettingsCell.setPadding(25, 25, 25, 25);
        labelRow.addView(reportSettingsCell);

        // Adds a label with the current aliquot file
//        TextView aliquotCell = new TextView(this);
//        aliquotCell.setText("Aliquot");
//        aliquotCell.setTextColor(Color.LTGRAY);
//        aliquotCell.setBackgroundColor(getResources().getColor(R.color.button_blue));
//        aliquotCell.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
//        aliquotCell.setTextSize((float) 15);
//        aliquotCell.setPadding(15, 15, 15, 15);
//        labelRow.addView(aliquotCell);

        // Setup to add buttons
        TableRow buttonRow = (TableRow) findViewById(R.id.buttonRow);
        buttonRow.setGravity(Gravity.CENTER);
//    buttonRow.setBackgroundColor(Color.GRAY);

        // Creates the Report Settings button
        Button changeReportSettingsButton = new Button(this);
        changeReportSettingsButton.setTextColor(Color.WHITE);
        changeReportSettingsButton.setTextSize((float) 18);
        changeReportSettingsButton.setText("Change Report Settings");
        changeReportSettingsButton.setPadding(15, 15, 15, 15);
        changeReportSettingsButton.setGravity(Gravity.CENTER);
        changeReportSettingsButton.setBackgroundColor(getResources().getColor(R.color.button_blue));
        changeReportSettingsButton.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        buttonRow.addView(changeReportSettingsButton);
        changeReportSettingsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent openReportSettingsMenu = new Intent("android.intent.action.REPORTSETTINGSMENU");
                startActivity(openReportSettingsMenu);
            }
        });

        // Determines whether or not to add additional buttons for images
        imageArray = retrieveImages(imageMap);

        // Adds button to view a concordia image
        if ((imageArray[0] != null) && !(imageArray[0].getImageURL().length() == 0)) {
            Button viewConcordiaButton = new Button(this);
            viewConcordiaButton.setTextColor(Color.WHITE);
            viewConcordiaButton.setTextSize((float) 18);
            viewConcordiaButton.setText("Concordia Plot");
            viewConcordiaButton.setPadding(15, 15, 15, 15);
            viewConcordiaButton.setGravity(Gravity.CENTER);
            viewConcordiaButton.setBackgroundColor(getResources().getColor(R.color.button_blue));
            viewConcordiaButton.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            buttonRow.addView(viewConcordiaButton);
            viewConcordiaButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Checks internet connection before getting images
                    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo mobileWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                    if (mobileWifi.isConnected()) {
                        Toast.makeText(TablePainterActivity.this, "Opening Concordia Image...", Toast.LENGTH_LONG).show();
                        Intent viewConcordiaIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(imageArray[0].getImageURL()));
//                    Intent viewConcordiaIntent = new Intent("android.intent.action.VIEWANALYSISIMAGE" );
                        viewConcordiaIntent.putExtra("ConcordiaImage", imageArray[0].getImageURL());
                        startActivity(viewConcordiaIntent);
                    } else {
                        //Handles lack of wifi connection
                        Toast.makeText(TablePainterActivity.this, "Please check your internet connection to view this image.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

       //Adds button to view a probability density image
        if ((imageArray[1] != null) && !(imageArray[1].getImageURL().length() == 0)) {
            Button viewProbabilityDensityButton = new Button(this);
            viewProbabilityDensityButton.setTextColor(Color.WHITE);
            viewProbabilityDensityButton.setTextSize((float) 15);
            viewProbabilityDensityButton.setText("Probability Density");
            viewProbabilityDensityButton.setPadding(15, 15, 15, 15);
            viewProbabilityDensityButton.setGravity(Gravity.CENTER);
            viewProbabilityDensityButton.setBackgroundColor(getResources().getColor(R.color.button_blue));
            viewProbabilityDensityButton.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            buttonRow.addView(viewProbabilityDensityButton);
            viewProbabilityDensityButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Checks internet connection before getting images
                    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo mobileWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                    if (mobileWifi.isConnected()) {
                        Toast.makeText(TablePainterActivity.this, "Opening Probability Density Image...", Toast.LENGTH_LONG).show();
                        Intent viewProbabilityDensityIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(imageArray[1].getImageURL()));
//                    Intent viewProbabilityDensityIntent = new Intent("android.intent.action.VIEWANALYSISIMAGE" );
//                    viewProbabilityDensityIntent.putExtra("ProbabilityDensityImage", imageArray[1].getImageURL());
                        startActivity(viewProbabilityDensityIntent);
                    } else {
                        //Handles lack of wifi connection
                        Toast.makeText(TablePainterActivity.this, "Please check your internet connection to view this image.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        HorizontalScrollView screenScroll = (HorizontalScrollView) findViewById(R.id.horizontalScrollView); // controls the horizontal scrolling of the table

        LinearLayout tableLayout = (LinearLayout) findViewById(R.id.displayTableLayout); // gives inner table layout for displaying
        TableLayout headerInformationTable = (TableLayout) findViewById(R.id.tableForHeader); // Report Settings header table

        ScrollView scrollPane = (ScrollView) findViewById(R.id.scrollPane); // Vertical scrolling for the aliquot portion of the table
        TableLayout aliquotDataTable = (TableLayout) findViewById(R.id.finalTable); // the aliquot specific info contained here

        // calculates number of rows based on the size of the fraction, five is separately
        // added for the Report Settings category rows
        final int ROWS = 5 + fractionMap.size();
        final int COLS = outputVariableNames.size();
        int rowCount = 0;

        // Gets column sizes from string array
        int[] columnSizes = distributeColumnsAppropriately(finalArray, ROWS, COLS);

        // Table Layout Printing
        for (int i = 0; i < ROWS; i++) {

            TableRow row = new TableRow(this);

            // puts rows in appropriate place on layout
            if (rowCount < 5) {
                // Report Settings and aliquot name rows
                headerInformationTable.addView(row);
            } else {
                // Adds aliquot rows to the aliquot scroll table
                aliquotDataTable.addView(row);
            }

            // loops through number of columns and adds text views to each row.
            // this creates cells!
            for (int j = 0; j < COLS; j++) {
                TextView cell = new TextView(this);
                cell.setTypeface(Typeface.MONOSPACE);
                cell.setMinEms(columnSizes[j]+2); // sets column spacing based on max character count and allows extra space for crowding
//                cell.setWidth(205);
                cell.setPadding(3, 4, 3, 4);
                cell.setTextColor(Color.BLACK);
                cell.setTextSize((float) 14.5);
                cell.setGravity(Gravity.RIGHT);

                if (rowCount < 5) {
                    cell.setTypeface(Typeface.DEFAULT_BOLD);
                    cell.setTypeface(Typeface.MONOSPACE);
                    cell.setGravity(Gravity.CENTER);
                }

                if (rowCount == 0) {
                    cell.setGravity(Gravity.LEFT);
                }

                // sets appropriate background color for cells
                if (rowCount < 4) {
                    // colors header rows
                    // if(rowCount % 2 == 0){ // colors header rows
                    cell.setBackgroundResource(R.drawable.background_blue_background);
                    // }else{
                    // cell.setBackgroundResource(R.drawable.dark_grey_background);
                    // }
                } else if (rowCount > 4 && rowCount % 2 == 1) {
                    // colors odd body rows
                    cell.setBackgroundResource(R.drawable.light_grey_background);
                } else if (rowCount == 4) {
                    // aliquot name cell
                    cell.setTextColor(Color.WHITE);
                    cell.setBackgroundResource(R.drawable.light_blue_background);
                    cell.setTypeface(Typeface.DEFAULT_BOLD);
                    cell.setTypeface(Typeface.MONOSPACE);

                } else {
                    // header rows and all other body rows
                    cell.setBackgroundResource(R.drawable.white_background);
                }

                // populates the first row with the associated category info
                // corresponding to each column
                // sets duplicates to invisible if a specified category name is
                // already presently being displayed
                if (rowCount == 0) {
                    if (contents.size() != 0  && contents.contains(finalArray[i][j])) {
                        cell.setText(finalArray[i][j]);
                        cell.setTextColor(Color.TRANSPARENT);
                    } else {
                        //category name doesn't exist in contents arraylist
                        cell.setText(finalArray[i][j]);
                        cell.setVisibility(View.VISIBLE);
                        contents.add(finalArray[i][j]);
                    }
                } else {
                    cell.setText(finalArray[i][j]);
                }

                if (cell.getText().equals("-")) {
                    cell.setGravity(Gravity.CENTER);
                }
                // append an individual cell to a content row
                row.addView(cell);
            }
            rowCount++;
        }
    }

    /*
    Goes through and figures out columns lengths given a table
     */
    protected int[] distributeColumnsAppropriately(String[][] finalArray, int ROWS, int COLS){
        int[] columnMaxCharacterCounts = new int[COLS];
        for (int currentColumn = 0; currentColumn < COLS; currentColumn++) {
            int widestCellCharacterCount = 0;
            int currentCellCharacterCount = 0;
            for (int currentRow = 0; currentRow < ROWS; currentRow++) {
//                Log.i("Column: " + currentColumn +  " Cell: " + currentRow + " Width: " + currentCellCharacterCount, "Measuring");
                currentCellCharacterCount = finalArray[currentRow][currentColumn].length();

                if(currentCellCharacterCount > widestCellCharacterCount){
                    widestCellCharacterCount = currentCellCharacterCount;
                }
//                Log.i("Widest Cell: " + widestCellCharacterCount, "Result");
            }
            columnMaxCharacterCounts[currentColumn] = widestCellCharacterCount/2; // Divides by 2 for appropriate EMS measurement
//            Log.i("Column: " + currentColumn +  " Widest Cell: " + widestCellCharacterCount, "Measuring");
        }
        return columnMaxCharacterCounts;
    }

    /*
     * Retrieves the images from the image map
     */
    private static Image[] retrieveImages(TreeMap<String, Image> imageMap) {
    	Image[] imageArray = new Image[3]; // two spaces allotted for the probability and concordia images, 3rd for report csv image 
    	Iterator<Entry<String, Image>> iterator = imageMap.entrySet().iterator();
    	int imageCount = 0; // image iterator
    	while (iterator.hasNext()) {
    		Entry<String, Image> image = iterator.next();
    		imageArray[imageCount] = image.getValue(); // places image in array
    	}
	
	 return imageArray;
    }

    /*
     * Fills the Report Settings portion of the array
     */
    private static String[][] fillReportSettingsArray(
	    ArrayList<String> outputVariableName,
	    TreeMap<Integer, Category> categoryMap) {
	final int COLUMNS = outputVariableName.size();
	final int ROWS = 4; // 4 is the number of rows for specific information
			    // in the array

	String[][] reportSettingsArray = new String[ROWS][COLUMNS];
	int columnNum = 0; // the current column number for the array

	Iterator<Entry<Integer, Category>> iterator = categoryMap.entrySet().iterator();
	while (iterator.hasNext()) {
	    Entry<Integer, Category> category = iterator.next();
	    int columnCount = 1; // the number of columns under each category

	    Iterator<Entry<Integer, Column>> columnIterator = category
		    .getValue().getCategoryColumnMap().entrySet().iterator();
	    while (columnIterator.hasNext()) {
		Entry<Integer, Column> column = columnIterator.next();
		int rowNum = 0; // the current row number for the array

		// Will always be in the 0th row, Category Information stored
		// here
		if (columnCount <= 1) {
		    reportSettingsArray[rowNum][columnNum] = category
			    .getValue().getDisplayName();
		    rowNum++;
		} else {
		    reportSettingsArray[rowNum][columnNum] = "";
		    rowNum++;
		}

		// puts the displayNames in the array
		reportSettingsArray[rowNum][columnNum] = column.getValue()
			.getDisplayName1();
		rowNum++;

		reportSettingsArray[rowNum][columnNum] = column.getValue()
			.getDisplayName2();
		rowNum++;

		reportSettingsArray[rowNum][columnNum] = column.getValue()
			.getDisplayName3();
		rowNum++;

		// String methodName = column.getValue().getMethodName();
		// String variableName = column.getValue().getVariableName();

		columnNum++;
		columnCount++;

		// Fills in uncertainty column information
		if (column.getValue().getUncertaintyColumn() != null) {
		    rowNum = 0;
		    reportSettingsArray[rowNum][columnNum] = "";
		    rowNum++;

		    // puts the displayNames in the array
		    reportSettingsArray[rowNum][columnNum] = column.getValue()
			    .getUncertaintyColumn().getDisplayName1();
		    rowNum++;

		    reportSettingsArray[rowNum][columnNum] = column.getValue()
			    .getUncertaintyColumn().getDisplayName2();
		    if (reportSettingsArray[rowNum][columnNum]
			    .equals("PLUSMINUS2SIGMA")) {
			reportSettingsArray[rowNum][columnNum] = "\u00B12\u03C3";
		    }
		    rowNum++;

		    reportSettingsArray[rowNum][columnNum] = column.getValue()
			    .getUncertaintyColumn().getDisplayName3();
		    if (reportSettingsArray[rowNum][columnNum]
			    .equals("PLUSMINUS2SIGMA%")) {
			reportSettingsArray[rowNum][columnNum] = "\u00B12\u03C3%";
		    }

		    rowNum++;

		    // String uncertaintyMethodName =
		    // column.getValue().getUncertaintyColumn().getMethodName();
		    // String uncertaintyVariableName =
		    // column.getValue().getUncertaintyColumn().getVariableName();

		    columnNum++;
		    columnCount++;
		}
	    }
	}
	return reportSettingsArray;
    }

    /*
     * Fills the Aliquot portion of the array
     */
    public static String[][] fillFractionArray(
	    ArrayList<String> outputVariableName,
	    TreeMap<Integer, Category> categoryMap,
	    TreeMap<String, Fraction> fractionMap, String aliquotName) {
	BigDecimal valueToBeRounded;
	BigDecimal roundedValue;
	final int COLUMNS = outputVariableName.size();
	final int ROWS = fractionMap.size();
	String[][] fractionArray = new String[ROWS][COLUMNS];
	int arrayColumnCount = 0; // the current column number for the array

	// Fills in the rows reserved for the aliquot name
	fractionArray[0][0] = aliquotName;
	for (int j = 1; j < COLUMNS; j++) {
	    fractionArray[0][j] = " ";
	}

	Iterator<Entry<Integer, Category>> categoryIterator = categoryMap
		.entrySet().iterator();
	while (categoryIterator.hasNext()) {
	    Entry<Integer, Category> category = categoryIterator.next();
	    Iterator<Entry<Integer, Column>> columnIterator = category
		    .getValue().getCategoryColumnMap().entrySet().iterator();

	    while (columnIterator.hasNext()) {
		Entry<Integer, Column> column = columnIterator.next();
		String variableName = column.getValue().getVariableName();

		// going to iterate through all fractions for every column
		Iterator<Entry<String, Fraction>> fractionIterator = fractionMap
			.entrySet().iterator();
		int arrayRowCount = 0; // the current row number for the array;

		while (fractionIterator.hasNext()) {
		    Entry<String, Fraction> fraction = fractionIterator.next();
		    if (variableName.equals("")) {
			fractionArray[arrayRowCount][arrayColumnCount] = fraction
				.getValue().getFractionID();
			arrayRowCount++;
		    } else {
			ValueModel valueModel = DomParser.getValueModelByName(
				fraction.getValue(), variableName);

			if (valueModel != null) {
			    float initialValue = valueModel.getValue();
			    String currentUnit = column.getValue().getUnits();
			    int countOfSignificantDigits = column.getValue()
				    .getCountOfSignificantDigits();
			    if (Numbers.getUnitConversionsMap().containsKey(
				    currentUnit)) {
				Integer dividingNumber = Numbers
					.getUnitConversionsMap().get(
						currentUnit);
				valueToBeRounded = new BigDecimal(initialValue
					/ (Math.pow(10, dividingNumber)));
				roundedValue = valueToBeRounded.setScale(
					countOfSignificantDigits,
					valueToBeRounded.ROUND_HALF_UP);
				fractionArray[arrayRowCount][arrayColumnCount] = String
					.valueOf(roundedValue);
			    }
			}

			else { // if value model is null
			    fractionArray[arrayRowCount][arrayColumnCount] = "-";
			}
			arrayRowCount++;

		    } // ends else
		} // ends while
		arrayColumnCount++;

		if (column.getValue().getUncertaintyColumn() != null) {
		    Iterator<Entry<String, Fraction>> fractionIterator2 = fractionMap
			    .entrySet().iterator();
		    arrayRowCount = 0; // the current row number for the array;
		    variableName = column.getValue().getUncertaintyColumn()
			    .getVariableName();
		    while (fractionIterator2.hasNext()) {
			Entry<String, Fraction> fraction = fractionIterator2
				.next();
			ValueModel valueModel = DomParser.getValueModelByName(
				fraction.getValue(), variableName);

			if (valueModel != null) {
			    float oneSigma = valueModel.getOneSigma();
			    String currentUnit = column.getValue().getUnits();
			    int uncertaintyCountOfSignificantDigits = column
				    .getValue().getUncertaintyColumn()
				    .getCountOfSignificantDigits();
			    if (Numbers.getUnitConversionsMap().containsKey(
				    currentUnit)) {
				Integer dividingNumber = Numbers
					.getUnitConversionsMap().get(
						currentUnit);
				valueToBeRounded = new BigDecimal(
					(oneSigma / (Math.pow(10,
						dividingNumber))) * 2);

				if (column.getValue().getUncertaintyType()
					.equals("PCT")) {
				    valueToBeRounded = new BigDecimal(
					    (oneSigma / (Math.pow(10,
						    dividingNumber))) * 200);
				}

				roundedValue = valueToBeRounded.setScale(
					uncertaintyCountOfSignificantDigits,
					valueToBeRounded.ROUND_HALF_UP);
				fractionArray[arrayRowCount][arrayColumnCount] = String
					.valueOf(roundedValue);
			    }
			} // closes if
			else { // if value model is null
			    fractionArray[arrayRowCount][arrayColumnCount] = "-";
			}
			arrayRowCount++;
		    } // ends loop through fraction
		    arrayColumnCount++;

		} // closes filling uncertainty columns

	    } // closes column
	} // closes category

	return fractionArray;
    } // closes method

    /*
     * Fills the entire application array.
     */
    private static String[][] fillArray(ArrayList<String> outputVariableName,
	    String[][] reportSettingsArray, String[][] fractionArray) {
	final int COLUMNS = outputVariableName.size();
	final int ROWS = 5 + fractionMap.size(); // 8 is the number of rows for
						 // specific information in the
						 // array
	String[][] finalArray = new String[ROWS][COLUMNS];

	// Fills in the Report Settings Part of Array
	for (int i = 0; i < 4; i++) {
	    for (int j = 0; j < COLUMNS; j++) {
		finalArray[i][j] = reportSettingsArray[i][j];
	    }
	}

	// Fills in the empty row for the aliquot
	finalArray[4][0] = AliquotParser.getAliquotName();
	for (int j = 1; j < COLUMNS; j++) {
	    finalArray[4][j] = " ";
	}

	// Fills in the fraction information of the array
	for (int column = 0; column < COLUMNS; column++) {
	    for (int row = 5; row < ROWS; row++) {
		finalArray[row][column] = fractionArray[(row - 5)][column];
	    }
	}
	return finalArray;
    }

    public String[][] getFinalArray() {
	return finalArray;
    }

    public static void setFinalArray(String[][] newFinalArray) {
	TablePainterActivity.finalArray = newFinalArray;
    }

    public static ArrayList<String> getOutputVariableName() {
	return outputVariableName;
    }

    public static void setOutputVariableName(
	    ArrayList<String> outputVariableName) {
	TablePainterActivity.outputVariableName = outputVariableName;
    }

    public static int getColumnCount() {
        return columnCount;
    }

    public static void setColumnCount(int count) {
        columnCount = count;
    }

    /*
 * This method gets the current time.
 */
    public String getCurrentTime(){
        java.text.DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy KK:mm");
        Date date = new Date();
        String time = dateFormat.format(date);
        return time;
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
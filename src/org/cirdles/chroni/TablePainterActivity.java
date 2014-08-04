package org.cirdles.chroni;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TableRow.LayoutParams;

public class TablePainterActivity extends Activity {

	private Button changeReportSettingsButton, viewConcordiaButton, viewProbabilityDensityButton; // display layout buttons
	
    private static String concordiaUrl, probabilityDensityUrl; // urls to neccessary images

    private static TreeMap<Integer, Category> categoryMap; // map returned from parsing Report Settings
    private static TreeMap<String, Fraction> fractionMap; // map returned from parsing Aliquot
    private static TreeMap<String, Image> imageMap; // map of image data returned from parsing Aliquot

	private static MapTuple maps; // The fraction and image map from parsing Aliquot file
    private static Image[] imageArray;  
	
    private static String[][] finalArray; // the completed array for displaying
    private static ArrayList<String> outputVariableName; // output variable names for column work

    private TextView testText;
    private String test;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setTheme(android.R.style.Theme_Holo);
	setContentView(R.layout.display);
	
	// Directories needed to place files in accurate locations
	File chroniDirectory = getDir("CHRONI", Context.MODE_PRIVATE); //Creating an internal directory for CHRONI files
	File aliquotDirectory = new File(chroniDirectory, "Aliquot");
	File reportSettingsDirectory = new File(chroniDirectory, "Report Settings");
	
	// Instantiates the Report Settings Parser
	ReportSettingsParser RSP = new ReportSettingsParser();
	String reportSettingsPath = String.valueOf(new File(chroniDirectory, "Report Settings")) + "/Default Report Settings.xml"; // sets default Report Settings XML
	if (getIntent().getStringExtra("ReportSettingsXML") != null) {
	    reportSettingsPath = getIntent().getStringExtra("ReportSettingsXML"); // gets the new location of the report settings xml
	}
	categoryMap = (TreeMap<Integer, Category>) RSP.runReportSettingsParser(reportSettingsPath);
	ArrayList<String> outputVariableName = RSP.getOutputVariableName();

	// Instantiates the Aliquot Parser
	AliquotParser AP = new AliquotParser();
	String aliquotPath = "";
	if (getIntent().getStringExtra("AliquotXML") != null) {
	    aliquotPath = getIntent().getStringExtra("AliquotXML"); // gets the new location of the aliquot xml
	}
	
	// Parses aliquot file and retrieves maps
	maps = AP.runAliquotParser(aliquotPath); 
	fractionMap = (TreeMap<String, Fraction>)maps.getFractionMap();
	imageMap = (TreeMap<String, Image>)maps.getImageMap();
	
	String aliquot = AP.getAliquotName();

	// fills the arrays
	String[][] reportSettingsArray = fillReportSettingsArray(
		outputVariableName, categoryMap);
	String[][] fractionArray = fillFractionArray(outputVariableName,
		categoryMap, fractionMap, aliquot);

	// handles the array sorting
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

	String[][] finalArray = fillArray(outputVariableName,reportSettingsArray, fractionArray);

	// TextView aliquotName = new TextView(this);
	ArrayList<String> contents = new ArrayList<String>();

	// Setup to add buttons
	LinearLayout buttonTableLayout = (LinearLayout) findViewById(R.id.displayLayout); // layout handle needed for buttons
	TableRow buttonRow = (TableRow) findViewById(R.id.buttonRow);
	buttonRow.setGravity(Gravity.CENTER);
	
	// Creates the Report Settings button
	changeReportSettingsButton = new Button(this);
	changeReportSettingsButton.setTextColor(Color.WHITE);
	changeReportSettingsButton.setTextSize((float) 15);
	changeReportSettingsButton.setText("Change Report Settings");
	changeReportSettingsButton.setPadding(15,15,15,15);
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
	
    if((imageArray[0] != null) && !(imageArray[0].getImageURL().length()==0)){
		viewConcordiaButton = new Button(this);
		viewConcordiaButton.setTextColor(Color.WHITE);
		viewConcordiaButton.setTextSize((float) 15);
		viewConcordiaButton.setText("Concordia Plot");
		viewConcordiaButton.setPadding(15,15,15,15);
		viewConcordiaButton.setGravity(Gravity.CENTER);
		viewConcordiaButton.setBackgroundColor(getResources().getColor(R.color.button_blue));
		viewConcordiaButton.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		buttonRow.addView(viewConcordiaButton);
		viewConcordiaButton.setOnClickListener(new View.OnClickListener() {
			    public void onClick(View v) {
				    Intent viewConcordiaIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(imageArray[0].getImageURL()));
				    startActivity(viewConcordiaIntent);
			    }
			});
    }
    
    if((imageArray[1] != null) && !(imageArray[1].getImageURL().length()==0)){
		viewProbabilityDensityButton = new Button(this);
		viewProbabilityDensityButton.setTextColor(Color.WHITE);
		viewProbabilityDensityButton.setTextSize((float) 15);
		viewProbabilityDensityButton.setText("Probability Density");
		viewProbabilityDensityButton.setPadding(15,15,15,15);
		viewProbabilityDensityButton.setGravity(Gravity.CENTER);
		viewProbabilityDensityButton.setBackgroundColor(getResources().getColor(R.color.button_blue));
		viewProbabilityDensityButton.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		buttonRow.addView(viewProbabilityDensityButton);
		viewProbabilityDensityButton.setOnClickListener(new View.OnClickListener() {
			    public void onClick(View v) {
				    Intent viewProbabilityDensityIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(imageArray[1].getImageURL()));
				    startActivity(viewProbabilityDensityIntent);
			    }
			});
    }
	
	HorizontalScrollView screenScroll = (HorizontalScrollView) findViewById(R.id.horizontalScrollView); // controls the horizontal scrolling of the table
	
	LinearLayout tableLayout = (LinearLayout) findViewById(R.id.displayTableLayout); // gives inner table layout for displaying
	TableLayout headerInformationTable = (TableLayout) findViewById(R.id.tableForHeader); // Report Settings header table

	ScrollView scrollPane = (ScrollView) findViewById(R.id.scrollPane); // Vertical scrolling for the aliquot portion of the table
	TableLayout aliquotDataTable = (TableLayout) findViewById(R.id.finalTable); // the aliquot specific info contained here

	// calculates number of rows based on the size of the fraction, five is
	// added for the Report Settings rows
	final int ROWS = 5 + fractionMap.size();
	int rowCount = 0;

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
	    for (int j = 0; j < outputVariableName.size(); j++) {
		TextView cell = new TextView(this);
		cell.setWidth(205);
		cell.setPadding(3, 4, 3, 4);
		cell.setTextColor(Color.BLACK);
		cell.setTextSize((float) 14.5);
		cell.setGravity(Gravity.RIGHT);

		if (rowCount < 5) {
		    cell.setTypeface(Typeface.DEFAULT_BOLD);
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
		} else {
		    // header rows and all other body rows
		    cell.setBackgroundResource(R.drawable.white_background);
		}

		// populates the first row with the associated category info
		// corresponding to each column
		// sets duplicates to invisible if a specified category name is
		// already presently being displayed
		if (rowCount == 0) {
		    if (contents.size() != 0
			    && contents.contains(finalArray[i][j])) {
			cell.setText(finalArray[i][j]);
			cell.setTextColor(Color.TRANSPARENT);
		    } else {
			// category name doesn't exist in contents arraylist
			cell.setText(finalArray[i][j]);
			cell.setVisibility(1);
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

	Iterator<Entry<Integer, Category>> iterator = categoryMap.entrySet()
		.iterator();
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
	    // Intent openHelpBlog = new Intent(Intent.ACTION_VIEW,
	    // Uri.parse("http://joyenettles.blogspot.com"));
	    // startActivity(openHelpBlog);
	    // return true;
	case R.id.exitProgram:
	    finish();
	    System.exit(0);

	default:
	    return super.onOptionsItemSelected(item);
	}
    }

}
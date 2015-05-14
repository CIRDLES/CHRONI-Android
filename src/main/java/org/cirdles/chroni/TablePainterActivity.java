package org.cirdles.chroni;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
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
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
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
    private static String[][] finalArray; // the completely parsed array for displaying
    private static ArrayList<String> outputVariableName; // output variable names for column work
    private static int columnCount; // maintains a count of the number of columns in the final display table

    private String aliquotFilePath, reportSettingsFilePath; // The complete path of the aliquot and report settings files to be parsed and display

    private CHRONIDatabaseHelper entryHelper; // used to help with history database
    private static final String PREF_REPORT_SETTINGS = "Current Report Settings";// Path of the current report settings file
    private static final String PREF_ALIQUOT = "Current Aliquot";// Path of the current report settings file

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        setContentView(R.layout.display);

        // Instantiates the Report Settings Parser and gets the current Report Settings path
        ReportSettingsParser reportSettingsParser = new ReportSettingsParser();
        setReportSettingsFilePath(retrieveReportSettingsFilePath());

        // Parses the Report Settings XML file
        categoryMap = (TreeMap<Integer, Category>) reportSettingsParser.runReportSettingsParser(getReportSettingsFilePath());
        ArrayList<String> outputVariableNames = reportSettingsParser.getOutputVariableName();

        // Instantiates the Aliquot Parser and gets the current Aliquot path
        AliquotParser aliquotParser = new AliquotParser();
        setAliquotFilePath(retrieveAliquotFilePath());

        // Parses aliquot file and retrieves maps
        MapTuple maps = aliquotParser.runAliquotParser(getAliquotFilePath());
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
        entryHelper.createEntry(getCurrentTime(), getAliquotFilePath(), getReportSettingsFilePath());
        Toast.makeText(TablePainterActivity.this, "Your current table info has been stored!", Toast.LENGTH_LONG).show();

        // Creates the row for the buttons
        TableRow labelRow = (TableRow) findViewById(R.id.labelRow);
        labelRow.setGravity(Gravity.CENTER);

        // Adds a button with the current report settings files
        Button reportSettingsCell = new Button(this);
        String reportSettingsText = "Report Settings: " + splitFileName(retrieveReportSettingsFilePath());
        reportSettingsCell.setTextSize((float) 15);
        reportSettingsCell.setText(reportSettingsText);
        reportSettingsCell.setTextColor(Color.BLACK);
        reportSettingsCell.setTypeface(Typeface.DEFAULT_BOLD);
        reportSettingsCell.setBackgroundColor(getResources().getColor(R.color.button_blue));
        reportSettingsCell.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        reportSettingsCell.setPadding(25, 25, 25, 25);
        labelRow.addView(reportSettingsCell);
        reportSettingsCell.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent openReportSettingsMenu = new Intent("android.intent.action.REPORTSETTINGSMENU");
                startActivity(openReportSettingsMenu);
            }
        });

        // Adds a label button with the current aliquot files
        Button aliquotCell = new Button(this);
        String aliquotCellText = "Aliquot: " + splitFileName(retrieveAliquotFilePath());
        aliquotCell.setTextSize((float) 15);
        aliquotCell.setText(aliquotCellText);
        aliquotCell.setTextColor(Color.BLACK);
        aliquotCell.setTypeface(Typeface.DEFAULT_BOLD);
        aliquotCell.setBackgroundResource(R.drawable.background_blue_background);
        aliquotCell.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        aliquotCell.setPadding(25, 25, 25, 25);
        labelRow.addView(aliquotCell);
        aliquotCell.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent openAliquotMenu = new Intent("android.intent.action.ALIQUOTMENU");
                startActivity(openAliquotMenu);
            }
        });

        // Setup to add image buttons
        TableRow buttonRow = (TableRow) findViewById(R.id.buttonRow);
        buttonRow.setGravity(Gravity.CENTER);
        buttonRow.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));


        // Adds button to view a concordia image
        if ((imageMap.containsKey("concordia"))) {
            Button viewConcordiaButton = new Button(this);
            viewConcordiaButton.setTextColor(Color.BLACK);
            viewConcordiaButton.setTextSize((float) 13);
            viewConcordiaButton.setText("Concordia");
            viewConcordiaButton.setTypeface(Typeface.DEFAULT_BOLD);
            viewConcordiaButton.setPadding(15, 15, 15, 15);
            viewConcordiaButton.setGravity(Gravity.CENTER);
            viewConcordiaButton.setBackgroundColor(getResources().getColor(R.color.light_grey));
            viewConcordiaButton.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            buttonRow.addView(viewConcordiaButton);
            viewConcordiaButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Checks internet connection before getting images
                    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo mobileWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                    if (mobileWifi.isConnected()) {
                        // Displays concordia images
                        Toast.makeText(TablePainterActivity.this, "Opening Concordia Image...", Toast.LENGTH_LONG).show();
                        Intent viewConcordiaIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(imageMap.get("concordia").getImageURL()));
//                    Intent viewConcordiaIntent = new Intent("android.intent.action.VIEWANALYSISIMAGE" );
//                        viewConcordiaIntent.putExtra("ConcordiaImage", imageMap.get("concordia").getImageURL());
                        startActivity(viewConcordiaIntent);
                    } else {
                        //Handles lack of wifi connection
                        Toast.makeText(TablePainterActivity.this, "Please check your internet connection to view this image.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        //Adds button to view a probability density image
        if ((imageMap.containsKey("probability_density"))) {
            Button viewProbabilityDensityButton = new Button(this);
            viewProbabilityDensityButton.setTextSize((float) 13);
            viewProbabilityDensityButton.setText("Probability Density");
            viewProbabilityDensityButton.setTypeface(Typeface.DEFAULT_BOLD);
            viewProbabilityDensityButton.setPadding(15, 15, 15, 15);
            viewProbabilityDensityButton.setGravity(Gravity.CENTER);
            // Decides what color button should be based on if there is a concordia button
            if ((imageMap.containsKey("concordia"))) {
                viewProbabilityDensityButton.setBackgroundColor(getResources().getColor(R.color.button_blue));
                viewProbabilityDensityButton.setTextColor(Color.WHITE);
            } else {
                viewProbabilityDensityButton.setBackgroundColor(getResources().getColor(R.color.light_grey));
                viewProbabilityDensityButton.setTextColor(Color.BLACK);
            }
            viewProbabilityDensityButton.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            buttonRow.addView(viewProbabilityDensityButton);

            //Adds on click functionality
            viewProbabilityDensityButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Checks internet connection before getting images
                    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo mobileWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                    if (mobileWifi.isConnected()) {
                        Toast.makeText(TablePainterActivity.this, "Opening Probability Density Image...", Toast.LENGTH_LONG).show();
                        Intent viewProbabilityDensityIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(imageMap.get("probability_density").getImageURL()));
//                    Intent viewProbabilityDensityIntent = new Intent("android.intent.action.VIEWANALYSISIMAGE" );
//                    viewProbabilityDensityIntent.putExtra("ProbabilityDensityImage",  imageMap.get("probability_density").getImageURL());
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
        TableLayout categoryNameTable = (TableLayout) findViewById(R.id.categoryNameTable); // Header table specifically for category names
        TableLayout headerInformationTable = (TableLayout) findViewById(R.id.tableForHeader); // Report Settings header table for display names 1-3

        ScrollView scrollPane = (ScrollView) findViewById(R.id.scrollPane); // Vertical scrolling for the aliquot portion of the table
        TableLayout aliquotDataTable = (TableLayout) findViewById(R.id.finalTable); // the aliquot specific info contained here

        // calculates number of rows based on the size of the fraction, five is separately
        // added for the Report Settings category rows
        final int ROWS = 5 + fractionMap.size();
        final int COLS = outputVariableNames.size();

        // Gets column sizes from string array
        int[] columnSizes = distributeTableColumns(finalArray, ROWS, COLS);
        int[] headerCellSizes = distributeHeaderCells(columnSizes);

        // Creates the row just reserved for header names
        TableRow categoryRow = new TableRow(this);
        categoryNameTable.addView(categoryRow);
//        //TODO: Figure out more elegant way to calculate the width of a column

        int categoryCount = 0;
        // Adds just enough cells for every category name
        Iterator<Entry<Integer, Category>> iterator = categoryMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<Integer, Category> category = iterator.next();
            if (category.getValue().getColumnCount() != 0) { // removes any invisible columns that may be in map
                TextView categoryCell = new TextView(this);
                categoryCell.setText(category.getValue().getDisplayName());
                categoryCell.setTypeface(Typeface.MONOSPACE);
                if (category.getValue().getDisplayName().contentEquals("Fraction") && categoryCount != 0) { // Easy fix to handle the issue of sizing with last fraction category TODO: Make better!
                    categoryCell.setMinEms(columnSizes[columnSizes.length - 1] + 2); // Simply sets same size as fraction because its always the same length with last column
                } else {
                    categoryCell.setMinEms(headerCellSizes[categoryCount] + (2 * category.getValue().getColumnCount())); // sets column spacing based on max character count and allows extra space for crowding
                }
                categoryCell.setPadding(3, 4, 3, 4);
                categoryCell.setTextSize((float) 14.5);
                categoryCell.setTextColor(Color.BLACK);
                categoryCell.setBackgroundResource(R.drawable.background_blue_background);
                categoryCell.setGravity(Gravity.LEFT);
                categoryRow.addView(categoryCell); // Adds cell to row
                categoryCount++;
            }

        }

        int rowCount = 1; // starts counting under the category name row

        // Table Layout Printing
        for (int currentRow = 1; currentRow < ROWS; currentRow++) {

            TableRow row = new TableRow(this);

            // puts rows in appropriate place on layout
            if (currentRow < 4) {
                // Report Settings and aliquot name rows
                headerInformationTable.addView(row);

            } else {
                // Adds aliquot rows to the aliquot scroll table
                aliquotDataTable.addView(row);
            }

            // loops through number of columns and adds text views to each row.
            // this creates cells!
            for (int currentColumn = 0; currentColumn < COLS; currentColumn++) {
                TextView cell = new TextView(this);
                cell.setTypeface(Typeface.MONOSPACE);
                cell.setMinEms(columnSizes[currentColumn] + 2); // sets column spacing based on max character count and allows extra space for crowding
                cell.setPadding(3, 4, 3, 4);
                cell.setTextColor(Color.BLACK);
                cell.setTextSize((float) 14.5);
                cell.setGravity(Gravity.RIGHT);

                if (currentRow < 5) {
                    // Handles all header row and aliquot name row design
                    cell.setTypeface(Typeface.DEFAULT_BOLD);
                    cell.setTypeface(Typeface.MONOSPACE);
                    cell.setGravity(Gravity.CENTER);
                }


                // sets appropriate background color for cells
                if (currentRow < 4) {
                    // Colors all header cells same color
                    cell.setBackgroundResource(R.drawable.background_blue_background);
                } else if (currentRow == 4) {
                    // Handles aliquot name cell
                    cell.setTextColor(Color.WHITE);
                    cell.setBackgroundResource(R.drawable.light_blue_background);
                } else if (currentRow > 4 && currentRow % 2 == 1) {
                    // colors odd body rows
                    cell.setBackgroundResource(R.drawable.light_grey_background);
                } else {
                    // colors all even body rows
                    cell.setBackgroundResource(R.drawable.white_background);
                }

                // Adds text to cells
                cell.setText(finalArray[currentRow][currentColumn]);
                cell.setVisibility(View.VISIBLE);


                if (cell.getText().equals("-")) {
                    cell.setGravity(Gravity.CENTER);
                }

                //left justify fraction column
                boolean isFraction = isFractionColumn(finalArray, currentColumn);
                if (isFraction) {
                    cell.setGravity(Gravity.LEFT);
                }
//                }
                // append an individual cell to a content row
                row.addView(cell);
            }
            rowCount++;
        }

    }

    /*
* Accesses current report settings file
*/
    private String retrieveReportSettingsFilePath() {
        SharedPreferences settings = getSharedPreferences(PREF_REPORT_SETTINGS, 0);
        return settings.getString("Current Report Settings", Environment.getExternalStorageDirectory() + "/CHRONI/Report Settings/Default Report Settings.xml"); // Gets current RS and if no file there, returns default as the current file
    }

    /*
* Accesses current report settings file
*/
    private String retrieveAliquotFilePath() {
        SharedPreferences settings = getSharedPreferences(PREF_ALIQUOT, 0);
        return settings.getString("Current Aliquot", "Error"); // Gets current RS and if no file there, returns default as the current file
    }

    /*
Splits report settings file name returning a displayable version without the entire path
*/
    private String splitFileName(String fileName) {
        String[] fileNameParts = fileName.split("/");
        String newFileName = fileNameParts[fileNameParts.length - 1];
        return newFileName;
    }

    /*
    Figures out if something is a part of the Fraction column.
     */
    private boolean isFractionColumn(String[][] displayArray, int columnIndex) {
        boolean isFractionColumn = false;
        String categoryName = displayArray[0][columnIndex]; // gets name of category from the top of column
        if (categoryName.contentEquals("Fraction")) {
            isFractionColumn = true;
        } else {
            isFractionColumn = false;
        }
        return isFractionColumn;
    }

    /*
 Goes through and figures out header cell lengths given a table
  */
    protected int[] distributeHeaderCells(int[] columnWidths) {
        int[] headerMaxCharacterCounts = new int[categoryMap.size()];
        int currentCategoryCount = 0;
        int currentColumnCount = 0;

        Iterator<Entry<Integer, Category>> iterator = categoryMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<Integer, Category> category = iterator.next();
            int categoryCellWidth = 0;
            if (category.getValue().getColumnCount() != 0) { // removes any invisible columns that may be in map

                Iterator<Entry<Integer, Column>> columnIterator = category
                        .getValue().getCategoryColumnMap().entrySet().iterator();
                while (columnIterator.hasNext()) {
                    Entry<Integer, Column> column = columnIterator.next();

                    categoryCellWidth += columnWidths[currentColumnCount];
                    currentColumnCount++;

                    if (column.getValue().getUncertaintyColumn() != null) {
                        categoryCellWidth += columnWidths[currentColumnCount];
                        currentColumnCount++;
                    }
                }
            }
            headerMaxCharacterCounts[currentCategoryCount] = categoryCellWidth;
            currentCategoryCount++;
        }
        return headerMaxCharacterCounts;
    }

    /*
    Goes through and figures out columns lengths given a table
     */
    protected int[] distributeTableColumns(String[][] finalArray, int ROWS, int COLS) {
        int[] columnMaxCharacterCounts = new int[COLS];
        for (int currentColumn = 0; currentColumn < COLS; currentColumn++) {
            int widestCellCharacterCount = 0;
            int currentCellCharacterCount = 0;
            for (int currentRow = 0; currentRow < ROWS; currentRow++) {
                if (currentRow != 0) { // Skips counting the header row
                    currentCellCharacterCount = finalArray[currentRow][currentColumn].length();
//                Log.i("Column: " + currentColumn + " Cell: " + currentRow + " Width: " + currentCellCharacterCount, "Measuring");
                    if (currentCellCharacterCount > widestCellCharacterCount) {
                        widestCellCharacterCount = currentCellCharacterCount;
                    }
//                Log.i("Widest Cell: " + widestCellCharacterCount, "Result");
                }
            }
            columnMaxCharacterCounts[currentColumn] = widestCellCharacterCount / 2; // Divides by 2 for appropriate EMS measurement
//            Log.i("Column: " + currentColumn +  " Widest Cell: " + widestCellCharacterCount, "Measuring");
//            Log.i("----------------------------------------------------------------------------", "Measuring");
        }
        return columnMaxCharacterCounts;
    }


    /*
     * Fills the Report Settings portion of the array
     * @param outputVariableName used to get size of the array
     * @param categoryMap used to populate the array
     * @return reportSettingsArray the array of the report settings
     * TODO: figure out smarter way to handle output variable name param (i.e. just need to send the number)
     */
    private static String[][] fillReportSettingsArray(
            ArrayList<String> outputVariableName,
            TreeMap<Integer, Category> categoryMap) {
        final int COLUMNS = outputVariableName.size();
        final int ROWS = 4; // 4 is the number of rows for specific information
        // in the array

        String[][] reportSettingsArray = new String[ROWS][COLUMNS];
        int totalColumnCount = 0; // the current column number for the array

        Iterator<Entry<Integer, Category>> iterator = categoryMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<Integer, Category> category = iterator.next();
            int currentCategoryColumnCount = 1; // the number of columns under each category

            Iterator<Entry<Integer, Column>> columnIterator = category
                    .getValue().getCategoryColumnMap().entrySet().iterator();
            while (columnIterator.hasNext()) {
                Entry<Integer, Column> column = columnIterator.next();
                int currentRowNum = 0; // the current row number for the array

                // If this is the first column, then puts the CATEGORY information there
                // This populates the first row of the table, or skips it
                if (currentCategoryColumnCount <= 1) {
                    reportSettingsArray[currentRowNum][totalColumnCount] = category
                            .getValue().getDisplayName();
                    currentRowNum++;
                } else {
                    reportSettingsArray[currentRowNum][totalColumnCount] = "";
                    currentRowNum++;
                }

                // puts the displayNames in each row of the header array
                reportSettingsArray[currentRowNum][totalColumnCount] = column.getValue()
                        .getDisplayName1();
                currentRowNum++;

                reportSettingsArray[currentRowNum][totalColumnCount] = column.getValue()
                        .getDisplayName2();
                currentRowNum++;

                reportSettingsArray[currentRowNum][totalColumnCount] = column.getValue()
                        .getDisplayName3();
                currentRowNum++;

                // String methodName = column.getValue().getMethodName();
                // String variableName = column.getValue().getVariableName();

                totalColumnCount++;
                currentCategoryColumnCount++;

                // Fills in uncertainty column information
                if (column.getValue().getUncertaintyColumn() != null) {
                    currentRowNum = 0; // Restarts row counting because on new column
                    reportSettingsArray[currentRowNum][totalColumnCount] = ""; // Initialized as blank because no uncertainty display name will ever be on the first row
                    currentRowNum++;

                    // puts the displayNames in the array row by row
                    reportSettingsArray[currentRowNum][totalColumnCount] = column.getValue()
                            .getUncertaintyColumn().getDisplayName1();
                    currentRowNum++;

                    // Puts display names in the header array and handles formatting of special characters
                    reportSettingsArray[currentRowNum][totalColumnCount] = column.getValue()
                            .getUncertaintyColumn().getDisplayName2();
                    if (reportSettingsArray[currentRowNum][totalColumnCount]
                            .equals("PLUSMINUS2SIGMA")) {
                        reportSettingsArray[currentRowNum][totalColumnCount] = "\u00B12\u03C3";
                    }
                    currentRowNum++;

                    reportSettingsArray[currentRowNum][totalColumnCount] = column.getValue()
                            .getUncertaintyColumn().getDisplayName3();
                    if (reportSettingsArray[currentRowNum][totalColumnCount]
                            .equals("PLUSMINUS2SIGMA%")) {
                        reportSettingsArray[currentRowNum][totalColumnCount] = "\u00B12\u03C3%";
                    }

                    currentRowNum++;


                    totalColumnCount++;
                    currentCategoryColumnCount++;
                }
            }
        }
        return reportSettingsArray;
    }

    /*
     * Fills the Aliquot portion of the array
     * @param outputVariableName keeps track of the number of columns and to retrieve value models
     * @param categoryMap used to fill table
     * @param fractionMap used to fill the faction array with the information
     * @param aliquotName puts the correct name in the table
     * @return fractionArray the bottom half of the array
     */
    public static String[][] fillFractionArray(
            ArrayList<String> outputVariableName,
            TreeMap<Integer, Category> categoryMap,
            TreeMap<String, Fraction> fractionMap, String aliquotName) {
        BigDecimal valueToBeRounded;
        BigDecimal roundedValue;
        String[] cellCalculations = new String[2]; // holds calculated values
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
                String methodName = column.getValue().getMethodName();

                // going to iterate through all fractions for every column
                Iterator<Entry<String, Fraction>> fractionIterator = fractionMap
                        .entrySet().iterator();
                int arrayRowCount = 0; // the current row number for the array;

                while (fractionIterator.hasNext()) {
                    Entry<String, Fraction> currentFraction = fractionIterator.next();
                    if (variableName.equals("")) {
                        // Value Models under Fraction don't have variable names so have to account for those specifically
                        if (methodName.equals("getFractionID")) {
                            fractionArray[arrayRowCount][arrayColumnCount] = currentFraction
                                    .getValue().getFractionID();
                        } else if (methodName.equals("getNumberOfGrains")) {
                            fractionArray[arrayRowCount][arrayColumnCount] = currentFraction.getValue().getNumberOfGrains();
                        }
                        arrayRowCount++;
                    } else {
                        // Retrieves the correct value model based off the variable name
                        ValueModel valueModel = DomParser.getValueModelByName(
                                currentFraction.getValue(), variableName);

                        if (valueModel != null) {
                            // Retrieves the info necessary to fill the table and perform calculations
                            float initialValue = valueModel.getValue();
                            float oneSigma = valueModel.getOneSigma();
                            String currentUnit = column.getValue().getUnits();
                            int countOfSignificantDigits = column.getValue().getCountOfSignificantDigits();
                            boolean isArbitraryMode = column.getValue().isDisplayedWithArbitraryDigitCount(); // determines if parent column is in arbitrary mode

                            // Performs the mathematical operations for the table
                            if (Numbers.getUnitConversionsMap().containsKey(
                                    currentUnit)) {
                                Integer dividingNumber = Numbers.getUnitConversionsMap().get(
                                        currentUnit); // gets the exponent for conversion
                                SignificantFigures valueToRound = new SignificantFigures(initialValue
                                        / (Math.pow(10, dividingNumber))); // does initial calculation

                                // Testing rounding work
                                // If there is an uncertainty column, figures out number of digits for shape in next column
                                if (column.getValue().getUncertaintyColumn() != null) {
                                    fractionArray[arrayRowCount][arrayColumnCount] = String.valueOf(valueToRound);
                                }else {
                                    // Puts Parent value in the cell with just parent amount of digits if no uncertainty column
                                    valueToBeRounded= new BigDecimal(initialValue / (Math.pow(10, dividingNumber))); // does initial calculation
                                    roundedValue = valueToBeRounded.setScale(
                                        countOfSignificantDigits,
                                        valueToBeRounded.ROUND_HALF_UP);
                                fractionArray[arrayRowCount][arrayColumnCount] = String
                                        .valueOf(roundedValue);
                                }
                                // Puts Parent value in the cell
//                                roundedValue = valueToBeRounded.setScale(
//                                        countOfSignificantDigits,
//                                        valueToBeRounded.ROUND_HALF_UP);
//                                fractionArray[arrayRowCount][arrayColumnCount] = String
//                                        .valueOf(roundedValue);


                                // Testing rounding method
                                //TODO:Do this better
//                                String columnMode = ""; // determines column mode based on uncertainty
//                                if (column.getValue().getUncertaintyColumn() != null) {
//                                    boolean uncertaintyInArbitraryMode = column.getValue().getUncertaintyColumn().isDisplayedWithArbitraryDigitCount();
//                                    if (!uncertaintyInArbitraryMode && !isArbitraryMode) {
//                                        columnMode = "bothSigFig";
//                                    } else if (uncertaintyInArbitraryMode && !isArbitraryMode) {
//                                        columnMode = "parentSigFig";
//                                    } else if (!uncertaintyInArbitraryMode && isArbitraryMode) {
//                                        columnMode = "parentArbitrary";
//                                    }
//                                    int uncertaintyCountOfSigFigs = column.getValue().getUncertaintyColumn().getCountOfSignificantDigits();
//                                    roundParentCellValue(columnMode, String.valueOf(valueToBeRounded), countOfSignificantDigits, uncertaintyCountOfSigFigs);
//
//                                } else {
//                                    if (isArbitraryMode) {
//                                        columnMode = "parentArbitrary";
//                                    } else {
//                                        columnMode = "parentSigFig";
//                                    }
//                                    roundParentCellValue(columnMode, String.valueOf(valueToBeRounded), countOfSignificantDigits, countOfSignificantDigits);
//                                }
                            }
                        } else { // if value model is null puts a hyphen in place
                            fractionArray[arrayRowCount][arrayColumnCount] = "-";
                        }
                        arrayRowCount++;

                    } // ends else
                } // ends while
                arrayColumnCount++;

                // Handles the mathematics of the uncertainty column
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
                            // Retrieves info necessary to do calculations and fill table
                            float oneSigma = valueModel.getOneSigma();
                            float initialValue = valueModel.getValue();
                            String currentUnit = column.getValue().getUnits();
                            int uncertaintyCountOfSignificantDigits = column
                                    .getValue().getUncertaintyColumn()
                                    .getCountOfSignificantDigits();

                            // Performs the mathematical operations for the table
                            if (Numbers.getUnitConversionsMap().containsKey(
                                    currentUnit)) {
                                Integer dividingNumber = Numbers
                                        .getUnitConversionsMap().get(
                                                currentUnit);

                                // Formats Uncertainty correctly
                                SignificantFigures valueToRound = new SignificantFigures("0.0");
                                if (column.getValue().getUncertaintyType().equals("ABS")) {     // Calculates value if column is ABS uncertainty
                                    valueToRound = new SignificantFigures(
                                            (oneSigma / (Math.pow(10,
                                                    dividingNumber))) * 2);
                                                System.out.println("ABS:" + valueToRound);
                                                System.out.println("%: " + (oneSigma / initialValue) * 200);
                                                System.out.println("------------------------------------------");

                                } else if (column.getValue().getUncertaintyType().equals("PCT")) {     // Calculates value if column is PCT uncertainty
                                    valueToRound = new SignificantFigures(
                                            (oneSigma / initialValue) * 200);
                                }

                                // Puts value of uncertainty column in cell
                                SignificantFigures newUncertaintyValue = valueToRound.setNumberSignificantFigures(uncertaintyCountOfSignificantDigits);
                                if (String.valueOf(newUncertaintyValue).contains("E")) {
                                    toCorrectDigits(String.valueOf(newUncertaintyValue));
                                }
                                fractionArray[arrayRowCount][arrayColumnCount] = String
                                        .valueOf(newUncertaintyValue);

                                // goes back and formats parent cell value to match uncertainty
                                String oldParentValue = fractionArray[arrayRowCount][arrayColumnCount - 1];
//                                if(column.getValue().getUncertaintyType().equals("PCT")){
//                                    BigDecimal parent = new BigDecimal(oldParentValue);
//                                    BigDecimal percentUncertainty = new BigDecimal(String.valueOf(newUncertaintyValue));
//                                    BigDecimal absoluteUncertaintyValue = parent.multiply(percentUncertainty);
//                                    SignificantFigures absValue = new SignificantFigures(String.valueOf(absoluteUncertaintyValue)).setNumberSignificantFigures(uncertaintyCountOfSignificantDigits);
//                                    System.out.println("Parent: " + parent);
//                                    System.out.println("%: " + percentUncertainty);
//                                    System.out.println("ABS/FINAL: " + absValue);
//                                    System.out.println("---------------------------------");
//                                    String newParentValue = formatShape(String.valueOf(absValue), oldParentValue);
//                                    fractionArray[arrayRowCount][arrayColumnCount - 1] = String.valueOf(newParentValue);
//                                }else {
                                    if (String.valueOf(newUncertaintyValue).contains(".")) {
                                        String parentFractional = formatShape(String.valueOf(newUncertaintyValue), fractionArray[arrayRowCount][arrayColumnCount - 1]);
                                        fractionArray[arrayRowCount][arrayColumnCount - 1] = parentFractional;
                                    } else {
                                        int uncertaintySigFigCount = newUncertaintyValue.getNumberSignificantFigures();
                                        SignificantFigures newParentValue = new SignificantFigures(oldParentValue).setNumberSignificantFigures(uncertaintySigFigCount);
                                        fractionArray[arrayRowCount][arrayColumnCount - 1] = String.valueOf(newParentValue);
                                    }
//                                }
                                // Test sigfig method
//                                boolean isArbitraryMode = column.getValue().isDisplayedWithArbitraryDigitCount(); // determines if parent column is in arbitrary mode
//                                roundUncertaintyCellValue(isArbitraryMode, String.valueOf(valueToBeRounded), uncertaintyCountOfSignificantDigits);

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
 *
 */
    public static String formatShape(String uncertaintyValue, String parentValue){
        String newParentValue = "";
        int lengthOfFraction = 0;
//        System.out.println("Original: " + parentValue);

        if(uncertaintyValue.contains(".")){
            String[] uncertaintyParts = uncertaintyValue.split("\\.");
            lengthOfFraction = uncertaintyParts[1].length();
//            System.out.println("Digits: " + lengthOfFraction);
            String[] parentParts = parentValue.split("\\.");
            String formattedParentFraction = parentParts[1].substring(0, lengthOfFraction);
            newParentValue = parentParts[0] + "." + formattedParentFraction;
//            System.out.println("Unct: " + uncertaintyValue);
//            System.out.println("New: " + newParentValue);
        }else{
            lengthOfFraction = uncertaintyValue.length();

        }


        System.out.println("-----------------------------------------------------------------------");

        return newParentValue;
    }

    /*
    Correctly formats string
    */
    public static String toCorrectDigits(String value){
        System.out.println("Value is " + value);
        String res = new BigDecimal(value).toPlainString();
        return res;
    }

    /*
    Creates the appropriate rounded version of a number for a parent cell
    @param currentMode: bothSigFig, parentArbitrary, parentSigFig, uncertaintyArbitrary, uncertaintySigFig
    @param value: the value in the cell to be rounded
    @param parentSigFigCount: the parent sigfig value for rounding the current number
    @param uncertaintySigFigCount: the uncertainty sigfig value for rounding the current number
     */
    public static String roundParentCellValue(String currentMode, String value, int parentSigFigCount, int uncertaintySigFigCount) {
        String roundedValue = "";
        Log.e("Rounding", "PARENT");
        Log.e("Rounding", "Original: " + value);


        if (currentMode.contentEquals("parentSigFig")) {
            // Sends entire number in for rounding based on total number of sigfigs as specified in the parent column
            Log.e("Rounding", "Parent(Whole) Sig Figs: " + parentSigFigCount);
            if (parentSigFigCount != 0) {
                if (value.contains("-")) {
                    roundedValue = value.substring(0, parentSigFigCount + 2); // adds two to account for decimal place and negative sign
                } else {
                    roundedValue = value.substring(0, parentSigFigCount + 1); // adds one to account for decimal place
                }
            } else {
                roundedValue = value;
            }
//            countSignificantFigures(value, parentSigFigCount);
        } else {
            // Splits number in two parts to access fractional half
            String[] valueParts = value.split("\\."); //splits off fractional part from parent value
            String wholeNumber = valueParts[0];
            String fractional = valueParts[1];

            if (currentMode.contentEquals("bothSigFig")) {
                //sends fractional portion and uncertainty sig fig count to display based on uncertainty sigfig
                Log.e("Rounding", "Uncertainty Sig Figs: " + uncertaintySigFigCount);
                if (uncertaintySigFigCount != 0) {
                    roundedValue = wholeNumber + "." + fractional.substring(0, uncertaintySigFigCount);
//                    countSignificantFigures(fractional, uncertaintySigFigCount);
                } else {
                    roundedValue = value;
                }

            } else if (currentMode.contentEquals("parentArbitrary")) {
                //sends fractional portion and parent sig fig count to display based on parent sigfig
                Log.e("Rounding", "Parent Sig Figs: " + parentSigFigCount);
                if (parentSigFigCount != 0) {
                    roundedValue = wholeNumber + "." + fractional.substring(0, parentSigFigCount);
//                    countSignificantFigures(fractional, parentSigFigCount);
                } else {
                    roundedValue = value;
                }

            }
        }
        Log.e("Rounding", "Rounded: " + roundedValue);
        Log.e("Rounding", "-------------------------------------------------------------------------------------");

        return roundedValue;
    }

    /*
 Creates the appropriate rounded version of a number for an uncertainty cell
 @param currentMode: bothSigFig, parentArbitrary, parentSigFig, uncertaintyArbitrary, uncertaintySigFig
 @param value: the value in the cell to be rounded
 @param uncertaintySigFigCount: the uncertainty sigfig value for rounding the current number
  */
    public static String roundUncertaintyCellValue(boolean isArbitrary, String value, int uncertaintySigFigCount) {
        String roundedValue = "";

        Log.e("Rounding", "UNCERTAINTY");
        Log.e("Rounding", "Original: " + value);

        NumberFormat formatter = new DecimalFormat();

        // Splits number in two parts to access fractional half
        String[] valueParts = value.split("\\."); //splits off fractional part from parent value
        String wholeNumber = valueParts[0];
        String fractional = valueParts[1];

        if (isArbitrary) { //ARBITRARY MODE
            //sends fractional portion and parent sig fig count to display based on uncertainty sigfig
            Log.e("Rounding", "Arb Sig Figs: " + uncertaintySigFigCount);
            roundedValue = wholeNumber + "." + fractional.substring(0, uncertaintySigFigCount);
//            countSignificantFigures(fractional, uncertaintySigFigCount);

        } else { // SIGFIG MODE
            //sends fractional portion and uncertainty sig fig count to display based on uncertainty sigfig
            Log.e("Rounding", "SIGFIG (Whole) Sig Figs: " + uncertaintySigFigCount);
            if (value.contains("-")) {
                roundedValue = value.substring(0, uncertaintySigFigCount + 2); //adds two to account for decimal place and negative
            } else {
                roundedValue = value.substring(0, uncertaintySigFigCount + 1); //adds one to account for decimal place
            }
//            countSignificantFigures(value, uncertaintySigFigCount);
        }

        // handles cases with 0 sigfig counts
        if (uncertaintySigFigCount == 0) {
            roundedValue = value;
        }

        Log.e("Rounding", "Rounded: " + roundedValue);
        Log.e("Rounding", "-------------------------------------------------------------------------------------");

        return roundedValue;
    }

    /*
Will be able to count the number of sigfigs in a float
@param value: value to be examined for number of sigfigs
@param numOfSigFigs: number of sigfigs to adjust string to
@return correctValue: value with appropriate number of sigfigs
*/
    public static void countSignificantFigures(String value, int numOfSigFigs) {
        Log.e("Rounding", "Original: " + value);
        // Practice with regexes
        String[] sig_figs = value.split("(^0+(\\.?)0*" + // 1 or more leading zeros followed by decimal and more zeros
                "|(~\\.)0+$" + // take off ending zeros if not before decimal
                "|\\.)"); // splits whatever is left by decimal
        int sum = 0;
        for(String fig:sig_figs){
            sum += fig.length();
        }

        Log.e("Rounding", "FinalSigFigCt: " + sum);
        Log.e("Rounding", "------------------------------------------------------------------------------------------");
    }

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
                        Uri.parse("http://chronihelpblog.wordpress.com"));
                startActivity(openHelpBlog);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public String getAliquotFilePath() {
        return aliquotFilePath;
    }

    public void setAliquotFilePath(String aliquotFilePath) {
        this.aliquotFilePath = aliquotFilePath;
    }

    public String getReportSettingsFilePath() {
        return reportSettingsFilePath;
    }

    public void setReportSettingsFilePath(String reportSettingsFilePath) {
        this.reportSettingsFilePath = reportSettingsFilePath;
    }
}
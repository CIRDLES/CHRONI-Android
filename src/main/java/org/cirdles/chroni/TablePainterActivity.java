package org.cirdles.chroni;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DateFormat;
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
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;


/**
 * This class creates the display table after parsing the aliquot and report settings files
 */
public class TablePainterActivity extends Activity {

    private static final int CHANGE_REPORT_SETTINGS = 1;
    private static final int CHANGE_ALIQUOT = 2;

    private static TreeMap<Integer, Category> categoryMap; // map returned from parsing Report Settings
    private static TreeMap<String, Fraction> fractionMap; // map returned from parsing Aliquot
    private static TreeMap<String, Image> imageMap; // map of image data returned from parsing Aliquot
    private static String[][] finalArray = {}; // the completely parsed array for displaying
    private static ArrayList<String> outputVariableNames; // output variable names for column work
    private static ArrayList<Integer> columnMaxLengths; // holds the max lengths for each column (used for padding)
    private static ArrayList<Boolean> columnDecimals;   // holds whether the current column contains a decimal or not

    // stores the table views so that they can be quickly updated if the screen is turned
    private static TableLayout categoryNameTable;
    private static TableLayout headerInformationTable;
    private static TableLayout aliquotDataTable;

    private static String previousAliquotFilePath = ""; // stores name of previous aliquot opened

    private String aliquotFilePath, reportSettingsFilePath; // The complete path of the aliquot and report settings files to be parsed and display

    private static final String PREF_REPORT_SETTINGS = "Current Report Settings";// Path of the current report settings file
    private static final String PREF_ALIQUOT = "Current Aliquot";// Path of the current report settings file

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_Holo);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        setContentView(R.layout.display);

        // if a new aliquot is trying to be opened, parse for data
        if (!retrieveAliquotFilePath().equals(previousAliquotFilePath)) {
            // resets tables to null
            categoryNameTable = null;
            headerInformationTable = null;
            aliquotDataTable = null;

            createData();
            createView();
        }
        // otherwise, just fill in previously parsed data
        else
            createView();

    }

    /**
     * Fills in all necessary maps, lists, etc. needed for creating the table view.
     *
     * This method has no input or output, but uses data already known in the application.
     */
    private void createData() {

        // Instantiates the Report Settings Parser and gets the current Report Settings path
        ReportSettingsParser reportSettingsParser = new ReportSettingsParser();
        setReportSettingsFilePath(retrieveReportSettingsFilePath());

        // Parses the Report Settings XML file
        categoryMap = (TreeMap<Integer, Category>) reportSettingsParser.runReportSettingsParser(getReportSettingsFilePath());
        outputVariableNames = reportSettingsParser.getOutputVariableName();

        columnMaxLengths = new ArrayList<Integer>();
        columnDecimals = new ArrayList<Boolean>();

        // gets and sets the current Aliquot path
        String aliquotPath = retrieveAliquotFilePath();
        System.out.println("@@@@ PATH: " + aliquotPath);
        setAliquotFilePath(aliquotPath);
        previousAliquotFilePath = aliquotPath;  // saves path for later

        // Parses aliquot file and retrieves maps
        MapTuple maps = AliquotParser.runAliquotParser(getAliquotFilePath());
        fractionMap = (TreeMap<String, Fraction>) maps.getFractionMap();
        imageMap = (TreeMap<String, Image>) maps.getImageMap();

        final String aliquot = AliquotParser.getAliquotName();

        // Fills the Report Setting and Aliquot arrays
        String[][] reportSettingsArray = fillReportSettingsArray(
                outputVariableNames, categoryMap);
        String[][] fractionArray = fillFractionArray(outputVariableNames,
                categoryMap, fractionMap, aliquot);

        //Sorts the table array
        Arrays.sort(fractionArray, new Comparator<String[]>() {
            @Override
            public int compare(final String[] entry1, final String[] entry2) {

                final String field1 = entry1[0].trim();
                final String field2 = entry2[0].trim();

                Comparator<String> forNoah = new IntuitiveStringComparator<String>();
                return forNoah.compare(field1, field2);
            }
        });

        // Creates the final table array for displaying
        finalArray = fillArray(outputVariableNames, reportSettingsArray, fractionArray);

        // Creates database entry from current entry
        CHRONIDatabaseHelper entryHelper = new CHRONIDatabaseHelper(this);
        entryHelper.createEntry(getCurrentTime(), getAliquotFilePath(), getReportSettingsFilePath());
        Toast.makeText(TablePainterActivity.this, "Your current table info has been stored!", Toast.LENGTH_LONG).show();

    }

    /**
     * Creates the actual view itself, by using the maps, lists, etc. that have either been
     * created or previously stored.
     *
     * This method has no input or output, only draws the table to the screen.
     */
    private void createView() {

        // Adds a button with the current report settings files
        Button reportSettingsCell = (Button) findViewById(R.id.reportSettingsTableButton);
        String reportSettingsText = "Report Settings: " + splitFileName(retrieveReportSettingsFilePath());
        reportSettingsCell.setText(reportSettingsText);
        reportSettingsCell.setBackgroundColor(getResources().getColor(R.color.button_blue));
        reportSettingsCell.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent openReportSettingsMenu = new Intent("android.intent.action.REPORTSETTINGSMENU");
                openReportSettingsMenu.putExtra("From_Table", "true");
                startActivityForResult(openReportSettingsMenu, CHANGE_REPORT_SETTINGS);
            }
        });

        // Adds a label button with the current aliquot files
        Button aliquotCell = (Button) findViewById(R.id.aliquotTableButton);
        String aliquotCellText = "Aliquot: " + splitFileName(retrieveAliquotFilePath());
        aliquotCell.setText(aliquotCellText);
        aliquotCell.setBackgroundResource(R.drawable.background_blue_background);
        aliquotCell.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent openAliquotMenu = new Intent("android.intent.action.ALIQUOTMENU");
                openAliquotMenu.putExtra("From_Table", "true");
                // starts the activity and waits for result to be returned
                startActivityForResult(openAliquotMenu, CHANGE_ALIQUOT);
            }
        });


        // Adds button to view a concordia image
        if ((imageMap.containsKey("concordia"))) {
            Button viewConcordiaButton = (Button) findViewById(R.id.concordiaTableButton);
            viewConcordiaButton.setBackgroundColor(getResources().getColor(R.color.light_grey));

            viewConcordiaButton.setVisibility(View.VISIBLE);
            viewConcordiaButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Checks internet connection before getting images
                    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo mobileWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                    if (mobileWifi.isConnected()) {
                        // Displays concordia images
                        Toast.makeText(TablePainterActivity.this, "Opening Concordia Image...", Toast.LENGTH_LONG).show();
                        Intent viewConcordiaIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(imageMap.get("concordia").getImageURL()));
                        startActivity(viewConcordiaIntent);
                    } else {
                        //Handles lack of wifi connection
                        Toast.makeText(TablePainterActivity.this, "Please check your internet connection to view this image.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        // Adds button to view a probability density image
        if ((imageMap.containsKey("probability_density"))) {
            Button viewProbabilityDensityButton = (Button) findViewById(R.id.probabilityDensityTableButton);
            // Decides what color button should be based on if there is a concordia button
            if ((imageMap.containsKey("concordia"))) {
                viewProbabilityDensityButton.setBackgroundColor(getResources().getColor(R.color.button_blue));
                viewProbabilityDensityButton.setTextColor(Color.WHITE);
            }else{
                viewProbabilityDensityButton.setBackgroundColor(getResources().getColor(R.color.light_grey));
                viewProbabilityDensityButton.setTextColor(Color.BLACK);
            }

            viewProbabilityDensityButton.setVisibility(View.VISIBLE);
            //Adds on click functionality
            viewProbabilityDensityButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Checks internet connection before getting images
                    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo mobileWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                    if (mobileWifi.isConnected()) {
                        Toast.makeText(TablePainterActivity.this, "Opening Probability Density Image...", Toast.LENGTH_LONG).show();
                        Intent viewProbabilityDensityIntent = new Intent(Intent.ACTION_VIEW, Uri.parse( imageMap.get("probability_density").getImageURL()));
                        startActivity(viewProbabilityDensityIntent);
                    } else {
                        //Handles lack of wifi connection
                        Toast.makeText(TablePainterActivity.this, "Please check your internet connection to view this image.", Toast.LENGTH_LONG).show();
                    }

                }
            });
        }


        // creates all three tables if first time displaying
        if (categoryNameTable == null || headerInformationTable == null || aliquotDataTable == null) {

            // Header table specifically for category names
            categoryNameTable = (TableLayout) findViewById(R.id.categoryNameTable);

            // Report Settings header table for display names 1-3
            headerInformationTable = (TableLayout) findViewById(R.id.tableForHeader);

            // the aliquot specific info contained here
            aliquotDataTable = (TableLayout) findViewById(R.id.finalTable);

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
            //TODO: Figure out more elegant way to calculate the width of a column

            int categoryCount = 0;
            // Adds just enough cells for every category name
            for (Entry<Integer, Category> category : categoryMap.entrySet()) {
                if (category.getValue().getColumnCount() != 0) { // removes any invisible columns that may be in map
                    TextView categoryCell = new TextView(this);
                    categoryCell.setText(category.getValue().getDisplayName());
                    categoryCell.setTypeface(Typeface.MONOSPACE);
                    categoryCell.setHorizontallyScrolling(true);    // make sure that the text does not wrap
                    if (category.getValue().getDisplayName().contentEquals("Fraction") && categoryCount != 0) { // Easy fix to handle the issue of sizing with last fraction category TODO: Make better!
                        // Simply sets same size as fraction because its always the same length with last column
                        categoryCell.setMinEms(columnSizes[columnSizes.length - 1] + 2);
                        categoryCell.setMaxEms(columnSizes[columnSizes.length - 1] + 2);
                    } else {
                        // sets column spacing based on max character count and allows extra space for crowding
                        categoryCell.setMinEms(headerCellSizes[categoryCount] + (2 * category.getValue().getColumnCount()));
                        categoryCell.setMaxEms(headerCellSizes[categoryCount] + (2 * category.getValue().getColumnCount()));

                    }
                    categoryCell.setPadding(3, 4, 3, 4);
                    categoryCell.setTextSize((float) 14.5);
                    categoryCell.setTextColor(Color.BLACK);
                    categoryCell.setBackgroundResource(R.drawable.background_blue_background);
                    categoryCell.setGravity(Gravity.START);
                    categoryRow.addView(categoryCell); // Adds cell to row
                }
                categoryCount++;

            }


            // TODO: Fix the spacing of the title rows
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

                    // sets column spacing based on max character count and allows extra space for crowding
                    cell.setMinEms(columnSizes[currentColumn] + 2);
                    cell.setMaxEms(columnSizes[currentColumn] + 2);
                    cell.setPadding(3, 4, 3, 4);
                    cell.setTextColor(Color.BLACK);
                    cell.setTextSize((float) 14.5);
                    cell.setGravity(Gravity.END);

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
                    if (currentRow > 4) {   // Handles all of the data cells
                        // Format the text so that it has the proper padding
                        String text = finalArray[currentRow][currentColumn];
                        int length = columnMaxLengths.get(currentColumn);
                        boolean hasDecimal = columnDecimals.get(currentColumn);
                        String paddedText = setTextPadding(text, length, hasDecimal);
                        cell.setText(paddedText);

                    } else { // Handles all of the header rows and aliquot name rows
                        cell.setTypeface(Typeface.DEFAULT_BOLD);
                        cell.setTypeface(Typeface.MONOSPACE);
                        cell.setGravity(Gravity.CENTER);

                        String text = finalArray[currentRow][currentColumn];
                        cell.setText(text);
                    }

                    cell.setVisibility(View.VISIBLE);

                    if (cell.getText().equals("-")) {
                        cell.setGravity(Gravity.CENTER);
                    }

                    // left justify fraction column
                    if (isFractionColumn(finalArray, currentColumn)) {
                        cell.setGravity(Gravity.START);
                    }

                    // append an individual cell to a content row
                    row.addView(cell);
                }
            }
        }

        // otherwise it removes the views from the previous parents (landscape or portrait)
        // and re-paints them onto the new view
        else {

            // removes tables from previous parents
            ScrollView aliquotParent = (ScrollView) aliquotDataTable.getParent();
            aliquotParent.removeView(aliquotDataTable);
            LinearLayout overallParent = (LinearLayout) categoryNameTable.getParent();
            overallParent.removeView(categoryNameTable);
            overallParent.removeView(headerInformationTable);

            // this is the scrollView where the actual data is displayed
            ScrollView finalTableLayout = (ScrollView) findViewById(R.id.scrollPane);
            finalTableLayout.removeView(findViewById(R.id.finalTable));
            finalTableLayout.addView(aliquotDataTable);

            // removes the standard xml versions of the tables
            LinearLayout overallLayout = (LinearLayout) findViewById(R.id.displayTableLayout);
            overallLayout.removeView(findViewById(R.id.scrollPane));
            overallLayout.removeView(findViewById(R.id.categoryNameTable));
            overallLayout.removeView(findViewById(R.id.tableForHeader));

            //adds the saved versions of the tables
            overallLayout.addView(categoryNameTable);
            overallLayout.addView(headerInformationTable);
            overallLayout.addView(finalTableLayout);

        }

    }

    /**
     * This method takes in a value string (text). It is also given the maximum length for a certain column along with
     * whether that column contains a decimal or not. It then pads the value accordingly to present in the table.
     * Is used when building the actual table in onCreate().
     */
    public String setTextPadding(String text, int maxLength, boolean hasDecimal) {
        String paddedText = text;

        if (hasDecimal) {   // if the column contains a decimal
            String[] decimalSplit = text.split("\\.");
            if (decimalSplit.length > 1) { // if the text has a decimal
                for (int i = 0; i < maxLength-decimalSplit[1].length(); i++) {
                    if (decimalSplit[1].length() < maxLength) {   // if the value after the decimal is less than the max length
                        paddedText += " ";
                    }
                }

            } else {    // if the text does not have a decimal
                for (int i = 0; i < maxLength + 1; i++) {     // add spaces for every digit after the decimal including the decimal itself
                    paddedText += " ";
                }
            }

        } else {    // if the column does not have a decimal
            for (int i = 0; i < (maxLength - text.length()) + 1; i++) {     // account fo the decimal itself
                paddedText += " ";
            }
        }

        return paddedText;
    }

    /**
     * Accesses current report settings file
     */
    private String retrieveReportSettingsFilePath() {
        SharedPreferences settings = getSharedPreferences(PREF_REPORT_SETTINGS, 0);

        // Gets current RS and if no file there, returns default as the current file
        return settings.getString("Current Report Settings", Environment.getExternalStorageDirectory()
                + "/CHRONI/Report Settings/Default Report Settings.xml");
    }

    /**
     * Accesses current report settings file
     */
    private String retrieveAliquotFilePath() {
        SharedPreferences settings = getSharedPreferences(PREF_ALIQUOT, 0);
        return settings.getString("Current Aliquot", "Error"); // Gets current RS and if no file there, returns default as the current file
    }

    /**
     * Splits report settings file name returning a displayable version without the entire path
     */
    private String splitFileName(String fileName){
        String[] fileNameParts = fileName.split("/");
        return fileNameParts[fileNameParts.length-1];
    }

    /**
     * Figures out if something is a part of the Fraction column.
     */
    private boolean isFractionColumn(String[][] displayArray, int columnIndex) {
        boolean isFractionColumn = false;
        String categoryName = displayArray[0][columnIndex];

        if (categoryName.contentEquals("Fraction")) {
            isFractionColumn = true;
        }

        return isFractionColumn;
    }

    /**
     * Goes through and figures out header cell lengths given a table
     */
    protected int[] distributeHeaderCells(int[] columnWidths){
        int[] headerMaxCharacterCounts = new int[categoryMap.size()];
        int currentCategoryCount = 0;
        int currentColumnCount = 0;

        for (Entry<Integer, Category> category : categoryMap.entrySet()) {
            int categoryCellWidth = 0;
            if(category.getValue().getColumnCount() != 0) { // removes any invisible columns that may be in map

                for (Entry<Integer, Column> column : category.getValue().getCategoryColumnMap().entrySet()) {
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

    /**
     * Goes through and figures out columns lengths given a table
     */
    protected int[] distributeTableColumns(String[][] finalArray, int ROWS, int COLS){
        int[] columnMaxCharacterCounts = new int[COLS];
        for (int currentColumn = 0; currentColumn < COLS; currentColumn++) {
            int widestCellCharacterCount = 0;
            int currentCellCharacterCount;
            for (int currentRow = 0; currentRow < ROWS; currentRow++) {
                if(currentRow != 0) { // Skips counting the header row
                    currentCellCharacterCount = finalArray[currentRow][currentColumn].length();

                    if (currentCellCharacterCount > widestCellCharacterCount) {
                        widestCellCharacterCount = currentCellCharacterCount;
                    }
                }
            }
            // Divides by 2 for appropriate EMS measurement
            columnMaxCharacterCounts[currentColumn] = widestCellCharacterCount/2;
        }
        return columnMaxCharacterCounts;
    }


    /**
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
        final int ROWS = 4; // 4 is the number of rows for specific information in the array

        String[][] reportSettingsArray = new String[ROWS][COLUMNS];
        int totalColumnCount = 0; // the current column number for the array

        // steps through each category in the map
        for (Entry<Integer, Category> category : categoryMap.entrySet()) {
            int currentCategoryColumnCount = 1; // the number of columns under each category

            // steps through each column in the category
            for (Entry<Integer, Column> column : category.getValue().getCategoryColumnMap().entrySet()) {

                int currentRowNum = 0; // the current row number for the array
                Column uncertaintyColumn = column.getValue().getUncertaintyColumn();    // Get Uncertainty Column for later use

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

                totalColumnCount++;
                currentCategoryColumnCount++;

                // Fills in uncertainty column information
                if (uncertaintyColumn != null) {
                    currentRowNum = 0;
                    reportSettingsArray[currentRowNum][totalColumnCount] = ""; // Initialized as blank because no uncertainty display name will ever be on the first row
                    currentRowNum++;

                    // puts the displayNames in the array
                    reportSettingsArray[currentRowNum][totalColumnCount] = uncertaintyColumn.getDisplayName1();
                    currentRowNum++;

                    // Puts display names in the header array and handles formatting of special characters
                    reportSettingsArray[currentRowNum][totalColumnCount] = uncertaintyColumn.getDisplayName2();
                    if (reportSettingsArray[currentRowNum][totalColumnCount]
                            .equals("PLUSMINUS2SIGMA")) {
                        reportSettingsArray[currentRowNum][totalColumnCount] = "\u00B12\u03C3";
                    }
                    currentRowNum++;

                    reportSettingsArray[currentRowNum][totalColumnCount] = uncertaintyColumn.getDisplayName3();
                    if (reportSettingsArray[currentRowNum][totalColumnCount]
                            .equals("PLUSMINUS2SIGMA%")) {
                        reportSettingsArray[currentRowNum][totalColumnCount] = "\u00B12\u03C3%";
                    }

                    totalColumnCount++;
                    currentCategoryColumnCount++;
                }
            }
        }

        return reportSettingsArray;
    }

    /**
     * Fills the Aliquot portion of the array
     *
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
        final int COLUMNS = outputVariableName.size();
        final int ROWS = fractionMap.size();
        String[][] fractionArray = new String[ROWS][COLUMNS];
        int arrayColumnCount = 0; // the current column number for the array

        // Fills in the rows reserved for the aliquot name
        fractionArray[0][0] = aliquotName;
        for (int j = 1; j < COLUMNS; j++) {
            fractionArray[0][j] = " ";
            columnMaxLengths.add(0);   // add a spot to columnLengths for each column in the table
            columnDecimals.add(false);  // add a spot to columnDecimals for each column as well
        }
        columnMaxLengths.add(0);    // add one more spot to get the correct number of columns (since j starts at 1 in the for loop)
        columnDecimals.add(false);

        // steps through each category in the map
        for (Entry<Integer, Category> category : categoryMap.entrySet()) {

            // steps through each column in the category
            for (Entry<Integer, Column> column : category.getValue().getCategoryColumnMap().entrySet()) {

                String variableName = column.getValue().getVariableName();
                String methodName = column.getValue().getMethodName();

                // Going to iterate through all the fractions and uncertainties (IF they exist) for every column
                Iterator<Entry<String, Fraction>> fractionIterator = fractionMap
                        .entrySet().iterator();
                int arrayRowCount = 0; // the current row number for the array;
                Column uncertaintyColumn = column.getValue().getUncertaintyColumn();    // Get Uncertainty Column for later use

                boolean uncertaintyColumnExists =  uncertaintyColumn != null; // See if there is an uncertainty column

                while (fractionIterator.hasNext()) {
                    Entry<String, Fraction> currentFraction = fractionIterator.next();

                    boolean firstDecimal = false;   // knows whether there has already been a decimal in the column

                    String uncertaintyValue = ""; // will contain the uncertainty VALUE to be used in obtaining the fraction shape later
                    String uncertaintyType = "";  // will contain the uncertainty TYPE to be used in obtaining the fraction shape later

                    // Fills in the UNCERTAINTY COLUMN (column on the right) if it exists
                    if (uncertaintyColumnExists) {
                        arrayColumnCount++;     // If uncertainty exists, go to the next column

                        String uncertaintyVariableName = uncertaintyColumn.getVariableName();

                        ValueModel uncertaintyValueModel = DomParser.getValueModelByName(
                                currentFraction.getValue(), uncertaintyVariableName);
                        if (uncertaintyValueModel != null) {
                            // Retrieves info necessary to do calculations and fill table
                            float oneSigma = uncertaintyValueModel.getOneSigma();
                            float initialValue = uncertaintyValueModel.getValue();
                            String currentUnit = column.getValue().getUnits();
                            int uncertaintyCountOfSignificantDigits = uncertaintyColumn
                                    .getCountOfSignificantDigits();

                            // Performs the mathematical operations for the table
                            if (Numbers.getUnitConversionsMap().containsKey(
                                    currentUnit)) {
                                Integer dividingNumber = Numbers
                                        .getUnitConversionsMap().get(
                                                currentUnit);
                                valueToBeRounded = new BigDecimal(
                                        (oneSigma / (Math.pow(10,
                                                dividingNumber))) * 2);

                                // Calculates value if column is percent uncertainty
                                uncertaintyType = column.getValue().getUncertaintyType();
                                if (uncertaintyType.equals("PCT")) {
                                    valueToBeRounded = new BigDecimal((oneSigma / initialValue) * 200);
                                }

                                // Rounds the uncertainty value based on the report settings
                                String newValue;    // instantiate value to end up in uncertainty place
                                if (uncertaintyColumn.isDisplayedWithArbitraryDigitCount()) {
                                    // when the uncertainty is ARBITRARY
                                    roundedValue = valueToBeRounded.setScale(
                                            uncertaintyCountOfSignificantDigits,
                                            BigDecimal.ROUND_HALF_UP); // performs rounding

                                    newValue = roundedValue.toPlainString();

                                } else {
                                    // when the uncertainty is in SIG FIG format
                                    newValue = toSignificantFiguresUncertaintyString(valueToBeRounded, uncertaintyCountOfSignificantDigits); // Rounds the uncertainty value appropriately
                                }

                                uncertaintyValue = newValue;    // save for later use
                                fractionArray[arrayRowCount][arrayColumnCount] = newValue; // places final value in array

                                // checks if the value is larger than other previous values
                                int valueLength;
                                String[] splitList = newValue.split("\\."); // splits the value to check decimal place
                                if (splitList.length > 1) { // if there is a decimal, length is the length after the decimal
                                    valueLength = splitList[1].length();
                                    if (!columnDecimals.get(arrayColumnCount)) {    // if there hasn't already been a decimal in column
                                        firstDecimal =  true;   // this is the first decimal
                                    }
                                    columnDecimals.set(arrayColumnCount, true); // adds a true to the column because it contains a decimal

                                } else {    // if not, the length is the overall length
                                    valueLength = splitList[0].length();
                                }

                                // puts the length value for the column into columnMaxLengths
                                if (valueLength > columnMaxLengths.get(arrayColumnCount)
                                        || firstDecimal) {
                                    // if the value is greater than current max length or it is the first decimal
                                    if (columnDecimals.get(arrayColumnCount) && (splitList.length > 1)) {
                                        // if the column is a decimal and the current value is a decimal
                                        columnMaxLengths.set(arrayColumnCount, valueLength);
                                    }
                                }
                            }
                        }

                        else    // value model is null
                            fractionArray[arrayRowCount][arrayColumnCount] = "-";


                        arrayColumnCount--; // Goes back to the fraction column
                    }

                    //  Fills in the FRACTION COLUMN (column on the left)
                    if (variableName.equals("")) {
                        // Value Models under Fraction don't have variable names so have to account for those specifically
                        if(methodName.equals("getFractionID")) {
                            fractionArray[arrayRowCount][arrayColumnCount] = currentFraction.getValue().getFractionID();
                        } else if(methodName.equals("getNumberOfGrains")) {
                            fractionArray[arrayRowCount][arrayColumnCount] = currentFraction.getValue().getNumberOfGrains();
                        }

                    } else {
                        // Retrieves the correct value model based off the variable name
                        ValueModel valueModel = DomParser.getValueModelByName(
                                currentFraction.getValue(), variableName);

                        if (valueModel != null) {
                            float initialValue = valueModel.getValue();
                            String currentUnit = column.getValue().getUnits();

                            // Performs the mathematical operations for the table
                            if (Numbers.getUnitConversionsMap().containsKey(currentUnit)) {
                                Integer dividingNumber = Numbers.getUnitConversionsMap().get(currentUnit); // gets the exponent for conversion
                                valueToBeRounded = new BigDecimal(initialValue / (Math.pow(10, dividingNumber))); // does initial calculation

                                /**
                                 * Obtains the roundedValue for the current column, based on the
                                 * following found in the report settings:
                                 *
                                 * 1. if it is in arbitrary format
                                 * 2. if it is in sig fig format
                                 *      a) if the uncertainty column is also in sig fig format
                                 *      b) if the uncertainty column is in arbitrary format
                                 */
                                if (column.getValue().isDisplayedWithArbitraryDigitCount()) {
                                    // when the report settings specifies an ARBITRARY digit
                                    // count AFTER the decimal
                                    roundedValue = valueToBeRounded.setScale(
                                            column.getValue().getCountOfSignificantDigits(),
                                            BigDecimal.ROUND_HALF_UP); // performs rounding

                                } else {
                                    // current column is in SIG FIG format

                                    if (!uncertaintyType.equals("")) {  // UNCERTAINTY COLUMN EXISTS

                                        // if BOTH current column AND it's uncertainty column are in
                                        // SIG FIG format, use the getShape() method to find shape
                                        if (!column.getValue().getUncertaintyColumn().isDisplayedWithArbitraryDigitCount()) {
                                            roundedValue = valueToBeRounded.setScale(
                                                    getShape(uncertaintyValue, uncertaintyType, valueToBeRounded),
                                                    BigDecimal.ROUND_HALF_UP); // performs rounding

                                        } else {
                                            // if current column is in SIG FIG format but the
                                            // uncertainty column is in ARBITRARY format, the shape
                                            // is just the arbitrary number of digits
                                            roundedValue = valueToBeRounded.round(
                                                    new MathContext(column.getValue().getCountOfSignificantDigits(),
                                                            RoundingMode.HALF_UP));
                                        }

                                    } else {
                                        // there is no uncertainty column, so round up based on
                                        // the number of significant figures
                                        roundedValue = valueToBeRounded.round(
                                                new MathContext(column.getValue().getCountOfSignificantDigits(),
                                                        RoundingMode.HALF_UP));
                                    }
                                }

                                fractionArray[arrayRowCount][arrayColumnCount] = roundedValue.toPlainString(); // Places final value in array

                                // check if the value is larger than other previous values
                                int valueLength;
                                String[] splitList = roundedValue.toPlainString().split("\\."); // split the value to check decimal place

                                if (splitList.length > 1) { // if there is a decimal, length is the length after the decimal
                                    valueLength = splitList[1].length();
                                    if (!columnDecimals.get(arrayColumnCount)) {    // if there hasn't already been a decimal in column
                                        firstDecimal = true;   // this is the first decimal
                                    }
                                    columnDecimals.set(arrayColumnCount, true); // add a true to the column because it contains a decimal

                                } else {    // if not, the length is the overall length
                                    valueLength = splitList[0].length();
                                }

                                // put the length value for the column into columnMaxLengths
                                if (valueLength > columnMaxLengths.get(arrayColumnCount)
                                        || firstDecimal) {
                                    // if the value is greater than current max length or it is the first decimal
                                    if (columnDecimals.get(arrayColumnCount) && (splitList.length > 1)) {
                                        // if the column is a decimal and the current value is a decimal
                                        columnMaxLengths.set(arrayColumnCount, valueLength);
                                    }
                                }

                            }


                        }

                        else    // value model is null
                            fractionArray[arrayRowCount][arrayColumnCount] = "-";

                    }

                    arrayRowCount++;    // Next row down

                } // done looping through fraction

                //  Goes to the next column
                if (uncertaintyColumnExists) {
                    arrayColumnCount += 2;  // If uncertainty column exists, advance TWO columns to the right
                }
                else {
                    arrayColumnCount++;     // If not, only advance ONE column to the right
                }

            }

        }

        return fractionArray;
    }


    /**
     * Handles the rounding of values when there is an uncertainty type and value. (i.e. does not
     * account for Corr. Coef., % disc, etc.)
     *
     * @param roundedUncertaintyValue the value of the uncertainty column
     * @param uncertaintyType tells what type of uncertainty there is
     * @param fractionValue the overall number value needed to be shaped
     * @return the final shape as an integer
     */
    public static int getShape(String roundedUncertaintyValue, String uncertaintyType, BigDecimal fractionValue) {
        int shape;

        if (uncertaintyType.equals("PCT")) {
            // get what the uncertainty looks like: fractionValue * (roundedUncertaintyValue / 100)
            String modelUncertainty = fractionValue.multiply((BigDecimal.valueOf(Double.parseDouble(roundedUncertaintyValue)).
                            divide(BigDecimal.valueOf(100.0), 20, RoundingMode.HALF_UP))).toPlainString();
            int uncertaintySigFigs = 0;

            // splits so that the sig figs can be counted
            String[] sigFigSplit = roundedUncertaintyValue.split("(^0+(\\.?)0*|(~\\.)0+$|\\.)");
            for (String fig : sigFigSplit) {
                uncertaintySigFigs += fig.length();     // adds to the total number of sig figs
            }

            String roundedModelUncertainty = "";    // will hold the new, rounded modelUncertainty
            boolean finished = false;
            boolean hasSeenNonZero = false; // knows whether the loop has gone over a non-zero number
            int sigFigCount = 0;    // counts the number of sig figs entered
            int i = 0;

            while (!finished) {
                String sub = modelUncertainty.substring(i, i+1);

                if (hasSeenNonZero && !sub.equals(".")) {
                    sigFigCount++;  // add to sigFigCount for the next loop through
                }

                if (!hasSeenNonZero && !sub.equals("0") && !sub.equals(".")) { // if the number is the first non-zero/non-decimal
                    hasSeenNonZero = true;
                    roundedModelUncertainty += sub;
                    sigFigCount++;

                } else if ((!hasSeenNonZero && (sub.equals("0")) || sub.equals("."))) {    // if it is a leading 0
                    roundedModelUncertainty += sub;

                } else if (hasSeenNonZero && sigFigCount == uncertaintySigFigs) {
                    roundedModelUncertainty += sub; // no need to round perfectly since we only need the shape of the rounded value
                    finished = true;

                } else {
                    roundedModelUncertainty += sub;
                }
                i++;
            }

            if (!roundedModelUncertainty.contains(".")) {
                shape = 0;

            } else {    // if there is a decimal in the new model uncertainty value, then the shape is the length after that decimal
                String decimalString = roundedModelUncertainty.split("\\.")[1];
                shape = decimalString.length();
            }

        } else {
                if (!roundedUncertaintyValue.contains(".")) {
                    shape = 0;

                } else {    // if there is a decimal in the uncertainty value, then the shape is the length after that decimal
                    String decimalString = roundedUncertaintyValue.split("\\.")[1];
                    shape = decimalString.length();
                }
            }

        return shape;
    }

    public static String toSignificantFiguresUncertaintyString(BigDecimal originalNumber, int significantFigures) {
        String formattedNumber = String.format("%." + significantFigures + "G", originalNumber);

        // if the number is in Scientific Notation, converts to a decimal
        if (formattedNumber.contains("+")) {
            // splits the number into its parts
            double leftNumber = Double.parseDouble(formattedNumber.split("E")[0]);
            int digits = Integer.parseInt(formattedNumber.split("\\+")[1]);
            int newFormattedNumber = (int) (leftNumber * (Math.pow(10, digits)));
            formattedNumber = String.valueOf(newFormattedNumber);

        } else if (formattedNumber.contains("-")) {
            //splits the number into its parts
            double leftNumber = Double.parseDouble(formattedNumber.split("E")[0]);
            int digits = Integer.parseInt(formattedNumber.split("-")[1]);
            int newFormattedNumber = (int) (leftNumber / (Math.pow(digits, 10)));
            formattedNumber = String.valueOf(newFormattedNumber);
        }

        return formattedNumber;
    }

    /**
     * Fills the entire application array.
     */
    private static String[][] fillArray(ArrayList<String> outputVariableName,
                                        String[][] reportSettingsArray, String[][] fractionArray) {

        final int COLUMNS = outputVariableName.size();
        final int ROWS = 5 + fractionMap.size(); // 8 is the number of rows for
        // specific information in the array
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

    /**
     * This method gets the current time.
     */
    public String getCurrentTime(){
        // Formats like so: Mon dd, yyyy hh:mm PM/AM
        java.text.DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
        Date date = new Date();
        return dateFormat.format(date);
    }

    /**
     * Used when either new report settings or aliquot files are chosen via a new Intent.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHANGE_ALIQUOT &&
                resultCode == RESULT_OK && data.hasExtra("newAliquot")) {

            if (data.getStringExtra("newAliquot").equals("true")) {
                setContentView(R.layout.display);
                categoryNameTable = null;
                headerInformationTable = null;
                aliquotDataTable = null;
                setAliquotFilePath(retrieveAliquotFilePath());

                // remakes data and displays it
                createData();
                createView();
            }
        }

        else if (requestCode == CHANGE_REPORT_SETTINGS &&
                resultCode == RESULT_OK && data.hasExtra("newReportSettings")) {

            if (data.getStringExtra("newReportSettings").equals("true")) {
                setContentView(R.layout.display);
                categoryNameTable = null;
                headerInformationTable = null;
                aliquotDataTable = null;
                setReportSettingsFilePath(retrieveReportSettingsFilePath());

                // remakes data and displays it
                createData();
                createView();
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


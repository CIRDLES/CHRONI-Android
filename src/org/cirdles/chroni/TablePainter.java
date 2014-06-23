package org.cirdles.chroni;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TableRow.LayoutParams;

public class TablePainter extends Activity{
	
	private static String concordiaUrl, probabilityDensityUrl; // urls to neccessary images

	private static TreeMap<Integer, Category> categoryMap; // map returned from parsing Report Settings
	private static TreeMap<String, Fraction> fractionMap; // map returned from parsing Aliquot
	
	private static String[][] finalArray; // the completed array for displaying
	private static ArrayList<String> outputVariableName; // output variable names for column work

	@Override
	public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
////        setContentView(R.layout.new_table);
//		
//        // Instantiates the Report Settings Parser
//		ReportSettingsParser RSP = new ReportSettingsParser();
//		String reportSettingsPath = "/sdcard/CHRONI/Default Report Settings.xml";	// sets the default report settings xml
//		if(getIntent().getStringExtra("ReportSettingsXML") != null){
//			reportSettingsPath = getIntent().getStringExtra("ReportSettingsXML");	// gets the new location of the report settings XML
//		}
//		categoryMap = (TreeMap<Integer, Category>) RSP.runReportSettingsParser(reportSettingsPath);
//		ArrayList<String> outputVariableName = RSP.getOutputVariableName();
//
//		// Instantiates the Aliquot Parser
//		AliquotParser AP = new AliquotParser();
//		String aliquotPath = "";
//		if(getIntent().getStringExtra("AliquotXML") != null){
//			aliquotPath = getIntent().getStringExtra("AliquotXML");	// gets the new location of the aliquot XML
//		}
////		String aliquotPath = "/sdcard/Download/geochron_7767.xml";
//		fractionMap = (TreeMap<String, Fraction>) AP.runAliquotParser(aliquotPath);
//		String aliquot = AP.getAliquotName();
//		
//		// fills the arrays
//		String[][] reportSettingsArray = fillReportSettingsArray(outputVariableName, categoryMap);
//		String[][] fractionArray = fillFractionArray(outputVariableName,categoryMap, fractionMap, aliquot);
//		
//		// handles the array sorting
//        Arrays.sort( fractionArray, new Comparator<String[]>() {
//            @Override
//            public int compare ( final String[] entry1, final String[] entry2 ) {
//                int retVal = 0;
//
//                final String field1 = entry1[0].trim();
//                final String field2 = entry2[0].trim();
//
//                Comparator<String> forNoah = new IntuitiveStringComparator<String>();
//                	return retVal = forNoah.compare( field1, field2 );
//            }
//        } );

//		String[][] finalArray = fillArray(outputVariableName,reportSettingsArray, fractionArray);
		finalArray = getFinalArray();
//        TableLayout table = (TableLayout) findViewById(R.id.myTable);

//	    TextView aliquotName = new TextView(this);
	    ArrayList<String> contents = new ArrayList<String>();
		
	    HorizontalScrollView screenScroll = (HorizontalScrollView)findViewById(R.id.horizontalScrollView); // controls the horizontal scrolling of the entire table
	    LinearLayout tableLayout = (LinearLayout)findViewById(R.id.displayTableLayout); // gives inner table layout for displaying
		TableLayout headerInformationTable = (TableLayout)findViewById(R.id.tableForHeader); // Report Settings header table
		
		ScrollView scrollPane = (ScrollView)findViewById(R.id.scrollPane); // Vertical scrolling for the aliquot portion of table
		TableLayout aliquotDataTable = (TableLayout)findViewById(R.id.finalTable); // the aliquot specific information 
		
		//  calculates number of rows based on the size of the fraction five is added for the Report Settings rows
		final int ROWS = 5 + fractionMap.size();
	    int rowCount = 0;

		// Table Layout Printing
		for(int i = 0; i < ROWS; i++ ){
			
			TableRow row = new TableRow(this);
			
			// puts rows in appropriate place on layout 
			if (rowCount < 5) {
				// Report Settings and aliquot name rows
				headerInformationTable.addView(row);
			} else {
				// Adds aliquot rows to the aliquot scroll table
				aliquotDataTable.addView(row);
			}
					
			// loops through number of columns and adds text views to each row. this creates cells!
			for(int j = 0; j < outputVariableName.size(); j++){ 
				TextView cell = new TextView(this);
				cell.setWidth(125);
				cell.setPadding(3, 4, 3, 4);
				cell.setTextColor(Color.BLACK);
				cell.setTextSize((float)14.5);
				cell.setGravity(Gravity.RIGHT);
							
				if (rowCount < 5){
					// All aliquot data cells
					cell.setTypeface(Typeface.DEFAULT_BOLD);
					cell.setGravity(Gravity.CENTER);
				}
				
				if (rowCount == 0){
					cell.setGravity(Gravity.LEFT);
				}
				
				// sets appropriate background color for cells
				if(rowCount < 4){
					// colors odd header rows
					cell.setBackgroundColor(getResources().getColor(R.color.dark_grey));
					cell.setTextColor(Color.WHITE);
				}
				else if (rowCount > 4 && rowCount % 2 == 1) {
					// colors odd body rows
					cell.setBackgroundColor(getResources().getColor(R.color.light_grey));
				}else if(rowCount == 4){
					// aliquot name cell
					cell.setTextColor(Color.WHITE);
					cell.setBackgroundColor(getResources().getColor(R.color.dark_grey));
					cell.setTypeface(Typeface.DEFAULT_BOLD);
				}
				else {
					// header rows and all other body rows
					cell.setBackgroundColor(getResources().getColor(R.color.white));
				}
					
				//populates the first row with the associated category info corresponding to each column
				//sets duplicates to invisible if a specified category name is already presently being displayed
				if (rowCount == 0) {
					if (contents.size() != 0 && contents.contains(finalArray[i][j])) {
						cell.setText(finalArray[i][j]);
						cell.setTextColor(Color.TRANSPARENT);
					} else {
						//category name doesn't exist in contents arraylist
						cell.setText(finalArray[i][j]);
						cell.setVisibility(1);
						contents.add(finalArray[i][j]);
					}
				} else {
					cell.setText(finalArray[i][j]);
				}
				
				if(cell.getText().equals("-")){
					// Accounts for the empty variable name cells
					cell.setGravity(Gravity.CENTER);					
				}
				//append an individual cell to a content row
				row.addView(cell);
			}
			rowCount++;
		}
	}     

	public String[][] getFinalArray() {
		return finalArray;
	}

	public static void setFinalArray(String[][] newFinalArray) {
		TablePainter.finalArray = newFinalArray;
	}
	
	public static ArrayList<String> getOutputVariableName() {
		return outputVariableName;
	}

	public static void setOutputVariableName(ArrayList<String> outputVariableName) {
		TablePainter.outputVariableName = outputVariableName;
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
	        	Intent openUserProfile = new Intent("android.intent.action.USERPROFILE");
	        	startActivity(openUserProfile);
	            return true;
	        case R.id.helpMenu:
//				Intent openHelpBlog = new Intent(Intent.ACTION_VIEW, Uri.parse("http://joyenettles.blogspot.com"));
//				startActivity(openHelpBlog);
//	        	return true;
	        case R.id.exitProgram:
                finish();
                System.exit(0);
                
             default:
                return super.onOptionsItemSelected(item);
        }
    }


}
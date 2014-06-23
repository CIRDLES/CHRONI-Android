//package org.cirdles.chroni;
//
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Comparator;
//import java.util.Iterator;
//import java.util.TreeMap;
//import java.util.Map.Entry;
//
//import android.app.Activity;
//import android.content.Intent;
//import android.graphics.Color;
//import android.graphics.Typeface;
//import android.net.Uri;
//import android.os.Bundle;
//import android.view.Gravity;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.widget.HorizontalScrollView;
//import android.widget.LinearLayout;
//import android.widget.ScrollView;
//import android.widget.TableLayout;
//import android.widget.TableRow;
//import android.widget.TextView;
//import android.widget.TableRow.LayoutParams;
//
//public class TableBuilder extends Activity{
//	
//	private static String concordiaUrl, probabilityDensityUrl; // urls to neccessary images
//
//	private static TreeMap<Integer, Category> categoryMap; // map returned from parsing Report Settings
//	private static TreeMap<String, Fraction> fractionMap; // map returned from parsing Aliquot
//	
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
////        setContentView(R.layout.new_table);
//		
//        // Instantiates the Report Settings Parser
//		ReportSettingsParser RSP = new ReportSettingsParser();
//		String reportSettingsPath = "/sdcard/Download/bigReportSettings.xml";	// sets the default report settings xml
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
//
//		String[][] finalArray = fillArray(outputVariableName,reportSettingsArray, fractionArray);
//
////        TableLayout table = (TableLayout) findViewById(R.id.myTable);
//
//	    int rowCount = 0;
////	    TextView aliquotName = new TextView(this);
//	    ArrayList<String> contents = new ArrayList<String>();
//		
//	    HorizontalScrollView screenScroll = (HorizontalScrollView)findViewById(R.id.horizontalScrollView);
//	    LinearLayout tableLayout = (LinearLayout)findViewById(R.id.displayTableLayout);
//		TableLayout headerTable = (TableLayout)findViewById(R.id.tableForHeader);
//		
//		ScrollView scrollPane = (ScrollView)findViewById(R.id.scrollPane);
//		TableLayout table = (TableLayout)findViewById(R.id.finalTable);
//		
//		// displays the array in the application
//		final int ROWS = 5 + fractionMap.size();
//
//		// Table Layout Printing
//		for(int i = 0; i < ROWS; i++ ){
//			
//			TableRow row = new TableRow(this);
//			
//			// puts rows in appropriate place on layout 
//			if (rowCount < 5) {
//				// Report Settings and aliquot name rows
//				headerTable.addView(row);
//			} else {
//				// Aliquot rows go in aliquot scroll table
//				table.addView(row);
//			}
//					
//			for(int j = 0; j < outputVariableName.size(); j++){
//				TextView rowElement = new TextView(this);
//				rowElement.setWidth(125);
//				rowElement.setPadding(3, 4, 3, 4);
//				rowElement.setTextColor(Color.BLACK);
//				rowElement.setTextSize((float)14.5);
//				rowElement.setGravity(Gravity.RIGHT);
//							
//				if (rowCount < 5){
//					rowElement.setTypeface(Typeface.DEFAULT_BOLD);
//					rowElement.setGravity(Gravity.CENTER);
//				}
//				
//				if (rowCount == 0){
//					rowElement.setGravity(Gravity.LEFT);
//				}
//				
//				// sets appropriate background color for cells
//				if(rowCount < 4){
//					// colors odd header rows
////					rowElement.setBackgroundResource(R.drawable.dark_grey_background);
//					rowElement.setTextColor(Color.WHITE);
//				}
//				else if (rowCount > 4 && rowCount % 2 == 1) {
//					// colors odd body rows
////					rowElement.setBackgroundResource(R.drawable.light_grey_background);
//				}else if(rowCount == 4){
//					// aliquot name cell
//					rowElement.setTextColor(Color.WHITE);
////					rowElement.setBackgroundResource(R.drawable.dark_grey_background);
//					rowElement.setTypeface(Typeface.DEFAULT_BOLD);
//				}
//				else {
//					// header rows and all other body rows
////					rowElement.setBackgroundResource(R.drawable.white_background);
//				}
//					
//				//populates the first row with the associated category info corresponding to each column
//				//sets duplicates to invisible if a specified category name is already presently being displayed
//				if (rowCount == 0) {
//					if (contents.size() != 0 && contents.contains(finalArray[i][j])) {
//						rowElement.setText(finalArray[i][j]);
//						rowElement.setTextColor(Color.TRANSPARENT);
//					} else {
//						//category name doesn't exist in contents arraylist
//						rowElement.setText(finalArray[i][j]);
//						rowElement.setVisibility(1);
//						contents.add(finalArray[i][j]);
//					}
//				} else {
//					rowElement.setText(finalArray[i][j]);
//				}
//				
//				if(rowElement.getText().equals("-")){
//					rowElement.setGravity(Gravity.CENTER);					
//				}
//				//append an individual cell to a content row
//				row.addView(rowElement);
//			}
//			rowCount++;
//		}
//	}     
//
//	/*
//	 * Fills the Report Settings portion of the array
//	 */
//	private static String[][] fillReportSettingsArray(ArrayList<String> outputVariableName, TreeMap<Integer, Category> categoryMap){
//		final int COLUMNS = outputVariableName.size(); 
//		final int ROWS = 4; // 4 is the number of rows for specific information in the array
//			
//		String[][] reportSettingsArray = new String[ROWS][COLUMNS]; 
//		int columnNum = 0; // the current column number for the array
//
//		Iterator<Entry<Integer, Category>> iterator = categoryMap.entrySet().iterator();		
//		while (iterator.hasNext()) {
//			Entry<Integer, Category> category = iterator.next();
//			int columnCount = 1; // the number of columns under each category
//			
//			Iterator<Entry<Integer, Column>> columnIterator = category.getValue().getCategoryColumnMap().entrySet().iterator();
//			while (columnIterator.hasNext()) {
//				Entry<Integer, Column> column = columnIterator.next();
//				int rowNum = 0; // the current row number for the array
//
//				// Will always be in the 0th row, Category Information stored here
//				if (columnCount <= 1){
//					reportSettingsArray[rowNum][columnNum] = category.getValue().getDisplayName();
//					rowNum++;
//				}else {
//					reportSettingsArray[rowNum][columnNum] = "";
//					rowNum++;
//				}
//				
//					// puts the displayNames in the array
//					reportSettingsArray[rowNum][columnNum] = column.getValue().getDisplayName1();
//					rowNum++;
//
//					reportSettingsArray[rowNum][columnNum] = column.getValue().getDisplayName2();
//					rowNum++;
//					
//					reportSettingsArray[rowNum][columnNum] = column.getValue().getDisplayName3();
//					rowNum++;
//					
////					String methodName = column.getValue().getMethodName();
////					String variableName = column.getValue().getVariableName();
//
//					columnNum++;
//					columnCount++;	
//					
//					// Fills in uncertainty column information
//					if(column.getValue().getUncertaintyColumn() != null){
//						rowNum = 0;
//						reportSettingsArray[rowNum][columnNum] = "";
//						rowNum++;
//					
//						// puts the displayNames in the array
//						reportSettingsArray[rowNum][columnNum] = column.getValue().getUncertaintyColumn().getDisplayName1();
//						rowNum++;
//
//						reportSettingsArray[rowNum][columnNum] = column.getValue().getUncertaintyColumn().getDisplayName2();
//						if(reportSettingsArray[rowNum][columnNum].equals("PLUSMINUS2SIGMA")){
//							reportSettingsArray[rowNum][columnNum] = "\u00B12\u03C3";
//						}
//						rowNum++;
//						
//						reportSettingsArray[rowNum][columnNum] = column.getValue().getUncertaintyColumn().getDisplayName3();
//						if(reportSettingsArray[rowNum][columnNum].equals("PLUSMINUS2SIGMA%")){
//							reportSettingsArray[rowNum][columnNum] = "\u00B12\u03C3%";
//						}
//						
//						
//						rowNum++;
//						
////						String uncertaintyMethodName = column.getValue().getUncertaintyColumn().getMethodName();					
////						String uncertaintyVariableName = column.getValue().getUncertaintyColumn().getVariableName();
//
//						columnNum++;
//						columnCount++;
//					}
//			}
//			}
//		
//		return reportSettingsArray;	
//	}
//
//	/*
//	 * Fills the Aliquot portion of the array
//	 */
//	public static String[][] fillFractionArray(ArrayList<String> outputVariableName, TreeMap<Integer, Category> categoryMap, TreeMap<String, Fraction> fractionMap, String aliquotName){
//		BigDecimal valueToBeRounded;
//		BigDecimal roundedValue;
//		final int COLUMNS = outputVariableName.size(); 
//		final int ROWS = fractionMap.size(); 		
//		String[][] fractionArray = new String[ROWS][COLUMNS]; 
//		int arrayColumnCount = 0; // the current column number for the array
//		
//		// Fills in the rows reserved for the aliquot name
//		fractionArray[0][0] = aliquotName;
//		for(int j = 1; j < COLUMNS; j++){
//				fractionArray[0][j] = " ";
//			}
//	
//		Iterator<Entry<Integer, Category>> categoryIterator = categoryMap.entrySet().iterator();		
//		while (categoryIterator.hasNext()) {
//			Entry<Integer, Category> category = categoryIterator.next();	
//			Iterator<Entry<Integer, Column>> columnIterator = category.getValue().getCategoryColumnMap().entrySet().iterator();
//			
//			while (columnIterator.hasNext()) {
//				Entry<Integer, Column> column = columnIterator.next();
//				String variableName = column.getValue().getVariableName();
//				
//				//going to iterate through all fractions for every column
//				Iterator<Entry<String, Fraction>> fractionIterator = fractionMap.entrySet().iterator();
//				int arrayRowCount = 0; // the current row number for the array; 
//
//				while(fractionIterator.hasNext()){
//					Entry<String, Fraction> fraction = fractionIterator.next();
//					if(variableName.equals("")){
//						fractionArray[arrayRowCount][arrayColumnCount] = fraction.getValue().getFractionID(); 
//						arrayRowCount++;
//					}else{
//						ValueModel valueModel = DomParser.getValueModelByName(fraction.getValue(), variableName);
//						
//						if(valueModel != null){
//							float initialValue = valueModel.getValue();
//							String currentUnit = column.getValue().getUnits();
//							int countOfSignificantDigits = column.getValue().getCountOfSignificantDigits();
//							if(Numbers.getUnitConversionsMap().containsKey(currentUnit)){
//								Integer dividingNumber = Numbers.getUnitConversionsMap().get(currentUnit);
//								valueToBeRounded = new BigDecimal(initialValue/(Math.pow(10, dividingNumber)));
//								roundedValue = valueToBeRounded.setScale(countOfSignificantDigits, valueToBeRounded.ROUND_HALF_UP);
//								fractionArray[arrayRowCount][arrayColumnCount] = String.valueOf(roundedValue);
//							}		
//						}
//
//						else {	// if value model is null
//							fractionArray[arrayRowCount][arrayColumnCount] = "-";
//						}
//						arrayRowCount++;
//
//					} //ends else	
//				} // ends while
//				arrayColumnCount++;
//				
//					if(column.getValue().getUncertaintyColumn() != null){
//						Iterator<Entry<String, Fraction>> fractionIterator2 = fractionMap.entrySet().iterator();
//						arrayRowCount = 0; // the current row number for the array; 
//						variableName = column.getValue().getUncertaintyColumn().getVariableName();
//						while(fractionIterator2.hasNext()){
//							Entry<String, Fraction> fraction = fractionIterator2.next();
//							ValueModel valueModel = DomParser.getValueModelByName(fraction.getValue(), variableName);
//							
//							if(valueModel != null){
//								float oneSigma = valueModel.getOneSigma();	
//								String currentUnit = column.getValue().getUnits();
//								int uncertaintyCountOfSignificantDigits = column.getValue().getUncertaintyColumn().getCountOfSignificantDigits();
//								if(Numbers.getUnitConversionsMap().containsKey(currentUnit)){
//									Integer dividingNumber = Numbers.getUnitConversionsMap().get(currentUnit);
//									valueToBeRounded = new BigDecimal((oneSigma/(Math.pow(10, dividingNumber))) * 2);
//									
//									if(column.getValue().getUncertaintyType().equals("PCT")){
//										valueToBeRounded = new BigDecimal((oneSigma/(Math.pow(10, dividingNumber))) * 200);
//										}
//									
//									roundedValue = valueToBeRounded.setScale(uncertaintyCountOfSignificantDigits, valueToBeRounded.ROUND_HALF_UP);
//									fractionArray[arrayRowCount][arrayColumnCount] = String.valueOf(roundedValue);
//							}
//							} //closes if
//							else {	// if value model is null
//									fractionArray[arrayRowCount][arrayColumnCount] = "-";
//								}
//								arrayRowCount++;
//					} // ends loop through fraction
//						arrayColumnCount++;
//					
//				} // closes filling uncertainty columns
//									
//				} // closes column
//		} // closes category 
//		
//		return fractionArray;
//	} // closes method
//
//	/*
//	 * Fills the entire application array.
//	 */
//	private static String[][] fillArray(ArrayList<String> outputVariableName, String[][] reportSettingsArray, String[][] fractionArray){
//		final int COLUMNS = outputVariableName.size(); 
//		final int ROWS = 5 + fractionMap.size(); // 8 is the number of rows for specific information in the array
//		String[][] finalArray = new String[ROWS][COLUMNS];
//		
//		// Fills in the Report Settings Part of Array
//		for(int i = 0; i < 4; i++){
//			for(int j = 0; j < COLUMNS; j++){
//				finalArray[i][j] = reportSettingsArray[i][j];
//			}
//		}
//		
//		// Fills in the empty row for the aliquot
//		finalArray[4][0] = AliquotParser.getAliquotName();
//		for(int j = 1; j < COLUMNS; j++){
//				finalArray[4][j] = " ";
//			}
//			
//		// Fills in the fraction information of the array
//			for(int column = 0; column < COLUMNS; column++){
//				for (int row = 5; row < ROWS; row++){
//				finalArray[row][column] = fractionArray[(row-5)][column];
//			}
//		}			
//		return finalArray;		
//	}
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu, menu);
//        return true;
//    }
//    
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle item selection
//        switch (item.getItemId()) {
//        	case R.id.returnToMenu:
//        		Intent openMainMenu = new Intent("android.intent.action.MAINMENU");
//        		startActivity(openMainMenu);
//        		return true;
//            case R.id.editProfileMenu:
//	        	Intent openUserProfile = new Intent("android.intent.action.USERPROFILE");
//	        	startActivity(openUserProfile);
//	            return true;
//	        case R.id.helpMenu:
//				Intent openHelpBlog = new Intent(Intent.ACTION_VIEW, Uri.parse("http://joyenettles.blogspot.com"));
//				startActivity(openHelpBlog);
//	        	return true;
//	        case R.id.exitProgram:
//                finish();
//                System.exit(0);
//                
////            case R.id.deleteFileMenu:
////            	Intent openReportSettingsMenu = new Intent("android.intent.action.REPORTSETTINGSMENU");
////				startActivity(openReportSettingsMenu);
////                return true;
////            case R.id.renameFileMenu:
////            	Intent openAliquotMenu = new Intent("android.intent.action.ALIQUOTMENU");
////				startActivity(openAliquotMenu);
////                return true;
////            case R.id.defaultFileMenu:
////            	Intent openAliquotMenu = new Intent("android.intent.action.ALIQUOTMENU");
////				startActivity(openAliquotMenu);
////                return true;
//
////            case R.id.selectAliquotMenu:
////            	Intent openAliquotMenu = new Intent("android.intent.action.ALIQUOTMENU");
////				startActivity(openAliquotMenu);
////                return true;
////            case R.id.selectReportSettingsMenu:
////            	Intent openReportSettingsMenu = new Intent("android.intent.action.REPORTSETTINGSMENU");
////				startActivity(openReportSettingsMenu);
////                return true;
//                
//             default:
//                return super.onOptionsItemSelected(item);
//        }
//    }
//
//
//}

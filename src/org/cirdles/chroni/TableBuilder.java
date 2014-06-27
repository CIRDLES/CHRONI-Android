package org.cirdles.chroni;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Map.Entry;

import android.os.Environment;

public class TableBuilder{
	
	private static String reportSettingsPath,  aliquotPath; // the file names to be parsed
	private static String concordiaUrl, probabilityDensityUrl; // urls to neccessary images

	private static TreeMap<Integer, Category> categoryMap; // map returned from parsing Report Settings
	private static TreeMap<String, Fraction> fractionMap; // map returned from parsing Aliquot
	
	public static void buildTable(){
		
        // Instantiates the Report Settings Parser
		ReportSettingsParser RSP = new ReportSettingsParser();
		if(getReportSettingsPath() == null){
			setReportSettingsPath(Environment.getExternalStorageDirectory() + "/CHRONI/Report Settings/Default Report Settings.xml"); // sets the default RS path if none specified
		}
		categoryMap = (TreeMap<Integer, Category>) RSP.runReportSettingsParser(getReportSettingsPath());
		ArrayList<String> outputVariableName = RSP.getOutputVariableName();

		// Instantiates the Aliquot Parser
		AliquotParser AP = new AliquotParser();
//		String aliquotPath = "/sdcard/Download/geochron_7767.xml";
		fractionMap = (TreeMap<String, Fraction>) AP.runAliquotParser(getAliquotPath());
		String aliquot = AP.getAliquotName();
		
		// fills the arrays
		String[][] reportSettingsArray = fillReportSettingsArray(outputVariableName, categoryMap);
		String[][] fractionArray = fillFractionArray(outputVariableName,categoryMap, fractionMap, aliquot);
		
		// handles the array sorting
        Arrays.sort( fractionArray, new Comparator<String[]>() {
            @Override
            public int compare ( final String[] entry1, final String[] entry2 ) {
                int retVal = 0;

                final String field1 = entry1[0].trim();
                final String field2 = entry2[0].trim();

                Comparator<String> forNoah = new IntuitiveStringComparator<String>();
                	return retVal = forNoah.compare( field1, field2 );
            }
        } );

		String[][] finalArray = fillArray(outputVariableName,reportSettingsArray, fractionArray);
		TablePainterActivity.setFinalArray(finalArray);
		TablePainterActivity.setOutputVariableName(outputVariableName);

	}     

	/*
	 * Fills the Report Settings portion of the array
	 */
	private static String[][] fillReportSettingsArray(ArrayList<String> outputVariableName, TreeMap<Integer, Category> categoryMap){
		final int COLUMNS = outputVariableName.size(); 
		final int ROWS = 4; // 4 is the number of rows for specific information in the array
			
		String[][] reportSettingsArray = new String[ROWS][COLUMNS]; 
		int columnNum = 0; // the current column number for the array

		Iterator<Entry<Integer, Category>> iterator = categoryMap.entrySet().iterator();		
		while (iterator.hasNext()) {
			Entry<Integer, Category> category = iterator.next();
			int columnCount = 1; // the number of columns under each category
			
			Iterator<Entry<Integer, Column>> columnIterator = category.getValue().getCategoryColumnMap().entrySet().iterator();
			while (columnIterator.hasNext()) {
				Entry<Integer, Column> column = columnIterator.next();
				int rowNum = 0; // the current row number for the array

				// Will always be in the 0th row, Category Information stored here
				if (columnCount <= 1){
					reportSettingsArray[rowNum][columnNum] = category.getValue().getDisplayName();
					rowNum++;
				}else {
					reportSettingsArray[rowNum][columnNum] = "";
					rowNum++;
				}
				
					// puts the displayNames in the array
					reportSettingsArray[rowNum][columnNum] = column.getValue().getDisplayName1();
					rowNum++;

					reportSettingsArray[rowNum][columnNum] = column.getValue().getDisplayName2();
					rowNum++;
					
					reportSettingsArray[rowNum][columnNum] = column.getValue().getDisplayName3();
					rowNum++;
					
//					String methodName = column.getValue().getMethodName();
//					String variableName = column.getValue().getVariableName();

					columnNum++;
					columnCount++;	
					
					// Fills in uncertainty column information
					if(column.getValue().getUncertaintyColumn() != null){
						rowNum = 0;
						reportSettingsArray[rowNum][columnNum] = "";
						rowNum++;
					
						// puts the displayNames in the array
						reportSettingsArray[rowNum][columnNum] = column.getValue().getUncertaintyColumn().getDisplayName1();
						rowNum++;

						reportSettingsArray[rowNum][columnNum] = column.getValue().getUncertaintyColumn().getDisplayName2();
						if(reportSettingsArray[rowNum][columnNum].equals("PLUSMINUS2SIGMA")){
							reportSettingsArray[rowNum][columnNum] = "\u00B12\u03C3";
						}
						rowNum++;
						
						reportSettingsArray[rowNum][columnNum] = column.getValue().getUncertaintyColumn().getDisplayName3();
						if(reportSettingsArray[rowNum][columnNum].equals("PLUSMINUS2SIGMA%")){
							reportSettingsArray[rowNum][columnNum] = "\u00B12\u03C3%";
						}
						
						
						rowNum++;
						
//						String uncertaintyMethodName = column.getValue().getUncertaintyColumn().getMethodName();					
//						String uncertaintyVariableName = column.getValue().getUncertaintyColumn().getVariableName();

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
	public static String[][] fillFractionArray(ArrayList<String> outputVariableName, TreeMap<Integer, Category> categoryMap, TreeMap<String, Fraction> fractionMap, String aliquotName){
		BigDecimal valueToBeRounded;
		BigDecimal roundedValue;
		final int COLUMNS = outputVariableName.size(); 
		final int ROWS = fractionMap.size(); 		
		String[][] fractionArray = new String[ROWS][COLUMNS]; 
		int arrayColumnCount = 0; // the current column number for the array
		
		// Fills in the rows reserved for the aliquot name
		fractionArray[0][0] = aliquotName;
		for(int j = 1; j < COLUMNS; j++){
				fractionArray[0][j] = " ";
			}
	
		Iterator<Entry<Integer, Category>> categoryIterator = categoryMap.entrySet().iterator();		
		while (categoryIterator.hasNext()) {
			Entry<Integer, Category> category = categoryIterator.next();	
			Iterator<Entry<Integer, Column>> columnIterator = category.getValue().getCategoryColumnMap().entrySet().iterator();
			
			while (columnIterator.hasNext()) {
				Entry<Integer, Column> column = columnIterator.next();
				String variableName = column.getValue().getVariableName();
				
				//going to iterate through all fractions for every column
				Iterator<Entry<String, Fraction>> fractionIterator = fractionMap.entrySet().iterator();
				int arrayRowCount = 0; // the current row number for the array; 

				while(fractionIterator.hasNext()){
					Entry<String, Fraction> fraction = fractionIterator.next();
					if(variableName.equals("")){
						fractionArray[arrayRowCount][arrayColumnCount] = fraction.getValue().getFractionID(); 
						arrayRowCount++;
					}else{
						ValueModel valueModel = DomParser.getValueModelByName(fraction.getValue(), variableName);
						
						if(valueModel != null){
							float initialValue = valueModel.getValue();
							String currentUnit = column.getValue().getUnits();
							int countOfSignificantDigits = column.getValue().getCountOfSignificantDigits();
							if(Numbers.getUnitConversionsMap().containsKey(currentUnit)){
								Integer dividingNumber = Numbers.getUnitConversionsMap().get(currentUnit);
								valueToBeRounded = new BigDecimal(initialValue/(Math.pow(10, dividingNumber)));
								roundedValue = valueToBeRounded.setScale(countOfSignificantDigits, valueToBeRounded.ROUND_HALF_UP);
								fractionArray[arrayRowCount][arrayColumnCount] = String.valueOf(roundedValue);
							}		
						}

						else {	// if value model is null
							fractionArray[arrayRowCount][arrayColumnCount] = "-";
						}
						arrayRowCount++;

					} //ends else	
				} // ends while
				arrayColumnCount++;
				
					if(column.getValue().getUncertaintyColumn() != null){
						Iterator<Entry<String, Fraction>> fractionIterator2 = fractionMap.entrySet().iterator();
						arrayRowCount = 0; // the current row number for the array; 
						variableName = column.getValue().getUncertaintyColumn().getVariableName();
						while(fractionIterator2.hasNext()){
							Entry<String, Fraction> fraction = fractionIterator2.next();
							ValueModel valueModel = DomParser.getValueModelByName(fraction.getValue(), variableName);
							
							if(valueModel != null){
								float oneSigma = valueModel.getOneSigma();	
								String currentUnit = column.getValue().getUnits();
								int uncertaintyCountOfSignificantDigits = column.getValue().getUncertaintyColumn().getCountOfSignificantDigits();
								if(Numbers.getUnitConversionsMap().containsKey(currentUnit)){
									Integer dividingNumber = Numbers.getUnitConversionsMap().get(currentUnit);
									valueToBeRounded = new BigDecimal((oneSigma/(Math.pow(10, dividingNumber))) * 2);
									
									if(column.getValue().getUncertaintyType().equals("PCT")){
										valueToBeRounded = new BigDecimal((oneSigma/(Math.pow(10, dividingNumber))) * 200);
										}
									
									roundedValue = valueToBeRounded.setScale(uncertaintyCountOfSignificantDigits, valueToBeRounded.ROUND_HALF_UP);
									fractionArray[arrayRowCount][arrayColumnCount] = String.valueOf(roundedValue);
							}
							} //closes if
							else {	// if value model is null
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
	private static String[][] fillArray(ArrayList<String> outputVariableName, String[][] reportSettingsArray, String[][] fractionArray){
		final int COLUMNS = outputVariableName.size(); 
		final int ROWS = 5 + fractionMap.size(); // 8 is the number of rows for specific information in the array
		String[][] finalArray = new String[ROWS][COLUMNS];
		
		// Fills in the Report Settings Part of Array
		for(int i = 0; i < 4; i++){
			for(int j = 0; j < COLUMNS; j++){
				finalArray[i][j] = reportSettingsArray[i][j];
			}
		}
		
		// Fills in the empty row for the aliquot
		finalArray[4][0] = AliquotParser.getAliquotName();
		for(int j = 1; j < COLUMNS; j++){
				finalArray[4][j] = " ";
			}
			
		// Fills in the fraction information of the array
			for(int column = 0; column < COLUMNS; column++){
				for (int row = 5; row < ROWS; row++){
				finalArray[row][column] = fractionArray[(row-5)][column];
			}
		}			
		return finalArray;		
	}

	public static String getReportSettingsPath() {
		return reportSettingsPath;
	}

	public static void setReportSettingsPath(String newReportSettingsPath) {
		reportSettingsPath = newReportSettingsPath;
	}

	public static String getAliquotPath() {
		return aliquotPath;
	}

	public static void setAliquotPath(String newAliquotPath) {
		aliquotPath = newAliquotPath;
	}




}

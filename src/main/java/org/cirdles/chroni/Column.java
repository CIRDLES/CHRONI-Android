package org.cirdles.chroni;

/**
 * Defines the structure of a column object for Report Settings files
 */
public class Column {

    // The display names for the Column
	private String displayName;
	private String displayName1;
	private String displayName2;
	private String displayName3;

    private String units;
	private String methodName;
	private String variableName;
	private int positionIndex; // index of the column
	private boolean displayedWithArbitraryDigitCount; // Determines how many digits will be displayed
	private int countOfSignificantDigits; // number of specified significant digits

	private Column uncertaintyColumn = null; // the uncertainty column contained within the given column
	private String uncertaintyType = null;
    private boolean isUncertaintyColumn = false; // true if the column is an uncertainty column

    /**
     * Instantiates a Column object given several attributes
     */
	public Column(String displayName, String displayName1,
			String displayName2, String displayName3, String units, String methodName, String variableName,
				  int pIndex, boolean displayedWithArbitraryDigitCount, int sigFigs) {
			this.displayName = displayName;
        	this.displayName1 = displayName1;
    		this.displayName2 = displayName2;
    		this.displayName3 = displayName3;
    		this.units = units;
    		this.methodName = methodName;
    		this.variableName = variableName;
    		this.positionIndex = pIndex;
			this.displayedWithArbitraryDigitCount = displayedWithArbitraryDigitCount;
    		this.countOfSignificantDigits = sigFigs;
	}


	@Override
    /**
     * Creates a string of the given column using the attributes found in that column
     */
	public String toString() {
		return "This Column object belongs to the " + displayName + " category. It has displayName1: " +
				displayName1 + ", displayName2: " + displayName2 + ", displayName3: " + displayName3 +
				", methodName: " + methodName + " , variableName: " + variableName + " and positionIndex: " +
				positionIndex + ".";
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getDisplayName1() {
		return displayName1;
	}

	public void setDisplayName1(String name) {
		this.displayName1 = name;
	}


	public String getDisplayName2() {
		return displayName2;
	}

	public void setDisplayName2(String name) {
		this.displayName2 = name;
	}


	public String getDisplayName3() {
		return displayName3;
	}

	public void setDisplayName3(String name) {
		this.displayName3 = name;
	}


	public Column getUncertaintyColumn() {
		return uncertaintyColumn;
	}

	public void setUncertaintyColumn(Column column) {
		this.uncertaintyColumn = column;
	}


	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String name) {
		this.methodName = name;
	}


	public String getVariableName() {
		return variableName;
	}

	public void setVariableName(String name) {
		this.variableName = name;
	}


	public String getUnits() {
		return units;
	}

	public int getCountOfSignificantDigits() {
		return countOfSignificantDigits;
	}


	public String getUncertaintyType() {
		return uncertaintyType;
	}


	public void setUncertaintyType(String uncertaintyType) {
		this.uncertaintyType = uncertaintyType;
	}

	public boolean isDisplayedWithArbitraryDigitCount() {
		return displayedWithArbitraryDigitCount;
	}

    public void setUncertaintyColumn(boolean isUncertaintyColumn) {
        this.isUncertaintyColumn = isUncertaintyColumn;
    }
}

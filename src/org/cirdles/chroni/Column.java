package org.cirdles.chroni;

public class Column {

	private String displayName;
	private String displayName1;
	private String displayName2;
	private String displayName3;
	private String units;
	private String methodName;
	private String variableName;
	private int positionIndex;
	private int countOfSignificantDigits;

	private Column uncertaintyColumn = null;
	private String uncertaintyType = null;
	
	public Column(String displayName, String displayName1,
			String displayName2, String displayName3, String units, String methodName, String variableName, int pIndex, int sigFigs) {
			this.displayName = displayName;
        	this.displayName1 = displayName1;
    		this.displayName2 = displayName2;
    		this.displayName3 = displayName3;
    		this.units = units;
    		this.methodName = methodName;
    		this.variableName = variableName;
    		this.positionIndex = pIndex;
    		this.countOfSignificantDigits = sigFigs;
	}


	@Override
	public String toString() {
		String columnInformation = "This Column object belongs to the " + displayName + " category. It has displayName1: " + displayName1 + ", displayName2: " + displayName2 + ", displayName3: " + displayName3 + ", methodName: " + methodName + " , variableName: " + variableName + " and positionIndex: " + positionIndex + ".";
		return columnInformation;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String categoryName) {
		this.displayName = categoryName;
	}
	
	public String getDisplayName1() {
		return displayName1;
	}

	public void setDisplayName1(String displayName1) {
		this.displayName1 = displayName1;
	}

	public String getDisplayName2() {
		return displayName2;
	}

	public void setDisplayName2(String displayName2) {
		this.displayName2 = displayName2;
	}

	public String getDisplayName3() {
		return displayName3;
	}

	public void setDisplayName3(String displayName3) {
		this.displayName3 = displayName3;
	}


	public int getPositionIndex() {
		return positionIndex;
	}


	public void setPositionIndex(int positionIndex) {
		this.positionIndex = positionIndex;
	}

	public Column getUncertaintyColumn() {
		return uncertaintyColumn;
	}


	public void setUncertaintyColumn(Column uncertaintyColumn) {
		this.uncertaintyColumn = uncertaintyColumn;
	}


	public String getMethodName() {
		return methodName;
	}


	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}


	public String getVariableName() {
		return variableName;
	}


	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}


	/**
	 * @return the units
	 */
	public String getUnits() {
		return units;
	}


	/**
	 * @param units the units to set
	 */
	public void setUnits(String units) {
		this.units = units;
	}


	/**
	 * @return the countOfSignificantDigits
	 */
	public int getCountOfSignificantDigits() {
		return countOfSignificantDigits;
	}


	/**
	 * @param countOfSignificantDigits the countOfSignificantDigits to set
	 */
	public void setCountOfSignificantDigits(int countOfSignificantDigits) {
		this.countOfSignificantDigits = countOfSignificantDigits;
	}


	public String getUncertaintyType() {
		return uncertaintyType;
	}


	public void setUncertaintyType(String uncertaintyType) {
		this.uncertaintyType = uncertaintyType;
	}

}

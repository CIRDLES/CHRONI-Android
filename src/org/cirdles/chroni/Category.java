package org.cirdles.chroni;

import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

public class Category {

	private String displayName;
	private int positionIndex;
	private SortedMap<Integer,Column> categoryColumnMap = null;

	public Category(String dn, int pIndex) {
		this.displayName = dn;
		this.positionIndex = pIndex;
		this.categoryColumnMap = new TreeMap <Integer, Column>();
		}

	@Override
	public String toString() {
		StringBuilder categoryString = new StringBuilder();
		categoryString.append("This object of Category has displayName: " + displayName + " and positionIndex: "
				+ positionIndex + "." + "\n");
		
		Iterator<Entry<Integer, Column>> iterator = categoryColumnMap.entrySet().iterator();
        while (iterator.hasNext()) {
        	Entry<Integer, Column> column = iterator.next();
			categoryString.append(column.getValue()).append("\n"); // Adds category name to the array of category names
	}
		return categoryString.toString() + "\n";
	
}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public int getPositionIndex() {
		return positionIndex;
	}

	public void setPositionIndex(int positionIndex) {
		this.positionIndex = positionIndex;
	}

	public SortedMap<Integer,Column> getCategoryColumnMap() {
		return categoryColumnMap;
	}

	public void setCategoryColumnMap(SortedMap<Integer,Column> categoryColumnMap) {
		this.categoryColumnMap = categoryColumnMap;
	}
}

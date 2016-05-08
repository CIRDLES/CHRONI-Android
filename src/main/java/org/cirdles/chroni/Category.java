/*
 * Copyright 2016 CIRDLES.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cirdles.chroni;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

/*
This class defines a Category object for a Report Settings file.
 */
public class Category {

	private String displayName; // name of the category
	private int positionIndex; // where the category is located
	private SortedMap<Integer,Column> categoryColumnMap = null; // map containing the columns of a category sorted by ints
    private int columnCount; // keeps track of how many visible columns are within a category

    /**
	 * Instantiates a Category given a display name and position index
     */
	public Category(String dn, int pIndex) {
		this.displayName = dn;
		this.positionIndex = pIndex;
		this.categoryColumnMap = new TreeMap <Integer, Column>();
	}

	@Override
    /**
     * Creates a string for each category
     */
	public String toString() {
		StringBuilder categoryString = new StringBuilder();
		categoryString.append("This Category has displayName: " + displayName + " and positionIndex: "
				+ positionIndex + "." + "\n");
		
		for (Entry<Integer, Column> column : categoryColumnMap.entrySet())
			// Adds category name to the array of category names
			categoryString.append(column.getValue()).append("\n");

		return categoryString.toString() + "\n";
	
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public SortedMap<Integer,Column> getCategoryColumnMap() {
		return categoryColumnMap;
	}


    public int getColumnCount() {
        return columnCount;
    }

    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }
}

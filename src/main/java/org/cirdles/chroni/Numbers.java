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

import java.util.HashMap;
import java.util.Map;

/**
 * Class used to store relevant information for data conversions for the table displayin
 */
public class Numbers {
    private static final Map<String, Integer> UnitConversions = new HashMap<String, Integer>();

    // key = units, value = exponent 
    static {
    	 UnitConversions.put( "", 0 );

         // mass is stored in grams
         UnitConversions.put( "g", 0 );
         UnitConversions.put( "mg", -3 );
         UnitConversions.put( "\u03bcg", -6 );
         UnitConversions.put( "ng", -9 );
         UnitConversions.put( "pg", -12 );
         UnitConversions.put( "fg", -15 );

         // concentrations
         UnitConversions.put( "\u0025", -2 );
         UnitConversions.put( "\u2030", -3 );
         UnitConversions.put( "ppm", -6 );
         UnitConversions.put( "ppb", -9 );
         UnitConversions.put( "g/g", 0 );

         // dates are stored in years
         UnitConversions.put( "a", 0 );
         UnitConversions.put( "ka", 3 );
         UnitConversions.put( "Ma", 6 );
         UnitConversions.put( "Ga", 9 );

         // misc in % per amu
         UnitConversions.put( "%/amu", -2 );
     }

	public static Map<String, Integer> getUnitConversionsMap() {
		return UnitConversions;
	}

}

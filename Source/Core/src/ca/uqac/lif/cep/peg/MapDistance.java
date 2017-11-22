/*
    A BeepBeep palette for mining event traces
    Copyright (C) 2017 Sylvain Hallé

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.uqac.lif.cep.peg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import ca.uqac.lif.cep.functions.BinaryFunction;
import ca.uqac.lif.cep.functions.FunctionException;
import ca.uqac.lif.cep.functions.UnaryFunction;

/**
 * Computes the distance between two maps. If <i>M</i><sub>1</sub> and
 * <i>M</i><sub>2</sub> are two maps, the distance between the two is
 * defined as:
 * &sum; |<i>M</i><sub>1</sub>[k] - <i>M</i><sub>2</sub>[k]|, where k
 * ranges over all the keys of <i>M</i><sub>1</sub>. <i>M</i>[k]
 * is defined as 0 for a non-existent key.
 * <p>
 * This function is represented graphically by this symbol:
 * <p>
 * <img src="{@docRoot}/doc-files/MapDistance.png" alt="Function symbol">
 * @author Sylvain Hallé
 *
 */
@SuppressWarnings("rawtypes")
public class MapDistance extends BinaryFunction<HashMap,HashMap,Number>
{
	/**
	 * A single instance of this function
	 */
	public static final transient MapDistance instance = new MapDistance();
	
	protected MapDistance()
	{
		super(HashMap.class, HashMap.class, Number.class);
	}

	@Override
	public Number getValue(HashMap x, HashMap y)
	{
		int distance = 0;
		for (Object k: x.keySet())
		{
			int n1 = ((Number) x.get(k)).intValue();
			int n2 = 0;
			if (y.containsKey(k))
			{
				n2 = ((Number) y.get(k)).intValue();
			}
			distance += Math.abs(n1 - n2);
		}
		return distance;
	}

	/**
	 * Creates a <tt>HashMap</tt> from the arguments. The method takes
	 * arguments 0 and 1, and creates a map entry 0 &rarr; 1. It then takes
	 * arguments 2 and 3, and creates a map entry 2 &rarr; 3, and so on.
	 * It ignores the last value if the number of arguments is odd.
	 * @param keys_and_values The keys and values to put in the map.
	 * @return The map
	 */
	public static HashMap<Object,Object> createMap(Object ... keys_and_values)
	{
		HashMap<Object,Object> map = new HashMap<Object,Object>();
		for (int i = 0; i < 2 * (keys_and_values.length / 2); i+=2)
		{
			map.put(keys_and_values[i], keys_and_values[i+1]);
		}
		return map;
	}
	
	/**
	 * From an array whose keys are strings, extracts a list of values,
	 * placed in increasing key order.
	 */	
	public static class ToValueArray extends UnaryFunction<HashMap,ArrayList>
	{
		public static final transient ToValueArray instance = new ToValueArray();
		
		protected ToValueArray()
		{
			super(HashMap.class, ArrayList.class);
		}

		@SuppressWarnings("unchecked")
		@Override
		public ArrayList getValue(HashMap x) throws FunctionException 
		{
			ArrayList<Object> sorted_values = new ArrayList<Object>();
			List<String> sorted_keys = new ArrayList<String>();
			sorted_keys.addAll(x.keySet());
			Collections.sort(sorted_keys);
			for (String key : sorted_keys)
			{
				sorted_values.add(x.get(key));
			}
			return sorted_values;
		}
		
	}

}
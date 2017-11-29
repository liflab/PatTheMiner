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
package ca.uqac.lif.cep.peg.ml;

import java.util.ArrayList;

import org.apache.commons.math3.ml.clustering.DoublePoint;

import ca.uqac.lif.cep.functions.FunctionException;
import ca.uqac.lif.cep.functions.UnaryFunction;

/**
 * Converts a list of numbers into a {@link DoublePoint} object.
 * @author Sylvain Hallé
 */
@SuppressWarnings("rawtypes")
public class ListToDoublePoint extends UnaryFunction<ArrayList,DoublePoint>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1520444324754482729L;
	public static final ListToDoublePoint instance = new ListToDoublePoint();
	
	private ListToDoublePoint()
	{
		super(ArrayList.class, DoublePoint.class);
	}

	@Override
	public DoublePoint getValue(ArrayList list) throws FunctionException
	{
		double[] values = new double[list.size()];
		for (int i = 0; i < list.size(); i++)
		{
			values[i] = ((Number) list.get(i)).doubleValue();
		}
		return new DoublePoint(values);
	}
}

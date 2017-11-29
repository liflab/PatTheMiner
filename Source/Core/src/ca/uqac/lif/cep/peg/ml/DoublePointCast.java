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

import java.util.List;

import org.apache.commons.math3.ml.clustering.DoublePoint;

import ca.uqac.lif.cep.functions.FunctionException;
import ca.uqac.lif.cep.functions.UnaryFunction;

/**
 * Attempts to create a {@link DoublePoint} out of an object. The conversion
 * rules are as follows:
 * <ul>
 * <li>A number is converted into a one-dimensional point</li>
 * <li>A list of numbers is converted into an n-dimensional point,
 * provided that all elements of the list are numbers</li>
 * <li>{@code null} otherwise</li>
 * </ul>
 * @author Sylvain Hallé
 */
public class DoublePointCast extends UnaryFunction<Object,DoublePoint>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2276328301740838266L;
	/**
	 * The unique instance of this function
	 */
	public static final DoublePointCast instance = new DoublePointCast();
	
	private DoublePointCast()
	{
		super(Object.class, DoublePoint.class);
	}

	@Override
	public DoublePoint getValue(Object x) throws FunctionException
	{
		return getDoublePoint(x);
	}
	
	/**
	 * Attempts to cast an object into a {@link DoublePoint}
	 * @param o The object
	 * @return A DoublePoint, or <tt>null</tt> if the cast failed
	 */
	public static DoublePoint getDoublePoint(Object o)
	{
		if (o instanceof Number)
		{
			return new DoublePoint(new double[]{((Number) o).doubleValue()});
		}
		if (o instanceof DoublePoint)
		{
			return (DoublePoint) o;
		}
		else if (o instanceof List)
		{
			List<?> l = (List<?>) o;
			double[] values = new double[l.size()];
			for (int i = 0; i < l.size(); i++)
			{
				Object elem = l.get(i);
				if (elem instanceof Number)
				{
					values[i] = ((Number) elem).doubleValue();
				}
				else
				{
					// We fail if one of the elements of the list
					// is not a number
					return null;
				}
			}
			return new DoublePoint(values);
		}
		return null;
	}
}

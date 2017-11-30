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

import java.util.Vector;

import ca.uqac.lif.cep.functions.BinaryFunction;

/**
 * Computes a distance metric between two numerical vectors. This is an
 * abstract class; concrete descendents implement the method
 * {@link #compute(Vector, Vector)} to calculate a specific distance metric.
 * @author Sylvain Hallé
 */
@SuppressWarnings("rawtypes")
public abstract class VectorDistance extends BinaryFunction<Vector,Vector,Number>
{
	protected VectorDistance()
	{
		super(Vector.class, Vector.class, Number.class);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Number getValue(Vector x, Vector y)
	{
		Vector<Number> v1 = (Vector<Number>) x;
		Vector<Number> v2 = (Vector<Number>) y;
		return compute(v1, v2);
	}
	
	public abstract Number compute(Vector<Number> v1, Vector<Number> v2);
}

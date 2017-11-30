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

import org.apache.commons.math3.ml.clustering.DoublePoint;

import ca.uqac.lif.cep.functions.FunctionException;
import ca.uqac.lif.cep.functions.UnaryFunction;
import ca.uqac.lif.cep.peg.Sequence;

/**
 * Function that turns a sequence of events into another sequence
 * containing a single vector of numbers.
 * @author Sylvain Hallé
 *
 * @param <T> The type of the events in the input sequence
 */
@SuppressWarnings("rawtypes")
public abstract class VectorFunction<T> extends UnaryFunction<Sequence,Sequence>
{
	public VectorFunction()
	{
		super(Sequence.class, Sequence.class);
	}

	@Override
	public Sequence<DoublePoint> getValue(Sequence x) throws FunctionException
	{
		@SuppressWarnings("unchecked")
		Sequence<T> in_seq = (Sequence<T>) x;
		DoublePoint point = computeVector(in_seq);
		Sequence<DoublePoint> seq = new Sequence<DoublePoint>();
		seq.add(point);
		return seq;
	}
	
	public abstract DoublePoint computeVector(Sequence<T> sequence);
}

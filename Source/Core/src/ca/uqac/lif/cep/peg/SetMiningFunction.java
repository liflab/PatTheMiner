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

import java.util.HashSet;
import java.util.Set;

import ca.uqac.lif.cep.functions.FunctionException;
import ca.uqac.lif.cep.functions.UnaryFunction;

/**
 * Extracts a pattern from a set of sequences.
 * @author Sylvain Hallé
 *
 * @param <T> The type of the events in the input sequences
 * @param <U> The type of the pattern that is mined from the set of sequences
 */
@SuppressWarnings("rawtypes")
public abstract class SetMiningFunction<T,U> extends UnaryFunction<Set,U>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6895170777692128662L;

	public SetMiningFunction(Class<U> u)
	{
		super(Set.class, u);
	}

	@Override
	public final U getValue(Set x) throws FunctionException
	{
		Set<Sequence<T>> sequences = new HashSet<Sequence<T>>();
		for (Object o : x)
		{
			@SuppressWarnings("unchecked")
			Sequence<T> seq = (Sequence<T>) o;
			sequences.add(seq);
		}
		return mine(sequences);
	}
	
	/**
	 * Performs mining on a set of sequences
	 * @param sequences The sequences
	 * @return
	 * @throws FunctionException 
	 */
	public abstract U mine(Set<Sequence<T>> sequences) throws FunctionException;
}

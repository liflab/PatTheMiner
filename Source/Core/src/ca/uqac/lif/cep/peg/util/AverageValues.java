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
package ca.uqac.lif.cep.peg.util;

import ca.uqac.lif.cep.peg.Sequence;
import ca.uqac.lif.cep.peg.ml.SetMiningFunction;
import java.util.Set;

/**
 * Mining function that computes the average of values in a set of
 * sequences of numbers.
 */
public class AverageValues extends SetMiningFunction<Number,Number>
{
	public static final AverageValues instance = new AverageValues();
	
	private AverageValues()
	{
		super(Number.class);
	}

	@Override
	public Number mine(Set<Sequence<Number>> sequences)
	{
		float count = 0;
		float total = 0;
		for (Sequence<Number> seq : sequences)
		{
			for (Number n : seq)
			{
				count++;
				total += n.floatValue();	
			}
		}
		if (count == 0)
			return 0;
		return total / count;
	}
	
	@Override
	public AverageValues clone()
	{
		return this;
	}
}

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

import java.util.Set;

/**
 * Mining function that computes the average values in a set of
 * sequences.
 */
public class AverageLength extends SetMiningFunction<Object,Number>
{
	public static final AverageLength instance = new AverageLength();
	
	private AverageLength()
	{
		super(Number.class);
	}

	@Override
	public Number mine(Set<Sequence<Object>> sequences)
	{
		float count = 0;
		float total = 0;
		for (Sequence<Object> seq : sequences)
		{
			count++;
			total += seq.size();
		}
		if (count == 0)
			return 0;
		return total / count;
	}
	
	@Override
	public AverageLength clone()
	{
		return this;
	}
}

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

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math3.ml.clustering.DoublePoint;
import ca.uqac.lif.cep.peg.Sequence;

@SuppressWarnings("rawtypes")
public abstract class ClusteringFunction extends SetMiningFunction<DoublePoint,Set>
{
	protected ClusteringFunction()
	{
		super(Set.class);
	}
	
	@Override
	public final Set mine(Set<Sequence<DoublePoint>> sequences)
	{
		Set<DoublePoint> points = new HashSet<DoublePoint>();
		for (Sequence<DoublePoint> seq : sequences)
		{
			if (!seq.isEmpty())
				points.add(seq.getFirst());
		}
		return computeClustering(points);
	}
		
	protected abstract Set<DoublePoint> computeClustering(Set<DoublePoint> points);
}

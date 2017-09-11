package ca.uqac.lif.cep.peg.ml;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math3.ml.clustering.DoublePoint;

import ca.uqac.lif.cep.peg.MiningFunction;
import ca.uqac.lif.cep.peg.Sequence;

@SuppressWarnings("rawtypes")
public abstract class ClusteringFunction extends MiningFunction<DoublePoint,Set>
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
		
	public abstract Set<DoublePoint> computeClustering(Set<DoublePoint> points);
}

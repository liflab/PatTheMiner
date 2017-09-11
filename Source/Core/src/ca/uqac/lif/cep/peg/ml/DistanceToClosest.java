package ca.uqac.lif.cep.peg.ml;

import java.util.Set;

import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.distance.DistanceMeasure;

import ca.uqac.lif.cep.functions.BinaryFunction;

@SuppressWarnings("rawtypes")
public class DistanceToClosest extends BinaryFunction<Set, DoublePoint, Number>
{
	protected DistanceMeasure m_measure;
	
	public DistanceToClosest(DistanceMeasure measure)
	{
		super(Set.class, DoublePoint.class, Number.class);
		m_measure = measure;
	}

	@Override
	public Number getValue(Set x, DoublePoint y)
	{
		@SuppressWarnings("unchecked")
		Set<DoublePoint> elements = (Set<DoublePoint>) x;
		double min_dist = Double.MAX_VALUE;
		double[] coords = y.getPoint();
		for (DoublePoint p : elements)
		{
			min_dist = Math.min(min_dist, m_measure.compute(p.getPoint(), coords));
		}
		return min_dist;
		
	}
}

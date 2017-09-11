package ca.uqac.lif.cep.peg.ml;

import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.distance.DistanceMeasure;

import ca.uqac.lif.cep.functions.BinaryFunction;

/**
 * Wrapper to use Apache Commons' {@link DistanceMeasure} objects as
 * BeepBeep {@link Function}s.
 * @author Sylvain Hallé
 */
public class DistanceFunction extends BinaryFunction<DoublePoint,DoublePoint,Double>
{
	protected DistanceMeasure m_measure;
	
	public DistanceFunction(DistanceMeasure measure)
	{
		super(DoublePoint.class, DoublePoint.class, Double.class);
		m_measure = measure;
	}

	@Override
	public Double getValue(DoublePoint x, DoublePoint y)
	{
		return m_measure.compute(x.getPoint(), y.getPoint());
	}
}

package ca.uqac.lif.cep.peg.ml;

import org.apache.commons.math3.ml.clustering.DoublePoint;

import ca.uqac.lif.cep.functions.FunctionException;
import ca.uqac.lif.cep.functions.UnaryFunction;

public class DoublePointCast extends UnaryFunction<Number,DoublePoint>
{
	public static final DoublePointCast instance = new DoublePointCast();
	
	private DoublePointCast()
	{
		super(Number.class, DoublePoint.class);
	}

	@Override
	public DoublePoint getValue(Number x) throws FunctionException
	{
		return new DoublePoint(new double[]{x.doubleValue()});
	}
}

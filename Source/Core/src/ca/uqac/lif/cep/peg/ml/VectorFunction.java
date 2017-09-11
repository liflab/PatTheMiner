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

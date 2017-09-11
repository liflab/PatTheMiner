package ca.uqac.lif.cep.peg.ml;

import org.apache.commons.math3.ml.clustering.DoublePoint;

import static ca.uqac.lif.cep.Connector.INPUT;
import static ca.uqac.lif.cep.Connector.OUTPUT;
import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.Connector.ConnectorException;
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.Pushable;
import ca.uqac.lif.cep.peg.Sequence;
import ca.uqac.lif.cep.tmf.SinkLast;

/**
 * Vector function derived from a processor outputting numerical
 * values. Given a sequence,
 * the function feeds all its events to some given processor {@code p},
 * and creates a 1-dimensional {@code DoublePoint} with the last
 * output of {@code p}.
 * @author Sylvain Hallé
 *
 * @param <T> The type of the events in the input trace
 */
public class ProcessorVectorFunction<T> extends VectorFunction<T>
{
	protected Processor m_processor;
	
	public ProcessorVectorFunction(Processor p)
	{
		super();
		m_processor = p;
	}

	@Override
	public DoublePoint computeVector(Sequence<T> sequence)
	{
		Processor proc = m_processor.clone();
		Pushable p = proc.getPushableInput();
		SinkLast sink = new SinkLast();
		try
		{
			Connector.connect(proc, OUTPUT, sink, INPUT);
		}
		catch (ConnectorException e1)
		{
			// Can't connect: return an empty point
			return new DoublePoint(new double[]{});
		}
		for (T e : sequence)
		{
			p.push(e);
		}
		Number[] last = (Number[]) sink.getLast();
		if (last.length > 0)
		{
			return new DoublePoint(new double[]{last[0].doubleValue()});
		}
		return new DoublePoint(new double[]{}); 
	}

}

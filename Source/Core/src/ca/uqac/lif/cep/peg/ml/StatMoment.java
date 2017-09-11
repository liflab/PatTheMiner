package ca.uqac.lif.cep.peg.ml;

import ca.uqac.lif.cep.ProcessorException;
import ca.uqac.lif.cep.UniformProcessor;

/**
 * Computes the running statistical moment of order <i>n</i> on a
 * stream of numbers.
 * @author Sylvain Hallé
 */
public class StatMoment extends UniformProcessor
{
	protected int m_order;
	
	protected double m_sum;
	
	protected double m_numEvents;
	
	public StatMoment(int order)
	{
		super(1, 1);
		m_order = order;
		m_sum = 0;
		m_numEvents = 0;
	}
	
	@Override
	public void reset()
	{
		super.reset();
		m_sum = 0;
		m_numEvents = 0;
	}

	@Override
	protected boolean compute(Object[] inputs, Object[] outputs)
			throws ProcessorException
	{
		Number n = (Number) inputs[0];
		m_numEvents++;
		m_sum += Math.pow(n.doubleValue(), m_order);
		outputs[0] = m_sum / m_numEvents;
		return true;
	}

	@Override
	public StatMoment clone()
	{
		return new StatMoment(m_order);
	}
	
}

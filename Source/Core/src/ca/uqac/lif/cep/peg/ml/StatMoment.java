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

import ca.uqac.lif.cep.ProcessorException;
import ca.uqac.lif.cep.UniformProcessor;

/**
 * Computes the running statistical moment of order <i>n</i> on a
 * stream of numbers.
 * @author Sylvain Hallé
 */
public class StatMoment extends UniformProcessor
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7769980524185280772L;

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

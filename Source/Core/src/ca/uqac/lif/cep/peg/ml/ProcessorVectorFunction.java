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

import org.apache.commons.math3.ml.clustering.DoublePoint;

import static ca.uqac.lif.cep.Connector.INPUT;
import static ca.uqac.lif.cep.Connector.OUTPUT;
import ca.uqac.lif.cep.Connector;
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
		Processor proc = m_processor.duplicate();
		Pushable p = proc.getPushableInput();
		SinkLast sink = new SinkLast();
		Connector.connect(proc, OUTPUT, sink, INPUT);
		for (T e : sequence)
		{
			p.push(e);
		}
		Object[] objs = sink.getLast();
		if (objs[0] instanceof DoublePoint)
		{
			return (DoublePoint) objs[0];
		}
		Number[] last = (Number[]) sink.getLast();
		if (last.length > 0)
		{
			return new DoublePoint(new double[]{last[0].doubleValue()});
		}
		return new DoublePoint(new double[]{}); 
	}

}

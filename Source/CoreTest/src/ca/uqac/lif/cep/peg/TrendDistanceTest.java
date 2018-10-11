/*
    A BeepBeep palette for mining event traces
    Copyright (C) 2017 Sylvain Hall�

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
package ca.uqac.lif.cep.peg;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.ProcessorException;
import ca.uqac.lif.cep.Pushable;
import ca.uqac.lif.cep.UniformProcessor;
import ca.uqac.lif.cep.tmf.Passthrough;
import ca.uqac.lif.cep.tmf.QueueSink;
import ca.uqac.lif.cep.util.Numbers;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import org.junit.Test;

/**
 * Unit tests for the trend distance pattern.
 */
public class TrendDistanceTest
{
  @Test
	public void test1()
	{
	  TrendDistance<Number,Number,Number> td 
	    = new TrendDistance<Number,Number,Number>(
	      0, // Reference trend
	      1, // Window width
	      new Passthrough(), // beta-processor
	      Numbers.subtraction, // distance metric
	      0, // distance threshold
	      Numbers.isGreaterOrEqual // comparison function
	      );
	  QueueSink qs = new QueueSink();
	  Queue<Object> q = qs.getQueue();
	  Connector.connect(td, qs);
	  Pushable p = td.getPushableInput();
	  p.push(0);
	  Object o = q.remove();
	  assertTrue((Boolean) o);
	  p.push(1);
	  o = q.remove();
    assertFalse((Boolean) o);
	}
	
	
	public static Set<Sequence<Number>> generateSequences(int how_many)
	{
		Set<Sequence<Number>> sequences = new HashSet<Sequence<Number>>();
		for (int i = 1; i <= how_many; i++)
		{
			Sequence<Number> seq = new Sequence<Number>();
			for (int j = 0; j < i; j++)
			{
				seq.add(j);
			}
			sequences.add(seq);
		}
		return sequences;
	}
		
	/**
	 * Processor that simply counts the input events.
	 */
	protected static class Counter extends UniformProcessor
	{
		protected int m_count = 0;
		
		public Counter()
		{
			super(1, 1);
		}
		
		public Counter(int start_value)
		{
			super(1, 1);
			m_count = start_value;
		}

		@Override
		protected boolean compute(Object[] inputs, Object[] outputs)
				throws ProcessorException
		{
			outputs[0] = m_count++;
			return true;
		}

		@Override
		public Processor clone()
		{
			return new Counter();
		}

    @Override
    public Processor duplicate(boolean with_state)
    {
      if (with_state)
      {
        return new Counter(m_count);
      }
      return new Counter();
    }
	}
}

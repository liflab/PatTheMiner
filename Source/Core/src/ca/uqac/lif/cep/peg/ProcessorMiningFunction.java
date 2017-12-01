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
package ca.uqac.lif.cep.peg;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.Pushable;
import ca.uqac.lif.cep.concurrency.ExceptionRunnable;
import ca.uqac.lif.cep.concurrency.ThreadManageable;
import ca.uqac.lif.cep.concurrency.ThreadManager;
import ca.uqac.lif.cep.concurrency.ThreadManager.ManagedThread;
import ca.uqac.lif.cep.functions.FunctionException;
import ca.uqac.lif.cep.tmf.SinkLast;

/**
 * Mining function that uses BeepBeep processors to extract patterns from
 * traces.
 * @author Sylvain Hallé
 *
 * @param <T> The type of the events in the input sequences
 * @param <U>The type of the pattern that is mined from the set of sequences
 */
public class ProcessorMiningFunction<T,U> extends SetMiningFunction<T,U> implements ThreadManageable
{
	/**
	 * The processor that is run on every trace
	 */
	protected Processor m_traceProcessor;

	/**
	 * The processor used to combine the values computed by each
	 * "trace processor"
	 */
	protected Processor m_combineProcessor;

	/**
	 * A thread manager
	 */
	protected ThreadManager m_manager;

	/**
	 * A set that will gather the values computed by each trace processor
	 */
	protected Set<U> m_collectedValues;

	protected U m_defaultValue = null;

	public ProcessorMiningFunction(Processor trace_processor, Processor combine_processor)
	{
		this(trace_processor, combine_processor, null);
	}

	@SuppressWarnings("unchecked")
	public ProcessorMiningFunction(Processor trace_processor, Processor combine_processor, U default_value)
	{
		super((Class<U>) combine_processor.getOutputType(0).getClass());
		m_traceProcessor = trace_processor;
		m_combineProcessor = combine_processor;
		m_manager = ThreadManager.defaultManager;
		m_collectedValues = new HashSet<U>();
		m_defaultValue = default_value;
	}

	public void setDefaultValue(U value)
	{
		m_defaultValue = value;
	}

	@Override
	public void setThreadManager(ThreadManager manager)
	{
		m_manager = manager;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object mine(@SuppressWarnings("rawtypes") Set sequences) throws FunctionException
	{
		Iterator<Sequence<T>> it = sequences.iterator();
		while (it.hasNext())
		{
			Sequence<T> seq = it.next();
			ProcessorRunnable pr = new ProcessorRunnable(seq);
			ManagedThread th = m_manager.tryNewThread(pr);
			if (th != null)
			{
				th.start();
			}
			else
			{
				pr.run();
			}
		}
		m_manager.waitForAll();
		m_combineProcessor.reset();
		SinkLast sink = new SinkLast();
		Connector.connect(m_combineProcessor, sink);
		Pushable p = m_combineProcessor.getPushableInput();
		p.push(m_collectedValues);
		Object[] values = sink.getLast();
		if (values != null)
		{
			return values[0];
		}
		return m_defaultValue;
	}

	protected class ProcessorRunnable extends ExceptionRunnable
	{
		protected Sequence<T> m_sequence;

		public ProcessorRunnable(Sequence<T> sequence)
		{
			super();
			m_sequence = sequence;
		}

		@Override
		public void tryToRun() 
		{
			Processor proc = m_traceProcessor.duplicate();
			SinkLast sink = new SinkLast();
			Connector.connect(proc, sink);
			Pushable p = proc.getPushableInput();
			for (T event : m_sequence)
			{
				p.push(event);
			}
			@SuppressWarnings("unchecked")
			U o = (U) sink.getLast()[0];
			if (o != null)
			{
				m_collectedValues.add(o);
			}
		}		
	}
}

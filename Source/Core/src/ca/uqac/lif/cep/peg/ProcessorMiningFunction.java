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
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.Connector.ConnectorException;
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.ProcessorException;
import ca.uqac.lif.cep.Pushable;
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
public class ProcessorMiningFunction<T,U> extends SetMiningFunction<T,U>
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
	protected ExecutorService m_service;

	/**
	 * A set that will gather the values computed by each trace processor
	 */
	protected HashSet<U> m_collectedValues;
	
	/**
	 * The default value returned by the mining function
	 */
	protected U m_defaultValue = null;
	
	/**
	 * An executor service to run multiple mining functions in parallel
	 */
	protected ExecutorService m_service;

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
		m_service = null;
		m_collectedValues = new HashSet<U>();
		m_defaultValue = default_value;
		m_service = Executors.newCachedThreadPool();
	}
	
	public void setDefaultValue(U value)
	{
		m_defaultValue = value;
	}

	public void setThreadManager(ExecutorService manager)
	{
		m_service = manager;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object mine(@SuppressWarnings("rawtypes") Set sequences) throws FunctionException
	{
		Iterator<Sequence<T>> it = sequences.iterator();
		List<Future<U>> futures = new ArrayList<Future<U>>();
		while (it.hasNext())
		{
			Sequence<T> seq = it.next();
			SequenceCallable pr = new SequenceCallable(seq);
			futures.add(m_service.submit(pr));
		}
		for (Future<U> fu : futures)
		{
			try 
			{
				U u = fu.get();
				m_collectedValues.add(u);
			}
			catch (InterruptedException e) 
			{
				throw new ProcessorException(e);
			}
			catch (ExecutionException e) 
			{
				throw new ProcessorException(e);
			}
		}
		m_combineProcessor.reset();
		SinkLast sink = new SinkLast();
		try 
		{
			Connector.connect(m_combineProcessor, sink);
		}
		catch (ConnectorException e)
		{
			throw new FunctionException(e);
		}
		Pushable p = m_combineProcessor.getPushableInput();
		p.push(m_collectedValues);
		Object[] values = sink.getLast();
		if (values != null)
		{
			return values[0];
		}
		return m_defaultValue;
	}

	protected class SequenceCallable implements Callable<U>
	{
		protected Sequence<T> m_sequence;

		public SequenceCallable(Sequence<T> sequence)
		{
			super();
			m_sequence = sequence;
		}

		@Override
		public U call() throws ConnectorException 
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
			return o;
		}		
	}
}

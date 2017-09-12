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
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static ca.uqac.lif.cep.Connector.INPUT;
import static ca.uqac.lif.cep.Connector.LEFT;
import static ca.uqac.lif.cep.Connector.OUTPUT;
import static ca.uqac.lif.cep.Connector.RIGHT;
import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.Connector.ConnectorException;
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.ProcessorException;
import ca.uqac.lif.cep.Pushable;
import ca.uqac.lif.cep.SingleProcessor;
import ca.uqac.lif.cep.concurrency.ThreadManager;
import ca.uqac.lif.cep.concurrency.ThreadManager.ManagedThread;
import ca.uqac.lif.cep.functions.BinaryFunction;
import ca.uqac.lif.cep.functions.Function;
import ca.uqac.lif.cep.functions.FunctionException;
import ca.uqac.lif.cep.functions.FunctionProcessor;
import ca.uqac.lif.cep.functions.UnaryFunction;
import ca.uqac.lif.cep.tmf.Passthrough;
import ca.uqac.lif.cep.tmf.QueueSink;

/**
 * 
 * @author Sylvain Hallé
 *
 * @param <S> The type of the events in the original traces 
 * @param <T> The type of the events in the traces after pre-processing
 * @param <U> The type of the learned pattern object
 * @param <V> The return type of the trace function
 * @param <W> The output type of the dissimilarity function
 */
public class PatternEventGraph<S,T,U,V,W> extends SingleProcessor
{
	/**
	 * A manager for the threads used in this PEG
	 */
	protected transient ThreadManager m_manager;
	
	/**
	 * A processor that performs a pre-processing of every trace.
	 * Input type: {@code S}, output type: {@code T}. By default,
	 * this processor is the {@code Passthrough}, meaning that no
	 * transformation is done on the traces.
	 */
	protected Processor m_preprocessing = new Passthrough(1);
	
	/**
	 * A function that performs a mining operation on a collection of
	 * sequences of type {@code T}. It returns a pattern, which is an
	 * object of type {@code U}.
	 */
	@SuppressWarnings("rawtypes")
	protected UnaryFunction<Set,U> m_miningFunction;
	
	/**
	 * The pattern learned by the mining function
	 */
	protected U m_pattern;
	
	/**
	 * The instance of the pushable to give the learned pattern to the
	 * dissimilarity function
	 */
	protected Pushable m_patternPushable;
	
	/**
	 * A processor that is evaluated on a single trace. It should accept
	 * events of type {@code T}, and return events of type {@code V}.
	 */
	protected Processor m_traceFunction;
	
	/**
	 * The instance of the pushable to give a new event to the
	 * trace function
	 */
	protected Pushable m_tracePushable;
	
	/**
	 * A function that evaluates how dissimilar is the result of the trace
	 * function (of type {@code V}), with respect to the pattern mined by the
	 * mining function (of type {@code U}). The result is an object of type
	 * {@code W}.
	 */
	protected BinaryFunction<U,V,W> m_dissimilarityFunction;
	
	/**
	 * Compares two objects of type {@code W}. This function is a partial order,
	 * hence it should return an object of type {@code Troolean} (a three-valued
	 * Boolean).
	 */
	protected Function m_partialOrder;
	
	/**
	 * The threshold
	 */
	protected W m_threshold;
	
	/**
	 * The instance of the pushable to give the threshold to the partial ordering
	 */
	protected Pushable m_thresholdPushable;
	
	/**
	 * The queue that will receive the events from the partial order
	 */
	protected Queue<?> m_queue;
	
	/**
	 * Creates a new empty pattern event graph.
	 */
	public PatternEventGraph()
	{
		super(1, 1);
		m_manager = ThreadManager.defaultManager;
	}

	@SuppressWarnings("unchecked")
	public void mine(Set<Sequence<S>> sequences) throws PegException
	{
		Object[] result = new Object[1];
		Set<Sequence<T>> transformed_sequences = new HashSet<Sequence<T>>();
		Lock lock = new ReentrantLock();
		for (Sequence<S> in_seq : sequences)
		{
			PreprocessRunnable pr = new PreprocessRunnable(in_seq, transformed_sequences, lock);
			ManagedThread t = m_manager.tryNewThread(pr);
			if (t == null)
			{
				mine(in_seq, transformed_sequences, lock);
			}
			else
			{
				pr.setThread(t);
				t.start();
			}
		}
		m_manager.waitForAll();
		try
		{
			m_miningFunction.compute(new Object[]{transformed_sequences}, result);
		}
		catch (FunctionException e)
		{
			throw new PegException(e);
		}
		m_pattern = (U) result[0];
	}
	
	protected void mine(Sequence<S> in_seq, Set<Sequence<T>> transformed_sequences, Lock lock)
	{
		Processor pre_proc = m_preprocessing.clone();
		Pushable p = pre_proc.getPushableInput();
		QueueSink sink = new QueueSink();
		try
		{
			Connector.connect(pre_proc, OUTPUT, sink, INPUT);
		}
		catch (ConnectorException e)
		{
			// TODO: Silently fail
			return;
		}
		for (S event : in_seq)
		{
			p.push(event);
		}
		@SuppressWarnings("unchecked")
		Queue<T> queue = (Queue<T>) sink.getQueue();
		Sequence<T> out_seq = new Sequence<T>();
		out_seq.addAll(queue);
		lock.lock();
		transformed_sequences.add(out_seq);
		lock.unlock();
	}
	
	/**
	 * Instantiates the internal piping of all the functions and processors for
	 * the computation of the pattern event graph. This must be called exactly
	 * once, after all the necessary elements have been specified (mining function,
	 * trace function, dissimilarity function, partial order,
	 * pre-processing function).
	 * @throws ConnectorException
	 */
	public void connect() throws ConnectorException
	{
		//Processor p_alpha = m_preprocessing.clone();
		Processor p_beta = m_preprocessing.clone();
		m_tracePushable = p_beta.getPushableInput();
		Connector.connect(p_beta, OUTPUT, m_traceFunction, INPUT);
		FunctionProcessor diss_p = new FunctionProcessor(m_dissimilarityFunction);
		m_patternPushable = diss_p.getPushableInput(LEFT);
		Connector.connect(m_traceFunction, OUTPUT, diss_p, RIGHT);
		FunctionProcessor order_p = new FunctionProcessor(m_partialOrder);
		Connector.connect(diss_p, OUTPUT, order_p, LEFT);
		m_thresholdPushable = order_p.getPushableInput(RIGHT);
		QueueSink sink = new QueueSink(1);
		Connector.connect(order_p, OUTPUT, sink, INPUT);
		m_queue = sink.getQueue();
	}

	@Override
	protected boolean compute(Object[] inputs, Queue<Object[]> outputs) throws ProcessorException
	{
		m_patternPushable.push(m_pattern);
		m_thresholdPushable.push(m_threshold);
		m_tracePushable.push(inputs[0]);
		while (!m_queue.isEmpty())
		{
			Object o = m_queue.remove();
			outputs.add(new Object[]{o});
		}
		return true;
	}

	@Override
	public Processor clone()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Sets the mining function
	 * @param function Must be a function accepting a {@code Set} of {@code Sequence<T>}
	 * objects
	 * @return This PEG
	 */
	@SuppressWarnings("rawtypes")
	public PatternEventGraph<S,T,U,V,W> setMiningFunction(UnaryFunction<Set,U> function)
	{
		m_miningFunction = function;
		return this;
	}
	
	/**
	 * Sets the trace function (i.e. processor)
	 * @param p The processor
	 * @return This PEG
	 */
	public PatternEventGraph<S,T,U,V,W> setTraceProcessor(Processor p)
	{
		m_traceFunction = p;
		return this;
	}
	
	/**
	 * Sets the trace function. This wraps the function into a
	 * {@code FunctionProcessor}.
	 * @param function The function
	 * @return This PEG
	 */
	public PatternEventGraph<S,T,U,V,W> setTraceFunction(UnaryFunction<T,V> function)
	{
		m_traceFunction = new FunctionProcessor(function);
		return this;
	}
	
	/**
	 * Sets the dissimilarity function.
	 * @param function The function
	 * @return This PEG
	 */
	public PatternEventGraph<S,T,U,V,W> setDissimilarityFunction(BinaryFunction<U,V,W> function)
	{
		m_dissimilarityFunction = function;
		return this;
	}
	
	/**
	 * Sets the partial ordering function.
	 * @param function The function. Should resolve to a {@code BinaryFunction<W,W,Troolean>}.
	 * @return This PEG
	 */
	public PatternEventGraph<S,T,U,V,W> setPartialOrder(Function function)
	{
		m_partialOrder = function;
		return this;
	}
	
	/**
	 * Sets the pre-processing function.
	 * @param p The processor
	 * @return This PEG
	 */
	public PatternEventGraph<S,T,U,V,W> setPreprocessing(Processor p)
	{
		m_preprocessing = p;
		return this;
	}
	
	/**
	 * Sets the pre-processing function. This wraps the function into a
	 * {@code FunctionProcessor}.
	 * @param function The function
	 * @return This PEG
	 */
	public PatternEventGraph<S,T,U,V,W> setPreprocessing(UnaryFunction<S,T> function)
	{
		m_preprocessing = new FunctionProcessor(function);
		return this;
	}
	
	/**
	 * Sets the threshold value.
	 * @param t The threshold
	 * @return This PEG
	 */
	public PatternEventGraph<S,T,U,V,W> setThreshold(W t)
	{
		m_threshold = t;
		return this;
	}
	
	/**
	 * Gets the pattern mined from the set of reference sequences
	 * @return The pattern
	 */
	public U getPattern()
	{
		return m_pattern;
	}
	
	/**
	 * Sets the thread manager to be used by this PEG.
	 * @param manager The manager
	 * @return This PEG
	 */
	public PatternEventGraph<S,T,U,V,W> setManager(ThreadManager manager)
	{
		m_manager = manager;
		return this;
	}
	
	protected class PreprocessRunnable implements Runnable
	{
		protected final Sequence<S> m_sequence;
		
		protected final Lock m_lock;
		
		protected final Set<Sequence<T>> m_transformedSequences;
		
		protected ManagedThread m_thread = null;
		
		public PreprocessRunnable(Sequence<S> sequence, Set<Sequence<T>> out_set, Lock lock)
		{
			super();
			m_sequence = sequence;
			m_lock = lock;
			m_transformedSequences = out_set;
		}
		
		public void setThread(ManagedThread thread)
		{
			m_thread = thread;
		}
		
		@Override
		public void run()
		{
			mine(m_sequence, m_transformedSequences, m_lock);
			if (m_thread != null)
			{
				m_thread.dispose();
			}
		}
	}

	@Override
	public void reset()
	{
		super.reset();		
	}
}

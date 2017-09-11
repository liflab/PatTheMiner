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

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.junit.Test;

import static ca.uqac.lif.cep.Connector.INPUT;
import static ca.uqac.lif.cep.Connector.LEFT;
import static ca.uqac.lif.cep.Connector.OUTPUT;
import static ca.uqac.lif.cep.Connector.RIGHT;
import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.Connector.ConnectorException;
import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.ProcessorException;
import ca.uqac.lif.cep.Pushable;
import ca.uqac.lif.cep.UniformProcessor;
import ca.uqac.lif.cep.functions.CumulativeFunction;
import ca.uqac.lif.cep.functions.CumulativeProcessor;
import ca.uqac.lif.cep.functions.FunctionProcessor;
import ca.uqac.lif.cep.functions.FunctionTree;
import ca.uqac.lif.cep.ltl.Troolean;
import ca.uqac.lif.cep.ltl.TrooleanCast;
import ca.uqac.lif.cep.numbers.Addition;
import ca.uqac.lif.cep.numbers.Division;
import ca.uqac.lif.cep.numbers.IsGreaterThan;
import ca.uqac.lif.cep.numbers.IsLessThan;
import ca.uqac.lif.cep.numbers.Subtraction;
import ca.uqac.lif.cep.peg.ml.DistanceToClosest;
import ca.uqac.lif.cep.peg.ml.KMeans;
import ca.uqac.lif.cep.peg.ml.KMeansTest;
import ca.uqac.lif.cep.peg.ml.MeanAndVariance;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.tmf.Passthrough;
import ca.uqac.lif.cep.tmf.QueueSink;

public class PegTest
{
	/**
	 * We create a PEG that compares the length of the current trace to
	 * the average length of the traces in some dummy set.
	 * The PEG should output false
	 * on an input trace, as soon as its length becomes greater than that of
	 * the average. This is done by computing avg - length, and asserting that
	 * the difference is greater than 0.
	 * @throws PegException 
	 * @throws ConnectorException 
	 */
	@Test
	public void testAverageLength() throws PegException, ConnectorException
	{
		Troolean.Value outcome;
		Set<Sequence<Number>> sequences = generateSequences();
		PatternEventGraph<Number,Number,Number,Number,Number> peg = new PatternEventGraph<Number,Number,Number,Number,Number>();
		peg.setMiningFunction(AverageLength.instance);
		peg.setTraceProcessor(new Counter());
		peg.setDissimilarityFunction(Subtraction.instance);
		peg.mine(sequences);
		Number average = peg.getPattern();
		peg.setThreshold(0);
		peg.setPartialOrder(new FunctionTree(TrooleanCast.instance, IsGreaterThan.instance));
		peg.connect();
		QueueSink sink = new QueueSink(1);
		Connector.connect(peg, sink);
		Pushable p = peg.getPushableInput();
		Queue<Object> queue = sink.getQueue();
		for (int i = 0; i < average.intValue(); i++)
		{
			p.push(0);
			outcome = (Troolean.Value) queue.remove();
			assertEquals(Troolean.Value.TRUE, outcome);
		}
		p.push(0);
		outcome = (Troolean.Value) queue.remove();
		assertEquals(Troolean.Value.FALSE, outcome);
	}
	
	/**
	 * We create a PEG that compares the values in the current trace to
	 * the average of the values in the traces from some dummy set.
	 * The PEG should output false
	 * on an input trace, as soon as an event is greater than that
	 * average. This is done by computing avg - val, and asserting that
	 * the difference is greater than 0.
	 * @throws PegException 
	 * @throws ConnectorException 
	 */
	@Test
	public void testAverageValues() throws PegException, ConnectorException
	{
		Troolean.Value outcome;
		Set<Sequence<Number>> sequences = generateSequences();
		PatternEventGraph<Number,Number,Number,Number,Number> peg = new PatternEventGraph<Number,Number,Number,Number,Number>();
		peg.setMiningFunction(AverageValues.instance);
		peg.setTraceProcessor(new Passthrough(1));
		peg.setDissimilarityFunction(Subtraction.instance);
		peg.mine(sequences);
		Number average = peg.getPattern();
		peg.setThreshold(0);
		peg.setPartialOrder(new FunctionTree(TrooleanCast.instance, IsGreaterThan.instance));
		peg.connect();
		QueueSink sink = new QueueSink(1);
		Connector.connect(peg, sink);
		Pushable p = peg.getPushableInput();
		Queue<Object> queue = sink.getQueue();
		float under_val = average.floatValue() - 1;
		for (int i = 0; i < 5; i++)
		{
			p.push(under_val); // Value under the average
			outcome = (Troolean.Value) queue.remove();
			assertEquals(Troolean.Value.TRUE, outcome);
		}
		p.push(under_val + 10); // Value over the average
		outcome = (Troolean.Value) queue.remove();
		assertEquals(Troolean.Value.FALSE, outcome);
	}
	
	/**
	 * We create a PEG that compares the values in the current trace to
	 * the average of the values in the traces from some dummy set.
	 * The PEG should output false
	 * on an input trace, as soon as the running average of the trace is greater 
	 * than that average. This is done by computing avg - run_avg, and asserting that
	 * the difference is greater than 0.
	 * @throws PegException 
	 * @throws ConnectorException 
	 */
	@Test
	public void testAverageRunning() throws PegException, ConnectorException
	{
		Troolean.Value outcome;
		Set<Sequence<Number>> sequences = generateSequences();
		PatternEventGraph<Number,Number,Number,Number,Number> peg = new PatternEventGraph<Number,Number,Number,Number,Number>();
		peg.setMiningFunction(AverageValues.instance);
		GroupProcessor running_average = new GroupProcessor(1, 1);
		{
			Fork f = new Fork(2);
			running_average.associateInput(INPUT, f, INPUT);
			CumulativeProcessor sum = new CumulativeProcessor(new CumulativeFunction<Number>(Addition.instance));
			Counter cnt = new Counter(1);
			Connector.connect(f, LEFT, sum, INPUT);
			Connector.connect(f, RIGHT, cnt, INPUT);
			FunctionProcessor div = new FunctionProcessor(Division.instance);
			Connector.connect(sum, OUTPUT, div, LEFT);
			Connector.connect(cnt, OUTPUT, div, RIGHT);
			running_average.associateOutput(OUTPUT, div, OUTPUT);
		}
		peg.setTraceProcessor(running_average);
		peg.setDissimilarityFunction(Subtraction.instance);
		peg.mine(sequences);
		@SuppressWarnings("unused")
		Number average = peg.getPattern(); // Supposed to be 0.667
		peg.setThreshold(0);
		peg.setPartialOrder(new FunctionTree(TrooleanCast.instance, IsGreaterThan.instance));
		peg.connect();
		QueueSink sink = new QueueSink(1);
		Connector.connect(peg, sink);
		Pushable p = peg.getPushableInput();
		Queue<Object> queue = sink.getQueue();
		p.push(0); // Running avg < 0.667
		outcome = (Troolean.Value) queue.remove();
		assertEquals(Troolean.Value.TRUE, outcome);
		p.push(0.5); // Running avg < 0.667
		outcome = (Troolean.Value) queue.remove();
		assertEquals(Troolean.Value.TRUE, outcome);
		p.push(10); // Running avg > 0.667
		outcome = (Troolean.Value) queue.remove();
		assertEquals(Troolean.Value.FALSE, outcome);
	}
	
	/**
	 * We create a PEG that:
	 * <ol>
	 * <li>Computes the running average and variance of each sequence in a
	 * reference set, resulting in a two-dimensional feature vector</li>
	 * <li>Applies <i>k</i>-means clustering (with <i>k</i>=2) to find the
	 * centroids of this set of feature vectors</li>
	 * <li>On a sequence that is read event by event, returns {@code FALSE}
	 * whenever the feature vector made of its running average/variance is
	 * at an Euclidean distance of more than <i>d<sub>T</sub></i> to the
	 * closest centroid (for some threshold <i>d<sub>T</sub></i>)</li>
	 * </ol>
	 * @throws PegException 
	 * @throws ConnectorException 
	 */
	@SuppressWarnings("rawtypes")
	@Test
	public void testClustering() throws PegException, ConnectorException
	{
		Troolean.Value outcome;
		Set<Sequence<Number>> sequences = KMeansTest.generateSequences();
		PatternEventGraph<Number,DoublePoint,Set,DoublePoint,Number> peg = new PatternEventGraph<Number,DoublePoint,Set,DoublePoint,Number>();
		peg.setPreprocessing(new MeanAndVariance());
		peg.setMiningFunction(new KMeans(2));
		peg.setTraceProcessor(new Passthrough(1));
		peg.setDissimilarityFunction(new DistanceToClosest(new EuclideanDistance()));
		peg.mine(sequences);
		peg.setThreshold(5);
		peg.setPartialOrder(new FunctionTree(TrooleanCast.instance, IsLessThan.instance));
		peg.connect();
		QueueSink sink = new QueueSink(1);
		Connector.connect(peg, sink);
		Pushable p = peg.getPushableInput();
		Queue<Object> queue = sink.getQueue();
		p.push(1);
		outcome = (Troolean.Value) queue.remove();
		assertEquals(Troolean.Value.TRUE, outcome);
		p.push(1);
		outcome = (Troolean.Value) queue.remove();
		assertEquals(Troolean.Value.TRUE, outcome);
		p.push(1);
		outcome = (Troolean.Value) queue.remove();
		assertEquals(Troolean.Value.TRUE, outcome);
		// Now we push events that will slowly pull the running average
		// away from the cluster centroids, so that the distance to the closest
		// center becomes larger than the threshold
		p.push(5);
		outcome = (Troolean.Value) queue.remove(); // avg = 2, close enough
		assertEquals(Troolean.Value.TRUE, outcome);
		p.push(5);
		outcome = (Troolean.Value) queue.remove(); // avg = 2.6, still close enough
		assertEquals(Troolean.Value.TRUE, outcome);
		p.push(23);
		outcome = (Troolean.Value) queue.remove(); // avg = 6, too far
		assertEquals(Troolean.Value.FALSE, outcome);
	}
	
	public static Set<Sequence<Number>> generateSequences()
	{
		Set<Sequence<Number>> sequences = new HashSet<Sequence<Number>>();
		for (int i = 1; i <= 3; i++)
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
	}
}

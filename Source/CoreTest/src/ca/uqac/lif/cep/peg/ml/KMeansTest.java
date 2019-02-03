/*
    A BeepBeep palette for mining event traces
    Copyright (C) 2017-2019 Sylvain Hallé and friends

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

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.junit.Test;

import ca.uqac.lif.cep.functions.FunctionException;
import ca.uqac.lif.cep.peg.Sequence;

/**
 * Unit tests using the k-means algorithm.
 */
public class KMeansTest
{
	@SuppressWarnings("unchecked")
	@Test
	public void test1() throws FunctionException
	{
		Set<Sequence<Number>> sequences = generateSequences();
		Set<Sequence<DoublePoint>> sequences_points = new HashSet<Sequence<DoublePoint>>();
		for (Sequence<Number> s_num : sequences)
		{
			// Compute mean and variance of each sequence
			ProcessorVectorFunction<Number> pvf = new ProcessorVectorFunction<Number>(new MeanAndVariance());
			Object[] points = new Object[1];
			Object[] inputs = new Object[1];
			inputs[0] = s_num;
			pvf.evaluate(inputs, points);
			sequences_points.add((Sequence<DoublePoint>) points[0]);
		}
		KMeans km = new KMeans(2);
		Object[] km_outputs = new Object[1];
		km.evaluate(new Object[]{sequences_points}, km_outputs);
		assertTrue(km_outputs[0] instanceof Set);
		Set<DoublePoint> centers = (Set<DoublePoint>) km_outputs[0];
		assertEquals(2, centers.size());
	}
	
	/**
	 * Generates a set of "fake" sequences of numbers, that are artificially
	 * grouped around two mean values: close to 1, and close to 100.
	 * @return
	 */
	public static Set<Sequence<Number>> generateSequences()
	{
		Set<Sequence<Number>> seqs = new HashSet<Sequence<Number>>();
		for (int i = 0; i < 3; i++)
		{
			Sequence<Number> seq = new Sequence<Number>();
			for (int j = 0; j < 3; j++)
			{
				seq.add(i * j);
			}
			seqs.add(seq);
		}
		for (int i = 0; i < 3; i++)
		{
			Sequence<Number> seq = new Sequence<Number>();
			for (int j = 0; j < 3; j++)
			{
				seq.add(100 + (i * j));
			}
			seqs.add(seq);
		}
		return seqs;
	}
}

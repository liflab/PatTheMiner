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

import static org.junit.Assert.assertEquals;

import java.util.Queue;

import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.junit.Test;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.Pushable;
import ca.uqac.lif.cep.Connector.ConnectorException;
import ca.uqac.lif.cep.tmf.QueueSink;

/**
 * Unit tests for the {@link MeanAndVariance} processor.
 */
public class MeanAndVarianceTest
{
	@Test
	public void test1() throws ConnectorException
	{
		MeanAndVariance sm = new MeanAndVariance();
		Pushable p = sm.getPushableInput();
		QueueSink sink = new QueueSink();
		Connector.connect(sm, 0, sink, 0);
		Queue<Object> queue = sink.getQueue();
		DoublePoint n;
		double[] n_vals;
		p.push(0);
		n = (DoublePoint) queue.remove();
		n_vals = n.getPoint();
		assertEquals(0f, n_vals[0], 0);
		assertEquals(0f, n_vals[1], 0);
		p.push(2);
		n = (DoublePoint) queue.remove();
		n_vals = n.getPoint();
		assertEquals(1f, n_vals[0], 0);
		assertEquals(1f, n_vals[1], 0);
		p.push(4);
		n = (DoublePoint) queue.remove();
		n_vals = n.getPoint();
		assertEquals(2f, n_vals[0], 0);
		assertEquals(2.67f, n_vals[1], 0.01f);
		p.push(4);
		n = (DoublePoint) queue.remove();
		n_vals = n.getPoint();
		assertEquals(2.5f, n_vals[0], 0);
		assertEquals(2.75f, n_vals[1], 0);
	}
}

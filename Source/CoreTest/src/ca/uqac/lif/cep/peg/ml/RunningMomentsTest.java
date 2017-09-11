package ca.uqac.lif.cep.peg.ml;

import static org.junit.Assert.assertEquals;

import java.util.Queue;

import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.junit.Test;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.Pushable;
import ca.uqac.lif.cep.Connector.ConnectorException;
import ca.uqac.lif.cep.tmf.QueueSink;

public class RunningMomentsTest
{
	@Test
	public void test1() throws ConnectorException
	{
		RunningMoments sm = new RunningMoments(2);
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
		assertEquals(2f, n_vals[1], 0);
		p.push(4);
		n = (DoublePoint) queue.remove();
		n_vals = n.getPoint();
		assertEquals(2f, n_vals[0], 0);
		assertEquals(6.67f, n_vals[1], 0.01f);
		p.push(4);
		n = (DoublePoint) queue.remove();
		n_vals = n.getPoint();
		assertEquals(2.5f, n_vals[0], 0);
		assertEquals(9f, n_vals[1], 0);
	}
}

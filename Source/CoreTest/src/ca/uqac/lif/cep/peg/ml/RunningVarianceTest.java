package ca.uqac.lif.cep.peg.ml;

import static org.junit.Assert.*;

import java.util.Queue;

import org.junit.Test;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.Connector.ConnectorException;
import ca.uqac.lif.cep.Pushable;
import ca.uqac.lif.cep.tmf.QueueSink;

public class RunningVarianceTest
{
	@Test
	public void test1() throws ConnectorException
	{
		RunningVariance sm = new RunningVariance();
		Pushable p = sm.getPushableInput();
		QueueSink sink = new QueueSink();
		Connector.connect(sm, 0, sink, 0);
		Queue<Object> queue = sink.getQueue();
		Number n;
		p.push(0);
		n = (Number) queue.remove();
		assertEquals(0f, n.floatValue(), 0);
		p.push(2);
		n = (Number) queue.remove();
		assertEquals(1f, n.floatValue(), 0);
		p.push(4);
		n = (Number) queue.remove();
		assertEquals(2.67f, n.floatValue(), 0.01f);
		p.push(4);
		n = (Number) queue.remove();
		assertEquals(2.75f, n.floatValue(), 0);
	}
}

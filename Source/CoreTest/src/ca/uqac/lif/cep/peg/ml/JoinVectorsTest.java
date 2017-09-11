package ca.uqac.lif.cep.peg.ml;

import static org.junit.Assert.*;

import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.junit.Test;

import ca.uqac.lif.cep.functions.FunctionException;

public class JoinVectorsTest
{
	@Test
	public void testEmpty() throws FunctionException
	{
		JoinVectors jv = new JoinVectors(0);
		DoublePoint[] inputs = new DoublePoint[0];
		DoublePoint[] outputs = new DoublePoint[1];
		jv.evaluate(inputs, outputs);
		assertTrue(outputs[0] instanceof DoublePoint);
		DoublePoint dp = outputs[0];
		assertEquals(0, dp.getPoint().length);
	}
	
	@Test
	public void testOne() throws FunctionException
	{
		JoinVectors jv = new JoinVectors(1);
		DoublePoint[] inputs = new DoublePoint[1];
		inputs[0] = new DoublePoint(new double[]{0});
		DoublePoint[] outputs = new DoublePoint[1];
		jv.evaluate(inputs, outputs);
		assertTrue(outputs[0] instanceof DoublePoint);
		double[] points = outputs[0].getPoint();
		assertEquals(1, points.length);
		assertEquals(0, points[0], 0);
	}
	
	@Test
	public void testTwo() throws FunctionException
	{
		JoinVectors jv = new JoinVectors(2);
		DoublePoint[] inputs = new DoublePoint[2];
		inputs[0] = new DoublePoint(new double[]{0});
		inputs[1] = new DoublePoint(new double[]{1, 2});
		DoublePoint[] outputs = new DoublePoint[1];
		jv.evaluate(inputs, outputs);
		assertTrue(outputs[0] instanceof DoublePoint);
		double[] points = outputs[0].getPoint();
		assertEquals(3, points.length);
		assertEquals(0, points[0], 0);
		assertEquals(1, points[1], 0);
		assertEquals(2, points[2], 0);
	}
}

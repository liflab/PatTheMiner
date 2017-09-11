package ca.uqac.lif.cep.peg.ml;

import java.util.Set;

import org.apache.commons.math3.ml.clustering.DoublePoint;

import ca.uqac.lif.cep.Context;
import ca.uqac.lif.cep.functions.Function;
import ca.uqac.lif.cep.functions.FunctionException;

public class JoinVectors extends Function
{
	/**
	 * The input arity of this function
	 */
	protected int m_inArity;
	
	/**
	 * The size of the resulting vector
	 */
	protected int m_size = -1;
	
	/**
	 * Creates a new instance of the function
	 * @param arity The number of vectors that will be joined together
	 */
	public JoinVectors(int arity)
	{
		super();
		m_inArity = arity;
	}

	@Override
	public void evaluate(Object[] inputs, Object[] outputs, Context context) throws FunctionException
	{
		// We ignore the context
		evaluate(inputs, outputs);
	}

	@Override
	public void evaluate(Object[] inputs, Object[] outputs) throws FunctionException
	{
		if (m_size < 0)
		{
			// We compute the total size of the output vector only the first time
			m_size = 0;
			for (int i = 0; i < m_inArity; i++)
			{
				m_size += ((DoublePoint) inputs[i]).getPoint().length;
			}
		}
		double[] values = new double[m_size];
		int j = 0;
		for (int i = 0; i < m_inArity; i++)
		{
			double[] point_val = ((DoublePoint) inputs[i]).getPoint();
			for (double d : point_val)
			{
				values[j++] = d;
			}
		}
		outputs[0] = new DoublePoint(values);
	}

	@Override
	public int getInputArity()
	{
		return m_inArity;
	}

	@Override
	public int getOutputArity()
	{
		return 1;
	}

	@Override
	public void reset()
	{
		m_size = -1;
	}

	@Override
	public JoinVectors clone()
	{
		return new JoinVectors(m_inArity);
	}

	@Override
	public void getInputTypesFor(Set<Class<?>> classes, int index)
	{
		classes.add(DoublePoint.class);
	}

	@Override
	public Class<?> getOutputTypeFor(int index)
	{
		return DoublePoint.class;
	}

}

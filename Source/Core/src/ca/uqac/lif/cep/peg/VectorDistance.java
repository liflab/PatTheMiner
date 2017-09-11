package ca.uqac.lif.cep.peg;

import java.util.Vector;

import ca.uqac.lif.cep.functions.BinaryFunction;

@SuppressWarnings("rawtypes")
public abstract class VectorDistance extends BinaryFunction<Vector,Vector,Number>
{
	protected VectorDistance()
	{
		super(Vector.class, Vector.class, Number.class);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Number getValue(Vector x, Vector y)
	{
		Vector<Number> v1 = (Vector<Number>) x;
		Vector<Number> v2 = (Vector<Number>) y;
		return compute(v1, v2);
	}
	
	public abstract Number compute(Vector<Number> v1, Vector<Number> v2);
}

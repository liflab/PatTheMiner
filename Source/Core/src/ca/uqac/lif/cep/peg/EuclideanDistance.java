package ca.uqac.lif.cep.peg;

import java.util.Vector;

public class EuclideanDistance extends VectorDistance
{
	public static final EuclideanDistance instance = new EuclideanDistance();
	
	private EuclideanDistance()
	{
		super();
	}

	@Override
	public Number compute(Vector<Number> v1, Vector<Number> v2)
	{
		float d = 0;
		int dim = Math.min(v1.size(), v2.size());
		for (int i = 0; i < dim; i++)
		{
			float val_1 = v1.get(i).floatValue();
			float val_2 = v2.get(i).floatValue();
			d += Math.pow(val_2 - val_1, 2);
		}
		return Math.sqrt(d);
	}

}

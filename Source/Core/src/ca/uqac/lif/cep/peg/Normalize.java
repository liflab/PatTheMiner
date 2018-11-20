package ca.uqac.lif.cep.peg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ca.uqac.lif.cep.functions.NothingToReturnException;
import ca.uqac.lif.cep.functions.UnaryFunction;

/**
 * Normalizes a collection of numbers
 * @author Sylvain Hallé
 */
public class Normalize extends UnaryFunction<Object,Object> 
{
	public static final transient Normalize instance = new Normalize();
	
	protected Normalize()
	{
		super(Object.class, Object.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getValue(Object x) 
	{
	  if (x instanceof Map)
	  {
	    Map<?,?> m = (Map<?,?>) x;
	    Map<Object,Number> out = new HashMap<Object,Number>();
	    float total = 0f;
	    for (Object o : m.values())
	    {
	      total += ((Number) o).floatValue();
	    }
	    if (total == 0)
	    {
	      return out;
	    }
	    for (Map.Entry<?,?> e : m.entrySet())
	    {
	      out.put(e.getKey(), ((Number) e.getValue()).floatValue() / total); 
	    }
	    return out;
	  }
		if (x instanceof Collection)
		{
			Collection<Number> l1 = (Collection<Number>) x;
			List<Number> l2 = new ArrayList<Number>(l1.size());
			float sum = 0;
			for (Number n : l1)
			{
				sum += n.floatValue();
			}
			if (sum == 0)
			{
				throw new NothingToReturnException(this);
			}
			for (Number n : l1)
			{
				l2.add(n.floatValue() / sum);
			}
			return l2;
		}
		return null;
	}

}

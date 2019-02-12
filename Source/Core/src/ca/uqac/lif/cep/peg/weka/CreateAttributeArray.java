package ca.uqac.lif.cep.peg.weka;

import ca.uqac.lif.cep.functions.Function;
import ca.uqac.lif.cep.functions.UnaryFunction;

/**
 * Creates an array of attribute values by successively applying a list
 * of functions on a single input object.
 * 
 * @param <T> The type of the input 
 */
public class CreateAttributeArray<T> extends UnaryFunction<T,Object[]>
{
  /**
   * An array storing the function to apply in order to compute each
   * feature value of the output array.
   */
  /*@ non_null @*/ protected Function[] m_features;
  
  /**
   * Creates a new instance of the function.
   * @param clazz The type of the input argument
   * @param features A list of functions; each must have unary input. The
   * <i>n</i>-th function will be called to generate the <i>n</i>-th value of
   * the output array. If a function does not have unary output, only the first
   * component of the output will be used.
   */
  public CreateAttributeArray(Class<T> clazz, Function ... features)
  {
    super(clazz, Object[].class);
    m_features = features;
  }

  @Override
  public Object[] getValue(T x)
  {
    Object[] out_array = new Object[m_features.length];
    Object[] ins = new Object[] {x};
    for (int i = 0; i < m_features.length; i++)
    {
      Object[] outs = new Object[1];
      m_features[i].evaluate(ins, outs);
      out_array[i] = outs[0];
    }
    return out_array;
  }
}

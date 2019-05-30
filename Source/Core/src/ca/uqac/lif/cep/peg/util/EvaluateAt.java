package ca.uqac.lif.cep.peg.util;

import ca.uqac.lif.cep.Context;
import ca.uqac.lif.cep.Connector.Variant;
import ca.uqac.lif.cep.functions.Function;
import java.util.Set;

public class EvaluateAt extends Function
{
  protected Object[] m_inputValues;
  
  public EvaluateAt(Object ... input_values)
  {
    super();
    m_inputValues = input_values;
  }

  @Override
  public void getInputTypesFor(Set<Class<?>> classes, int index)
  {
    if (index == 0)
    {
      classes.add(Function.class);
    }
  }

  @Override
  public void evaluate(Object[] inputs, Object[] outputs, Context context)
  {
    Function fct = (Function) inputs[0];
    fct.evaluate(m_inputValues, outputs, context);
  }

  @Override
  public int getInputArity()
  {
    return 1;
  }

  @Override
  public int getOutputArity()
  {
    return 1;
  }

  @Override
  public Class<?> getOutputTypeFor(int index)
  {
    return Variant.class;
  }

  @Override
  public EvaluateAt duplicate(boolean with_state)
  {
    return this;
  }

}

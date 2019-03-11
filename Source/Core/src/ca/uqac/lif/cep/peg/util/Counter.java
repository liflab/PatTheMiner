package ca.uqac.lif.cep.peg.util;

import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.UniformProcessor;

public class Counter extends UniformProcessor
{
  /**
   * The current value of this counter
   */
  protected int m_value = 0;
  
  public Counter(int start_value)
  {
    super(1, 1);
    m_value = start_value;
  }
  
  @Override
  protected boolean compute(Object[] inputs, Object[] outputs)
  {
    outputs[0] = m_value++;
    return true;
  }

  @Override
  public Processor duplicate(boolean with_state)
  {
    if (with_state)
    {
      return new Counter(m_value);
    }
    return new Counter(0);
  }

}

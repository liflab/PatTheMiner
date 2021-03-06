/*
    A BeepBeep palette for mining event traces
    Copyright (C) 2017-2019 Sylvain Hallé and friends

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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

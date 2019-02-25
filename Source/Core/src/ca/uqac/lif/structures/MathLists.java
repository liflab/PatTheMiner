/*
    A BeepBeep palette for mining event traces
    Copyright (C) 2017-2019 Sylvain Hallé

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
package ca.uqac.lif.structures;

import ca.uqac.lif.cep.UniformProcessor;
import java.util.ArrayList;
import java.util.List;

/**
 * A container object for functions and processors applying to lists.
 * 
 * @author Sylvain Hallé
 */
public class MathLists
{
  private MathLists()
  {
    // Utility class
  }

  /**
   * Processor that updates a list
   * @since 0.10.2
   */
  protected abstract static class MathListUpdateProcessor extends UniformProcessor
  {
    /**
     * The underlying list
     */
    protected MathList<Object> m_list;

    /**
     * Create a new instance of the processor
     */
    public MathListUpdateProcessor()
    {
      super(1, 1);
      m_list = new MathList<Object>();
    }

    @Override
    public void reset()
    {
      super.reset();
      m_list.clear();
    }

    @Override
    public Class<?> getOutputType(int index)
    {
      return List.class;
    }
  }

  /**
   * Updates a list.
   * @since 0.10.2
   */
  public static class PutInto extends MathListUpdateProcessor
  {
    /**
     * Create a new instance of the processor
     */
    public PutInto()
    {
      super();
    }

    @Override
    public PutInto duplicate(boolean with_state)
    {
      PutInto pi = new PutInto();
      if (with_state)
      {
        pi.m_list.addAll(m_list);
      }
      return pi;
    }

    @Override
    protected boolean compute(Object[] inputs, Object[] outputs)
    {
      m_list.add(inputs[0]);
      outputs[0] = m_list;
      return true;
    }
  }

  /**
   * Updates a list.
   * @since 0.10.2
   */
  public static class PutIntoNew extends MathListUpdateProcessor
  {
    /**
     * Create a new instance of the processor
     */
    public PutIntoNew()
    {
      super();
    }

    @Override
    public PutIntoNew duplicate(boolean with_state)
    {
      PutIntoNew pi = new PutIntoNew();
      if (with_state)
      {
        pi.m_list.addAll(m_list);
      }
      return pi;
    }

    @Override
    protected boolean compute(Object[] inputs, Object[] outputs)
    {
      m_list.add(inputs[0]);
      ArrayList<Object> new_set = new ArrayList<Object>();
      new_set.addAll(m_list);
      outputs[0] = new_set;
      return true;
    }
  }
}

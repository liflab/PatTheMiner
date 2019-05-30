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

import ca.uqac.lif.cep.functions.Function;
import ca.uqac.lif.cep.functions.UnaryFunction;

import java.util.Map;

/**
 * Turns a {@link Map} object into a {@link Function} that fetches the
 * value associated to its argument in the map.
 */
@SuppressWarnings("rawtypes")
public class MapToFunction extends UnaryFunction<Map,Function>
{
  public static transient MapToFunction instance = new MapToFunction();
  
  MapToFunction()
  {
    super(Map.class, Function.class);
  }
  
  @Override
  public MapFunction getValue(Map x)
  {
    return new MapFunction(x);
  }

  protected class MapFunction extends UnaryFunction<Object,Object>
  {
    protected Map<?,?> m_map;
    
    public MapFunction(Map<?,?> map)
    {
      super(Object.class, Object.class);
      m_map = map;
    }

    @Override
    public Object getValue(Object x)
    {
      if (!m_map.containsKey(x))
      {
        return m_map.get(x);
      }
      return null;
    }
  }
}

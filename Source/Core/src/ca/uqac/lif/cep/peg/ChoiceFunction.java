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
package ca.uqac.lif.cep.peg;

import java.util.Map;
import ca.uqac.lif.cep.functions.UnaryFunction;

/**
 * Choice function - takes as constructor parameters t, u, base and map. t =
 * input type (a.k.a. type of the keys inside the HashMap that will be passed) u
 * = output type (a.k.a. type of the values inside the HashMAp that will be
 * passed) base = the function which will compare keys in order to return a
 * value map = HashMap that is passed that will live inside the function's body.
 * <p>
 * The Choice Function should take a value that is of the same type as the map's
 * keys and compare this value with each key. Depending on the base function, it
 * can then return the value associated with the picked key.
 * <p>
 * An example to illustrate - Choice Function receives a time stamp - 4PM. It
 * will then compare this context to each and every key in the map (say the map
 * has two keys which are intervals, 8AM-5PM and 5PM-8AM). Also, let's say our
 * base function is one which find the closest key and returns it. We can then
 * find the key which resembles most 4PM and return this key's value as output.
 * 
 * [8AM-5PM] => 1000 [5PM-8AM] => 500
 * 
 * In this case, the base function would return the key [8AM-5PM] and the
 * ChoiceFunction would send 1000 as output to the processor.
 * 
 * @author Alexandre Larouche
 */
public class ChoiceFunction<Q,R> extends UnaryFunction<Q,R>
{
  /**
   * An associative map between contexts (keys) and their associated
   * trends (values) 
   */
  protected Map<Q,R> m_choices;
  
  /**
   * A default value to return if the computed context is not any of the map's
   * keys
   */
  protected R m_default;

  /**
   * Creates a new instance of a choice function
   * @param clazz_q The class for the keys of the map
   * @param clazz_r The class for the values of the map
   * @param map The associative map between contexts (keys) and their associated
   * trends (values) 
   * @param def_value A default value to return if the computed context is not
   * any of the map's keys. This value is assumed to be <tt>null</tt> if not
   * specified.
   */
  public ChoiceFunction(Class<Q> clazz_q, Class<R> clazz_r, Map<Q,R> map, R def_value)
  {
    super(clazz_q, clazz_r);
    m_choices = map;
    m_default = def_value;
  }
  
  /**
   * Creates a new instance of a choice function
   * @param clazz_q The class for the keys of the map
   * @param clazz_r The class for the values of the map
   * @param map The associative map between contexts (keys) and their associated
   * trends (values) 
   * @param def_value A default value to return if the computed context is not
   * any of the map's keys
   */
  public ChoiceFunction(Class<Q> clazz_q, Class<R> clazz_r, Map<Q,R> map)
  {
    this(clazz_q, clazz_r, map, null);
  }

  @Override
  public R getValue(Q x)
  {
    if (!m_choices.containsKey(x))
    {
      return m_default;
    }
    return m_choices.get(x);
  }
}

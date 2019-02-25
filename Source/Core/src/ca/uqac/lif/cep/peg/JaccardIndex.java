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
package ca.uqac.lif.cep.peg;

import ca.uqac.lif.cep.functions.BinaryFunction;
import java.util.Collection;

/**
 * Computes the Jaccard index of two collections.
 * <p>
 * <strong>Note:</strong> the elements in the two underlying collections
 * must have a proper definition of {{@link #equals(Object)} and
 * {@link #hashCode()}; otherwise, incorrect indices may be calculated.
 */
@SuppressWarnings("rawtypes")
public class JaccardIndex extends BinaryFunction<Collection,Collection,Number>
{
  /**
   * A reference to a single instance of the function
   */
  public static final transient JaccardIndex instance = new JaccardIndex();
  
  JaccardIndex()
  {
    super(Collection.class, Collection.class, Number.class);
  }

  @Override
  public Number getValue(Collection x, Collection y)
  {
    int cap_size = 0, cup_size = 0;
    for (Object o : x)
    {
      cup_size++;
      if (y.contains(o))
      {
        cap_size++;
      }
    }
    for (Object o : y)
    {
      if (!x.contains(o))
      {
        cup_size++;
      }
    }
    if (cup_size == 0)
    {
      return 0;
    }
    return (float) cap_size / (float) cup_size;
  }
}

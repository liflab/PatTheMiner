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
package ca.uqac.lif.cep.peg.weka;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 * A set of utility methods for manipulating Weka objects.
 */
public class WekaUtils
{
  private WekaUtils()
  {
    // Utility class, don't instantiate!
    throw new UnsupportedOperationException();
  }
  
  /**
   * Creates a discrete attribute by giving its name and the list of possible
   * values. This is a utility method meant to simplify the creation of
   * discrete attributes, which is a bit verbose when directly using Weka's
   * objects and methods. 
   * @param name The attribute's name
   * @param values The possible values for the attribute
   * @return
   */
  /*@ non_null @*/ public static Attribute createAttribute(/*@ non_null @*/ String name, String ... values)
  {
    FastVector vec = new FastVector(values.length);
    for (String val : values)
    {
      vec.addElement(val);
    }
    Attribute att = new Attribute(name, vec);
    return att;
  }

  /**
   * Creates an {@link Instance} from an array of values
   * @param dataset The dataset linked to this instance
   * @param array The array of values. The array can contain <tt>null</tt>
   * elements; these will be interpreted as missing values.
   * @return The {@link Instance}
   */
  /*@ requires array.length == m_attributes.length @*/
  /*@ non_null @*/ public static Instance createInstanceFromArray(
      /*@ non_null @*/ Instances dataset, /*@ non_null @*/ Object[] array, 
      Attribute ... attributes)
  {
    Instance ins = new Instance(attributes.length);
    ins.setDataset(dataset);
    for (int i = 0; i < attributes.length; i++)
    {
      Object o = array[i];
      if (o == null)
      {
        // Interpret nulls as missing values
        ins.setMissing(attributes[i]);
      }
      // Must cast the element of the array to either a double or a String
      else if (o instanceof Number)
      {
        ins.setValue(attributes[i], ((Number) o).doubleValue());
      }
      else
      {
        ins.setValue(attributes[i], o.toString());
      }
    }
    return ins;
  }
  
  /**
   * Gets the name of a class based on its index. By default, a Weka
   * {@link Classifier} returns a <tt>double</tt> value when given an instance;
   * this value corresponds to the index of the value in the attribute's
   * list of possible values.
   * @param d The number returned by the classifier for a given instance
   * @param attributes The list of attributes used by the classifier. The
   * class attribute is taken to be the last argument.
   * @return The name of the class. The value <tt>null</tt> may be returned
   * if the position <tt>d</tt> does not correspond to a valid index for
   * the class attribute.
   */
  /*@ null @*/ public static String getClassValue(double d, Attribute ...attributes)
  {
    Attribute att = attributes[attributes.length - 1];
    return att.value((int) d); 
  }
}

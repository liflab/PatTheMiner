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
}

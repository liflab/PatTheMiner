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

import ca.uqac.lif.cep.functions.BinaryFunction;
import ca.uqac.lif.cep.functions.FunctionException;
import ca.uqac.lif.cep.functions.UnaryFunction;
import java.util.Collection;
import java.util.Enumeration;
import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.Drawable;
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

  public static class EvaluateClassifier extends BinaryFunction<Classifier,Object,String>
  {
    /*@ non_null @*/ protected Instances m_dataSet;

    /*@ non_null @*/ protected Attribute[] m_attributes;

    public EvaluateClassifier(/*@ non_null @*/ Instances dataset, Attribute ... attributes)
    {
      super(Classifier.class, Object.class, String.class);
      m_dataSet = dataset;
      m_attributes = attributes;
    }

    @Override
    public String getValue(Classifier c, Object y)
    {
      Instance ins = WekaUtils.createInstanceFromArray(m_dataSet, createArray(null, y), m_attributes);
      try
      {
        double d = c.classifyInstance(ins);
        return getClassValue(d, m_attributes);
      }
      catch (Exception e)
      {
        throw new FunctionException(e);
      }
    }

    @Override
    public EvaluateClassifier duplicate(boolean with_state)
    {
      return this;
    }
  }

  /**
   * Special-purpose function that merges a scalar value <i>x</i> and a
   * collection <i>y</i> of size <i>n</i> and a* scalar value <i>x</i> into an
   * array of size <i>n</i>+1 where
   * <i>x</i> is placed at the last position. This is just a {@link Function}
   * wrapper around {@link WekaUtils#createArray(Object, Object)}.
   */
  public static class MergeIntoArray extends BinaryFunction<Object,Object,Object[]>
  {
    /**
     * A reference to a single instance of the function
     */
    public static final transient MergeIntoArray instance = new MergeIntoArray();

    protected MergeIntoArray()
    {
      super(Object.class, Object.class, Object[].class);
    }

    @Override
    public Object[] getValue(Object x, Object y)
    {
      return createArray(x, y);
    }
  }

  /**
   * Special-purpose function that merges a scalar value <i>x</i> and a
   * collection <i>y</i> of size <i>n</i> and a* scalar value <i>x</i> into an
   * array of size <i>n</i>+1 where
   * <i>x</i> is placed at the last position.
   * @param x The scalar value
   * @param y The array
   */
  public static Object[] createArray(Object x, Object y)
  {
    if (y instanceof Collection<?>)
    {
      Collection<?> c = (Collection<?>) y;
      Object[] z = new Object[c.size() + 1];
      int i = 0;
      for (Object o : c)
      {
        z[i] = o;
        i++;
      }
      z[i] = x;
      return z;
    }
    if (y.getClass().isArray())
    {
      Object[] c = (Object[]) y;
      Object[] z = new Object[c.length + 1];
      for (int i = 0; i < c.length; i++)
      {
        z[i] = c[i];
      }
      z[c.length] = x;
      return z;
    }
    return new Object[] {y, x};    
  }

  /**
   * Creates a Weka {@link Instances} object for use with a classifier.
   * @param dataset_name The name of the dataset
   * @param capacity A capacity for the {@link Instances} object
   * @param attributes THe list of attributes to use in this dataset
   * @return The correctly instantiated {@link Instances} object
   */
  /*@ requires capacity > 0 @*/
  /*@ non_null @*/ public static Instances createInstances(/*@ non_null @*/ String dataset_name, 
      int capacity, Attribute ... attributes)
  {
    FastVector att_info = new FastVector();
    for (Attribute att : attributes)
    {
      att_info.addElement(att);
    }
    Instances instances = new Instances(dataset_name, att_info, capacity);
    // By convention, the last attribute is the class
    instances.setClassIndex(attributes.length - 1);
    return instances;
  }

  public static class GetGraph extends UnaryFunction<Drawable,String>
  {
    public static final transient GetGraph instance = new GetGraph();

    private GetGraph()
    {
      super(Drawable.class, String.class);
    }

    @Override
    public String getValue(Drawable x)
    {
      try
      {
        return x.graph();
      }
      catch (Exception e)
      {
        throw new FunctionException(e);
      }
    }
  }

  /**
   * Gets a "vanilla" instance of a classifier object based on its generic
   * name.
   * @param name The name of the classifier
   * @return An instance of the classifier, or <tt>null</tt> if the given
   * name does not correspond to a supported classifier.
   */
  /*@ null @*/ public static Classifier getClassifier(/*@ non_null @*/ String name)
  {
    if (name.compareTo("J48") == 0)
    {
      return new J48();
    }
    return null;
  }
  
  /**
   * Counts the instances in a dataset
   * @param i The dataset
   * @return The number of instances
   */
  public static int countInstances(/*@ non_null @*/ Instances i)
  {
    int n = 0;
    @SuppressWarnings("rawtypes")
    Enumeration en = i.enumerateInstances();
    while (en.hasMoreElements())
    {
      en.nextElement();
      n++;
    }
    return n;
  }
}

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
package ca.uqac.lif.cep.peg.ml;

import ca.uqac.lif.cep.ProcessorException;
import ca.uqac.lif.cep.UniformProcessor;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class UpdateClassifier extends UniformProcessor
{
  /**
   * A name given to the dataset
   */
  /*@ non_null @*/ protected String m_dataSetName;

  /**
   * The set of instances
   */
  /*@ non_null @*/ protected Instances m_instances;

  /**
   * A capacity value, necessary when creating the {@link Instances}
   * collection.
   */
  protected static final transient int s_capacity = 10;

  /**
   * The list of attributes that are given to the classifier
   */
  /*@ non_null @*/ protected final Attribute[] m_attributes;

  /**
   * The classifier that will be applied on the instances
   */
  /*@ non_null @*/ protected Classifier m_classifier;

  public UpdateClassifier(/*@ non_null @*/ Classifier c, /*@ non_null @*/ String name, Attribute ... attributes)
  {
    super(1, 1);
    m_classifier = c;
    m_dataSetName = name;
    m_attributes = attributes;
    FastVector att_info = new FastVector();
    for (Attribute att : attributes)
    {
      att_info.addElement(att);
    }
    m_instances = new Instances(name, att_info, s_capacity);
    // By convention, the last attribute is the class
    m_instances.setClassIndex(attributes.length - 1);
  }

  @Override
  protected boolean compute(Object[] inputs, Object[] outputs)
  {
    Object[] input_array = (Object[]) inputs[0];
    Instance new_instance = createInstanceFromArray(input_array);
    m_instances.add(new_instance);
    try
    {
      m_classifier.buildClassifier(m_instances);
    }
    catch (Exception e)
    {
      throw new ProcessorException(e);
    }
    outputs[0] = m_classifier;
    return true;
  }

  /**
   * Creates an {@link Instance} from an array of values
   * @param array The array of values. The array can contain <tt>null</tt>
   * elements; these will be interpreted as missing values.
   * @return The {@link Instance}
   */
  /*@ requires array.length == m_attributes.length @*/
  /*@ pure non_null @*/ protected Instance createInstanceFromArray(/*@ non_null @*/ Object[] array)
  {
    Instance ins = new Instance(m_attributes.length);
    for (int i = 0; i < m_attributes.length; i++)
    {
      Object o = array[i];
      if (o == null)
      {
        // Interpret nulls as missing
        ins.setMissing(m_attributes[i]);
      }
      // Must cast the element of the array to either a double or a String
      else if (o instanceof Number)
      {
        ins.setValue(m_attributes[i], ((Number) o).doubleValue());
      }
      else
      {
        ins.setValue(m_attributes[i], o.toString());
      }
    }
    return ins;
  }

  @Override
  public UpdateClassifier duplicate(boolean with_state)
  {
    Classifier new_c = null;
    try
    {
      new_c = Classifier.makeCopy(m_classifier);
    }
    catch (Exception e)
    {
      throw new UnsupportedOperationException("Cannot make a copy of classifier " + m_classifier);
    }
    UpdateClassifier uc = new UpdateClassifier(new_c, m_dataSetName, m_attributes);
    if (with_state)
    {
      uc.m_instances = new Instances(m_instances);
    }
    return uc;
  }
  
  /*@ non_null @*/ public static Attribute createAttribute(/*@ non_null @*/ String name, String ... values)
  {
    Attribute att = new Attribute(name);
    for (String val : values)
    {
      att.addStringValue(val);
    }
    return att;
  }
}
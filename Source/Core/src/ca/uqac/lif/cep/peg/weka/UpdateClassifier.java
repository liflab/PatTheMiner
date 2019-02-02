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
  
  /**
   * The number of new input events required to update the classifier.
   */
  protected int m_updateInterval = 1;
  
  /**
   * The number of input events since the last time the classifier has
   * been updated.
   */
  protected int m_eventsSinceUpdate = 0;
  
  /**
   * Creates a new update classifier processor.
   * @param c The classifier used to classify the instances. Depending on the
   * actual {@link Classifier} instance used, a different classification
   * algorithm will be used to classify the instances.
   * @param update_interval The number of new input events required to
   * update the classifier. Between these, the classifier from the last
   * update will be output.
   * @param name A name given to the dataset corresponding to the input events
   * @param attributes A list of {@link Attribute}s describing the elements
   * of the array given as input to the processor. The <i>i</i>-th attribute
   * object describes the contents of the <i>i</i>-th element of the input
   * array.
   */
  public UpdateClassifier(/*@ non_null @*/ Classifier c, int update_interval, 
      /*@ non_null @*/ String name, Attribute ... attributes)
  {
    super(1, 1);
    m_classifier = c;
    m_dataSetName = name;
    m_attributes = attributes;
    m_updateInterval = update_interval;
    FastVector att_info = new FastVector();
    for (Attribute att : attributes)
    {
      att_info.addElement(att);
    }
    m_instances = new Instances(name, att_info, s_capacity);
    // By convention, the last attribute is the class
    m_instances.setClassIndex(attributes.length - 1);
  }

  /**
   * Creates a new update classifier processor, which updates the classifier
   * upon every input event.
   * @param c The classifier used to classify the instances. Depending on the
   * actual {@link Classifier} instance used, a different classification
   * algorithm will be used to classify the instances.
   * @param name A name given to the dataset corresponding to the input events
   * @param attributes A list of {@link Attribute}s describing the elements
   * of the array given as input to the processor. The <i>i</i>-th attribute
   * object describes the contents of the <i>i</i>-th element of the input
   * array.
   */
  public UpdateClassifier(/*@ non_null @*/ Classifier c, 
      /*@ non_null @*/ String name, Attribute ... attributes)
  {
    this(c, 1, name, attributes);
  }
  
  /**
   * Sets the number of new input events required to update the internal
   * classifier.
   * @param interval The interval. Must be greater than 0.
   * @return This processor
   */
  /*@ requires interval > 0 @*/
  /*@ ensures m_updateInterval == interval @*/
  /*@ non_null @*/ public UpdateClassifier setUpdateInterval(int interval)
  {
    m_updateInterval = interval;
    return this;
  }

  @Override
  protected boolean compute(Object[] inputs, Object[] outputs)
  {
    m_eventsSinceUpdate++;
    Object[] input_array = (Object[]) inputs[0];
    Instance new_instance = null;
    try
    {
      new_instance = WekaUtils.createInstanceFromArray(m_instances, input_array, m_attributes);
    }
    catch (IllegalArgumentException e)
    {
      throw new ProcessorException(e);
    }
    m_instances.add(new_instance);
    if (m_eventsSinceUpdate > m_updateInterval)
    {
      m_eventsSinceUpdate = 0;
    }
    if (m_eventsSinceUpdate == 0)
    {
      // We update the classifier only when the number of input events
      // is a multiple of m_updateInterval; otherwise, we re-output the
      // same classifier
      try
      {
        m_classifier.buildClassifier(m_instances);
      }
      catch (Exception e)
      {
        throw new ProcessorException(e);
      }
    }
    outputs[0] = m_classifier;
    return true;
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
    UpdateClassifier uc = new UpdateClassifier(new_c, m_updateInterval, m_dataSetName, m_attributes);
    if (with_state)
    {
      uc.m_instances = new Instances(m_instances);
      uc.m_eventsSinceUpdate = m_eventsSinceUpdate;
    }
    return uc;
  }
  
  /**
   * Gets the dataset created by this processor
   * @return The dataset
   */
  /*@ pure non_null @*/ public Instances getDataset()
  {
    return m_instances;
  }
}
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
import java.util.ArrayList;
import java.util.Collection;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Updates a Weka {@link Classifier} using input events as its instances.
 * Graphically, this processor is illustrated as follows:
 * <p>
 * <img src="{@docRoot}/doc-files/UpdateClassifier.png" alt="Processor">
 * <p>
 * The classification process is based on <i>n</i> {@link Attribute}s, named
 * <i>A</i><sub>1</sub>, &hellip; <i>A</i><sub><i>n</i></sub>. Each attribute
 * can be either numerical or categorical (i.e. taking its value from a list
 * of predefined constants). One of these attributes, <i>A</i><sub><i>c</i></sub>,
 * is called the <em>class attribute</em>. The learning process consists of
 * finding a function (the "classifier") which can predict the value of
 * <i>A</i><sub><i>c</i></sub> based on the value of the other attributes.
 * <p>
 * The processor receives an input stream which can be made of two things:
 * <ol>
 * <li>An array of of <i>n</i> elements. This array represents an
 * <em>instance</em>: the element at position <i>i</i> in the collection
 * corresponds to the value of attribute <i>A</i><sub><i>i</i></sub>. By
 * convention, the class attribute is taken to be the last element of the
 * collection.</li>
 * <li>A {@link Collection} of such arrays.</li>
 * </ol> 
 * <p>
 * Every time a new instance (or set of instances) is given to the processor, it
 * creates an {@link Instance} from it, and then updates a Weka {@link Classifier}
 * object. The processor thus produces a stream of classifiers. Since updating a
 * classifier can be a time consuming operation, the processor can be told to
 * accumulate instances for some time, and only update the classifier every
 * <i>n</i> events. In all cases, the processor outputs a classifier upon every
 * output event: either an updated classifier, or the last output classifier. 
 */
public class UpdateClassifier extends UniformProcessor
{
  /**
   * A name given to the dataset. This is because Weka requires sets of
   * instances to be given a name.
   */
  /*@ non_null @*/ protected String m_dataSetName;

  /**
   * The list of instances given to the processor.
   * Input events will be converted into instances and
   * accumulated into this object.
   */
  protected int m_instanceSize = 0;

  /**
   * The set of instances. Accumulated instances are sent to the classifier
   * through this object.
   */
  /*@ non_null @*/ protected Instances m_instances;

  /**
   * The size of the circular buffer storing the instances
   * to be learned.
   */
  protected int m_rollWidth = 0;

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
   * The number of input events received so far
   */
  protected long m_instanceCount = 0;

  /**
   * Creates a new update classifier processor.
   * @param c The classifier used to classify the instances. Depending on the
   * actual {@link Classifier} instance used, a different classification
   * algorithm will be used to classify the instances. Please refer to Weka's
   * documentation for information about how to create a classifier.
   * @param update_interval The number of new input events required to
   * update the classifier. Between these, the classifier from the last
   * update will be output. By default, the interval is 1 (i.e. the processor
   * updates the classifier upon every input event).
   * @param roll_width The size of the circular buffer storing the instances
   * to be learned. If set to a number less than 1, the buffer will store all
   * the instances. Otherwise, only the last <tt>roll_width</tt> instances are
   * kept.
   * @param name A name given to the dataset corresponding to the input events.
   * This is because Weka requires sets of instances to be given a name.
   * @param attributes A list of {@link Attribute}s describing the elements
   * of the array given as input to the processor. The <i>i</i>-th attribute
   * object describes the contents of the <i>i</i>-th element of the input
   * array.
   */
  public UpdateClassifier(/*@ non_null @*/ Classifier c, int update_interval,
      int roll_width, /*@ non_null @*/ String name, Attribute ... attributes)
  {
    super(1, 1);
    m_classifier = c;
    m_dataSetName = name;
    m_attributes = attributes;
    m_updateInterval = update_interval;
    m_rollWidth = roll_width;
    m_instanceSize = 0;
    m_instances = WekaUtils.createInstances(m_dataSetName, s_capacity, m_attributes);
  }

  /**
   * Creates a new update classifier processor, which updates the classifier
   * upon every input event.
   * @param c The classifier used to classify the instances. Depending on the
   * actual {@link Classifier} instance used, a different classification
   * algorithm will be used to classify the instances. Please refer to Weka's
   * documentation for information about how to create a classifier.
   * @param name A name given to the dataset corresponding to the input events.
   * This is because Weka requires sets of instances to be given a name.
   * @param attributes A list of {@link Attribute}s describing the elements
   * of the array given as input to the processor. The <i>i</i>-th attribute
   * object describes the contents of the <i>i</i>-th element of the input
   * array.
   */
  public UpdateClassifier(/*@ non_null @*/ Classifier c, 
      /*@ non_null @*/ String name, Attribute ... attributes)
  {
    this(c, 1, 0, name, attributes);
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

  @SuppressWarnings("unchecked")
  @Override
  protected boolean compute(Object[] inputs, Object[] outputs)
  {
    m_eventsSinceUpdate++;
    m_instanceCount++;
    Collection<Object> collection = null;
    if (inputs[0] instanceof Collection)
    {
      collection = (Collection<Object>) inputs[0];
    }
    else
    {
      collection = new ArrayList<Object>(1);
      collection.add(inputs[0]);
    }
    for (Object o : collection)
    {
      if (o == null || !o.getClass().isArray())
      {
        throw new ProcessorException("The input event to UpdateClassifier is null or is not an array");
      }
      Object[] input_array = (Object[]) o;
      Instance new_instance = null;
      try
      {
        new_instance = WekaUtils.createInstanceFromArray(m_instances, input_array, m_attributes);
      }
      catch (IllegalArgumentException e)
      {
        throw new ProcessorException(e);
      }
      if (m_rollWidth > 0 && m_instanceSize == m_rollWidth)
      {
        m_instances.delete(0);
        m_instances.add(new_instance);
      }
      else
      {
        m_instances.add(new_instance);
        m_instanceSize++;  
      }
    }
    if (m_eventsSinceUpdate >= m_updateInterval)
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

  /**
   * Gets the number of instances fed to the classifier
   * @return The number of instances received so far
   */
  /*@ pure @*/ long getInstanceCount()
  {
    return m_instanceCount;
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
    UpdateClassifier uc = new UpdateClassifier(new_c, m_updateInterval, m_rollWidth, m_dataSetName, m_attributes);
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

  /**
   * Gets the attributes handled by the classifier used by this processor
   * @return The attributes
   */
  /*@ pure non_null @*/ public Attribute[] getAttributes()
  {
    return m_attributes;
  }

  /**
   * Gets the classifier used by this processor chain in its current state.
   * Note that this method should normally be used only for debugging purposes;
   * to use the classifier in a processor chain, simply connect the output of
   * the processor to a downstream processor chain.
   * @return The classifier
   */
  /*@ pure non_null @*/ Classifier getClassifier()
  {
    return m_classifier;
  }

  @Override
  public void reset()
  {
    super.reset();
    m_instanceCount = 0;
    m_eventsSinceUpdate = 0;
    m_instances = WekaUtils.createInstances(m_dataSetName, s_capacity, m_attributes);
    m_instanceSize = 0;
  }
}
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

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.ProcessorException;
import ca.uqac.lif.cep.functions.ApplyFunction;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Instances;

/**
 * Updates a Weka {@link Classifier} and casts the resulting object into
 * a BeepBeep {@link Function}.
 * @see {@link UpdateClassifier}
 */
public class UpdateClassifierFunction extends GroupProcessor
{
  /**
   * The underlying classifier processor. It is saved here only so that
   * its instantiation parameters can be retrieved on a call to
   * {@link #duplicate(boolean)}.
   */
  protected UpdateClassifier m_classifier;
  
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
  public UpdateClassifierFunction(/*@ non_null @*/ Classifier c, int update_interval,
      int roll_width, /*@ non_null @*/ String name, Attribute ... attributes)
  {
    super(1, 1);
    m_classifier = new UpdateClassifier(c, update_interval, roll_width, name, attributes);
    ApplyFunction to_fct = new ApplyFunction(new WekaUtils.CastClassifierToFunction(
        m_classifier.getDataset(), attributes));
    Connector.connect(m_classifier, to_fct);
    addProcessors(m_classifier, to_fct);
    associateInput(0, m_classifier, 0);
    associateOutput(0, to_fct, 0);
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
  public UpdateClassifierFunction(/*@ non_null @*/ Classifier c, 
      /*@ non_null @*/ String name, Attribute ... attributes)
  {
    this(c, 1, 0, name, attributes);
  }
  
  @Override
  public UpdateClassifierFunction duplicate(boolean with_state)
  {
    if (with_state)
    {
      throw new UnsupportedOperationException("This processor does not support stateful duplication");
    }
    Classifier new_c = null;
    try
    {
      new_c = Classifier.makeCopy(m_classifier.getClassifier());
    }
    catch (Exception e)
    {
      throw new ProcessorException(e);
    }
    UpdateClassifierFunction ucf = new UpdateClassifierFunction(new_c, m_classifier.m_updateInterval,
        m_classifier.m_rollWidth, m_classifier.m_dataSetName, m_classifier.m_attributes);
    ucf.m_classifier.m_makeCopy = m_classifier.m_makeCopy;
    return ucf;
  }
  
  /**
   * Gets the dataset created by this processor
   * @return The dataset
   */  
  /*@ pure non_null @*/ public Instances getDataset()
  {
    return m_classifier.getDataset();
  }
  
  /**
   * Gets the number of instances fed to the classifier
   * @return The number of instances received so far
   */
  /*@ pure @*/ public long getInstanceCount()
  {
    return m_classifier.getInstanceCount();
  }
  
  /**
   * Sets whether the processor should output its own internal classifier,
   * or a <em>clone</em> of this classifier.
   * 
   * @param b Set to <tt>true</tt> to create a copy of the internal
   * classifier, <tt>false</tt> otherwise (default). Setting this parameter
   * to <tt>true</tt> obviously has consequences on the throughput of the
   * processor.
   * @return This processor
   */
  public UpdateClassifierFunction makeCopy(boolean b)
  {
    m_classifier.makeCopy(b);
    return this;
  }
}

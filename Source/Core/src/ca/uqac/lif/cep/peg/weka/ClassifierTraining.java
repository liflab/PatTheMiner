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

import static ca.uqac.lif.cep.Connector.BOTTOM;
import static ca.uqac.lif.cep.Connector.INPUT;
import static ca.uqac.lif.cep.Connector.OUTPUT;
import static ca.uqac.lif.cep.Connector.TOP;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.functions.ApplyFunction;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.tmf.Trim;
import ca.uqac.lif.cep.tmf.Window;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Instances;

/**
 * Processor chain that trains a classifier by associating a collection of
 * feature values computed by a processor &beta; on a window, to a
 * <em>class</em> computed by a processor &kappa; on a subsequent window.
 * <p>
 * <img src="{@docRoot}/doc-files/ClassifierTraining.png" alt="Processor chain">
 * <p>
 * The process can be illustrated in the figure above. In this workflow, a
 * first copy of the stream is trimmed from its first $t$ events (box #1);
 * a sliding window of $m$ events (box #2) is submitted to a {@link Classifier},
 * noted by &kappa; (box #3). This processor acts as an oracle, which
 * assigns a label to each successive window of width <i>m</i> inside
 * the stream. In a second copy of the stream, an arbitrary trend
 * is computed over a sliding window of <i>n</i> events by a trend processor &beta;
 * (boxes #4 and #5). This trend $a$ and the classification
 * <i>c</i> computed from &kappa; are combined into a tuple
 * (<i>a</i>,<i>c</i>) &in; <i>A</i> &times; <i>C</i>, where <i>A</i> is the
 * set of trends, and <i>C</i> is the set of possible categories returned by
 * &kappa;.
 * <p>
 * This stream of tuples is accumulated into a set (box #6), and this
 * set is submitted to an {@link UpdateClassifier} processor, represented 
 * by box #7. Upon receiving these tuples, the learning processor's task 
 * is to update a classification function 
 * <i>f</i> : <i>A</i> &rarr; <i>C</i>, which can be obtained 
 * from various means, such as the use of a machine learning algorithm. The 
 * output of this processor is therefore a stream of <em>classifiers</em>
 * (i.e. functions).
 * <p>
 * In other words, at each position <i>i</i> in the input stream, this
 * chain considers two windows:
 * <ul>
 * <li>a first window <i>W</i><sub>1</sub>,
 * comprising events <i>i</i> to <i>i</i>+<i>m</i>, over which a set of
 * features is calculated by processor &beta;</li>
 * <li>a second window <i>W</i><sub>2</sub>,
 * comprising events <i>i</i>+<i>t</i> to <i>i</i>+<i>t</i>+<i>n</i>, over which the
 * class is calculated by the "oracle" processor &kappa;</li>
 * </ul>
 * This is illustrated by the diagram below:
 * <p>
 * <img src="{@docRoot}/doc-files/Anticipation.png" alt="Anticipation">
 * <p>
 * A quick calculation reveals that the output of this chain is offset by
 * <i>t</i>+<i>n</i>-<i>m</i> events with respect to the input (in other
 * words, the first window for which an output classifier will be produced
 * starts at position <i>t</i>+<i>n</i>-<i>m</i>). This also corresponds to
 * the offset (in number of events) between the start of the "trend" window
 * and the start of the "oracle" window.
 * <p>
 * When represented as a single box, this processor uses the following diagram:
 * <p>
 * <img src="{@docRoot}/doc-files/ClassifierTraining-box.png" alt="Processor">
 */
public class ClassifierTraining extends GroupProcessor
{
  /**
   * A reference to the &beta; processor in the chain
   */
  protected Processor m_beta;

  /**
   * A reference to the &kappa; processor in the chain
   */
  protected Processor m_kappa;

  /**
   * A reference to the instance of {@link UpdateClassifier} in the chain
   */
  protected UpdateClassifier m_uc;

  /**
   * The offset (in number of events) between the "feature" window
   * and the "class" window
   */
  protected int m_t;

  /**
   * The width of the "class" window
   */
  protected int m_n;

  /**
   * The width of the "feature" window
   */
  protected int m_m;

  /**
   * Creates a new instance of the classifier training processor chain.
   * The parameters <tt>t</tt>, <tt>m</tt> and <tt>n</tt> must satisfy the
   * condition <tt>t</tt>+<tt>n</tt> &geq; <tt>m</tt>.
   * @param beta The processor computing the trend over the window. This
   * corresponds to &beta; in the diagram
   * @param kappa The processor used as the oracle. This
   * corresponds to &kappa; in the diagram
   * @param uc A processor that updates a classifier based on the
   * feature/class pairs produced by &beta; and &kappa;
   * @param t The offset (in number of events) between the "feature" window
   * and the "class" window
   * @param n The width of the "class" window
   * @param m The width of the "feature" window
   */
  /*@ requires t + n >= m @*/
  public ClassifierTraining(/*@ non_null @*/ Processor beta, /*@ non_null @*/ Processor kappa, 
      /*@ non_null @*/ UpdateClassifier uc, int t, int n, int m)
  {
    super(1, 1);
    m_beta = beta;
    m_kappa = kappa;
    m_uc = uc;
    m_t = t;
    m_n = n;
    m_m = m;
    Fork f1 = new Fork(2);
    Trim trim = new Trim(t);
    Connector.connect(f1, TOP, trim, INPUT);
    Window win_top = new Window(kappa, n);
    Connector.connect(trim, win_top);
    Window win_bot = new Window(beta, m);
    Connector.connect(f1, BOTTOM, win_bot, INPUT);
    ApplyFunction merge = new ApplyFunction(WekaUtils.MergeIntoArray.instance);
    Connector.connect(win_top, OUTPUT, merge, TOP);
    Connector.connect(win_bot, OUTPUT, merge, BOTTOM);
    Connector.connect(merge, uc);
    addProcessors(f1, trim, win_top, win_bot, merge, uc);
    associateInput(INPUT, f1, INPUT);
    associateOutput(OUTPUT, uc, OUTPUT);
  }

  @Override
  public ClassifierTraining duplicate(boolean with_state)
  {
    return new ClassifierTraining(m_beta.duplicate(with_state), m_kappa.duplicate(with_state), 
        m_uc.duplicate(with_state), m_t, m_n, m_m);
  }

  /**
   * Returns the dataset associated to the internal classifier
   * @return The dataset
   */
  public Instances getDataset()
  {
    return m_uc.getDataset();
  }

  /**
   * Returns the attributes associated to the internal classifier
   * @return The attributes
   */
  public Attribute[] getAttributes()
  {
    return m_uc.getAttributes();
  }
  
  /**
   * Returns the classifier used by the processor chain in its current state
   * Note that this method should normally be used only for debugging purposes;
   * to use the classifier in a processor chain, simply connect the output of
   * the processor to a downstream processor chain.
   * @return The classifier
   */
  /*@ pure non_null @*/ Classifier getClassifier()
  {
    return m_uc.getClassifier();
  }
}
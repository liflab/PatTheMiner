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
import static ca.uqac.lif.cep.Connector.LEFT;
import static ca.uqac.lif.cep.Connector.OUTPUT;
import static ca.uqac.lif.cep.Connector.TOP;
import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.functions.ApplyFunction;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.tmf.Trim;
import ca.uqac.lif.cep.tmf.Window;

/**
 * <p>
 * <img src="{@docRoot}/doc-files/SelfTrainedClassPrediction.png" alt="Processor chain">
 * <p>
 * An input stream is forked in two (box #1). The top stream is sent into a
 * {@link ClassifierTraining} processor, which produces a stream of classifiers
 * (box #2). The bottom stream is used to compute a set of features by a tend
 * processor on a window of width <i>m</i> (box #3). Since the classifier training
 * does not output a classifier before receiving <i>t</i>+<i>n</i>-<i>m</i> events
 * (the so-called "warm up" phase), the first <i>t</i>+<i>n</i>-<i>m</i> trends
 * are trimmed from this output stream. The trend and classifier streams are then
 * sent to an {@link ApplyFunction} processor: given a classifier and a trend,
 * it returns the class associated to the trend, as learned by the classifier. 
 */
public class SelfTrainedClassPrediction extends GroupProcessor
{
  /**
   * The parameters <tt>t</tt>, <tt>m</tt> and <tt>n</tt> must satisfy the
   * condition <tt>t</tt>+<tt>n</tt> &geq; <tt>m</tt>.
   * @param ct The classifier training processor used in the chain
   * @param beta The processor computing the trend over the window. This
   * corresponds to &beta; in the diagram
   * @param t The offset (in number of events) between the "feature" window
   * and the "class" window
   * @param n The width of the "class" window
   * @param m The width of the "feature" window
   */
  /*@ requires t + n >= m @*/
  public SelfTrainedClassPrediction(ClassifierTraining ct, Processor beta, int t, int n, int m)
  {
    super(1, 1);
    Fork f = new Fork(2);
    Connector.connect(f, TOP, ct, INPUT);
    Window win = new Window(beta, m);
    Connector.connect(f, BOTTOM, win, INPUT);
    Trim trim = new Trim(t + n - m);
    ApplyFunction af = new ApplyFunction(new WekaUtils.EvaluateClassifier(ct.getDataset(), ct.getAttributes()));
    Connector.connect(win, trim);
    Connector.connect(ct, OUTPUT, af, LEFT);
    Connector.connect(trim, OUTPUT, af, BOTTOM);
    addProcessors(f, ct, win, trim, af);
    associateInput(INPUT, f, INPUT);
    associateOutput(OUTPUT, af, OUTPUT);
  }
}
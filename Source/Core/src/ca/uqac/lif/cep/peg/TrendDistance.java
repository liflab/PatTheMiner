/*
    A BeepBeep palette for mining event traces
    Copyright (C) 2017-2018 Sylvain Hallé and friends

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

import static ca.uqac.lif.cep.Connector.INPUT;
import static ca.uqac.lif.cep.Connector.OUTPUT;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.functions.StreamVariable;
import ca.uqac.lif.cep.functions.BinaryFunction;
import ca.uqac.lif.cep.functions.Constant;
import ca.uqac.lif.cep.functions.Function;
import ca.uqac.lif.cep.functions.ApplyFunction;
import ca.uqac.lif.cep.functions.FunctionTree;
import ca.uqac.lif.cep.tmf.Window;

/**
 * Processor that evaluates in real-time whether a trend computed
 * over an input event stream is "close enough" to a reference
 * trend.
 * <p>
 * Graphically, the computation made by the {@link TrendDistance}
 * processor can be represented as follows:
 * <p>
 * <img src="{@docRoot}/doc-files/TrendDistance.png" alt="TrendDistance">
 * <p>
 * First, a stream of events is received and sent into a sliding
 * window computation (box #1 in the diagram). Input events come
 * in from the left, and new output events are produced and come
 * out from the right of the box. Such a computation requires
 * two parameters: the width of the window (labelled n), and
 * a computation β to perform on each window, represented by
 * box #2. Concretely, the sliding window computation creates
 * a window of the first n events received (e<sub>0</sub>, 
 * e<sub>1</sub>, &hellip;, e<sub>n−1</sub>). It
 * then feeds this window of events to computation β; the output
 * returned by β is the first event to be output by box #1. The
 * computation then “slides” one event forward, and creates a new
 * window made of events e<sub>1</sub> to e<sub>n</sub>. It feeds this window
 * to β, whose return value is the second event to be output by box #1
 * —and so on.
 * <p>
 * The end result is that, from an input stream of events, box
 * #1 creates a new stream, made of the application of β on
 * successive windows of width n. Intuitively, β is intended to
 * represent the calculation of a “trend” on a window that slides
 * over the input stream. Since the goal of the process is to detect
 * whether the input stream exhibits a “deviation” from some
 * norm, the computed trends will be compared to some reference.
 * This is the purpose of box #3, which evaluates what is called
 * the distance metric. It takes two arguments; the first is the
 * stream of trends computed by box #1; the second is a stream of
 * reference patterns, provided by box #4. In the simplest scenario,
 * the reference pattern does not change throughout the whole
 * input stream, and box #4 simply returns the same reference
 * pattern P over and over.
 * <p>
 * For each pair (P, p), where P is the reference pattern and
 * p is a pattern computed by box #1, the distance metric δ(P, p)
 * is evaluated, and its value d p is returned as the output of box
 * #3. Intuitively, δ is a function that estimates “how far” pattern
 * p is from reference P. The workflow is expected to produce a
 * notification when that distance becomes larger than a specific
 * threshold. This is the task of box #6, which compares the
 * computed distance d<sub>p</sub> with a fixed threshold value d (provided
 * by box #5). The function v is called the comparison function:
 * v(d<sub>p</sub>,d) returns T (true) when d<sub>p</sub> is “greater” than
 * the maximum threshold value d.
 * <p>
 * The net effect is that the workflow receives a
 * stream of arbitrary input events e<sub>0</sub>, e<sub>1</sub>, &hellip;,
 * and produces as its output a sequence of Boolean values b<sub>0</sub>,
 * b<sub>1</sub>, &hellip; Under normal
 * conditions, this workflow outputs the value ⊥ (false) repeatedly.
 * The occurrence of value T indicates an "anomaly".
 * 
 * @author Sylvain Hallé
 *
 * @param <P> The type of the pattern
 * @param <Q> The type returned by the beta processor
 * @param <R> The type returned by the distance function
 */
public class TrendDistance<P,Q,R> extends GroupProcessor
{
  /**
   * Instantiates a new trend distance processor.
   * @param pattern The reference pattern
   * @param n The width of the window
   * @param beta The processor that computes the trend on each window
   * @param delta The distance metric
   * @param d The maximum distance threshold
   * @param comp The comparison function between the computed distance
   *   and the maximum distance threshold
   */
  public TrendDistance(P pattern, int n, Processor beta, Function delta, R d, BinaryFunction<R,R,Boolean> comp)
  {
    super(1, 1);
    Window wp = new Window(beta, n);
    build(wp, pattern, delta, d, comp);
  }

  /**
   * Instantiates a new trend distance processor.
   * @param pattern The reference pattern
   * @param window A window processor
   * @param delta The distance metric
   * @param d The maximum distance threshold
   * @param comp The comparison function between the computed distance
   *   and the maximum distance threshold
   */
  public TrendDistance(P pattern, Processor window, Function delta, R d, BinaryFunction<R,R,Boolean> comp)
  {
    super(1, 1);
    build(window, pattern, delta, d, comp);
  }

  protected void build(Processor window, P pattern, Function delta, R d, BinaryFunction<R,R,Boolean> comp)
  {
    associateInput(INPUT, window, INPUT);
    ApplyFunction distance = new ApplyFunction(new FunctionTree(delta,
        new Constant(pattern),
        new StreamVariable(0)
        ));
    Connector.connect(window, distance);
    ApplyFunction too_far = new ApplyFunction(new FunctionTree(comp,
        new StreamVariable(0),
        new Constant(d)
        ));
    Connector.connect(distance, too_far);
    associateOutput(OUTPUT, too_far, OUTPUT);
    addProcessors(window, distance, too_far);
  }
}

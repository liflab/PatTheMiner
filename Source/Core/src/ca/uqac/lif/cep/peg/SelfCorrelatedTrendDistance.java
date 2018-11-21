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

import static ca.uqac.lif.cep.Connector.BOTTOM;
import static ca.uqac.lif.cep.Connector.INPUT;
import static ca.uqac.lif.cep.Connector.OUTPUT;
import static ca.uqac.lif.cep.Connector.TOP;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.functions.ApplyFunction;
import ca.uqac.lif.cep.functions.BinaryFunction;
import ca.uqac.lif.cep.functions.Constant;
import ca.uqac.lif.cep.functions.Function;
import ca.uqac.lif.cep.functions.FunctionTree;
import ca.uqac.lif.cep.functions.StreamVariable;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.tmf.Trim;
import ca.uqac.lif.cep.tmf.Window;

/**
 * Compares a trend calculated on a window events to another trend computed on
 * another window placed further away in the past. Graphically, this generic
 * pattern can be represented as follows:
 * <p>
 * <img src="{@docRoot}/doc-files/SCTD-1-chain.png" alt="Self-correlated trend distance">
 * <p>
 * The processor can also be seen as a "black box" taking four parameters, and
 * in this case is represented as follows:
 * <p>
 * <img src="{@docRoot}/doc-files/SCTD-1.png" alt="Self-correlated trend distance">
 * <p>
 * The parameters are:
 * <table>
 * <tr><th align="center">Parameter</th><th>Description</th></tr>
 * <tr>
 *   <td align="center">n</td>
 *   <td>The width of each window ("past" and "present")</td>
 * </tr>
 * <tr>
 *   <td align="center">m</td>
 *   <td>The amount of offset between the "past" and the "present" window</td>
 * </tr>
 * <tr>
 *   <td align="center"><img src="{@docRoot}/doc-files/BetaProcessor.png" alt="&beta;"></td>
 *   <td>The processor used to compute the trend on both windows</td>
 * </tr>
 * <tr>
 *   <td align="center"><img src="{@docRoot}/doc-files/DistanceFunction.png" alt="&delta;"></td>
 *   <td>The metric used to compute the distance between the two trends</td>
 * </tr> 
 * </table>
 * <p>
 * Optionally, the pattern can be instantiated with two more parameters: a
 * comparison function and a threshold metric. The pattern is extended to become
 * the following:
 * <p>
 * <img src="{@docRoot}/doc-files/SCTD-2-chain.png" alt="Self-correlated trend distance">
 * <p>
 * Represented as a single box, this becomes:
 * <p>
 * <img src="{@docRoot}/doc-files/SCTD-2.png" alt="Self-correlated trend distance">
 * <p>
 * The additional parameters are:
 * <table>
 * <tr><th align="center">Parameter</th><th>Description</th></tr>
 * <tr>
 *   <td align="center">d</td>
 *   <td>A distance threshold</td>
 * </tr>
 * <tr>
 *   <td align="center"><img src="{@docRoot}/doc-files/ComparisonFunction.png" alt="Comparison"></td>
 *   <td>A function that compares the distance between trends to d, and
 *   determines if the distance "exceeds" the threshold</td>
 * </tr>
 * </table>
 * <p>
 * Let us explain this workflow. Boxes #1–3 and #5–6 in the previous diagram
 * are similar to the static {@link TrendDistance} pattern. The difference lies
 * upstream, in how the “current”
 * and the “reference” trends are extracted from the input event
 * sequence. That sequence is first split into two copies (box
 * #9). The topmost copy is trimmed of its first m events, as
 * sis represented by box #7. This makes such that the streams
 * entering boxes #1 and #4 are offset by m: while box #4 receives
 * the stream of events e₀ , e₁, &hellip;, box #1 receives the stream
 * e<sub>m</sub>, e<sub>m+1</sub>, &hellip; These two boxes then apply the same
 * computation β on a sliding window: box #1 on a window of width n, and
 * box #4 on a window of width m. The output of β on these two
 * windows is then sent to the distance metric (box #3), and the
 * rest of the process unfolds similarly to the static trend distance
 * workflow we have introduced earlier.
 * <p>
 * As before, the distance metric (box #3) is fed with a
 * sequence of pairs of trends (p, t), where t ∈ T is the trend
 * computed on the latest window of n events. The reference
 * trend p ∈ P, however, is now also computed from a window
 * of events of the same stream. Let k be the number of events
 * received from the input stream so far, with k ≥ m + n. Due
 * to the presence of the trimming box (#7), it can be observed
 * that, when box #1 applies β on a window of the last n events
 * ({e<sub>k−(n−1)</sub>, e<sub>k−(n−2)</sub>, &hellip;, e<sub>k</sub>})
 *  box #4 applies β on a window of the m preceding events 
 * ({e<sub>k−n−(m−1)</sub>, e<sub>k−n−(m−2)</sub>, &hellip;, e<sub>k−n</sub>s}).
 * In other words, the distance metric compares the trend computed
 * from the last n events to a reference computed from the m
 * events before them. The rest works in a similar way to the static trend
 * distance workflow.
 * <p>
 * The first variant is simply missing the boxes #5 and #6; rather than
 * comparing the distance to a threshold, it outputs the distance directly.
 * 
 * @param <P> The type of the pattern
 * @param <Q> The type returned by the beta processor
 * @param <R> The type returned by the distance function
 */
public class SelfCorrelatedTrendDistance<P,Q,R> extends GroupProcessor
{
  /*@ require m > 0
   *@ require n > 0
   */
  public SelfCorrelatedTrendDistance(int m, int n, /*@ non_null @*/ Processor beta, /*@ non_null @*/ Function delta, R d, /*@ non_null @*/ BinaryFunction<R,R,Boolean> comp)
  {
    super(1, 1);
    build(m, n, beta, delta, d, comp);
  }

  /*@ require m > 0
   *@ require n > 0
   */
  public SelfCorrelatedTrendDistance(int m, int n, /*@ non_null @*/ Processor beta, /*@ non_null @*/ Function delta)
  {
    super(1, 1);
    build(m, n, beta, delta, null, null);
  }

  /*@ require m > 0
   *@ require n > 0
   */
  protected void build(int m, int n, /*@ non_null @*/ Processor beta, /*@ non_null @*/ Function delta, /*@ null @*/ R d, /*@ null @*/ BinaryFunction<R,R,Boolean> comp)
  {
    Fork fork = new Fork(2);
    associateInput(INPUT, fork, INPUT);
    Trim trim = new Trim(m);
    Connector.connect(fork, TOP, trim, INPUT);
    Window win1 = new Window(beta.duplicate(), n);
    Connector.connect(trim, win1);
    Window win2 = new Window(beta.duplicate(), m);
    Connector.connect(fork, BOTTOM, win2, INPUT);
    ApplyFunction distance = new ApplyFunction(delta);
    Connector.connect(win1, OUTPUT, distance, TOP);
    Connector.connect(win2, OUTPUT, distance, BOTTOM);
    if (d != null && comp == null)
    {
      ApplyFunction too_far = new ApplyFunction(new FunctionTree(comp,
          new StreamVariable(0),
          new Constant(d)
          ));
      Connector.connect(distance, too_far);
      associateOutput(OUTPUT, too_far, OUTPUT);
      addProcessors(fork, trim, win1, win2, distance, too_far);
    }
    else
    {
      associateOutput(OUTPUT, distance, OUTPUT);
      addProcessors(fork, trim, win1, win2, distance);
    }
  }
}

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

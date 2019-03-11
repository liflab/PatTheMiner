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
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.tmf.Window;

/**
 * Group processor which generalizes the TrendDistance processor. Takes data in
 * (a list for example) and extracts a context as well as a trend. The context
 * will serve as a way for the processor to figure out which data to compare the
 * trend to. The "figuring out" process will be done by the ChoiceFunction
 * "func" which can be coded ad-hoc.
 * <p>
 * A classic example is taking the value
 * associated with the closest context in func's map. The trend and the value
 * extracted from context can then be operated on with, for example, a
 * subtraction and then compared like in the TrendDistance processor.
 * 
 * @param <Q> The type of the context value
 * @param <R> The type of the trend value
 * 
 * @author Alexandre Larouche
 */
@SuppressWarnings("rawtypes")
public class ContextRef<Q, R> extends GroupProcessor
{
  /**
   * Creates a new contextual processor.
   * @param trend_proc
   *          The processor that computes the trend on each window (extracts the
   *          values of the trace)
   * @param n
   *          The width of the window for the trend processor
   * @param context_proc
   *          The processor that computes the context on each window (extracts the
   *          keys of the trace)
   * @param m
   *          The width of the window for the context processor
   * @param func
   *          Processors which select the right value to pick depending on which
   *          context (key) was extracted from betaC
   * @param delta
   *          The distance metric
   * @param d
   *          The maximum distance threshold
   * @param comp
   *          The comparison function between the computed distance and the
   *          maximum distance threshold
   */
  public ContextRef(Processor trend_proc, int n, Processor context_proc, int m, ChoiceFunction<Q,R> func,
      Function delta, R d, BinaryFunction<R, R, Boolean> comp)
  {
    super(1, 1);
    Window wp = new Window(trend_proc, n);
    Window xp = new Window(context_proc, m);
    build(wp, xp, func, delta, d, comp);
  }

  /**
   * Creates a new contextual processor.
   * 
   * @param window
   *          The processor that computes the trend on each window (extracts the
   *          values of the trace)
   * @param contextWindow
   *          The processor that computes the context on each window (extracts the
   *          keys of the trace)
   * @param func
   *          Processors which select the right value to pick depending on which
   *          context (/key) was extracted from betaC
   * @param delta
   *          The distance metric
   * @param d
   *          The maximum distance threshold
   * @param comp
   *          The comparison function between the computed distance and the
   *          maximum distance threshold
   */
  public ContextRef(Processor window, Processor contextWindow, ChoiceFunction func, Function delta,
      R d, BinaryFunction<R, R, Boolean> comp)
  {
    super(1, 1);
    build(window, contextWindow, func, delta, d, comp);
  }
  
  /**
   * Creates an empty contextual processor. This constructor should not be
   * called directly.
   */
  protected ContextRef()
  {
    super(1, 1);
  }

  protected void build(Processor window, Processor contextWindow, ChoiceFunction func,
      Function delta, R d, BinaryFunction<R, R, Boolean> comp)
  {
    Fork fork = new Fork(2);
    ApplyFunction choice = new ApplyFunction(func);

    ApplyFunction distance = new ApplyFunction(
        new FunctionTree(delta, StreamVariable.X, StreamVariable.Y));
    Connector.connect(window, distance);
    ApplyFunction too_far = new ApplyFunction(
        new FunctionTree(comp, new StreamVariable(0), new Constant(d)));

    Connector.connect(fork, 0, window, INPUT);
    Connector.connect(fork, 1, contextWindow, INPUT);
    Connector.connect(contextWindow, OUTPUT, choice, INPUT);
    Connector.connect(window, 0, distance, 1);
    Connector.connect(choice, OUTPUT, distance, 0);
    Connector.connect(distance, too_far);
    associateInput(INPUT, fork, INPUT);
    associateOutput(OUTPUT, too_far, OUTPUT);
    addProcessors(window, contextWindow, distance, too_far, choice);
  }
  
  @Override
  public ContextRef duplicate(boolean with_state)
  {
    if (with_state)
    {
      throw new UnsupportedOperationException("This processor cannot be duplicated with its state");
    }
    ContextRef ref = new ContextRef();
    ref.duplicateInto(this);
    return ref;
  }
}

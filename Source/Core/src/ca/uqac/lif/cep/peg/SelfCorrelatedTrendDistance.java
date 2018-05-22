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
 * 
 * @param <P> The type of the pattern
 * @param <Q> The type returned by the beta processor
 * @param <R> The type returned by the distance function
 */
public class SelfCorrelatedTrendDistance<P,Q,R> extends GroupProcessor
{
  public SelfCorrelatedTrendDistance(int m, int n, Processor beta, Function delta, R d, BinaryFunction<R,R,Boolean> comp)
  {
    super(1, 1);
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
    ApplyFunction too_far = new ApplyFunction(new FunctionTree(comp,
        new StreamVariable(0),
        new Constant(d)
        ));
    Connector.connect(distance, too_far);
    associateOutput(OUTPUT, too_far, OUTPUT);
    addProcessors(fork, trim, win1, win2, distance, too_far);
  }
}

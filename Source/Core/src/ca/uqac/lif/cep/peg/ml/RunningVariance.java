/*
    A BeepBeep palette for mining event traces
    Copyright (C) 2017 Sylvain Hallé

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
package ca.uqac.lif.cep.peg.ml;

import static ca.uqac.lif.cep.Connector.INPUT;
import static ca.uqac.lif.cep.Connector.LEFT;
import static ca.uqac.lif.cep.Connector.OUTPUT;
import static ca.uqac.lif.cep.Connector.RIGHT;
import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.Connector.ConnectorException;
import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.functions.StreamVariable;
import ca.uqac.lif.cep.functions.Constant;
import ca.uqac.lif.cep.functions.ApplyFunction;
import ca.uqac.lif.cep.functions.FunctionTree;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.util.Numbers;

/**
 * Processor computing the running variance of a stream of numbers.
 * This is implemented as a group processor, that computes
 * E[X]<sup>2</sup> - E[X<sup>2</sup>], where E[X] are instances of 
 * the {@link StatMoment} processor.
 * @author Sylvain Hallé
 *
 */
public class RunningVariance extends GroupProcessor
{
	public RunningVariance()
	{
		super(1, 1);
		try
		{
			Fork f = new Fork(2);
			StatMoment sm_1 = new StatMoment(1);
			StatMoment sm_2 = new StatMoment(2);
			Connector.connect(f, LEFT, sm_1, INPUT);
			Connector.connect(f, RIGHT, sm_2, INPUT);
			ApplyFunction square = new ApplyFunction(new FunctionTree(Numbers.power, new StreamVariable(0), new Constant(2)));
			Connector.connect(sm_1, OUTPUT, square, INPUT);
			ApplyFunction minus = new ApplyFunction(Numbers.subtraction);
			Connector.connect(square, OUTPUT, minus, RIGHT);
			Connector.connect(sm_2, OUTPUT, minus, LEFT);
			associateInput(INPUT, f, INPUT);
			associateOutput(OUTPUT, minus, OUTPUT);
			addProcessors(f, sm_1, sm_2, square, minus);
		}
		catch (ConnectorException e)
		{
			// Fail silently
		}
	}
}

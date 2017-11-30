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
import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.functions.ArgumentPlaceholder;
import ca.uqac.lif.cep.functions.FunctionProcessor;
import ca.uqac.lif.cep.functions.FunctionTree;
import ca.uqac.lif.cep.tmf.Fork;

/**
 * Processor that outputs a two-dimensional {@code DoublePoint} out of a
 * stream of numbers, containing the running average and running variance
 * of the stream.
 * @author Sylvain Hallé
 */
public class MeanAndVariance extends GroupProcessor
{
	public MeanAndVariance()
	{
		super(1, 1);
		Fork f = new Fork(2);
		associateInput(INPUT, f, INPUT);
		StatMoment avg = new StatMoment(1);
		Connector.connect(f, LEFT, avg, INPUT);
		RunningVariance var = new RunningVariance();
		Connector.connect(f, RIGHT, var, INPUT);
		FunctionTree join = new FunctionTree(new JoinVectors(2),
				new FunctionTree(DoublePointCast.instance, new ArgumentPlaceholder(0)),
				new FunctionTree(DoublePointCast.instance, new ArgumentPlaceholder(1)));
		FunctionProcessor join_p = new FunctionProcessor(join);
		Connector.connect(avg, OUTPUT, join_p, LEFT);
		Connector.connect(var, OUTPUT, join_p, RIGHT);
		associateOutput(OUTPUT, join_p, OUTPUT);
		addProcessors(f, avg, var, join_p);
	}
}

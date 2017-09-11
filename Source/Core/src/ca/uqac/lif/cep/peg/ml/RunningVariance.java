package ca.uqac.lif.cep.peg.ml;

import static ca.uqac.lif.cep.Connector.INPUT;
import static ca.uqac.lif.cep.Connector.LEFT;
import static ca.uqac.lif.cep.Connector.OUTPUT;
import static ca.uqac.lif.cep.Connector.RIGHT;
import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.Connector.ConnectorException;
import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.functions.ArgumentPlaceholder;
import ca.uqac.lif.cep.functions.Constant;
import ca.uqac.lif.cep.functions.FunctionProcessor;
import ca.uqac.lif.cep.functions.FunctionTree;
import ca.uqac.lif.cep.numbers.Power;
import ca.uqac.lif.cep.numbers.Subtraction;
import ca.uqac.lif.cep.tmf.Fork;

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
			FunctionProcessor square = new FunctionProcessor(new FunctionTree(Power.instance, new ArgumentPlaceholder(0), new Constant(2)));
			Connector.connect(sm_1, OUTPUT, square, INPUT);
			FunctionProcessor minus = new FunctionProcessor(Subtraction.instance);
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

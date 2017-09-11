package ca.uqac.lif.cep.peg.ml;

import static ca.uqac.lif.cep.Connector.INPUT;
import static ca.uqac.lif.cep.Connector.LEFT;
import static ca.uqac.lif.cep.Connector.OUTPUT;
import static ca.uqac.lif.cep.Connector.RIGHT;
import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.Connector.ConnectorException;
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
		try
		{
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
		catch (ConnectorException e)
		{
			// Silently fail
		}
	}
}

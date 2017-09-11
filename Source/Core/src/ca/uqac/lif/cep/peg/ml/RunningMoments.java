package ca.uqac.lif.cep.peg.ml;

import static ca.uqac.lif.cep.Connector.INPUT;
import static ca.uqac.lif.cep.Connector.OUTPUT;
import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.Connector.ConnectorException;
import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.functions.FunctionProcessor;
import ca.uqac.lif.cep.tmf.Fork;

/**
 * Processor that outputs a n-dimensional {@code DoublePoint} out of a
 * stream of numbers, containing the first <i>n</i> running statistical
 * moments of the stream.
 * @author Sylvain Hallé
 */
public class RunningMoments extends GroupProcessor
{
	/**
	 * The number of statistical moments to compute
	 */
	protected int m_numMoments = 1;
	
	public RunningMoments(int num_moments)
	{
		super(1, 1);
		m_numMoments = num_moments;
		try
		{
			Fork f = new Fork(m_numMoments);
			associateInput(INPUT, f, INPUT);
			FunctionProcessor join = new FunctionProcessor(new JoinVectors(m_numMoments));
			for (int i = 1; i <= m_numMoments; i++)
			{
				StatMoment avg = new StatMoment(i);
				Connector.connect(f, i - 1, avg, INPUT);
				FunctionProcessor cast = new FunctionProcessor(DoublePointCast.instance);
				Connector.connect(avg, OUTPUT, cast, INPUT);
				Connector.connect(cast, OUTPUT, join, i - 1);
				addProcessors(avg, cast);
			}
			associateOutput(OUTPUT, join, OUTPUT);
			addProcessors(f, join);
		}
		catch (ConnectorException e)
		{
			// Silently fail
		}
	}
	
	public RunningMoments()
	{
		this(1);
	}
}

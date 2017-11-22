package ca.uqac.lif.cep.peg;

import static ca.uqac.lif.cep.Connector.INPUT;
import static ca.uqac.lif.cep.Connector.OUTPUT;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.Connector.ConnectorException;
import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.functions.ArgumentPlaceholder;
import ca.uqac.lif.cep.functions.BinaryFunction;
import ca.uqac.lif.cep.functions.Constant;
import ca.uqac.lif.cep.functions.Function;
import ca.uqac.lif.cep.functions.FunctionProcessor;
import ca.uqac.lif.cep.functions.FunctionTree;
import ca.uqac.lif.cep.tmf.Window;

/**
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
	 * 
	 * @param pattern
	 * @param n
	 * @param beta
	 * @param delta
	 * @param d
	 * @param comp
	 */
	public TrendDistance(P pattern, int n, Processor beta, Function delta, R d, BinaryFunction<R,R,Boolean> comp)
	{
		super(1, 1);
		try
		{
			Window wp = new Window(beta, n);
			associateInput(INPUT, wp, INPUT);
			FunctionProcessor distance = new FunctionProcessor(new FunctionTree(delta,
					new Constant(pattern),
					new ArgumentPlaceholder(0)
					));
			Connector.connect(wp, distance);
			FunctionProcessor too_far = new FunctionProcessor(new FunctionTree(comp,
					new ArgumentPlaceholder(0),
					new Constant(d)
					));
			Connector.connect(distance, too_far);
			associateOutput(OUTPUT, too_far, OUTPUT);
		} 
		catch (ConnectorException e) 
		{
			e.printStackTrace();
		}
	}
}

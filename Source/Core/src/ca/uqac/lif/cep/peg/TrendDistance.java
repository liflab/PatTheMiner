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
		Window wp = new Window(beta, n);
		associateInput(INPUT, wp, INPUT);
		ApplyFunction distance = new ApplyFunction(new FunctionTree(delta,
				new Constant(pattern),
				new StreamVariable(0)
				));
		Connector.connect(wp, distance);
		ApplyFunction too_far = new ApplyFunction(new FunctionTree(comp,
				new StreamVariable(0),
				new Constant(d)
				));
		Connector.connect(distance, too_far);
		associateOutput(OUTPUT, too_far, OUTPUT);
	}
}

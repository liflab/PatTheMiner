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
package ca.uqac.lif.cep.peg.forecast;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.UniformProcessor;
import ca.uqac.lif.cep.functions.ApplyFunction;
import ca.uqac.lif.cep.functions.Constant;
import ca.uqac.lif.cep.functions.Cumulate;
import ca.uqac.lif.cep.functions.CumulativeFunction;
import ca.uqac.lif.cep.functions.Function;
import ca.uqac.lif.cep.functions.FunctionTree;
import ca.uqac.lif.cep.functions.TurnInto;
import ca.uqac.lif.cep.tmf.Filter;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.tmf.Slice;
import ca.uqac.lif.cep.tmf.SliceLast;
import ca.uqac.lif.cep.tmf.Window;
import ca.uqac.lif.cep.util.Bags;
import ca.uqac.lif.cep.util.Maps;
import ca.uqac.lif.cep.util.Numbers;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SelfLearningPrediction extends GroupProcessor
{
  public SelfLearningPrediction(Function slice, Processor phi, int m, int t, Processor kappa, int n, Processor classifier)
  {
    super(1, 1);
    Fork main_fork = new Fork(3);
    // Branch 1
    SliceLast s_last = new SliceLast(slice, new PredictiveLearning.LearningSlice(phi, m, t, kappa, n));
    Connector.connect(main_fork, 0, s_last, 0);
    Connector.connect(s_last, classifier);
    // Branch 2
    Slice s_pred = new Slice(slice, new Window(phi, m));
    Connector.connect(main_fork, 1, s_pred, 0);
    Filter filter = new Filter();
    Connector.connect(s_pred, 0, filter, 0);
    // Branch 3
    GroupProcessor sum = new GroupProcessor(1, 1);
    {
      TurnInto to = new TurnInto(1);
      Cumulate add = new Cumulate(new CumulativeFunction<Number>(Numbers.addition));
      Connector.connect(to, add);
      sum.associateInput(0, to, 0);
      sum.associateOutput(0, add, 0);
      sum.addProcessors(to, add);
    }
    Slice s_count = new Slice(slice, sum);
    Connector.connect(main_fork, 2, s_count, 0);
    ApplyFunction count = new ApplyFunction(new FunctionTree(Numbers.isGreaterOrEqual,
        new FunctionTree(Bags.maxValue, Maps.values),
        new Constant(t + m - n)));
    Connector.connect(s_count, count);
    Connector.connect(count, 0, filter, 1);
    ApplyToValues av = new ApplyToValues();
    Connector.connect(classifier, 0, av, 0);
    Connector.connect(filter, 0, av, 1);
    addProcessors(main_fork, s_last, classifier, s_pred, filter, s_count, count, av);
    associateInput(0, main_fork, 0);
    associateOutput(0, av, 0);
  }
  
  /**
   * Processor that receives two streams. The first is a stream of functions,
   * and the second is a stream of maps. Upon receiving a function and a map,
   * the processor returns a new map by applying the function to each value
   * of the original map.
   */
  public static class ApplyToValues extends UniformProcessor
  {
    /**
     * Creates a new instance of the processor
     */
    public ApplyToValues()
    {
      super(2, 1);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected boolean compute(Object[] inputs, Object[] outputs)
    {
      Function f = (Function) inputs[0];
      Map<Object,Object> map = (Map<Object,Object>) inputs[1];
      Map<Object,Object> out_map = new HashMap<Object,Object>();
      for (Map.Entry<Object,Object> e : map.entrySet())
      {
        Object[] in_args = new Object[] {e.getValue()};
        Object[] out_args = new Object[1];
        f.evaluate(in_args, out_args);
        out_map.put(e.getKey(), out_args[0]);
      }
      outputs[0] = out_map;
      return true;
    }

    @Override
    public Processor duplicate(boolean with_state)
    {
      if (with_state)
      {
        throw new UnsupportedOperationException("This processor does not support stateful duplication");
      }
      return new ApplyToValues();
    }
    
    @Override
    public void getInputTypesFor(Set<Class<?>> classes, int index)
    {
      if (index == 0)
      {
        classes.add(Function.class);
      }
      if (index == 1)
      {
        classes.add(Map.class);
      }
    }
    
    @Override
    public Class<?> getOutputType(int index)
    {
      return Map.class;
    }
  }
}

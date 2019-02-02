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
package ca.uqac.lif.cep.peg.ml;

import static ca.uqac.lif.cep.Connector.BOTTOM;
import static ca.uqac.lif.cep.Connector.INPUT;
import static ca.uqac.lif.cep.Connector.OUTPUT;
import static ca.uqac.lif.cep.Connector.TOP;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.functions.ApplyFunction;
import ca.uqac.lif.cep.functions.BinaryFunction;
import ca.uqac.lif.cep.peg.weka.UpdateClassifier;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.tmf.Trim;
import ca.uqac.lif.cep.tmf.Window;
import java.util.Collection;

/**
 * Processor chain that trains a classifier by associating a collection of
 * feature values computed by a processor &beta; on a window, to a
 * <em>class</em> computed by a processor &kappa; on a subsequent window.
 * @param beta The processor calculating the feature values on a window
 * of events. It can produce either a collection/array or a single value.
 * @param kappa The processor calculating the class from a window of
 * events.
 * @param uc A processor that updates a classifier based on the
 * feature/class pairs produced by &beta; and &kappa;
 * @param t The offset (in number of events) between the "feature" window
 * and the "class" window
 * @param n The width of the "class" window
 * @param m The width of the "feature" window
 */
public class ClassifierTraining extends GroupProcessor
{
  public ClassifierTraining(Processor beta, Processor kappa, 
      UpdateClassifier uc, int t, int n, int m)
  {
    super(1, 1);
    Fork f1 = new Fork(2);
    Trim trim = new Trim(t);
    Connector.connect(f1, TOP, trim, INPUT);
    Window win_top = new Window(kappa, n);
    Connector.connect(trim, win_top);
    Window win_bot = new Window(beta, m);
    Connector.connect(f1, BOTTOM, win_bot, INPUT);
    ApplyFunction merge = new ApplyFunction(MergeIntoArray.instance);
    Connector.connect(win_top, OUTPUT, merge, TOP);
    Connector.connect(win_bot, OUTPUT, merge, BOTTOM);
    Connector.connect(merge, uc);
    addProcessors(f1, trim, win_top, win_bot, merge, uc);
    associateInput(INPUT, f1, INPUT);
    associateOutput(OUTPUT, uc, OUTPUT);
  }
  
  /**
   * Special-purpose function that merges a scalar value <i>x</i> and a
   * collection <i>y</i> of size <i>n</i> and a* scalar value <i>x</i> into an
   * array of size <i>n</i>+1 where
   * <i>x</i> is placed at the last position.
   */
  protected static class MergeIntoArray extends BinaryFunction<Object,Object,Object[]>
  {
    /**
     * A reference to a single instance of the function
     */
    public static final transient MergeIntoArray instance = new MergeIntoArray();
    
    protected MergeIntoArray()
    {
      super(Object.class, Object.class, Object[].class);
    }

    @Override
    public Object[] getValue(Object x, Object y)
    {
      if (y instanceof Collection<?>)
      {
        Collection<?> c = (Collection<?>) y;
        Object[] z = new Object[c.size() + 1];
        int i = 0;
        for (Object o : c)
        {
          z[i] = o;
          i++;
        }
        z[i] = x;
        return z;
      }
      if (y.getClass().isArray())
      {
        Object[] c = (Object[]) y;
        Object[] z = new Object[c.length + 1];
        for (int i = 0; i < c.length; i++)
        {
          z[i] = c[i];
        }
        z[c.length] = x;
        return z;
      }
      return new Object[] {y, x};
    }
  }
}

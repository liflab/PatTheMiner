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

import static ca.uqac.lif.cep.Connector.BOTTOM;
import static ca.uqac.lif.cep.Connector.INPUT;
import static ca.uqac.lif.cep.Connector.OUTPUT;
import static ca.uqac.lif.cep.Connector.TOP;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.functions.ApplyFunction;
import ca.uqac.lif.cep.functions.Function;
import ca.uqac.lif.cep.peg.weka.UpdateClassifier;
import ca.uqac.lif.cep.peg.weka.WekaUtils;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.tmf.SliceLast;
import ca.uqac.lif.cep.tmf.Trim;
import ca.uqac.lif.cep.tmf.Window;

/**
 * Processor chain that trains a classifier by associating a collection of
 * feature values computed by a processor &phi; on a window, to a class
 * computed by a processor &kappa; on another window. Graphically, this chain
 * corresponds to the following diagram:
 * <p>
 * <img src="{@docRoot}/doc-files/Learning.png" alt="Processor chain">
 */
public class PredictiveLearning extends GroupProcessor
{
  public PredictiveLearning(Function slice, Processor phi, int m, int t, Processor kappa, int n, UpdateClassifier uc)
  {
    super(1, 1);
    SliceLast slicer = new SliceLast(slice, new LearningSlice(phi, m, t, kappa, n));
    Connector.connect(slicer, uc);
    addProcessors(slicer, uc);
    associateInput(0, slicer, 0);
  }
  
  public static class LearningSlice extends GroupProcessor
  {
    public LearningSlice(Processor phi, int m, int t, Processor kappa, int n)
    {
      super(1, 1);
      Fork f1 = new Fork(2);
      Trim trim = new Trim(t);
      Connector.connect(f1, TOP, trim, INPUT);
      Window win_top = new Window(kappa, n);
      Connector.connect(trim, win_top);
      Window win_bot = new Window(phi, m);
      Connector.connect(f1, BOTTOM, win_bot, INPUT);
      ApplyFunction merge = new ApplyFunction(WekaUtils.MergeIntoArray.instance);
      Connector.connect(win_top, OUTPUT, merge, TOP);
      Connector.connect(win_bot, OUTPUT, merge, BOTTOM);
      addProcessors(f1, trim, win_top, win_bot, merge);
      associateInput(INPUT, f1, INPUT);
      associateOutput(OUTPUT, merge, OUTPUT);
    }
  }
}

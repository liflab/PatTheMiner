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
import ca.uqac.lif.cep.peg.weka.UpdateClassifier;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.tmf.Trim;
import ca.uqac.lif.cep.tmf.Window;

public class SelfClassPrediction extends GroupProcessor
{
  public SelfClassPrediction(Processor beta, Processor kappa, UpdateClassifier uc, int t, int n, int m)
  {
    super(1, 1);
    Fork f1 = new Fork(2);
    Trim trim = new Trim(t);
    Connector.connect(f1, TOP, trim, INPUT);
    Window win_top = new Window(kappa, n);
    Connector.connect(trim, win_top);
    Window win_bot = new Window(beta, m);
    Connector.connect(f1, BOTTOM, win_bot, INPUT);
    Fork f2 = new Fork(2);
    Connector.connect(win_bot, f2);
    
    associateInput(INPUT, f1, INPUT);
  }
  
  
}
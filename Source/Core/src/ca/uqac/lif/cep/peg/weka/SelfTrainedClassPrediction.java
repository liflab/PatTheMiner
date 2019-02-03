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
package ca.uqac.lif.cep.peg.weka;

import static ca.uqac.lif.cep.Connector.BOTTOM;
import static ca.uqac.lif.cep.Connector.INPUT;
import static ca.uqac.lif.cep.Connector.LEFT;
import static ca.uqac.lif.cep.Connector.OUTPUT;
import static ca.uqac.lif.cep.Connector.TOP;
import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.functions.ApplyFunction;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.tmf.Insert;
import ca.uqac.lif.cep.tmf.Window;
import weka.classifiers.Classifier;

/**
 * <p>
 * <img src="{@docRoot}/doc-files/SelfTrainedClassPrediction.png" alt="Processor chain">
 */
public class SelfTrainedClassPrediction extends GroupProcessor
{
  public SelfTrainedClassPrediction(ClassifierTraining ct, Processor beta, Classifier c, int t, int n, int m)
  {
    super(1, 1);
    Fork f = new Fork(2);
    Connector.connect(f, TOP, ct, INPUT);
    Window win = new Window(beta, m);
    Connector.connect(f, BOTTOM, win, INPUT);
    Insert ins = new Insert(t + n - m, c);
    Connector.connect(ct, ins);
    ApplyFunction af = new ApplyFunction(new WekaUtils.EvaluateClassifier(ct.getDataset(), ct.getAttributes()));
    Connector.connect(ins, OUTPUT, af, LEFT);
    Connector.connect(win, OUTPUT, af, BOTTOM);
    associateInput(INPUT, f, INPUT);
    associateOutput(OUTPUT, af, OUTPUT);
  }
}
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
import ca.uqac.lif.cep.Pushable;
import ca.uqac.lif.cep.functions.ApplyFunction;
import ca.uqac.lif.cep.functions.Constant;
import ca.uqac.lif.cep.functions.RaiseArity;
import ca.uqac.lif.cep.peg.weka.UpdateClassifier;
import ca.uqac.lif.cep.peg.weka.WekaUtils;
import ca.uqac.lif.cep.tmf.SinkLast;
import ca.uqac.lif.cep.util.NthElement;
import static org.junit.Assert.*;
import org.junit.Test;
import weka.classifiers.Classifier;
import weka.classifiers.trees.Id3;
import weka.core.Attribute;
import weka.core.Instance;

/**
 * Unit tests for the {@link ClassifierTraining} processor.
 */
public class PredictiveLearningTest
{
  /**
   * Simple test. Input events are arrays made of 0/1 (attribute "A")
   * and a Boolean (attribute "B"). The classifier is trained by trying to
   * use the value of attribute A to predict the value of B in the same event
   * (i.e. t = 0, m = n = 0).
   */
  @Test
  public void test1() throws Exception
  {
    Classifier cl_out;
    Instance inst;
    Attribute[] attributes = new Attribute[] {
        WekaUtils.createAttribute("A", "0", "1"),
        WekaUtils.createAttribute("B", "true", "false")
    };
    ApplyFunction beta = new ApplyFunction(new NthElement(0));
    ApplyFunction kappa = new ApplyFunction(new NthElement(1));
    UpdateClassifier uc = new UpdateClassifier(new Id3(), "test", attributes);
    PredictiveLearning ct = new PredictiveLearning(new RaiseArity(1, new Constant(0)), beta, 1, 0, kappa, 1, uc);
    SinkLast sink = new SinkLast();
    Connector.connect(ct, sink);
    Pushable p = ct.getPushableInput();
    // Push (0,T)
    p.push(new Object[] {"0", "true"});
    cl_out = (Classifier) sink.getLast()[0];
    assertNotNull(cl_out);
    // The classifier should associate (0,?) to class T (index 0)
    inst = WekaUtils.createInstanceFromArray(uc.getDataset(), new Object[] {"0", null}, attributes);
    assertEquals("true", WekaUtils.getClassValue(cl_out.classifyInstance(inst), attributes));
    // Push (1,F)
    p.push(new Object[] {"1", "false"});
    cl_out = (Classifier) sink.getLast()[0];
    assertNotNull(cl_out);
    // The classifier should associate (0,?) to class T (index 0)
    inst = WekaUtils.createInstanceFromArray(uc.getDataset(), new Object[] {"0", null}, attributes);
    assertEquals("true", WekaUtils.getClassValue(cl_out.classifyInstance(inst), attributes));
    // The classifier should associate (1,?) to class F (index 1)
    inst = WekaUtils.createInstanceFromArray(uc.getDataset(), new Object[] {"1", null}, attributes);
    assertEquals("false", WekaUtils.getClassValue(cl_out.classifyInstance(inst), attributes));
  }
}

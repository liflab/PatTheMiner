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

import static org.junit.Assert.*;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.Pushable;
import ca.uqac.lif.cep.functions.ApplyFunction;
import ca.uqac.lif.cep.functions.Constant;
import ca.uqac.lif.cep.functions.FunctionTree;
import ca.uqac.lif.cep.functions.RaiseArity;
import ca.uqac.lif.cep.peg.weka.UpdateClassifier;
import ca.uqac.lif.cep.peg.weka.WekaUtils;
import ca.uqac.lif.cep.tmf.SinkLast;
import ca.uqac.lif.cep.util.Bags;
import ca.uqac.lif.cep.util.NthElement;
import org.junit.Test;
import weka.classifiers.Classifier;
import weka.classifiers.trees.Id3;
import weka.core.Attribute;

/**
 * Unit tests for the {@link SelfTrainedClassPrediction} processor.
 */
public class SelfLearningPredictionTest
{
  @Test
  public void test1()
  {
    int t = 0, m = 1, n = 1;
    Attribute[] attributes = new Attribute[] {
        WekaUtils.createAttribute("A", "foo", "bar"), 
        WekaUtils.createAttribute("class", "Y", "Z")};
    Classifier cl = new Id3();
    UpdateClassifier uc = new UpdateClassifier(cl, "test", attributes);
    ApplyFunction phi = new ApplyFunction(new NthElement(0));
    ApplyFunction kappa = new ApplyFunction(new NthElement(1));
    SelfLearningPrediction stcp = new SelfLearningPrediction(new RaiseArity(1, new Constant(0)), phi.duplicate(), m, t, kappa.duplicate(), n, uc);
    SinkLast sink = new SinkLast();
    Connector.connect(stcp, sink);
    Pushable p = stcp.getPushableInput();
    p.push(new Object[] {"foo", "Y"});
    assertEquals("Y", sink.getLast()[0]);
    p.push(new Object[] {"foo", "Y"});
    assertEquals("Y", sink.getLast()[0]);
    p.push(new Object[] {"bar", "Z"});
    assertEquals("Z", sink.getLast()[0]);
  }
  
  @Test
  public void test2()
  {
    int t = 1, m = 1, n = 1;
    Attribute[] attributes = new Attribute[] {
        WekaUtils.createAttribute("A", "foo", "bar"),
        WekaUtils.createAttribute("B", "0", "1"), 
        WekaUtils.createAttribute("class", "Y", "Z")};
    Classifier cl = new Id3();
    UpdateClassifier uc = new UpdateClassifier(cl, "test", attributes);
    ApplyFunction phi = new ApplyFunction(new FunctionTree(
        new Bags.ToArray(String.class, String.class),
        new NthElement(0),
        new NthElement(1)));
    ApplyFunction kappa = new ApplyFunction(new NthElement(2));
    SelfLearningPrediction stcp = new SelfLearningPrediction(new RaiseArity(1, new Constant(0)), phi.duplicate(), m, t, kappa.duplicate(), n, uc);
    SinkLast sink = new SinkLast();
    Connector.connect(stcp, sink);
    Pushable p = stcp.getPushableInput();
    p.push(new Object[] {"foo", "0", "Y"});
    assertEquals(0l, uc.getInstanceCount());
    assertNull(sink.getLast()); // The chain does not output a class before t+n-m events
    p.push(new Object[] {"bar", "1", "Y"});
    assertEquals(1l, uc.getInstanceCount());
    assertEquals("Y", sink.getLast()[0]);
    p.push(new Object[] {"bar", "1", "Z"});
    assertEquals(2l, uc.getInstanceCount());
    assertEquals("Z", sink.getLast()[0]);
    p.push(new Object[] {"foo", "1", "Z"});
    assertEquals("Y", sink.getLast()[0]);
  }
}

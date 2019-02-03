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

import static org.junit.Assert.*;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.Pushable;
import ca.uqac.lif.cep.functions.ApplyFunction;
import ca.uqac.lif.cep.tmf.SinkLast;
import ca.uqac.lif.cep.util.NthElement;
import org.junit.Test;
import weka.classifiers.Classifier;
import weka.classifiers.trees.Id3;
import weka.core.Attribute;

/**
 * Unit tests for the {@link SelfTrainedClassPrediction} processor.
 */
public class SelfTrainedClassPredictionTest
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
    ApplyFunction beta = new ApplyFunction(new NthElement(0));
    ApplyFunction kappa = new ApplyFunction(new NthElement(1));
    ClassifierTraining ct = new ClassifierTraining(beta.duplicate(), kappa.duplicate(), uc, t, n, m);
    SelfTrainedClassPrediction stcp = new SelfTrainedClassPrediction(ct, beta.duplicate(), new Id3(), t, n, m);
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
        WekaUtils.createAttribute("class", "Y", "Z")};
    Classifier cl = new Id3();
    UpdateClassifier uc = new UpdateClassifier(cl, "test", attributes);
    ApplyFunction beta = new ApplyFunction(new NthElement(0));
    ApplyFunction kappa = new ApplyFunction(new NthElement(1));
    ClassifierTraining ct = new ClassifierTraining(beta.duplicate(), kappa.duplicate(), uc, t, n, m);
    SelfTrainedClassPrediction stcp = new SelfTrainedClassPrediction(ct, beta.duplicate(), new Id3(), t, n, m);
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
}

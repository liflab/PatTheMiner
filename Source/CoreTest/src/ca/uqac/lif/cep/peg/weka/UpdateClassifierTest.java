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

import org.junit.Test;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.Pushable;
import ca.uqac.lif.cep.peg.weka.UpdateClassifier;
import ca.uqac.lif.cep.tmf.SinkLast;
import weka.classifiers.Classifier;
import weka.classifiers.trees.Id3;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.Instance;

/**
 * Unit tests for the {@link UpdateClassifier} processor.
 */
public class UpdateClassifierTest
{
  @Test
  public void testUpdate1() throws Exception
  {
    Attribute[] attributes = new Attribute[] {
        new Attribute("a"), 
        WekaUtils.createAttribute("class", "A", "B")};
    Classifier cl = new J48();
    Classifier cl_out;
    UpdateClassifier uc = new UpdateClassifier(cl, "test", attributes);
    SinkLast sink = new SinkLast();
    Connector.connect(uc, sink);
    Pushable p = uc.getPushableInput();
    Object[] input_array = new Object[] {10, "A"};
    p.push(input_array);
    cl_out = (Classifier) sink.getLast()[0];
    assertNotNull(cl_out);
    // Create an instance and check what the classifier does with it
    Instance inst = WekaUtils.createInstanceFromArray(uc.getDataset(), new Object[] {10, null}, attributes);
    double d = cl_out.classifyInstance(inst);
    assertEquals("A", WekaUtils.getClassValue(d, attributes)); // a=10 should be associated to class A
  }
  
  @Test
  public void testUpdate2() throws Exception
  {
    double d;
    Instance inst;
    Attribute[] attributes = new Attribute[] {
        WekaUtils.createAttribute("A", "foo", "bar"), 
        WekaUtils.createAttribute("class", "Y", "Z")};
    Classifier cl = new Id3();
    Classifier cl_out;
    UpdateClassifier uc = new UpdateClassifier(cl, "test", attributes);
    SinkLast sink = new SinkLast();
    Connector.connect(uc, sink);
    Pushable p = uc.getPushableInput();
    p.push(new Object[] {"foo", "Y"});
    p.push(new Object[] {"bar", "Z"});
    cl_out = (Classifier) sink.getLast()[0];
    assertNotNull(cl_out);
    // Create an instance and check what the classifier does with it
    inst = WekaUtils.createInstanceFromArray(uc.getDataset(), new Object[] {"foo", null}, attributes);
    d = cl_out.classifyInstance(inst);
    assertEquals("Y", WekaUtils.getClassValue(d, attributes)); // A=foo should be associated to class Y
    inst = WekaUtils.createInstanceFromArray(uc.getDataset(), new Object[] {"bar", null}, attributes);
    d = cl_out.classifyInstance(inst);
    assertEquals("Z", WekaUtils.getClassValue(d, attributes)); // A=bar should be associated to class Z
  }
}

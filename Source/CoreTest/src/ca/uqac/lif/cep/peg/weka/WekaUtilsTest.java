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
import weka.classifiers.Classifier;
import weka.classifiers.trees.Id3;
import weka.core.Attribute;
import weka.core.Instances;

/**
 * Unit tests for the {@link WekaUtils} utility class. 
 */
public class WekaUtilsTest
{
  @Test
  public void testEvaluateClassifier1() throws Exception
  {
    Attribute[] attributes = new Attribute[] {
        WekaUtils.createAttribute("A", "0", "1"),
        WekaUtils.createAttribute("B", "true", "false")
    };
    Classifier cl = new Id3();
    Instances dataset = WekaUtils.createInstances("test", 10, attributes);
    dataset.add(WekaUtils.createInstanceFromArray(dataset, new Object[] {"0", "true"}, attributes));
    dataset.add(WekaUtils.createInstanceFromArray(dataset, new Object[] {"1", "false"}, attributes));
    cl.buildClassifier(dataset);
    WekaUtils.EvaluateClassifier ev_c = new WekaUtils.EvaluateClassifier(dataset, attributes);
    Object[] out_array = new Object[1];
    ev_c.evaluate(new Object[] {cl, new Object[] {"0", null}}, out_array);
    assertEquals("true", out_array[0]);
    ev_c.evaluate(new Object[] {cl, new Object[] {"1", null}}, out_array);
    assertEquals("false", out_array[0]);
  }
  
  @Test
  public void testEvaluateClassifier2() throws Exception
  {
    Attribute[] attributes = new Attribute[] {
        WekaUtils.createAttribute("A", "0", "1"),
        WekaUtils.createAttribute("B", "0", "1"),
        WekaUtils.createAttribute("C", "true", "false")
    };
    Classifier cl = new Id3();
    Instances dataset = WekaUtils.createInstances("test", 10, attributes);
    dataset.add(WekaUtils.createInstanceFromArray(dataset, new Object[] {"0", "0", "true"}, attributes));
    dataset.add(WekaUtils.createInstanceFromArray(dataset, new Object[] {"1", "0", "false"}, attributes));
    cl.buildClassifier(dataset);
    WekaUtils.EvaluateClassifier ev_c = new WekaUtils.EvaluateClassifier(dataset, attributes);
    Object[] out_array = new Object[1];
    ev_c.evaluate(new Object[] {cl, new Object[] {"0", "0", null}}, out_array);
    assertEquals("true", out_array[0]);
    ev_c.evaluate(new Object[] {cl, new Object[] {"1", "1", null}}, out_array);
    assertEquals("false", out_array[0]);
  }
}

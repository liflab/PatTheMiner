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

import java.util.Map;
import java.util.Queue;

import org.junit.Test;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.Pushable;
import ca.uqac.lif.cep.functions.Constant;
import ca.uqac.lif.cep.functions.IdentityFunction;
import ca.uqac.lif.cep.functions.RaiseArity;
import ca.uqac.lif.cep.peg.weka.RoteClassifier;
import ca.uqac.lif.cep.peg.weka.WekaUtils;
import ca.uqac.lif.cep.tmf.Passthrough;
import ca.uqac.lif.cep.tmf.QueueSink;
import ca.uqac.lif.cep.util.NthElement;
import ca.uqac.lif.cep.util.Numbers;
import weka.core.Attribute;
import weka.core.Instances;

public class StaticPredictionTest
{
  @SuppressWarnings("unchecked")
  @Test
  public void test1()
  {
    Map<Object,Object> map;
    StaticPrediction sp = new StaticPrediction(new RaiseArity(1, new Constant(0)), new Passthrough(), 1, new IdentityFunction(1));
    QueueSink sink = new QueueSink();
    Connector.connect(sp, sink);
    Queue<Object> q = sink.getQueue();
    Pushable p = sp.getPushableInput();
    p.push(6);
    map = (Map<Object,Object>) q.remove();
    assertEquals(1, map.size());
    assertEquals(6, map.get(0));
    p.push(2);
    map = (Map<Object,Object>) q.remove();
    assertEquals(1, map.size());
    assertEquals(2, map.get(0));
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void test2()
  {
    Map<Object,Object> map;
    StaticPrediction sp = new StaticPrediction(Numbers.isEven, new Passthrough(), 1, new IdentityFunction(1));
    QueueSink sink = new QueueSink();
    Connector.connect(sp, sink);
    Queue<Object> q = sink.getQueue();
    Pushable p = sp.getPushableInput();
    p.push(6);
    map = (Map<Object,Object>) q.remove();
    assertEquals(1, map.size());
    assertEquals(6, map.get(true));
    p.push(3);
    map = (Map<Object,Object>) q.remove();
    assertEquals(2, map.size());
    assertEquals(6, map.get(true));
    assertEquals(3, map.get(false));
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void testWeka()
  {
    Map<Object,Object> map;
    
    Attribute[] attributes = new Attribute[] {
        WekaUtils.createAttribute("A", "0", "1"),
        WekaUtils.createAttribute("B", "0", "1"),
        WekaUtils.createAttribute("C", "foo", "bar", "baz")
    };
    RoteClassifier rc = new RoteClassifier(attributes);
    Instances dataset = WekaUtils.createInstances("test", 10, attributes);
    rc.addAssociation(WekaUtils.createInstanceFromArray(dataset, new Object[] {"0", "1", null}, attributes), 0);
    rc.addAssociation(WekaUtils.createInstanceFromArray(dataset, new Object[] {"1", "0", null}, attributes), 2);
    StaticPrediction sp = new StaticPrediction(new NthElement(0), new Passthrough(), 1, new WekaUtils.ClassifierFunction(rc, dataset, attributes));
    QueueSink sink = new QueueSink();
    Connector.connect(sp, sink);
    Queue<Object> q = sink.getQueue();
    Pushable p = sp.getPushableInput();
    p.push(new Object[] {"0", "1"});
    map = (Map<Object,Object>) q.remove();
    assertEquals(1, map.size());
    assertEquals("foo", map.get("0"));
    p.push(new Object[] {"1", "0"});
    map = (Map<Object,Object>) q.remove();
    assertEquals(2, map.size());
    assertEquals("foo", map.get("0"));
    assertEquals("baz", map.get("1"));
  }  
}

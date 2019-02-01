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

import static org.junit.Assert.*;

import org.junit.Test;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.tmf.SinkLast;
import weka.classifiers.Classifier;
import weka.classifiers.trees.Id3;
import weka.core.Attribute;

public class UpdateClassifierTest
{
  @Test
  public void testUpdate1()
  {
    Classifier cl = new Id3();
    Classifier cl_out;
    UpdateClassifier uc = new UpdateClassifier(cl, "test", new Attribute("a"), new Attribute("class"));
    SinkLast sink = new SinkLast();
    Connector.connect(uc, sink);
    cl_out = (Classifier) sink.getLast()[0];
    
  }
}

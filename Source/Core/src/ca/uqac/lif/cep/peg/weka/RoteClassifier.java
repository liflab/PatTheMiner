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

import java.util.HashMap;
import java.util.Map;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

/**
 * A simple classifier that simply memorizes direct associations between
 * features and classes. It is mostly used for testing purposes.
 */
public class RoteClassifier extends Classifier
{
  /**
   * Dummy UID
   */
  private static final long serialVersionUID = 1L;

  protected Map<Instance,Double> m_associations;

  protected Attribute[] m_attributes;

  public RoteClassifier(Attribute ... attributes)
  {
    super();
    m_associations = new HashMap<Instance,Double>();
    m_attributes = attributes;
  }

  public void addAssociation(Instance inst, double class_index)
  {
    m_associations.put(inst, class_index);
  }



  @Override
  public void buildClassifier(Instances inst) throws Exception
  {
    // Do nothing
  }

  @Override
  public double classifyInstance(Instance inst)
  {
    for (Instance k_inst : m_associations.keySet())
    {
      if (WekaUtils.isEqual(k_inst, inst, m_attributes))
      {
        return m_associations.get(k_inst);
      }
    }
    return 0d;
  }

}
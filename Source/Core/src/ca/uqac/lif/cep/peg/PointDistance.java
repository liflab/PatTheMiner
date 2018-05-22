/*
    A BeepBeep palette for mining event traces
    Copyright (C) 2017-2018 Sylvain Hallé

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
package ca.uqac.lif.cep.peg;

import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.distance.DistanceMeasure;

import ca.uqac.lif.cep.functions.BinaryFunction;

public class PointDistance extends BinaryFunction<DoublePoint,DoublePoint,Number>
{
  /**
   * The distance measure to evaluate
   */
  /*@ non_null @*/ DistanceMeasure m_distance;
  
  /**
   * Creates a new point distance metric
   * @param d The distance measure to evaluate
   */
  public PointDistance(/*@ non_null @*/ DistanceMeasure d)
  {
    super(DoublePoint.class, DoublePoint.class, Number.class);
    m_distance = d;
  }
  
  @Override
  public /*@ non_null @*/ Number getValue(/*@ non_null @*/ DoublePoint dp1, /*@ non_null @*/ DoublePoint dp2)
  {
    return m_distance.compute(dp1.getPoint(), dp2.getPoint());
  }
}

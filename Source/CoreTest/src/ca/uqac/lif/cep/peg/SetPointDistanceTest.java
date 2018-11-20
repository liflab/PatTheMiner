/*
    A BeepBeep palette for mining event traces
    Copyright (C) 2017-2018 Sylvain Hallé and friends

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

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.junit.Test;

/**
 * Unit tests for the set point distance 
 */
public class SetPointDistanceTest
{
  @Test
  public void test1()
  {
    SetPointDistance spd = new SetPointDistance(new PointDistance(new EuclideanDistance()));
    Set<DoublePoint> c1 = getSet(getPoint(1, 1));
    Set<DoublePoint> c2 = getSet(getPoint(1, 1));
    double d = getDistance(spd, c1, c2);
    assertEquals(0d, d, 0.01d);
  }
  
  @Test
  public void test2()
  {
    SetPointDistance spd = new SetPointDistance(new PointDistance(new EuclideanDistance()));
    Set<DoublePoint> c1 = getSet(getPoint(2, 1));
    Set<DoublePoint> c2 = getSet(getPoint(1, 1));
    double d = getDistance(spd, c1, c2);
    assertEquals(1d, d, 0.01d);
  }
  
  @Test
  public void test3()
  {
    SetPointDistance spd = new SetPointDistance(new PointDistance(new EuclideanDistance()));
    Set<DoublePoint> c1 = getSet(getPoint(2, 1));
    Set<DoublePoint> c2 = getSet();
    double d = getDistance(spd, c1, c2);
    assertEquals(Double.MAX_VALUE, d, 0.01d);
  }
  
  @Test
  public void test4()
  {
    SetPointDistance spd = new SetPointDistance(new PointDistance(new EuclideanDistance()));
    Set<DoublePoint> c1 = getSet(getPoint(1, 1), getPoint(2, 1));
    Set<DoublePoint> c2 = getSet(getPoint(1, 1), getPoint(3, 1));
    double d = getDistance(spd, c1, c2);
    assertEquals(1d, d, 0.01d);
  }
  
  public static double getDistance(SetPointDistance spd, Set<DoublePoint> c1, Set<DoublePoint> c2)
  {
    Object[] val = new Object[1];
    spd.evaluate(new Object[] {c1, c2}, val);
    return (Double) val[0];
  }
  
  public static Set<DoublePoint> getSet(DoublePoint ... points)
  {
    Set<DoublePoint> c1 = new HashSet<DoublePoint>();
    for (DoublePoint dp : points)
    {
      c1.add(dp);
    }
    return c1;
  }
  
  public static DoublePoint getPoint(double ... coords)
  {
    return new DoublePoint(coords);
  }
}

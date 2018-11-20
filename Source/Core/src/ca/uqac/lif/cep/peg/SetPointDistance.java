/*
    A BeepBeep companion for MMT
    Copyright (C) 2018 Sylvain Hallé and friends

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

import ca.uqac.lif.cep.functions.BinaryFunction;
import java.util.Collection;
import java.util.HashSet;
import org.apache.commons.math3.ml.clustering.DoublePoint;

/**
 * Computes the distance between two sets of points. Given sets P and Q
 * of same cardinality n, a set of pairs {(p₁,q₁), &hellip; (pₙ,qₙ)}
 * &subseteq; 2<sup>P &times; Q</sup> is created
 * by finding the pair of points p &in; P and q &in; Q such that &Delta;(p,q)
 * is the smallest (for some {@link PointDistance} &Delta;), and restarting
 * the process with P \ {p} and Q \ {q}. The distance is then defined as
 * the sum of &Delta;(p,q) for i &in; [1,n]. If |P| ≠ |Q|, the distance is
 * defined as &infin;.
 * <p>
 * Let us illustrate this concept with the plot below.
 * <p>
 * <img src="{@docRoot}/doc-files/SetPointDistance-example.png" alt="Plot">
 * <p>
 * Here, set P is made of four 2D points (represented in green) and Q is made
 * of the four red points. Let us assume that &Delta; is the two-dimensional
 * Euclidean distance. Closest neighbors have already been matched, and their
 * distance is marked in black. In this case, the distance between P and Q
 * would be computed as 2 + 2 + √5 + √10 ≈ 9.4.
 * <p>
 * Note that this distance is dependent on the underlying {@link PointDistance}
 * that is being used. If we replace Euclidean distance by Manhattan distance,
 * pairs of points do not change, but the total distance becomes
 * 2 + 2 + 3 + 4 = 11.
 * <p>
 * The implementation of this function is a naïve nested loop that runs in
 * O(|P|<sup>3</sup>).
 */
@SuppressWarnings("rawtypes")
public class SetPointDistance extends BinaryFunction<Collection,Collection,Number>
{
  /**
   * The point distance used to measure the distance between points
   */
  /*@ not_null @*/ protected PointDistance m_distanceMetric;

  /**
   * The distance value used to represent "infinity"
   */
  protected double m_infinity = Double.MAX_VALUE;

  /**
   * Creates a new point set distance function.
   * @param distance The point distance used to measure the distance between
   * points 
   */
  public SetPointDistance(/*@ not_null @*/ PointDistance distance)
  {
    super(Collection.class, Collection.class, Number.class);
    m_distanceMetric = distance;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Number getValue(Collection x, Collection y)
  {
    if (x.size() != y.size())
    {
      return m_infinity;
    }
    int size = x.size();
    double distance = 0;
    // We copy x and y to new collections, since we will remove elements from them
    Collection<DoublePoint> c1 = new HashSet<DoublePoint>();
    c1.addAll((Collection<DoublePoint>) x);
    Collection<DoublePoint> c2 = new HashSet<DoublePoint>();
    c2.addAll((Collection<DoublePoint>) y);
    for (int i = 0; i < size; i++)
    {
      DoublePoint p1 = null, p2 = null;
      double d = m_infinity;
      for (DoublePoint dp1 : c1)
      {
        for (DoublePoint dp2 : c2)
        {
          double dist = evaluateDistance(dp1, dp2, m_distanceMetric);
          if (dist <= d)
          {
            d = dist;
            p1 = dp1;
            p2 = dp2;
          }
        }
      }
      distance += d;
      c1.remove(p1);
      c2.remove(p2);
    }
    return distance;
  }

  /**
   * Evaluates the distance between two points
   * @param p1 The first point
   * @param p2 The second point
   * @param dis The distance metric
   * @return The distance
   */
  /*@ pure @*/ protected static double evaluateDistance(/*@ non_null @*/ DoublePoint p1, /*@ non_null @*/ DoublePoint p2, PointDistance dis)
  {

    Object[] d = new Object[1];
    Object[] args = new Object[] {p1, p2};
    dis.evaluate(args, d);
    return (Double) d[0];
  }
}

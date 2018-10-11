/*
    A BeepBeep palette for mining event traces
    Copyright (C) 2017 Sylvain Hallé

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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;

import ca.uqac.lif.cep.functions.UnaryFunction;

/**
 * Computes the <a href="https://en.wikipedia.org/wiki/K-means_clustering"><i>K</i>-means
 * clustering</a> algorithm on a set of
 * points. The input of this function is a {@link Multiset} of objects,
 * and the output is a multiset of {@link DoublePoint}s representing
 * the coordinates of the centroids of the computed clusters.
 * The function is permissive, as it attempts to cast every element of
 * the input set into a {@link DoublePoint}, using
 * {@link DoublePointCast#getDoublePoint(Object)}. This means that
 * lists of numbers can be used instead of {@link DoublePoint}s.
 * 
 * @author Sylvain Hallé
 */
@SuppressWarnings("rawtypes")
public class KMeansFunction extends UnaryFunction<Collection,Set>
{
	/**
	 * The number of clusters to compute
	 */
	protected int m_k;
	
	/**
	 * Creates a new instance of the <i>K</i>-means function.
	 * @param k The value of <i>K</i>, i.e. the numbers of clusters in the
	 * resulting set.
	 */
	public KMeansFunction(int k)
	{
		super(Collection.class, Set.class);
		m_k = k;
	}
	
	@Override
	public Set<?> getValue(Collection points)
	{
		KMeansPlusPlusClusterer<DoublePoint> clusterer = new KMeansPlusPlusClusterer<DoublePoint>(m_k);
		Set<DoublePoint> d_points = new HashSet<DoublePoint>();
		for (Object o : points)
		{
			d_points.add(DoublePointCast.getDoublePoint(o));
		}
		List<CentroidCluster<DoublePoint>> clusters = clusterer.cluster(d_points);
		Set<DoublePoint> centers = new HashSet<DoublePoint>();
		for (CentroidCluster<DoublePoint> cluster : clusters)
		{
			centers.add((DoublePoint) cluster.getCenter());
		}
		return centers;
	}

}

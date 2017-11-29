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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;

public class KMeans extends ClusteringFunction
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2837095140235044456L;
	/**
	 * The number of clusters to compute
	 */
	protected int m_k;
	
	public KMeans(int k)
	{
		super();
		m_k = k;
	}
	
	@Override
	protected Set<DoublePoint> computeClustering(Set<DoublePoint> points)
	{
		KMeansPlusPlusClusterer<DoublePoint> clusterer = new KMeansPlusPlusClusterer<DoublePoint>(m_k);
		List<CentroidCluster<DoublePoint>> clusters = clusterer.cluster(points);
		Set<DoublePoint> centers = new HashSet<DoublePoint>();
		for (CentroidCluster<DoublePoint> cluster : clusters)
		{
			centers.add((DoublePoint) cluster.getCenter());
		}
		return centers;
	}

}

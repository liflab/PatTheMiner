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
	 * The number of clusters to compute
	 */
	protected int m_k;
	
	public KMeans(int k)
	{
		super();
		m_k = k;
	}
	
	@Override
	public Set<DoublePoint> computeClustering(Set<DoublePoint> points)
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

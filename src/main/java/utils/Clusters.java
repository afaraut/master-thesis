package utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Anthony on 25/05/2016.
 */
public class Clusters implements Serializable {
    public enum Clustering_method {
        KMEANS, DBSCAN, OPTICS
    }

    static private final long serialVersionUID = 66L;
    private Clustering_method m_method;
    private Map<String, Object> m_options;
    private int[] m_repartition;
    private HashMap<String, ArrayList<ClusterElement>> m_clusters;
    private ArrayList<ClusterElement> m_listClusterElement;

    public Clusters(Map<String, Object> options, int[] repartition, HashMap<String, ArrayList<ClusterElement>> clusters, ArrayList<ClusterElement> listClusterElement ) {
        m_method = null;
        m_options = options;
        m_repartition = repartition;
        m_clusters = clusters;
        m_listClusterElement = listClusterElement;
    }

    public void set_method(Clustering_method method) {
        m_method = method;
    }

    public Map<String, Object> get_options() {
        return m_options;
    }

    public int[] get_repartition() {
        return m_repartition;
    }

    public HashMap<String, ArrayList<ClusterElement>> get_mapClusters() {
        return m_clusters;
    }

    public List<ClusterElement> get_listClusterElement() {
        return m_listClusterElement;
    }

    public String toString() {
        if (m_method != null) {
            if (m_method == Clustering_method.DBSCAN) {
                return "DBSCAN clustering";
            } else if (m_method == Clustering_method.KMEANS) {
                return "KMEANS clustering";
            } else if (m_method == Clustering_method.OPTICS) {
                return "OPTICS clustering";
            }
        }
        return "Method not defined";
    }
}

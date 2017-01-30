package utils;

import java.io.Serializable;

/**
 * Created by Anthony on 20/06/2016.
 * Package : orianeClustering .
 * Project : PhDTrack.
 */
public class ClusterElement implements Serializable {

    static private final long serialVersionUID = 1L;
    private String m_idTwitter;
    private String m_text;
    private Integer m_numCluster;
    private Integer m_groundTruthCluster;

    public ClusterElement(String idTwitter, String text, Integer numCluster) {
        m_idTwitter = idTwitter;
        m_text = text;
        m_numCluster = numCluster;
    }

    public ClusterElement(String idTwitter, String text, Integer numCluster, Integer groundTruthCluster) {
        m_idTwitter = idTwitter;
        m_text = text;
        m_numCluster = numCluster;
        m_groundTruthCluster = groundTruthCluster;
    }

    public String get_idTwitter() {
        return m_idTwitter;
    }

    public String get_text() {
        return m_text;
    }

    public String toString() {
        return m_idTwitter + " - " + m_text;
    }

    public int get_numCluster() {
        return m_numCluster;
    }

    public void set_numCluster(Integer m_numCluster) {
        this.m_numCluster = m_numCluster;
    }

    public int get_groundTruthNumCluster() {
        return m_groundTruthCluster;
    }
}

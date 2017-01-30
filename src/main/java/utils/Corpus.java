package utils;

import weka.core.Instances;

import java.util.List;
import java.io.Serializable;

/**
 * Created by Anthony on 05/04/2016.
 */
public class Corpus implements Serializable {
    static private final long serialVersionUID = 6L;
    private List<String> m_texts;
    private List<String> m_ids;
    private Instances m_data;
    private Clusters m_clusters;

    public Corpus(List<String> texts, List<String> ids) {
        m_texts = texts;
        m_ids = ids;
        m_data = null;
        m_clusters = null;
    }

    public List<String> get_ids() {
        return m_ids;
    }

    public List<String> get_texts() {
        return m_texts;
    }

    public Instances get_data() {
        return m_data;
    }

    public void set_instances(Instances data) {
        m_data = data;
    }

    public void set_texts(List<String> texts) {
        m_texts = texts;
    }

    public Clusters get_clusters() {
        return m_clusters;
    }

    public void set_clusters(Clusters clusters) {
        this.m_clusters = clusters;
    }

}

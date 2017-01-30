package thesis;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.*;
import weka.clusterers.*;
import weka.core.*;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.AddID;
import weka.filters.unsupervised.attribute.Remove;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Anthony on 10/03/2016.
 */
public class Weka {

    /**
     * Allows to Add an ID to each Instance
     * @param data The instances
     * @return The instances with the ids
     * @throws Exception
     */
    public static Instances addID(Instances data) throws Exception {
        AddID addId = new AddID();
        addId.setInputFormat(data);
        return Filter.useFilter(data, addId);
    }

    /**
     * Allows to clean all the instances because Weka doesn't accept string attribute
     *
     * @param data The instances to clean
     * @return The instances cleaned
     * @throws Exception
     */
    public static Instances cleanInstances(Instances data, String[] optionsRemove) throws Exception {
        Remove remove = new Remove();            // new instance of filter
        remove.setOptions(optionsRemove);        // set options
        remove.setInputFormat(data);             // inform filter about dataset **AFTER** setting options
        return Filter.useFilter(data, remove);   // apply filter
    }

    /**
     * Create the model for the WEKA instances
     *
     * @param vectorLength Length of the vector in the instance
     * @return The model structure
     */
    public static Instances createWEKAInstances(String name, int vectorLength) {
        FastVector atts = new FastVector();
        atts.addElement(new Attribute("text", (FastVector) null)); // -- Add string
        atts.addElement(new Attribute("id", (FastVector) null));

        for (int i = 1; i <= vectorLength; i++) {
            atts.addElement(new Attribute("val" + i)); // -- Add numeric value
        }
        return new Instances(name, atts, 0);
    }

    public static Instances createWEKAInstancesOneClass(String name, int vectorLength) {
        FastVector atts = new FastVector();
        atts.addElement(new Attribute("text", (FastVector) null)); // -- Add string
        atts.addElement(new Attribute("id", (FastVector) null));

        FastVector fvClassVal = new FastVector(1);
        fvClassVal.addElement("positive");

        for (int i = 1; i <= vectorLength; i++) {
            atts.addElement(new Attribute("val" + i)); // -- Add numeric value
        }
        atts.addElement(new Attribute("theClass", fvClassVal));

        return new Instances(name, atts, 0);
    }

    public static Instance createWEKAInstanceOneClass(Instances data, String text, String id, String elemClass, double[] values) {
        double[] vals = new double[data.numAttributes()];
        vals[0] = data.attribute(0).addStringValue(text); // -- Add string
        vals[1] = data.attribute(1).addStringValue(id); // -- Add the id

        for (int i = 0; i < values.length; i++) {
            vals[i + 2] = values[i]; // -- Add numeric value
        }
        data.setClassIndex(data.numAttributes() - 1);
        if (elemClass.equals("positive")){
            vals[data.numAttributes() - 1] = 0; // 0 is the index of positive in the FastVector
        }
        else {
            vals[data.numAttributes() - 1] = Instance.missingValue(); // Unclassified element
        }
        return new Instance(1.0, vals);
    }

    public static Instances createWEKAInstancesWithClass(String name, int vectorLength) {
        FastVector atts = new FastVector();
        atts.addElement(new Attribute("text", (FastVector) null)); // -- Add string
        atts.addElement(new Attribute("id", (FastVector) null));

        FastVector fvClassVal = new FastVector(2);
        fvClassVal.addElement("positive");
        fvClassVal.addElement("negative");

        for (int i = 1; i <= vectorLength; i++) {
            atts.addElement(new Attribute("val" + i)); // -- Add numeric value
        }

        atts.addElement(new Attribute("theClass", fvClassVal));

        return new Instances(name, atts, 0);
    }

    public static Instance createWEKAInstanceWithClass(Instances data, String text, String id, String elemClass, double[] values) {
        double[] vals = new double[data.numAttributes()];
        vals[0] = data.attribute(0).addStringValue(text); // -- Add string
        vals[1] = data.attribute(1).addStringValue(id); // -- Add the id

        for (int i = 0; i < values.length; i++) {
            vals[i + 2] = values[i]; // -- Add numeric value
        }
        data.setClassIndex(data.numAttributes() - 1);
        if (elemClass.equals("positive")){
            vals[data.numAttributes() - 1] = 0; // 0 is the index of positive in the FastVector
        }
        else {
            vals[data.numAttributes() - 1] = 1; // 1 is the index of negative in the FastVector
        }

        return new Instance(1.0, vals);
    }

    /**
     * Create a WEKA instance according to the model previously defined
     *
     * @param data   List of all the instances already existing
     * @param text   The instance text
     * @param id     The instance id
     * @param values The instance values
     * @return The instance created thanks to the inputs
     */
    public static Instance createWEKAInstance(Instances data, String text, String id, double[] values) {

        double[] vals = new double[data.numAttributes()];
        vals[0] = data.attribute(0).addStringValue(text); // -- Add string
        vals[1] = data.attribute(1).addStringValue(id); // -- Add the id

        for (int i = 0; i < values.length; i++) {
            vals[i + 2] = values[i]; // -- Add numeric value
        }
        return new Instance(1.0, vals);
    }

    /**
     * Optics implementation with the WEKA library
     *
     * @param data      The input data for clustering in a WEKA format
     * @param epsilon   The distance of the neighborhood
     * @param minPoints The minimum points for a cluster to be considered as such
     * @return The optics object
     * @throws Exception
     */
    public static OPTICS optics(Instances data, double epsilon, int minPoints) throws Exception {
        OPTICS optics = new OPTICS();
        String[] options = new String[1];
        options[0] = "-no-gui";
        optics.setOptions(options);
        optics.setEpsilon(epsilon);
        optics.setMinPoints(minPoints);
        optics.buildClusterer(data);

        return optics;
    }

    /**
     * Kmeans implementation with the WEKA library
     *
     * @param data            The input data for clustering in a WEKA format
     * @param numberOfCluster The number of cluster wanted
     * @return The kmeans object
     * @throws Exception
     */
    public static SimpleKMeans kmeans(Instances data, int numberOfCluster) throws Exception {
        SimpleKMeans kMeans = new SimpleKMeans();
        kMeans.setNumClusters(numberOfCluster);
        kMeans.setPreserveInstancesOrder(true);
        kMeans.buildClusterer(data);

        return kMeans;
    }

    /**
     * DBscan implementation with the WEKA library
     *
     * @param data      The input data for clustering in a WEKA format
     * @param epsilon   The distance of the neighborhood
     * @param minPoints The minimum points for a cluster to be considered as such
     * @return The DBscan object
     * @throws Exception
     */
    public static DBSCAN dbscann(Instances data, double epsilon, int minPoints) throws Exception {
        DBSCAN dbscan = new DBSCAN();
        dbscan.setEpsilon(epsilon);
        dbscan.setMinPoints(minPoints);
        dbscan.buildClusterer(data);

        return dbscan;
    }

    /**
     * Implementation of the elbow method in order to find a K for Kmeans
     *
     * @param data The input data for clustering in a WEKA format
     * @throws Exception
     */
    public static void elbow_method(Instances data) throws Exception {
        double[] tab = new double[14];
        for (int i = 0; i < 14; i++) {
            SimpleKMeans kMeans = kmeans(data, i + 2);
            tab[i] = kMeans.getSquaredError();
        }
    }

    /**
     * Display optics results in a specific way (CSV)
     *
     * @param data      The input data for clustering in a WEKA format
     * @param epsilon   The distance of the neighborhood
     * @param minPoints The minimum points for a cluster to be considered as such
     * @return A string representation of the result
     * @throws Exception
     */
    public static Clusters opticsClusteringToSerialization(Corpus corpus, Instances data, double epsilon, int minPoints) throws Exception {
        OPTICS optics = optics(data, epsilon, minPoints);
        return formatClusteringResultsToSerialization(corpus, optics, data, epsilon + "; " + minPoints + "; ");
    }

    /**
     * Display kmeans results in a specific way (CSV)
     *
     * @param data            The input data for clustering in a WEKA format
     * @param numberOfCluster The number of cluster wanted
     * @return A string representation of the result
     * @throws Exception
     */
    public static Clusters kmeansClusteringToSerialization(Corpus corpus, Instances data, int numberOfCluster) throws Exception {
        SimpleKMeans kMeans = kmeans(data, numberOfCluster);
        return formatClusteringResultsToSerialization(corpus, kMeans, data, numberOfCluster + "; ");
    }

    /**
     * Display dbscan results in a specific way (CSV)
     *
     * @param data      The input data for clustering in a WEKA format
     * @param epsilon   The distance of the neighborhood
     * @param minPoints The minimum points for a cluster to be considered as such
     * @return A string representation of the result
     * @throws Exception
     */
    public static Clusters dbscanClusteringToSerialization(Corpus corpus, Instances data, double epsilon, int minPoints) throws Exception {
        DBSCAN dbscan = dbscann(data, epsilon, minPoints);
        return formatClusteringResultsToSerialization(corpus, dbscan, data, epsilon + "; " + minPoints + "; ");
    }

    public static Clusters formatClusteringResultsToSerialization(Corpus corpus, AbstractClusterer ac, Instances data, String params_option) throws Exception {
        int[] repartition = new int[ac.numberOfClusters()];
        int number_of_clustered_element = 0;
        int number_of_non_clustered_element = 0;

        for (int i = 0; i < data.numInstances(); i++) {
            Instance instance = data.instance(i);
            try {
                Integer cluster_nb = (ac.clusterInstance(instance) + 1);
                repartition[cluster_nb]++;
                number_of_clustered_element++;
            } catch (Exception e) {
                // Noise
                number_of_non_clustered_element++;
            }
        }

        int min_max[] = Toolbox.min_max_array(repartition);

        Map<String, Object> options = new HashMap<>();
        options.put("number_of_clusters", ac.numberOfClusters());
        options.put("min_elem", min_max[0]);
        options.put("max_elem", min_max[1]);
        options.put("total_nb_of_elem", (number_of_clustered_element + number_of_non_clustered_element));
        options.put("number_of_clustered_element", number_of_clustered_element);
        options.put("number_of_non_clustered_element", number_of_non_clustered_element);
        options.put("params_option", params_option);

        List<String> ids = corpus.get_ids();
        List<String> text = corpus.get_texts();

        ArrayList<ClusterElement> clusterElements = new ArrayList<>();
        HashMap<String, ArrayList<ClusterElement>> mapClusters = new HashMap<>();
        HashMap<String, ArrayList<String>> mapClustersString = new HashMap<>();
        for (int i = 0; i < data.numInstances(); i++) {
            Instance instance = data.instance(i);
            ArrayList<ClusterElement> list;
            ArrayList<String> listString;

            Integer clusterNumber;
            try {
                Integer cluster_nb = (ac.clusterInstance(instance));
                clusterNumber = cluster_nb;
            } catch (Exception e) { // Noise
                clusterNumber = 10000;
            }
            ClusterElement ce = new ClusterElement(ids.get(i), text.get(i), clusterNumber);
            String textElement = text.get(i);
            if (mapClusters.containsKey("" + clusterNumber)) {
                list = mapClusters.get("" + clusterNumber);
            } else {
                list = new ArrayList<>();
            }

            if (mapClustersString.containsKey("" + clusterNumber)){
                listString = mapClustersString.get("" + clusterNumber);
            }
            else {
                listString = new ArrayList<>();
            }

            list.add(ce);
            listString.add(textElement);

            clusterElements.add(ce);
            mapClusters.put(""+clusterNumber, list);
            mapClustersString.put(""+clusterNumber, listString);
        }

        return new Clusters(options, repartition, mapClusters, clusterElements);
    }
}
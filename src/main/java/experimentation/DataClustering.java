package experimentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thesis.Weka;
import utils.Clusters;
import utils.Corpus;
import utils.Toolbox;
import weka.core.Instances;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by Anthony on 18/05/2016.
 */
public class DataClustering {

    private static Logger log = LoggerFactory.getLogger(DataLoading.class);

    /**
     * Kmeans clustering with WEKA
     *
     * @param os     Some options and parameters for the clustering
     * @param corpus The corpus for the clustering
     * @return Clustering results
     * @throws Exception
     */
    public static Clusters kmeans(OptionsSerial os, Corpus corpus) throws Exception {
        HashMap<String, Object> params = os.get_dataClustering();
        Integer numberOfCluster = (Integer) params.get("numberOfCluster");

        String[] optionsRemove = new String[2];
        optionsRemove[0] = "-R";
        optionsRemove[1] = "1,2";

        Instances newData = Weka.cleanInstances(corpus.get_data(), optionsRemove);

        Clusters cls = Weka.kmeansClusteringToSerialization(corpus, newData, numberOfCluster);
        cls.set_method(Clusters.Clustering_method.KMEANS);

        return cls;
    }

    /**
     * DBscan clustering with WEKA
     *
     * @param os     Some options and parameters for the clustering
     * @param corpus The corpus for the clustering
     * @return Clustering results
     * @throws Exception
     */
    public static Clusters dbscan(OptionsSerial os, Corpus corpus) throws Exception {
        HashMap<String, Object> params = os.get_dataClustering();
        Double epsilon = (Double) params.get("epsilon");
        Integer minPoints = (Integer) params.get("minPoints");

        String[] optionsRemove = new String[2];
        optionsRemove[0] = "-R";
        optionsRemove[1] = "1,2";

        Instances newData = Weka.cleanInstances(corpus.get_data(), optionsRemove);

        Clusters cls = Weka.dbscanClusteringToSerialization(corpus, newData, epsilon, minPoints);
        cls.set_method(Clusters.Clustering_method.DBSCAN);

        return cls;
    }

    /**
     * Optics clustering with WEKA
     *
     * @param os     Some options and parameters for the clustering
     * @param corpus The corpus for the clustering
     * @return Clustering results
     * @throws Exception
     */
    public static Clusters optics(OptionsSerial os, Corpus corpus) throws Exception {
        HashMap<String, Object> params = os.get_dataClustering();
        Double epsilon = (Double) params.get("epsilon");
        Integer minPoints = (Integer) params.get("minPoints");

        String[] optionsRemove = new String[2];
        optionsRemove[0] = "-R";
        optionsRemove[1] = "1,2";

        Instances newData = Weka.cleanInstances(corpus.get_data(), optionsRemove);

        Clusters cls = Weka.opticsClusteringToSerialization(corpus, newData, epsilon, minPoints);
        cls.set_method(Clusters.Clustering_method.OPTICS);

        return cls;
    }

    /**
     * Generate csv content with result from the clustering stage
     *
     * @param cls The results of the clustering stage
     * @return A string (CSV) representation of the results
     */
    public static StringBuilder generateCsvContent(Clusters cls) {

        StringBuilder sb = new StringBuilder();
        Map<String, Object> map_options = cls.get_options();

        sb.append("number_of_clusters; min_elem; max_elem; total_nb_of_elem; number_of_clustered_element; number_of_non_clustered_element; param1; param2;\n");

        sb.append(map_options.get("number_of_clusters") + "; ");
        sb.append(map_options.get("min_elem") + "; ");
        sb.append(map_options.get("max_elem") + "; ");
        sb.append(map_options.get("total_nb_of_elem") + "; ");
        sb.append(map_options.get("number_of_clustered_element") + "; ");
        sb.append(map_options.get("number_of_non_clustered_element") + "; ");
        sb.append(map_options.get("params_option") + "; ");

        int[] repartition = cls.get_repartition();
        Integer number_of_clusters = repartition.length;

        sb.append("\n\n\n\n\n\n");
        sb.append("; nb elem per cluster; cluster number;\n");
        for (int i = 0; i < number_of_clusters; i++) {
            sb.append("; " + i + "; " + repartition[i] + ";\n");
        }
        return sb;
    }

    /**
     * ClusteringAction interface allowing to call methods more easily
     */
    interface ClusteringAction {
        Clusters clusterize(OptionsSerial os, Corpus corpus) throws Exception;
    }

    /**
     * Arrays allowing to call method with an index
     */
    private static ClusteringAction[] clusteringAction = new ClusteringAction[]{
            (os, corpus) -> kmeans(os, corpus),
            (os, corpus) -> dbscan(os, corpus),
            (os, corpus) -> optics(os, corpus),
    };

    /**
     * Allows to execute a specific clustering method
     *
     * @param os     Some options and parameters for the clustering
     * @param corpus The corpus for the clustering
     * @param index  The number corresponding to the clustering method to execute
     * @return A string description of the results
     * @throws Exception
     */
    public static Clusters executeClustering(OptionsSerial os, Corpus corpus, int index) throws Exception {
        return clusteringAction[index - 1].clusterize(os, corpus);
    }

    /**
     * Allows to execute the clustering stage
     *
     * @param os Some options and parameters in order to execute this stage
     * @throws Exception
     */
    public static void execute(OptionsSerial os) throws Exception {
        String filename = os.get_filenameProcessing();
        HashMap<String, Object> params = os.get_dataClustering();
        Boolean stop = (Boolean) params.get("stop");

        File f = new File(filename);
        Corpus corpus;

        if (f.exists() && !f.isDirectory()) {
            log.trace("Processing file already exists");
            corpus = (Corpus) Toolbox.deserialization(filename);

            Clusters clustering_results = executeClustering(os, corpus, (Integer) params.get("method"));
            StringBuilder s_builder = generateCsvContent(clustering_results);

            filename = os.get_filenameClustering();

            // --- Serialization
            //
            corpus.set_clusters(clustering_results);
            Toolbox.serialization(corpus, filename);

            if (!stop){
                HashMap<String, Object> evaluation_params = os.get_dataEvaluation();
                if(evaluation_params != null){
                    DataEvaluation.execute(os);
                }
                HashMap<String, Object> extraction_params = os.get_dataExtraction();
                if (extraction_params != null){
                    DataExtraction.execute(os);
                }
            }
        } else { // --- Backtracking
            log.trace("Processing file does not exist");
            DataProcessing.execute(os);
        }
    }
}
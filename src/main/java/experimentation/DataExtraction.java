package experimentation;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.ClusterElement;
import thesis.Index;
import thesis.TFIDFCalculator;
import constants.ConstantsGlobal;
import utils.Corpus;
import utils.Toolbox;

import java.io.File;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Anthony on 08/08/2016.
 * Package : experimentation .
 * Project : PhDTrack.
 */
public class DataExtraction {

    private static Logger log = LoggerFactory.getLogger(DataExtraction.class);

    public static ArrayList<String> sortAndExtractTopX(Map<String,Double> map, Integer x){
        map = Toolbox.sortByValue(map, -1);
        return map.keySet().stream().limit(x).collect(Collectors.toCollection(ArrayList<String>::new));
    }

    private static void toCsv(Map<String,Double> map, String filename, Integer x, Integer index){
        ArrayList<String> topXtokens = sortAndExtractTopX(map, x);

        StringBuilder sb = new StringBuilder();
        sb.append(index + ";;;;;;;;;;\n");

        sb.append(";");
        for (String s : topXtokens){
            sb.append(s + "; ");
        }
        sb.append("\n;");
        for (String s : topXtokens){
            Double tfidf = map.get(s);
            sb.append(tfidf + "; ");
        }
        sb.append("\n;");
        for (String s : topXtokens){
            sb.append("; ");
        }
        sb.append("\n");

        String baseName = FilenameUtils.getBaseName(filename) + "." + FilenameUtils.getExtension(filename);
        String[] link = filename.split("/");
        String path = String.join("/", Arrays.copyOfRange(link, 0, link.length - 1)) + "/";

        Toolbox.write_file(path, baseName, sb.toString().replaceAll("\"", "\\\\\""), StandardOpenOption.APPEND);
        //Toolbox.write_file(Constants_Global.CLUSTERING_RESULTS_DIRECTORY, filename, sb.toString().replaceAll("\"", "\\\\\""), StandardOpenOption.APPEND);
        Toolbox.write_file(ConstantsGlobal.SERIALIZATION_DIRECTORY, "_extraction_test.csv", sb.toString().replaceAll("\"", "\\\\\""), StandardOpenOption.APPEND);
    }

    public static void getTopXTokenByClusterToCSV(ArrayList<String> clusters, String filename, Integer x){
        Index.createIndex(clusters);
        StringBuilder sb = new StringBuilder();
        sb.append("cluster; ");
        for (int i = 0; i < x; i++){
            sb.append(i + 1 + "; ");
        }
        sb.append("\n");
        String baseName = FilenameUtils.getBaseName(filename) + "." + FilenameUtils.getExtension(filename);
        String[] link = filename.split("/");
        String path = String.join("/", Arrays.copyOfRange(link, 0, link.length - 1)) + "/";

        Toolbox.write_file(path, baseName, sb.toString(), StandardOpenOption.CREATE);
        //Toolbox.write_file(Constants_Global.CLUSTERING_RESULTS_DIRECTORY, filename, sb.toString(), StandardOpenOption.CREATE);

        for (int i= 0; i < clusters.size(); i++){
            Map<String,Double> sorted_map = new HashMap<>();
            for (String word : clusters.get(i).split("\\s+")){
                sorted_map.put(word, TFIDFCalculator.tf_idfByWord(clusters, word, i));
            }
            toCsv(sorted_map, filename, x, i + 1);
        }
    }

    public static void getTopXTHashtagByClusterToCSV (ArrayList<String> clusters, String filename, Integer x){
        Index.createIndex(clusters);
        StringBuilder sb = new StringBuilder();
        sb.append("cluster; ");
        for (int i = 0; i < x; i++){
            sb.append(i + 1 + "; ");
        }
        sb.append("\n");
        String baseName = FilenameUtils.getBaseName(filename) + "." + FilenameUtils.getExtension(filename);
        String[] link = filename.split("/");
        String path = String.join("/", Arrays.copyOfRange(link, 0, link.length - 1)) + "/";

        Toolbox.write_file(path, baseName, sb.toString(), StandardOpenOption.CREATE);
        //Toolbox.write_file(Constants_Global.CLUSTERING_RESULTS_DIRECTORY, filename, sb.toString(), StandardOpenOption.CREATE);

        Toolbox.write_file(ConstantsGlobal.SERIALIZATION_DIRECTORY, "_extraction_test.csv", sb.toString(), StandardOpenOption.CREATE);

        for (int i= 0; i < clusters.size(); i++){
            Map<String,Double> sorted_map = new HashMap<>();
            for (String word : clusters.get(i).split("\\s+")){
                if (word.startsWith("#")) {
                    sorted_map.put(word, TFIDFCalculator.tf_idfByWord(clusters, word, i));
                }
            }
            toCsv(sorted_map, filename, x, i + 1);
        }
    }

    public static void getTopXTHashtagByClusterToWordClouds (ArrayList<String> clusters,/* ArrayList<String> filenames, */String filename, Integer x){
        Index.createIndex(clusters);

        String baseName = FilenameUtils.getBaseName(filename) + "." + FilenameUtils.getExtension(filename);
        String[] link = filename.split("/");
        String path = String.join("/", Arrays.copyOfRange(link, 0, link.length - 1)) + "/";

        Toolbox.write_file(path, baseName, "", StandardOpenOption.CREATE);

        for (int i= 0; i < clusters.size(); i++){
            Map<String, Double> sorted_map = new HashMap<>();
            for (String word : clusters.get(i).split("\\s+")){
                if (word.startsWith("#")) {
                    sorted_map.put(word, TFIDFCalculator.tf_idfByWord(clusters, word, i));
                }
            }
            int max = 0;
            ArrayList<String> map = sortAndExtractTopX(sorted_map, x);

            for (String s : map){
                if (max < (int)Math.floor(sorted_map.get(s) * 100000)) {
                    max = (int)Math.floor(sorted_map.get(s) * 100000);
                }
            }
            for (String s : map){
                Toolbox.write_file(path, baseName, ((int)Math.floor((sorted_map.get(s) * 100000 / max * 5)) + 1) + " " + s + " #4158db \n", StandardOpenOption.APPEND);
            }
            Toolbox.write_file(path, baseName, "\n***************** " + /*filenames.get(i) + */ " ****************\n", StandardOpenOption.APPEND);
        }
    }

    public static void getTopXTHashtagByClusterToFile (ArrayList<String> clusters,/* ArrayList<String> filenames, */String filename, Integer x){
        Index.createIndex(clusters);

        String baseName = FilenameUtils.getBaseName(filename) + "." + FilenameUtils.getExtension(filename);
        String[] link = filename.split("/");
        String path = String.join("/", Arrays.copyOfRange(link, 0, link.length - 1)) + "/";

        Toolbox.write_file(path, baseName, "", StandardOpenOption.CREATE);

        for (int i= 0; i < clusters.size(); i++){
            Map<String, Double> sorted_map = new HashMap<>();
            for (String word : clusters.get(i).split("\\s+")){
                if (word.startsWith("#")) {
                    sorted_map.put(word, TFIDFCalculator.tf_idfByWord(clusters, word, i));
                }
            }
            ArrayList<String> map = sortAndExtractTopX(sorted_map, x);

            for (String s : map){
                Toolbox.write_file(path, baseName, sorted_map.get(s) + " " + s + "\n", StandardOpenOption.APPEND);
            }
            Toolbox.write_file(path, baseName, "\n***************** " + /*filenames.get(i) + */ " ****************\n", StandardOpenOption.APPEND);
        }
    }

    public static void getTopXTWordByClusterToFile (ArrayList<String> clusters,/* ArrayList<String> filenames, */String filename, Integer x){
        Index.createIndex(clusters);

        String baseName = FilenameUtils.getBaseName(filename) + "." + FilenameUtils.getExtension(filename);
        String[] link = filename.split("/");
        String path = String.join("/", Arrays.copyOfRange(link, 0, link.length - 1)) + "/";

        Toolbox.write_file(path, baseName, "", StandardOpenOption.CREATE);

        for (int i= 0; i < clusters.size(); i++){
            Map<String, Double> sorted_map = new HashMap<>();
            for (String word : clusters.get(i).split("\\s+")){
                if (!word.startsWith("#")) {
                    sorted_map.put(word, TFIDFCalculator.tf_idfByWord(clusters, word, i));
                }
            }
            ArrayList<String> map = sortAndExtractTopX(sorted_map, x);

            for (String s : map){
                Toolbox.write_file(path, baseName, sorted_map.get(s) + " " + s + "\n", StandardOpenOption.APPEND);
            }
            Toolbox.write_file(path, baseName, "\n***************** " + /*filenames.get(i) + */ " ****************\n", StandardOpenOption.APPEND);
        }
    }

    public static ArrayList<String> getConcatenatedSentencesForEachClustersProcessed(Corpus corpus) {
        ArrayList<String> clusters = new ArrayList<>();
        HashMap<String, ArrayList<ClusterElement>> listMapClusters = corpus.get_clusters().get_mapClusters();
        for (Map.Entry<String, ArrayList<ClusterElement>> entry : listMapClusters.entrySet()) {
            ArrayList<ClusterElement> values = entry.getValue();
            String concatenated = "";
            for (ClusterElement ce : values){
                concatenated = concatenated.concat(" " + ce.get_text());
            }
            clusters.add(concatenated);
        }
        return clusters;
    }

    /**
     * ClusteringAction interface allowing to call methods more easily
     */
    interface ExtractionAction {
        void extract(ArrayList<String> clusters, String filename, Integer x) throws Exception;
    }

    /**
     * Arrays allowing to call method with an index
     */
    private static ExtractionAction[] evaluationAction = new ExtractionAction[]{
            (clusters, filename, x) -> getTopXTokenByClusterToCSV(clusters, filename, x),
            (clusters, filename, x) -> getTopXTHashtagByClusterToCSV(clusters, filename, x),
            (clusters, filename, x) -> getTopXTHashtagByClusterToWordClouds(clusters, filename, x)
    };

    /**
     * Allows to execute a specific clustering method
     *
     * @param clusters The clusters from Weka (all the sentences are concatened)
     * @param filename The filename for save the extraction keywords
     * @param x The max token we want to extract
     * @param index  The number corresponding to the clustering method to execute
     * @return A string description of the results
     * @throws Exception
     */
    public static void execute_extraction(ArrayList<String> clusters, String filename, Integer x, int index) throws Exception {
        evaluationAction[index - 1].extract(clusters, filename, x);
    }

    /**
     * Allows to execute the evalusation stage
     *
     * @param os Some options and parameters in order to execute this stage
     * @throws Exception
     */
    public static void execute(OptionsSerial os) throws Exception {
        String filename = os.get_filenameClustering();
        HashMap<String, Object> params = os.get_dataExtraction();
        Integer numberOfToken = (Integer) params.get("numberOfToken");

        File f = new File(filename);
        Corpus corpus;

        if (f.exists() && !f.isDirectory()) {
            log.trace("Clustering file already exists");
            corpus = (Corpus) Toolbox.deserialization(filename);
            ArrayList<String> clusters = getConcatenatedSentencesForEachClustersProcessed(corpus);

            execute_extraction(clusters, os.get_filenameExtraction(), numberOfToken, (Integer) params.get("method"));
        } else { // --- Backtracking
            log.trace("Clustering file does not exist");
            DataClustering.execute(os);
        }
    }
}

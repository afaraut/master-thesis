package experimentation;

import constants.ConstantsGlobal;
import utils.Toolbox;
import utils.W2vD2vValues;

import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by Anthony on 19/05/2016.
 */
public class AppLoop {

    public static Integer nBElement = 700;
    public static String w2vD2vType = "w2v";

    private static String namefileEvaluation = nBElement + "_eval_km_clustering_" + w2vD2vType + ".csv";
    private static String namefileExtraction = nBElement + "_extract_km_clustering_" + w2vD2vType + ".txt";

    public static String namefileEvaluation1 = nBElement + "_eval_km_clustering_" + w2vD2vType + "_1.csv";
    public static String namefileEvaluation2 = nBElement + "_eval_km_clustering_" + w2vD2vType + "_2.csv";

    public static String str_values;

    public static void main (String[] args) throws Exception {

        HashMap<String, HashMap<String, Object>> parameters = new HashMap<>();

        HashMap<String, Object> dataLoading = new HashMap<>();
        dataLoading.put("method", 1);
        dataLoading.put("dbCollection", "TwitterOrianeRenamed");
        dataLoading.put("dbName", "ImageDataset");
        dataLoading.put("limit", -1);
        dataLoading.put("stop", false);

        parameters.put("Data_loading", dataLoading);

        HashMap<String, Object> dataPreProcessing = new HashMap<>();
        dataPreProcessing.put("method", Arrays.asList(10, 9));
        dataPreProcessing.put("stop", false);

        parameters.put("Data_preprocessing", dataPreProcessing);

        HashMap<String, Object> dataClustering = new HashMap<>();
        dataClustering.put("method", 1);
        dataClustering.put("numberOfCluster", 2);
        dataClustering.put("stop", false);

        parameters.put("Data_clustering", dataClustering);

        HashMap<String, Object> dataEvaluation = new HashMap<>();
        dataEvaluation.put("method", 4);
        dataEvaluation.put("filenameevaluation", namefileEvaluation);
        dataEvaluation.put("stop", false);

        parameters.put("Data_evaluation", dataEvaluation);

        HashMap<String, Object> dataExtraction = new HashMap<>();
        dataExtraction.put("method", 4);
        dataExtraction.put("numberOfToken", 10);
        dataExtraction.put("filenameextraction", namefileExtraction);
        dataExtraction.put("stop", false);

        parameters.put("Data_extraction", dataExtraction);

        int type;
        W2vD2vValues optionProcessing;

        HashMap<String, Object> dataProcessing = new HashMap<>();

        OptionsSerial os;

        ArrayList<Integer> w2v_vectorLength = new ArrayList<>();
        w2v_vectorLength.add(70);
        w2v_vectorLength.add(100); // Defaut value (gensim)
        w2v_vectorLength.add(200);

        ArrayList<Integer> w2v_windowSize = new ArrayList<>();
        w2v_windowSize.add(5); // Defaut value (gensim)
        w2v_windowSize.add(8);
        w2v_windowSize.add(10);

        ArrayList<Integer> w2v_minWordFrequency = new ArrayList<>();
        w2v_minWordFrequency.add(2);
        w2v_minWordFrequency.add(5); // Defaut value (gensim)
        w2v_minWordFrequency.add(8);

        ArrayList<Integer> w2v_netIterations = new ArrayList<>();
        w2v_netIterations.add(5); // Defaut value (gensim)
        w2v_netIterations.add(50);
        w2v_netIterations.add(100);

        ArrayList<Integer> w2v_layerSize = new ArrayList<>();
        w2v_layerSize.add(200);
        w2v_layerSize.add(300); // Defaut value (gensim) ??
        w2v_layerSize.add(400);

        ArrayList<Float> w2v_learningRate = new ArrayList<>();
        w2v_learningRate.add(0.00025f);
        w2v_learningRate.add(0.025f); // Defaut value (gensim) ??
        w2v_learningRate.add(2.5f);

        String header = "nb element; order; TP; TN; FP; FN; precision; recall; f1; a; b; c; d; rand_index; "
                + "confusion[0][0]; confusion[0][1]; confusion[0][2]; confusion[1][0]; confusion[1][1];  confusion[1][2];"
                + "confusion[2][0]; confusion[2][1];  confusion[2][2]; nmi; "
                + "jaccard[0][0]; jaccard[0][1]; jaccard[1][0]; jaccard[1][1]; " +
                "type; learning rate; min learning rate; vector length; batch size; "
                + "min word frequency; net iterations; sub sampling; layer size; window size\n";

        Toolbox.write_file(ConstantsGlobal.CLUSTERING_RESULTS_DIRECTORY + "clustering/", namefileEvaluation, header, StandardOpenOption.CREATE);
        Toolbox.write_file(ConstantsGlobal.CLUSTERING_RESULTS_DIRECTORY + "clustering/", namefileEvaluation1, header, StandardOpenOption.CREATE);
        Toolbox.write_file(ConstantsGlobal.CLUSTERING_RESULTS_DIRECTORY + "clustering/", namefileEvaluation2, header, StandardOpenOption.CREATE);

        for (Integer aW2v_vectorLength : w2v_vectorLength) {
            for (Integer aW2v_windowSize : w2v_windowSize) {
                for (Integer aW2v_minWordFrequency : w2v_minWordFrequency) {
                    for (Integer aW2v_netIteration : w2v_netIterations) {
                        for (Integer aW2v_layerSize : w2v_layerSize) {
                            for (Float aW2v_learningRate : w2v_learningRate) {

                                type = 1;
                                optionProcessing = new W2vD2vValues(type,
                                        aW2v_learningRate, 0.0001, aW2v_vectorLength, 1000, aW2v_minWordFrequency,
                                        aW2v_netIteration, 0.001, aW2v_layerSize, aW2v_windowSize);

                                dataProcessing.clear();
                                dataProcessing.put("method", optionProcessing);
                                dataProcessing.put("stop", false);

                                parameters.put("Data_processing", dataProcessing);

                                str_values = "";
                                if (type == 1) {
                                    str_values = "Word2vec; "+aW2v_learningRate+"; 0.0001; " + aW2v_vectorLength + "; 1000; " + aW2v_minWordFrequency + "; " + aW2v_netIteration +
                                            "; 0.001; " + aW2v_layerSize + "; " + aW2v_windowSize;
                                } else if (type == 2) {
                                    str_values = "Doc2Vec; "+aW2v_learningRate+"; 0.0001; " + aW2v_vectorLength + "; 1000; " + aW2v_minWordFrequency + "; " + aW2v_netIteration +
                                            "; 0.001; " + aW2v_layerSize + "; " + aW2v_windowSize;
                                }

                                Toolbox.write_file(ConstantsGlobal.CLUSTERING_RESULTS_DIRECTORY + "clustering/", namefileExtraction, "\n***************** " + str_values + " ****************\n", StandardOpenOption.APPEND);

                                os = new OptionsSerial(parameters);
                                DataClustering.execute(os);
                            }
                        }
                    }
                }
            }
        }
    }
}

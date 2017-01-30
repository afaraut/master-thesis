package experimentation;

import utils.W2vD2vValues;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by Anthony on 19/05/2016.
 */
public class App {

    public static void main(String[] args) throws Exception {

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

        int type = 2;
        float learningRate = 0.025f;
        double minLearningRate = 0.0001;
        int vectorLength = 70;
        int batchSize = 100;
        int minWordFrequency = 2;
        int netIterations = 5;
        double subSampling = 0.001;
        int layerSize = 100;
        int windowSize = 2;

        W2vD2vValues optionProcessing = new W2vD2vValues(type,
                learningRate, minLearningRate, vectorLength, batchSize, minWordFrequency,
                netIterations, subSampling, layerSize, windowSize);

        HashMap<String, Object> dataProcessing = new HashMap<>();
        dataProcessing.put("method", optionProcessing);
        dataProcessing.put("stop", false);

        parameters.put("Data_processing", dataProcessing);

        HashMap<String, Object> dataClustering = new HashMap<>();
        dataClustering.put("method", 1);
        dataClustering.put("numberOfCluster", 2);
        dataClustering.put("stop", false);

        parameters.put("Data_clustering", dataClustering);

        HashMap<String, Object> dataEvaluation = new HashMap<>();
        dataEvaluation.put("method", 4);
        dataEvaluation.put("filenameevaluation", "_evaluation_test.csv");
        dataEvaluation.put("stop", false);

        parameters.put("Data_evaluation", dataEvaluation);

        HashMap<String, Object> dataExtraction = new HashMap<>();
        dataExtraction.put("method", 2);
        dataExtraction.put("numberOfToken", 10);
        dataEvaluation.put("filenameextraction", "_extraction_test.csv");
        dataExtraction.put("stop", false);

        parameters.put("Data_extraction", dataExtraction);

        OptionsSerial os = new OptionsSerial(parameters);

        DataClustering.execute(os);
    }
}
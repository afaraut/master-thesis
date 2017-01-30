package experimentation;

import org.bson.Document;
import constants.ConstantsGlobal;
import utils.MongoDB;
import utils.Toolbox;

import java.util.Arrays;
import java.util.HashMap;

import utils.W2vD2vValues;

import static java.util.Arrays.asList;

/**
 * Created by Anthony on 19/05/2016.
 */
public class AppWordClouds {

    public static void main(String[] args) throws Exception {

        HashMap<String, HashMap<String, Object>> parameters = new HashMap<>();

        HashMap<String, Object> dataLoading = new HashMap<>();
        dataLoading.put("method", 3);
        dataLoading.put("dbCollection", "TwitterFDL2015");
        dataLoading.put("dbName", "ImageDataset");
        dataLoading.put("timestamp_from", ""); // 1449493211046
        dataLoading.put("timestamp_to", ""); // 1449522011046l
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
        int minWordFrequency = 5;
        int netIterations = 5;
        double subSampling = 0.001;
        int layerSize = 300;
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
/*
        HashMap<String, Object> dataEvaluation = new HashMap<>();
        dataEvaluation.put("method", 4);
        dataEvaluation.put("filenameevaluation", "_evaluation_test.csv");
        dataEvaluation.put("stop", false);

        parameters.put("Data_evaluation", dataEvaluation);*/

        HashMap<String, Object> dataExtraction = new HashMap<>();
        dataExtraction.put("method", 3);
        dataExtraction.put("numberOfToken", 15);
        dataExtraction.put("directoryextraction", ConstantsGlobal.SERIALIZATION_DIRECTORY);
        dataExtraction.put("filenameextraction", "_extraction_test.csv");
        dataExtraction.put("stop", false);

        parameters.put("Data_extraction", dataExtraction);

        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERFDL2015);

        Document min = MongoDB.getCollection().aggregate(asList(
                new Document("$group", new Document("_id", null).append("min_timestamp_ms", new Document("$min", "$timestamp_ms"))))).first();

        Document max = MongoDB.getCollection().aggregate(asList(
                new Document("$group", new Document("_id", null).append("max_timestamp_ms", new Document("$max", "$timestamp_ms"))))).first();

        long min_timestamp = Long.parseLong(min.getString("min_timestamp_ms"));
        long max_timestamp = Long.parseLong(max.getString("max_timestamp_ms"));
        long window = Toolbox.hours2milliseconds(8);
        long lt;
        for (long gt = min_timestamp; gt < max_timestamp; gt+=window) {
            lt = (gt + window);

            dataLoading.put("timestamp_from", "" + gt);
            dataLoading.put("timestamp_to", "" + lt);

            OptionsSerial os = new OptionsSerial(parameters);

            DataClustering.execute(os);
            Toolbox.deleteSerialFiles(ConstantsGlobal.SERIALIZATION_DIRECTORY);
        }

        MongoDB.close();
    }
}
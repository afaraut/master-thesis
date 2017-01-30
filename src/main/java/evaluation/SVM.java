package evaluation;

import constants.ConstantsGlobal;
import constants.ConstantsPreProcessingRegex;
import constants.ConstantsWord2Vec;
import experimentation.DataPreprocessing;
import experimentation.DataProcessing;
import org.bson.Document;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thesis.*;
import thesis.Word2vecDoc2vec;
import utils.*;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LibSVM;
import weka.core.Instance;
import weka.core.Instances;

import java.util.*;

import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;

import weka.core.SelectedTag;


/**
 * Created by Anthony on 09/08/2016.
 * Package : experimentation .
 * Project : PhDTrack.
 */
public class SVM {

    private static Logger log = LoggerFactory.getLogger(SVM.class);

    public enum CLASSTYPE {
        ONECLASS, MULTICLASS
    }

    public static HashMap<String, Object> loadClusters() {

        ArrayList<String> linesFDL = new ArrayList<>();
        ArrayList<String> idsFDL = new ArrayList<>();
        ArrayList<String> lines = new ArrayList<>();
        ArrayList<String> ids = new ArrayList<>();

        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERORIANERENAMED);

        for (Document document : MongoDB.getCollection().find().projection(fields(include("cluster_id", "text", "id_str"), excludeId()))) {
            String sentence = document.getString("text");
            String cluster_id = document.getString("cluster_id");

            sentence = sentence.replaceAll(ConstantsPreProcessingRegex.REGEX_LINE_BREAK, " ");
            sentence = sentence.toLowerCase();
            sentence = DataPreprocessing.execute_pre_processing(sentence, 10);
            sentence = DataPreprocessing.execute_pre_processing(sentence, 9);

            if(cluster_id.equals("13")) { // ID of FDL cluster
                linesFDL.add(sentence.trim());
                idsFDL.add(document.getString("id_str"));
            }
            else {
                lines.add(sentence.trim());
                ids.add(document.getString("id_str"));
            }
        }
        MongoDB.close();

        ArrayList<String> linesEntireCorpus = new ArrayList<>();
        linesEntireCorpus.addAll(linesFDL);
        linesEntireCorpus.addAll(lines);

        ArrayList<String> idsEntireCorpus = new ArrayList<>();
        idsEntireCorpus.addAll(idsFDL);
        idsEntireCorpus.addAll(ids);

        HashMap<String, Object> map = new HashMap();
        map.put("corpus", new Corpus(linesEntireCorpus, idsEntireCorpus));
        map.put("numberOfPositiveElements", linesFDL.size());

        return map; // Thanks (multiple return)
    }

    public static HashMap<String, Object> loadClustersAllCorpus() {

        ArrayList<String> linesFDL = new ArrayList<>();
        ArrayList<String> idsFDL = new ArrayList<>();
        ArrayList<String> lines = new ArrayList<>();
        ArrayList<String> ids = new ArrayList<>();

        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERFDL2015CLUSTERED);

        for (Document document : MongoDB.getCollection().find().projection(fields(include("cluster", "text", "id_str"), excludeId()))) {
            String sentence = document.getString("text");
            Integer cluster_id = document.getInteger("cluster");

            sentence = sentence.replaceAll(ConstantsPreProcessingRegex.REGEX_LINE_BREAK, " ");
            sentence = sentence.toLowerCase();
            sentence = DataPreprocessing.execute_pre_processing(sentence, 10);
            sentence = DataPreprocessing.execute_pre_processing(sentence, 9);

            if(cluster_id == 1) { // FDL
                linesFDL.add(sentence.trim());
                idsFDL.add(document.getString("id_str"));
            }
            else {
                lines.add(sentence.trim());
                ids.add(document.getString("id_str"));
            }
        }
        MongoDB.close();

        ArrayList<String> linesEntireCorpus = new ArrayList<>();
        linesEntireCorpus.addAll(linesFDL);
        linesEntireCorpus.addAll(lines);

        ArrayList<String> idsEntireCorpus = new ArrayList<>();
        idsEntireCorpus.addAll(idsFDL);
        idsEntireCorpus.addAll(ids);

        HashMap<String, Object> map = new HashMap();
        map.put("corpus", new Corpus(linesEntireCorpus, idsEntireCorpus));
        map.put("numberOfPositiveElements", linesFDL.size());

        return map; // Thanks (multiple return)
    }

    public static Instances createInstancesTFIDF(Corpus corpus, Integer numberOfPositiveElements, CLASSTYPE type) throws Exception {

        List<String> lines = corpus.get_texts();
        List<String> ids = corpus.get_ids();

        // -- Create Index
        Index.createIndex(lines);

        List<String> uniqueWords = Index.get_UniqueWords();

        Instances data;
        if (type == CLASSTYPE.ONECLASS){
            data = Weka.createWEKAInstancesOneClass("SVM", uniqueWords.size());
        }
        else {
            data = Weka.createWEKAInstancesWithClass("SVM", uniqueWords.size());
        }

        for (int i = 0; i < lines.size(); i++){

            String tweet = lines.get(i);
            String id = ids.get(i);
            double[] tfidf_vector = TFIDFCalculator.tf_idfVector(lines, tweet, i);

            String textClass;
            if (i < numberOfPositiveElements){ // --- Fête des lumières tweets
                textClass = "positive";
            }
            else { // --- NOT Fête des lumières tweets
                textClass = "negative";
            }

            // -- Create the Weka Instance
            Instance ins;
            if (type == CLASSTYPE.ONECLASS){
                ins = Weka.createWEKAInstanceOneClass(data, tweet, id, textClass, tfidf_vector);
            }
            else {
                ins = Weka.createWEKAInstanceWithClass(data, tweet, id, textClass, tfidf_vector);
            }
            data.add(ins); // Add instance to the instances
        }

        return data;
    }


    public static Instances createInstancesDoc2vec(Corpus corpus, Integer numberOfPositiveElements, W2vD2vValues optionProcessing, CLASSTYPE type) throws Exception {

        String filename = ConstantsGlobal.SERIALIZATION_MODEL_DIRECTORY + "D2V_" + optionProcessing.generateNamefile("_") + ".serial";
        List<Integer> list_options = Arrays.asList(10, 9);
        WordVectors vec = DataProcessing.loadOrComputeModel(filename, ConstantsGlobal.ProcessingType.DOC2VEC, list_options);

        Instances data;
        if (type == CLASSTYPE.ONECLASS){
            data = Weka.createWEKAInstancesOneClass("D2V", ConstantsWord2Vec.VECTOR_LENGTH);
        }
        else {
            data = Weka.createWEKAInstancesWithClass("D2V", ConstantsWord2Vec.VECTOR_LENGTH);
        }

        List<String> lines = corpus.get_texts();
        List<String> ids = corpus.get_ids();

        for (int i = 0; i < lines.size(); i++) {
            String tweet = lines.get(i);
            String id = ids.get(i);
            double[] d2v_vector = vec.getWordVector("DOC_" + id);

            String textClass;
            if (i < numberOfPositiveElements){ // --- Fête des lumières tweets
                textClass = "positive";
            }
            else { // --- NOT Fête des lumières tweets
                textClass = "negative";
            }

            // -- Create the Weka Instance
            Instance ins;
            if (type == CLASSTYPE.ONECLASS){
                ins = Weka.createWEKAInstanceOneClass(data, tweet, id, textClass, d2v_vector);
            }
            else {
                ins = Weka.createWEKAInstanceWithClass(data, tweet, id, textClass, d2v_vector);
            }
            data.add(ins); // Add instance to the instances
        }

        return data;
    }


    public static Instances createInstancesWord2vec(Corpus corpus, Integer numberOfPositiveElements, W2vD2vValues optionProcessing, CLASSTYPE type) throws Exception {

        String filename = ConstantsGlobal.SERIALIZATION_MODEL_DIRECTORY + "W2V_" + optionProcessing.generateNamefile("_") + ".serial";
        List<Integer> list_options = Arrays.asList(10, 9);
        WordVectors vec = DataProcessing.loadOrComputeModel(filename, ConstantsGlobal.ProcessingType.WORD2VEC, list_options);

        Instances data;
        if (type == CLASSTYPE.ONECLASS){
            data = Weka.createWEKAInstancesOneClass("W2V", ConstantsWord2Vec.VECTOR_LENGTH);
        }
        else {
            data = Weka.createWEKAInstancesWithClass("W2V", ConstantsWord2Vec.VECTOR_LENGTH);
        }

        List<String> lines = corpus.get_texts();
        List<String> ids = corpus.get_ids();

        // -- Create Index
        Index.createIndex(lines);

        for (int i = 0; i < lines.size(); i++) {
            String tweet = lines.get(i);
            String id = ids.get(i);
            double[] weightedTweetVector = Word2vecDoc2vec.average_vector_weighted(tweet, lines, vec, i);

            String textClass;
            if (i < numberOfPositiveElements){ // --- Fête des lumières tweets
                textClass = "positive";
            }
            else { // --- NOT Fête des lumières tweets
                textClass = "negative";
            }
            // -- Create the Weka Instance
            Instance ins;
            if (type == CLASSTYPE.ONECLASS){
                ins = Weka.createWEKAInstanceOneClass(data, tweet, id, textClass, weightedTweetVector);
            }
            else {
                ins = Weka.createWEKAInstanceWithClass(data, tweet, id, textClass, weightedTweetVector);
            }
            data.add(ins); // Add instance to the instances
        }

        return data;
    }

    public static void main(String[] args) throws Exception {

        HashMap<String, Object> map = loadClusters();
        Corpus corpus = (Corpus) map.get("corpus");
        Integer numberOfPositiveElements = (Integer) map.get("numberOfPositiveElements");

        W2vD2vValues optionProcessing = new W2vD2vValues(1, 0.1f, 1e-2, 300, 100, 8, 5, 1e-2, 300, 8);

        //Instances data = createInstancesTFIDF(corpus, numberOfPositiveElements, CLASSTYPE.MULTICLASS);
        Instances data = createInstancesWord2vec(corpus, numberOfPositiveElements, optionProcessing, CLASSTYPE.MULTICLASS);
        //Instances data = createInstancesDoc2vec(corpus, numberOfPositiveElements, optionProcessing, CLASSTYPE.MULTICLASS);

        String[] optionsRemove = new String[2];
        optionsRemove[0] = "-R";
        optionsRemove[1] = "1,2";
        Instances newData = Weka.cleanInstances(data, optionsRemove);

        Random ran = new Random(System.currentTimeMillis());
        newData.randomize(ran);

        int max = 20;
        double[] acc = new double[max];
        while(max > 0) {

            // -- copy and randomize instances
            Instances full = new Instances(newData);
            full.randomize(ran);

            // -- using 5 fold CV to emulate the 80-20 random split of jkms
            Instances train = full.trainCV(5, 0);
            Instances test = full.testCV(5, 0);

            LibSVM classifier = new LibSVM();
            classifier.setKernelType(new SelectedTag(LibSVM.KERNELTYPE_LINEAR, LibSVM.TAGS_KERNELTYPE));
            //classifier.setKernelType(new SelectedTag(LibSVM.KERNELTYPE_RBF,LibSVM.TAGS_KERNELTYPE));
            classifier.buildClassifier(train);

            Evaluation eval = new Evaluation(train);
            eval.evaluateModel(classifier, test);

            acc[acc.length - max] = eval.pctCorrect(); // percentage of correctly classified instances (see also pctIncorrect())
            max--;
        }

        double mu = 0;
        for(double d : acc) {
            mu += d;
        }
        mu /= acc.length;

        double std = 0;
        for(double d : acc) {
            std += (d-mu)*(d-mu);
        }
        std = Math.sqrt(std/acc.length);
        log.trace("mean accuracy : " + mu + " +/- " + std);
    }
}

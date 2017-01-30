package evaluation;

import constants.ConstantsGlobal;
import constants.ConstantsWord2Vec;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thesis.Weka;
import utils.*;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LibSVM;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SelectedTag;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardOpenOption;
import java.util.*;


/**
 * Created by Anthony on 09/08/2016.
 * Package : experimentation .
 * Project : PhDTrack.
 */
public class SVMServer {

    private static Logger log = LoggerFactory.getLogger(SVMPredictionServer.class);
    private static String namefile = "700dataset_small.csv";

    public static WordVectors loadModelFromServer(String filename) throws IOException {
        WordVectors vec = null;
        File f = new File(filename);
        if (f.exists() && !f.isDirectory()) {
            log.trace(filename + " already exists");
            vec = WordVectorSerializer.loadTxtVectors(new File(filename));
        }
        else {
            log.warn(filename + " DOESN'T EXIST");
        }
        return vec;
    }

    public static Instances createInstancesDoc2vec(Corpus corpus, Integer numberOfPositiveElements, W2vD2vValues optionProcessing) throws Exception {

        String filename = ConstantsGlobal.SERIALIZATION_MODEL_DIRECTORY + "D2V_" + optionProcessing.generateNamefile("_") + ".serial";
        WordVectors vec = loadModelFromServer(filename);

        Instances data = Weka.createWEKAInstancesWithClass("D2V", ConstantsWord2Vec.VECTOR_LENGTH);

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
            Instance ins = Weka.createWEKAInstanceWithClass(data, tweet, id, textClass, d2v_vector);
            data.add(ins); // Add instance to the instances
        }

        return data;
    }

    public static void svmEvaluation (Instances data) throws Exception {

        //data = Weka.addID(data);

        String[] optionsRemove = new String[2];
        optionsRemove[0] = "-R";                 // "range"
        //optionsRemove[1] = "2,3";                // first attribute
        optionsRemove[1] = "1,2";                // first attribute
        Instances newData = Weka.cleanInstances(data, optionsRemove);

        Random ran = new Random(System.currentTimeMillis());
        newData.randomize(ran);
        int iteration = 5;
        int max = iteration;
        double[] acc = new double[max];
        while(max > 0) {

            Instances full = new Instances(newData);
            full.randomize(ran);

            Instances train = full.trainCV(5, 0);
            Instances test = full.testCV(5, 0);

            String[] optionsClassifier = {"-H", "0"};
            LibSVM classifier = new LibSVM();
            classifier.setOptions(optionsClassifier);
            classifier.setKernelType(new SelectedTag(LibSVM.KERNELTYPE_LINEAR, LibSVM.TAGS_KERNELTYPE));
            //classifier.setKernelType(new SelectedTag(LibSVM.KERNELTYPE_RBF,LibSVM.TAGS_KERNELTYPE));

            //classifier.setOptions(optionsClassifier);
            classifier.buildClassifier(train);
            //classifier.setOptions(optionsClassifier);

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
        String result = iteration + "; " + mu + "; " + std + "; " + "linear; ";
        Toolbox.write_file(ConstantsGlobal.CLUSTERING_RESULTS_DIRECTORY + "svm/", namefile, result, StandardOpenOption.APPEND);
    }


    public static void main(String[] args) throws Exception {
/*
        HashMap<String, Object> map = loadClusters();
        HashMap<String, Object> map_all = loadClustersAllCorpus();

        Toolbox.serialization(map, Constants_Global.SERIALIZATION_SRC_DIRECTORY + "700dataset.serial");
        Toolbox.serialization(map_all, Constants_Global.SERIALIZATION_SRC_DIRECTORY + "31000dataset.serial");
*/
        //HashMap<String, Object> map = (HashMap<String, Object>) Toolbox.deserialization(Constants_Global.SERIALIZATION_SRC_DIRECTORY + "31000dataset.serial");

        HashMap<String, Object> map = (HashMap<String, Object>) Toolbox.deserialization(ConstantsGlobal.SERIALIZATION_SRC_DIRECTORY + "700dataset.serial");

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

        W2vD2vValues optionProcessing;
        Corpus corpus = (Corpus) map.get("corpus");
        Integer numberOfPositiveElements = (Integer) map.get("numberOfPositiveElements");
        Instances data;

        // Reduce the size of the NOT "Fête des lumières"
        //corpus.set_texts(corpus.get_texts().subList(0, 7000));
        //corpus.set_ids(corpus.get_ids().subList(0, 7000));

        log.trace("CORPUS SIZE texts " + corpus.get_texts().size());
        log.trace("CORPUS SIZE ids " + corpus.get_ids().size());

        String header = "iterations; mean accuracy; standard deviation; kernel; type; learning rate; min learning rate; vector length; batch size; "
                + "min word frequency; net iterations; sub sampling; layer size; window size\n";

        Toolbox.write_file(ConstantsGlobal.CLUSTERING_RESULTS_DIRECTORY + "svm/", namefile, header, StandardOpenOption.CREATE);

        for (Integer aW2v_vectorLength : w2v_vectorLength) {
            for (Integer aW2v_windowSize : w2v_windowSize) {
                for (Integer aW2v_minWordFrequency : w2v_minWordFrequency) {
                    for (Integer aW2v_netIteration : w2v_netIterations) {
                        for (Integer aW2v_layerSize : w2v_layerSize) {

                            optionProcessing = new W2vD2vValues(2,
                                    0.025f,
                                    0.0001,
                                    aW2v_vectorLength,
                                    1000,
                                    aW2v_minWordFrequency,
                                    aW2v_netIteration,
                                    0.001,
                                    aW2v_layerSize,
                                    aW2v_windowSize);

                            data = createInstancesDoc2vec(corpus, numberOfPositiveElements, optionProcessing);
                            svmEvaluation(data);

                            String values = "Doc2Vec; 0.025f; 0.0001; " + aW2v_vectorLength + "; 1000; " + aW2v_minWordFrequency + "; " + aW2v_netIteration+
                                    "; 0.001; " + aW2v_layerSize + "; " + aW2v_windowSize;
                            Toolbox.write_file(ConstantsGlobal.CLUSTERING_RESULTS_DIRECTORY + "svm/", namefile, values + "\n", StandardOpenOption.APPEND);


                        }
                    }
                }
            }
        }
        data = SVM.createInstancesTFIDF(corpus, numberOfPositiveElements, SVM.CLASSTYPE.MULTICLASS);
        svmEvaluation(data);
        String values = "TF-IDF; ";
        Toolbox.write_file(ConstantsGlobal.CLUSTERING_RESULTS_DIRECTORY + "svm/", namefile, values + "\n", StandardOpenOption.APPEND);
    }
}

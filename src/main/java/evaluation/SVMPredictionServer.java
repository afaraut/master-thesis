package evaluation;

import constants.ConstantsGlobal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.ClusterElement;
import utils.RealClustering;
import thesis.Weka;
import utils.Corpus;
import utils.Toolbox;
import utils.W2vD2vValues;
import weka.classifiers.functions.LibSVM;
import weka.core.Instances;
import weka.core.SelectedTag;

import java.io.IOException;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static evaluation.SVM.createInstancesWord2vec;
import static evaluation.SVM.createInstancesDoc2vec;
import static experimentation.DataEvaluation.*;
import static experimentation.DataExtraction.getTopXTHashtagByClusterToFile;


/**
 * Created by Anthony on 09/08/2016.
 * Package : experimentation .
 * Project : PhDTrack.
 */
public class SVMPredictionServer {

    private static Logger log = LoggerFactory.getLogger(SVMPredictionServer.class);

    private static String kernel = "rbf";
    private static Integer nBElement = 700;
    private static Integer w2vD2vType = 1;
    private static String type = "w2v";

    private static String namefileEvaluation = ConstantsGlobal.CLUSTERING_RESULTS_DIRECTORY + "svm/" + nBElement + "_eval_" + kernel + "_" + type + ".csv";
    private static String namefileExtraction = ConstantsGlobal.CLUSTERING_RESULTS_DIRECTORY + "svm/" + nBElement + "_extract_" + kernel + "_" + type + ".txt";

    private static int iteration = 1;

    public static void svmPrediction (Instances data, int[] values) throws Exception {
        data = Weka.addID(data);
        String[] optionsRemove = new String[2];
        optionsRemove[0] = "-R";                 // "range"
        optionsRemove[1] = "2,3";                // first attribute

        Instances newData = Weka.cleanInstances(data, optionsRemove);

        //Random ran = new Random(System.currentTimeMillis());
        Random ran = new Random(42);
        newData.randomize(ran);

        // using 5 fold CV to emulate the 80-20 random split of jkms
        Instances train = newData.trainCV(5, 0);
        Instances test = newData.testCV(5, 0);

        int max = iteration;
        while(max > 0) {
            String[] optionsClassifier = {"-H", "0"};
            LibSVM classifier = new LibSVM();
            classifier.setOptions(optionsClassifier);
            if (kernel.equals("linear")) {
                classifier.setKernelType(new SelectedTag(LibSVM.KERNELTYPE_LINEAR, LibSVM.TAGS_KERNELTYPE));
            }
            else if (kernel.equals("rbf")) {
                classifier.setKernelType(new SelectedTag(LibSVM.KERNELTYPE_RBF,LibSVM.TAGS_KERNELTYPE));
            }

            classifier.buildClassifier(train);

            for (int i = 0; i < test.numInstances(); i++) {
                int id = (int) test.instance(i).value(0); // Get back the ID
                if (classifier.classifyInstance(test.instance(i)) != 1.0) {
                    values[id - 1] += 1; // id -1 because the id values begin at 1
                } else {
                    values[id - 1] -= 1;
                }
            }
            max--;
        }
        svmEvaluation(data, values);
    }

    public static void svmEvaluation(Instances data, int[] values) throws IOException {
        ArrayList<ClusterElement> predicted = new ArrayList<>();
        ArrayList<String> ids = new ArrayList<>();

        ArrayList<String> clustersForExtraction = new ArrayList<>();
        String fdl = "";
        String noFdl = "";

        for (int i = 0; i < data.numInstances(); i++) {
            String text = data.instance(i).stringValue(1);
            String id = data.instance(i).stringValue(2);
            if (values[i] > 0){
                predicted.add(new ClusterElement(id, text, 1));
                ids.add(id);
                fdl = fdl.concat(" " + text);

            } else if (values[i] < 0) {
                predicted.add(new ClusterElement(id, text, 0));
                ids.add(id);
                noFdl = noFdl.concat(" " + text);
            }
        }

        ArrayList<ClusterElement> reality = RealClustering.getDatasetBinaryClusteringInListFromIdsServer(ids, ConstantsGlobal.SERIALIZATION_SRC_DIRECTORY + "corpus_clustered.serial");
        //ArrayList<ClusterElement> handmadeClusters = RealClustering.getDatasetBinaryClusteringInListFromIds(ids);

        StringBuilder sb = new StringBuilder();
        //sb.append("a; b; c; d; precision; recall; f1; rand_index; nmi; jaccard[0][0]; jaccard[0][1]; jaccard[1][0]; jaccard[1][1]; \n");

        //sb.append(iteration + "; ");
        sb.append(nBElement + "; ");
        sb.append(clusteringToPrecisionRecallF1(reality, predicted));
        sb.append(clusteringToRandIndex(reality, predicted));
        sb.append(clusteringToNmi(reality, predicted));
        sb.append(clusteringToJaccard(reality, predicted));
        sb.append(kernel + "; ");
        //sb.append("\n");


        Toolbox.write_file("./", namefileEvaluation, sb.toString(), StandardOpenOption.APPEND);

        clustersForExtraction.add(fdl);
        clustersForExtraction.add(noFdl);

        getTopXTHashtagByClusterToFile(clustersForExtraction, namefileExtraction, 15);
        //getTopXTWordByClusterToFile(clustersForExtraction, "./svmExtractionTest.txt", 100);
    }

    public static Corpus reduceCorpusSizeTo(Corpus corpus, int size) {

        ArrayList<String> newTexts = new ArrayList<String>(corpus.get_texts().subList(0, size));
        ArrayList<String> newIDs = new ArrayList<String>(corpus.get_ids().subList(0, size));

        return new Corpus(newTexts, newIDs);
    }

    public static void main(String[] args) throws Exception {

        HashMap<String, Object> map = null;
        if (nBElement == 700) {
            map = (HashMap<String, Object>) Toolbox.deserialization(ConstantsGlobal.SERIALIZATION_SRC_DIRECTORY +"700dataset.serial");
        }
        else if (nBElement == 31000 || nBElement == 7000) {
            map = (HashMap<String, Object>) Toolbox.deserialization(ConstantsGlobal.SERIALIZATION_SRC_DIRECTORY +"31000dataset.serial");
        }

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

        W2vD2vValues optionProcessing;
        Corpus corpus = (Corpus) map.get("corpus");


        if (nBElement == 7000){
            corpus = reduceCorpusSizeTo(corpus, nBElement);
        }

        log.trace("TYPE " + type);
        log.trace("Kernel " + kernel);
        log.trace("Will work on " + corpus.get_texts().size() + " texts");
        log.trace("Will work on " + corpus.get_ids().size() + " ids");

        Integer numberOfPositiveElements = (Integer) map.get("numberOfPositiveElements");

        int[] values = new int[corpus.get_texts().size()];
        log.trace("" + numberOfPositiveElements);

        Instances data;

        String header = "Nb element; TP; TN; FP; FN; precision; recall; f1; a; b; c; d; rand_index; "
                + "confusion[0][0]; confusion[0][1]; confusion[0][2]; confusion[1][0]; confusion[1][1];  confusion[1][2];"
                + "confusion[2][0]; confusion[2][1];  confusion[2][2]; nmi; "
                + "jaccard[0][0]; jaccard[0][1]; jaccard[1][0]; jaccard[1][1]; " +
                "kernel; type; learning rate; min learning rate; vector length; batch size; "
                + "min word frequency; net iterations; sub sampling; layer size; window size\n";

        Toolbox.write_file("./", namefileEvaluation, header, StandardOpenOption.CREATE);

        for (Integer aW2v_vectorLength : w2v_vectorLength) {
            for (Integer aW2v_windowSize : w2v_windowSize) {
                for (Integer aW2v_minWordFrequency : w2v_minWordFrequency) {
                    for (Integer aW2v_netIteration : w2v_netIterations) {
                        for (Integer aW2v_layerSize : w2v_layerSize) {
                            for (Float aW2v_learningRate : w2v_learningRate) {

                                optionProcessing = new W2vD2vValues(w2vD2vType,
                                        aW2v_learningRate,
                                        0.0001,
                                        aW2v_vectorLength,
                                        1000,
                                        aW2v_minWordFrequency,
                                        aW2v_netIteration,
                                        0.001,
                                        aW2v_layerSize,
                                        aW2v_windowSize);

                                if (w2vD2vType == 1){ // Word2vec
                                    data = createInstancesWord2vec(corpus, numberOfPositiveElements, optionProcessing, SVM.CLASSTYPE.MULTICLASS);
                                }
                                else { // Doc2vec
                                    data = createInstancesDoc2vec(corpus, numberOfPositiveElements, optionProcessing, SVM.CLASSTYPE.MULTICLASS);
                                }

                                svmPrediction (data, values);

                                String str_values = "";
                                if (w2vD2vType == 1) {
                                    str_values = "Word2vec; "+aW2v_learningRate+"; 0.0001; " + aW2v_vectorLength + "; 1000; " + aW2v_minWordFrequency + "; " + aW2v_netIteration+
                                            "; 0.001; " + aW2v_layerSize + "; " + aW2v_windowSize;
                                }
                                else if (w2vD2vType == 2){
                                    str_values = "Doc2Vec; "+aW2v_learningRate+"; 0.0001; " + aW2v_vectorLength + "; 1000; " + aW2v_minWordFrequency + "; " + aW2v_netIteration+
                                            "; 0.001; " + aW2v_layerSize + "; " + aW2v_windowSize;
                                }

                                Toolbox.write_file("./", namefileEvaluation, str_values + "\n", StandardOpenOption.APPEND);
                                Toolbox.write_file("./", namefileExtraction, "\n***************** " + str_values + " ****************\n", StandardOpenOption.APPEND);
                            }
                        }
                    }
                }
            }
        }
        /*data = createInstancesTFIDF(corpus, numberOfPositiveElements);
        svmPrediction(data, values);
        String str_values = "TF-IDF; ";
        Toolbox.write_file("./", namefileEvaluation, str_values + "\n", StandardOpenOption.APPEND);*/
    }
}

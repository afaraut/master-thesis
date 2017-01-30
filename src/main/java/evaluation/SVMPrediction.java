package evaluation;


import utils.ClusterElement;
import utils.RealClustering;
import thesis.Weka;
import utils.Corpus;

import utils.W2vD2vValues;
import weka.classifiers.functions.LibSVM;
import weka.core.Instances;
import weka.core.SelectedTag;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.Random;

import static evaluation.SVM.createInstancesDoc2vec;
import static experimentation.DataEvaluation.clusteringToPrecisionRecallF1;
import static experimentation.DataEvaluation.clusteringToJaccard;
import static experimentation.DataEvaluation.clusteringToNmi;
import static experimentation.DataEvaluation.clusteringToRandIndex;


/**
 * Created by Anthony on 09/08/2016.
 * Package : experimentation .
 * Project : PhDTrack.
 */
public class SVMPrediction {

    public static void svmPrediction (Instances data, int[] values) throws Exception {
        data = Weka.addID(data);
        String[] optionsRemove = new String[2];
        optionsRemove[0] = "-R";
        optionsRemove[1] = "2,3";

        Instances newData = Weka.cleanInstances(data, optionsRemove);

        Random ran = new Random(System.currentTimeMillis());
        newData.randomize(ran);

        Instances train = newData.trainCV(5, 0);
        Instances test = newData.testCV(5, 0);

        int max = 2;
        while(max > 0) {
            //String[] optionsClassifier = {"-H", "0"};
            LibSVM classifier = new LibSVM();
            //classifier.setOptions(optionsClassifier);
            classifier.setKernelType(new SelectedTag(LibSVM.KERNELTYPE_LINEAR, LibSVM.TAGS_KERNELTYPE));
            //classifier.setKernelType(new SelectedTag(LibSVM.KERNELTYPE_RBF,LibSVM.TAGS_KERNELTYPE));

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

    public static void svmEvaluation(Instances data, int[] values){
        ArrayList<ClusterElement> predicted = new ArrayList<>();
        ArrayList<String> ids = new ArrayList<>();

        for (int i = 0; i < data.numInstances(); i++) {
            String text = data.instance(i).stringValue(1);
            String id = data.instance(i).stringValue(2);
            if (values[i] > 0){
                predicted.add(new ClusterElement(id, text, 1));
                ids.add(id);
            } else if (values[i] < 0) {
                predicted.add(new ClusterElement(id, text, 0));
                ids.add(id);
            }
        }

        ArrayList<ClusterElement> handmadeClusters = RealClustering.getDatasetBinaryClusteringInListFromIds(ids);

        StringBuilder sb = new StringBuilder();
        sb.append("a; b; c; d; precision; recall; f1; rand_index; nmi; jaccard[0][0]; jaccard[0][1]; jaccard[1][0]; jaccard[1][1]\n");

        sb.append(clusteringToPrecisionRecallF1(handmadeClusters, predicted));
        sb.append(clusteringToRandIndex(handmadeClusters, predicted));
        sb.append(clusteringToNmi(handmadeClusters, predicted));
        sb.append(clusteringToJaccard(handmadeClusters, predicted));

        sb.append("\n");
        System.out.println(sb);
    }

    public static void main(String[] args) throws Exception {

        //HashMap<String, Object> map = SVM.loadClustersAllCorpus();
        HashMap<String, Object> map = SVM.loadClusters();
        Corpus corpus = (Corpus) map.get("corpus");

        int[] values = new int[corpus.get_texts().size()];

        Integer numberOfPositiveElements = (Integer) map.get("numberOfPositiveElements");
        System.out.println(numberOfPositiveElements);
        //Instances data = createInstancesTFIDF(corpus, numberOfPositiveElements, SVM.CLASSTYPE.MULTICLASS);

        W2vD2vValues optionProcessing = new W2vD2vValues(2, 0.1f, 1e-2, 300, 100, 8, 5, 1e-2, 300, 8);

        //Instances data = createInstancesWord2vec(corpus, numberOfPositiveElements, optionProcessing, SVM.CLASSTYPE.MULTICLASS);
        Instances data = createInstancesDoc2vec(corpus, numberOfPositiveElements, optionProcessing, SVM.CLASSTYPE.MULTICLASS);

        svmPrediction (data,values);
    }
}

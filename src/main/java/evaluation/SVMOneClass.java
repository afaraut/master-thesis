package evaluation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thesis.Weka;
import utils.*;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LibSVM;
import weka.core.Instances;
import weka.core.SelectedTag;

import java.util.*;


/**
 * Created by Anthony on 09/08/2016.
 * Package : experimentation .
 * Project : PhDTrack.
 */
public class SVMOneClass {

    private static Logger log = LoggerFactory.getLogger(SVMOneClass.class);

    public static void main(String[] args) throws Exception {

        HashMap<String, Object> map = SVM.loadClusters();
        Corpus corpus = (Corpus) map.get("corpus");
        Integer numberOfPositiveElements = (Integer) map.get("numberOfPositiveElements");

        //Instances data = SVM.createInstancesTFIDF(corpus, numberOfPositiveElements, SVM.CLASSTYPE.ONECLASS);

        W2vD2vValues optionProcessing = new W2vD2vValues(1, 0.1f, 1e-2, 300, 100, 8, 5, 1e-2, 300, 8);
        Instances data = SVM.createInstancesWord2vec(corpus, numberOfPositiveElements, optionProcessing, SVM.CLASSTYPE.ONECLASS);
        //Instances data = SVM.createInstancesDoc2vec(corpus, numberOfPositiveElements, optionProcessing, SVM.CLASSTYPE.ONECLASS);

        String[] optionsRemove = new String[2];
        optionsRemove[0] = "-R";                 // "range"
        optionsRemove[1] = "1,2";                // first attribute

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
            classifier.setSVMType(new SelectedTag(LibSVM.SVMTYPE_ONE_CLASS_SVM, LibSVM.TAGS_SVMTYPE));
            classifier.buildClassifier(train);

            Evaluation eval = new Evaluation(train);
            eval.evaluateModel(classifier, test);
            System.out.println("___________________________________");
            System.out.println(eval.toClassDetailsString());
            System.out.println("***********************************");
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
        log.info("mean accuracy : " + mu + " +/- " + std);
    }
}

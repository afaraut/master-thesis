package experimentation;

import constants.ConstantsGlobal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.ClusterElement;
import utils.RealClustering;
import utils.Corpus;
import utils.Toolbox;

import java.io.File;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.lang.Math.abs;

/**
 * Created by Anthony on 27/07/2016.
 * Package : experimentation .
 * Project : PhDTrack.
 */
public class DataEvaluation {

    private static Logger log = LoggerFactory.getLogger(DataLoading.class);
    private static String globalFilename;

    public static StringBuilder clusteringToAll(ArrayList<ClusterElement> reality, ArrayList<ClusterElement> predicted) {

        StringBuilder sb = new StringBuilder();
        sb.append("a; b; c; d; precision; recall; f1; rand_index; nmi; jaccard[0][0]; jaccard[0][1]; jaccard[1][0]; jaccard[1][1];\n");

        sb.append(clusteringToRandIndex(reality, predicted));
        sb.append(clusteringToNmi(reality, predicted));
        sb.append(clusteringToJaccard(reality, predicted));
        sb.append("\n");
        return sb;
    }

    public static StringBuilder clusteringToAllServer(ArrayList<ClusterElement> reality, ArrayList<ClusterElement> predicted) {

        StringBuilder sb = new StringBuilder();

        sb.append(AppLoop.nBElement + "; 1; " );
        sb.append(clusteringToPrecisionRecallF1(reality, predicted));
        sb.append(clusteringToRandIndex(reality, predicted));
        sb.append(clusteringToNmi(reality, predicted));
        sb.append(clusteringToJaccard(reality, predicted));

        Toolbox.write_file(ConstantsGlobal.CLUSTERING_RESULTS_DIRECTORY + "clustering/", globalFilename, sb.toString(), StandardOpenOption.APPEND);
        Toolbox.write_file(ConstantsGlobal.CLUSTERING_RESULTS_DIRECTORY + "clustering/", globalFilename, "TF-IDF; \n", StandardOpenOption.APPEND);

        Toolbox.write_file(ConstantsGlobal.CLUSTERING_RESULTS_DIRECTORY + "clustering/", AppLoop.namefileEvaluation1, sb.toString(), StandardOpenOption.APPEND);
        Toolbox.write_file(ConstantsGlobal.CLUSTERING_RESULTS_DIRECTORY + "clustering/", AppLoop.namefileEvaluation1, "TF-IDF; \n", StandardOpenOption.APPEND);


        for (ClusterElement ce : predicted){
            ce.set_numCluster(ce.get_numCluster() ^ 1); // 0 -> 1 and 1 -> 0
        }

        sb = new StringBuilder();
        sb.append(AppLoop.nBElement + "; 1; " );
        sb.append(clusteringToPrecisionRecallF1(reality, predicted));
        sb.append(clusteringToRandIndex(reality, predicted));
        sb.append(clusteringToNmi(reality, predicted));
        sb.append(clusteringToJaccard(reality, predicted));

        Toolbox.write_file(ConstantsGlobal.CLUSTERING_RESULTS_DIRECTORY + "clustering/", AppLoop.namefileEvaluation2, sb.toString(), StandardOpenOption.APPEND);
        Toolbox.write_file(ConstantsGlobal.CLUSTERING_RESULTS_DIRECTORY + "clustering/", AppLoop.namefileEvaluation2, "TF-IDF; \n", StandardOpenOption.APPEND);

        return sb;
    }

    public static StringBuilder clusteringToPrecisionRecallF1(ArrayList<ClusterElement> reality, ArrayList<ClusterElement> predicted) {

        int TP = 0;
        int TN = 0;
        int FP = 0;
        int FN = 0;

        for (int i = 0; i < predicted.size(); i++) {
            ClusterElement predictedElement = predicted.get(i);
            for (int j = 0; j < reality.size(); j++) {
                ClusterElement groundTruthElement = reality.get(j);
                if (predictedElement.get_idTwitter().equals(groundTruthElement.get_idTwitter())){
                    if (predictedElement.get_numCluster() == groundTruthElement.get_numCluster()) {
                        if (predictedElement.get_numCluster() == 1){
                            TP++;
                        }
                        else if (predictedElement.get_numCluster() == 0) {
                            TN++;
                        }
                    }
                    else if (predictedElement.get_numCluster() != groundTruthElement.get_numCluster()) {
                        if (predictedElement.get_numCluster() == 1){
                            FP++;
                        }
                        else if (predictedElement.get_numCluster() == 0) {
                            FN++;
                        }
                    }
                }
            }
        }

        double precision = (double)TP / (TP + FP);
        double recall = (double)TP / (TP + FN);
        double f1;
        if (precision + recall == 0){
            f1 = 0;
        }
        else {
            f1 = (2 * precision * recall) / (precision + recall);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(TP + "; ");
        sb.append(TN + "; ");
        sb.append(FP + "; ");
        sb.append(FN + "; ");
        sb.append(precision + "; ");
        sb.append(recall + "; ");
        sb.append(f1 + "; ");
        return sb;
    }

    public static StringBuilder clusteringToRandIndex(ArrayList<ClusterElement> reality, ArrayList<ClusterElement> predicted) {

        ArrayList<ClusterElement> allElements = new ArrayList<>();

        for (ClusterElement ceGroudTruth : reality) {
            for (ClusterElement cePredicted : predicted){
                if (ceGroudTruth.get_idTwitter().equals(cePredicted.get_idTwitter())){

                    String id = ceGroudTruth.get_idTwitter();
                    String text = ceGroudTruth.get_text();
                    Integer clusterPredicted = cePredicted.get_numCluster();
                    Integer clusterGroudTruth = ceGroudTruth.get_numCluster();

                    allElements.add(new ClusterElement(id, text, clusterPredicted, clusterGroudTruth));
                }
            }
        }

        int a = 0;
        int b = 0;
        int c = 0;
        int d = 0;

        for (int i = 0; i < allElements.size(); i++) {
            for (int j = i + 1; j < allElements.size(); j++){

                ClusterElement ce1 = allElements.get(i); // Tweet 1
                ClusterElement ce2 = allElements.get(j); // Tweet 2

                if (ce1.get_numCluster() == ce1.get_groundTruthNumCluster() && ce2.get_numCluster() == ce2.get_groundTruthNumCluster()){
                    a++;
                }
                else if (ce1.get_numCluster() != ce1.get_groundTruthNumCluster() && ce2.get_numCluster() != ce2.get_groundTruthNumCluster()){
                    b++;
                }
                else if (ce1.get_numCluster() == ce1.get_groundTruthNumCluster() && ce2.get_numCluster() != ce2.get_groundTruthNumCluster()){
                    c++;
                }
                else if (ce1.get_numCluster() != ce1.get_groundTruthNumCluster() && ce2.get_numCluster() == ce2.get_groundTruthNumCluster()){
                    d++;
                }
            }
        }
        double rand_ind = (double)(a + b) / (a + b + c + d);

        StringBuilder sb = new StringBuilder();
        sb.append(a + "; ");
        sb.append(b + "; ");
        sb.append(c + "; ");
        sb.append(d + "; ");
        sb.append(rand_ind + "; ");
        return sb;
    }

    public static ArrayList<ArrayList<ClusterElement>> convertClusterList2ListListClusters (ArrayList<ClusterElement> list) {
        ArrayList<ArrayList<ClusterElement>> listOfList = new ArrayList<>();
        ArrayList<ClusterElement> fdl = new ArrayList<>();
        ArrayList<ClusterElement> noFdl = new ArrayList<>();
        for (ClusterElement ce : list) {
            if (ce.get_numCluster() == 0){ // NOT FDL
                noFdl.add(ce);
            }
            else if (ce.get_numCluster() == 1){ // FDL
                fdl.add(ce);
            }
        }
        listOfList.add(fdl);
        listOfList.add(noFdl);
        return listOfList;
    }

    public static StringBuilder clusteringToJaccard(ArrayList<ClusterElement> param_reality, ArrayList<ClusterElement>  param_predicted) {

        ArrayList<ArrayList<ClusterElement>> reality = convertClusterList2ListListClusters(param_reality);
        ArrayList<ArrayList<ClusterElement>> predicted = convertClusterList2ListListClusters(param_predicted);

        Double matrix[][] = new Double[predicted.size()][reality.size()];

        for (int i = 0; i < reality.size(); i++) {
            for (int j = 0; j < predicted.size(); j++) {
                List<ClusterElement> intersection = Toolbox.intersect(reality.get(i), predicted.get(j));
                List<ClusterElement> union = Toolbox.union(reality.get(i), predicted.get(j));
                matrix[i][j] = (double)Math.abs(intersection.size()) / Math.abs(union.size()); // Jaccard similarity intersection / union
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < reality.size(); i++) {
            for (int j = 0; j < predicted.size(); j++) {
                sb.append(matrix[i][j] + "; ");
            }
        }
        return sb;
    }

    public static StringBuilder clusteringToNmi(ArrayList<ClusterElement> reality, ArrayList<ClusterElement> predicted) {

        int[][] confusionMatrix = new int[3][3];

        // -- GENERATION OF THE CONFUSION MATRIX
        for (int i = 0; i < predicted.size(); i++) {
            ClusterElement ce1 = predicted.get(i);
            for (int j = 0; j < reality.size(); j++){
                ClusterElement ce2 = reality.get(j);
                if (ce1.get_idTwitter().equals(ce2.get_idTwitter())){
                    confusionMatrix[ce1.get_numCluster()][ce2.get_numCluster()]++;
                }
            }
        }

        // -- GENERATION OF THE MARGINAL DISTRIBUTION
        for (int i = 0; i < confusionMatrix.length - 1; i++) {
            for (int j = 0; j <  confusionMatrix[0].length - 1; j++) {
                confusionMatrix[i][confusionMatrix[0].length - 1] += confusionMatrix[i][j];
                confusionMatrix[confusionMatrix.length - 1][j] += confusionMatrix[i][j];
                confusionMatrix[confusionMatrix.length - 1][confusionMatrix[0].length - 1]+=confusionMatrix[i][j];
            }
        }

        // -- DISPLAY THE CONFUSION MATRIX
        for (int i = 0; i < confusionMatrix.length; i++) {
            for (int j = 0; j <  confusionMatrix[0].length; j++) {
                System.out.print(confusionMatrix[i][j] + " ");
            }
            System.out.println("\n");
        }

        // -- NMI COMPUTATION
        double i_mutualInfo = 0;
        double h_x = 0;
        double h_y = 0;
        int len = confusionMatrix[confusionMatrix.length - 1][confusionMatrix[0].length - 1];
        for (int i = 0; i < confusionMatrix.length - 1; i++) {
            int pX = confusionMatrix[i][confusionMatrix[0].length - 1];

            for (int j = 0; j <  confusionMatrix[0].length - 1; j++) {

                int pY = confusionMatrix[confusionMatrix.length - 1][j];
                int inter_pXY = confusionMatrix[i][j];

                if (inter_pXY != 0){
                    double tmp1 = (double)abs(inter_pXY) / len;
                    double tmp2 = Toolbox.log(((double)len * abs(inter_pXY) / (abs(pX) * abs(pY))), 2);
                    // CALCULATING I
                    i_mutualInfo+= tmp1 * tmp2 ;
                }
            }

            // -- CALCULATING THE ENTROPIES
            int pY = confusionMatrix[confusionMatrix.length - 1][i]; // Puisque la matrice est carré je peux faire ça
            if (pX != 0){
                h_x += (double)abs(pX) / len * Toolbox.log((double)abs(pX) / len, 2);
            }
            if (pY != 0){
                h_y += (double)abs(pY) / len * Toolbox.log((double)abs(pY) / len, 2);
            }
        }
        h_x = -h_x;
        h_y = -h_y;

        double nmi = (double)i_mutualInfo / (((double)h_x + h_y) / 2);
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < confusionMatrix.length; i++) {
            for (int j = 0; j <  confusionMatrix[0].length; j++) {
                sb.append(confusionMatrix[i][j] + "; ");
            }
        }

        sb.append(nmi + "; ");
        return sb;
    }

    /**
     * ClusteringAction interface allowing to call methods more easily
     */
    interface EvaluationAction {
        StringBuilder evaluate(ArrayList<ClusterElement> reality, ArrayList<ClusterElement> predicted) throws Exception;
    }

    /**
     * Arrays allowing to call method with an index
     */
    private static EvaluationAction[] evaluationAction = new EvaluationAction[]{
            (reality, predicted) -> clusteringToJaccard(reality, predicted),
            (reality, predicted) -> clusteringToRandIndex(reality, predicted),
            (reality, predicted) -> clusteringToNmi(reality, predicted),
            (reality, predicted) -> clusteringToAllServer(reality, predicted),
    };

    /**
     * Allows to execute a specific clustering method
     *
     * @param reality The reality dataset
     * @param predicted The predicted dataset
     * @param index  The number corresponding to the clustering method to execute
     * @return A string description of the results
     * @throws Exception
     */
    public static StringBuilder execute_evaluation(ArrayList<ClusterElement> reality, ArrayList<ClusterElement> predicted, int index) throws Exception {
        return evaluationAction[index - 1].evaluate(reality, predicted);
    }

    /**
     * Allows to execute the evalusation stage
     *
     * @param os Some options and parameters in order to execute this stage
     * @throws Exception
     */
    public static void execute(OptionsSerial os) throws Exception {
        String filename = os.get_filenameClustering();
        HashMap<String, Object> params = os.get_dataEvaluation();

        File f = new File(filename);
        Corpus corpus;

        if (f.exists() && !f.isDirectory()) {
            log.trace("Clustering file already exists");
            corpus = (Corpus) Toolbox.deserialization(filename);

            globalFilename = (String) params.get("filenameevaluation");

            ArrayList<ClusterElement> reality = RealClustering.
                    getDatasetBinaryClusteringInListFromIdsServer((ArrayList<String>) corpus.get_ids(),
                            ConstantsGlobal.SERIALIZATION_SRC_DIRECTORY + "corpus_clustered.serial");

            ArrayList<ClusterElement> predicted = (ArrayList<ClusterElement>) corpus.get_clusters().get_listClusterElement();

            StringBuilder sb = execute_evaluation(reality, predicted, (Integer) params.get("method"));
            //Toolbox.write_file(path, baseName, sb.toString(), StandardOpenOption.CREATE);

            Toolbox.write_file(ConstantsGlobal.CLUSTERING_RESULTS_DIRECTORY + "clustering/", globalFilename, sb.toString(), StandardOpenOption.APPEND);
            Toolbox.write_file(ConstantsGlobal.CLUSTERING_RESULTS_DIRECTORY + "clustering/", globalFilename, "TF-IDF; \n", StandardOpenOption.APPEND);
            //Toolbox.write_file("./result_clustering/", globalFilename, AppHashMapLoop.str_values + "\n", StandardOpenOption.APPEND);

        } else { // --- Backtracking
            log.trace("Clustering file does not exist");
            DataClustering.execute(os);
        }
    }
}

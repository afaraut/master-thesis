package experimentation;

import constants.ConstantsGlobal;
import constants.ConstantsWord2Vec;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thesis.Word2vecDoc2vec;
import thesis.Index;
import thesis.TFIDFCalculator;
import thesis.Weka;
import utils.*;
import weka.core.Instance;
import weka.core.Instances;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Anthony on 18/05/2016.
 */
public class DataProcessing {

    private static Logger log = LoggerFactory.getLogger(DataProcessing.class);

    public static List<String> cleanBigCorpus(List<String> lines, List<Integer> list_options){
        for (int i = 0; i < lines.size(); i++) {
            for (Integer value : list_options) {
                String s = lines.get(i);
                String sentence = DataPreprocessing.execute_pre_processing(s, value);
                lines.set(i, sentence.trim());
            }
        }
        return lines;
    }

    public static WordVectors loadOrComputeModel(String filename, ConstantsGlobal.ProcessingType type, List<Integer> list_options) throws IOException {
        WordVectors vec;
        File f = new File(filename);
        if (f.exists() && !f.isDirectory()) {
            Instant starts = Instant.now();
            vec = WordVectorSerializer.loadTxtVectors(new File(filename));
            Instant ends = Instant.now();
            log.trace("Time for load the model " + Duration.between(starts, ends));
        }
        else {
            //Corpus bigCorpus = Data_loading.load_data(Constants_Global.DBNAME, Constants_Global.DBCOLLECTIONTWITTERFDL2015, -1);
            Corpus bigCorpus = (Corpus) Toolbox.deserialization("bigcorpus.serial");
            List<String> lines = bigCorpus.get_texts();

            // -- Clean the big corpus with the same parameters
            lines = cleanBigCorpus(lines, list_options);
            bigCorpus.set_texts(lines);
            Word2Vec w2v;
            if (type == ConstantsGlobal.ProcessingType.WORD2VEC){
                // -- Execute word2vec
                Instant starts = Instant.now();
                w2v = Word2vecDoc2vec.word2Vec(lines);
                Instant ends = Instant.now();
                log.trace("Time for compute the model (word2Vec) " + Duration.between(starts, ends));
            } else {
                // -- Execute doc2vec
                Instant starts = Instant.now();
                w2v = Word2vecDoc2vec.doc2Vec(bigCorpus);
                Instant ends = Instant.now();
                log.trace("Time for compute the model (doc2Vec) " + Duration.between(starts, ends));
                cleanDoc2VecModel(w2v);
            }
            vec = w2v;
            // -- Save word2vec model
            WordVectorSerializer.writeWordVectors(w2v, filename);
        }
        return vec;
    }

    /**
     * Word2vec processing on the data
     * Generate WordVvec vectors
     *
     * @param os     Some options and parameters for the processing
     * @param corpus The corpus for the processing
     * @return Instances object for Weka clustering
     */
    public static Instances word2vec(OptionsSerial os, Corpus corpus) throws IOException {
        log.trace("----------- word2vec");

        String filename = os.get_filenameWord2Vec();
        List<Integer> list_options = (List<Integer>) os.get_dataPreprocessing().get("method");
        WordVectors vec = loadOrComputeModel(filename, ConstantsGlobal.ProcessingType.WORD2VEC, list_options);

        Instances data = Weka.createWEKAInstances("W2V", ConstantsWord2Vec.VECTOR_LENGTH);
        List<String> lines = corpus.get_texts();
        List<String> ids = corpus.get_ids();

        // -- Create Index
        Index.createIndex(lines);

        for (int i = 0; i < lines.size(); i++) {
            String tweet = lines.get(i);
            String id = ids.get(i);
            double[] weightedTweetVector = Word2vecDoc2vec.average_vector_weighted(tweet, lines, vec, i);
            // -- Create the Weka Instance
            Instance ins = Weka.createWEKAInstance(data, tweet, id, weightedTweetVector);
            data.add(ins); // Add instance to the instances
        }
        return data;
    }

    /**
     * Doc2Vec processing on the data
     * Generate Doc2Vec vectors
     *
     * @param os     Some options and parameters for the processing
     * @param corpus The corpus for the processing
     * @return Instances object for Weka clustering
     */
    public static Instances doc2vec(OptionsSerial os, Corpus corpus) throws IOException {
        log.trace("----------- doc2vec");

        String filename = os.get_filenameDoc2Vec();
        List<Integer> list_options = (List<Integer>) os.get_dataPreprocessing().get("method");

        WordVectors vec = loadOrComputeModel(filename, ConstantsGlobal.ProcessingType.DOC2VEC, list_options);

        Instances data = Weka.createWEKAInstances("D2V", ConstantsWord2Vec.VECTOR_LENGTH);
        List<String>lines = corpus.get_texts();
        List<String> ids = corpus.get_ids();

        for (int i = 0; i < lines.size(); i++) {
            String tweet = lines.get(i);
            String id = ids.get(i);
            double[] d2v_vector = vec.getWordVector("DOC_" + id);

            // -- Create the Weka Instance
            Instance ins = Weka.createWEKAInstance(data, tweet, id, d2v_vector);
            data.add(ins); // Add instance to the instances
        }

        return data;
    }

    /**
     * TFIDF processing on the data
     * Generate TF-IDF vectors
     *
     * @param os     Some options and parameters for the processing
     * @param corpus The corpus for the processing
     * @return Instances object for Weka clustering
     */
    public static Instances tfidf(OptionsSerial os, Corpus corpus) {
        log.trace("----------- tfidf");

        List<String>lines = corpus.get_texts();
        List<String> ids = corpus.get_ids();

        // -- Create Index
        Index.createIndex(lines);

        List<String> uniqueWords = Index.get_UniqueWords();
        Instances data = Weka.createWEKAInstances("TFIDF", uniqueWords.size());

        for (int i = 0; i < lines.size(); i++) {
            String tweet = lines.get(i);
            String id = ids.get(i);
            double[] tfidf_vector = TFIDFCalculator.tf_idfVector(lines, tweet, i);
            Instance ins = Weka.createWEKAInstance(data, tweet, id, tfidf_vector);
            data.add(ins); // Add instance to the instances
        }
        return data;
    }

    public static void cleanDoc2VecModel (WordVectors vec){
        Integer numWords = vec.vocab().numWords();
        for (int i = 0; i < numWords; i++) {
            String name = vec.vocab().elementAtIndex(i).getLabel();
            if (name != null && !name.startsWith("DOC_")) {
                vec.vocab().removeElement(name);
            }
        }
    }

    /**
     * ProcessingAction interface allowing to call methods more easily
     */
    interface ProcessingAction {
        Instances process(OptionsSerial os, Corpus corpus) throws Exception;
    }

    /**
     * Arrays allowing to call method with an index
     */
    private static ProcessingAction[] processingAction = new ProcessingAction[]{
            (os, corpus) -> word2vec(os, corpus),
            (os, corpus) -> doc2vec(os, corpus),
            (os, corpus) -> tfidf(os, corpus),
    };

    /**
     * Allows to execute a specific processing method
     *
     * @param os     Some options and parameters for the processing
     * @param corpus The corpus for the processing
     * @param index  The number corresponding to the processing method to execute
     * @return A string description of the results
     * @throws Exception
     */
    public static Instances execute_processing(OptionsSerial os, Corpus corpus, int index) throws Exception {
        return processingAction[index - 1].process(os, corpus);
    }

    /**
     * Allows to execute the processing stage
     *
     * @param os Some options and parameters in order to execute this stage
     * @throws Exception
     */
    public static void execute(OptionsSerial os) throws Exception {
        String filename = os.get_filenamePreprocessing();
        HashMap<String, Object> params = os.get_dataProcessing();
        W2vD2vValues param = (W2vD2vValues) params.get("method");
        Boolean stop = (Boolean) params.get("stop");

        File f = new File(filename);
        Corpus corpus;
        if (f.exists() && !f.isDirectory()) {
            log.trace("Pre-processing file already exists");
            corpus = (Corpus) Toolbox.deserialization(filename);
            Instances data = execute_processing(os, corpus, param.get_type());

            filename = os.get_filenameProcessing();
            corpus.set_instances(data);
            Toolbox.serialization(corpus, filename);

            if (!stop){
                DataClustering.execute(os);
            }

        } else { // --- Backtracking
            log.trace("Pre-processing file does not exist");
            DataPreprocessing.execute(os);
        }
    }
}

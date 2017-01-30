package evaluation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;

import experimentation.OptionsSerial;
import utils.W2vD2vValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thesis.Word2vecDoc2vec;
import constants.ConstantsGlobal;
import utils.Corpus;
import utils.Toolbox;

public class ModelsGenerator {

    private static Logger log = LoggerFactory.getLogger(ModelsGenerator.class);

    /**
     * It allows to generate a word2vec or doc2vec model if doesn't exist
     * @param corpus The messages from social network
     * @param filename The filename of the model
     * @param type The type of the model (w2v, d2v)
     * @throws IOException
     */
    public static void computeModelFromServer(Corpus corpus, String filename, ConstantsGlobal.ProcessingType type) throws IOException {
        File f = new File(filename);
        if (f.exists() && !f.isDirectory()) {
            log.warn(filename + " already exists");
        }
        else {
            List<String> lines = corpus.get_texts();
            Word2Vec w2v;
            if (type == ConstantsGlobal.ProcessingType.WORD2VEC){
                // -- Execute word2vec
                w2v = Word2vecDoc2vec.word2Vec(lines);
            } else {
                // -- Execute doc2vec
                w2v = Word2vecDoc2vec.doc2Vec(corpus);
                Word2vecDoc2vec.cleanDoc2VecModel(w2v);
            }
            // -- Save word2vec model
            WordVectorSerializer.writeWordVectors(w2v, filename);
        }
    }

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

        int type;
        W2vD2vValues optionProcessing;
        HashMap<String, Object> dataProcessing = new HashMap<>();
        OptionsSerial os;

        ArrayList<Integer> w2v_vectorLength = new ArrayList<>();
        w2v_vectorLength.add(70);
        w2v_vectorLength.add(100); // Defaut value (gensim)
        w2v_vectorLength.add(200);
        //w2v_vectorLength.add(300);

        ArrayList<Integer> w2v_windowSize = new ArrayList<>();
        w2v_windowSize.add(5); // Defaut value (gensim)
        w2v_windowSize.add(8);
        w2v_windowSize.add(10);

        ArrayList<Integer> w2v_minWordFrequency = new ArrayList<>();
        w2v_minWordFrequency.add(2);
        w2v_minWordFrequency.add(5); // Defaut value (gensim)
        w2v_minWordFrequency.add(8);
        //w2v_minWordFrequency.add(10);

        ArrayList<Integer> w2v_netIterations = new ArrayList<>();
        w2v_netIterations.add(5); // Defaut value (gensim)
        w2v_netIterations.add(50);
        w2v_netIterations.add(100);
        //w2v_netIterations.add(150);

        ArrayList<Integer> w2v_layerSize = new ArrayList<>();
        w2v_layerSize.add(200);
        w2v_layerSize.add(300); // Defaut value (gensim) ??
        w2v_layerSize.add(400);

        String filename = ConstantsGlobal.SERIALIZATION_SRC_DIRECTORY + "bigcorpus.serial";
        File file = new File(filename);
        Corpus corpus;
        if (file.exists() && !file.isDirectory()) {
            corpus = (Corpus) Toolbox.deserialization(filename);
            log.info("bigcorpus.serial read ... ");

            for (Integer aW2v_vectorLength : w2v_vectorLength) {
                for (Integer aW2v_windowSize : w2v_windowSize) {
                    for (Integer aW2v_minWordFrequency : w2v_minWordFrequency) {
                        for (Integer aW2v_netIteration : w2v_netIterations) {
                            for (Integer aW2v_layerSize : w2v_layerSize) {

                                type = 2; // 1 (word2vec), 2 (doc2vec)
                                optionProcessing = new W2vD2vValues(type,
                                        0.025f,  0.0001, aW2v_vectorLength, 1000, aW2v_minWordFrequency,
                                        aW2v_netIteration, 0.001, aW2v_layerSize, aW2v_windowSize);

                                dataProcessing.clear();
                                log.info(optionProcessing.toString());

                                dataProcessing.put("method", optionProcessing);
                                dataProcessing.put("stop", true);

                                parameters.put("Data_processing", dataProcessing);

                                os = new OptionsSerial(parameters);
                                computeModelFromServer(corpus, os.get_filenameDoc2Vec(), ConstantsGlobal.ProcessingType.DOC2VEC);
                            }
                        }
                    }
                }
            }
        }
    }
}

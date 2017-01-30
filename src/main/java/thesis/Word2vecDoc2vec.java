package thesis;

import constants.ConstantsWord2Vec;
import utils.Word2DocLabelterator;

import org.deeplearning4j.models.embeddings.WeightLookupTable;
import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.models.word2vec.wordstore.inmemory.InMemoryLookupCache;
import org.deeplearning4j.text.documentiterator.LabelsSource;
import org.deeplearning4j.text.sentenceiterator.CollectionSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import utils.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Anthony on 08/03/2016.
 */
public class Word2vecDoc2vec {

    private static Logger log = LoggerFactory.getLogger(Word2vecDoc2vec.class);

    public static void cleanDoc2VecModel (WordVectors vec){
        Integer numWords = vec.vocab().numWords();
        for (int i = 0; i < numWords; i++) {
            if (vec != null && vec.vocab() != null && vec.vocab().elementAtIndex(i) != null) {
                String name = vec.vocab().elementAtIndex(i).getLabel();
                if (name != null && !name.startsWith("DOC_")) {
                    vec.vocab().removeElement(name);
                }
            }
        }
    }

    /**
     * Word2Vec process
     *
     * @param lines The corpus
     * @return The Word2Vec model
     */
    public static Word2Vec word2Vec(List<String> lines) {

        SentenceIterator iter = new CollectionSentenceIterator(lines);
        log.info("# W2V Build model....");

        InMemoryLookupCache cache = new InMemoryLookupCache();
        WeightLookupTable table = new InMemoryLookupTable.Builder()
                .vectorLength(ConstantsWord2Vec.VECTOR_LENGTH)
                .useAdaGrad(false)
                .cache(cache)
                .lr(ConstantsWord2Vec.LEARNING_RATE).build();

        log.info("Building model....");
        Word2Vec vec = new Word2Vec.Builder()
                .batchSize(ConstantsWord2Vec.BATCH_SIZE)
                .sampling(ConstantsWord2Vec.SUBSAMPLING) //negative sampling
                .minWordFrequency(ConstantsWord2Vec.MIN_WORD_FREQUENCY)
                .iterations(ConstantsWord2Vec.NET_ITERATIONS)
                .layerSize(ConstantsWord2Vec.LAYER_SIZE)
                .lookupTable(table)
                .vocabCache(cache)
                .seed(42)
                .windowSize(ConstantsWord2Vec.WINDOW_SIZE)
                .iterate(iter)
                //.tokenizerFactory(tokenizer)
                .build();

        vec.fit();
        return vec;
    }

    private static Map<String,String> generateMapDoc2Vec(Corpus corpus){
        List<String> lines = corpus.get_texts();
        List<String> ids = corpus.get_ids();
        Map<String,String> docMap = new HashMap<>();
        Integer size = lines.size();
        for(int i = 0 ; i < size; i++){
            docMap.put("DOC_" + ids.get(i), lines.get(i));
        }
        return docMap;
    }


    public static ParagraphVectors doc2Vec(Corpus corpus) {

        Map<String,String> docMap = generateMapDoc2Vec(corpus);

        log.info("# D2V Build model....");

        TokenizerFactory tokenizer = new DefaultTokenizerFactory();
        tokenizer.setTokenPreProcessor(new CommonPreprocessor());

        InMemoryLookupCache cache = new InMemoryLookupCache ();
        WeightLookupTable table = new InMemoryLookupTable.Builder()
                .vectorLength(ConstantsWord2Vec.VECTOR_LENGTH)
                .useAdaGrad(false)
                .cache(cache)
                .lr(ConstantsWord2Vec.LEARNING_RATE).build();

        LabelsSource source = new LabelsSource();
        Word2DocLabelterator iterator = new Word2DocLabelterator.Builder()
                .build(docMap);

        ParagraphVectors vec = new ParagraphVectors.Builder()
                .batchSize(ConstantsWord2Vec.BATCH_SIZE)
                .sampling(ConstantsWord2Vec.SUBSAMPLING) //negative sampling
                .minWordFrequency(ConstantsWord2Vec.MIN_WORD_FREQUENCY)
                .iterations(ConstantsWord2Vec.NET_ITERATIONS)
                .layerSize(ConstantsWord2Vec.LAYER_SIZE)
                .lookupTable(table)
                .labelsSource(source)
                .iterate(iterator)
                .vocabCache(cache)
                .seed(42)
                .windowSize(ConstantsWord2Vec.WINDOW_SIZE)
                .tokenizerFactory(tokenizer)
                .build();

        vec.fit();
        return vec;
    }

    /**
     * Word2vec weighting vectors with TF IDF
     *
     * @param text  The text from social network
     * @param lines The entire corpus
     * @param vec   The word2vec vectors
     * @param id    The text id from the database
     * @return The weighted vectors
     */
    public static double[] average_vector_weighted(String text, List<String> lines, WordVectors vec, Integer id) {
        ArrayList<Double> tfidfVector = TFIDFCalculator.tf_idfBySentence(lines, text, id); // vector with X values
        double[] weightedTweetVector = new double[ConstantsWord2Vec.VECTOR_LENGTH];

        String[] words = text.split("\\s+");
        for (int i = 0; i < ConstantsWord2Vec.VECTOR_LENGTH; i++) {
            double numerator = 0;

            for (int j = 0; j < words.length; j++) {
                double[] word2VecVector = vec.getWordVector(words[j]);
                numerator += tfidfVector.get(j) * word2VecVector[i];
            }
            weightedTweetVector[i] = numerator;
        }
        return weightedTweetVector;
    }

    /**
     * Word2vec weighting vectors with TF IDF
     *
     * @param text  The text from social network
     * @param lines The entire corpus
     * @param vec   The word2vec model
     * @param id    The text id from the database
     * @return The weighted vectors
     */
    public static double[] average_vector_weighted(String text, List<String> lines, Word2Vec vec, Integer id) {
        ArrayList<Double> tfidfVector = TFIDFCalculator.tf_idfBySentence(lines, text, id); // vector with X values
        double[] weightedTweetVector = new double[ConstantsWord2Vec.VECTOR_LENGTH];

        String[] words = text.split("\\s+");
        for (int i = 0; i < ConstantsWord2Vec.VECTOR_LENGTH; i++) {
            double numerator = 0;

            for (int j = 0; j < words.length; j++) {
                double[] word2VecVector = vec.getWordVector(words[j]);
                numerator += tfidfVector.get(j) * word2VecVector[i];
            }
            weightedTweetVector[i] = numerator;
        }
        return weightedTweetVector;
    }
}
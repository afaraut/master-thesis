package thesis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by Anthony on 22/03/2016.
 */
public class TFIDFCalculator {

    private static Logger log = LoggerFactory.getLogger(TFIDFCalculator.class);
    /**
     * Calculation of the "Term frequency"
     *
     * @param doc          The index of the sentence in the corpus
     * @param term         The term we want to calculate the tf value
     * @param length_tweet The sentence's length
     * @return
     */
    private static double tf(Integer doc, String term, Integer length_tweet) {
        Map<String, Integer> tf_document = Index.get_indexTf().get(doc);
        if (tf_document.get(term) == null) {
            log.error("PROBLEM WITH TF-IDF INDEX AND THE TERM " + term);
            return 0;
        }
        return ((double) tf_document.get(term)) / length_tweet;
    }

    /**
     * Calculation of the "Inverse document frequency"
     *
     * @param docs The text corpus
     * @param term The term we want to calculate the idf value
     * @return
     */
    private static double idf(List<String> docs, String term) {
        Map<String, Integer> hash = Index.get_indexDf();
        Integer value = hash.get(term);
        return Math.log(docs.size() / value);
    }

    /**
     * Calculation of the "Term frequency – Inverse document frequency"
     *
     * @param doc          The index of the sentence in the corpus
     * @param docs         The text corpus
     * @param term         The term we want to calculate the tf-idf value
     * @param length_tweet The sentence's length
     * @return
     */
    public static double tf_idf(Integer doc, List<String> docs, String term, Integer length_tweet) {
        return tf(doc, term, length_tweet) * idf(docs, term);
    }

    /**
     * Calculation of the "Term frequency – Inverse document frequency" by sentence
     *
     * @param docs     The text corpus
     * @param doc      The sentence
     * @param indexDoc The index of the sentence in the corpus
     * @return
     */
    public static ArrayList<Double> tf_idfBySentence(List<String> docs, String doc, Integer indexDoc) {
        ArrayList<Double> tf_idf = new ArrayList<>();
        String[] words = doc.split("\\s+");
        Integer length_tweet = Index.get_lengthTweets()[indexDoc];
        for (int i = 0; i < words.length; i++) {
            tf_idf.add(tf_idf(indexDoc, docs, words[i], length_tweet));
        }
        return tf_idf;
    }

    public static Double tf_idfByWord(List<String> docs, String word, Integer indexDoc) {
        Integer length_tweet = Index.get_lengthTweets()[indexDoc];
        return tf_idf(indexDoc, docs, word, length_tweet);
    }

    public static double[] tf_idfVector(List<String> docs, String doc, Integer indexDoc) {
        List<String> uniqueWords = Index.get_UniqueWords();
        String[] words = doc.split("\\s+");
        double[] tf_idf = new double[uniqueWords.size()];
        Integer length_tweet = Index.get_lengthTweets()[indexDoc];

        for (int i = 0; i < words.length; i++) {
            // get the index of the word in the entire corpus
            Integer index = uniqueWords.indexOf(words[i]);
            tf_idf[index] = tf_idf(indexDoc, docs, words[i], length_tweet);
        }
        return tf_idf;
    }

}
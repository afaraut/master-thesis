package thesis;

import java.util.*;

/**
 * Created by Anthony on 30/03/2016.
 */
public class Index {

    private static Map<String, Integer> m_indexDf;;
    private static Map<Integer, HashMap<String, Integer>> m_indexTf;
    private static int[] m_lengthTweets;
    private static List<String> m_uniqueWords;

    public static Map<Integer, HashMap<String, Integer>> get_indexTf() {
        return m_indexTf;
    }
    public static Map<String, Integer> get_indexDf() {
        return m_indexDf;
    }
    public static int[] get_lengthTweets() {
        return m_lengthTweets;
    }
    public static List<String> get_UniqueWords() { return m_uniqueWords;}

    /**
     * Create the index for accelerate the TF_IDF calculation
     *
     * @param corpus The entire corpus
     */
    public static void createIndex(List<String> corpus) {

        m_indexTf = new HashMap<>();
        m_indexDf = new HashMap<>();
        m_uniqueWords = new ArrayList<>();
        m_lengthTweets = new int[corpus.size()];

        String[] wordsOfATweet;
        Set<String> uniqueWords = new LinkedHashSet<>();

        // -- For each document
        for (int j = 0; j < corpus.size(); j++) {
            wordsOfATweet = corpus.get(j).split("\\s+");
            HashMap<String, Integer> tfByTweet = new HashMap<>();

            // -- For each word in the document
            for (int i = 0; i < wordsOfATweet.length; i++) {
                String word = wordsOfATweet[i];
                uniqueWords.add(word);
                if (!tfByTweet.containsKey(word)) {
                    if (m_indexDf.containsKey(word)) {
                        m_indexDf.put(word, m_indexDf.get(word) + 1);
                    } else {
                        m_indexDf.put(word, 1);
                    }
                }

                if (tfByTweet.containsKey(word)) {
                    tfByTweet.put(word, tfByTweet.get(word) + 1);
                } else {
                    tfByTweet.put(word, 1);
                }
            }

            m_lengthTweets[j] = wordsOfATweet.length;
            m_indexTf.put(j, tfByTweet);
        }
        m_uniqueWords.addAll(uniqueWords);
        java.util.Collections.sort(m_uniqueWords);
    }
}

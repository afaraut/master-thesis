package experimentation;

import constants.ConstantsGlobal;
import constants.ConstantsPreProcessingRegex;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static utils.Toolbox.replaceByLeftPattern;
import static utils.Toolbox.replaceByRightPattern;

/**
 * Created by Anthony on 14/05/2016.
 */
public class DataPreprocessing {

    private static Logger log = LoggerFactory.getLogger(DataPreprocessing.class);

    private static Set<String> m_setOfUniqueHashtags;
    private static Set<String> m_setOfUniqueHashtagsFromAtLeastXUsers;

    /**
     * Allows to remove line breaks in a sentence
     *
     * @param sentence The sentence to clean
     * @return The sentence cleaned
     */
    public static String remove_line_breaks(String sentence) {
        return sentence.replaceAll(ConstantsPreProcessingRegex.REGEX_LINE_BREAK, " ");
    }

    /**
     * Allows to remove usernames in a sentence
     *
     * @param sentence The sentence to clean
     * @return The sentence cleaned
     */
    public static String remove_usernames(String sentence) {
        return sentence.replaceAll(ConstantsPreProcessingRegex.REGEX_USERNAME, " ");
    }

    /**
     * Allows to remove uri (links) in a sentence
     *
     * @param sentence The sentence to clean
     * @return The sentence cleaned
     */
    public static String remove_uri(String sentence) {
        return sentence.replaceAll(ConstantsPreProcessingRegex.REGEX_URI, " ");
    }

    /**
     * Allows to remove accents in a sentence
     *
     * @param sentence The sentence to clean
     * @return The sentence cleaned
     */
    public static String remove_accents(String sentence) {
        return StringUtils.stripAccents(sentence);
    }

    /**
     * Allows to clean following points (as example ...... !!!!!!! ????????) in a sentence
     * ....... -> .
     * !!!!!!!!!!!!!! -> !
     * ???? -> ?
     *
     * @param sentence The sentence to clean
     * @return The sentence cleaned
     */
    public static String clean_following_points(String sentence) {
        sentence = sentence.replaceAll(ConstantsPreProcessingRegex.REGEX_FOLLOWING_POINTS, " . ");
        sentence = sentence.replaceAll(ConstantsPreProcessingRegex.REGEX_FOLLOWING_INTERROGATION_POINTS, " ? ");
        return sentence.replaceAll(ConstantsPreProcessingRegex.REGEX_FOLLOWING_EXCLAMATION_POINTS, " ! ");
    }

    /**
     * Allows to add spaces between punctuations and words in a sentence
     * qwerty. -> qwerty .
     * qwerty, -> qwerty ,
     * qwerty? -> qwerty ?
     * qwerty! -> qwerty !
     * (qwerty) -> ( qwerty )
     * [qwerty] -> [ qwerty ]
     * "qwerty" -> " qwerty "
     *
     * @param sentence The sentence to clean
     * @return The sentence cleaned
     */
    public static String clean_space_between_punctuations(String sentence) {
        sentence = replaceByRightPattern(sentence, ConstantsPreProcessingRegex.REGEX_POINT, " . ");
        sentence = replaceByRightPattern(sentence, ConstantsPreProcessingRegex.REGEX_COMMA, " , ");
        sentence = replaceByRightPattern(sentence, ConstantsPreProcessingRegex.REGEX_INTERROGATION_POINT, " ? ");
        sentence = replaceByRightPattern(sentence, ConstantsPreProcessingRegex.REGEX_EXCLAMATION_POINT, " ! ");

        sentence = replaceByLeftPattern(sentence, ConstantsPreProcessingRegex.REGEX_LEFT_PARENTHESIS, " ( ");
        sentence = replaceByRightPattern(sentence, ConstantsPreProcessingRegex.REGEX_RIGHT_PARENTHESIS, " ) ");

        sentence = replaceByLeftPattern(sentence, ConstantsPreProcessingRegex.REGEX_LEFT_BRACKET, " [ ");
        sentence = replaceByRightPattern(sentence, ConstantsPreProcessingRegex.REGEX_RIGHT_BRACKET, " ] ");

        sentence = replaceByLeftPattern(sentence, ConstantsPreProcessingRegex.REGEX_LEFT_QUOTE, " \" ");
        return replaceByRightPattern(sentence, ConstantsPreProcessingRegex.REGEX_RIGHT_QUOTE, " \" ");
    }

    /**
     * Allows to clean following spaces in a sentence
     * azerty         qwerty -> azerty qwerty
     *
     * @param sentence The sentence to clean
     * @return The sentence cleaned
     */
    public static String clean_following_spaces(String sentence) {
        return sentence.replaceAll(ConstantsPreProcessingRegex.REGEX_FOLLOWING_SPACES, " ");
    }

    /**
     * Allows to trim and lowercase text in a sentence
     * QWERTY qwerty -> qwerty qwerty
     *
     * @param sentence The sentence to clean
     * @return The sentence cleaned
     */
    public static String clean_trim_lowercase(String sentence) {
        sentence = sentence.trim();
        return sentence.toLowerCase();
    }

    private static String enrichSentence(String sentence, Set<String> setHashtags){
        List<String> list = Arrays.asList((String[])sentence.split("\\s+"));
        for (String hashtag : setHashtags) {
            if (list.contains(hashtag)) {
                // Case of the first word
                if (list.indexOf(hashtag) == 0) {
                    sentence = sentence.replaceFirst(hashtag, "#" + hashtag);
                }
                sentence = sentence.replaceAll(" " + hashtag, " #" + hashtag);
            }
        }
        return sentence;
    }

    public static String enrichSentenceWithUniqueHashtags(String sentence) {
        if (m_setOfUniqueHashtags == null) {
            m_setOfUniqueHashtags = MongoDB.getUniqueHashtags();
        }
        return enrichSentence(sentence, m_setOfUniqueHashtags);
    }

    public static String enrichSentenceWithUniqueHashtagsFromAtLeastXUsers(String sentence) {
        /*if (m_setOfUniqueHashtagsFromAtLeastXUsers == null) {
            m_setOfUniqueHashtagsFromAtLeastXUsers = MongoDB.getUniqueHashtagsFromAtLeastXUsers();
        }*/
        if (m_setOfUniqueHashtagsFromAtLeastXUsers == null) {
            try {
                m_setOfUniqueHashtagsFromAtLeastXUsers = (Set<String>) Toolbox.deserialization(ConstantsGlobal.SERIALIZATION_SRC_DIRECTORY + "uniqueHashtags.serial");
            } catch (IOException e) {
                e.printStackTrace();
                log.error("PROBLEM WITH THE FILE -> uniqueHashtags.serial" );
            }
        }
        return enrichSentence(sentence, m_setOfUniqueHashtagsFromAtLeastXUsers);
    }

    public static String clean_all(String sentence) {
        String tmp_sentence = sentence;
        // length - 2 because the last element is clean all .. (This method)
        // the other elements are for the enrichment
        for (int i = 0; i < preProcessingAction.length - 2; i++){
            tmp_sentence = preProcessingAction[i].preprocess(tmp_sentence);
        }
        return tmp_sentence;
    }

    /**
     * PreProcessingAction interface allowing to call methods more easily
     */
    interface PreProcessingAction {
        String preprocess(String s);
    }

    /**
     * Arrays allowing to call method with an index
     */
    private static PreProcessingAction[] preProcessingAction = new PreProcessingAction[]{
            s -> remove_line_breaks(s),
            s -> remove_usernames(s),
            s -> remove_uri(s),
            s -> remove_accents(s),
            s -> clean_following_points(s),
            s -> clean_space_between_punctuations(s),
            s -> clean_following_spaces(s),
            s -> clean_trim_lowercase(s),
            //s -> enrichSentenceWithUniqueHashtags(s),
            s -> enrichSentenceWithUniqueHashtagsFromAtLeastXUsers(s),
            s -> clean_all(s),
    };

    /**
     * Allows to execute a specific pre-processing method
     *
     * @param s     The sentence to clean
     * @param index The number corresponding to the pre-processing method to execute
     * @return The sentence cleaned
     */
    public static String execute_pre_processing(String s, int index) {
        return preProcessingAction[index - 1].preprocess(s);
    }

    /**
     * Allows to execute the pre-processing stage
     *
     * @param os Some options and parameters in order to execute this stage
     * @throws Exception
     */
    public static void execute(OptionsSerial os) throws Exception {
        String filename = os.get_filenameLoading();
        HashMap<String, Object> params = os.get_dataPreprocessing();
        List<Integer> list_options = (List<Integer>) params.get("method");
        Boolean stop = (Boolean) params.get("stop");

        File f = new File(filename);
        Corpus corpus;

        if (f.exists() && !f.isDirectory()) {
            log.trace("Loading file already exists");
            corpus = (Corpus) Toolbox.deserialization(filename);

            List<String> sentences = corpus.get_texts();
            for (int i = 0; i < sentences.size(); i++) {
                String s = sentences.get(i);
                for (Integer value : list_options) {
                    String sentence = execute_pre_processing(s, value);
                    sentence = sentence.trim();
                    sentences.set(i, sentence);
                }
            }
            corpus.set_texts(sentences);
            filename = os.get_filenamePreprocessing();
            Toolbox.serialization(corpus, filename);

            if (!stop){
                DataProcessing.execute(os);
            }
        } else { // --- Backtracking
            log.trace("Loading file does not exist");
            DataLoading.execute(os);
        }
    }
}
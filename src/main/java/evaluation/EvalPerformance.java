package evaluation;

import constants.ConstantsGlobal;
import constants.ConstantsPreProcessingRegex;
import constants.ConstantsWord2Vec;
import org.bson.Document;
import org.deeplearning4j.models.word2vec.Word2Vec;
import thesis.Index;
import thesis.Word2vecDoc2vec;
import thesis.Weka;
import utils.*;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;
import static utils.Toolbox.regexMatch;

/**
 * Created by Anthony on 13/04/2016.
 */
public class EvalPerformance {

     /**
     * Load data from MongoDB database
     *
     * @param dbName       The name of the database
     * @param dbCollection The name of the collection
     * @param limit        The limit of element wanted
     * @return List containing the messages from social networks
     */
    public static Corpus loadData(String dbName, String dbCollection, int limit) {
        MongoDB.connection(dbName, dbCollection);

        List<String> lines = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        for (Document document : MongoDB.getCollection().find().projection(fields(include("text", "id_str"), excludeId())).limit(limit)) {

            String text = document.getString("text");
            text = text.replaceAll(ConstantsPreProcessingRegex.REGEX_LINE_BREAK, " ");
            text = text.toLowerCase();

            if (text.length() > 4 && regexMatch(text, ConstantsPreProcessingRegex.REGEX_ALPHANUMERIC_CHARACTER)) {
                lines.add(text);
                ids.add(document.getString("id_str"));
            }
        }
        MongoDB.close();
        return new Corpus(lines, ids);
    }

    public static void main(String[] args) throws Exception {
        String repertory = "./load/";
        for (int i = 5000; i <= 30000; i += 5000) {
// --------------------------------------------------------------------------------------------- 1. Loading data
            String nameFile = repertory + "1_loading_data_" + i + ".txt";
            String text = "Loading data " + i + " values";
            RuntimeInfo start = SystemInfo.info(SystemInfo.Moment.START);
            Corpus corpus = loadData(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERFDL2015, i);
            RuntimeInfo end = SystemInfo.info(SystemInfo.Moment.END);
            SystemInfo.info_memo_usage_time(start, end, text, nameFile);
// --------------------------------------------------------------------------------------------- 1.  Loading data

            List<String> lines = corpus.get_texts();
            List<String> ids = corpus.get_ids();

// --------------------------------------------------------------------------------------------- 2. Index creation
            nameFile = repertory + "2_creation_index_" + i + "_values.txt";
            text = "Creation index " + i + " values";
            start = SystemInfo.info(SystemInfo.Moment.START);
            // -- Create Index
            Index.createIndex(lines);
            end = SystemInfo.info(SystemInfo.Moment.END);
            SystemInfo.info_memo_usage_time(start, end, text, nameFile);
// --------------------------------------------------------------------------------------------- 2. Index creation

// --------------------------------------------------------------------------------------------- 3. Word2Vec
            nameFile = repertory + "3_word2vec_" + i + "_values.txt";
            text = "Word2vec " + i + " values";
            // -- Execute word2vec
            Word2Vec vec = Word2vecDoc2vec.word2Vec(lines);
            end = SystemInfo.info(SystemInfo.Moment.END);
            SystemInfo.info_memo_usage_time(start, end, text, nameFile);
// --------------------------------------------------------------------------------------------- 3. Word2Vec

            Instances data = Weka.createWEKAInstances("App", ConstantsWord2Vec.VECTOR_LENGTH);

// --------------------------------------------------------------------------------------------- 4. Word2VecTFIDFWekaInstance
            nameFile = repertory + "4_word2Vec_TFIDF_WekaInstance_" + i + "_values.txt";
            text = "Word2Vec TFIDF WekaInstance " + i + " values";

            for (int j = 0; j < lines.size(); j++) {
                String tweet = lines.get(j);
                String id = ids.get(j);
                double[] weightedTweetVector = Word2vecDoc2vec.average_vector_weighted(tweet, lines, vec, j);
                // -- Create the Weka Instance
                Instance ins = Weka.createWEKAInstance(data, tweet, id, weightedTweetVector);
                data.add(ins); // Add instance to the instances
            }
            end = SystemInfo.info(SystemInfo.Moment.END);
            SystemInfo.info_memo_usage_time(start, end, text, nameFile);
// --------------------------------------------------------------------------------------------- 4. Word2VecTFIDFWekaInstance
        }
    }
}

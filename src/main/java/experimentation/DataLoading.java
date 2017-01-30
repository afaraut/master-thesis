package experimentation;

import com.mongodb.client.FindIterable;
import constants.ConstantsPreProcessingRegex;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Anthony on 14/05/2016.
 */
public class DataLoading {

    private static Logger log = LoggerFactory.getLogger(DataLoading.class);

    /**
     * Create corpus from documents from database
     * /!\ Need to have a current connection to the database
     *
     * @param documents Documents from a previous request on the database
     * @return The Corpus created
     */
    private static Corpus create_corpus_from_documents(FindIterable<Document> documents) {
        List<String> lines = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        for (Document document : documents) {
            String text = document.getString("text");
            text = text.replaceAll(ConstantsPreProcessingRegex.REGEX_LINE_BREAK, " ");
            text = text.toLowerCase();
            lines.add(text);
            ids.add(document.getString("id_str"));
        }
        return new Corpus(lines, ids);
    }

    /**
     * Load data from the database with a max number of element
     *
     * @param dbName       The database name
     * @param dbCollection The database collection
     * @param limit        The max number of element
     * @return The Corpus loaded
     */
    public static Corpus load_data(String dbName, String dbCollection, int limit) {
        MongoDB.connection(dbName, dbCollection);
        Corpus corpus = create_corpus_from_documents(MongoDB.getElements(limit));
        MongoDB.close();
        return corpus;
    }

    /**
     * Load data from the database from a timestamp and with a max number of element
     *
     * @param dbName       The database name
     * @param dbCollection The database collection
     * @param timestamp    The timestamp from which to load the data
     * @param limit        The max number of element
     * @return The Corpus loaded
     */
    public static Corpus load_data_from(String dbName, String dbCollection, String timestamp, int limit) {
        MongoDB.connection(dbName, dbCollection);
        Corpus corpus = create_corpus_from_documents(MongoDB.getElementsFromTimestamp(timestamp, limit));
        MongoDB.close();
        return corpus;
    }

    /**
     * Load data from the database from a timestamp and with a max number of element
     *
     * @param dbName       The database name
     * @param dbCollection The database collection
     * @param timestampFrom    The timestamp from which to load the data
     * @param timestampTo    The timestamp until which to load the data
     * @param limit        The max number of element
     * @return The Corpus loaded
     */
    public static Corpus load_data_from_to(String dbName, String dbCollection, String timestampFrom, String timestampTo, int limit) {
        MongoDB.connection(dbName, dbCollection);
        Corpus corpus = create_corpus_from_documents(MongoDB.getElementsFromTimestampToTimestamp(timestampFrom, timestampTo, limit));
        MongoDB.close();
        return corpus;
    }

    /**
     * Load data from the database with a max number of element
     *
     * @param os Some options and parameters in order to execute this stage
     * @throws IOException
     */
    public static void data_loading(OptionsSerial os) throws Exception {
        HashMap<String, Object> params = os.get_dataLoading();

        String dbName = (String) params.get("dbName");
        String dbCollection = (String) params.get("dbCollection");
        Integer limit = (Integer) params.get("limit");

        Corpus corpus = load_data(dbName, dbCollection, limit);
        Toolbox.serialization(corpus, os.get_filenameLoading());
    }

    /**
     * Load data from the database from a timestamp and with a max number of element
     *
     * @param os Some options and parameters in order to execute this stage
     * @throws IOException
     */
    public static void data_loading_from(OptionsSerial os) throws Exception {
        HashMap<String, Object> params = os.get_dataLoading();

        String dbName = (String) params.get("dbName");
        String dbCollection = (String) params.get("dbCollection");
        String timestamp = (String) params.get("timestamp");
        Integer limit = (Integer) params.get("limit");

        Corpus corpus = load_data_from(dbName, dbCollection, timestamp, limit);
        Toolbox.serialization(corpus, os.get_filenameLoading());
    }

    /**
     * Load data from the database from a timestamp to a timestamp and with a max number of element
     *
     * @param os Some options and parameters in order to execute this stage
     * @throws IOException
     */
    public static void data_loading_from_to(OptionsSerial os) throws Exception {
        HashMap<String, Object> params = os.get_dataLoading();

        String dbName = (String) params.get("dbName");
        String dbCollection = (String) params.get("dbCollection");
        String timestampFrom = (String) params.get("timestamp_from");
        String timestampTo = (String) params.get("timestamp_to");
        Integer limit = (Integer) params.get("limit");
        log.trace("************************** REQUEST WITH PARAMETERS **************************");
        log.trace("timestampFrom " + timestampFrom);
        log.trace("timestampTo " + timestampTo);
        log.trace("*****************************************************************************");
        Corpus corpus = load_data_from_to(dbName, dbCollection, timestampFrom, timestampTo, limit);
        Toolbox.serialization(corpus, os.get_filenameLoading());
    }

    /**
     * ClusteringAction LoadingAction allowing to call methods more easily
     */
    interface LoadingAction {
        void loading(OptionsSerial os) throws Exception;
    }

    /**
     * Arrays allowing to call method with an index
     */
    private static LoadingAction[] loadingAction = new LoadingAction[]{
            os -> data_loading(os),
            os -> data_loading_from(os),
            os -> data_loading_from_to(os)
    };

    /**
     * Allows to execute a specific loading method
     *
     * @param os    Some options and parameters for the loading
     * @param index The number corresponding to the loading method to execute
     * @throws Exception
     */
    public static void execute_loading(OptionsSerial os, int index) throws Exception {
        loadingAction[index - 1].loading(os);
    }

    /**
     * Allows to execute the loading stage
     *
     * @param os Some options and parameters in order to execute this stage
     * @throws Exception
     */
    public static void execute(OptionsSerial os) throws Exception {
        HashMap<String, Object> params = os.get_dataLoading();
        execute_loading(os, (Integer) params.get("method"));
        Boolean stop = (Boolean) params.get("stop");
        if (!stop){
            DataPreprocessing.execute(os);
        }
    }
}
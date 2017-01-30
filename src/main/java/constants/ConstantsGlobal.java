package constants;

/**
 * Created by Anthony on 08/03/2016.
 */
public class ConstantsGlobal {
    /// ----- Database
    public static final String SERVER = "localhost";
    public static final int PORT = 27017;
    public static final String DBNAME = "ImageDataset";
    public static final String DBCOLLECTIONTWITTERFDL2015 = "TwitterFDL2015";
    public static final String DBCOLLECTIONTWITTERFDL2015CLUSTERED = "TwitterFDL2015Clustered";
    public static final String DBCOLLECTIONTWITTERCLUSTERORIANE = "TwitterClusterOriane";
    public static final String DBCOLLECTIONTWITTERORIANERENAMED = "TwitterOrianeRenamed";
    public static final String DBCOLLECTIONTWITTERFDL2015_CLUSTERS = "TwitterFDL2015Clusters";

    /// ----- Serialization
    public static final String SERIALIZATION_DIRECTORY = "./data/serialization/";
    public static final String SERIALIZATION_MODEL_DIRECTORY = "./data/serialization/model/";
    public static final String SERIALIZATION_SRC_DIRECTORY = "./data/src_serial/";

    /// ----- Clusters results
    public static final String CLUSTERING_RESULTS_DIRECTORY = "./data/results/";

    public enum ProcessingType {
        WORD2VEC, DOC2VEC
    }
}

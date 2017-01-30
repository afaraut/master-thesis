package experimentation;

import constants.ConstantsGlobal;
import utils.W2vD2vValues;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Anthony on 18/05/2016.
 */
public class OptionsSerial {

    private String m_filenameLoading;
    private String m_filenamePreprocessing;
    private String m_filenameProcessing;
    private String m_filenameClustering;
    private String m_filenameEvaluation;
    private String m_filenameExtraction;
    private String m_filenameWord2Vec;
    private String m_filenameDoc2Vec;

    private HashMap<String, Object> m_data_loading;
    private HashMap<String, Object> m_data_preprocessing;
    private HashMap<String, Object> m_data_processing;
    private HashMap<String, Object> m_data_clustering;
    private HashMap<String, Object> m_data_evaluation;
    private HashMap<String, Object> m_data_extraction;

    public OptionsSerial(HashMap<String, HashMap<String, Object>> parameters) throws Exception {

        m_data_loading =  parameters.get("Data_loading");
        m_data_preprocessing = parameters.get("Data_preprocessing");
        m_data_processing = parameters.get("Data_processing");;
        m_data_clustering = parameters.get("Data_clustering");
        m_data_evaluation = parameters.get("Data_evaluation");
        m_data_extraction = parameters.get("Data_extraction");

        generateFilenames();
    }

    /**
     * Generates all filenames for the serialization and the params files
     */
    private void generateFilenames() {

        Integer m_data_method_loading = (Integer) m_data_loading.get("method");
        List<Integer> m_data_method_preprocessing = (List<Integer>) m_data_preprocessing.get("method");
        W2vD2vValues m_data_method_processing = (W2vD2vValues) m_data_processing.get("method");

        m_filenameLoading = ConstantsGlobal.SERIALIZATION_DIRECTORY + m_data_method_loading;

        String tmp = "";
        for (Integer i : m_data_method_preprocessing) {
            tmp = tmp.concat(i + "-");
        }
        tmp = tmp.substring(0, tmp.length() - 1);
        m_filenamePreprocessing = m_filenameLoading + "_" + tmp;
        m_filenameProcessing = m_filenamePreprocessing + "_" + m_data_method_processing.generateNamefile("-");


        if (m_data_processing != null){
            m_filenameWord2Vec = ConstantsGlobal.SERIALIZATION_MODEL_DIRECTORY + "W2V_" + m_data_method_processing.generateNamefile("_") + ".serial";
            m_filenameDoc2Vec = ConstantsGlobal.SERIALIZATION_MODEL_DIRECTORY + "D2V_" + m_data_method_processing.generateNamefile("_") + ".serial";
        }

        if (m_data_clustering != null){

            Integer m_data_method_clustering = (Integer) m_data_clustering.get("method");

            if (m_data_method_clustering == 1) {
                Integer numberOfCluster = (Integer) m_data_clustering.get("numberOfCluster");
                m_filenameClustering = m_filenameProcessing + "_" + m_data_method_clustering + "-" + numberOfCluster;
            }
            else if (m_data_method_clustering == 2 || m_data_method_clustering == 3) {
                Integer epsilon = (Integer) m_data_clustering.get("epsilon");
                Integer minPoints = (Integer) m_data_clustering.get("minPoints");
                m_filenameClustering = m_filenameProcessing + "_" + m_data_method_clustering + "-" + epsilon + "-" + minPoints;
            }
        }

        if (m_data_evaluation != null) {
            Integer m_data_method_evaluation = (Integer) m_data_evaluation.get("method");
            m_filenameEvaluation = m_filenameClustering + "_eval_" + m_data_method_evaluation;
            m_filenameEvaluation += ".csv";
        }
        if (m_data_extraction != null) {
            Integer m_data_method_extraction = (Integer) m_data_extraction.get("method");
            m_filenameExtraction = m_filenameClustering + "_extrac_" + m_data_method_extraction;
            m_filenameExtraction += ".csv";
        }

        m_filenameLoading += ".serial";
        m_filenamePreprocessing += ".serial";
        m_filenameProcessing += ".serial";
        m_filenameClustering += ".serial";

    }


    public String get_filenameLoading() {
        return m_filenameLoading;
    }

    public String get_filenamePreprocessing() {
        return m_filenamePreprocessing;
    }

    public String get_filenameProcessing() {
        return m_filenameProcessing;
    }

    public String get_filenameClustering() {
        return m_filenameClustering;
    }

    public HashMap<String, Object> get_dataLoading() {
        return m_data_loading;
    }

    public HashMap<String, Object> get_dataPreprocessing() {
        return m_data_preprocessing;
    }

    public HashMap<String, Object> get_dataProcessing() {
        return m_data_processing;
    }

    public HashMap<String, Object> get_dataClustering() {
        return m_data_clustering;
    }

    public HashMap<String, Object> get_dataEvaluation() {
        return m_data_evaluation;
    }

    public HashMap<String, Object> get_dataExtraction() {
        return m_data_extraction;
    }

    public String get_filenameWord2Vec() {
        return m_filenameWord2Vec;
    }

    public String get_filenameDoc2Vec() {
        return m_filenameDoc2Vec;
    }

    public String get_filenameEvaluation() {
        return m_filenameEvaluation;
    }

    public String get_filenameExtraction() {
        return m_filenameExtraction;
    }
}


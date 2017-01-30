package utils;

import constants.ConstantsWord2Vec;

/**
 * Created by Anthony on 07/08/2016.
 * Package : utils .
 * Project : PhDTrack.
 */
public class W2vD2vValues {

    private int m_type;
    private float m_learningRate;
    private double m_minLearningRate;
    private int m_vectorLength;
    private int m_batchSize;
    private int m_minWordFrequency;
    private int m_netIterations;
    private double m_subSampling;
    private int m_layerSize;
    private int m_windowSize;

    public W2vD2vValues(int type, float learningRate, double minLearningRate, int vectorLength, int batchSize,
                        int minWordFrequency, int netIterations, double subSampling, int layerSize,
                        int windowSize){

        ConstantsWord2Vec.LEARNING_RATE = learningRate;
        ConstantsWord2Vec.MIN_LEARNING_RATE = minLearningRate;
        ConstantsWord2Vec.VECTOR_LENGTH = vectorLength;
        ConstantsWord2Vec.BATCH_SIZE = batchSize;
        ConstantsWord2Vec.MIN_WORD_FREQUENCY = minWordFrequency;
        ConstantsWord2Vec.NET_ITERATIONS = netIterations;
        ConstantsWord2Vec.SUBSAMPLING = subSampling;
        ConstantsWord2Vec.LAYER_SIZE = layerSize;
        ConstantsWord2Vec.WINDOW_SIZE = windowSize;

        m_type = type;
        m_learningRate  = ConstantsWord2Vec.LEARNING_RATE;
        m_minLearningRate = ConstantsWord2Vec.MIN_LEARNING_RATE;
        m_vectorLength = ConstantsWord2Vec.VECTOR_LENGTH;
        m_batchSize = ConstantsWord2Vec.BATCH_SIZE;
        m_minWordFrequency = ConstantsWord2Vec.MIN_WORD_FREQUENCY;
        m_netIterations = ConstantsWord2Vec.NET_ITERATIONS;
        m_subSampling = ConstantsWord2Vec.SUBSAMPLING;
        m_layerSize = ConstantsWord2Vec.LAYER_SIZE;
        m_windowSize = ConstantsWord2Vec.WINDOW_SIZE;
    }

    public String generateNamefile(String delimiter){

        if (delimiter.equals(",")){
            return "Delimiter ',' not allowed";
        }

        String learningRate = "" + m_learningRate;
        learningRate = learningRate.replaceAll("\\.",",");
        String minLearningRate = "" + m_minLearningRate;
        minLearningRate = minLearningRate.replaceAll("\\.", ",");
        String subSampling = "" + m_subSampling;
        subSampling = subSampling.replaceAll("\\.", ",");

        return m_type + delimiter + learningRate + delimiter + minLearningRate + delimiter + m_vectorLength + delimiter +
                m_batchSize + delimiter + m_minWordFrequency + delimiter + m_netIterations + delimiter +
                subSampling + delimiter + m_layerSize + delimiter + m_windowSize;
    }

    public int get_type(){
        return m_type;
    }

    public String toString() {
        return m_type + " "  + m_learningRate + " "  + m_minLearningRate + " "  + m_vectorLength + " "  +
                m_batchSize + " "  + m_minWordFrequency + " "  + m_netIterations + " "  +
                m_subSampling + " "  + m_layerSize + " "  + m_windowSize;
    }
}
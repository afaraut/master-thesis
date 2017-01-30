package thesis;

import java.util.ArrayList;

/**
 * Created by Anthony on 25/04/2016.
 */
public class CalculationDistance {

    /**
     * Allows to calculate the average euclidean distance from a vector list
     *
     * @param vector_list The list containing all the vectors
     * @return The average euclidean distance
     */
    public static double average_euclidean_distance(ArrayList<double[]> vector_list) {
        int size = vector_list.size();
        double average = 0;
        for (int i = 0; i < size; i++) {
            double[] vect = vector_list.get(i);
            for (int j = i + 1; j < size; j++) {
                average += calculate_euclidean_distance(vect, vector_list.get(j));
            }
        }
        average /= (((size * size) / 2) - size);
        return average;
    }

    /**
     * Allows to calculate the euclidean distance between two vectors
     *
     * @param vectorA The first vector
     * @param vectorB The second vector
     * @return The euclidean distance between the two vectors
     */
    public static double calculate_euclidean_distance(double[] vectorA, double[] vectorB) {
        double sum = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            sum += Math.pow((vectorA[i] - vectorB[i]), 2);
        }
        return Math.sqrt(sum);
    }

    /**
     * Allows to calculate the average cosine distance from a vector list
     *
     * @param vector_list The list containing all the vectors
     * @return The average cosine distance
     */
    public static double average_cosine_distance(ArrayList<double[]> vector_list) {
        int size = vector_list.size();
        double average = 0;
        for (int i = 0; i < size; i++) {
            double[] vect = vector_list.get(i);
            for (int j = i + 1; j < size; j++) {
                average += calculate_cosine_distance(vect, vector_list.get(j));
            }
        }
        average /= (((size * size) / 2) - size);
        return average;
    }

    /**
     * Allows to calculate the cosine distance between two vectors
     *
     * @param vectorA The first vector
     * @param vectorB The second vector
     * @return The cosine distance between the two vectors
     */
    public static double calculate_cosine_distance(double[] vectorA, double[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}

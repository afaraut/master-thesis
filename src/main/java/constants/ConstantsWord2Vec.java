package constants;

/**
 * Created by Anthony on 31/03/2016.
 */
public class ConstantsWord2Vec {

    public static float LEARNING_RATE = 0.025f;
    public static double MIN_LEARNING_RATE = 1e-3; // It is the floor on the learning rate
    public static int VECTOR_LENGTH = 100; // Vector length - specifies the number of features in the word vector.
    public static int BATCH_SIZE = 1000; // It's the amount of words you process at a time.
    public static int MIN_WORD_FREQUENCY = 5; // It's the minimum number of times a word must appear in the corpus.
    public static int NET_ITERATIONS = 100; // Seems to be good
    public static double SUBSAMPLING = 1e-5;
    public static int LAYER_SIZE = 100;
    public static int WINDOW_SIZE = 8;

}

package experimentation;

import org.bson.Document;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Clusters;
import constants.ConstantsGlobal;
import utils.Corpus;
import utils.Toolbox;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import static utils.MongoDB.getAllTweets;
import static utils.MongoDB.getFDLTweets;

/**
 * Created by Anthony on 25/05/2016.
 */
public class DataVisualisation extends ApplicationFrame {

    private static Logger log = LoggerFactory.getLogger(DataLoading.class);

    /**
     * Constructor, generates the frame
     *
     * @param title       The title of the frame
     * @param repartition The array containing the clusters repartition
     */
    public DataVisualisation(final String title, int[] repartition) {
        super(title);
        IntervalXYDataset dataset = create_dataset(repartition);

        JFreeChart chart = ChartFactory.createXYBarChart(title, "Cluster number", false, "Number of element per cluster ",
                dataset, PlotOrientation.VERTICAL, true, true, false);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        setContentPane(chartPanel);
    }

    /**
     * Create the dataset of the values
     *
     * @param repartition The array containing the clusters repartition
     * @return The XY values for the display
     */
    private IntervalXYDataset create_dataset(int[] repartition) {
        XYSeries series = new XYSeries("Clustering");
        int length = repartition.length;

        for (int i = 0; i < length; i++) {
            series.add(i, repartition[i]);
        }
        return new XYSeriesCollection(series);
    }

    /**
     * Generates json file for GoogleMapApi
     * @param documents The messages from social networks
     * @param filename The filename of the json file
     */
    public static void generateMap(ArrayList<Document> documents, String filename){
        JSONObject elements = new JSONObject();
        for (Document doc : documents){
            String text = doc.getString("text").replaceAll("\\n", "");
            Document geo = (Document)doc.get("geo");
            Integer cluster = doc.getInteger("cluster");
            ArrayList<Double> coordinates = (ArrayList<Double>)geo.get("coordinates");
            Double latitude = Double.parseDouble("" + coordinates.get(0));
            Double longitude = Double.parseDouble("" + coordinates.get(1));

            JSONObject place = new JSONObject();
            JSONObject geolocation = new JSONObject();
            geolocation.accumulate("latitude", latitude);
            geolocation.accumulate("longitude", longitude);

            place.accumulate("geo", geolocation);
            place.accumulate("text", text);
            place.accumulate("cluster", cluster);
            elements.accumulate("points", place);
        }
        FileWriter file = null;
        try {
            file = new FileWriter(filename);
            file.write("var mapse=" + Toolbox.JSONObjecttoPrettyJSONFormat( elements.toString())+ ";");
        } catch (IllegalArgumentException | IOException e) {
            e.printStackTrace();

        } finally {
            try {
                file.flush();
                file.close();
            } catch (IllegalArgumentException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(final String[] args) throws IOException {
        Corpus corpus = (Corpus) Toolbox.deserialization(ConstantsGlobal.SERIALIZATION_DIRECTORY + "2_1-2-3_7-9_2.serial");
        Clusters cls = corpus.get_clusters();
        int[] repartition = cls.get_repartition();

        DataVisualisation demo = new DataVisualisation(cls.toString(), repartition);
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);



        // -- GoogleMapAPI
        ArrayList<Document> documents = getFDLTweets();
        generateMap(documents, "FDLtweets.js");
        log.trace("FDL tweets generated");

        documents = getAllTweets();
        generateMap(documents, "Alltweets.js");
        log.trace("All tweets generated");
    }
}
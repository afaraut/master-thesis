package utils;

import constants.ConstantsGlobal;
import org.bson.Document;

import java.io.IOException;
import java.util.*;

import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;

/**
 * Created by Anthony on 20/06/2016.
 */
public class RealClustering {

    public static HashMap<String, ArrayList<ClusterElement>> getDatasetClustered() {
        HashMap<String, ArrayList<ClusterElement>> datasetClustered = new HashMap<>();
        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERORIANERENAMED);
        for (Document document : MongoDB.getCollection().find().projection(fields(include("text", "cluster_id", "id_str"), excludeId()))) {
            String sentence = document.getString("text");
            String cluster_id = document.getString("cluster_id");
            String id_twitter = document.getString("id_str");
            ClusterElement ce = new ClusterElement(id_twitter, sentence, Integer.parseInt(cluster_id));
            ArrayList<ClusterElement> list;
            if (datasetClustered.containsKey(cluster_id)) {
                list = datasetClustered.get(cluster_id);
            } else {
                list = new ArrayList<>();
            }
            list.add(ce);
            datasetClustered.put(cluster_id, list);
        }
        MongoDB.close();
        return datasetClustered;
    }

    public static ArrayList<ClusterElement> getDatasetBinaryClusteringInListFromIds(ArrayList<String> ids) {
        ArrayList<ClusterElement> listMapClusters = new ArrayList();
        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERFDL2015CLUSTERED);
        for (Document document : MongoDB.getCollection().find().projection(fields(include("text", "cluster", "id_str"), excludeId()))) {
            String sentence = document.getString("text");
            Integer cluster_id = document.getInteger("cluster");
            String id_twitter = document.getString("id_str");
            if (ids.contains(id_twitter)) {
                listMapClusters.add(new ClusterElement(id_twitter, sentence, cluster_id));
            }
        }
        MongoDB.close();
        return listMapClusters;
    }

    public static ArrayList<ClusterElement> getDatasetBinaryClusteringInListFromIdsServer(ArrayList<String> ids, String filename) throws IOException {
        ArrayList<ClusterElement> corpus = (ArrayList<ClusterElement>) Toolbox.deserialization(filename);
        ArrayList<ClusterElement> elements = new ArrayList<>();
        for (ClusterElement ce : corpus) {
            if (ids.contains(ce.get_idTwitter())) {
                elements.add(ce);
            }
        }
        return elements;
    }
}

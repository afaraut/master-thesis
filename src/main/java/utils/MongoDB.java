package utils;

import com.mongodb.client.FindIterable;
import constants.ConstantsGlobal;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;

/**
 * Created by Anthony on 08/03/2016.
 */
public class MongoDB {
    private static MongoClient mongoClient;
    private static MongoCollection<Document> collection;

    /**
     * Connection to the MongoDB database
     *
     * @param dataBaseName   The name of the database
     * @param collectionName The name of the collection
     */
    public static void connection(String dataBaseName, String collectionName) {
        mongoClient = new MongoClient(ConstantsGlobal.SERVER, ConstantsGlobal.PORT);
        MongoDatabase db = mongoClient.getDatabase(dataBaseName);
        collection = db.getCollection(collectionName);
    }

    /**
     * Getter for the MongoDB collection
     *
     * @return The MongoDB collection
     */
    public static MongoCollection<Document> getCollection() {
        return collection;
    }

    /**
     * Get an element from the database using its id
     *
     * @param dataBaseName   The name of the database
     * @param collectionName The name of the collection
     * @param id             The id of the element
     * @return The element wanted
     */
    public static Document getElementById(String dataBaseName, String collectionName, String id) {
        MongoDB.connection(dataBaseName, collectionName);
        Document d = collection.find(new Document("id_str", id)).first();
        MongoDB.close();
        return d;
    }

    /**
     * Get X element(s) from the database
     *
     * @param limit The max number of elements wanted
     * @return The X elements
     */
    public static FindIterable<Document> getElements(Integer limit) {
        FindIterable<Document> docs;
        if (limit == -1) {
            docs = collection.find().projection(fields(include("text", "id_str"), excludeId()));
        }
        else {
            docs = collection.find().projection(fields(include("text", "id_str"), excludeId())).limit(limit);
        }
        return docs;
    }

    /**
     * Get X element(s) from the database from a specific timestamp
     *
     * @param timestamp The first element will start after this timestamp
     * @param limit     The max number of elements wanted
     * @return The X elements
     */
    public static FindIterable<Document> getElementsFromTimestamp(String timestamp, Integer limit) {
        FindIterable<Document> docs;
        if (limit == -1) {
            docs = collection.find(new Document("timestamp_ms", new Document("$gt", timestamp)));
        }
        else {
            docs = collection.find(new Document("timestamp_ms", new Document("$gt", timestamp))).limit(limit);
        }
        return docs;
    }

    /**
     * Get X element(s) from the database from a specific timestamp
     *
     * @param timestampFrom The first element will start after this timestamp
     * @param timestampTo The last element will end before this timestamp
     * @param limit     The max number of elements wanted
     * @return The X elements
     */
    public static FindIterable<Document> getElementsFromTimestampToTimestamp(String timestampFrom, String timestampTo, Integer limit) {
        FindIterable<Document> docs;
        if (limit == -1) {
            docs = collection.find(new Document("timestamp_ms", new Document("$gt", timestampFrom).append("$lt", timestampTo)));
        }
        else {
            docs = collection.find(new Document("timestamp_ms", new Document("$gt", timestampFrom).append("$lt", timestampTo))).limit(limit);
        }
        return docs;
    }


    /**
     * Get the hashtags from the database
     * @return The hashtags present in the database
     */
    private static FindIterable<Document> getHashtags(){
        FindIterable<Document> docs;
        docs = collection.find(new Document("entities.hashtags.0", new Document("$exists", true))).projection(fields(include("entities.hashtags.text", "id_str", "user.id_str"), excludeId()));
        return docs;
    }

    /**
     * Get the hashtags cleaned from the database
     * @return The hashtags cleaned present in the database
     */
    public static Set<String> getUniqueHashtags() {
        Set<String> set = new TreeSet<>();
        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERFDL2015);

        for (Document document : MongoDB.getHashtags()) {
            JSONObject json = new JSONObject(document).getJSONObject("entities");
            JSONArray array = json.getJSONArray("hashtags");

            for (int i = 0; i < array.length(); i++) {
                String text = ((JSONObject) array.get(i)).getString("text");
                text = text.toLowerCase();
                text = StringUtils.stripAccents(text);
                // --- Remove non-alphanumeric hashtags
                Pattern pattern = Pattern.compile("[^a-zA-ZÀ-ÿ\\d\\s:]");
                Matcher matcher = pattern.matcher(text);
                if (!matcher.find()) {
                    set.add(text);
                }
            }
        }
        MongoDB.close();
        return set;
    }

    /**
     * Get the hashtags cleaned from at least X users from the database
     * @return The hashtags cleaned from at least X users present in the database
     */
    public static Set<String> getUniqueHashtagsFromAtLeastXUsers() {
        Map<String, Set<String>> map = new HashMap<>();
        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERFDL2015);
        for (Document document : MongoDB.getHashtags()) {
            JSONObject json_entities = new JSONObject(document).getJSONObject("entities");
            String user = new JSONObject(document).getJSONObject("user").getString("id_str");
            JSONArray array = json_entities.getJSONArray("hashtags");

            for (int i = 0; i < array.length(); i++) {
                String text = ((JSONObject) array.get(i)).getString("text");
                text = text.toLowerCase();
                text = StringUtils.stripAccents(text);

                Set<String> tmp_set = new TreeSet<>();
                tmp_set.add(user);

                // --- Remove non-alphanumeric hashtags
                Pattern pattern = Pattern.compile("[^a-zA-ZÀ-ÿ\\d\\s:]");
                Matcher matcher = pattern.matcher(text);
                if (!matcher.find()) {
                    if (map.containsKey(text)) {
                        // --- Adding the rest of the set
                        tmp_set.addAll(map.get(text));
                    }
                    // --- Adding the new hashtag
                    map.put(text, tmp_set);
                }
            }
        }
        MongoDB.close();

        // In the map there is now :
        //
        //       Key : #hashtag - Value : user1, user2, user3 (the users whose have posted this #hashtag
        //
        //      - All the hashtag by user
        //      - Now, all the hashtag posted by at least X users will be kept
        //      - The others will be removed

        Set<String> set = new TreeSet<>();
        for (Map.Entry<String, Set<String>> item : map.entrySet()) {
            if (item.getValue().size() >= 2) {
                set.add(item.getKey());
            }
        }
        return set;
    }

    public static ArrayList<Document> getFDLTweets(){
        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERFDL2015CLUSTERED);
        ArrayList<Document> documents = MongoDB.getCollection().find(new Document("cluster", new Document("$eq", 1)).append("geo", new Document("$exists", true))).into(new ArrayList<>());
        MongoDB.close();
        return documents;
    }

    public static ArrayList<Document> getAllTweets(){
        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERFDL2015CLUSTERED);
        ArrayList<Document> documents = MongoDB.getCollection().find(new Document("geo", new Document("$exists", true))).into(new ArrayList<>());
        MongoDB.close();
        return documents;
    }

    /**
     * Get X tweet(s) from a timestamp
     * @return The X elemnts from the timestamp given
     */
    public static Corpus get700TweetsFromFDL2015(int number, String timestamp) {
        List<String> lines = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERFDL2015);

        // -- 1449594000000 : 8 dec 2015 - 18h 00: 00
        for (Document document : MongoDB.getElementsFromTimestamp(timestamp, number)) {
            lines.add(document.getString("text"));
            ids.add(document.getString("id_str"));
        }

        MongoDB.close();
        return new Corpus(lines, ids);
    }

    /**
     * Close the connection to the MongoDB database
     */
    public static void close() {
        mongoClient.close();
    }
}
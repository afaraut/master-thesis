package utils;

import constants.ConstantsGlobal;
import constants.ConstantsPreProcessingRegex;
import experimentation.DataProcessing;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.*;

import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;
import static java.util.Arrays.asList;
import static utils.RealClustering.getDatasetClustered;
import static utils.Toolbox.*;

/**
 * Created by Anthony on 30/08/2016.
 */
public class DataFromDataset {

    private static Logger log = LoggerFactory.getLogger(DataFromDataset.class);

    public static void print_HandMadeClusters () {
        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERCLUSTERORIANE);
        HashMap<String, String> idname = new HashMap<>();
        for (Document document : MongoDB.getCollection().find().projection(fields(include("id", "name"), excludeId()))) {
            String id = document.getString("id");
            String name = document.getString("name");
            idname.put(id, name);
        }
        MongoDB.close();

        HashMap<String, ArrayList<ClusterElement>> mapOrianeClusters = getDatasetClustered();
        for(Map.Entry<String, ArrayList<ClusterElement>> entry : mapOrianeClusters.entrySet()) {
            String key = entry.getKey();
            ArrayList<ClusterElement> value = entry.getValue();
            String name = idname.get(key);
            log.trace("[" + name + "] " + value.size());
        }
    }

    public static ArrayList<Integer> print_elementsPerHours (Integer hours) {
        ArrayList<Integer> elements = new ArrayList<>();

        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERFDL2015);

        Document min = MongoDB.getCollection().aggregate(asList(
                new Document("$group", new Document("_id", null).append("min_timestamp_ms", new Document("$min", "$timestamp_ms"))))).first();

        Document max = MongoDB.getCollection().aggregate(asList(
                new Document("$group", new Document("_id", null).append("max_timestamp_ms", new Document("$max", "$timestamp_ms"))))).first();

        long min_timestamp = Long.parseLong(min.getString("min_timestamp_ms"));
        long max_timestamp = Long.parseLong(max.getString("max_timestamp_ms"));

        long window = Toolbox.hours2milliseconds(hours);
        int counter;
        long lt;

        for (long gt = min_timestamp; gt < max_timestamp; gt+=window) {
            lt = (gt + window);

            counter = MongoDB.getCollection().find(new Document("timestamp_ms", new Document("$gt", ""+gt).append("$lt", ""+lt))).into(new ArrayList<>()).size();
            elements.add(counter);
        }

        MongoDB.close();

        for(Integer element : elements) {
            log.trace("" + element);
        }

        return elements;
    }

    public static void print_NumberOfTweetWithMedia(){
        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERFDL2015);
        log.trace("" + MongoDB.getCollection().find(new Document("extended_entities.media.0", new Document("$exists", true))).into(new ArrayList<>()).size());
        MongoDB.close();
    }

    public static void print_NumberOfTweetWithMediaFeteDesLumieres() {
        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERFDL2015_CLUSTERS);
        ArrayList test = MongoDB.getCollection().find(new Document("cluster", new Document("$eq", 1))).projection(fields(include("id_str"), excludeId())).into(new ArrayList<>());
        MongoDB.close();
        int counter = 0;
        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERFDL2015);
        for (Object s : test){
            String id = ((Document)s).getString("id_str");
            Document doc = MongoDB.getCollection().find(new Document("id_str", new Document("$eq", id))).projection(fields(include("extended_entities.media.0"), excludeId())).first();
            Object extended_entities = doc.get("extended_entities");
            if (extended_entities != null){
                counter++;
            }
        }
        MongoDB.close();
        log.trace("counter " +counter);
    }

    public static void print_NumberOfTweetWithPhoto(){
        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERFDL2015);
        log.trace("" + MongoDB.getCollection().find(new Document("extended_entities.media.0.type", new Document("$eq", "photo"))).into(new ArrayList<>()).size());
        MongoDB.close();
    }

    public static void print_NumberOfTweetWithPhotoVideoFeteDesLumieres(){
        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERFDL2015_CLUSTERS);
        ArrayList test = MongoDB.getCollection().find(new Document("cluster", new Document("$eq", 1))).projection(fields(include("id_str"), excludeId())).into(new ArrayList<>());
        MongoDB.close();
        int counterPhoto = 0;
        int counterVideo = 0;
        int counterAnimated_gif = 0;
        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERFDL2015);
        for (Object s : test){
            String id = ((Document)s).getString("id_str");
            Document doc = MongoDB.getCollection().find(new Document("id_str", new Document("$eq", id))).projection(fields(include("extended_entities.media"), excludeId())).first();
            Document extended_entities = (Document) doc.get("extended_entities");
            if (extended_entities != null) {
                String type = ((Document) ((ArrayList) extended_entities.get("media")).get(0)).getString("type");
                if (type != null && type.equals("photo")){
                    counterPhoto++;
                }
                else if (type != null && type.equals("video")){
                    counterVideo++;
                }
                if (type != null && type.equals("animated_gif")){
                    counterAnimated_gif++;
                }
            }
        }
        MongoDB.close();
        log.trace("counterPhoto " + counterPhoto);
        log.trace("counterVideo " + counterVideo);
        log.trace("counterAnimated_gif " + counterAnimated_gif);
    }

    public static void print_NumberOfTweetWithVideo(){
        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERFDL2015);
        log.trace("" + MongoDB.getCollection().find(new Document("extended_entities.media.0.type", new Document("$eq", "video"))).into(new ArrayList<>()).size());
        MongoDB.close();
    }

    public static void print_NumberOfTweetWithAnimated_gif(){
        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERFDL2015);
        log.trace("" + MongoDB.getCollection().find(new Document("extended_entities.media.0.type", new Document("$eq", "animated_gif"))).into(new ArrayList<>()).size());
        MongoDB.close();
    }

    public static void print_NumberOfTweetWithURI(){
        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERFDL2015);
        log.trace("" + MongoDB.getCollection().find(new Document("entities.urls.0", new Document("$exists", true))).into(new ArrayList<>()).size());
        MongoDB.close();
    }

    public static void print_NumberOfTweetWithUserMentions(){
        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERFDL2015);
        log.trace("" + MongoDB.getCollection().find(new Document("entities.user_mentions.0", new Document("$exists", true))).into(new ArrayList<>()).size());
        MongoDB.close();
    }


    public static void print_NumberOfTweetWithHashtag(){
        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERFDL2015);
        log.trace("" + MongoDB.getCollection().find(new Document("entities.hashtags.0", new Document("$exists", true))).into(new ArrayList<>()).size());
        MongoDB.close();
    }

    public static void print_NumberOfTweetWithURIUserMentionsHashtagFeteDesLumieres(){
        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERFDL2015_CLUSTERS);
        ArrayList test = MongoDB.getCollection().find(new Document("cluster", new Document("$eq", 1))).projection(fields(include("id_str"), excludeId())).into(new ArrayList<>());
        MongoDB.close();
        int counterURI = 0;
        int counterUserMentions = 0;
        int counterHashtag = 0;
        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERFDL2015);
        for (Object s : test){
            String id = ((Document)s).getString("id_str");
            Document doc = MongoDB.getCollection().find(new Document("id_str", new Document("$eq", id))).projection(fields(include("entities"), excludeId())).first();
            Document entities = (Document) doc.get("entities");
            if (entities != null) {
                ArrayList url = (ArrayList) entities.get("urls");
                ArrayList user_mentions = (ArrayList) entities.get("user_mentions");
                ArrayList hashtags = (ArrayList) entities.get("hashtags");

                if (url != null && url.size() > 0){
                    counterURI++;
                }
                if (user_mentions != null && user_mentions.size() > 0){
                    counterUserMentions++;
                }
                if (hashtags != null && hashtags.size() > 0){
                    counterHashtag++;
                }
            }
        }
        MongoDB.close();
        log.trace("counterURI " + counterURI);
        log.trace("counterUserMentions " + counterUserMentions);
        log.trace("counterHashtag " + counterHashtag);
    }

    public static void print_NumberOfTweetWithGeolocation(){
        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERFDL2015);
        log.trace("" + MongoDB.getCollection().find(new Document("geo", new Document("$exists", true))).into(new ArrayList<>()).size());
        MongoDB.close();
    }

    public static void print_NumberOfTweetWithGeolocationFeteDesLumieres(){
        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERFDL2015_CLUSTERS);
        ArrayList test = MongoDB.getCollection().find(new Document("cluster", new Document("$eq", 1))).projection(fields(include("id_str"), excludeId())).into(new ArrayList<>());
        MongoDB.close();
        int counterGeolocation = 0;
        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERFDL2015);
        for (Object s : test){
            String id = ((Document)s).getString("id_str");
            Document doc = MongoDB.getCollection().find(new Document("id_str", new Document("$eq", id))).projection(fields(include("geo"), excludeId())).first();
            Document geo = (Document) doc.get("geo");
            if (geo != null) {
                log.trace("" + geo);
                counterGeolocation++;
            }
        }
        MongoDB.close();
        log.trace("counterGeolocation " + counterGeolocation);
    }

    public static void print_MinMaxGeolocationFeteDesLumieres(){
        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERFDL2015_CLUSTERS);
        ArrayList test = MongoDB.getCollection().find(new Document("cluster", new Document("$eq", 1))).projection(fields(include("id_str"), excludeId())).into(new ArrayList<>());
        MongoDB.close();

        double[] latitude = new double[2];
        double[] longitude = new double[2];

        latitude[0] = Double.MAX_VALUE;
        latitude[1] = Double.MIN_VALUE;

        longitude[0] = Double.MAX_VALUE;
        longitude[1] = Double.MIN_VALUE;

        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERFDL2015);
        for (Object s : test){
            String id = ((Document)s).getString("id_str");
            Document doc = MongoDB.getCollection().find(new Document("id_str", new Document("$eq", id))).projection(fields(include("geo"), excludeId())).first();
            Document geo = (Document) doc.get("geo");
            if (geo != null) {

                ArrayList<Double> coordinates = (ArrayList<Double>) geo.get("coordinates");

                double c0 = Double.parseDouble("" + coordinates.get(0));
                double c1 = Double.parseDouble("" + coordinates.get(1));

                if (c0 < latitude[0]) { // MIN
                    latitude[0] = c0;
                }
                else if (c0 > latitude[1]) { // MAX
                    latitude[1] = c0;
                }
                log.trace("" + coordinates);


                if (c1 < longitude[0]) { // MIN
                    longitude[0] = c1;
                }
                else if (c1 > longitude[1]) { // MAX
                    longitude[1] = c1;
                }
            }
        }
        log.trace("latitude [" + latitude[0] + ", " + latitude[1] + "]");
        log.trace("longitude [" + longitude[0]  + ", " +  longitude[1] + "]");
        MongoDB.close();
    }

    public static void print_AllLanguagesFromCorpus () {
        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERFDL2015);
        for (Document document : MongoDB.getCollection().aggregate(asList(
                new Document("$group", new Document("_id", "$lang").append("count", new Document("$sum", 1))), new Document("$sort", new Document("count", -1))))){
            log.trace("[ " + document.getString("_id") + " " +  document.getInteger("count") + " ]");
        }
        MongoDB.close();
    }

    public static Map<String, Integer> print_AllHashtagsAndTheirNumberFromCorpus() {
        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERFDL2015);
        Map<String, Integer> set = new HashMap();

        for (Document document : MongoDB.getCollection().find(new Document("entities.hashtags.0", new Document("$exists", true))).projection(fields(include("entities.hashtags.text", "id_str"), excludeId()))) {
            JSONObject json = new JSONObject(document).getJSONObject("entities");
            JSONArray array = json.getJSONArray("hashtags");

            for (int i = 0; i < array.length(); i++) {
                String hashtag = ((JSONObject) array.get(i)).getString("text");
                hashtag = hashtag.toLowerCase();
                if (set.containsKey(hashtag)) {
                    set.put(hashtag, set.get(hashtag) + 1);
                } else {
                    set.put(hashtag, 1);
                }
            }
        }
        MongoDB.close();
        log.trace("There is : " + set.size() + " element(s) in the set.");

        Map<String, Integer> sortedMap = sortMapByComparator(set);
        printMap(sortedMap);

        return set;
    }

    public static void print_AllLanguagesFor122FeteDesLumieresTweets () {
        Set<String> langs = new HashSet<>();

        HashMap<String, Integer> map = new HashMap<>();

        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERORIANERENAMED);
        ArrayList test = MongoDB.getCollection().find(new Document("cluster_id", new Document("$eq", "13"))).projection(fields(include("id_str"), excludeId())).into(new ArrayList<>());
        MongoDB.close();

        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERFDL2015);
        for (Object s : test){
            String id = ((Document)s).getString("id_str");
            String lang = MongoDB.getCollection().find(new Document("id_str", new Document("$eq", id))).projection(fields(include("lang"), excludeId())).first().getString("lang");
            langs.add(lang);

            if (map.containsKey(lang)) {
                map.put(lang, map.get(lang) + 1);
            }
            else {
                map.put(lang, 1);
            }

        }
        MongoDB.close();


        for (String lang : langs){
            log.trace(lang);
        }

        Toolbox.printMap(map);
    }

    public static void print_min_max_timestamp (){
        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERORIANERENAMED);
        ArrayList test = MongoDB.getCollection().find().projection(fields(include("id_str"), excludeId())).into(new ArrayList<>());
        MongoDB.close();
        log.trace("" + test.size());

        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERFDL2015);
        for (Object s : test){
            String id = ((Document)s).getString("id_str");
            String timestamp_ms_str = MongoDB.getCollection().find(new Document("id_str", new Document("$eq", id))).projection(fields(include("timestamp_ms"), excludeId())).first().getString("timestamp_ms");
            long timestamp_ms = Long.parseLong(timestamp_ms_str);
            if (timestamp_ms < min ) {
                min = timestamp_ms;
            }
            if (timestamp_ms > max) {
                max = timestamp_ms;
            }

        }
        MongoDB.close();

        log.trace("min " + min);
        log.trace("max " + max);
    }

    public static void print_AllLanguagesForFeteDesLumieresTweets () {
        Set<String> langs = new HashSet<>();

        HashMap<String, Integer> map = new HashMap<>();

        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERFDL2015_CLUSTERS);
        ArrayList test = MongoDB.getCollection().find(new Document("cluster", new Document("$eq", 1))).projection(fields(include("id_str"), excludeId())).into(new ArrayList<>());
        MongoDB.close();

        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERFDL2015);
        for (Object s : test){
            String id = ((Document)s).getString("id_str");
            String lang = MongoDB.getCollection().find(new Document("id_str", new Document("$eq", id))).projection(fields(include("lang"), excludeId())).first().getString("lang");
            langs.add(lang);

            if (map.containsKey(lang)) {
                map.put(lang, map.get(lang) + 1);
            }
            else {
                map.put(lang, 1);
            }

        }
        MongoDB.close();


        for (String lang : langs){
            log.trace(lang);
        }

        Toolbox.printMap(map);
    }


    public static void wordsCloud(){
        ArrayList<String> clusters = new ArrayList<>();
        ArrayList<String> filenames = new ArrayList<>();
        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERFDL2015);

        Document min = MongoDB.getCollection().aggregate(asList(
                new Document("$group", new Document("_id", null).append("min_timestamp_ms", new Document("$min", "$timestamp_ms"))))).first();

        Document max = MongoDB.getCollection().aggregate(asList(
                new Document("$group", new Document("_id", null).append("max_timestamp_ms", new Document("$max", "$timestamp_ms"))))).first();

        long min_timestamp = Long.parseLong(min.getString("min_timestamp_ms"));
        long max_timestamp = Long.parseLong(max.getString("max_timestamp_ms"));

        long window = Toolbox.hours2milliseconds(8);
        long lt;
        int counter = 0;
        String textConcatenated;
        for (long gt = min_timestamp; gt < max_timestamp; gt+=window) {
            lt = (gt + window);
            textConcatenated = "";
            for (Document document : MongoDB.getCollection().find(new Document("timestamp_ms", new Document("$gt", "" + gt).append("$lt", "" + lt))).projection(fields(include("text"), excludeId())).into(new ArrayList<>())){
                String text = document.getString("text");
                textConcatenated = textConcatenated.concat(" " + text.replaceAll(ConstantsPreProcessingRegex.REGEX_LINE_BREAK, " "));
            }
            clusters.add(textConcatenated);

            Timestamp timegt = new Timestamp(gt);
            String timegtstr =  timegt.toString().replaceAll(":", "_").replaceAll("\\.", "_");

            Timestamp timelt = new Timestamp(lt);
            String timeltstr =  timelt.toString().replaceAll(":", "_").replaceAll("\\.", "_");

            filenames.add(timegtstr + " - " + timeltstr + ".png");
            log.trace(gt + " - " + lt);

        }
        log.trace("" + clusters.size());
        MongoDB.close();

        clusters = (ArrayList<String>) DataProcessing.cleanBigCorpus(clusters, Arrays.asList(10, 9));
    }

    public static void print_FeteDesLumieresTweets () {
        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERFDL2015_CLUSTERS);
        ArrayList test = MongoDB.getCollection().find(new Document("cluster", new Document("$eq", 1))).projection(fields(include("id_str"), excludeId())).into(new ArrayList<>());
        MongoDB.close();

        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERFDL2015);
        for (Object s : test){
            String id = ((Document)s).getString("id_str");
            Document doc = MongoDB.getCollection().find(new Document("id_str", new Document("$eq", id))).projection(fields(include("text"), excludeId())).first();
            String text = doc.getString("text");
            //if( doc.getString("lang").equals("en")){
            log.trace(text.replaceAll("\\n", " "));
            //}
        }
        MongoDB.close();
    }


    public static void print_NonFeteDesLumieresTweets () {
        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERFDL2015_CLUSTERS);
        ArrayList test = MongoDB.getCollection().find(new Document("cluster", new Document("$eq", 0))).projection(fields(include("id_str"), excludeId())).into(new ArrayList<>());
        MongoDB.close();
        int counter = 0;
        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERFDL2015);
        for (Object s : test){
            String id = ((Document)s).getString("id_str");
            Document doc = MongoDB.getCollection().find(new Document("id_str", new Document("$eq", id))).projection(fields(include("text", "lang"), excludeId())).first();
            String text = doc.getString("text");
            //if( doc.getString("lang").equals("it")){
            log.trace(text.replaceAll("\\n", " "));
            //}
            counter++;
        }
        MongoDB.close();
        log.trace("counter" + counter);
    }

    public static void print_ExampleOfCleansing() {
        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERFDL2015);
        ArrayList<String> corpus = new ArrayList<>();

        for (Document doc : MongoDB.getCollection().find().projection(fields(include("text"), excludeId()))) {
            String text = doc.getString("text");
            corpus.add(text);
        }
        MongoDB.close();

        for (int i = 690; i < 700; i++){
            log.trace(corpus.get(i).replaceAll("\\n", " "));
        }
        corpus = (ArrayList<String>) DataProcessing.cleanBigCorpus(corpus, Arrays.asList(10, 9));
        for (int i = 690; i < 700; i++){
            log.trace(corpus.get(i).replaceAll("\\n", " "));
        }
    }

    public static void print_numberOfTweetWithHashtagsAferEnrichment() {
        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERFDL2015);
        ArrayList<String> corpus = new ArrayList<>();

        for (Document doc : MongoDB.getCollection().find().projection(fields(include("text"), excludeId()))) {
            String text = doc.getString("text");
            corpus.add(text);
        }
        MongoDB.close();
        int before = 0;
        int after = 0;
        for (int i = 0; i < corpus.size(); i++){
            if (corpus.get(i).contains("#")){
                before++;
            }
        }
        corpus = (ArrayList<String>) DataProcessing.cleanBigCorpus(corpus, Arrays.asList(9));

        for (int i = 0; i < corpus.size(); i++){
            if (corpus.get(i).contains("#")){
                after++;
            }
        }
        log.trace("before " + before);
        log.trace("after " + after);
    }

    public static void makeNewDatabase (){
        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERFDL2015_CLUSTERS);
        ArrayList<Document> test = MongoDB.getCollection().find().projection(fields(include("id_str", "cluster"), excludeId())).into(new ArrayList<>());
        MongoDB.close();

        ArrayList<Document> news = new ArrayList<>();
        log.trace("LOADING ...");
        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERFDL2015);
        int counter = 0;
        for (Document s : test) {
            String id = s.getString("id_str");
            Integer cluster = s.getInteger("cluster");
            Document doc = MongoDB.getCollection().find(new Document("id_str", new Document("$eq", id))).projection(fields(include(), excludeId())).first();
            doc.append("cluster", cluster);
            news.add(doc);
            counter++;
            if (counter % 1000 == 0) {
                log.trace(" ..." + counter);
            }
        }
        MongoDB.close();
        log.trace("SAVING ...");

        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERFDL2015CLUSTERED);
        MongoDB.getCollection().insertMany(news);
        MongoDB.close();
    }

    public static void print_FDLDataByTimeSlot () {
        MongoDB.connection(ConstantsGlobal.DBNAME, ConstantsGlobal.DBCOLLECTIONTWITTERFDL2015CLUSTERED);
        long min_timestamp = 1449594011046l;
        long max_timestamp = 1449622811046l;

        long window = Toolbox.hours2milliseconds(1) / 2;
        int counterFDL;
        int counterNoFDL;
        long lt;
        log.trace("min_timestamp " + min_timestamp);
        int counter = 1;
        for (long gt = min_timestamp; gt < max_timestamp; gt+=window) {
            counterFDL = 0;
            counterNoFDL = 0;

            lt = (gt + window);
            ArrayList<Document> tmp = MongoDB.getCollection().find(new Document("timestamp_ms", new Document("$gt", ""+gt).append("$lt", ""+lt))).into(new ArrayList<>());

            for (Document d : tmp){
                if (d.getInteger("cluster") == 1) {
                    counterFDL++;
                }
                else if (d.getInteger("cluster") == 0) {
                    counterNoFDL++;
                }
            }
            int length = new Timestamp(gt - 3600000).toString().length() - 7;
            String date1 = new Timestamp(gt - 3600000).toString().substring(8, 8 + 2) + "\\_dec\\_" + new Timestamp(gt - 3600000).toString().substring(8 + 3, length);
            String date2 = new Timestamp(lt - 3600000).toString().substring(8, 8 + 2) + "\\_dec\\_" + new Timestamp(lt - 3600000).toString().substring(8 + 3, length);
            System.out.print(counter++ + " ");
            System.out.print(date1 + "\\_-\\_" + date2);

            double percent = ((double)counterFDL / counterNoFDL) * 100;
            DecimalFormat df = new DecimalFormat("########.0");

            log.trace( " " + counterFDL + " " + counterNoFDL + " " + df.format(percent) + "\\% " +  tmp.size());
        }
        MongoDB.close();
    }

    public static void main (String[] args) {
        print_HandMadeClusters();
        print_AllHashtagsAndTheirNumberFromCorpus();
        print_elementsPerHours(6);
        print_NumberOfTweetWithHashtag();

        print_NumberOfTweetWithGeolocation();

        print_NumberOfTweetWithURI();
        print_NumberOfTweetWithUserMentions();
        print_NumberOfTweetWithMedia();
        print_NumberOfTweetWithPhoto();
        print_NumberOfTweetWithVideo();
        print_NumberOfTweetWithAnimated_gif();

        print_AllLanguagesFromCorpus();


        print_AllLanguagesFor122FeteDesLumieresTweets();
        print_min_max_timestamp();

        print_AllLanguagesForFeteDesLumieresTweets();

        print_FeteDesLumieresTweets();
        print_NonFeteDesLumieresTweets();


        print_NumberOfTweetWithMediaFeteDesLumieres();
        print_NumberOfTweetWithPhotoVideoFeteDesLumieres();

        print_NumberOfTweetWithURIUserMentionsHashtagFeteDesLumieres();
        print_NumberOfTweetWithGeolocationFeteDesLumieres();

        print_MinMaxGeolocationFeteDesLumieres();

        print_ExampleOfCleansing();

        print_numberOfTweetWithHashtagsAferEnrichment();

        print_MinMaxGeolocationFeteDesLumieres();
        print_FDLDataByTimeSlot();
    }
}

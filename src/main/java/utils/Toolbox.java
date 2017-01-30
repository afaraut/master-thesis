package utils;

import com.google.gson.*;
import org.json.JSONArray;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Anthony on 11/05/2016.
 */
public class Toolbox {

    /**
     * Indentation of a json Object
     *
     * @param jsonString The string representation of the json
     * @return The string indented
     */
    public static String JSONObjecttoPrettyJSONFormat(String jsonString) {
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(jsonString).getAsJsonObject();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = gson.toJson(json);

        return prettyJson;
    }

    /**
     * Indentation of a json Array
     *
     * @param jsonString The string representation of the json
     * @return The string indented
     */
    public static String JSONArraytoPrettyJSONFormat(String jsonString) {
        JsonParser parser = new JsonParser();
        JsonArray json = parser.parse(jsonString).getAsJsonArray();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = gson.toJson(json);

        return prettyJson;
    }

    /**
     * Save a JSON Array into a file
     *
     * @param filename The name of the json file
     * @param obj      The JSON array
     */
    public static void save_JSONArrayToFile(String filename, JSONArray obj) {
        FileWriter file = null;
        try {
            file = new FileWriter(filename);
            file.write(Toolbox.JSONArraytoPrettyJSONFormat(obj.toString()));
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

    /**
     * Pattern to replace right subtext in a text thanks to Regex
     *
     * @param text         The entire text
     * @param regex        The regex in order to match the subpart
     * @param replace_text The subtext we want to replace
     * @return The modified text
     */
    public static String replaceByRightPattern(String text, String regex, String replace_text) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            text = text.replace(matcher.group(0), matcher.group(1) + replace_text);
        }
        return text;
    }

    /**
     * Pattern to replace left subtext in a text thanks to Regex
     *
     * @param text         The entire text
     * @param regex        The regex in order to match the subpart
     * @param replace_text The subtext we want to replace
     * @return The modified text
     */
    public static String replaceByLeftPattern(String text, String regex, String replace_text) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            text = text.replace(matcher.group(0), replace_text + matcher.group(2) + " ");
        }
        return text;
    }

    public static Boolean regexMatch(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return true;
        }
        return false;
    }

    /**
     * Serialization of a java object into a file
     *
     * @param obj      The java object to serialized
     * @param filename The name of the file
     * @throws IOException
     */
    public static void serialization(Object obj, String filename) throws IOException {
        FileOutputStream fos = new FileOutputStream(filename);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        try {
            oos.writeObject(obj);
            oos.flush();
        } finally {
            try {
                oos.close();
            } finally {
                fos.close();
            }
        }
    }

    /**
     * Deserialization of a java object into a file
     *
     * @param filename The name of the file
     * @return The java object deserialized
     * @throws IOException
     */
    public static Object deserialization(String filename) throws IOException {
        FileInputStream fis = new FileInputStream(filename);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Object obj = null;
        try {
            obj = ois.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                ois.close();
            } finally {
                fis.close();
            }
        }
        return obj;
    }

    /**
     * Get the min / max from an array of int
     *
     * @param array The array of int
     * @return The min max value in an array allowing the multiple return
     * array[0] contains the min value
     * array[1] contains the max value
     */
    public static int[] min_max_array(int[] array) {
        IntSummaryStatistics stat = Arrays.stream(array).summaryStatistics();
        return new int[]{stat.getMin(), stat.getMax()};
    }

    /**
     * Write file in a specific directory
     * If the directory doesn't exist, it will be create
     *
     * @param directory The directory in which the file have to be saved
     * @param filename  The file name
     * @param content   The content of the file
     */
    public static void write_file(String directory, String filename, String content, StandardOpenOption option) {
        try {
            File filed = new File(directory);
            if (!filed.exists()) {
                Files.createDirectories(Paths.get(directory));
            }
            Files.write(Paths.get(directory + filename), content.getBytes(), option);
        } catch (IOException e) {
            //e.printStackTrace();
            try {
                Files.write(Paths.get(directory + filename), content.getBytes(), StandardOpenOption.CREATE);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * Intersection between two list of specific objects
     *
     * @param list1 The first list of ClusterElements
     * @param list2 The second list of ClusterElements
     * @return A list of intersect elements from the both lists passed in parameter
     */
    public static ArrayList<ClusterElement> intersect(List<ClusterElement> list1, List<ClusterElement> list2) {
        ArrayList<ClusterElement> intersectionElements = new ArrayList<>();
        for (ClusterElement ce1 : list1) {
            for (ClusterElement ce2 : list2) {
                if (ce1.get_idTwitter().equals(ce2.get_idTwitter())) {
                    intersectionElements.add(ce1);
                }
            }
        }
        return intersectionElements;
    }

    /**
     * Union between two list of specific objects
     *
     * @param list1 The first list of ClusterElements
     * @param list2 The second list of ClusterElements
     * @return A list of union elements from the both lists passed in parameter
     */
    public static ArrayList<ClusterElement> union(List<ClusterElement> list1, List<ClusterElement> list2) {
        ArrayList<ClusterElement> unionElements = new ArrayList<>();
        for (ClusterElement ce1 : list1) {
            Boolean val = false;
            for (ClusterElement ce3 : unionElements) {
                if (ce3.get_idTwitter().equals(ce1.get_idTwitter())) {
                    val = true;
                    break;
                }
            }
            if (!val) {
                unionElements.add(ce1);
            }
        }
        for (ClusterElement ce2 : list2) {
            Boolean val = false;
            for (ClusterElement ce3 : unionElements) {
                if (ce3.get_idTwitter().equals(ce2.get_idTwitter())) {
                    val = true;
                    break;
                }
            }
            if (!val) {
                unionElements.add(ce2);
            }
        }
        return unionElements;
    }

    public static <K, V> Map<K, V> sortByValue(Map<K, V> map, int order) {
        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Object>() {
            public int compare(Object o1, Object o2) {
                if (order == 1){
                    return ((Comparable<V>) ((Map.Entry<K, V>) (o1)).getValue()).compareTo(((Map.Entry<K, V>) (o2)).getValue());
                }
                return ((Comparable<V>) ((Map.Entry<K, V>) (o2)).getValue()).compareTo(((Map.Entry<K, V>) (o1)).getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<>();
        for (Iterator<Map.Entry<K, V>> it = list.iterator(); it.hasNext();) {
            Map.Entry<K, V> entry = (Map.Entry<K, V>) it.next();
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public static void displayMatrix(Double[][] matrix) {
        int x = matrix.length;
        int y = matrix[0].length;
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                System.out.print(matrix[i][j] + " | ");
            }
            System.out.println();
        }
    }

    /**
     * Compute the log Base X
     *
     * @param x    The number we want to compute the log
     * @param base The base
     * @return The log base X of the number
     */
    public static double log(double x, int base) {
        return (Math.log(x) / Math.log(base));
    }

    public static void printMap(Map<String, Integer> map) {
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            System.out.println("[ " + entry.getKey() + " : " + entry.getValue() + " ]");
        }
    }

    public static Map<String, Integer> sortMapByComparator(Map<String, Integer> unsortMap) {

        List<Map.Entry<String, Integer>> list =  new LinkedList<>(unsortMap.entrySet());
        Collections.sort(list, (o1, o2) -> (o1.getValue()).compareTo(o2.getValue()));

        // Convert sorted map back to a Map
        Map<String, Integer> sortedMap = new LinkedHashMap<>();
        for (Iterator<Map.Entry<String, Integer>> it = list.iterator(); it.hasNext(); ) {
            Map.Entry<String, Integer> entry = it.next();
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    public static void deleteSerialFiles(String emplacement) {
        File path = new File(emplacement);
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].toString().endsWith(".serial")){
                    files[i].delete();
                    System.out.println("DELETE " + files[i]);
                }
            }
        }
    }

    public static long hours2milliseconds(Integer hours){
        return hours * 60 * 60 * 1000;
    }

    /**
     * Convert Bytes number to human readable number
     *
     * @param bytes The bytes number
     * @param si    1000 or 1024
     * @return Formated version of the bytes number
     */
    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}

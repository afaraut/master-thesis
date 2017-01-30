package constants;

/**
 * Created by Anthony on 12/04/2016.
 */
public class ConstantsPreProcessingRegex {
    public static final String REGEX_LINE_BREAK = "\n";
    public static final String REGEX_USERNAME = "(@[a-zA-Z])\\w+";
    public static final String REGEX_URI = "\\(?\\b(http://|https://|www[.])[-A-Za-z0-9+&amp;@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&amp;@#/%=~_()|]";

    public static final String REGEX_FOLLOWING_POINTS = "([\\.]+[\\.]+[\\.])+";
    public static final String REGEX_FOLLOWING_INTERROGATION_POINTS = "([\\?]+[\\?])+";
    public static final String REGEX_FOLLOWING_EXCLAMATION_POINTS = "([!]+[!])+";
    public static final String REGEX_FOLLOWING_SPACES = "\\s+\\s+";

    public static final String REGEX_POINT = "([a-zA-ZÀ-ÿ1-9]+)([ ]*)([\\.]+)";
    public static final String REGEX_COMMA = "([a-zA-ZÀ-ÿ1-9]+)([ ]*)([,]+)";
    public static final String REGEX_INTERROGATION_POINT = "([a-zA-ZÀ-ÿ1-9]+)([ ]*)([?]+)";
    public static final String REGEX_EXCLAMATION_POINT = "([a-zA-ZÀ-ÿ1-9]+)([ ]*)([!]+)";

    public static final String REGEX_LEFT_PARENTHESIS = "([\\(]+)([a-zA-ZÀ-ÿ1-9]+)([ ]*)";
    public static final String REGEX_RIGHT_PARENTHESIS = "([a-zA-ZÀ-ÿ1-9]+)([ ]*)([\\)]+)";

    public static final String REGEX_LEFT_BRACKET = "([\\[]+)([a-zA-ZÀ-ÿ1-9]+)([ ]*)";
    public static final String REGEX_RIGHT_BRACKET = "([a-zA-ZÀ-ÿ1-9]+)([ ]*)([\\]]+)";

    public static final String REGEX_LEFT_QUOTE = "([\\\"]+)([a-zA-ZÀ-ÿ1-9]+)([ ]*)";
    public static final String REGEX_RIGHT_QUOTE = "([a-zA-ZÀ-ÿ1-9]+)([ ]*)([\\\"]+)";

    public static final String REGEX_ALPHANUMERIC_CHARACTER = "([a-zA-ZÀ-ÿ1-9]+)";
}

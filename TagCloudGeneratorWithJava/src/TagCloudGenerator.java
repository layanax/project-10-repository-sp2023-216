import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * TagCloudGenerator generates an HTML file displaying a tag cloud from an input
 * text file.
 *
 * @author Layan Abdallah & Oak Hodous
 */
public final class TagCloudGenerator {

    /**
     * No argument constructor--private to prevent instantiation.
     */
    private TagCloudGenerator() {
    }

    /**
     * Generates the header of the HTML file.
     *
     * @param out
     *            output stream
     * @param n
     *            user-defined value for the number of words to include in tag
     *            cloud
     * @param inName
     *            the name of the inputed file
     * @requires <pre> out.is.open </pre>
     * @ensures <pre> out.is.open and output.content = #out.content *
     * [tag cloud headers] </pre>
     */
    private static void indexHeaders(PrintWriter out, String inName, int n) {
        out.println("<!DOCTYPE html>\n<html>\n<head>");
        out.println("\t<title>Tag Cloud</title>");

        //link to CSS file
        out.println(
                "\t<link href=\"http://web.cse.ohio-state.edu/software/2231/web-"
                        + "sw2/assignments/projects/tag-cloud-generator/data/"
                        + "tagcloud.css\" rel=\"stylesheet\" type=\"text/css\">");
        out.println(
                "\t<link href=\"tagcloud.css\" type=\"text/css\" rel=\"stylesheet\">");

        //additional CSS style requirements for tag cloud
        out.println("\t<style>");
        out.println(
                ".rdp {--rdp-cell-size: 40px;--rdp-accent-color: #0000ff;--rdp-"
                        + "background-color: #e7edff;--rdp-accent-color-dark: "
                        + "#3003e1;--rdp-background-color-dark: #180270;--rdp-outline: "
                        + "2px solid var(--rdp-accent-color);--rdp-outline-selected: "
                        + "2px solid rgba(0, 0, 0, 0.75);margin: 1em;}");
        out.println(".rdp-vhidden {box-sizing: border-box;padding: 0;margin: 0;"
                + "background: transparent;border: 0;-moz-appearance: none;-webkit"
                + "-appearance: none;appearance: none;position: absolute !important;"
                + "top: 0;width: 1px !important;height: 1px !important;padding: 0 "
                + "!important;overflow: hidden !important;clip: rect(1px, 1px, 1px, 1px)"
                + " !important;border: 0 !important;}");
        out.println("</style>");

        out.println("</head>\n<body>");
        out.println(
                "\t<h2>Top " + n + " words in " + inName + " </h2>\n\t<hr>\n");

    }

    /**
     * Reads input and returns a map of repeated words and their counts.
     *
     * @param in
     *            input stream
     * @return a map containing words and their counts
     */
    private static Map<String, Integer> repeatedWords(BufferedReader in) {
        Map<String, Integer> map = new HashMap<>();

        try {
            String line = in.readLine();
            while (line != null) {
                String[] words = line.split("[ \t\n\r,-.!?\\[\\]';:/()]+");
                for (String word : words) {
                    if (!word.isEmpty()) {
                        String lowerCaseWord = word.toLowerCase();
                        //update map with word count and increment if already present
                        //otherwise, just add count of 1
                        if (map.containsKey(lowerCaseWord)) {
                            map.replace(lowerCaseWord,
                                    map.get(lowerCaseWord) + 1);
                        } else {
                            map.put(lowerCaseWord, 1);
                        }
                    }
                }
                line = in.readLine();
            }
        } catch (IOException error) {
            System.err.println(
                    "Error: could not read input: " + error.getMessage());
        }
        return map;

    }

    /**
     * Outputs an HTML tag cloud with words and their counts.
     *
     * @param map
     *            a map containing words and their counts
     * @param out
     *            output stream
     * @param n
     *            user-defined value for the number of words to include in tag
     *            cloud
     * @requires <pre> out.is.open </pre>
     * @ensures <pre> out.content = #out.content * [print out words in tag cloud
     * format] </pre>
     */
    private static void tagCloud(Map<String, Integer> map, PrintWriter out,
            int n) {
        out.println("<div class=\"cdiv\">");
        out.println("<p class = \"cbox\">");

        //calculate font sizes for tag cloud
        int maxCount = Integer.MIN_VALUE;
        int minCount = Integer.MAX_VALUE;
        Set<String> mapSeq = map.keySet();
        for (String i : mapSeq) {
            int count = map.get(i);
            maxCount = Math.max(maxCount, count);
            minCount = Math.min(minCount, count);
        }

        //sorted queue of map pairs based on counts
        Map<String, Integer> sortedWords = createSortedMap(map, n);

        for (Map.Entry<String, Integer> temp : sortedWords.entrySet()) {

            String word = temp.getKey();
            int count = temp.getValue();

            //calculates font size based on count
            int fontSize = calculateFontSize(count, minCount, maxCount);

            //outputs word with correct font size
            out.println("<span style=\"cursor:default\" class = \" f" + fontSize
                    + "\" title = \"count: " + temp.getValue() + "\">" + word
                    + "</span>");
        }
        out.println("</p>");
        out.println("</div>");
    }

    /**
     * Calculates the font size for a word in the tag cloud.
     *
     * @param count
     *            the count of occurrences of the word
     * @param minCount
     *            the minimum count among all words
     * @param maxCount
     *            the maximum count among all words
     * @return the font size for the word
     */
    private static int calculateFontSize(int count, int minCount,
            int maxCount) {

        final int minFontSize = 11;
        final int maxFontSize = 48;

        //compare min and max counts to calculate relative size of word
        int fontSize = maxFontSize - minFontSize;
        if (maxCount > minCount) {
            fontSize = fontSize * (count - minCount);
            fontSize = fontSize / (maxCount - minCount);
            fontSize = fontSize + minFontSize;
        } else {
            fontSize = maxFontSize;
        }
        return fontSize;
    }

    /**
     * Creates and returns a sorted queue of words from the given map.
     *
     * @param n
     *            user-defined value for the number of words to include in tag
     *            cloud
     * @param map
     *            the map containing words and their counts
     * @requires map is not modified during the execution of this
     * @return a queue of words sorted into alphabetical order
     */
    private static Map<String, Integer> createSortedMap(
            Map<String, Integer> map, int n) {

        Map<String, Integer> sortedMap = null;
        try {
            Comparator<Map.Entry<String, Integer>> countOrder = new CountComparator();

            List<Map.Entry<String, Integer>> l1 = new LinkedList<Map.Entry<String, Integer>>(
                    map.entrySet());
            Collections.sort(l1, countOrder);

            List<Map.Entry<String, Integer>> list2 = l1.subList(0, n);

            Comparator<Entry<String, Integer>> alphabeticalOrder = new WordComparator();
            Collections.sort(list2, alphabeticalOrder);

            sortedMap = new LinkedHashMap<String, Integer>();

            for (int i = 0; i < list2.size(); i++) {
                sortedMap.put(list2.get(i).getKey(), list2.get(i).getValue());
            }
        } catch (Exception e) {
            System.err.println("error: file did not have enough words");

        }

        return sortedMap;

    }

    /**
     * A comparator for sorting words by their counts in descending order.
     */
    private static class CountComparator
            implements Comparator<Map.Entry<String, Integer>> {
        @Override
        public int compare(Map.Entry<String, Integer> pair1,
                Map.Entry<String, Integer> pair2) {

            //sorts by count in descending order
            int result = pair2.getValue().compareTo(pair1.getValue());

            if (result == 0) {
                result = pair1.getKey().compareTo(pair2.getKey());
            }

            return result;
        }
    }

    /**
     * A comparator for sorting words alphabetically.
     */
    private static class WordComparator
            implements Comparator<Map.Entry<String, Integer>> {
        @Override
        public int compare(Map.Entry<String, Integer> pair1,
                Map.Entry<String, Integer> pair2) {

            //sorts alphabetically
            int result = pair1.getKey().compareTo(pair2.getKey());

            if (result == 0) {
                result = pair1.getValue().compareTo(pair2.getValue());
            }

            return result;
        }
    }

    /**
     * Main method that reads input and generates the tag cloud HTML file.
     *
     * @param args
     */
    public static void main(String[] args) {
        BufferedReader in = null;
        PrintWriter out = null;
        BufferedReader input = new BufferedReader(
                new InputStreamReader(System.in));

        String inputFile = "";
        String outputFile = "";
        int n = 0;
        //prompts user for input and output file names
        try {
            System.out.print("Enter the name of an input file: ");
            inputFile = input.readLine();
            in = new BufferedReader(new FileReader(inputFile));
        } catch (IOException error) {
            System.out.println("Error: invalid input file: " + error);
        }

        try {
            System.out.print("Enter the name of the output HTML file: ");
            outputFile = input.readLine();
            out = new PrintWriter(
                    new BufferedWriter(new FileWriter(outputFile)));
        } catch (IOException error) {
            System.out.println("Error: invalid output file");
        }

        try {
            System.out.print(
                    "Enter the number of words to include in the Tag Cloud: ");
            n = Integer.parseInt(input.readLine());
        } catch (IOException error) {
            System.out.println("Error: could not read inputed number");
        }

        if (n < 0) {
            System.out.println(
                    "Error: invalid input (number of words must be >= 0)");
        } else {
            //generates HTML headers for output file
            indexHeaders(out, inputFile, n);

            //processes input file, counts words, and generates tag cloud
            Map<String, Integer> map = repeatedWords(in);
            tagCloud(map, out, n);

            try {
                in.close();
                out.close();
            } catch (IOException error) {
                System.err.println("Error: could not close files");

            }

            System.out.println("Tag Cloud created!");

        }

    }
}

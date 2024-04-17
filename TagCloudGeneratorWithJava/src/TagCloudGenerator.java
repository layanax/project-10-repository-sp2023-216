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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import components.utilities.Reporter;

//PROBLEMS AND QUESTIONS
// is it ok to not use a sorting machine anymore and instead use collections.sort?
// figure out how to not use reporter becuase that is part of components

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

        //sorted queue of map pairs based on counts

        //calculate font sizes for tag cloud
        int maxCount = Integer.MIN_VALUE;
        int minCount = Integer.MAX_VALUE;
        Set<String> MapSeq = map.keySet();
        for (String i : MapSeq) {

            int count = map.get(i);
            maxCount = Math.max(maxCount, count);
            minCount = Math.min(minCount, count);
        }

        Queue<Map.Entry<String, Integer>> sortedWords = createSortedQueue(map,
                n);

        while (sortedWords.size() > 0) {
            Map.Entry<String, Integer> entry = sortedWords.remove();
            String word = entry.getKey();
            int count = entry.getValue();

            //calculates font size based on count
            int fontSize = calculateFontSize(count, minCount, maxCount);

            //outputs word with correct font size
            out.println("<span style=\"cursor:default\" class = \" f" + fontSize
                    + "\" title = \"count: " + entry.getValue() + "\">" + word
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
        double relativeSize = ((double) count - minCount)
                / (maxCount - minCount);

        //calculate font size
        int font = (int) Math.ceil(
                relativeSize + relativeSize * (maxFontSize - minFontSize));

        return font;
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
    private static Queue<Map.Entry<String, Integer>> createSortedQueue(
            Map<String, Integer> map, int n) {
        Comparator<Entry<String, Integer>> countOrder = new CountComparator();

        List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(
                map.entrySet());
        Collections.sort(list, countOrder);

        List<Map.Entry<String, Integer>> list2 = list.subList(0, n);

        Comparator<Entry<String, Integer>> alphabeticalOrder = new WordComparator();
        Collections.sort(list2, alphabeticalOrder);

        Queue<Map.Entry<String, Integer>> queue = new LinkedList<Map.Entry<String, Integer>>();

        for (int i = 0; i < list2.size(); i++) {
            queue.add(list2.get(i));
        }

        return queue;

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
            return pair2.getValue() - pair1.getValue();
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
            return pair1.getKey().compareTo(pair2.getKey());
        }
    }

    /**
     * Main method that reads input and generates the tag cloud HTML file.
     *
     * @param args
     */
    public static void main(String[] args) {
        BufferedReader in;
        PrintWriter out;
        BufferedReader input = new BufferedReader(
                new InputStreamReader(System.in));

        //prompts user for input and output file names
        try {
            System.out.print("Enter the name of an input file: ");
            String inputFile = input.readLine();
            in = new BufferedReader(new FileReader(inputFile));
            Reporter.assertElseFatalError(in.ready(), "invalid input file");

            System.out.print("Enter the name of the output HTML file: ");
            String outputFile = input.readLine();
            out = new PrintWriter(
                    new BufferedWriter(new FileWriter(outputFile)));

            System.out.print(
                    "Enter the number of words to include in the Tag Cloud: ");
            int n = Integer.parseInt(input.readLine());

            Reporter.assertElseFatalError(n > 0,
                    "Number of words must be positive (n > 0).");

            //generates HTML headers for output file
            indexHeaders(out, inputFile, n);

            //processes input file, counts words, and generates tag cloud
            Map<String, Integer> map = repeatedWords(in);
            tagCloud(map, out, n);

            in.close();
            out.close();

        } catch (IOException error) {
            System.err.println("ERROR");
        }

        System.out.println("finished!");

    }
}

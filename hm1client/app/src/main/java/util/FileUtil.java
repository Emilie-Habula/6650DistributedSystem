package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import model.AnalysisResult;

/**
 * The FileUtil class is a utility class for file-related operations, including writing to
 * and analyzing data from a CSV file.
 * It has a constant CSV_FILE_PATH representing the path to the CSV file.
 * The constructor initializes a BufferedWriter for writing and a BufferedReader for
 * reading from the CSV file.
 * The write method writes a value to the CSV file in a synchronized manner to handle concurrency.
 * The analysis method reads data from the CSV file, calculates average, median, and p99 times,
 * and returns an AnalysisResult object with these values.
 * The latencies are stored in a list (allLatencies) for further analysis.
 * Error handling is implemented for IOExceptions, including printing stack traces for debugging.
 */


// Utility class for file-related operations, including writing to and analyzing data from a CSV file
public class FileUtil {
    // Path to the CSV file
    private static final String CSV_FILE_PATH = "output.csv";

    // BufferedWriter for writing to the CSV file
    private final BufferedWriter bufferedWriter;

    // BufferedReader for reading from the CSV file
    private final BufferedReader bufferedReader;

    // Constructor that initializes BufferedWriter and BufferedReader
    public FileUtil() throws IOException {
        bufferedWriter = new BufferedWriter(new java.io.FileWriter(CSV_FILE_PATH));
        bufferedReader = new BufferedReader(new java.io.FileReader(CSV_FILE_PATH));
    }

    // Synchronized method for writing a value to the CSV file for handling concurrency.
    public synchronized boolean write(String value) {
        try {
            // Write the value to the CSV file and move to the next line
            bufferedWriter.write(value);
            bufferedWriter.write("\n");
        } catch (IOException e) {
            // Print the stack trace and return false in case of an IOException
            e.printStackTrace();
            return false;
        }
        // Return true to indicate successful writing
        return true;
    }

    // Method for analyzing data from the CSV file and calculating average, median, and p99 times
    public AnalysisResult analysis(int totalRequests) {
        // Initialize total latency to 0 and create a list to store all latencies
        BigInteger totalLatency = new BigInteger("0");
        List<Integer> allLatencies = new ArrayList<>();
        try {
            // Read the CSV file line by line
            String line = bufferedReader.readLine();
            while(line!= null) {
                // Split the line into values using a comma as a delimiter
                String[] vals = line.split(",");
                if(vals.length == 4) {
                    // Parse the latency value and add it to the list and total latency
                    int latency = Integer.parseInt(vals[2]);
                    allLatencies.add(latency);
                    totalLatency = totalLatency.add(new BigInteger(vals[2]));
                }
                line = bufferedReader.readLine(); // Read the next line
            }
        } catch (IOException e) {
            e.printStackTrace();// Print the stack trace and return null in case of an IOException
            return null;
        }
        // Create an AnalysisResult object to store the calculated values
        AnalysisResult analysisResult = new AnalysisResult();

        // Check if there are any latencies recorded
        if(allLatencies.size() == 0) {
            return analysisResult;
        }
        // Calculate and set the average time (latency)
        BigInteger avgLatency = totalLatency.divide(new BigInteger(String.valueOf(totalRequests)));
        analysisResult.setAvgTime(avgLatency.intValue());

        // Sort the list of latencies in ascending order
        allLatencies.sort((a, b) -> a-b);

        // Calculate and set the median time
        analysisResult.setMedianTime(allLatencies.get(totalRequests/2));

        // Calculate and set the P99 time (99th percentile)
        analysisResult.setP99Time(allLatencies.get((int)(0.99*totalRequests)));

        // Return the AnalysisResult object with calculated values
        return analysisResult;
    }
}

package model;

/**
 * This class represents an AnalysisResult with three metrics:
 * average time, median time, and p99 time.
 */
public class AnalysisResult {
    // Private member variables to store average time, median time, and p99 time
    private int avgTime;
    private int medianTime;
    private int p99Time;

    // Getter method for retrieving the average time
    public int getAvgTime() {
        return avgTime;
    }

    // Setter method for setting the average time
    public void setAvgTime(int avgTime) {
        this.avgTime = avgTime;
    }

    // Getter method for retrieving the median time
    public int getMedianTime() {
        return medianTime;
    }

    // Setter method for setting the median time
    public void setMedianTime(int medianTime) {
        this.medianTime = medianTime;
    }

    // Getter method for retrieving the p99 time
    public int getP99Time() {
        return p99Time;
    }

    // Setter method for setting the p99 time
    public void setP99Time(int p99Time) {
        this.p99Time = p99Time;
    }
}

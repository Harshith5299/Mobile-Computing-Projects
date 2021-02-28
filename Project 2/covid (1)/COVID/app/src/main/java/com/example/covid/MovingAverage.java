package com.example.covid;

import java.util.LinkedList;
import java.util.Queue;

public class MovingAverage {
    Queue<Double> window = new LinkedList<>();
    private final int period;
    private double sum;
    private double currentAverage;
    private int peakCount = 0;
    private boolean increasing = false;

    public MovingAverage(int period) {
        if (BuildConfig.DEBUG && period <= 0) {
            throw new AssertionError("Period should be a positive integer!");
        }
        this.period = period;
    }

    public void addData(double value) {
        sum += value;
        double previousAverage = currentAverage;

        window.add(value);

        if (window.size() > period) {
            sum -= window.remove();
        }

        currentAverage = getAverage();

        if(currentAverage > previousAverage){
            if(!increasing){
                peakCount++;
            }
            increasing = true;
        }
        else if(currentAverage < previousAverage){
            if(increasing){
                peakCount++;
            }
            increasing = false;
        }
    }

    public double getAverage() {
        if (window.isEmpty()) {
            return 0;
        }
        return sum / window.size();
    }

    public int getPeakCount() {
        return peakCount;
    }
}

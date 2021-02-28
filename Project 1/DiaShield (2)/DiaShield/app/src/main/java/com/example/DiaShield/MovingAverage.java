package com.example.DiaShield;

import java.util.LinkedList;
import java.util.Queue;

public class MovingAverage {
    Queue<Double> window = new LinkedList<>();
    private final int space;

    private double Average;
    private int NumPeaks = 0;

    private boolean increasing = false;
    private double sum;

    public MovingAverage(int space) {
        if (BuildConfig.DEBUG && space <= 0) {
            throw new AssertionError("Period should be a positive integer!");
        }
        this.space = space;
    }
    public double getAverage() {
        if (window.isEmpty()) {
            return 0;
        }
        return sum / window.size();
    }

    public void addData(double value) {
        sum += value;
        double previousAverage = Average;

        window.add(value);
        if (window.size() > space) {
            sum -= window.remove();
        }

        Average = getAverage();

        if(Average > previousAverage){
            if(!increasing){
                NumPeaks++;
            }
            increasing = true;
        }
        else if(Average < previousAverage){
            if(increasing){
                NumPeaks++;
            }
            increasing = false;
        }
    }



    public int getNumPeaks() {
        return NumPeaks;
    }
}

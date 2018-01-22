package com.scorpion.optimize;

/**
 * Created by Scorpion on 2017/3/6.
 */
public class AvaResult {
    private double QoS;
    private int m;
    private int[] tour;

    public double getQoS() {
        return QoS;
    }

    public void setQoS(double qoS) {
        QoS = qoS;
    }

    public int getM() {
        return m;
    }

    public void setM(int m) {
        this.m = m;
    }

    public int[] getTour() {
        return tour;
    }

    public void setTour(int[] tour) {
        this.tour = tour;
    }
}

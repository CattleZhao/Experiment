package com.scorpion.optimize;

/**
 * Created by Scorpion on 2017/3/6.
 */
public class BestTour {
    private double QoS;
    private int m;

    public BestTour() {
    }
    public BestTour(double qoS, int m) {
        QoS = qoS;
        this.m = m;
    }

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
}

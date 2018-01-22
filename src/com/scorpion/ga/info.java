package com.scorpion.ga;

/**
 * Created by Scorpion on 2017/3/11.
 */
public class info {
    private double bestqs;
    private int[] bestt = new int[14];
    
    public double getBestqs() {
        return bestqs;
    }
    
    public void setBestqs(double bestqs) {
        this.bestqs = bestqs;
    }
    
    public int[] getBestt() {
        return bestt;
    }
    
    public void setBestt(int[] bestt) {
        this.bestt = bestt;
    }
}

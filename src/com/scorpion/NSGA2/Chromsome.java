package com.scorpion.NSGA2;

import java.util.ArrayList;

/**
 * 基因类
 */
public class Chromsome {
    public int[] binChrom;//二进制态数组
    public double[] fitness;//适应度值数组，多个目标函数
    public int numDominated;//支配当前个体的个体数
    public int paretoRank;//个体的Pareto等级
    public double crowdingDistance;//当前个体的拥挤距离
    public ArrayList<Chromsome> dominatingList;//当前个体支配的个体的集合
    public int numSplit;//当前个体对应的数据分割方式

    public Chromsome() {
        fitness = null;
        binChrom = null;
        numDominated = 0;
        paretoRank = 0;
        crowdingDistance = 0.0;
        dominatingList = new ArrayList<Chromsome>();
        numSplit = 0;
    }

    //构造函数，参数为染色体长度和目标函数的数目(背包问题的话就是背包的数量)
    public Chromsome(int length, int numObjective, int numSplit) {
        fitness = new double[numObjective];
        binChrom = new int[length];
        numDominated = 0;
        crowdingDistance = 0.0;
        dominatingList = new ArrayList<Chromsome>();
        paretoRank = 0;
        this.numSplit = numSplit;
    }

    public int[] getBinChrom() {
        return binChrom;
    }

    public void setBinChrom(int[] binChrom) {
        this.binChrom = binChrom;
    }

    public double[] getFitness() {
        return fitness;
    }

    public void setFitness(double[] fitness) {
        this.fitness = fitness;
    }

    public int getNumDominated() {
        return numDominated;
    }

    public void setNumDominated(int numDominated) {
        this.numDominated = numDominated;
    }

    public int getParetoRank() {
        return paretoRank;
    }

    public void setParetoRank(int paretoRank) {
        this.paretoRank = paretoRank;
    }

    public double getCrowdingDistance() {
        return crowdingDistance;
    }

    public void setCrowdingDistance(double crowdingDistance) {
        this.crowdingDistance = crowdingDistance;
    }

    public ArrayList<Chromsome> getDominatingList() {
        return dominatingList;
    }

    public void setDominatingList(ArrayList<Chromsome> dominatingList) {
        this.dominatingList = dominatingList;
    }

    public int getNumSplit() {
        return numSplit;
    }

    public void setNumSplit(int numSplit) {
        this.numSplit = numSplit;
    }
}

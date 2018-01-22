package com.scorpion.optimize;

import Test.Service;

import java.util.ArrayList;

/**
 * Created by Scorpion on 2017/3/6.
 */
public class ACO {
    private Ant[] ants;
    private int antNum;
    private int serviceNum;
    private int MAX_GEN;
    private double[] pheromone;
    private double[] distance;
    private double bestQos;
    private int m;
    private int[] bestService;
    private ArrayList<Service> serviceList;

    private double alpha;
    private double beta;
    private double rho;
    public int length;

    public ACO(int n, int m, int g, double a, double b, double r, double[] distance, ArrayList<Service> list) {
        serviceNum = n;
        antNum = m;
        ants = new Ant[antNum];
        MAX_GEN = g;
        alpha = a;
        beta = b;
        rho = r;
        serviceList = list;
        this.distance = distance;
        length = serviceList.size();
    }

    /**
     * 初始化ACO算法类
     */
    public void init() {
        pheromone = new double[length];
        for (int i = 0; i < length; i++) {
            pheromone[i] = 0.1;
        }
        bestQos = Double.MIN_VALUE;
        bestService = new int[serviceNum];
        //随机放置蚂蚁
        for (int i = 0; i < antNum; i++) {
            ants[i] = new Ant(serviceNum);
            ants[i].init(distance, alpha, beta, serviceList);
        }
    }

    public AvaResult solve() {
        for (int g = 0; g < MAX_GEN; g++) {
            for (int i = 0; i < antNum; i++) {
                for (int j = 1; j < serviceNum; j++) {
                    ants[i].selectNextService(pheromone);
                }
                System.out.println(ants[i].getTabu());
                BestTour bestTour = ants[i].calculateQOS();
                if (bestTour.getQoS() > bestQos) {
                    bestQos = bestTour.getQoS();
                    m = bestTour.getM();
                    for (int k = 0; k < serviceNum; k++) {
                        bestService[k] = ants[i].getTabu().get(k).intValue();
                    }
                }
                for (int j = 0; j < serviceNum; j++) {
                    ants[i].getDelta()[ants[i].getTabu().get(j).intValue()] = (double) (1. / ants[i].getQOS());
                }
            }
            //更新信息素
            updatePheromone();
            //重新初始化蚂蚁
            for (int i = 0; i < antNum; i++) {
                ants[i].init(distance, alpha, beta, serviceList);
            }

        }
        AvaResult result = returnOptimal();
        return result;
    }

    private void updatePheromone() {
        //信息素挥发
        for (int i = 0; i < length; i++) {
            pheromone[i] = pheromone[i] * (1 - rho);
        }
        //信息素更新
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < antNum; j++) {
                pheromone[i] += ants[j].getDelta()[i];
            }
        }
    }

    public AvaResult returnOptimal() {
        AvaResult result = new AvaResult();
        System.out.println("The optimal QoS is: " + bestQos);
        System.out.println("The optimal m is: " + m + "   n is: " + serviceNum);
        System.out.println("The best tour is: ");
        for (int i = 0; i < serviceNum; i++) {
            System.out.print(bestService[i] + " ");
        }
        System.out.println("\n");
        result.setQoS(bestQos);
        result.setM(m);
        result.setTour(bestService);
        return result;
    }

    public Ant[] getAnts() {
        return ants;
    }

    public void setAnts(Ant[] ants) {
        this.ants = ants;
    }

    public int getAntNum() {
        return antNum;
    }

    public void setAntNum(int antNum) {
        this.antNum = antNum;
    }

    public int getServiceNum() {
        return serviceNum;
    }

    public void setServiceNum(int serviceNum) {
        this.serviceNum = serviceNum;
    }

    public int getMAX_GEN() {
        return MAX_GEN;
    }

    public void setMAX_GEN(int MAX_GEN) {
        this.MAX_GEN = MAX_GEN;
    }

    public double[] getPheromone() {
        return pheromone;
    }

    public void setPheromone(double[] pheromone) {
        this.pheromone = pheromone;
    }

    public double[] getDistance() {
        return distance;
    }

    public void setDistance(double[] distance) {
        this.distance = distance;
    }

    public double getBestQos() {
        return bestQos;
    }

    public void setBestQos(double bestQos) {
        this.bestQos = bestQos;
    }

    public int[] getBestService() {
        return bestService;
    }

    public void setBestService(int[] bestService) {
        this.bestService = bestService;
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public double getBeta() {
        return beta;
    }

    public void setBeta(double beta) {
        this.beta = beta;
    }

    public double getRho() {
        return rho;
    }

    public void setRho(double rho) {
        this.rho = rho;
    }
}

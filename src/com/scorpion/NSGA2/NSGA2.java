package com.scorpion.NSGA2;

import Test.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

/**
 * 本身就自带精英保留策略
 * NSGA-II的基本思想：首先，随机产生规模为N的初识种群，非支配排序后通过遗传算法的选择、交叉、
 * 变异三个基本操作得到第一代子代种群；其次，从第二代开始，将父代种群与子代种群合并，进行快速非支配排序，
 * 同时对每个非支配层中的个体进行拥挤度计算，根据非支配关系以及个体的拥挤度选取合适的个体组成新的父代种群；
 * 最后，通过遗传算法的基本操作产生新的子代种群；依此类推，直到满足程序结束的条件。
 */
public class NSGA2 {
    final static boolean DEBUG = false;// debug模式

    private int popSize;// 种群规模

    private int maxGen;// 最大进化代数

    private int numObjective;// 优化目标个数

    private int gen;// 当前进化代数

    double pc;// 交叉概率

    double pm;// 变异概率

    private ArrayList<Chromsome> popList;// 当代种群

    private ArrayList<Chromsome> prePopList;// 上一代种群

    /**
     * 跟本问题相关的一些变量
     */
    private ArrayList<Service> serviceList;// 服务列表

    private int numService;// cloud storage service候选的数量(erasure coding中的n)

    private int numSplit;// erasure coding中的m(即data object原始划分的块数)

    private int dataSize;// the size of data object(GB/MB/KB?)

    private double readFrequency;// the frequency of users access data


    /**
     * 一些中间变量
     */
    public static ArrayList<Service> list = new ArrayList<>();// 代表可供选择的云服务

    public static ArrayList<ArrayList<Service>> serviceListList = new ArrayList<ArrayList<Service>>();

    public static ArrayList<ArrayList<Service>> noListList = new ArrayList();

    public static int length;

    /**
     * 写文件
     */
    final static File file = new File("/Users/scorpion/ExpFiles/nsga1.txt");
    static BufferedWriter bufferedWriter;

    static {
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 构造函数
     */
    public NSGA2(int popSize, int numObjective, int maxGen, ArrayList<Service> serviceList,
                 double pc, double pm, int numService, int numSplit, int dataSize,
                 double readFrequency) {
        this.popSize = popSize;
        this.numObjective = numObjective;
        this.maxGen = maxGen;
        this.gen = 0;
        this.pc = pc;
        this.pm = pm;
        this.serviceList = serviceList;
        this.length = serviceList.size();
        this.numService = numService;
        this.numSplit = numSplit;
        this.dataSize = dataSize;
        this.readFrequency = readFrequency;
        this.popList = new ArrayList<Chromsome>();
        for (int i = 0; i < this.popSize; i++) {
            Chromsome chromsome = new Chromsome(numService, numObjective, numSplit);
            popList.add(chromsome);
        }

        this.prePopList = new ArrayList<Chromsome>();
        for (int i = 0; i < this.popSize; i++) {
            Chromsome chromsome = new Chromsome(numService, numObjective, numSplit);
            prePopList.add(chromsome);
        }
    }

    /**
     * 初始化个体中的染色体，随机产生第一代染色体
     * 这种初始化方法能保证一个个体中不会存在相同的云服务时id，对于数据存储这个问题，
     * 每个云存储服务商上至多存储一个数据块(data chunk)
     */
    public void initPopulation() {
        int i, j, k;
        for (k = 0; k < popSize; k++) {//种群数量
            Random random = new Random();
            popList.get(k).binChrom[0] = random.nextInt(65535) % length;
            for (i = 1; i < numService; ) {//染色体长度
                popList.get(k).binChrom[i] = random.nextInt(65535) % length;
                for (j = 0; j < i; j++) {
                    if (popList.get(k).binChrom[i] == popList.get(k).binChrom[j]) {
                        break;
                    }
                }
                if (j == i) {
                    i++;
                }
            }
        }
    }

    /**
     * 计算种群中个体所对应的服务商选择而产生的存储过程中产生的成本和可用性
     *
     * @param binChrom
     * @return
     */
    public double[] calCostAndAva(int[] binChrom) {
        double totalCost = 0.0;
        double availability = 0.0;
        Service[] serArr = new Service[binChrom.length];// 初始化binChrom对应的服务商数组
        ArrayList<Service> serList = new ArrayList<>();// binChrom对应的Service List
        for (int i = 0; i < binChrom.length; i++) {
            //chromsome对应的binChrom云服务商放到serArr、serList中
            Service service = serviceList.get(binChrom[i]);
            serArr[i] = service;
            serList.add(service);
        }


        // 可用云服务商中选择m个对应的所有情况，_selectServiceMap即剩下的
        Map<Integer, ArrayList<ArrayList<Service>>> selectServiceMap = new HashMap<>();
        Map<Integer, ArrayList<ArrayList<Service>>> _selectServiceMap = new HashMap<>();

        //求selectServiceMap和_selectServiceMap
        for (int m = numSplit; m <= binChrom.length; m++) {
            serviceListList = new ArrayList<>();
            noListList = new ArrayList<>();
            combination(m, serArr);
            for (int i = 0; i < serviceListList.size(); i++) {
                ArrayList<Service> tempList = new ArrayList();
                tempList.addAll(serList);
                tempList.removeAll(serviceListList.get(i));
                noListList.add(tempList);
            }
            selectServiceMap.put(m, serviceListList);
            _selectServiceMap.put(m, noListList);
        }


        //计算erasure coding(numSplit, numService)下的个体的availability和cost
        //availability
        for (int k = numSplit; k <= binChrom.length; k++) {
            ArrayList<ArrayList<Service>> serListList = selectServiceMap.get(k);
            ArrayList<ArrayList<Service>> _serListList = _selectServiceMap.get(k);
            for (int i = 0; i < serListList.size(); i++) {
                double temp_ava = 1.0;
                double temp_ava2 = 1.0;
                for (Service ser : serListList.get(i)) {
                    temp_ava *= (ser.getAvailibility() / 100);
                }
//                System.out.println("21313        " + _serListList.get(i).size());
                for (Service ser : _serListList.get(i)) {
                    temp_ava2 *= (1.0 - ser.getAvailibility() / 100);
//                    System.out.println("2   " + temp_ava2);
                }
                availability += temp_ava * temp_ava2;
            }
        }

        //total cost
        double storage_cost = 0.0;// 存储成本
        double bandwidth_cost = 0.0;// 带宽成本
        double operation_cost = 0.0;// 操作成本
        Calculate._bubbleSort(serArr);// 按照带宽价格从小到大排序
        for (int i = 0; i < binChrom.length; i++) {
            storage_cost += dataSize / numSplit * 1.0 * serList.get(i).getPricePerGB();
        }
        for (int i = 0; i < numSplit; i++) {
            bandwidth_cost += (serArr[i].getPriceOutbandwidth() * readFrequency * dataSize / numSplit * 1.0);
            operation_cost += (1 / numSplit) * readFrequency * serArr[i].getPricePer10kGet();
        }
        totalCost = storage_cost + bandwidth_cost + operation_cost;
        Calculate.df.format(totalCost);
//        System.out.println(Calculate.df.format(availability) + " " + Calculate.df.format(totalCost));
        double[] temp = {availability, totalCost};
//        System.out.println(Arrays.toString(temp));
        return temp;
    }

    /**
     * 计算每个目标函数的适应度，在本问题中即计算存储中产生的成本以及整个数据的可用性。
     */
    public void calFitness() {
        for (int i = 0; i < popSize; i++) {
            // 对种群中的所有个体依次计算其适应度值数组
            double[] fitness = calCostAndAva(popList.get(i).binChrom);// 临时变量，用户暂时保存个体的适应度值数组
            popList.get(i).setFitness(fitness);
        }
    }

    /**
     * 将当代种群的相关数据复制到prePopList中，供下次迭代时使用
     */
    public void copyPopListToPrePopList() {
        for (int i = 0; i < popSize; i++) {
            prePopList.set(i, popList.get(i));
        }
    }

    /**
     * 判断两个个体p和q之间的Pareto支配关系：@tag=1 表示p支配q；@tag=2 表示q支配p；
     *
     * @tag=0 表示p和q之间等价，没有支配关系
     */
    public int isDominate(Chromsome p, Chromsome q) {
        int tag = 0;// tag=0表示q和q之间等价，没有支配关系
        int index1 = 0;
        int index2 = 0;
        for (int i = 0; i < numObjective; i++) { //判断p支配q否
            if (p.fitness[i] > q.fitness[i]) {
                index1 += 1;
            }
        }
        if (index1 == numObjective) {
            tag = 1;// 表示p支配q
        }
        for (int i = 0; i < numObjective; i++) { //判断p支配q否
            if (p.fitness[i] < q.fitness[i]) {
                index2 += 1;
            }
        }
        if (index2 == numObjective) {
            tag = 2;// 表示p支配q
        }
        return tag;
    }

    /**
     * 对list列表中的个体按照fitness值从小到大进行排序（快速排序）
     */
    public ArrayList<Chromsome> getSortList(ArrayList<Chromsome> list, int numObjective) {
        Chromsome temp = new Chromsome();
        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size(); j++) {
                if (list.get(i).fitness[numObjective] > list.get(j).fitness[numObjective]) {
                    temp = list.get(i);
                    list.set(i, list.get(j));
                    list.set(j, temp);
                }
            }
        }
        return list;
    }

    /**
     * 快速非支配排序
     */
    public ArrayList<ArrayList<Chromsome>> fastNondominateSort() {

        //第一个Pareto等级集合
        ArrayList<Chromsome> firstParetoRankSet = new ArrayList<>();
        //所有Pareto等级集合
        ArrayList<ArrayList<Chromsome>> paretoRankSetList = new ArrayList<ArrayList<Chromsome>>();
        //两代种群集合
        ArrayList<Chromsome> unionPopList = new ArrayList<Chromsome>();

        /*for (int i = 0; i < 2 * popSize; i++) {
            Chromsome chromsome = new Chromsome(numService, numObjective, numSplit);
            unionPopList.add(chromsome);
        }*/

        //将prePopList、PopList中的元素复制到unionPopList中
        for (int i = 0; i < popSize; i++) {
            unionPopList.add(prePopList.get(i));
        }
        for (int i = 0; i < popSize; i++) {
            unionPopList.add(popList.get(i));
        }

        // 每个个体和所有个体比较，判断支配关系
        for (int i = 0; i < unionPopList.size(); i++) {
            for (int j = 0; j < unionPopList.size(); j++) {
                int tag = this.isDominate(unionPopList.get(i), unionPopList.get(j));
                if (tag == 1) {
                    unionPopList.get(i).dominatingList.add(unionPopList.get(j));// 记录当前个体支配的个体（解）
                } else if (tag == 2) {
                    unionPopList.get(i).numDominated += 1; //支配当前个体的个体数目+1
                }
            }
            // 如果当前个体的numDominated属性等于0，即该个体不受任何个体支配，属于支配最前沿，
            // 则将其加入第一个Pareto等级集合，并将其paretoRank值设置为1
            if (unionPopList.get(i).numDominated == 0) {
                unionPopList.get(i).setParetoRank(1);
                firstParetoRankSet.add(unionPopList.get(i));
            }
        }
        paretoRankSetList.add(firstParetoRankSet);

        int rank = 0;
        while (paretoRankSetList.get(rank).size() > 0
                && paretoRankSetList.get(rank) != null) {
            // 用于储存下一前沿的pareto集合
            ArrayList<Chromsome> paretoRankSet = new ArrayList<Chromsome>();
            // 依次处理当前pareto等级里的所有个体，遍历其所支配的个体集合
            // 执行numDominated-1
            for (Chromsome currentChromsome : paretoRankSetList.get(rank)) {
                if (currentChromsome.getDominatingList().size() > 0
                        && currentChromsome.getDominatingList() != null) {
                    for (Chromsome dominatedChromsome : currentChromsome.getDominatingList()) {
                        dominatedChromsome.numDominated -= 1;
                        // 若当前个体numDominated=0，修改个体的paretoRank属性值，并将其加入相应的pareto等级集合
                        if (dominatedChromsome.numDominated == 0) {
                            // 从第二个等级开始
                            dominatedChromsome.setParetoRank(rank + 1);
                            paretoRankSet.add(dominatedChromsome);
                        }
                    }
                }
            }
            if (paretoRankSet != null && paretoRankSet.size() > 0) { //如果paretoRankSet不为空，将其加入paretoRankSetList中；否则，说明所有个体都已经处理完毕，退出循环
                paretoRankSetList.add(paretoRankSet);// 将新生成的paretoRankSet加入paretoRankSetList中供后面计算crowdingDistance时使用
                rank += 1;
            } else {
                break;
            }
        }
        return paretoRankSetList;
    }

    /**
     * 拥挤距离的计算
     */
    public void crowdingdistance(ArrayList<ArrayList<Chromsome>> paretoRankSetList) {
        for (ArrayList<Chromsome> paretoRankSet : paretoRankSetList) {
            if (paretoRankSet.size() > 0 && paretoRankSet != null) {
                // 依照每个目标计算个体的crowdingDistance的值
                for (int i = 0; i < numObjective; i++) {
                    // 对paretoRankSet中的个体按照fitness从小到大排序，在我们这个问题中可能在一开始的时候将价格定为负数
                    // 这样就可以和可用性一样度量，越大越好，control函数那里可以修改，或者数据库
                    paretoRankSet = this.getSortList(paretoRankSet, i);
                    //修改两个边界值
                    if (paretoRankSet.size() == 1) {// paretoRankSet中只有一个个体
                        paretoRankSet.get(0).crowdingDistance += 100.0;
                    } else if (paretoRankSet.size() == 2) {// paretoRankSet中有两个个体
                        paretoRankSet.get(0).crowdingDistance += 100.0;
                        paretoRankSet.get(1).crowdingDistance += 1000000.0;
                    } else { // paretoRankSet个体数大于2
                        double minFitness = paretoRankSet.get(0).fitness[i];//目标函数的最小值
                        double maxFitness = paretoRankSet.get(paretoRankSet.size() - 1).fitness[i];// 最大值

                        paretoRankSet.get(0).crowdingDistance += 100.0;// 将第1个和最后1个个体的Distance设置为无穷大，以便保存处在边界上的个体，为了便于计算设置为10000.0
                        paretoRankSet.get(paretoRankSet.size() - 1).crowdingDistance += 1000000.0;

                        for (int j = 1; j < paretoRankSet.size() - 2; j++) {// 依次计算其他个体的crowdingDistance
                            paretoRankSet.get(j).crowdingDistance += ((paretoRankSet.get(j + 1).fitness[i] -
                                    paretoRankSet.get(j - 1).fitness[i]) / (maxFitness - minFitness));
                        }
                    }
                }
            }
        }

        // 在一个paretoRankSet中根据个体的crowdingDistance从大到小对个体进行排序
        for (int i = 0; i < paretoRankSetList.size(); i++) {
            ArrayList<Chromsome> paretoRankSet = paretoRankSetList.get(i);
            for (int j = 0; j < paretoRankSet.size(); j++) {
                Chromsome temp = new Chromsome();
                for (int k = j + 1; k < paretoRankSet.size(); k++) {
                    if (paretoRankSet.get(j).crowdingDistance < paretoRankSet.get(k).crowdingDistance) {
                        temp = paretoRankSet.get(j);
                        paretoRankSet.set(j, paretoRankSet.get(k));
                        paretoRankSet.set(k, temp);
                    }
                }
            }
        }

        if (DEBUG) {
            for (int i = 0; i < paretoRankSetList.size(); i++) {// 计算pareto等级集合中个体的crowdingDistance值
                ArrayList<Chromsome> paretoRankSet = paretoRankSetList.get(i);
                System.out.println("第" + i + "个Pareto集合的crowdingDistance： ");
                if (paretoRankSet.size() > 0 && paretoRankSet != null) {
                    for (int j = 0; j < paretoRankSet.size(); j++) {
                        System.out.println(paretoRankSet.get(j).crowdingDistance + "  ");
                    }
                }
                System.out.println();
            }
        }

    }

    /**
     * 形成新的种群
     */
    public void makeNewPopulation() {
        // 对两代种群进行快速非支配排序
        ArrayList<ArrayList<Chromsome>> paretoRankSetList = this.fastNondominateSort();
        // 对每个Pareto前沿计算拥挤距离并排序
        this.crowdingdistance(paretoRankSetList);
        //记录加入到popList中的个体总数
        int numChromsome = 0;
        // popList的下标
        int popListIndex = 0;
        //取出前面n个个体作为下一代种群
        for (int i = 0; i < paretoRankSetList.size(); i++) {
            ArrayList<Chromsome> paretoRankSet = paretoRankSetList.get(i);
            numChromsome += paretoRankSet.size();

            if (numChromsome < popSize && popListIndex < popSize) {
                for (int j = 0; j < paretoRankSet.size(); j++) {
                    popList.set(popListIndex++, paretoRankSet.get(j));
                }
            } else {
                int overNum = popSize - numChromsome;
                for (int j = 0; j < paretoRankSet.size() - overNum &&
                        popListIndex < popSize; j++) {
                    popList.set(popListIndex++, paretoRankSet.get(j));
                }
            }
        }
    }

    /**
     * PMX需要进行冲突检测
     * 1 对种群中的个体进行随机配对并将配对结果保存到index中
     * 2 对配对后的个体执行单点交叉，按照index中的次序相邻两个个体进行交叉
     * 3 交叉后的个体代替父代个体进入种群
     */
    public void crossoverOperator() {
        Random random = new Random();
        int point;// 配对过程中的中间变量
        int temp;

        // 对种群中的个体进行随机配对
        int[] index = new int[popSize];
        for (int i = 0; i < popSize; i++) {
            index[i] = i;
        }
        for (int i = 0; i < popSize; i++) {
            point = random.nextInt(popSize - i);
            temp = index[i];
            index[i] = index[point + i];
            index[point + i] = temp;
        }

        for (int i = 0; i < popSize - 1; i += 2) {
            double pro = random.nextDouble();
            if (pro < pc) {
                int[] temp1 = new int[numService];
                int[] temp2 = new int[numService];
                System.arraycopy(popList.get(index[i]).binChrom, 0, temp1, 0, numService);
                System.arraycopy(popList.get(index[i + 1]).binChrom, 0, temp2, 0, numService);
                point = random.nextInt(numService);// 交叉点
                // 交换index[i]和index[i+1]在交叉点后面的基因
                for (int j = point; j < numService; j++) {
                    temp = popList.get(index[i]).binChrom[j];
                    popList.get(index[i]).binChrom[j] = popList.get(index[i + 1]).binChrom[j];
                    popList.get(index[i + 1]).binChrom[j] = temp;
                }
                //处理产生的冲突基因
                // 将互换的基因段以外的部分中与互换后基因段中的元素冲突用另一父代的相应位置代替，直到没有冲突
                //i基因
                for (int j = 0; j < point; j++) {
                    for (int k = point; k < numService; k++) {
                        if (popList.get(index[i]).binChrom[j] == popList.get(index[i]).binChrom[k]) {
                            popList.get(index[i]).binChrom[j] = temp2[k];
                        }
                    }
                }
                //i+1基因
                for (int j = 0; j < point; j++) {
                    for (int k = point; k < numService; k++) {
                        if (popList.get(index[i + 1]).binChrom[j] == popList.get(index[i + 1]).binChrom[k]) {
                            popList.get(index[i + 1]).binChrom[j] = temp1[k];
                        }
                    }
                }
            }
        }
    }

    /**
     * Order Crossover (OX)
     * 不需要进行冲突检测
     */
    public void orderCrossover() {
        Random random = new Random();
        int point;// 配对过程中的中间变量
        int temp;
        int point1;// 两个交叉点
        int point2;

        // 对种群中的个体进行随机配对
        int[] index = new int[popSize];
        for (int i = 0; i < popSize; i++) {
            index[i] = i;
        }
        for (int i = 0; i < popSize; i++) {
            point = random.nextInt(popSize - i);
            temp = index[i];
            index[i] = index[point + i];
            index[point + i] = temp;
        }
        for (int i = 0; i < popSize - 1; i += 2) {
            double pro = random.nextDouble();
            if (pro < pc) {
                int[] temp1 = new int[popList.get(index[i]).binChrom.length];
                int[] temp2 = new int[popList.get(index[i + 1]).binChrom.length];
                System.arraycopy(popList.get(index[i]).binChrom, 0, temp1, 0, numService);
                System.arraycopy(popList.get(index[i + 1]).binChrom, 0, temp2, 0, numService);
                point1 = random.nextInt(numService);// 交叉点
                point2 = random.nextInt(numService);// 交叉点
                while (point1 == point2) {
                    point2 = random.nextInt(numService);
                }
                if (point1 > point2) {//确保ran1<ran2
                    temp = point1;
                    point1 = point2;
                    point2 = temp;
                }
                int ww = 0;
                // 在第i个个体的基础上补充i+1的个体
                for (int m = 0; m < point1; m++) {
                    for (int j = ww; j < numService; j++) {
                        int flag = 1;
                        for (int k = point1; k <= point2; ) {
                            if (popList.get(index[i + 1]).binChrom[j] == popList.get(index[i]).binChrom[k]) {
                                flag = 0;
                                break;
                            }
                            k++;
                        }
                        if (flag == 1) {
                            popList.get(index[i]).binChrom[m] = popList.get(index[i + 1]).binChrom[j];
                            ww = j + 1;
                            break;
                        }
                    }
                }
                for (int m = point2 + 1; m < numService; m++) {
                    for (int j = ww; j < numService; j++) {
                        int flag = 1;
                        for (int k = point1; k <= point2; ) {
                            if (popList.get(index[i + 1]).binChrom[j] == popList.get(index[i]).binChrom[k]) {
                                flag = 0;
                                break;
                            }
                            k++;
                        }
                        if (flag == 1) {
                            popList.get(index[i]).binChrom[m] = popList.get(index[i + 1]).binChrom[j];
                            ww = j + 1;
                            break;
                        }
                    }
                }
                // 在第i+1个个体的基础上补充i的个体
                ww = 0;
                for (int m = 0; m < point1; m++) {
                    for (int j = ww; j < numService; j++) {
                        int flag = 1;
                        for (int k = point1; k <= point2; ) {
                            if (temp1[j] == popList.get(index[i + 1]).binChrom[k]) {
                                flag = 0;
                                break;
                            }
                            k++;
                        }
                        if (flag == 1) {
                            popList.get(index[i + 1]).binChrom[m] = temp1[j];
                            ww = j + 1;
                            break;
                        }
                    }
                }
                for (int m = point2 + 1; m < numService; m++) {
                    for (int j = ww; j < numService; j++) {
                        int flag = 1;
                        for (int k = point1; k <= point2; ) {
                            if (temp1[j] == popList.get(index[i + 1]).binChrom[k]) {
                                flag = 0;
                                break;
                            }
                            k++;
                        }
                        if (flag == 1) {
                            popList.get(index[i + 1]).binChrom[m] = temp1[j];
                            ww = j + 1;
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * 按照pm进行单点变异，即随机确定两个变异点，然后交换位置
     */
    public void mutationOperator() {
        Random random = new Random();
        for (int i = 0; i < popSize - 1; i++) {
            double pro = random.nextDouble();
            if (pro < pm) {
                int ran1, ran2, temp;
                int count;
                count = random.nextInt(65535) % numService;
                for (int j = 0; j < count; j++) {
                    ran1 = random.nextInt(65535) % numService;
                    ran2 = random.nextInt(65535) % numService;
                    while (ran1 == ran2) {
                        ran2 = random.nextInt(65535) % numService;
                    }
                    temp = popList.get(j).binChrom[ran1];
                    popList.get(j).binChrom[ran1] = popList.get(j).binChrom[ran2];
                    popList.get(j).binChrom[ran2] = temp;
                }
            }
        }
    }

    /**
     * 性能评估
     */
    public double performanceMeasure() {
        //用于储存pareto前沿
        ArrayList<Chromsome> paretoFront = new ArrayList<Chromsome>();
        //每个目标最大的fitness
        double[] maxFitness = new double[this.numObjective];
        //每个目标最小的fitness
        double[] minFitness = new double[this.numObjective];
        //公式的分子
        double paraUp = 0.0;
        //公式的分母
        double paraDown = 0.0;
        //拥挤度平均值
        double meanDis = 0.0;
        //取出pareto前沿的个体,顺便算出拥挤度之和
        for (int i = 0; i < popList.size(); i++) {
            if (popList.get(i).paretoRank == 1) {
                paretoFront.add(popList.get(i));
                meanDis += popList.get(i).crowdingDistance;
            }
        }
        meanDis = meanDis / paretoFront.size();
        for (int i = 0; i < numObjective; i++) {
            maxFitness[i] = Double.MAX_VALUE * (-1.0);// 因为在这里可能要把价格取负处理
            minFitness[i] = Double.MAX_VALUE;
        }
        for (int i = 0; i < this.numObjective; i++) {
            for (int j = 0; j < paretoFront.size(); j++) {
                if (paretoFront.get(j).fitness[i] > maxFitness[i]) {
                    maxFitness[i] = paretoFront.get(j).fitness[i];
                }
                if (paretoFront.get(j).fitness[i] < minFitness[i]) {
                    minFitness[i] = paretoFront.get(j).fitness[i];
                }
            }
        }
        //计算分子
        for (int i = 0; i < this.numObjective; i++) {
            paraUp += (maxFitness[i] - minFitness[i]);
        }
        //计算分母
        for (int i = 0; i < paretoFront.size(); i++) {
            paraDown += Math.pow((paretoFront.get(i).crowdingDistance - meanDis), 2);
        }

        //计算评价值
        double measure = paraUp / paraDown;
        return measure;
    }

    /**
     * 记录实验结果
     */
    public void writeFile(double measure) throws IOException {
        bufferedWriter.write(numObjective + " " + numService + "   " + measure);
        bufferedWriter.newLine();
        for (int i = 0; i < popList.size(); i++) {
            if (popList.get(i).paretoRank == 1) {
                bufferedWriter.write(Arrays.toString(popList.get(i).binChrom));
                bufferedWriter.write(Arrays.toString(popList.get(i).fitness));
                bufferedWriter.write("  ");
                Calculate.df.format(popList.get(i).crowdingDistance);
                System.out.println(Calculate.df.format(popList.get(i).crowdingDistance));
                bufferedWriter.write(String.valueOf(Calculate.df.format(popList.get(i).crowdingDistance)));
                bufferedWriter.newLine();
            }

        }
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }

    /**
     * 运行算法逻辑
     */
    public double runNSGA2() throws IOException {

        // 开始执行时间
        long startTime = System.currentTimeMillis();

        // 初始化种群
        this.initPopulation();

        // 计算当前的适应度
        this.calFitness();

        // 将种群保存起来供下次迭代
        this.copyPopListToPrePopList();

        while (gen < maxGen) {

            // 进入下一代
            gen += 1;

            //交叉、变异操作
            this.orderCrossover();
//            this.crossoverOperator();
            this.mutationOperator();

            //计算适应度
            this.calFitness();

            // 找出支配高，拥挤距离大的个体形成新的种群
            this.makeNewPopulation();

            //将种群保存起来供下次迭代
            this.copyPopListToPrePopList();
        }
        long endTime = System.currentTimeMillis();
        System.out.println(numSplit + "  " + numService);
        System.out.println("NSGA2执行时间：" + (endTime - startTime) + "毫秒");
        Double measure = new Double(this.performanceMeasure());
        if (!measure.isInfinite() && !measure.isNaN()) {
            System.out.println(measure);
            writeFile(measure);
            return measure;
        } else {
            return 0;
        }
    }

    /**
     * 排列组合，即从所有云服务中挑选m个的所有组合
     *
     * @param m
     * @param service
     */
    public static void combination(int m, Service service[]) {
        if (service == null || service.length == 0) {
            return;
        }
        for (int i = 1; i <= service.length; i++) {
            combine(m, service, 0, i, NSGA2.list);
        }
    }

    public static void combine(int m, Service[] cs, int begin, int number, ArrayList<Service> list) {
        if (number == 0) {
            if (list.size() == m) {
                ArrayList temp = new ArrayList();
                for (Service ser : list)
                    temp.add(ser);
                serviceListList.add(temp);
                return;
            }

        }
        if (begin == cs.length) {
            return;
        }
        list.add(cs[begin]);
        combine(m, cs, begin + 1, number - 1, list);
        list.remove((Service) cs[begin]);
        combine(m, cs, begin + 1, number, list);
    }
}

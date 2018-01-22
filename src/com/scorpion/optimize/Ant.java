package com.scorpion.optimize;

import Test.Service;

import java.util.*;

/**
 * Created by Scorpion on 2017/3/6.
 */
public class Ant {

    static ArrayList<ArrayList<Service>> serviceListList = new ArrayList<ArrayList<Service>>();
    static ArrayList<ArrayList<Service>> noListList = new ArrayList();
    static ArrayList<Service> list = new ArrayList<>();

    private Vector<Integer> tabu;//禁忌表，用来表示该蚂蚁访问过的云服务
    private Vector<Integer> allowService;//允许搜索的云服务
    private double[] delta;//信息素变化矩阵
    private ArrayList<Service> serviceList;

    private double[] distance;

    private double alpha;
    private double beta;

    private double QOS;//组合QoS值
    private int serviceNum;//云服务商数量

    private int firstService;
    private int currentService;
    public int length;

    public Ant() {
        serviceNum = 13;
        QOS = 0.0;
    }

    public Ant(int num) {
        serviceNum = num;
        QOS = 0.0;
    }

    /**
     * 初始化蚂蚁，随机选择起始位置
     *
     * @return
     */
    public void init(double[] distance, double a, double b, ArrayList<Service> list) {
        alpha = a;
        beta = b;
        tabu = new Vector<>();
        serviceList = list;
        allowService = new Vector<>();
        this.distance = distance;
        length = serviceList.size();
        delta = new double[length];
        for (int i = 0; i < length; i++) {
            allowService.add(i);
            delta[i] = 0.f;
        }
        Random random = new Random(System.currentTimeMillis());
        firstService = random.nextInt(length);
        for (Integer i : allowService) {
            if (i.intValue() == firstService) {
                allowService.remove(i);
                break;
            }
        }
        tabu.add(Integer.valueOf(firstService));
        currentService = firstService;
    }

    /**
     * 选择下一个云服务商
     *
     * @return
     */
    public void selectNextService(double[] pheromone) {
        double[] p = new double[length];
        double sum = 0.f;
        //计算分母部分
        for (Integer i : allowService) {
            sum += Math.pow(pheromone[i.intValue()], alpha) * Math.pow(distance[i.intValue()], beta);
        }
        //计算概率矩阵
        for (int i = 0; i < length; i++) {
            boolean flag = false;
            for (Integer j : allowService) {
                if (i == j.intValue()) {
                    p[i] = (double) (Math.pow(pheromone[i], alpha) * Math.pow(distance[i], beta)) / sum;
                    flag = true;
                }
            }
            if (flag == false) {
                p[i] = 0.f;
            }
        }
        //轮盘赌选择下一个城市
        Random random = new Random(System.currentTimeMillis());
        double selectD = random.nextDouble();
        int selectService = 0;
        double sum1 = 0.f;
        for (int i = 0; i < length; i++) {
            sum1 += p[i];
            if (sum1 >= selectD) {
                selectService = i;
                break;
            }
        }
        //从允许选择到的云服务商中去除selectService
        for (Integer i : allowService) {
            if (i.intValue() == selectService) {
                allowService.remove(i);
                break;
            }
        }
        //在禁忌表中添加selectService
        tabu.add(Integer.valueOf(selectService));
    }

    /**
     * 计算总的QoS的值
     *
     * @return QoS的值
     */
    public BestTour calculateQOS() {
        double best_QoS = Double.MIN_VALUE;
        ArrayList<Service> serviceArrayList = new ArrayList<>();
        Service[] serArr = new Service[this.getTabu().size()];
        for (int i = 0; i < this.getTabu().size(); i++) {
            serviceArrayList.add(serviceList.get(this.getTabu().get(i)));
            serArr[i] = serviceList.get(this.getTabu().get(i));
        }
        Map<Integer, ArrayList<ArrayList<Service>>> selectServiceMap = new HashMap<>();
        Map<Integer, ArrayList<ArrayList<Service>>> _selectServiceMap = new HashMap<>();
        for (int m = 1; m <= serviceArrayList.size(); m++) {
            serviceListList = new ArrayList<>();//
            noListList = new ArrayList<>();
            combiantion(m, serArr);
            for (int i = 0; i < serviceListList.size(); i++) {
                ArrayList<Service> tempList = new ArrayList();
                tempList.addAll(serviceArrayList);
                tempList.removeAll(serviceListList.get(i));
                noListList.add(tempList);
            }
            selectServiceMap.put(m, serviceListList);
            _selectServiceMap.put(m, noListList);
        }
        BestTour bestTour = new BestTour();
        for (int m = 1; m <= serviceArrayList.size(); m++) {
            double sum_ava = 0.0;
            double ava = 0.0;
            for (int k = m; k <= serviceArrayList.size(); k++) {
                ArrayList<ArrayList<Service>> serListList = selectServiceMap.get(k);
                ArrayList<ArrayList<Service>> _serListList = _selectServiceMap.get(k);
                for (int i = 0; i < serListList.size(); i++) {
                    double temp_ava = 1.0;
                    for (Service ser : serListList.get(i)) {
                        temp_ava *= (ser.getAvailibility() / 100);
                    }
                    for (Service ser : _serListList.get(i)) {
                        temp_ava *= (1.0 - ser.getAvailibility() / 100);
                    }
                    ava += temp_ava;
                }
            }
            sum_ava += ava;
            //System.out.println(m + "    " + serviceArrayList.size() + "   " + sum_ava);
            if (sum_ava >= 0.99) {
                double qos = 0.0;
                //计算价格
                double total_price = 0.0;
                double price_storage = 0.0;
                double price_out = 0.0;
                _bubbleSort(serArr);//按照带宽价格升序排序
                double unit = 2000 / m;
                for (int i = 0; i < serviceArrayList.size(); i++) {//存储价格
                    price_storage += unit * serviceArrayList.get(i).getPricePerGB();
                }
                System.out.println("price_storage  "+ price_storage);
                for (int i = 0; i < m; i++) {//带宽价格
                    price_out += (serArr[i].getPriceOutbandwidth() * 2 * unit +
                            (1 / m) * 2 * serArr[i].getPricePer10kGet());
                }
                System.out.println("price_out   "+price_out);
                total_price = price_out + price_storage;
                System.out.println(total_price);
                qos = 0.2 * sum_ava + 0.8 / total_price;
                System.out.println(unit);
                System.out.println(m + "    " + serviceArrayList.size() + "   " + sum_ava + "   " + total_price
                        + "  "+qos);
                if (qos > best_QoS) {
                    bestTour = new BestTour();
                    best_QoS = qos;
                    bestTour.setM(m);
                    bestTour.setQoS(best_QoS);
                }
            }
        }
        return bestTour;
    }


    /*
     * 排列组合
     */
    public static void combiantion(int m, Service service[]) {
        if (service == null || service.length == 0) {
            return;
        }

        for (int i = 1; i <= service.length; i++) {
            combine(m, service, 0, i, list);
        }
    }

    // 从字符数组中第begin个字符开始挑选number个字符加入list中
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

    public static void _bubbleSort(Service[] serviceArr) {
        Service temp; // 记录临时中间值
        int size = serviceArr.length; // 数组大小
        for (int i = 0; i < size - 1; i++) {
            for (int j = i + 1; j < size; j++) {
                if (serviceArr[i].getPriceOutbandwidth() > serviceArr[j].getPriceOutbandwidth()) { // 交换两数的位置
                    temp = serviceArr[i];
                    serviceArr[i] = serviceArr[j];
                    serviceArr[j] = temp;
                }
            }
        }
    }

    public Vector<Integer> getTabu() {
        return tabu;
    }

    public void setTabu(Vector<Integer> tabu) {
        this.tabu = tabu;
    }

    public Vector<Integer> getAllowService() {
        return allowService;
    }

    public void setAllowService(Vector<Integer> allowService) {
        this.allowService = allowService;
    }

    public double[] getDelta() {
        return delta;
    }

    public void setDelta(double[] delta) {
        this.delta = delta;
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

    public double getQOS() {
        return QOS = calculateQOS().getQoS();
    }

    public void setQOS(double QOS) {
        this.QOS = QOS;
    }

    public int getServiceNum() {
        return serviceNum;
    }

    public void setServiceNum(int serviceNum) {
        this.serviceNum = serviceNum;
    }

    public int getFirstService() {
        return firstService;
    }

    public void setFirstService(int firstService) {
        this.firstService = firstService;
    }

    public int getCurrentService() {
        return currentService;
    }

    public void setCurrentService(int currentService) {
        this.currentService = currentService;
    }

    public double[] getDistance() {
        return distance;
    }

    public void setDistance(double[] distance) {
        this.distance = distance;
    }

    public ArrayList<Service> getServiceList() {
        return serviceList;
    }

    public void setServiceList(ArrayList<Service> serviceList) {
        this.serviceList = serviceList;
    }
}

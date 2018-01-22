package com.scorpion.NSGA2;

import Test.Service;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class Calculate {
    /**
     * 排列组合，即从所有云服务中挑选m个的所有组合
     * @param m
     * @param service
     */
    public static void combination(int m, Service service[]){
        if(service == null || service.length == 0){
            return;
        }
        for (int i = 1 ; i <= service.length ; i++) {
            combine(m, service, 0, i, NSGA2.list);
        }
    }
    public static void combine(int m, Service[] cs, int begin, int number, ArrayList<Service> list){
        if (number == 0) {
            if (list.size() == m) {
                ArrayList temp = new ArrayList();
                for (Service ser : list)
                    temp.add(ser);
                NSGA2.serviceListList.add(temp);
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

    /**
     * 按照带宽价格从小到大进行排序
     * @param serviceArr
     */
    public static void _bubbleSort(Service[] serviceArr){
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

    /**
     * 保留两位小数
     */
    public static double format(double num){
        DecimalFormat df = new DecimalFormat("######0.000");
        return num;
    }
    public static DecimalFormat df = new DecimalFormat("######0.00");


}

package com.scorpion.NSGA2;

import JDBC.MongoJDBC;
import Test.Service;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class NSGA2Simulation {
    final static boolean DEBUG = false;
    final static int expsize = 5;
    final static int dataSize = -200; //datasize
    final static double readFrequency = 0.3;

    public static void main(String[] args) throws IOException {
        int popSize = 200;//种群规模
        int maxGen = 500;// 最大进化次数
        int numObjective = 2;//目标函数
        ArrayList<Service> serviceList = getServiceList();
        double pc = 0.65;// 交叉概率
        double pm = 0.02;// 变异概率
//        NSGA2 nsga2 = new NSGA2(popSize, numObjective, maxGen, serviceList,
//                pc, pm, 3, 2, dataSize, readFrequency);
//        nsga2.runNSGA2();
        for (int i = 2; i < 6; i++) {
            for (int j = 1; j < i; j++) {
                NSGA2 nsga2 = new NSGA2(popSize, numObjective, maxGen, serviceList,
                        pc, pm, i, j, dataSize, readFrequency);
                nsga2.runNSGA2();
            }
        }


    }

    public static ArrayList<Service> getServiceList(){
        Random random = new Random(System.currentTimeMillis());
        MongoJDBC jdbc = new MongoJDBC();
        DB db = jdbc.connection();
        DBCollection collection = db.getCollection("exp3");
        DBCursor cursor = collection.find();
        ArrayList<Service> serviceList = new ArrayList<>();
        ArrayList<Service> temp = new ArrayList<>();
        int[] index = new int[expsize];
        for (int i = 0; i < expsize; i++) {
            index[i] = random.nextInt(35);
        }
        int count = 0;
        while (cursor.hasNext()) {
            DBObject doc = cursor.next();
            String serviceId = (String) doc.get("serviceId");
            double pricePerGB = (double) doc.get("pricePerGB");
            double pricePer10kGet = (double) doc.get("pricePer10kGet");
            double priceOutbandwidth = (double) doc.get("priceOutbandwidth");
            double availibility = (double) doc.get("availability");
            Service service = new Service(serviceId, pricePerGB, pricePer10kGet, priceOutbandwidth, availibility,
                    0);
            temp.add(service);
            count++;
        }
        for (int i = 0; i < expsize; i++) {
            serviceList.add(temp.get(index[i]));
        }
        return temp;
    }
}

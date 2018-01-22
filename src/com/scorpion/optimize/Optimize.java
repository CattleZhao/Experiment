package com.scorpion.optimize;

import JDBC.MongoJDBC;
import Test.Service;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;

/**
 * Created by Scorpion on 2017/3/6.
 */
public class Optimize {
    public static void main(String[] args) {
        MongoJDBC jdbc = new MongoJDBC();
        DB db = jdbc.connection();
        DBCollection collection = db.getCollection("exp2");
        DBCursor cursor = collection.find();
        ArrayList<Service> serviceList = new ArrayList<>();
        Service[] serviceArr = new Service[13];
        double[] distance = new double[13];
        int count = 0;
        while (cursor.hasNext()) {
            if(count == 13)
                break;
            DBObject doc = cursor.next();
            String serviceId = (String) doc.get("serviceId");
            double pricePerGB = (double) doc.get("pricePerGB");
            double pricePer10kGet = (double) doc.get("pricePer10kGet");
            double priceOutbandwidth = (double) doc.get("priceOutbandwidth");
            double availibility = (double) doc.get("availability");
            double totalprice = pricePerGB + priceOutbandwidth;
            double delta = 0.5 * availibility / 100 + 0.5 * totalprice;
            Service service = new Service(serviceId, pricePerGB, pricePer10kGet, priceOutbandwidth, availibility,
                    delta);
            distance[count] = delta;
            serviceList.add(service);
            count++;
        }
        System.out.println(serviceList.size());
        double best_qos = Double.MIN_VALUE;
        AvaResult bestResult = new AvaResult();
        for (int m = 2; m <= count; m++) {
            ACO aco = new ACO(m, 100, 200, 1.0, 3.0, 0.7, distance, serviceList);
            aco.init();
            AvaResult result=aco.solve();
            if (result.getQoS() > best_qos) {
                best_qos = result.getQoS();
                bestResult.setQoS(best_qos);
                bestResult.setM(result.getM());
                bestResult.setTour(result.getTour());
            }
        }
        System.out.println("最优的结果是：");
        System.out.println("QoS = " + bestResult.getQoS());
        System.out.println("m = " + bestResult.getM() + " ,n = " + bestResult.getTour().length);
        System.out.println("选择的云服务商为：");
        for (int i = 0; i < bestResult.getTour().length; i++) {
            System.out.print(bestResult.getTour()[i] + " ");
        }
    }
}

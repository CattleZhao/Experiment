package Test;

import JDBC.MongoJDBC;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static sun.swing.MenuItemLayoutHelper.max;

public class test {
    static Service[] serviceArr = new Service[13];
    static ArrayList<Service> serviceList = new ArrayList<Service>();

    static ArrayList<ArrayList<Service>> serviceListList = new ArrayList<ArrayList<Service>>();
    static ArrayList<ArrayList<Service>> noListList = new ArrayList();

    static ArrayList<Service> newList = new ArrayList<>();
    static ArrayList<Service> list = new ArrayList<>();
    static int count = 0;
    /*final static File file = new File("D:/TestCloud4.txt");
    static BufferedWriter bufferedWriter;

    static {
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    /*
     * 对所有云服务商的可用性进行降序排序
     */
    public static void bubbleSort(Service[] serviceArr) {
        Service temp; // 记录临时中间值
        int size = serviceArr.length; // 数组大小
        for (int i = 0; i < size - 1; i++) {
            for (int j = i + 1; j < size; j++) {
                if (serviceArr[i].getAvailibility() < serviceArr[j].getAvailibility()) { // 交换两数的位置
                    temp = serviceArr[i];
                    serviceArr[i] = serviceArr[j];
                    serviceArr[j] = temp;
                }
            }
        }
    }/*
     * 对所有云服务商的综合价格进行升序排序
     */

    public static void _bubbleSort(Service[] serviceArr) {
        Service temp; // 记录临时中间值
        int size = serviceArr.length; // 数组大小
        for (int i = 0; i < size - 1; i++) {
            for (int j = i + 1; j < size; j++) {
                if (serviceArr[i].getDelta() > serviceArr[j].getDelta()) { // 交换两数的位置
                    temp = serviceArr[i];
                    serviceArr[i] = serviceArr[j];
                    serviceArr[j] = temp;
                }
            }
        }
    }
    public static void sort(int[] a){
        int temp;
        int size = a.length;
        for (int i = 0; i < size - 1; i++) {
            for (int j = i + 1; j < size; j++) {
                if (a[i]> a[j]) { // 交换两数的位置
                    temp = a[i];
                    a[i] = a[j];
                    a[j] = temp;
                }
            }
        }
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

    /*public static void WriteFile(Integer i, ArrayList<Service> arrayList, ArrayList<Service> _arrayList) throws IOException {
        bufferedWriter.write("第 " + i.toString() + " 种情况");
        bufferedWriter.write("选择的");
        bufferedWriter.newLine();
        for (Service s : arrayList) {
            bufferedWriter.write(s.getServiceId());
            bufferedWriter.newLine();
        }
        bufferedWriter.newLine();
        bufferedWriter.write("未选择的");
        bufferedWriter.newLine();
        for (Service s : _arrayList) {
            bufferedWriter.write(s.getServiceId());
            bufferedWriter.newLine();
        }
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }*/

    /*
    求最大值最小值
     */
    /*public static double MAX_ARRY(double arry[]) {
        double max = arry[0];
        for (int i = 0; i < arry.length; i++) {
            if (arry[i] >= max)
                max = arry[i];
        }
        return max;
    }

    public static double MIN_ARRY(double arry[]) {
        double min = arry[0];
        for (int i = 0; i < arry.length; i++) {
            if (arry[i] <= min)
                min = arry[i];
        }
        return min;
    }*/

    public static void main(String[] args) throws IOException {

        double user_ava = 100;
        ArrayList<String> a = new ArrayList<>();
        ArrayList<Service> G_S = new ArrayList();
        ArrayList<Service> G_C = new ArrayList();
        MongoJDBC jdbc = new MongoJDBC();
        DB db = jdbc.connection();
        DBCollection collection = db.getCollection("exp2");
        DBCursor cursor = collection.find();
        int count = 0;
        while (cursor.hasNext()) {
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
            serviceArr[count] = service;
            serviceList.add(service);
            count++;
        }
        //bubbleSort(serviceArr);

        //int m = 0, n = 0;
//        for (n = 2; n <= 13; n++) {
//            int flag = 0;
//            G_S = new ArrayList<>();
//            for (int i = 0; i < n; i++) {
//                G_S.add(serviceArr[i]);
//            }
//
//            Service[] G_SArr = new Service[n];
//            for (int i = 0; i < G_S.size(); i++) {
//                G_SArr[i] = G_S.get(i);
//            }
//            Map<Integer, ArrayList<ArrayList<Service>>> selectServiceMap = new HashMap<>();
//            Map<Integer, ArrayList<ArrayList<Service>>> _selectServiceMap = new HashMap<>();
//            for (m = 1; m <= n; m++) {
//                serviceListList = new ArrayList<>();//
//                noListList = new ArrayList<>();
//                combiantion(m, G_SArr);
//                for (int i = 0; i < serviceListList.size(); i++) {
//                    ArrayList<Service> tempList = new ArrayList();
//                    tempList.addAll(G_S);
//                    tempList.removeAll(serviceListList.get(i));
//                    noListList.add(tempList);
//                }
//                selectServiceMap.put(m, serviceListList);
//                _selectServiceMap.put(m, noListList);
//            }
//            for (m = 1; m <= n; m++) {
//
//                double sum_ava = 0.0;
//                double ava = 0.0;
//                for (int k = m; k <= n; k++) {
//                    ArrayList<ArrayList<Service>> serListList = selectServiceMap.get(k);
//                    ArrayList<ArrayList<Service>> _serListList = _selectServiceMap.get(k);
//                    for (int i = 0; i < serListList.size(); i++) {
//                        double temp_ava = 1.0;
//                        for (Service ser : serListList.get(i)) {
//                            temp_ava *= (ser.getAvailibility() / 100);
//                        }
//                        for (Service ser : _serListList.get(i)) {
//                            temp_ava *= (1.0 - ser.getAvailibility() / 100);
//                        }
//                        ava += temp_ava;
//                    }
//                }
//                sum_ava += ava;
//                String s1 = "m = " + m + ", n = " + n;
//                Double s2 = sum_ava * 100.0;
//                if (s2 > 99.99 && s2 < 100.0) {
//                    a.add(s1);
//                    a.add(s2.toString());
//                    //System.out.println("m = " + m + ", n = " + n);
//                    //System.out.println(sum_ava);
//                }
//
//
//                /*if (sum_ava >= user_ava / 100) {
//                    flag = 1;
//                    break;
//                }*/
//            }
//            /*if (flag == 1)
//                break;*/
//
//        }
        //WriteFile(a);
        //System.out.println("m = " + m + ", n = " + n);
        /*ArrayList<Service> newList = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            newList.add(serviceArr[i]);
        }
        Service[] G_SArr = new Service[9];
        for (int i = 0; i < newList.size(); i++) {
            G_SArr[i] = newList.get(i);
        }
        serviceListList = new ArrayList<>();//
        noListList = new ArrayList<>();
        combiantion(7, G_SArr);

        for (int i = 0; i < serviceListList.size(); i++) {
            ArrayList<Service> tempList = new ArrayList();
            tempList.addAll(newList);
            tempList.removeAll(serviceListList.get(i));
            noListList.add(tempList);
        }
//        for (int i = 0; i < serviceListList.size(); i++) {
//            WriteFile(i + 1, serviceListList.get(i), noListList.get(i));
//        }
        for (int i = 0; i < newList.size(); i++) {
            double p_GB = newList.get(i).getPricePerGB();
            double p_OB = newList.get(i).getPriceOutbandwidth();
            double t_p = 0.5 * p_GB + 0.5 * p_OB;
            newList.get(i).setDelta(t_p);
        }
        _bubbleSort(G_SArr);
        for (int i = 0; i < G_SArr.length; i++) {
            System.out.println(G_SArr[i].getDelta());
        }
*/

    }
}

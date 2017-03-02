package Test;

import JDBC.MongoJDBC;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class test {
    static Service[] serviceArr = new Service[13];
    static ArrayList<Service> serviceList = new ArrayList<Service>();

    static ArrayList<ArrayList<Service>> serviceListList = new ArrayList<ArrayList<Service>>();
    static ArrayList<ArrayList<Service>> noListList = new ArrayList();

    static ArrayList<Service> list = new ArrayList<>();
    static int count = 0;

    /*
     * 对所有云服务商的delta进行排序
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

    /*public static void WriteFile(ArrayList<ArrayList<Service>> listOfList,
                                 ArrayList<ArrayList<Service>> noListList) throws IOException {
        File file = new File("D:/TestCloud.txt");
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
        for(ArrayList<Service> list : listOfList){
            for(Service service:list){
                bufferedWriter.write(service.getServiceId()+" ");
            }
        }
        bufferedWriter.flush();
        bufferedWriter.close();
    }*/
    public static void main(String[] args) {

        ArrayList<Service> G_S = new ArrayList();
        ArrayList<Service> G_C = new ArrayList();
        MongoJDBC jdbc = new MongoJDBC();
        DB db = jdbc.connection();
        DBCollection collection = db.getCollection("exp");
        DBCursor cursor = collection.find();
        int count = 0;
        while (cursor.hasNext()) {
            DBObject doc = cursor.next();
            String serviceId = (String) doc.get("serviceId");
            double pricePerGB = (double) doc.get("pricePerGB");
            double pricePer10kGet = (double) doc.get("pricePer10kGet");
            double priceOutbandwidth = (double) doc.get("priceOutbandwidth");
            double availibility = (double) doc.get("availability");
            double totalprice = 2048.0 * pricePerGB + 10.0 * 2048.0 * priceOutbandwidth + 100 * pricePer10kGet / 10000;
            double normal_price = (totalprice - 204.8) / (4809 - 204.8);
            double delta = 0.5 * availibility / 100 + 0.5 / normal_price;
            Service service = new Service(serviceId, pricePerGB, pricePer10kGet, priceOutbandwidth, availibility,
                    delta);
            serviceArr[count] = service;
            serviceList.add(service);
            count++;
        }
        bubbleSort(serviceArr);

        int m, n;
        for (n = 2; n <= 8; n++) {
            G_S = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                G_S.add(serviceArr[i]);
            }

            Service[] G_SArr = new Service[n];
            for (int i = 0; i < G_S.size(); i++) {
                G_SArr[i] = G_S.get(i);
            }
            Map<Integer, ArrayList<ArrayList<Service>>> selectServiceMap = new HashMap<>();
            Map<Integer, ArrayList<ArrayList<Service>>> _selectServiceMap = new HashMap<>();
            for (m = 1; m <= n; m++) {
                serviceListList = new ArrayList<>();//
                noListList = new ArrayList<>();
                combiantion(m, G_SArr);
                for (int i = 0; i < serviceListList.size(); i++) {
                    ArrayList<Service> tempList = new ArrayList();
                    tempList.addAll(G_S);
                    tempList.removeAll(serviceListList.get(i));
                    noListList.add(tempList);
                }
                selectServiceMap.put(m, serviceListList);
                _selectServiceMap.put(m, noListList);
            }
            for (m = 1; m <= n; m++) {
                double sum_ava = 0.0;
                for (int k = m; k <= n; k++) {
                    double ava = 1.0;
                    ArrayList<ArrayList<Service>> serListList = selectServiceMap.get(k);
                    ArrayList<ArrayList<Service>> _serListList = _selectServiceMap.get(k);
                    for(int i = 0;i<serListList.size();i++){
                        
                    }
                }
            }


        }

    }
}

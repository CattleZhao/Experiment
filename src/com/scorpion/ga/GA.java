package com.scorpion.ga;

import JDBC.MongoJDBC;
import Test.Service;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import java.util.*;

/**
 * Created by Scorpion on 2017/3/10.
 */
public class GA {
    private int scale;//种群规模
    private int serviceNum;//服务数量，染色体的长度
    private int MAX_GEN;//运行代数
    private int bestT;//最佳出现代数
    private double bestQOS;//最佳QoS值
    private int best_M;//最佳m取值
    private int[] bestTour;//最佳服务商选择
    
    //初始化种群，父代种群，行数表示种群规模，一行代表一个个体，即染色体，列表示染色体的基因片段
    private int[][] oldPopulation;
    private int[][] newPopulation;//新的种群，子代种群
    private double[] fitness;//种群适应度，表示种群中各个个体的适应度
    private int[] fitM;
    
    private double[] Pi;//种群中各个个体的累计概率
    private double Pc;//交叉概率
    private double Pm;//变异概率
    private int t;//当前代数
    private int length;
    
    private ArrayList<Service> serviceArrayList = new ArrayList<>();//代表可供选择的云服务
    static ArrayList<ArrayList<Service>> serviceListList = new ArrayList<ArrayList<Service>>();
    static ArrayList<ArrayList<Service>> noListList = new ArrayList();
    static ArrayList<Service> list = new ArrayList<>();
    
    static ArrayList<info> infos = new ArrayList<>();
    
    private Random random;
    
    public GA() {
    }
    
    /**
     * constructor of GA
     *
     * @param scale      种群规模
     * @param serviceNum 服务商的数量
     * @param MAX_GEN    运行代数
     * @param pc         交叉率
     * @param pm         变异率
     * @param list       可供选择服务商链表
     */
    public GA(int scale, int serviceNum, int MAX_GEN, double pc, double pm, ArrayList<Service> list) {
        this.scale = scale;
        this.serviceNum = serviceNum;
        this.MAX_GEN = MAX_GEN;
        this.Pc = pc;
        this.Pm = pm;
        this.serviceArrayList = list;
        this.length = list.size();
        this.fitM = new int[scale];
    }
    
    /**
     * 初始化GA算法类
     */
    private void init() {
        bestQOS = Double.MIN_VALUE;
        bestTour = new int[serviceNum];
        bestT = 0;
        t = 0;
        newPopulation = new int[scale][serviceNum];
        oldPopulation = new int[scale][serviceNum];
        fitness = new double[scale];
        Pi = new double[scale];
        random = new Random(System.currentTimeMillis());
    }
    
    /**
     * 初始化种群
     */
    void initGroup() {
        int i, j, k;
        for (k = 0; k < scale; k++) {//种群数量
            oldPopulation[k][0] = random.nextInt(65535) % length;
            for (i = 1; i < serviceNum; ) {//染色体长度
                oldPopulation[k][i] = random.nextInt(65535) % length;
                for (j = 0; j < i; j++) {
                    if (oldPopulation[k][i] == oldPopulation[k][j]) {
                        break;
                    }
                }
                if (j == i) {
                    i++;
                }
            }
        }
        /*for(i = 0;i<scale;i++){
            for(j = 0;j<serviceNum;j++){
                System.out.print(oldPopulation[i][j]+",");
            }
            System.out.println();
        }*/
    }
    
    /**
     * 计算种群适应度，在这个问题中即每个种群的QoS值
     *
     * @param chromosome
     * @return
     */
    public double[] evaluate(int[] chromosome) {
        double best_QoS = Double.MIN_VALUE;
        int bestM = 0;
        int length = chromosome.length;
        ArrayList<Service> serList = new ArrayList<>();//个体选择的云服务
        Service[] serArr = new Service[length];
        for (int i = 0; i < length; i++) {
            serList.add(serviceArrayList.get(chromosome[i]));
            serArr[i] = serviceArrayList.get(chromosome[i]);
        }
        Map<Integer, ArrayList<ArrayList<Service>>> selectServiceMap = new HashMap<>();
        Map<Integer, ArrayList<ArrayList<Service>>> _selectServiceMap = new HashMap<>();
        for (int m = 1; m <= serList.size(); m++) {
            serviceListList = new ArrayList<>();//
            noListList = new ArrayList<>();
            combiantion(m, serArr);
            for (int i = 0; i < serviceListList.size(); i++) {
                ArrayList<Service> tempList = new ArrayList();
                tempList.addAll(serList);
                tempList.removeAll(serviceListList.get(i));
                noListList.add(tempList);
            }
            selectServiceMap.put(m, serviceListList);
            _selectServiceMap.put(m, noListList);
        }
        for (int m = 1; m <= length; m++) {
            double sum_ava = 0.0;
            double ava = 0.0;
            for (int k = m; k <= length; k++) {
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
            
            if (sum_ava >= 0.99) {
                double qos = 0.0;
                double total_price = 0.0;
                double storage_price = 0.0;
                double out_price = 0.0;
                _bubbleSort(serArr);//按照带宽价格升序排序
                double unit = 2000 / m;//2000G原始数据分成m份
                for (int i = 0; i < length; i++) {//存储价格
                    storage_price += unit * serList.get(i).getPricePerGB();
                }
                for (int i = 0; i < m; i++) {//带宽价格+operation价格
                    out_price += (serArr[i].getPriceOutbandwidth() * 2 * unit
                            + (1 / m) * 2 * serArr[i].getPricePer10kGet());
                }
                total_price = storage_price + out_price;
                qos = 0.2 * sum_ava + 0.8 / total_price;
                if (qos > best_QoS) {
                    best_QoS = qos;
                    bestM = m;
                }
            }
        }
        double[] a = new double[2];
        a[0] = best_QoS;
        a[1] = bestM;
        return a;
    }
    
    /**
     * 计算种群中各个个体的累计概率，前提是已经计算出各个个体的适应度即QoS--fitness[],作为轮盘赌选择策略一部分，Pi[]
     *
     * @param
     */
    void countRate() {
        int k;
        double sumFitness = 0.0;
        double[] tempf = new double[scale];
        
        for (k = 0; k < scale; k++) {
            tempf[k] = 10.0 / fitness[k];
            sumFitness += tempf[k];
        }
        
        Pi[0] = (tempf[0] / sumFitness);
        for (k = 1; k < scale; k++) {
            Pi[k] = (tempf[k] / sumFitness + Pi[k - 1]);
        }
    }
    
    //挑选某代种群中适应度最高的个体，直接复制到子代中，精英保留策略
    public void selectBestGh() {
        int k, i, maxid;
        double maxevaluation;
        
        maxid = 0;
        maxevaluation = fitness[0];
        for (k = 1; k < scale; k++) {
            if (maxevaluation < fitness[k]) {
                maxevaluation = fitness[k];
                maxid = k;
            }
        }
        
        if (bestQOS < maxevaluation) {
            bestQOS = maxevaluation;
            bestT = t;
            best_M = fitM[maxid];
            for (i = 0; i < serviceNum; i++) {
                bestTour[i] = oldPopulation[maxid][i];
            }
        }
        //复制染色体，k表示新染色体在种群中的位置，kk表示旧染色体在种群的位置
        copyGh(0, maxid);
    }
    
    //复制染色体，k表示新染色体在种群中的位置，kk表示旧染色体在种群的位置
    public void copyGh(int k, int kk) {
        int i;
        for (i = 0; i < serviceNum; i++) {
            newPopulation[k][i] = oldPopulation[kk][i];
        }
    }
    
    //轮盘赌选择策略挑选
    public void select() {
        int k, i, selectId;
        double ran1;
        for (k = 1; k < scale; k++) {
            ran1 = (random.nextInt(65535) % 1000 / 1000.0);
            //产生方式
            for (i = 0; i < scale; i++) {
                if (ran1 <= Pi[i]) {
                    break;
                }
            }
            selectId = i;
            copyGh(k, selectId);
        }
    }
    
    /**
     * 进化函数，保留最好的染色体不进行交叉变异
     */
    public void evolution() {
        int k;
        //挑选某代种群中适应度最高的个体
        selectBestGh();
        //轮盘赌选择策略挑选scale-1个下一代个体
        select();
        
        double r;
        for (k = 1; k + 1 < scale / 2; k = k + 2) {
            r = random.nextDouble();//产生概率
            if (r < Pc) {
                OXCross(k, k + 1);//进行交叉
            } else {
                r = random.nextDouble();
                if (r < Pm) {
                    OnCVariation(k);//进行变异
                }
                r = random.nextDouble();
                if (r < Pm) {
                    OnCVariation(k + 1);
                }
            }
        }
        if (k == scale / 2 - 1) {//剩最后一个染色体没有交叉
            r = random.nextDouble();
            if (r < Pm) {
                OnCVariation(k);
            }
        }
    }
    
    /**
     * 交叉算子，相同染色体交叉产生不同子代染色体
     *
     * @param k1
     * @param k2
     */
    public void OXCross(int k1, int k2) {
        int i, j, k, flag;
        int ran1, ran2, temp;
        int[] Gh1 = new int[serviceNum];
        int[] Gh2 = new int[serviceNum];
        
        ran1 = random.nextInt(65535) % serviceNum;
        ran2 = random.nextInt(65535) % serviceNum;
        while (ran1 == ran2) {
            ran2 = random.nextInt(65535) % serviceNum;
        }
        if (ran1 > ran2) {//确保ran1<ran2
            temp = ran1;
            ran1 = ran2;
            ran2 = temp;
        }
        
        //将染色体1中的第三部分移到染色体2的首部
        for (i = 0, j = ran2; j < serviceNum; i++, j++) {
            Gh2[i] = newPopulation[k1][j];
        }
        flag = i;//染色体2源基因开始位置
        for (k = 0, j = flag; j < serviceNum; ) {
            Gh2[j] = newPopulation[k2][k++];
            for (i = 0; i < flag; i++) {
                if (Gh2[i] == Gh2[j]) {
                    break;
                }
            }
            if (i == flag) {
                j++;
            }
        }
        
        flag = ran1;
        for (k = 0, j = 0; k < serviceNum; ) {
            Gh1[j] = newPopulation[k1][k++];
            for (i = 0; i < flag; i++) {
                if (newPopulation[k2][i] == Gh1[j]) {
                    break;
                }
            }
            if (i == flag) {
                j++;
            }
        }
        flag = serviceNum - ran1;
        for (i = 0, j = flag; j < serviceNum; i++, j++) {
            Gh1[j] = newPopulation[k2][i];
        }
        
        for (i = 0; i < serviceNum; i++) {
            newPopulation[k1][i] = Gh1[i];//交叉完毕放回到种群
            newPopulation[k2][i] = Gh2[i];
        }
    }
    
    /**
     * 多次对换变异算子
     *
     * @param k
     */
    public void OnCVariation(int k) {
        int ran1, ran2, temp;
        int count;//对换的次数
        
        count = random.nextInt(65535) % serviceNum;
        
        for (int i = 0; i < count; i++) {
            ran1 = random.nextInt(65535) % serviceNum;
            ran2 = random.nextInt(65535) % serviceNum;
            while (ran1 == ran2) {
                ran2 = random.nextInt(65535) % serviceNum;
            }
            temp = newPopulation[k][ran1];
            newPopulation[k][ran1] = newPopulation[k][ran2];
            newPopulation[k][ran2] = temp;
        }
    }
    
    /**
     * solve函数
     */
    public void solve() {
        int i, k;
        
        //初始化种群
        initGroup();
        //计算初始化种群适应度，Fitness[max]
        for (k = 0; k < scale; k++) {
            fitness[k] = evaluate(oldPopulation[k])[0];
            fitM[k]=(int)(evaluate(oldPopulation[k])[1]);
        }
        //计算初始化种群中各个个体的累计概率，Pi[max]
        countRate();
        System.out.println("初始化种群...");
        for (k = 0; k < scale; k++) {
            for (i = 0; i < serviceNum; i++) {
                System.out.print(oldPopulation[k][i] + ",");
            }
            System.out.println();
            System.out.println("----" + fitness[k] + " " + Pi[k]);
        }
        
        for (t = 0; t < MAX_GEN; t++) {
            evolution();
            //将新种群newGroup复制到旧种群oldGroup中，准备下一代进化
            for (k = 0; k < scale; k++) {
                for (i = 0; i < serviceNum; i++) {
                    oldPopulation[k][i] = newPopulation[k][i];
                }
            }
            //计算种群适应度
            for (k = 0; k < scale; k++) {
                fitness[k] = evaluate(oldPopulation[k])[0];
                fitM[k]=(int)evaluate(oldPopulation[k])[1];
            }
            countRate();
        }
        System.out.println("最后种群...");
        for (k = 0; k < scale; k++) {
            for (i = 0; i < serviceNum; i++) {
                System.out.print(oldPopulation[k][i] + ",");
            }
            System.out.println();
            System.out.println("----" + fitness[k] + " " + Pi[k]);
        }
        info info = new info();
        System.out.println("最佳长度出现代数： ");
        System.out.println(bestT);
        System.out.println("最佳QoS");
        System.out.println(bestQOS);
        System.out.println("最佳m");
        System.out.println(best_M);
        info.setBestqs(bestQOS);
        System.out.println("最佳路径：");
        
        for (i = 0; i < serviceNum; i++) {
            info.getBestt()[i] = bestTour[i];
            System.out.print(bestTour[i] + ",");
        }
        infos.add(info);
    }
    
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
            if (count == 13)
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
        System.out.println("start...");
        
        for (int m = 2; m <= 10; m++) {
            System.out.println("------------------------------------");
            System.out.println("mmmmmm   " + m);
            GA ga = new GA(200, m, 2000, 0.1, 0.1, serviceList);
            ga.init();
            ga.solve();
            System.out.println("-------------------------------------");
        }
        double best = Double.MIN_VALUE;
        ArrayList<Integer> index = new ArrayList<>();
        for (info i : infos) {
            int l;
            if (i.getBestqs() > best) {
                index = new ArrayList<>();
                best = i.getBestqs();
                for (int q = 0; q < i.getBestt().length; q++)
                    index.add(i.getBestt()[q]);
            }
        }
        System.out.println("======================================");
        System.out.println("最佳QoS");
        System.out.println(best);
        System.out.println("最佳路径：");
        for (int i = 0; i < index.size(); i++) {
            System.out.print(index.get(i) + ",");
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
    
    /**
     * 按照outBandwidthPrice价格进行升序排序
     */
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
    
}

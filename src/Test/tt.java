package Test;

import java.io.*;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class tt {
    public static void WriteFile(ArrayList<String> list) throws IOException {
        File file = new File("D:/temp.txt");
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
        for (String s : list) {
            bufferedWriter.write(s);
        }
        bufferedWriter.flush();
        bufferedWriter.close();
    }

    public static void main(String args[]) throws IOException {
        int popSize = 10;
        int length = 35;
        int numService = 5;
        int j;
        for (int k = 0; k < popSize; k++) {//Random random  = new Random(System.currentTimeMillis())种群数量
            Random random = new Random(System.currentTimeMillis());
            int[] binChrom = new int[numService];
            binChrom[0] = random.nextInt(65535) % length;
            for (int i = 1; i < numService; ) {//染色体长度
                binChrom[i] = random.nextInt(65535) % length;
                for (j = 0; j < i; j++) {
                    if (binChrom[i] == binChrom[j]) {
                        break;
                    }
                }
                if (j == i) {
                    i++;
                }
            }
            System.out.println(Arrays.toString(binChrom));
        }
//        int[] a = {2, 27, 5};
//        int[] b = {1, 24, 34};
//
//        Random random = new Random(System.currentTimeMillis());
//        int[] temp1 = new int[3];
//        int[] temp2 = new int[3];
//        System.arraycopy(a, 0, temp1, 0, 3);
//        System.arraycopy(b, 0, temp2, 0, 3);
//        int point1 = random.nextInt(3);// 交叉点
//        int point2 = random.nextInt(3);// 交叉点
//        while (point1 == point2) {
//            point2 = random.nextInt(3);
//        }
//        int temp;
//        if (point1 > point2) {//确保ran1<ran2
//            temp = point1;
//            point1 = point2;
//            point2 = temp;
//        }
//        point1 = 0;
//        point2 = 1;
//        int ww = 0;
//        for (int i = 0; i < point1; i++) {
//            for (int j = ww; j < 3; j++) {
//                int flag = 1;
//                for (int k = point1; k <= point2; ) {
//                    if (b[j] == a[k]) {
//                        flag = 0;
//                        break;
//                    }
//                    k++;
//                }
//                if (flag == 1) {
//                    a[i] = b[j];
//                    ww = j + 1;
//                    break;
//                }
//            }
//        }
//        for (int i = point2 + 1; i < 3; i++) {
//            for (int j = ww; j < 3; j++) {
//                int flag = 1;
//                for (int k = point1; k <= point2; ) {
//                    if (b[j] == a[k]) {
//                        flag = 0;
//                        break;
//                    }
//                    k++;
//                }
//                if (flag == 1) {
//                    a[i] = b[j];
//                    ww = j + 1;
//                    break;
//                }
//            }
//        }
//
//        ww = 0;
//        for (int i = 0; i < point1; i++) {
//            for (int j = ww; j < 3; j++) {
//                int flag = 1;
//                for (int k = point1; k <= point2; ) {
//                    if (temp1[j] == b[k]) {
//                        flag = 0;
//                        break;
//                    }
//                    k++;
//                }
//                if (flag == 1) {
//                    b[i] = temp1[j];
//                    ww = j + 1;
//                    break;
//                }
//            }
//        }
//        for (int i = point2 + 1; i < 3; i++) {
//            for (int j = ww; j < 3; j++) {
//                int flag = 1;
//                for (int k = point1; k <= point2; ) {
//                    if (temp1[j] == b[k]) {
//                        flag = 0;
//                        break;
//                    }
//                    k++;
//                }
//                if (flag == 1) {
//                    b[i] = temp1[j];
//                    ww = j + 1;
//                    break;
//                }
//            }
//        }
//        System.out.println(Arrays.toString(a));
//        System.out.println(Arrays.toString(b));

    }
}

class www {
    private int a;
    private int b;
    private int[] c;

    public www(int a, int b, int[] c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public int[] getC() {
        return c;
    }

    public static void sort(int[] a) {
        int temp;
        int size = a.length;
        for (int i = 0; i < size - 1; i++) {
            for (int j = i + 1; j < size; j++) {
                if (a[i] > a[j]) { // 交换两数的位置
                    temp = a[i];
                    a[i] = a[j];
                    a[j] = temp;
                }
            }
        }
    }
}
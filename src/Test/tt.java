package Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class tt {
    public static void WriteFile(ArrayList<String> list) throws IOException {
        File file = new File("D:/temp.txt");
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
       for(String s:list){
           bufferedWriter.write(s);
       }
        bufferedWriter.flush();
        bufferedWriter.close();
    }
    public static void main(String args[]) throws IOException {
        ArrayList<String> list = new ArrayList<>();
        list.add("22111");
        list.add("2");
        list.add("222");
        WriteFile(list);
    }
}
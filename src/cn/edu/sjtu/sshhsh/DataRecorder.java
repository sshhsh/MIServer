package cn.edu.sjtu.sshhsh;

import io.netty.buffer.ByteBuf;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

public class DataRecorder {
    private Map<String, Node> nodes;
    private String time;
    private class Node{
        String ID;
        int tmp;
        boolean isfirst;
        long count;
        int count10;
        long previousTime;
        FileWriter writer;
        Node(){
            ID = "null";
            isfirst = true;
            count = 0;
            count10 = 0;
            previousTime = System.currentTimeMillis();
        }
    }

    public DataRecorder(){
        if(nodes==null) {
            nodes = new Hashtable<>();
        }
        if(time==null) {
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HH.mm.ss");
            time = format.format(new Date()) + "z50_3";
            File file = new File(time);
            if (!file.exists()) {//如果文件夹不存在
                file.mkdir();//创建文件夹
            }
        }
    }

    public void getData(String clientIP, ByteBuf b){
        if(!nodes.containsKey(clientIP)){
            Node n = new Node();
            nodes.put(clientIP, n);
        }
        Node current = nodes.get(clientIP);

        try {
            while (b.isReadable()){
                if(current.isfirst){
                    current.tmp = b.readByte();
//                    System.out.print(current.tmp);
//                    System.out.print("  ");
                    if((current.tmp&1)==0){
                        System.out.println(clientIP + ": First packet loss!");
                        continue;
                    }
                    current.isfirst = false;
                }else {
                    byte second = b.readByte();
                    if((second&1)!=0){
                        current.tmp = second;
                        System.out.println(clientIP + ": Second packet loss!");
                        continue;
                    }
//                    int res = (current.tmp<<4)|((second>>4)&15);
                    int res = (((current.tmp<<6)&(~127))|(second>>1)&127);
//                    System.out.print(second>>4);
//                    System.out.print("  ");
//                    System.out.println(res);
//                    System.out.print('\n');
                    current.isfirst = true;

//                    int id = second&15;
                    if(current.writer==null){
                        current.writer=new FileWriter(time + '/' + clientIP);
                        current.ID = clientIP;
                    }
                    if(!current.ID.equals(clientIP)){
                        System.out.println("WRONG ID");
                    }else {
                        current.writer.write(Integer.toString(res) + ',');
                        current.count++;
                        current.count10++;
                        if(current.count10 >= 10){
                            current.writer.write('\n');
                            current.count10 = 0;
                        }
                    }
                }

                long currentTime = System.currentTimeMillis();
                if(currentTime-current.previousTime>1000){
                    System.out.print(current.ID);
                    System.out.print(' ');
                    System.out.println(current.count);
                    current.previousTime = currentTime;
                    current.count = 0;
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        for (String key : nodes.keySet()) {
            if(nodes.get(key).writer!=null) {
                nodes.get(key).writer.close();
            }
        }
    }
}

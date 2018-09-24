package cn.edu.sjtu.sshhsh;

import io.netty.buffer.ByteBuf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ShortBuffer;
import java.util.Hashtable;
import java.util.Map;

public class RealTimeClassifier {
    private Map<String, Node> nodes;
    private final static int BUFSIZE = 5000;
    long startTime;
    private class Node{
        String ID;
        int tmp;
        boolean isfirst;
        long count;
        long count2;
        long previousTime;
        int send;
        short[] data;
        ShortBuffer dataBuf;
        Node(){
            ID = "null";
            isfirst = true;
            count = 0;
            count2 = 0;
            send = 0;
            data = new short[BUFSIZE];
            dataBuf = ShortBuffer.allocate(BUFSIZE*2);
            dataBuf.clear();
            previousTime = System.currentTimeMillis();
        }
    }

    public RealTimeClassifier(){
        if(nodes==null) {
            nodes = new Hashtable<>();
        }

        startTime=System.currentTimeMillis();
    }

    public void getData(String clientIP, ByteBuf b){
        if(!nodes.containsKey(clientIP)){
            Node n = new Node();
            nodes.put(clientIP, n);
        }
        Node current = nodes.get(clientIP);
        if(current.count2>=BUFSIZE){
            System.out.println("release");
            return;
        }
        while (b.isReadable()){
            if(current.isfirst){
                current.tmp = b.readByte();
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
                int res = (((current.tmp<<6)&(~127))|(second>>1)&127);
                current.isfirst = true;

                current.count++;
                current.count2++;
                if(current.count2 <= BUFSIZE)
                    current.dataBuf.put((short) res);
                if(current.count2 == BUFSIZE){
                    while (b.isReadable()){
                        b.readByte();
                    }
                    current.dataBuf.flip();
                    current.dataBuf.get(current.data, 0, BUFSIZE);
                    current.dataBuf.clear();
                    current.send++;
                    if(current.send>=1) {
                        current.send=0;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                sendPost("http://127.0.0.1:5000/classify", clientIP, current.data);
                            }
                        }).run();
//                        sendPost("http://127.0.0.1:5000/classify", clientIP, current.data);
                    }
                    current.count2 = 0;
                }
            }

            if(current.ID.equals("null")){
                current.ID = clientIP;
            }
            if(!current.ID.equals(clientIP)) {
                System.out.println("WRONG ID");
            }

            long currentTime = System.currentTimeMillis();
            if(currentTime-current.previousTime>1000){
                System.out.print(current.ID);
                System.out.print(' ');
                System.out.print(current.count2);

                System.out.print(' ');
                System.out.print(current.count + " run time: ");
                System.out.println((currentTime - startTime)/1000);
                current.previousTime = currentTime;
                current.count = 0;
            }
        }
    }

    public static String sendPost(String url, String from, short[] data) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
//            // 设置通用的请求属性
//            conn.setRequestProperty("accept", "*/*");
//            conn.setRequestProperty("connection", "Keep-Alive");
//            conn.setRequestProperty("user-agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("content-type", "text/plain");
            conn.setRequestProperty("who", from);
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            //1.获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            //2.中文有乱码的需要将PrintWriter改为如下
            //out=new OutputStreamWriter(conn.getOutputStream(),"UTF-8")
            // 发送请求参数
            for(short d : data){
                out.println(d);
            }
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！"+e);
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        System.out.println("post推送结果："+result);
        return result;
    }
}

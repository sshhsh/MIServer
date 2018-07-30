package cn.edu.sjtu.sshhsh;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

public class DiscardServerHandler extends ChannelInboundHandlerAdapter { // (1)
    static private Map<String, Node> nodes;
    static private String time;

    private class Node{
        int ID;
        int tmp;
        boolean isfirst;
        long count;
        long previousTime;
        FileWriter writer;
        Node(){
            ID = -1;
            isfirst = true;
            count = 0;
            previousTime = System.currentTimeMillis();
        }
    }

    public DiscardServerHandler(){
        if(nodes==null) {
            nodes = new Hashtable<>();
        }
        if(time==null) {
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HH.mm.ss");
            time = format.format(new Date());
            File file = new File(time);
            if (!file.exists()) {//如果文件夹不存在
                file.mkdir();//创建文件夹
            }
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) { // (2)
        try {
            InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
            String clientIP = insocket.getAddress().getHostAddress();
            if(!nodes.containsKey(clientIP)){
                Node n = new Node();
                nodes.put(clientIP, n);
            }
            Node current = nodes.get(clientIP);

//            System.out.println(clientIP);
            // Do something with msg
            ByteBuf b = (ByteBuf) msg;
            while (b.isReadable()){
                if(current.isfirst){
                    current.tmp = b.readByte();
//                    System.out.print(current.tmp);
//                    System.out.print("  ");
                    current.isfirst = false;
                }else {
                    byte second = b.readByte();
                    int res = (current.tmp<<4)|((second>>4)&15);
//                    System.out.print(second>>4);
//                    System.out.print("  ");
//                    System.out.println(res);
//                    System.out.print('\n');
                    current.isfirst = true;

                    int id = second&15;
                    if(current.writer==null){
                        current.writer=new FileWriter(time + '/' + Integer.toString(id));
                        current.ID = id;
                    }
                    if(current.ID!=id){
                        System.out.println("WRONG ID");
                    }else {
                        current.writer.write(Integer.toString(res) + ',');
                        current.count++;
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
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        // 当出现异常就关闭连接
        cause.printStackTrace();
        ctx.close();
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
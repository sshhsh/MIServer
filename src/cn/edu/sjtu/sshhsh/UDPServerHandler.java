package cn.edu.sjtu.sshhsh;

import io.netty.buffer.ByteBuf;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.ChannelHandlerContext;


public class UDPServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {
//    static private DataRecorder dataRecorder;
    static private RealTimeClassifier dataRecorder;

    public UDPServerHandler(){
        if(dataRecorder == null) {
//            dataRecorder = new DataRecorder();
            dataRecorder = new RealTimeClassifier();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet)
            throws Exception
    {
        String clientIP = packet.sender().getAddress().getHostAddress();

        ByteBuf buf = (ByteBuf) packet.copy().content();
        dataRecorder.getData(clientIP, buf);

        buf.release();
    }
}

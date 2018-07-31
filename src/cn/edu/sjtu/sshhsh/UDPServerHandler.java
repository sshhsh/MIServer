package cn.edu.sjtu.sshhsh;

import io.netty.buffer.ByteBuf;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;

public class UDPServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    static private DataRecorder dataRecorder;

    public UDPServerHandler(){
        if(dataRecorder == null)
            dataRecorder = new DataRecorder();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet)
            throws Exception
    {
        InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
        String clientIP = insocket.getAddress().getHostAddress();

        ByteBuf buf = (ByteBuf) packet.copy().content();
        dataRecorder.getData(clientIP, buf);
    }
}

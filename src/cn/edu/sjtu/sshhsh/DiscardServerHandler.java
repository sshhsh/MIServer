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
    static private DataRecorder dataRecorder;

    public DiscardServerHandler(){
        if(dataRecorder==null)
            dataRecorder = new DataRecorder();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) { // (2)
        try {
            InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
            String clientIP = insocket.getAddress().getHostAddress();

            ByteBuf b = (ByteBuf) msg;
            dataRecorder.getData(clientIP, b);

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
}
package cn.edu.sjtu.sshhsh;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import java.net.InetSocketAddress;

public class DiscardServerHandler extends ChannelInboundHandlerAdapter { // (1)
    static private RealTimeClassifier dataRecorder;

    public DiscardServerHandler(){
        if(dataRecorder==null)
            dataRecorder = new RealTimeClassifier();
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
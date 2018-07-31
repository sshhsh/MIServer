package cn.edu.sjtu.sshhsh;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

public class UDPServer {
    private int port;

    public UDPServer(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        Bootstrap b = new Bootstrap();
        EventLoopGroup group = new NioEventLoopGroup();
        b.group(group)
                .channel(NioDatagramChannel.class)
                .handler(new UDPServerHandler());
        b.bind(port).sync().channel().closeFuture().await();
    }
}

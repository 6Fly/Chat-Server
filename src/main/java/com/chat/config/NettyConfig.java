package com.chat.config;

import com.chat.bean.ImNode;
import com.chat.handle.NettyServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.InetSocketAddress;

/**
 * @author L
 */
@Slf4j
@Component
public class NettyConfig implements InitializingBean {

    @Resource
    private NettyProperties nettyProperties;

    private final NioEventLoopGroup boss = new NioEventLoopGroup();

    private final NioEventLoopGroup worker = new NioEventLoopGroup();



    public void nettyStart(){
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(boss,worker)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(nettyProperties.getPort()))
                    //初始化服务器连接队列
                    .option(ChannelOption.SO_BACKLOG,1024)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new NettyServerHandler());
                        }
                    });
            ChannelFuture future = serverBootstrap.bind().sync();
            if (future.isSuccess()){
                log.info("===netty start success!===");
            }
        } catch (InterruptedException e) {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
            e.printStackTrace();
        }

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        nettyStart();
        ServiceRegistry serviceRegistry = new ServiceRegistry("127.0.0.1:2181");
        String registerAddr = "192.168.10.14:"+nettyProperties.getPort();
        serviceRegistry.register(registerAddr);
        log.info("netty registry success:{}",registerAddr);
    }
}

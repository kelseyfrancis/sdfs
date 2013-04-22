package sdfs.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sdfs.store.Store;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

class ServerInitializer extends ChannelInitializer<SocketChannel> {

    private static final Logger log = LoggerFactory.getLogger(ServerInitializer.class);

    private final SSLContext sslContext;
    private final Store store;

    public ServerInitializer(SSLContext sslContext, Store store) {
        this.sslContext = sslContext;
        this.store = store;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        SSLEngine engine = sslContext.createSSLEngine();
        engine.setUseClientMode(false);
        engine.setNeedClientAuth(true);

        pipeline.addLast(new SslHandler(engine));

//        pipeline.addLast(new SnappyFramedDecoder());
//        pipeline.addLast(new SnappyFramedEncoder());
        pipeline.addLast(new ChunkedWriteHandler());
        pipeline.addLast(new ServerHandler(store));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        try {
            throw cause;
        } catch (Throwable e) {
            log.error("Could not initialize server channel.", e);
        }
        ctx.close();
    }
}
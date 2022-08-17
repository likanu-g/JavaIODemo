package org.example.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Server {
    private static final int DEFAULT_PORT = 3333;
    private final int port;

    public Server(int port) {
        this.port = port;
    }

    public Server() {
        this(DEFAULT_PORT);
    }

    public void start() throws IOException, ExecutionException, InterruptedException {
        AsynchronousServerSocketChannel serverSocketChannel = AsynchronousServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(port));
        Future<AsynchronousSocketChannel> accept;
        while (true) {
            accept = serverSocketChannel.accept();
            AsynchronousSocketChannel socketChannel = accept.get();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            socketChannel.read(buffer, buffer, new ReadHandler(socketChannel));
        }
    }

    static class  ReadHandler implements CompletionHandler<Integer, ByteBuffer> {
        private AsynchronousSocketChannel channel;

        public ReadHandler(AsynchronousSocketChannel channel) {
            this.channel = channel;
        }

        @Override
        public void completed(Integer result, ByteBuffer msg) {
            String body = new String(msg.array(), 0, result);
            System.out.println("server received data: " + body);
            ByteBuffer sendBuffer = ByteBuffer.allocate(1024);
            sendBuffer.clear();
            String sendCount = "Hello client";
            sendBuffer.flip();
            Future<Integer> write = channel.write(sendBuffer);
            while (!write.isDone()) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("response success.");
        }

        @Override
        public void failed(Throwable exc, ByteBuffer attachment) {

        }
    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        Server server = new Server();
        server.start();
    }

}

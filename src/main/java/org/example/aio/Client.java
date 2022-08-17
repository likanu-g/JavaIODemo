package org.example.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Client {
    private final String serverHost;
    private final int serverPort;
    private AsynchronousSocketChannel socketChannel;

    public Client(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    public void connect() throws IOException {
        socketChannel = AsynchronousSocketChannel.open();
        Future<Void> connect = socketChannel.connect(new InetSocketAddress(serverHost, serverPort));
        while (!connect.isDone()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendMsg() throws ExecutionException, InterruptedException {
        ByteBuffer sendBuffer = ByteBuffer.allocate(1024);
        sendBuffer.clear();
        String sendContent = "Hello server";
        sendBuffer.put(sendContent.getBytes(StandardCharsets.UTF_8));
        sendBuffer.flip();
        Future<Integer> write = socketChannel.write(sendBuffer);
        while (!write.isDone()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        Future<Integer> read = socketChannel.read(buffer);
        while (!read.isDone()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("client received data from server: " + new String(buffer.array(), 0, read.get()));
    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        Client client = new Client("127.0.0.1", 3333);
        client.connect();
        client.sendMsg();
    }
}

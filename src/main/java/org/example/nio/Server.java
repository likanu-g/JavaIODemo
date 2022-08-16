package org.example.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class Server {
    private Selector selector;
    private static final int DEFAULT_PORT = 3333;
    private final int port;


    public Server(int port) {
        this.port = port;
    }
    public Server(){
        this(DEFAULT_PORT);
    }

    public void start() throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        ServerSocket serverSocket = serverSocketChannel.socket();
        serverSocket.bind(new InetSocketAddress(port));
        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        while (true) {
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> selectionKeyIterator = selectionKeys.iterator();
            while (selectionKeyIterator.hasNext()) {
                SelectionKey selectionKey = selectionKeyIterator.next();
                selectionKeyIterator.remove();
                handleEven(selectionKey);
            }
        }

    }

    private void handleEven(SelectionKey selectionKey) throws IOException {
        SocketChannel client;
        if(selectionKey.isAcceptable()) {
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
            client = serverSocketChannel.accept();
            if(null == client) {
                return;
            }
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);
        }else if(selectionKey.isReadable()) {
            client = (SocketChannel) selectionKey.channel();
            ByteBuffer receiveBuffer = ByteBuffer.allocate(1024);
            receiveBuffer.clear();
            int count = client.read(receiveBuffer);
            if(count > 0) {
                String receiveContext = new String(receiveBuffer.array(), 0, count);
                System.out.println("receive client msg: " + receiveContext);
            }
            ByteBuffer sendBuffer = ByteBuffer.allocate(1024);
            sendBuffer.clear();
            client = (SocketChannel) selectionKey.channel();
            String sendContent = "Hello Client.";
            sendBuffer.put(sendContent.getBytes(StandardCharsets.UTF_8));
            sendBuffer.flip();
            client.write(sendBuffer);
            System.out.println("send msg to client: " + sendContent);
        }
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.start();
    }
}

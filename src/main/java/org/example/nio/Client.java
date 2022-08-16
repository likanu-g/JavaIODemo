package org.example.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    private final String serverHost;
    private final int serverPort;
    private Selector selector;
    private SelectionKey selectionKey;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private SocketChannel client;


    public Client(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    public void connect() throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        selector = Selector.open();
        selectionKey = socketChannel.register(selector, 0);
        boolean isConnected = socketChannel.connect(new InetSocketAddress(serverHost, serverPort));
        if(!isConnected) {
            selectionKey.interestOps(SelectionKey.OP_CONNECT);
        }
        selector.select();
        Set<SelectionKey> selectionKeys = selector.selectedKeys();
        Iterator<SelectionKey> iterator = selectionKeys.iterator();
        selectionKey = iterator.next();
        iterator.remove();
        int readOps = selectionKey.readyOps();
        if((readOps & SelectionKey.OP_CONNECT) != 0) {
            client = (SocketChannel) selectionKey.channel();
        }
        executorService.execute(this::handleEven);
    }

    private void sendMsg() throws IOException {
        if(!client.finishConnect()) {
            throw new Error();
        }
        ByteBuffer sendBuffer = ByteBuffer.allocate(1024);
        sendBuffer.clear();
        sendBuffer.put("hello server.".getBytes(StandardCharsets.UTF_8));
        sendBuffer.flip();
        client.write(sendBuffer);
        if(selectionKey != null) {
            selectionKey.interestOps(SelectionKey.OP_READ);
        }
    }

    private void handleEven(){

        try {
            while (true) {
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                SocketChannel client;
                while (iterator.hasNext()) {
                    selectionKey = iterator.next();
                    iterator.remove();
                    int readOps = selectionKey.readyOps();
                    if((readOps & SelectionKey.OP_READ) != 0) {
                        client = (SocketChannel) selectionKey.channel();
                        ByteBuffer receiveBuffer = ByteBuffer.allocate(1024);
                        receiveBuffer.clear();
                        int count = client.read(receiveBuffer);
                        if(count > 0) {
                            String msg = new String(receiveBuffer.array(), 0, count);
                            System.out.println("receive msg from server:" + msg);
                        }
                    }
                }
            }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    public static void main(String[] args) throws IOException {
        Client client = new Client("127.0.0.1", 3333);
        client.connect();
        client.sendMsg();
    }
}

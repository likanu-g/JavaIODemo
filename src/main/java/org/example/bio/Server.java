package org.example.bio;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int DEFAULT_PORT = 3333;
    private final int port;

    public Server() {
        this(DEFAULT_PORT);
    }

    public Server(int port) {
        this.port = port;
    }
    public void start() throws Exception {
        //创建ServerSocket
        ServerSocket serverSocket = new ServerSocket(this.port);
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                new Thread(() -> {
                    byte[] data = new byte[1024];
                    try {
                        InputStream inputStream = socket.getInputStream();
                        while (true) {
                            int len;
                            while ((len = inputStream.read(data)) != -1) {
                                System.out.println(new String(data, 0 ,len));
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Server server = new Server();
        server.start();
    }
}

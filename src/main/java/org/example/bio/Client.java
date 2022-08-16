package org.example.bio;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class Client {
    private final String serverHost;
    private final int serverPort;
    private Socket socket;


    public Client(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }
    public void connect() {
        try {
            Socket socket = new Socket(serverHost, serverPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void request() {
        while (true) {
            if(socket == null) {
                try {
                    socket = new Socket(serverHost, serverPort);
                    socket.getOutputStream().write((new Date() + " : Hello server.").getBytes(StandardCharsets.UTF_8));
                    socket.getOutputStream().flush();
                    Thread.sleep(3000);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client("127.0.0.1", 3333);
        client.connect();
        client.request();
    }
}

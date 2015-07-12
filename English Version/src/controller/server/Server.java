package controller.server;


import model.InformationsServer;
import model.Message;

import java.io.IOException;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.concurrent.TimeUnit;

/**
 * Class server receives messages from different users and asssigns to a new user 
 * and object of class Client.java
 */
public class Server implements Runnable {
    
    private boolean disconnect = false;
    private Client[] clients;
    private ServerSocket socketserver;
    
    private Thread replyBroadcast;
    private ReceiveBroadcast receiveBroadcast;
    
    private String name;
    private int size;
    
    private InformationsServer infos;
    private String address;
    private int port;
    private String type;
    private String password;
    private String host;


    public Server(String name, String host, int size, String type, String password) throws IOException {
        if (size <= 0) {
            this.size = 25;
        } 
        else {
            this.size = size;
        }
        clients = new Client[this.size];
        this.name = name;
        this.type = type;
        this.host = host;
        this.password = password;

        socketserver = new ServerSocket(0, size, InetAddress.getLocalHost());
        InetAddress serverAddress = socketserver.getInetAddress();
        this.infos = new InformationsServer(this.name, getNumberClients(), this.size, serverAddress.getHostAddress(), 
                                            socketserver.getLocalPort(), this.host, this.type);
        this.port = socketserver.getLocalPort();
        this.address = serverAddress.toString();
        receiveBroadcast = new ReceiveBroadcast(infos, password);
        replyBroadcast = new Thread(receiveBroadcast);
        replyBroadcast.start();
    }

    /**
     * run
     */
    public void run() {
        while (!disconnect) {
            try {
                addClient(socketserver.accept());
            } catch (IOException e) {
                System.out.println("e2.1");
            }
        }
        try {
            socketserver.close();
        } catch (IOException e) {
            System.out.println("e2.2");
        }
    }

    public ServerSocket getSocketserveur() {
        return socketserver;
    }

    public void addClient(Socket socket) throws IOException {
        if (!disconnect) {
            Client client = new Client(this, socket);
            for (int i = 0; i < clients.length; i++) {
                if (clients[i] == null) {
                    clients[i] = client;
                    infos.setClients(getNumberClients());
                    Thread t = new Thread(client);
                    t.start();
                    break;
                }
            }
        }
    }

    public void sendToAll(Message message, Client without) {
        try {
            for (int i = 0; i < clients.length; i++) {
                if (clients[i] != null && clients[i] != without) {
                    clients[i].sendMessage(message);
                }
            }
        } catch (Exception e) {
            System.out.println("e3");
        }
    }

    public void sendPrivateMessage(Message message) {
        try {
            for (int i = 0; clients[i] != null; i++) {
                if (clients[i].getNom().equals(message.getReceiver())) {
                    clients[i].sendMessage(message);
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("e4");
        }
    }
    
    public void sendList(Client client) {
        try {
            for (int i = 0; i < clients.length; i++) {
                if (clients[i] != null) {
                    TimeUnit.MILLISECONDS.sleep(SLEEP);
                    client.sendMessage(new Message(Message.NEW_USER, null, clients[i].getNom(), null));
                }
            }
        } catch (Exception e) {
            System.out.println("e5.1");
        }
    }

    public void deleteClient(Client client) {
        for (int i = 0; i < clients.length; i++) {
            if (clients[i] != null && clients[i].equals(client)) {
                clients[i].setRunning(false);
                clients[i] = null;
                infos.setClients(getNumberClients());
            }
        }
    }

    public int getNumberClients() {
        int nb = 0;
        for (int i = 0; i < clients.length; i++) {
            if (clients[i] != null)
                nb++;
        }
        return nb;
    }

    public boolean contains(Client c) {
        for (int i = 0; i < clients.length; i++) {
            if (clients[i] != null && c.equals(clients[i]) && clients[i] != c)
                return true;
        }
        return false;
    }

    public void stopServer() { 
        sendToAll(new Message(4, null, null, null), null);
        disconnect = true;
        receiveBroadcast.setRunning(false);
        for (int i = 0; i < clients.length; i++) {
            if (clients[i] != null) {
                clients[i].setRunning(false);
                clients[i] = null;
            }
        }
          try {
            socketserver.close();
        } catch (IOException e) {
            System.out.println("e6");
        }

    }
   
    public InformationsServer getInfos() {
        return infos;
    }

    public String getNom() {
        return name;
    }


    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }
    
    // Constantes
    public static final int BROADCAST_PORT = 13371;
    public static final int SLEEP = 120;
}


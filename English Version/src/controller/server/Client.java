package controller.server;


import model.Message;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.net.Socket;

import java.util.concurrent.TimeUnit;

import model.Aes;

/**
 * Class Client is an object assigned to every user which is connected to the server.
 */  
public class Client implements Runnable {
    
    private Server server;
    private Socket socket;
    private PrintWriter flowExit;
    private BufferedReader flowIncomming; 
    private Aes aes = new Aes();
    
    private String name;
    private boolean running = true;
    

    public Client(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;
    }

    public void run() {
        try {
            while (running) {
                flowIncomming = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String s = flowIncomming.readLine();
                if (s != null) {
                    s = aes.decrypt(s, 1);
                    Message message = new Message(s);
                    if (message.getType() == Message.MESSAGE) {
                        server.sendToAll(message, null);
                    }
                    else if (message.getType() == Message.NEW_USER) {
                        name = message.getSender();
                        boolean aliasTaken = false;
                        while (server.contains(this)) {
                            name = name + "_";
                            aliasTaken = true;
                        }
                        server.sendToAll(new Message(Message.NEW_USER, "nouveau", name, null), this);
                        server.sendList(this);
                        TimeUnit.MILLISECONDS.sleep(200);
                        if (aliasTaken)
                            sendMessage(new Message(Message.NEW_USER, "pseudo", name, null));
                    }
                    else if (message.getType() == Message.EXIT) {
                        server.sendToAll(new Message(3, null, name, null), null);
                        server.deleteClient(this);
                        running = false;
                    } else if (message.getType() == Message.PRIVATE_MESSAGE) {
                        server.sendPrivateMessage(message);
                    } else if (message.getType() == Message.SEND_FILE) {
                        server.sendPrivateMessage(message);
                    } else if (message.getType() == Message.FILE_CANCELLED) {
                        server.sendPrivateMessage(message);
                    }
                }
            }
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
            running = false;
        }
    }

    public void sendMessage(Message message) {
        try {
            flowExit = new PrintWriter(socket.getOutputStream());
            String s=aes.encrypt(message.toString(), 1);
            flowExit.println(s);
            flowExit.flush();
            } catch (Exception e) {
            System.out.println("An error occured");
        }
    }

    public boolean equals(Client client) {
        if (name.equals(client.getNom())) {
            return true;
        } else {
            return false;
        }
    }

    public String getNom() {
        return name;
    }
    

    public void setRunning(boolean running) {
        this.running = running;
    }
}

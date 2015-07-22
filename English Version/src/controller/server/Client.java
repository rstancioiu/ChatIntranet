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
                    answerMessage(s);
                }
            }
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
            running = false;
        }
    }
    public void answerMessage(String s) {
        Message message = new Message(s);
        int typeOfMessage = message.getType();
        
        switch(typeOfMessage) {
            case Message.MESSAGE:
                server.sendToAll(message, null);
                break;
        
            case Message.NEW_USER:
                name = message.getSender();
                while (server.contains(this)) {
                    name = name + "_";
                }
                server.sendToAll(new Message(Message.NEW_USER, "new", name, null), this);
                server.sendList(this);
                sendMessage(new Message(Message.NEW_USER, "pseudo", name, null));
                break;
        
            case Message.EXIT: 
                server.sendToAll(new Message(3, null, name, null), null);
                server.deleteClient(this);
                running = false;
                break;
        
            case Message.PRIVATE_MESSAGE:
                server.sendPrivateMessage(message);
                break;
        
            case Message.SEND_FILE:
                server.sendPrivateMessage(message);
                break;
        
            case Message.FILE_CANCELLED:
                server.sendPrivateMessage(message);
                break;
        
            default:
                break;
        }
    }

    public void sendMessage(Message message) {
        try {
            String s=aes.encrypt(message.toString(), 1);
            
            flowExit = new PrintWriter(socket.getOutputStream());
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

package controller.client;

import controller.server.Server;

import model.InformationsServer;

import view.Window;


import java.io.IOException;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import java.net.InetAddress;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import java.util.concurrent.TimeUnit;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.SwingUtilities;

import model.Aes;

import view.ServerList;

/**
 * Broadcaster class sends broadcasts and the password from the user to the server
 * @see model.InformationsServer
 * @see view.Window
 * @see JList
 * @see DatagramPacket
 * @see DatagramSocket
 */
public class Broadcaster implements Runnable {

    private DatagramPacket packetBroadcast;
    private DatagramPacket packetReply;
    private DatagramSocket socket; 
    private Aes aes = new Aes();
    private static final String PW_ACCEPTED = "wowsuchpassword~~";
    private static final String DISCOVERY = "youwutm8~~";
    private byte[] data; 

    private Timer timer;
    private Timer timerRefreshJlist;

    private String messageReceived;

    private boolean acceptedConnection;
    private byte[] recu = new byte[4096]; 

    private DatagramPacket packetPassword; 

    private Boolean running = true;
    private Window window;
    private InformationsServer infos;
    private ServerList serverList;


    /**
     * The construct of the class
     * @param window
     * @param serverList
     */
    public Broadcaster(Window window, ServerList serverList) {
        this.window = window;
        try {
            data = (aes.encrypt(DISCOVERY,0)).getBytes();
            byte[] dataSent = new byte[4096];
            for(int i=0;i<4096;++i){
                if(i<data.length)
                    dataSent[i]=data[i];
                else dataSent[i]=0;
            }
            acceptedConnection = false;
            this.serverList = serverList;
            /* creation de la connection Socket*/
            socket = new DatagramSocket();
            socket.setBroadcast(true);
            packetBroadcast =
                new DatagramPacket(dataSent, dataSent.length, InetAddress.getByName("255.255.255.255"),
                                   Server.BROADCAST_PORT);
            /* paquet ou l'on "stocke" la reponse recue*/
            packetReply = new DatagramPacket(recu, recu.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        //timers used to send the broadcast and update the list of servers
        timer = new Timer();
        timer.schedule(new sendPacketBroadcast(), 0, 1200);
        timerRefreshJlist = new Timer();

        while (running) {
            try {
                socket.receive(packetReply);
                messageReceived = new String(packetReply.getData());
                String messageTruncated="";
                for(int i=0;i<messageReceived.length();++i) {
                    if(messageReceived.charAt(i)==0) {
                        break;
                    }
                    else messageTruncated+=messageReceived.charAt(i);;
                }
                messageReceived= aes.decrypt(messageTruncated,0);
                for (int i = 1; i < messageReceived.length(); i++) {
                    if (messageReceived.charAt(i) == '~' && messageReceived.charAt(i - 1) == '~') {
                        messageReceived = messageReceived.substring(0, i + 1);
                        break;
                    }
                }
                if (messageReceived.equals(PW_ACCEPTED)) {
                    acceptedConnection = true;
                    TimeUnit.MILLISECONDS.sleep(300);
                } else {
                    infos = new InformationsServer(messageReceived);
                    serverList.addServer(infos);
                    acceptedConnection = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("ERROR WHILE BROADCASTING");
            }
        }
    }

    /**
     * Sends a password to the server to be verified
     * @param password
     */
    public void sendPassword(String password) {
        try {
            String messageSent = password + "~~";
            byte[] data2 = (aes.encrypt(messageSent,0)).getBytes();
            byte[] dataSent = new byte[4096];
            for(int i=0;i<4096;++i){
                if(i<data2.length)
                    dataSent[i]=data2[i];
                else dataSent[i]=0;
            }
            packetPassword =
                new DatagramPacket(dataSent, dataSent.length,
                                   InetAddress.getByName(serverList.getAdresseByIndex(window.getIndexSelected())),
                                   Server.BROADCAST_PORT);
            socket.send(packetPassword);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Class which send broadcasts each X seconds 
     */
    private class sendPacketBroadcast extends TimerTask {
        public void run() {
            try {
                socket.send(packetBroadcast); // envoie de packet
            } catch (IOException e) {
                socket.close();
                System.out.println("Broadcast intterompu");
            }
        }
    }


    public boolean isAccepted() {
        return acceptedConnection;
    }

    public void setAcceptedConnection(boolean acceptedConnection) {
        this.acceptedConnection = acceptedConnection;
    }
}


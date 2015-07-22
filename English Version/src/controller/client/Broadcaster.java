package controller.client;

import controller.server.Server;

import java.io.IOException;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.swing.JList;

import model.Aes;
import model.InformationsServer;

import view.ServerList;
import view.Window;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Broadcaster class sends broadcasts and the password from the user to the server
 * @see model.InformationsServer
 * @see view.Window
 * @see JList
 * @see DatagramPacket
 * @see DatagramSocket
 */
public class Broadcaster implements Runnable {
    
    private static final Logger log = LogManager.getLogger();
    private static int SIZE_MESSAGE=4096;

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
    private byte[] recu = new byte[SIZE_MESSAGE]; 

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
        log.info("Broadcaster created");
        this.window = window;
        try {
            data = (aes.encrypt(DISCOVERY,0)).getBytes();
            byte[] dataSent = new byte[SIZE_MESSAGE];
            for(int i=0;i<SIZE_MESSAGE;++i){
                if(i<data.length)
                    dataSent[i]=data[i];
                else dataSent[i]=0;
            }
            acceptedConnection = false;
            this.serverList = serverList;
            socket = new DatagramSocket();
            socket.setBroadcast(true);
            log.info("Packet Broadcast Discovery");
            packetBroadcast =
                new DatagramPacket(dataSent, dataSent.length, InetAddress.getByName("255.255.255.255"),
                                   Server.BROADCAST_PORT);
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
                String messageDecrypted= aes.decrypt(truncateMessage(messageReceived),0);
                for (int i = 1; i < messageDecrypted.length(); i++) {
                    if (messageDecrypted.charAt(i) == '~' && messageDecrypted.charAt(i - 1) == '~') {
                        messageDecrypted = messageDecrypted.substring(0, i + 1);
                        break;
                    }
                }
                if (messageDecrypted.equals(PW_ACCEPTED)) {
                    log.info("Message PASSWORD_ACCEPTED received");
                    acceptedConnection = true;
                    TimeUnit.MILLISECONDS.sleep(300);
                } else {
                    infos = new InformationsServer(messageDecrypted);
                    log.info("Informations from " + infos.getName());
                    serverList.addServer(infos);
                    acceptedConnection = false;
                }
            } catch (Exception e) {
                log.error("ERROR WHILE BROADCASTING "+ e);
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
            byte[] dataSent = new byte[SIZE_MESSAGE];
            for(int i=0;i<SIZE_MESSAGE;++i){
                if(i<data2.length)
                    dataSent[i]=data2[i];
                else dataSent[i]=0;
            }
            packetPassword =
                new DatagramPacket(dataSent, dataSent.length,
                                   InetAddress.getByName(serverList.getAddressByIndex(window.getIndexSelected())),
                                   Server.BROADCAST_PORT);
            socket.send(packetPassword);
            log.info("Send Password to "+ serverList.getAddressByIndex(window.getIndexSelected()));
        } catch (Exception e) {
            log.error("ERROR WHEN SENDING A PASSWORD "+ e);
        }
    }


    /**
     * Class which send broadcasts each X seconds 
     */
    private class sendPacketBroadcast extends TimerTask {
        public void run() {
            try {
                log.info("packet broadcast sent");
                socket.send(packetBroadcast); // envoie de packet
            } catch (IOException e) {
                socket.close();
                log.info("Broadcast interrupted - socket closed");
            }
        }
    }

    
    public String truncateMessage(String message) {
        String messageTruncated="";
        for(int i=0;i<message.length();++i) {
            if(message.charAt(i)==0) {
                break;
            }
            else messageTruncated+=message.charAt(i);
        }
        return messageTruncated;
    }

    public boolean isAccepted() {
        return acceptedConnection;
    }

    public void setAcceptedConnection(boolean acceptedConnection) {
        this.acceptedConnection = acceptedConnection;
    }
}


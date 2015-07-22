package controller.server;


import java.io.IOException;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import model.Aes;
import model.InformationsServer;

/**
 * Class ReceiveBroadcast replies to the broadcasts sent by the different users.
 */
public class ReceiveBroadcast implements Runnable {
    
    private DatagramSocket socketUdp;
    private DatagramPacket packetReplyPassword;
    private DatagramPacket packetReplyInfos; 
    private DatagramPacket packetReceived;
    
    private byte[] dataReceived = new byte[SIZE_MESSAGE];
    private InformationsServer infos;
    private InetAddress clientAddress;
    private String password;
    private String messageReceived;
    private Aes aes=new Aes();
    private boolean running = true;
    public static final String PW_ACCEPTED = "wowsuchpassword~~";
    public static final String DISCOVERY = "youwutm8~~";

    public ReceiveBroadcast(InformationsServer infos, String password) throws IOException {
        this.infos = infos;
        this.password = password;
        socketUdp = new DatagramSocket(Server.BROADCAST_PORT); 
        socketUdp.setBroadcast(true); 
        packetReceived = new DatagramPacket(dataReceived, dataReceived.length);
    }

    public void run() {
        try {
            while (running) {
                socketUdp.receive(packetReceived); 
                clientAddress = packetReceived.getAddress(); 
                messageReceived = new String(packetReceived.getData());
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
                if (messageReceived.equals(password + "~~") &&
                    infos.getType() == "prive") {
                    String messageSent=aes.encrypt(PW_ACCEPTED,0);
                    byte[] dataSent = new byte[SIZE_MESSAGE];
                    byte[] message = messageSent.getBytes();
                    for(int i=0;i<SIZE_MESSAGE;++i){
                        if(i<message.length)
                            dataSent[i]=message[i];
                        else dataSent[i]=0;
                    }
                    packetReplyPassword =
                        new DatagramPacket(dataSent, dataSent.length,
                                           clientAddress, packetReceived.getPort());
                    socketUdp.send(packetReplyPassword);
                } else if (messageReceived.equals(DISCOVERY)) {
                    String messageSent=aes.encrypt((infos.sendData()),0);
                    byte[] dataSent = new byte[SIZE_MESSAGE];
                    byte[] message = messageSent.getBytes();
                    for(int i=0;i<SIZE_MESSAGE;++i){
                        if(i<message.length)
                            dataSent[i]=message[i];
                        else dataSent[i]=0;
                    }
                    packetReplyInfos =
                        new DatagramPacket(dataSent,
                                           dataSent.length, clientAddress,
                                           packetReceived.getPort());
                    socketUdp.send(packetReplyInfos); 
                }
            }
            socketUdp.close();
        } catch (Exception e) {
            System.out.println("ServerBroadcast timed out");
            e.printStackTrace();
            running = false;
            socketUdp.close();
        }
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public void setInfos(InformationsServer infos) {
        this.infos = infos;
    }
    
    private static int SIZE_MESSAGE=4096;
}

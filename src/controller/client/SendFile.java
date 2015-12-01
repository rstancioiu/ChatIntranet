package controller.client;

import java.awt.Toolkit;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.concurrent.TimeUnit;

import model.Language;

import view.PrivateChat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class SendFile sends the user's file to another user.
 */
public class SendFile implements Runnable {
    
    private static final Logger log = LogManager.getLogger();
    private Socket socket;
    private ServerSocket server;
    private InetAddress address;
    private int port;
    
    private File sentFile;
    private PrivateChat privateChat;
    private Language language;

    /**
     *
     * @param sentFile
     * @param privateChat
     * @param language
     */
    public SendFile(File sentFile,PrivateChat privateChat,Language language) {
        this.language=language;
        this.sentFile = sentFile;
        this.privateChat = privateChat;
        try {
            server = new ServerSocket(0, 0, InetAddress.getLocalHost());
            port = server.getLocalPort();
            address = server.getInetAddress();
            log.info("sending file "+ sentFile.getName());
        } catch (IOException e) {
            log.error("CONNEXION IMPOSSIBLE");
            privateChat.insertLine(language.getValue("CONNEXION_IMPOSSIBLE"),privateChat.getTransfer(),true);
        }
    }
   
   /**
    * 
    */
    public void run() {
        try {
            socket = server.accept();
            privateChat.insertLine(language.getValue("DOWNLOAD_OF")+" "+sentFile.getName() +" "+language.getValue("STARTED"),privateChat.getTransfer(),true);
            byte[] bytearray = new byte[(int)sentFile.length()];
            FileInputStream fin = new FileInputStream(sentFile);
            BufferedInputStream bin = new BufferedInputStream(fin);
            bin.read(bytearray, 0, bytearray.length);
            OutputStream os = socket.getOutputStream();
            os.write(bytearray, 0, bytearray.length);
            os.flush();
            TimeUnit.MILLISECONDS.sleep(300);
            socket.close();
            privateChat.insertLine(language.getValue("DOWNDLOAD_OF")+" "+sentFile.getName()+language.getValue("FINISHED"),
                                   privateChat.getTransfer(),true);
            Toolkit.getDefaultToolkit().beep();
            log.info("Download of {} finished",sentFile.getName());

        } catch (Exception e) {
            log.trace("ERROR SENDING FILE {}",e);
            privateChat.insertLine(language.getValue("ERROR_SENDING_FILE"),
                             privateChat.getTransfer(),true);
        }
        try {
            socket.close();
        } catch (IOException ioe) {
            log.error("IMPOSSIBLE TO CLOSE THE SOCKET",ioe);
        }
    }
    
    /**
     *
     * @return port
     */
    public int getPort() {
        return port;
    }
    
    /**
     *
     * @return address
     */
    public InetAddress getAddress() {
        return address;
    }
    
    
    /**
     *
     * @return file name
     */
    public String getFileName() {
        return sentFile.getName();
    }
    
    /**
     *
     * @return sizeFile
     */
    public int getSize(){
        return (int)sentFile.length();
    }
}


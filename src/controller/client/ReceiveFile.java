package controller.client;

import java.awt.Toolkit;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.InetAddress;
import java.net.Socket;

import java.util.concurrent.TimeUnit;

import javax.swing.ProgressMonitor;

import model.Language;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import view.PrivateChat;

/**
 * Class ReceiveFile receives files from an user
 */
public class ReceiveFile implements Runnable {

    private static final Logger log = LogManager.getLogger();
    private Socket socket;
    private InetAddress address;
    private int port;
    
    private int size;
    private String fileName;
    
    private ProgressMonitor download;
    private PrivateChat privateChat;
    private Language language;
 
 
    /**
     *
     * @param address
     * @param file
     * @param port
     * @param size
     * @param privateChat
     * @param language
     */
    public ReceiveFile(InetAddress address, String file, int port, int size, PrivateChat privateChat,Language language) {
        this.language=language;
        this.address = address;
        this.size = size;
        this.fileName = file;
        this.port = port;
        this.privateChat = privateChat;
        download = new ProgressMonitor(privateChat, language.getValue("DOWNLOAD"), "", 0, 100);
        try {
            socket = new Socket(address, port);
        } catch (IOException e) {
            log.error("Error receiving a file");
            privateChat.insertLine(language.getValue("ERROR_SENDING_FILE"), privateChat.getTransfer(),true);
        }
    }
    
    /**
     * 
     */
    public void run() {
        try {
            privateChat.insertLine(language.getValue("DONWLOAD_OF") + " " + fileName + " (" + size/1000 + " Ko) ...",
                             privateChat.getTransfer(),true);
            download.setMillisToDecideToPopup(0);
            download.setMillisToPopup(0);
            download.setProgress(0);
            
            int readBytes;
            int currentTotal = 0;
            byte[] bytearray = new byte[size + 1];
            InputStream is = socket.getInputStream();
            FileOutputStream fos = new FileOutputStream(fileName);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            readBytes = is.read(bytearray, 0, bytearray.length);
            currentTotal = readBytes;
            TimeUnit.MILLISECONDS.sleep(100);
            
            //read
            do {
                readBytes = is.read(bytearray, currentTotal, (bytearray.length - currentTotal));
                if (readBytes >= 0)
                    currentTotal += readBytes;
                Float f = new Float((double) currentTotal / (size + 1));
                download.setProgress(Math.round(99 * f.floatValue()));
            } while (readBytes > -1);
            download.setProgress(99);
            
            //write
            bos.write(bytearray, 0, currentTotal);
            log.info("donwload of file {} finished",fileName);
            bos.flush();
            bos.close();
            socket.close();
            privateChat.insertLine("Download FINISHED !", privateChat.getTransfer(),true);
            download.setProgress(100);
            
            Toolkit.getDefaultToolkit().beep();
        } catch (Exception e) {
            log.trace("ERROR RECEIVING FILE {}",e);
            privateChat.insertLine(language.getValue("ERROR_SENDING_FILE"),
                             privateChat.getTransfer(),true);
            try {
                socket.close();
            } catch (IOException ioe) {
                log.trace("IMPOSSIBLE TO CLOSE SOCKET {}",ioe);
            }
        }
    }
}

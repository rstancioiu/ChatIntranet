package controller.client;

import java.awt.Color;
import java.awt.Dimension;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.net.Socket;

import java.text.SimpleDateFormat;

import java.util.Date;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import model.Aes;
import model.InformationsServer;
import model.Language;
import model.Message;

import view.AllChat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class Discussion handles the messages received by the user.
 * @see Message
 * @see AllChat
 * @see Socket
 * @see JTextPane
 */
public class Discussion extends JTextPane implements Runnable {
    
    private static final Logger log = LogManager.getLogger();
    private AllChat allChat;
    private Socket socket;
    private PrintWriter flowExit;
    private BufferedReader flowIncomming;
    private Message message;
    private Aes aes = new Aes();

    private StyledDocument doc;
    private Style myAlias;
    private Style alias;
    private Style body;
    private Style infos;
    private Style error;

    private DefaultListModel model = new DefaultListModel();
    private boolean exit = false;
    private Language language;


    public Discussion(InformationsServer infos, AllChat od, Language language) {
        this.language = language;
        this.setSize(new Dimension(450, 300));
        log.info("Creation of a discussion");
        doc = (StyledDocument) this.getDocument();
        this.allChat = od;
        this.setEditable(false);
        od.getListeUtilisateurs().setModel(model);
        model.addElement("  " + language.getValue("USER_LIST") + "  ");
        // Declarations des Styles
        error = this.addStyle("error", null);
        StyleConstants.setForeground(error, Color.RED);
        myAlias = this.addStyle("me", null);
        StyleConstants.setForeground(myAlias, Color.RED);
        alias = this.addStyle("sender", null);
        StyleConstants.setForeground(alias, Color.BLUE);
        body = this.addStyle("message body", null);
        StyleConstants.setFontSize(body, 12);
        StyleConstants.setFontSize(alias, 13);
        StyleConstants.setFontSize(myAlias, 13);
        StyleConstants.setFontSize(error, 14);
        this.infos = this.addStyle("", null);
        
        //creation of the tcp socket and the communication with the server
        try {
            socket = new Socket(infos.getAdrdess(), infos.getPort());
            sendMessage(new Message(Message.NEW_USER, null, od.getAlias(), null));
            insertLine(language.getValue("WELCOME_MESSAGE"), this.infos, true);
            allChat.setVisibleWindow();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, language.getValue("SERVER_NO_CONNECTION"),
                                          language.getValue("SERVER_NO_REPLY"), JOptionPane.ERROR_MESSAGE);
            insertLine(language.getValue("CONNEXION_FINISHED"), error, false);
            exit = true;
            od.quit();
        }
    }

    public void run() {
        while (!exit) {
            try {
                flowIncomming = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String s = flowIncomming.readLine();
                if (s != null) {
                    s = aes.decrypt(s, 1);
                    message = new Message(s);
                    handleMessage(message);
                }
            } catch (Exception e) {
                insertLine(language.getValue("ERROR_DISCONNECTED"), error, true);
                exit = true;
            }
        }
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("Impossible to close the socket");
        } catch (NullPointerException npe) {
            System.out.println("Socket closed");
        }
    }

    public void handleMessage(Message m) {
        int typeOfMessage = m.getType();
        
        log.info("Message received of type : "+ typeOfMessage);
        
        if (typeOfMessage == Message.MESSAGE) {
            if (m.getSender().equals(allChat.getAliasLabel().getText())) {
                insertLine(m.getSender(), myAlias, true);
            } else {
                insertLine(m.getSender(), alias, true);
            }
            insertLine(" : " + m.getBody(), body, false);
            allChat.scrollDown();
        } else if (typeOfMessage == Message.NEW_USER) {
            String sender = m.getSender();
            if (m.getBody().equals("new")) {
                model.addElement(sender);
                insertLine(sender + " " + language.getValue("JUST_CONNECTED"), infos, true);
                allChat.scrollDown();
            } else if (m.getBody().equals("pseudo")) {
                insertLine(language.getValue("ALIAS") + " \"" + allChat.getAliasLabel().getText() + " " +
                           language.getValue("ALIAS_CHOSEN") + sender + " !", infos, true);
                allChat.setAliasLabel(sender);
            } else
                model.addElement(sender);
        } else if (typeOfMessage == Message.EXIT) {
            String sender = m.getSender();
            model.removeElement(sender);
            insertLine(sender + " " + language.getValue("LEAVE_GROUP"), infos, true);
            allChat.scrollDown();
        } else if (typeOfMessage == Message.HOST_EXIT) {
            insertLine(language.getValue("HOST_LEAVE_GROUP"), error, true);
            JOptionPane.showMessageDialog(null,
                                          language.getValue("HOST_STOP") + " '" + allChat.getNameDiscussion() + "'",
                                          ":'(", JOptionPane.ERROR_MESSAGE);
        } else if (typeOfMessage == Message.SEND_FILE || typeOfMessage == Message.FILE_CANCELLED ||
                   typeOfMessage == Message.PRIVATE_MESSAGE) {
            allChat.updatePrivateDiscussion(m);
        }
    }

    /**
     * Method which sends a message to the server
     * @param message
     */
    public void sendMessage(Message message) {
        try {
            flowExit = new PrintWriter(socket.getOutputStream());
            log.info("Sending a message");
            String s = aes.encrypt(message.toString(), 1);
            flowExit.println(s);
            flowExit.flush();
        } catch (Exception e) {
            log.error("ERROR SENDING MESSAGE "+e);
            insertLine(language.getValue("ERROR_SENDING_MESSAGE"), error, false);
        }
    }

    /**
     * Method which sends an exit message to the server
     */
    public void quit() {
        log.info("quit discussion");
        this.sendMessage(new Message(3, null, allChat.getAlias(), null));
        exit = true;
    }

    public void insertLine(String texte, Style style, boolean heure) {
        try {
            if (heure) {
                SimpleDateFormat h = new SimpleDateFormat("hh:mm");
                String localHour = h.format(new Date());
                doc.insertString(doc.getLength(), "\n[" + localHour + "]   ", infos);
            }
            doc.insertString(doc.getLength(), texte, style);
        } catch (BadLocationException e) {
            setText(getText() + "\n" + language.getValue("ERROR_OCCURED"));
        }
    }
}

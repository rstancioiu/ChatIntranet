package view;


import controller.client.Discussion;
import controller.client.ReceiveFile;
import controller.client.SendFile;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import model.Language;
import model.Message;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PrivateChat extends JPanel {
    
    private static final Logger log = LogManager.getLogger();
    private BorderLayout layout = new BorderLayout();
    private JPanel panelTop = new JPanel();
    private JPanel panelBottom = new JPanel();
    private JButton quit = new JButton();
    private JButton buttonSend = new JButton();
    private JButton buttonSendFile;
    private JTextArea textArea = new JTextArea(249, 34);
    private JTextPane textPane = new JTextPane();
    private JScrollPane scroll = new JScrollPane();
    
    private StyledDocument document;
    private Style styleAlias;
    private Style myAlias;
    private Style body;
    private Style send;
    private Style informations;
    
    private final String sender;
    private String fileName;
    private String alias;
    private ArrayList<SendFile> listSendFile = new ArrayList<SendFile>();
    private final int MAX_SIZE = (int) Math.pow(2, 29);
    
    private Discussion discussion;
    private PrivateChat privateChat = this;
    private AllChat allChat;
    private Groups groups;
    private Language language;
    
    public PrivateChat(String receiv, String aliasClient, AllChat allChat,
                                    Groups new_groups, Discussion discus, Language lang) {
        this.language=lang;
        this.groups = new_groups;
        this.alias = aliasClient;
        this.discussion = discus;
        this.allChat = allChat;
        this.sender = receiv;
        
        log.info("A private chat created between "+ alias + " " + sender);
        
        buttonSendFile =new JButton(language.getValue("SEND_FILE"));
        document = (StyledDocument) textPane.getDocument();
        myAlias = textPane.addStyle("expediteur", null);
        StyleConstants.setForeground(myAlias, Color.BLUE);
        StyleConstants.setFontSize(myAlias, 13);
        styleAlias = textPane.addStyle("expediteur", null);
        StyleConstants.setForeground(styleAlias, Color.RED);
        body = textPane.addStyle("corps du message", null);
        StyleConstants.setFontSize(body, 12);
        StyleConstants.setFontSize(styleAlias, 13);
        send = textPane.addStyle("transferStyle", null);
        StyleConstants.setForeground(send, Color.BLUE);
        StyleConstants.setFontSize(send, 13);
        SimpleAttributeSet attributes = new SimpleAttributeSet();
        StyleConstants.setBold(attributes, true);
        informations = textPane.addStyle("", null);
        //
        this.setLayout(layout);
        panelTop.setLayout(new FlowLayout());
        this.setSize(new Dimension(400, 300));
        quit.setText("Quit");
        textPane.setEditable(false);
        panelTop.add(quit, BorderLayout.WEST);
        panelTop.add(buttonSendFile, BorderLayout.WEST);
        this.add(panelTop, BorderLayout.PAGE_START);
        scroll = new JScrollPane(textPane);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setAutoscrolls(true);
        this.add(scroll, BorderLayout.CENTER);
        buttonSend.setPreferredSize(new Dimension(90, 34));
        buttonSend.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendPrivateMessage();
            }
        });
        quit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                quit_actionPerformed(e);
            }
        });
        buttonSendFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser c = new JFileChooser();
                int choix = c.showOpenDialog(groups);
                if (choix == JFileChooser.APPROVE_OPTION) {
                    fileName = c.getSelectedFile().getName();
                    File fichier = c.getSelectedFile();
                    if (fichier.length() < MAX_SIZE) {
                        SendFile transferFichier = new SendFile(fichier, privateChat,language);
                        Thread envFichier = new Thread(transferFichier);
                        envFichier.start();
                        listSendFile.add(transferFichier);
                        int port = transferFichier.getPort();
                        String adresse = transferFichier.getAddress().getHostAddress();
                        Message messageEnv =
                            new Message(Message.SEND_FILE, fileName, (int) fichier.length(), port, adresse,
                                        alias, sender);
                        discussion.sendMessage(messageEnv);
                    } else {
                        insertLine(language.getValue("FILE_SIZE_EXCEEDED"), send,true);
                    }

                }
            }
        });

        JScrollPane jscrollpane4 =
            new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        textArea.setRows(2);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        buttonSend.setText("Send");


        panelBottom.add(jscrollpane4, new BorderLayout());
        panelBottom.add(buttonSend, null);
        this.add(panelBottom, BorderLayout.PAGE_END);

        Action action1 = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                sendPrivateMessage();
            }
        };
        String keyStrokeAndKey = "ENTER";
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keyStrokeAndKey);
        InputMap im = textArea.getInputMap();
        textArea.getActionMap().put(im.get(keyStroke), action1);


        Action action2 = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                buttonSend.requestFocus();
            }
        };
        KeyStroke remove = KeyStroke.getKeyStroke("TAB");
        InputMap im2 = textArea.getInputMap();
        im2.put(remove, action2);
    }

    private void quit_actionPerformed(ActionEvent e) {
        allChat.quitDiscussion(sender);
        groups.getTabs().remove(this);
    }

    private void sendPrivateMessage() {
        boolean spaces = true;
        if (textArea.getText().length() != 0) {
            int i = 0;
            while ((spaces == true) && (i < textArea.getText().length())) {
                if (!Character.isWhitespace(textArea.getText().charAt(i)))
                    spaces = false;
                else
                    spaces = true;
                i++;
            }
        } else
            spaces = true;
        if (spaces == false) {
            
                Message message = new Message(Message.PRIVATE_MESSAGE, textArea.getText(), alias, sender);
                insertLine(message.getSender(),styleAlias,true);
                insertLine(" : "+message.getBody(),body,false);
                discussion.sendMessage(message);
                textArea.setText(null);
                scrollerDown();
        }
    }

    public void updatePrivateDiscussion(Message message) {

        if (message.getType() == Message.PRIVATE_MESSAGE) {
                insertLine(message.getSender(),myAlias,true);
                insertLine(" : "+message.getBody(),body,false);
                scrollerDown();
                Toolkit.getDefaultToolkit().beep();
            
        } else if (message.getType() == Message.SEND_FILE) {
            double taille = (message.getFileSize() / 1000);
            int chx =
                JOptionPane.showConfirmDialog(null,
                                              message.getSender() + " "+language.getValue("SEND_WANT") +" " + "\n" +
                                              message.getFileName() + " - " + taille + " Ko", language.getValue("DOWNLOAD"),
                                              JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (chx == JOptionPane.YES_OPTION) {
                String directory = "";
                try {
                    JFileChooser fichierChoix1 = new JFileChooser();
                    fichierChoix1.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    int choix = fichierChoix1.showSaveDialog(groups);

                    if (choix == JFileChooser.APPROVE_OPTION) {
                        directory = fichierChoix1.getSelectedFile().getAbsolutePath();
                        String suffixeDossier = "\\";
                        for (int i = 0; i < directory.length(); i++) {
                            if (directory.charAt(i) == '/') {
                                suffixeDossier = "/"; 
                                break;
                            }
                        }
                        ReceiveFile recevoir =
                            new ReceiveFile(InetAddress.getByName(message.getAdresse()),
                                           directory + suffixeDossier + message.getFileName(), message.getPort(),
                                           message.getFileSize(), privateChat,language);
                        Thread recevoirFichier = new Thread(recevoir);
                        recevoirFichier.start();
                    } else if (choix == JFileChooser.CANCEL_OPTION) {
                        Message messageAnnuler =
                            new Message(Message.FILE_CANCELLED, message.getFileName(), alias, sender);
                        discussion.sendMessage(messageAnnuler);
                        insertLine("Download cancelled",
                                     send,true);
                        scrollerDown();
                    }
                } catch (UnknownHostException uhe) {
                    insertLine(language.getValue("ERROR_SENDING_FILE"), send,true);
                }
            } else if (chx == JOptionPane.NO_OPTION) {

                insertLine("Download cancelled", send,true);
                scrollerDown();
                Message messageAnnuler =
                    new Message(Message.FILE_CANCELLED, message.getFileName(), alias, sender);
                discussion.sendMessage(messageAnnuler);
            }
        } else if (message.getType() == message.FILE_CANCELLED) {

            insertLine(language.getValue("DOWNLOAD_OF") + " " + message.getFileName() + " " + language.getValue("DOWNLOAD_OF_CANCELLED"), send,true);
            scrollerDown();
            int k = 0;
            for (int i = 0; i < listSendFile.size(); i++) {
                if ((message.getFileName()).equals(listSendFile.get(i).getFileName())) {
                    k = i;
                }
            }
            listSendFile.remove(k);
        }
    }

    public void insertLine(String s, Style style, boolean heure) {
        try {
            if (heure) {
                SimpleDateFormat h = new SimpleDateFormat("hh:mm");
                 String localHour = h.format(new Date());
                document.insertString(document.getLength(), "\n["+localHour+"]  ", informations);
            }
            document.insertString(document.getLength(), s, style);
        } catch (BadLocationException e) {
            textPane.setText(textPane.getText() + " "+language.getValue("ERROR_OCCURED"));
        }
        scrollerDown();
    }
    
    public void scrollerDown() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                scroll.getVerticalScrollBar().setValue(scroll.getVerticalScrollBar().getMaximum());
            }
        });
    }

    public String getSender() {
        return sender;
    }


    public Style getTransfer() {
        return send;
    }
}

package view;

import controller.client.Discussion;

import controller.server.Server;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import javax.swing.SwingUtilities;

import model.InformationsServer;
import model.Language;
import model.Message;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AllChat extends JPanel {
    private static final Logger log = LogManager.getLogger();
    private JPanel top = new JPanel();
    private JPanel bottom = new JPanel();
    private JPanel panel1 = new JPanel();
    private JTextArea textArea = new JTextArea(214, 35);
    private JButton buttonSend = new JButton();
    private JButton buttonQuit = new JButton();
    private JLabel labelAlias = new JLabel();
    private JScrollPane scroll1;
    private JScrollPane scroll2;
    private JList listUsers = new JList();

    private String alias;
    private ArrayList<String> privateDiscussions = new ArrayList<String>();
    private ArrayList<PrivateChat> listPrivateDiscussions = new ArrayList<PrivateChat>();
    
    private Discussion discussion;
    private Groups groups;
    private InformationsServer infos;
    private Language language;
    private Server server;
   
    public AllChat(String alias, InformationsServer infos, Groups groups, Server server,Language language) {
        this.language=language;
        this.setLayout(new BorderLayout());
        this.alias = alias;
        this.infos = infos;
        this.server = server;
        this.groups = groups;
        
        log.info(alias + "is entering in " + server.getName());
        
        discussion = new Discussion(infos, this,language);
        Thread threadprincipal = new Thread(discussion);
        threadprincipal.start();
        this.add(discussion, BorderLayout.CENTER);
        labelAlias.setText(this.alias);
        
        buttonSend.setText("Send");
        buttonSend.setPreferredSize(new Dimension(90, 34));
        buttonSend.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buttonSend_actionPerformed(e);
            }
        });
        
        buttonQuit.setText("Quit");
        buttonQuit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buttonQuit_actionPerformed(e);
            }
        });
        
        labelAlias.setText(alias);
        top.add(labelAlias, null);
        top.add(buttonQuit, null);
        this.add(top, BorderLayout.NORTH);
        bottom.add(panel1, null);
        bottom.add(buttonSend, null);
        
        textArea.setRows(2);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("Arial", 0, 14));
        //action of ENTER key
        Action action1 = new AbstractAction() {    
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        };
        String keyStrokeAndKey = "ENTER";
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keyStrokeAndKey);
        InputMap im = textArea.getInputMap();
        textArea.getActionMap().put(im.get(keyStroke), action1);
        
        // action of TAB key
        Action action2 = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                buttonSend.requestFocus();
            }
        };
        KeyStroke remove = KeyStroke.getKeyStroke("TAB");
        InputMap im2 = textArea.getInputMap();
        im2.put(remove, action2);
        //
        this.add(bottom, BorderLayout.SOUTH);
        scroll1 = new JScrollPane(discussion);
        scroll1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll1.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        this.add(scroll1);
        scroll2 = new JScrollPane(textArea);
        scroll2.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll2.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        scroll2.setWheelScrollingEnabled(false);
        scroll2.setAutoscrolls(true);
        panel1.add(scroll2, BorderLayout.CENTER);
        JScrollPane scroll = new JScrollPane(listUsers);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        this.add(scroll, BorderLayout.EAST);
        MouseListener mouseListener = new MouseAdapter() {
            // double click 
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    if (listUsers.getSelectedIndex() != 0) {
                        addPrivateDiscussion((String)listUsers.getSelectedValue());
                    }
                }
            }
        };
        listUsers.addMouseListener(mouseListener);
        listUsers.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {
            }
            public void keyPressed(KeyEvent e) {
                if ((!listUsers.isSelectionEmpty()) && (listUsers.getSelectedIndex() != 0)) {
                    if (e.getKeyCode() == e.VK_ENTER)
                        addPrivateDiscussion((String)listUsers.getSelectedValue());
                }
            }
            
            public void keyReleased(KeyEvent e) {
            }
        });
    }

    private void buttonSend_actionPerformed(ActionEvent e) {
        sendMessage();
    }

    private void buttonQuit_actionPerformed(ActionEvent e) {
        if (server != null && server.getInfos().equals(infos) &&
            labelAlias.getText().equals(server.getInfos().getHost())) { 
            int choix =
                JOptionPane.showConfirmDialog(null, language.getValue("SERVER_SHUT_DOWN"), language.getValue("QUIT"),
                                              JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (choix == JOptionPane.YES_OPTION) {
                quit();
                server.stopServer();
                groups.getWindow().getBoutonCreer().setEnabled(true);
            } else if (choix == JOptionPane.NO_OPTION) {
                quit();

            }
        } else {
            quit();
        }

    }

    public void quit() {
        try {
            discussion.quit();
        } catch (NullPointerException e) {
            groups.getTabs().remove(this);
            log.error("Error on quitting",e);
        }
        groups.getTabs().remove(this);
        if(groups.getTabs().getTabCount()==0)
            groups.close();
        for (int i = 0; i < groups.getTabs().getTabCount(); i++) {
            if (privateDiscussions.size() != 0) {
                for (int j = 0; j < privateDiscussions.size(); j++) {
                    if (("#" + privateDiscussions.get(j)).equals(groups.getTabs().getTitleAt(i))) {
                        groups.getTabs().remove(i);
                    }
                }
            }
            listPrivateDiscussions.clear();
            privateDiscussions.clear();
        }
    }


    private void sendMessage() {
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
            Message message = new Message(1, textArea.getText(), labelAlias.getText(), null);
            discussion.sendMessage(message);
            textArea.setText(null);
        }
    }

    public void addPrivateDiscussion(String name) {
        alias = labelAlias.getText();
        if (!name.equals(alias)) {
            privateDiscussions.add(name);
            listPrivateDiscussions.add(new PrivateChat(name, alias, this, groups, discussion,language));
            groups.addTab(listPrivateDiscussions.get(listPrivateDiscussions.size()-1));
            log.info(name+" and "+alias+ " are in a private conversation");
        }
    }

    public void updatePrivateDiscussion(Message message) {
        int length = privateDiscussions.size();
        if (privateDiscussions.size() != 0) {
            boolean contain = false;
            for (int i = 0; i < privateDiscussions.size(); i++) {
                if (message.getSender().equals(privateDiscussions.get(i))) {
                    contain = true;
                    listPrivateDiscussions.get(i).updatePrivateDiscussion(message);
                }
            }
            if (contain == false) {
                addPrivateDiscussion(message.getSender());
                listPrivateDiscussions.get(length).updatePrivateDiscussion(message);
            }
        } else {
            addPrivateDiscussion(message.getSender());
            listPrivateDiscussions.get(0).updatePrivateDiscussion(message);
        }
    }
    
    public void quitDiscussion(String nameSender) {
        for (int i = 0; i < privateDiscussions.size(); i++) {
            if (nameSender.equals(privateDiscussions.get(i))) {
                privateDiscussions.remove(privateDiscussions.get(i));
                listPrivateDiscussions.remove(listPrivateDiscussions.get(i));
                break;
            }
        }
    }
    
    public void scrollDown() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                scroll1.getVerticalScrollBar().setValue(scroll1.getVerticalScrollBar().getMaximum());
            }
        });

    }

    public String getNameDiscussion() {
        String nameDiscussion = infos.getName() + " - "+ language.getValue("CONNECTED_AS") + " " + alias;
        int numberCharMax = 18;
        if (nameDiscussion.length() > numberCharMax)
            return nameDiscussion.substring(0, numberCharMax - 3) + "...";
        else
            return nameDiscussion;
    }

    public JList getListeUtilisateurs() {
        return listUsers;
    }
    
    public String getAlias() {
        return alias;
    }

    public Server getServer() {
        return server;
    }

    public void setAliasLabel(String s) {
        labelAlias.setText(s);
    }

    public JLabel getAliasLabel() {
        return labelAlias;
    }
    
    public void setVisibleWindow() {
        groups.setVisible(true);
    }
}

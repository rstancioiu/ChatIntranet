package view;

import controller.client.Broadcaster;

import controller.server.Server;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.IOException;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import model.InformationsServer;
import model.Language;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Window extends JFrame {
    private static final Logger log = LogManager.getLogger();
    private JButton buttonCreate = new JButton();
    private JPanel panelTop = new JPanel();
    private BorderLayout layout = new BorderLayout();
    private JScrollPane scrollList = new JScrollPane();
    private JTextField fieldAlias = new JTextField();
    private JLabel labelAlias = new JLabel();

    private int size = 25;
    private String serverType;
    private Broadcaster broadcast;
    private ServerList serverList;
    private int indexSelected;
    private Groups groups;
    private Server server;
    private boolean noGroup = true;
    private Language language;


    public Window(Language lang) {
        this.language = lang;
        this.setSize(new Dimension(400, 600));
        serverList = new ServerList(this,language);
        broadcast = new Broadcaster(this, serverList);
        Thread t = new Thread(broadcast);
        log.info("Window created");
        t.start();
        this.setLayout(layout);
        this.add(panelTop, BorderLayout.NORTH);
        panelTop.add(labelAlias, null);
        panelTop.add(fieldAlias, null);
        panelTop.add(buttonCreate, null);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(scrollList);
        this.add(panel,BorderLayout.CENTER);
        scrollList.setViewportView(serverList);
        fieldAlias.setColumns(15);
        labelAlias.setText("pseudo :");
        scrollList.setBounds(new Rectangle(84, 5, 25, 130));
        buttonCreate.setText(language.getValue("CREATE_GROUP"));
        buttonCreate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boutonCreerActionPerformed(e);
            }
        });
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                windowClosingEvent(e);
            }
        });
    }

    public void verifyConnection(InformationsServer infos) {
        log.info("Verification of the connection to: "+infos.getName());
        if (infos.getClients() == infos.getClientsMax()) {
            log.info("Group " +  infos.getName() + " is full");
            JOptionPane.showMessageDialog(null, language.getValue("GROUP_FULL"), language.getValue("NO_PLACE"), JOptionPane.ERROR_MESSAGE);
        } else if (infos.getType().equals("public")) {
            if (fieldAlias.getText().length() != 0) {
                log.info("Connection to public group:  "+infos.getName());
                Connection(fieldAlias.getText(), infos);
            } else {
                String pseudo = "";
                try {
                    while (pseudo.equals("")) {
                        pseudo =
                            JOptionPane.showInputDialog(null,language.getValue("ALIAS_QUESTION"),
                                                        language.getValue("INDETIFICATION"),
                                                        JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (NullPointerException e) {
                    pseudo = "";
                }
                if (pseudo.length() > 30) {
                    pseudo = pseudo.substring(0, 30);
                }
                if (!pseudo.equals("")) {
                    fieldAlias.setText(pseudo);
                    log.info("Connection to public group:  "+infos.getName());
                    Connection(pseudo, infos);
                }
            }

        } else if (infos.getType().equals("prive")) {
            String pseudo = fieldAlias.getText();
            boolean arret = false;
            while (!arret) {
                for (;;) {
                    LoginPanel panel = new LoginPanel(pseudo,language);

                    int n =
                        JOptionPane.showConfirmDialog(null, panel, language.getValue("LOGIN"), JOptionPane.OK_CANCEL_OPTION,
                                                      JOptionPane.PLAIN_MESSAGE, null);
                    if (n == JOptionPane.OK_OPTION) {
                        try {
                            pseudo = panel.getUserName();
                            if (!pseudo.equals("")) {
                                broadcast.sendPassword(panel.getPassword());
                                TimeUnit.MILLISECONDS.sleep(100);
                                if (broadcast.isAccepted() == true) {
                                    log.info("Connection to private group:  "+infos.getName());
                                    Connection(pseudo, infos);
                                    fieldAlias.setText(pseudo);
                                    arret = true;
                                    break;
                                }
                                fieldAlias.setText(pseudo);
                            }
                        } catch (InterruptedException e) {
                            log.error("Error on connecting to a private group");
                            showErrorMessage(language.getValue("ERROR_OCCURED"), language.getValue("Error"));
                        }
                    } else {
                        arret = true;
                        break;
                    }
                }
            }
            fieldAlias.setText(pseudo);
        }
    }

    private void Connection(String pseudo, InformationsServer infos) {
        broadcast.setAcceptedConnection(true);
        System.out.println();
        if (noGroup) {
            groups = new Groups(this,language);
            noGroup = false;
            AllChat od = new AllChat(pseudo, infos, groups, server,language);
            groups.addTab(od);
        }
        else 
        {
            AllChat od = new AllChat(pseudo, infos, groups, server,language); 
            groups.addTab(od);
        }
    }

    public void boutonCreerActionPerformed(ActionEvent e) {
        CreateServerPanel panel = new CreateServerPanel(language);
        int n =
            JOptionPane.showConfirmDialog(null, panel, language.getValue("NEW_GROUP"), JOptionPane.OK_CANCEL_OPTION,
                                          JOptionPane.PLAIN_MESSAGE, null);
        if (n == JOptionPane.OK_OPTION) {
            serverType = panel.getServerType();
            try {
                String nameS = panel.getServerName();
                while (nameS.equals("")) {
                    nameS =
                        JOptionPane.showInputDialog(null, language.getValue("CHOOSE_NAME_GROUP"),
                                                    language.getValue("NAME_GROUP"), JOptionPane.WARNING_MESSAGE);
                }
                String name;
                name = fieldAlias.getText();
                boolean connecte = false;
                while (name.equals("")) {
                    name =
                        JOptionPane.showInputDialog(null, language.getValue("ALIAS_QUESTION"), language.getValue("IDENTIFICATION"),
                                                    JOptionPane.OK_CANCEL_OPTION);
                }
                if (!name.equals("")) {
                    try {
                        size = panel.getServerSize();
                        server = new Server(nameS, name, size, serverType, panel.getPassword());
                        Thread t = new Thread(server);
                        t.start();

                        buttonCreate.setEnabled(false);
                        InformationsServer info = server.getInfos();
                        connecte = true;
                        log.info("Connection to group:  "+info.getName());
                        Connection(name, info);

                    } catch (IOException i) {
                        buttonCreate.setEnabled(false);
                        log.error("Error creating a server");
                        showErrorMessage(language.getValue("ERROR_GROUP"), language.getValue("ERROR"));
                    }
                }
                fieldAlias.setText(name);
                if (name.length() > 30) {
                    name = name.substring(0, 30);
                }
            } catch (NullPointerException npe) {
                log.error("Error creating a server");
                showErrorMessage(language.getValue("ERROR_OCCURED"), language.getValue("ERROR"));
            }
        }
    }


    public void showErrorMessage(String txt, String titre) {
        JOptionPane.showMessageDialog(null, txt, titre, JOptionPane.ERROR_MESSAGE);
    }

    public JButton getBoutonCreer() {
        return buttonCreate;
    }

    public int getIndexSelected() {
        return indexSelected;
    }

    private void windowClosingEvent(WindowEvent e) {
        if (!noGroup) {
            ArrayList<AllChat> tabOnglets = groups.getTabDiscussions();
            for (int i = 0; i < tabOnglets.size(); i++) {
                tabOnglets.get(i).quit();
                if (tabOnglets.get(i) != null) {
                    if (tabOnglets.get(i).getServer() != null) {
                        tabOnglets.get(i).getServer().stopServer();
                    }
                }
            }
        }
        System.exit(0);
    }

    public void setNoGroup() {
        noGroup = true;
    }
}
package view;

import controller.Broadcaster;
import controller.Serveur;

import model.InfoServeur;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.io.IOException;

import java.text.NumberFormat;

import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

/**
 * La classe OngletRooms de la fenetre affiche la liste des groupes de discussion disponibles et propose
 * aux utilisateurs de creer un serveur ou de rejoindre un groupe deja existant. Les serveurs ainsi crees,
 * peuvent etre proteges par un mot de passe. On utilise la classe Broadcaster (une extension de Jlist)
 * pour mettre a jour l'affichage.
 * Elle herite de Jpanel
 * @see controller.Broadcaster
 * @see controller.Serveur
 * @see model.InfoServeur
 */
public class OngletRooms extends JPanel {
    @SuppressWarnings("compatibility:8511127925898716357")
    private static final long serialVersionUID = 1L;

    // composants de l'interface
    private JButton boutonGO = new JButton();
    private JButton boutonCreer = new JButton();
    private JPanel panelHaut = new JPanel();
    private BorderLayout layout = new BorderLayout();
    private JScrollPane scrollListe = new JScrollPane();
    private JTextField champPseudo = new JTextField();
    private JLabel labelPseudo = new JLabel();

    private int taille = 25;
    private String serverType;
    private Broadcaster broadcast;
    private ListeServeur listeServeurs;
    private InfoServeur infos;
    private int listeIndexSelecte;
    private Fenetre fenetre;
    private Serveur serveur;

    /**
     * constructeur de l'onglet - ajout des composants avec leurs proprietes
     * @param fenetre
     */
    public OngletRooms(Fenetre fenetre) {
        this.fenetre = fenetre;
        try {
            listeServeurs = new ListeServeur();
            broadcast = new Broadcaster(this,listeServeurs);
        } catch (IOException e) {
            afficherMessageErreur("Impossible de communiquer avec le reseau, verifiez votre connexion", "Erreur");
        }
        Thread t = new Thread(broadcast);
        t.start();
        // Ajout des composants SWING ici
        this.setLayout(layout);
        this.setSize(new Dimension(500, 400));
        this.add(panelHaut, BorderLayout.PAGE_START);
        panelHaut.add(labelPseudo, null);
        panelHaut.add(champPseudo, null);
        panelHaut.add(boutonGO);
        panelHaut.add(boutonCreer, null);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(scrollListe);
        this.add(panel);
        scrollListe.setViewportView(listeServeurs);
        MouseListener mouseListener = new MouseAdapter() {
            //connection avec la partie TCP

            /**
             * connection a une discussion par double clique
             */
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    verifierConnection();
                    // Connection a la discussion;
                }
            }
        };
        listeServeurs.addKeyListener(new KeyListener() {
            /**
             * redefinition
             */
            public void keyTyped(KeyEvent e) {

            }

            /**
             * redefinition permettant la connection par la touche ENTER
             */
            public void keyPressed(KeyEvent e) {
                if (!listeServeurs.isSelectionEmpty()) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER)
                        verifierConnection();
                }
            }

            /**
             * redefinition
             */
            public void keyReleased(KeyEvent e) {
            }
        });
        listeServeurs.addMouseListener(mouseListener);
        champPseudo.setColumns(15);
        labelPseudo.setText("pseudo :");
        scrollListe.setBounds(new Rectangle(84, 5, 25, 130));
        boutonCreer.setText("Creer ma Room");
        boutonCreer.addActionListener(new ActionListener() {
            /**
             * action du bouton creer une room
             */
            public void actionPerformed(ActionEvent e) {
                boutonCreerActionPerformed(e);
            }
        });

        boutonGO.setText("Go");
        boutonGO.addActionListener(new ActionListener() {
            /**
             * action du bouton go permettant de rejoindre une room
             */
            public void actionPerformed(ActionEvent evt) {
                boutonGOActionPerformed(evt);
            }
        });
    }

    /**
     * permet d'etablir la connexion si elle est possible, soit pour un groupe de discussion public
     * (sans demande de mot de passe),soit pour un groupe de discussion prive (avec mot de passe).
     */
    private void verifierConnection() {
        if (listeServeurs.getSelectedValue() instanceof InfoServeur) { // convertir l'objet dans la jlist en infoserver
            listeIndexSelecte = listeServeurs.getSelectedIndex();
            infos = (InfoServeur) listeServeurs.getSelectedValue();
            if (infos.getClients() == infos.getClientsMax()) {
                JOptionPane.showMessageDialog(null, "La discussion est pleine :'(", "Plus de place",
                                              JOptionPane.ERROR_MESSAGE);
            } else if (infos.getType().equals("public")) { // cas ou le serveur est de type "public"
                if (champPseudo.getText().length() != 0) { // si l'utilisateur a deja renseign� un pseudo dans le jLabel
                    Connection(champPseudo.getText(), infos);
                } else { // Sinon on ouvre un pop-up
                    String pseudo = "";
                    try {
                        while (pseudo.equals("")) {
                            pseudo =
                                JOptionPane.showInputDialog(null, "Quel est votre pseudo ?",
                                                            "Qui etes-vous ? (30 caractères max)",
                                                            JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (NullPointerException e) {
                        pseudo = "";
                    }
                    if (pseudo.length() > 30) {
                        pseudo = pseudo.substring(0, 30);
                    }
                    if (!pseudo.equals("")) {
                        champPseudo.setText(pseudo);
                        Connection(pseudo, infos);
                    }
                }

            } else if (infos.getType().equals("prive")) { 
                    String pseudo = champPseudo.getText();
                    boolean arret = false; 
                    while (!arret) {
                        for (;;) {
                            PanelLogin panel= new PanelLogin(pseudo);
                            
                            int n =
                                JOptionPane.showConfirmDialog(null, panel, "Login",
                                                              JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE,null);
                            if (n == JOptionPane.OK_OPTION) {
                                try {
                                    pseudo = panel.getNomUtilisateur();
                                    if(!pseudo.equals(""))
                                    {
                                        broadcast.envoyerMdp(panel.getPassword());
                                        TimeUnit.MILLISECONDS.sleep(100);
                                        if (broadcast.isConnectionAccepte() == true) {
                                            Connection(pseudo, infos);
                                            champPseudo.setText(pseudo);
                                            arret=true;
                                            break;
                                        }
                                        champPseudo.setText(pseudo);
                                    }
                                } catch (InterruptedException e) {
                                    afficherMessageErreur("Une exception est produit lors de la connexion","Erreur");
                                } catch (IOException e) {
                                    afficherMessageErreur("Impossible d'envoyer le mot de passe au serveur pour verification",
                                                          "Erreur");
                                }
                            } else {
                                arret = true;
                                break;
                            }
                        }
                    }
                    champPseudo.setText(pseudo);
            }
        }
    }

    /**
     * permet d'etablir la connection
     * @param pseudo
     * @param infos
     */
    private void Connection(String pseudo, InfoServeur infos) {
        broadcast.setConnectionAccepte(true);
        OngletDiscussion od = new OngletDiscussion(pseudo, infos, fenetre, serveur);
        fenetre.ajouterOnglet(od);
    }
    
    /**
     * action du bouton pour creer un serveur de discussion prive ou public. De plus ,
     * l'utilisateur peut choisir un nom , la taille du groupe et un mot de passe.
     */
    private void boutonCreerActionPerformed(ActionEvent e) {
        PanelCreerServeur panel = new PanelCreerServeur();
        int n = JOptionPane.showConfirmDialog(null, panel, "Nouvelle Room", JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE,null);
        if (n == JOptionPane.OK_OPTION) {
            serverType = panel.getServerType();
            try {
                String nomS = panel.getNomServeur();
                while (nomS.equals("")) {
                    nomS =
                        JOptionPane.showInputDialog(null, "Donnez un nom a votre Room (30 caractères max)",
                                                    "Nom de la Room ", JOptionPane.WARNING_MESSAGE);
                }
                String nom;
                nom = champPseudo.getText();
                boolean connecte = false;
                while (nom.equals("")) {
                    nom =
                        JOptionPane.showInputDialog(null, "Quel est votre pseudo ?",
                                                    "Qui etes-vous ? (30 caractères max)",
                                                    JOptionPane.OK_CANCEL_OPTION);
                }
                if (!nom.equals("")) {
                    try {
                        taille = panel.getTailleServeur();
                        serveur = new Serveur(nomS, nom, taille, serverType, panel.getPassword());
                        Thread t = new Thread(serveur);
                        t.start();

                        boutonCreer.setEnabled(false); // on ne peut hebergeur qu'une seule discussion
                        InfoServeur info = serveur.getInfos();
                        connecte = true;
                        Connection(nom, info);
                        
                    } catch (IOException i) {
                        afficherMessageErreur("Impossible de creer la Room :'(", "Erreur");
                    }
                }
                champPseudo.setText(nom);
                if (nom.length() > 30) {
                    nom = nom.substring(0, 30);
                }
            } catch (NullPointerException npe) {
                afficherMessageErreur("Une erreur a ete produit","Erreur");
            }
        }
    }

    /**
     * action du bouton go pour rejoindre une discussion
     */
    private void boutonGOActionPerformed( ActionEvent evt) {
        verifierConnection();
    }

    /**
     * affichage d'un pop-up d'erreur en cas d'echec de connection.
     * @param txt
     * @param titre
     */
    public void afficherMessageErreur(String txt, String titre) {
        JOptionPane.showMessageDialog(null, txt, titre, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * getter du bouton creer
     * @return le bouton creer
     */
    public JButton getBoutonCreer() {
        return boutonCreer;
    }

    /**
     * getter de l'indice de l'element selectionne
     * @return l'index de la selection
     */
    public int getListeIndexSelecte() {
        return listeIndexSelecte;
    }
}

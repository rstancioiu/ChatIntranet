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
    
    // composants de l'interface
    private JButton boutonGO = new JButton();
    private JButton boutonCreer = new JButton();
    private JPanel panelHaut = new JPanel();
    private BorderLayout layout = new BorderLayout();
    private JScrollPane scrollListe = new JScrollPane();
    private JTextField champPseudo = new JTextField();
    private JLabel labelPseudo = new JLabel();
    private JTextField nomUtilisateur = new JTextField();
    private JPasswordField password = new JPasswordField();
    private JTextField nomServeur = new JTextField();
    private JFormattedTextField tailleServeur = new JFormattedTextField();
    private JPasswordField mdpServeur = new JPasswordField();
    
    private int taille = 25;
    private String serverType;
    private Broadcaster listeServeurs;
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
        listeServeurs = new Broadcaster(this); }
        catch(IOException e) {
            afficherMessageErreur("Impossible de communiquer avec le reseau, verifiez votre connexion","Erreur");
        }
        Thread t = new Thread(listeServeurs);
        t.start();
        // Ajout des composants SWING ici
        this.setLayout(layout);
        this.setSize(new Dimension(500, 400));
        this.add(panelHaut, BorderLayout.PAGE_START);
        panelHaut.add(labelPseudo, null);
        panelHaut.add(champPseudo, null);
        panelHaut.add(boutonGO);
        panelHaut.add(boutonCreer, null);
        this.add(scrollListe, BorderLayout.CENTER);
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

            } else if (infos.getType().equals("prive")) { // cas ou le serveur est de type "priv�"
                if (champPseudo.getText().length() != 0) {
                    String pseudo = champPseudo.getText();
                    if (pseudo.length() > 30) {
                        pseudo = pseudo.substring(0, 30);
                    }
                    nomUtilisateur.setText(pseudo);
                    for (;;
                    ) { //boucle infinie pour mettre le motDePasse
                        int i =
                     JOptionPane.showConfirmDialog(null, new PanelLogin(), "Login", JOptionPane.OK_CANCEL_OPTION);
                        if (i == JOptionPane.OK_OPTION) {
                            try {
                                listeServeurs.envoyerMdp(new String(password.getPassword()));
                                TimeUnit.MILLISECONDS.sleep(100); // attendre 0.1 secondes pour verifier le mot de passe
                                if (listeServeurs.isConnectionAccepte() == true) { // si la connection a ete accepte
                                    Connection(nomUtilisateur.getText(), infos);
                                    champPseudo.setText(nomUtilisateur.getText());
                                    break;
                                }
                            } catch (InterruptedException e) {
                                System.out.println("ongletRooms exception");
                                }catch(IOException e) {
                                    afficherMessageErreur("Impossible d'envoyer le mot de passe au serveur pour verification","Erreur");
                                }
                        } else if (i == JOptionPane.CANCEL_OPTION) { // si on appuie sur "annuler"
                            break;
                        }
                    }
                } else { // pareil ici:
                    String pseudo = "";
                    boolean arret = false; //pour arreter la boucle while
                    while (pseudo.equals("") && (arret == false)) {
                        for (;;) {
                            int n =
                                JOptionPane.showConfirmDialog(null, new PanelLogin(), "Login",
                                                              JOptionPane.OK_CANCEL_OPTION);
                            if (n == JOptionPane.OK_OPTION) {
                                pseudo = nomUtilisateur.getText();
                                try {
                                    listeServeurs.envoyerMdp(new String(password.getPassword()));
                                    TimeUnit.MILLISECONDS.sleep(100);
                                    if (listeServeurs.isConnectionAccepte() == true && !pseudo.equals("")) {
                                        Connection(pseudo, infos);
                                        champPseudo.setText(pseudo);
                                        break;
                                    }
                                    champPseudo.setText(pseudo);
                                    nomUtilisateur.setText(pseudo);
                                } catch (InterruptedException e) {
                                    }catch(IOException e) {
                                        afficherMessageErreur("Impossible d'envoyer le mot de passe au serveur pour verification","Erreur");
                                    }
                            } else if (n == JOptionPane.CANCEL_OPTION) {
                                arret = true;
                                break;
                            }
                        }
                    }
                    champPseudo.setText(pseudo);
                }
            }
        }
    }

    /**
     * permet d'etablir la connection
     * @param pseudo
     * @param infos
     */
    private void Connection(String pseudo, InfoServeur infos) {
        listeServeurs.setConnectionAccepte(true);
        OngletDiscussion od = new OngletDiscussion(pseudo, infos, fenetre, serveur);
        fenetre.ajouterOnglet(od);
    }

    /**
     * fenetre de dialogue pour renseigner le mot de passe et le pseudo
     * classe privee qui herite de Jpanel
     */
    private class PanelLogin extends JPanel {
        private JPanel jPanel2 = new JPanel();
        private JPanel jPanel3 = new JPanel();
        private JLabel jLabel1 = new JLabel();
        private JLabel jLabel2 = new JLabel();
        private GridLayout gridLayout1 = new GridLayout();
        private GridLayout gridLayout2 = new GridLayout();

        /**
         * constructeur de la fenetre de dialogue Login
         */
        public PanelLogin() {
            this.setLayout(new BorderLayout());
            password.setText("");
            jPanel2.setLayout(gridLayout2);
            jPanel3.setLayout(gridLayout1);
            JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, jPanel2, jPanel3);
            splitPane.setResizeWeight(0.8);
            add(splitPane, BorderLayout.CENTER);
            splitPane.setContinuousLayout(true);
            jLabel1.setText("Pseudo : ");
            jLabel1.setLabelFor(nomUtilisateur);
            nomUtilisateur.setPreferredSize(new Dimension(80, 20));
            jLabel2.setText("Mot de passe :");
            password.setPreferredSize(new Dimension(80, 20));
            jPanel2.add(jLabel1, BorderLayout.WEST);
            jPanel2.add(nomUtilisateur, BorderLayout.CENTER);
            this.add(jPanel2, BorderLayout.NORTH);
            this.add(jPanel3, BorderLayout.SOUTH);
            jPanel3.add(jLabel2, BorderLayout.WEST);
            jPanel3.add(password, BorderLayout.CENTER);
        }
    }

    /**
     * fenetre de renseignement pour creer un serveur
     * classe qui herite de Jpanel
     */
    private class PanelCreerServeur extends JPanel {
        private JCheckBox checkbox = new JCheckBox();

        /**
         * constructeur de la fenetre CreerServeur
         */
        public PanelCreerServeur() {
            nomServeur = new JTextField();

            NumberFormat f = NumberFormat.getNumberInstance();
            f.setParseIntegerOnly(true);
            f.setMaximumIntegerDigits(3);

            tailleServeur = new JFormattedTextField(f);
            tailleServeur.setText("25");
            this.setLayout(new GridLayout(0, 2, 10, 5));
            JPanel panel = new JPanel();
            panel.add(new JLabel("Mot de passe :"));
            panel.add(checkbox);
            this.add(new JLabel("Nom de la Room :"));
            this.add(nomServeur);
            this.add(new JLabel("Taille de la Room :"));
            this.add(tailleServeur);
            this.add(panel);
            this.add(mdpServeur);
            mdpServeur.setEnabled(false);
            checkbox.addActionListener(new ActionListener() {

            	/**
            	 * permet l'utilisation d'un mot de passe
            	 */
                public void actionPerformed(ActionEvent e) {
                    if (checkbox.isSelected()) {
                        mdpServeur.setEnabled(true);
                    } else {
                        mdpServeur.setEnabled(false);
                        mdpServeur.setText("");
                    }

                }
            });
        }
    }

    /**
     * action du bouton pour creer un serveur de discussion prive ou public. De plus ,
     * l'utilisateur peut choisir un nom , la taille du groupe et un mot de passe.
     * @param e
     */
    private void boutonCreerActionPerformed(ActionEvent e) {

        //choix du nom du serveur non vide
        int n =
            JOptionPane.showConfirmDialog(null, new PanelCreerServeur(), "Nouvelle Room",
                                          JOptionPane.OK_CANCEL_OPTION);
        //prendre le mot de passe et etablir le type du serveur
        if (n == JOptionPane.OK_OPTION) {
            if (mdpServeur.isEnabled() == false) {
                serverType = "public";
            } else {
                serverType = "prive";
            }
            try{
            String nomS = nomServeur.getText();
            while (nomS.equals("")) {
                nomS =
                    JOptionPane.showInputDialog(null, "Donnez un nom a votre Room (30 caractères max)",
                                                "Nom de la Room ", JOptionPane.WARNING_MESSAGE);
            }

            String nom = champPseudo.getText();
            int s = 0;
            boolean connecte = false;
            while (nom.equals("")) {
                nom =
                    JOptionPane.showInputDialog(null, "Quel est votre pseudo ?", "Qui etes-vous ? (30 caractères max)",
                                                JOptionPane.OK_CANCEL_OPTION);
            }
            if (!nom.equals("")) {
                try {
                    taille = Math.abs(Integer.parseInt(tailleServeur.getText()));
                } catch (NumberFormatException nfe) {
                    taille = 25;
                }
                if (taille == 0) {
                    taille = 25;
                }
                try {
                    serveur = new Serveur(nomS, nom, taille, serverType, new String(mdpServeur.getPassword()));
                    Thread t = new Thread(serveur);
                    t.start();

                } catch (IOException i) {
                    afficherMessageErreur("Impossible de creer la Room :'(","Erreur");
                }

                boutonCreer.setEnabled(false); // on ne peut hebergeur qu'une seule discussion
                InfoServeur info = serveur.getInfos();

                connecte = true;
                Connection(nom, info);
            }
            if (nom.length() > 30) {
                nom = nom.substring(0, 30);
            }
            champPseudo.setText(nom);

            if (connecte == false) {

                taille = Math.abs(Integer.parseInt(tailleServeur.getText()));
                if (taille == 0)
                    taille = 25;
                try {
                    serveur = new Serveur(nomS, nom, taille, serverType, new String(mdpServeur.getPassword()));
                    Thread t = new Thread(serveur);
                    t.start();
                } catch (Exception i) {
                    afficherMessageErreur("Impossible de creer la Room :'(","Erreur");
                }

                boutonCreer.setEnabled(false);
                InfoServeur info = serveur.getInfos();

                connecte = true;
                Connection(nom, info);
            }
            if (nom.length() > 30) {
                nom = nom.substring(0, 30);
            }
            }catch(NullPointerException npe){ 
             
            }
        }
    }
    
    /**
     * action du bouton go pour rejoindre une discussion
     * @param evt
     */
    private void boutonGOActionPerformed(ActionEvent evt) {
        verifierConnection();
    }
    
    /**
     * affichage d'un pop-up d'erreur en cas d'echec de connection
     * @param txt
     * @param titre
     */
    public void afficherMessageErreur(String txt,String titre) {
        JOptionPane.showMessageDialog(null,txt,titre,
                                      JOptionPane.ERROR_MESSAGE);
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

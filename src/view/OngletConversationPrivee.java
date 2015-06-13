package view;


import controller.Discussion;
import controller.EnvFichier;
import controller.RecFichier;

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

import model.Message;


/**
 * OngletConversationPrivee est un onglet de fenetre permettant a 2 utilisateurs qui appartiennent au meme 
 * groupe de communiquer independement. Elle herite de Jpanel et appartient a la classe OngletDiscussion en
 * etant un objet pour cette classe. De plus, elle utilise les classes controller.EnvFichier ,
 * controller.RecFichier pour envoyer et recevoir de fichiers.
 * @see view.OngletDiscussion
 * @see view.Fenetre
 * @see controller.Discussion
 * @see controller.EnvFichier
 * @see controller.RecFichier
 * @see Message
 */
public class OngletConversationPrivee extends JPanel {
    
    //interface du panel 
    private BorderLayout layout = new BorderLayout();
    private JPanel panelHaut = new JPanel();
    private JPanel panelBas = new JPanel();
    private JButton quitter = new JButton();
    private JButton boutonEnvoyer = new JButton();
    private JButton boutonEnvFichier = new JButton("Envoyer Fichier");
    private JTextArea zoneSaisie = new JTextArea(249, 34);
    private JTextPane zoneTexte = new JTextPane();
    private JScrollPane scrollConversation = new JScrollPane();
    
    //les styles utilises
    private StyledDocument document;
    private Style stylePseudo;
    private Style monpseudo;
    private Style corps;
    private Style transfert;
    private Style informations;
    
    private final String destinataire;
    private String nomFichier;
    private String monPseudo;
    private ArrayList<EnvFichier> listeFichierEnvoye = new ArrayList<EnvFichier>();
    private final int TAILLE_MAX = (int) Math.pow(2, 29); // taille max d'un fichier qu'on peut envoyer
    
    private Discussion discussion;
    private OngletConversationPrivee OCP = this;
    private OngletDiscussion ongletDiscussion;
    private Fenetre fenetre;
    
    /**
     * constructeur avec les elements de l'interface et leur proprietes
     * @param exped
     * @param pseudoClient
     * @param ongletDiscussion
     * @param fenetreInitiale
     * @param discussionPane
     */
    public OngletConversationPrivee(String exped, String pseudoClient, OngletDiscussion ongletDiscussion,
                                    Fenetre fenetreInitiale, Discussion discussionPane) {
        this.fenetre = fenetreInitiale;
        this.monPseudo = pseudoClient;
        this.discussion = discussionPane;
        this.ongletDiscussion = ongletDiscussion;
        this.destinataire = exped;
        // Declarations des Styles
        document = (StyledDocument) zoneTexte.getDocument();
        monpseudo = zoneTexte.addStyle("expediteur", null);
        StyleConstants.setForeground(monpseudo, Color.BLUE);
        StyleConstants.setFontSize(monpseudo, 13);
        stylePseudo = zoneTexte.addStyle("expediteur", null);
        StyleConstants.setForeground(stylePseudo, Color.RED);
        corps = zoneTexte.addStyle("corps du message", null);
        StyleConstants.setFontSize(corps, 12);
        StyleConstants.setFontSize(stylePseudo, 13);
        transfert = zoneTexte.addStyle("transferStyle", null);
        StyleConstants.setForeground(transfert, Color.BLUE);
        StyleConstants.setFontSize(transfert, 13);
        SimpleAttributeSet attributes = new SimpleAttributeSet();
        StyleConstants.setBold(attributes, true);
        informations = zoneTexte.addStyle("", null);
        //
        this.setLayout(layout);
        panelHaut.setLayout(new FlowLayout());
        this.setSize(new Dimension(400, 300));
        quitter.setText("Fermer");
        zoneTexte.setEditable(false);
        panelHaut.add(quitter, BorderLayout.WEST);
        panelHaut.add(boutonEnvFichier, BorderLayout.WEST);
        this.add(panelHaut, BorderLayout.PAGE_START);
        scrollConversation = new JScrollPane(zoneTexte);
        scrollConversation.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollConversation.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        scrollConversation.setAutoscrolls(true);
        this.add(scrollConversation, BorderLayout.CENTER);
        boutonEnvoyer.setPreferredSize(new Dimension(90, 34));
        boutonEnvoyer.addActionListener(new ActionListener() {
            /**
             * action event bouton envoyer
             */
            public void actionPerformed(ActionEvent e) {
                envoyerMessagePrive();
            }
        });
        quitter.addActionListener(new ActionListener() {
            /**
             * action event bouton quitter
             */
            public void actionPerformed(ActionEvent e) {
                quitter_actionPerformed(e);
            }
        });
        boutonEnvFichier.addActionListener(new ActionListener() {
            /**
             * action event bouton envoiFichier
             */
            public void actionPerformed(ActionEvent e) {
                JFileChooser c = new JFileChooser();
                int choix = c.showOpenDialog(fenetre);
                if (choix == JFileChooser.APPROVE_OPTION) {
                    nomFichier = c.getSelectedFile().getName();
                    File fichier = c.getSelectedFile();
                    if (fichier.length() < TAILLE_MAX) {
                        EnvFichier transferFichier = new EnvFichier(fichier, OCP);
                        Thread envFichier = new Thread(transferFichier);
                        envFichier.start();
                        listeFichierEnvoye.add(transferFichier);
                        int port = transferFichier.getPort();
                        String adresse = transferFichier.getAddress().getHostAddress();
                        Message messageEnv =
                            new Message(Message.TRANSFERT_FICHIER, nomFichier, (int) fichier.length(), port, adresse,
                                        monPseudo, destinataire);
                        discussion.envoyer(messageEnv);
                    } else {
                        insererLigne("le fichier est trop volumineux pour etre envoye :'(", transfert,true);
                    }

                }
            }
        });

        JScrollPane jscrollpane4 =
            new JScrollPane(zoneSaisie, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        zoneSaisie.setRows(2);
        zoneSaisie.setLineWrap(true);
        zoneSaisie.setWrapStyleWord(true);
        boutonEnvoyer.setText("Envoyer");


        panelBas.add(jscrollpane4, new BorderLayout());
        panelBas.add(boutonEnvoyer, null);
        this.add(panelBas, BorderLayout.PAGE_END);

        Action action1 = new AbstractAction() {

            /**
             * Override de l'action du bouton entrer pour pouvoir envoyer avec
             */
            public void actionPerformed(ActionEvent e) {
                envoyerMessagePrive();
            }
        };
        String keyStrokeAndKey = "ENTER";
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keyStrokeAndKey);
        InputMap im = zoneSaisie.getInputMap();
        zoneSaisie.getActionMap().put(im.get(keyStroke), action1);


        Action action2 = new AbstractAction() {


            /**
             * Override de l'action de la touche TAB pour ne pas influer dans le texte de saisie
             */
            public void actionPerformed(ActionEvent e) {
                boutonEnvoyer.requestFocus();
            }
        };
        KeyStroke remove = KeyStroke.getKeyStroke("TAB");
        InputMap im2 = zoneSaisie.getInputMap();
        im2.put(remove, action2);
    }

    /**
     * methode de l'action du bouton quitter
     * @param e
     */
    private void quitter_actionPerformed(ActionEvent e) {
        ongletDiscussion.quitterConversation(destinataire);
        fenetre.getOnglets().remove(this);
    }

    /**
     * methode d'envoi du message prive
     */
    private void envoyerMessagePrive() {
        boolean espaces = true;
        if (zoneSaisie.getText().length() != 0) {
            int i = 0;
            while ((espaces == true) && (i < zoneSaisie.getText().length())) {
                if (!Character.isWhitespace(zoneSaisie.getText().charAt(i)))
                    espaces = false;
                else
                    espaces = true;
                i++;
            }
        } else
            espaces = true;
        if (espaces == false) {
            
                Message message = new Message(Message.MESSAGE_PRIVE, zoneSaisie.getText(), monPseudo, destinataire);
                insererLigne(message.getExpediteur(),stylePseudo,true);
                insererLigne(" : "+message.getCorps(),corps,false);
                discussion.envoyer(message);
                zoneSaisie.setText(null);
                scrollerBasConversation();
        }
    }

    /**
     * actualise la conversation en fonction des messages recus
     * @param message
     */
    public void updateConversationPrive(Message message) {

        if (message.getType() == Message.MESSAGE_PRIVE) {
                insererLigne(message.getExpediteur(),monpseudo,true);
                insererLigne(" : "+message.getCorps(),corps,false);
                scrollerBasConversation();
                Toolkit.getDefaultToolkit().beep();
            
        } else if (message.getType() == Message.TRANSFERT_FICHIER) {
            double taille = (message.getTailleFichier() / 1000);
            int chx =
                JOptionPane.showConfirmDialog(null,
                                              message.getExpediteur() + " souhaite vous envoyer un fichier\n" +
                                              message.getNomFichier() + " - " + taille + " Ko", "Recevoir un fichier",
                                              JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (chx == JOptionPane.YES_OPTION) {
                String directory = "";
                try {
                    JFileChooser fichierChoix1 = new JFileChooser();
                    fichierChoix1.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    int choix = fichierChoix1.showSaveDialog(fenetre);

                    if (choix == JFileChooser.APPROVE_OPTION) {
                        directory = fichierChoix1.getSelectedFile().getAbsolutePath();
                        /* il faut distinguer le cas Windows (dossier séparés par "\") et linux ("/") */
                        String suffixeDossier = "\\";
                        for (int i = 0; i < directory.length(); i++) {
                            if (directory.charAt(i) == '/') {
                                suffixeDossier = "/"; // on est sur linux
                                break;
                            }
                        }
                        RecFichier recevoir =
                            new RecFichier(InetAddress.getByName(message.getAdresse()),
                                           directory + suffixeDossier + message.getNomFichier(), message.getPort(),
                                           message.getTailleFichier(), OCP);
                        Thread recevoirFichier = new Thread(recevoir);
                        recevoirFichier.start();
                    } else if (choix == JFileChooser.CANCEL_OPTION) {
                        Message messageAnnuler =
                            new Message(Message.TRANSFERT_ANNULE, message.getNomFichier(), monPseudo, destinataire);
                        discussion.envoyer(messageAnnuler);
                        insererLigne("Transfert annule",
                                     transfert,true);
                        scrollerBasConversation();
                    }
                } catch (UnknownHostException uhe) {
                    insererLigne("Impossible de recevoir le fichier :'(", transfert,true);
                }
            } else if (chx == JOptionPane.NO_OPTION) {

                insererLigne("Transfert annule", transfert,true);
                scrollerBasConversation();
                Message messageAnnuler =
                    new Message(Message.TRANSFERT_ANNULE, message.getNomFichier(), monPseudo, destinataire);
                discussion.envoyer(messageAnnuler);
            }
        } else if (message.getType() == message.TRANSFERT_ANNULE) {

            insererLigne("Le transfert du fichier " + message.getNomFichier() + " a ete annule", transfert,true);
            scrollerBasConversation();
            int k = 0;
            for (int i = 0; i < listeFichierEnvoye.size(); i++) {
                if ((message.getNomFichier()).equals(listeFichierEnvoye.get(i).getFichier())) {
                    k = i;
                }
            }
            listeFichierEnvoye.remove(k);
        }
    }

    /**
     * rajoute une ligne de texte dans la conversation avec l'heure ou pas + gere l'exception
     * @param s
     */
    public void insererLigne(String s, Style style, boolean heure) {
        try {
            if (heure) {
                SimpleDateFormat h = new SimpleDateFormat("hh:mm");
                 String heureLocale = h.format(new Date());
                document.insertString(document.getLength(), "\n["+heureLocale+"]  ", informations);
            }
            document.insertString(document.getLength(), s, style);
        } catch (BadLocationException e) {
            zoneTexte.setText(zoneTexte.getText() + " Une erreur bizarre s'est produite");
        }
        scrollerBasConversation();
    }
    
    /**
     * gestion du scroller
     */
    public void scrollerBasConversation() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                scrollConversation.getVerticalScrollBar().setValue(scrollConversation.getVerticalScrollBar().getMaximum());
            }
        });
    }
    
    // Accesseurs
    
    /**
     * getter expediteur
     * @return expediteur
     */
    public String getExpediteur() {
        return destinataire;
    }


    /**
     * getter du style des informations affichees sur les tranferts
     * @return transfert
     */
    public Style getTransfer() {
        return transfert;
    }
}

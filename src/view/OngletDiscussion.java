package view;

import controller.Broadcaster;
import controller.Client;
import controller.Discussion;
import controller.EnvFichier;
import controller.RecevoirBroadcast;
import controller.RecFichier;
import controller.Serveur;

import model.InfoServeur;
import model.Message;

//dsadsa
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
import java.util.concurrent.TimeUnit;
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

/**
 * La classe OngletDiscussion s'affiche dans Fenetre et contient la discussion d'un groupe. Elle utilise 
 * la classe discussion pour communiquer avec le serveur, et donc , avec les autres utilisateurs. En outre,
 * les utilisateurs peuvent creer des conversations privees grace a la liste des objets OngletConversationPrivee()
 * qui peut se trouver dans cet onglet.
 * Elle herite de JPanel
 * @see view.Fenetre
 * @see view.OngletConversationPrivee
 * @see controller.Discussion
 * @see model.Message
 * @see controller.Serveur
 */
public class OngletDiscussion extends JPanel {
    
    //composants d'interface
    private JPanel haut = new JPanel();
    private JPanel bas = new JPanel();
    private JPanel jPanel1 = new JPanel();
    private JTextArea barreDeTexte = new JTextArea(214, 35);
    private JButton boutonEnvoyer = new JButton();
    private JButton boutonQuitter = new JButton();
    private JLabel pseudoLabel = new JLabel();
    private JScrollPane scroll1;
    private JScrollPane scroll2;
    private JList listeUtilisateurs = new JList();

    private String pseudo;
    private ArrayList<String> conversationsPrivees = new ArrayList<String>();
    private ArrayList<OngletConversationPrivee> listeConvPrive = new ArrayList<OngletConversationPrivee>();
    private InfoServeur infos;
    
    private Discussion discussion;
    private Fenetre fenetre;
    private Serveur serveur;
   
    /**
     * constructeur avec les elements de l'interface et leur proprietes
     * @param pseudo
     * @param infos
     * @param fenetre
     * @param serveur
     */
    public OngletDiscussion(String pseudo, InfoServeur infos, Fenetre fenetre, Serveur serveur) {

        this.setLayout(new BorderLayout());
        this.pseudo = pseudo;
        this.infos = infos;
        this.serveur = serveur;
        this.fenetre = fenetre;

        discussion = new Discussion(infos, this);
        Thread threadprincipal = new Thread(discussion);
        threadprincipal.start();
        this.add(discussion, BorderLayout.CENTER);
        pseudoLabel.setText(this.pseudo);
        boutonEnvoyer.setText("Envoyer");
        boutonEnvoyer.setPreferredSize(new Dimension(90, 34));
        boutonEnvoyer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                BoutonEnvoyer_actionPerformed(e);
            }
        });
        /*boutonQuitter +actionListener -appel de la methode BoutonQuitter_actionPerformed*/
        boutonQuitter.setText("Quitter");
        boutonQuitter.addActionListener(new ActionListener() {
        	/**
        	 * action du bouton quitter
        	 */
            public void actionPerformed(ActionEvent e) {
                BoutonQuitter_actionPerformed(e);
            }
        });
        pseudoLabel.setText(pseudo);
        haut.add(pseudoLabel, null);
        haut.add(boutonQuitter, null);
        this.add(haut, BorderLayout.NORTH);
        bas.add(jPanel1, null);
        bas.add(boutonEnvoyer, null);
        /*line wrapping et style word pour JTextArea barreDeTexte*/
        barreDeTexte.setRows(2);
        barreDeTexte.setLineWrap(true);
        barreDeTexte.setWrapStyleWord(true);
        barreDeTexte.setFont(new Font("Arial", 0, 14));
        Action action1 = new AbstractAction() {
        	/**
        	 * action de la touche ENTER
        	 */
            public void actionPerformed(ActionEvent e) {
                envoyer();
            }
        };
        String keyStrokeAndKey = "ENTER";
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keyStrokeAndKey);
        InputMap im = barreDeTexte.getInputMap();
        barreDeTexte.getActionMap().put(im.get(keyStroke), action1);
        /* methode pour changer focus quand "TAB" est touche et d'annuler l'action
             * de tabulation dans barreDeTexte
             */
        Action action2 = new AbstractAction() {
        	/**
        	 * action de la touche TAB
        	 */
            public void actionPerformed(ActionEvent e) {
                boutonEnvoyer.requestFocus();
            }
        };
        KeyStroke remove = KeyStroke.getKeyStroke("TAB");
        InputMap im2 = barreDeTexte.getInputMap();
        im2.put(remove, action2);
        //
        this.add(bas, BorderLayout.SOUTH);
        scroll1 = new JScrollPane(discussion);
        scroll1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll1.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        this.add(scroll1);
        scroll2 = new JScrollPane(barreDeTexte);
        scroll2.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll2.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        scroll2.setWheelScrollingEnabled(false);
        scroll2.setAutoscrolls(true);
        jPanel1.add(scroll2, BorderLayout.CENTER);
        JScrollPane scroll = new JScrollPane(listeUtilisateurs);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        this.add(scroll, BorderLayout.EAST);
        MouseListener mouseListener = new MouseAdapter() {
        	/**
        	 * ouvre une conversation privee a partir d'un double clique sur la personne
        	 */
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    if (listeUtilisateurs.getSelectedIndex() != 0) {
                        ajouterCP((String)listeUtilisateurs.getSelectedValue());
                    }
                }
            }
        };
        listeUtilisateurs.addMouseListener(mouseListener);
        listeUtilisateurs.addKeyListener(new KeyListener() {
        	/**
        	 * redefinition de keyTyped
        	 */
            public void keyTyped(KeyEvent e) {

            }

            /**
             * redefinition de keyPressed pour lancer une conversation privee
             */
            public void keyPressed(KeyEvent e) {
                if ((!listeUtilisateurs.isSelectionEmpty()) && (listeUtilisateurs.getSelectedIndex() != 0)) {
                    if (e.getKeyCode() == e.VK_ENTER)
                        ajouterCP((String)listeUtilisateurs.getSelectedValue());
                }
            }

            /**
             * redefinition de keyReleased
             */
            public void keyReleased(KeyEvent e) {
            }
        });
    }

    /**
     * action du bouton envoyer
     * @param e
     */
    private void BoutonEnvoyer_actionPerformed(ActionEvent e) {
        envoyer();
    }

    /**
     * action du bouton quitter
     * @param e
     */
    private void BoutonQuitter_actionPerformed(ActionEvent e) {
        if (serveur != null && serveur.getInfos().equals(infos) &&
            pseudoLabel.getText().equals(serveur.getInfos().getHost())) { //si le serveur qu'on quitte est celui qu'on a cree
            int choix =
                JOptionPane.showConfirmDialog(null, "Souhaitez-vous arreter le serveur en partant ?", "Quitter",
                                              JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (choix == JOptionPane.YES_OPTION) {
                quitter();
                serveur.arreter();
                fenetre.getOngletrooms().getBoutonCreer().setEnabled(true);
            } else if (choix == JOptionPane.NO_OPTION) {
                quitter();

            }
        } else {
            quitter();
        }

    }

    /**
     * permet de quitter une conversation proprement
     */
    public void quitter() {
        try {
            discussion.quitter();
        } catch (NullPointerException e) {
            fenetre.getOnglets().remove(this);
        }
        fenetre.getOnglets().remove(this);
        for (int i = 0; i < fenetre.getOnglets().getTabCount(); i++) {
            if (conversationsPrivees.size() != 0) {
                for (int j = 0; j < conversationsPrivees.size(); j++) {
                    if (("#" + conversationsPrivees.get(j)).equals(fenetre.getOnglets().getTitleAt(i))) {
                        fenetre.getOnglets().remove(i);
                    }
                }
            }
            listeConvPrive.clear();
            conversationsPrivees.clear();
        }
    }

    /**
     * permet d'envoyer un message pour la discussion
     */
    private void envoyer() {
        boolean espaces = true;
        if (barreDeTexte.getText().length() != 0) {
            int i = 0;
            while ((espaces == true) && (i < barreDeTexte.getText().length())) {
                if (!Character.isWhitespace(barreDeTexte.getText().charAt(i)))
                    espaces = false;
                else
                    espaces = true;
                i++;
            }
        } else
            espaces = true;
        if (espaces == false) {
            Message message = new Message(1, barreDeTexte.getText(), pseudoLabel.getText(), null);
            discussion.envoyer(message);
            barreDeTexte.setText(null);
        }
    }

    /**
     * cree une conversation privee entre 2 utilisateurs
     * @param nom
     */
    public void ajouterCP(String nom) {
        pseudo = pseudoLabel.getText();
        if (!nom.equals(pseudo)) {
            conversationsPrivees.add(nom);
            listeConvPrive.add(new OngletConversationPrivee(nom, pseudo, this, fenetre, discussion));
            fenetre.ajouterOnglet(listeConvPrive.get(listeConvPrive.size()-1));

        }
    }

    /**
     * actualise la conversation privee
     * @param message
     */
    public void updateCP(Message message) {
        int length = conversationsPrivees.size();
        if (conversationsPrivees.size() != 0) {
            boolean contain = false;
            for (int i = 0; i < conversationsPrivees.size(); i++) {
                if (message.getExpediteur().equals(conversationsPrivees.get(i))) {
                    contain = true;
                    listeConvPrive.get(i).updateConversationPrive(message);
                }
            }
            if (contain == false) {
                ajouterCP(message.getExpediteur());
                listeConvPrive.get(length).updateConversationPrive(message);
            }
        } else {
            ajouterCP(message.getExpediteur());
            listeConvPrive.get(0).updateConversationPrive(message);
        }
    }
    
    /**
     * permet de retirer un utilisateur de la conversation
     * @param nomExpediteur
     */
    public void quitterConversation(String nomExpediteur) {
        for (int i = 0; i < conversationsPrivees.size(); i++) {
            if (nomExpediteur.equals(conversationsPrivees.get(i))) {
                conversationsPrivees.remove(conversationsPrivees.get(i));
                listeConvPrive.remove(listeConvPrive.get(i));
                break;
            }
        }

    }
    
    /**
     * gestion du scroller
     */
    public void scrollerBasDiscussion() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                scroll1.getVerticalScrollBar().setValue(scroll1.getVerticalScrollBar().getMaximum());
            }
        });

    }

    /**
     * getter de la liste de participants a la discussion
     * @return la liste d'utilisateurs
     */
    public JList getListeUtilisateurs() {
        return listeUtilisateurs;
    }

    /**
     * getter du pseudo de l'utilisateur
     * @return le pseudo
     */
    public String getPseudo() {
        return pseudo;
    }

    /**
     * getter du serveur qui heberge la discussion
     * @return le serveur
     */
    public Serveur getServeur() {
        return serveur;
    }

    /**
     * getter du nom de la discussion
     * @return le nom de la discussion
     */
    public String getNomDiscussion() {
        String nomDiscussion = infos.getNom() + " - connecte en tant que que " + pseudo;
        int caracteresMax = 18;
        if (nomDiscussion.length() > caracteresMax)
            return nomDiscussion.substring(0, caracteresMax - 3) + "...";
        else
            return nomDiscussion;
    }

    /**
     * setter de l'affichage du pseudo de l'utilisateur
     * @param s
     */
    public void setPseudoLabel(String s) {
        pseudoLabel.setText(s);
    }

    /**
     * getter de l'affichage du pseudo de l'utilisateur
     * @return le contenu du pseudoLabel
     */
    public JLabel getPseudoLabel() {
        return pseudoLabel;
    }
}

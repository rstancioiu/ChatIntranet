package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * classe fenetre, interface de l'application qui va contenir plusieurs onglets correspondants
 * aux differentes fonctionnalites du chat
 * @see view.Application
 * @see view.OngletDiscussion
 * @see view.OngletRooms
 * @see view.OngletConversationPrivee
 */
public class Fenetre extends JFrame {
    
    private JTabbedPane Onglets = new JTabbedPane();
    private OngletRooms ongletrooms = new OngletRooms(this);
    private ArrayList<OngletDiscussion> tabDiscussions = new ArrayList<OngletDiscussion>();
    
    private BorderLayout borderLayout1 = new BorderLayout();
 
   
    /**
     * constructeur de la fenetre
     */
    public Fenetre() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * contenu de la fenetre a la construction 
     * @throws Exception
     */
    private void jbInit() throws Exception {
        this.getContentPane().setLayout(borderLayout1);
        this.setSize(new Dimension(600, 400));
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                this_windowClosing(e);
            }
        });
        Onglets.addTab("Liste des Rooms", ongletrooms);
        this.getContentPane().add(Onglets, BorderLayout.CENTER);
        Onglets.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    }
    
    /**
     * ferme les discussions proprement a la fermeture de la fenetre (quand on appuie sur la croix)
     * @param e
     */
    private void this_windowClosing(WindowEvent e) {
        for (int i = 0; i < tabDiscussions.size(); i++) {
            tabDiscussions.get(i).quitter();
            if (tabDiscussions.get(i) != null) {
                if(tabDiscussions.get(i).getServeur()!=null) {
                    tabDiscussions.get(i).getServeur().arreter();
                }
                
            }
        }
        System.exit(0);
    }

    /**
     * ajoute un onglet a la fenetre
     * @param od
     */
    public void ajouterOnglet(JPanel od) {
        if (od instanceof OngletDiscussion) {
            tabDiscussions.add(((OngletDiscussion) od));
            Onglets.addTab(((OngletDiscussion) od).getNomDiscussion(), od);
            Onglets.setSelectedIndex(Onglets.getTabCount() - 1);
        } else if (od instanceof OngletConversationPrivee) {
            Onglets.addTab("#" + ((OngletConversationPrivee) od).getExpediteur(), od);
            Onglets.setSelectedIndex(Onglets.getTabCount() - 1);
        }
    }

    /**
     * getter pour tous les onglets
     * @return onglets
     */
    public JTabbedPane getOnglets() {
        return Onglets;
    }
    
    /**
      * getter de l'onglet affichant les rooms
      * @return ongletrooms
      */
    public OngletRooms getOngletrooms() {
        return ongletrooms;
    }
}

package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import java.awt.GridLayout;

import java.awt.List;

import java.awt.event.MouseListener;

import java.util.ArrayList;
import java.util.Timer;

import java.util.TimerTask;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import model.InfoServeur;

@SuppressWarnings("oracle.jdeveloper.java.serialversionuid-field-missing")
public class ListeServeur extends JPanel {
    private  Timer refreshList;
    private ArrayList<InfoServeur> liste= new ArrayList<InfoServeur>();
    private DefaultListModel model;
    private ListeServeur listeServeur;
    private OngletRooms or;
    private int position;
    private int limit=20;
    
    public ListeServeur(OngletRooms or) {
        this.or=or;
        this.setLayout(new GridLayout(limit,1));
        this.setFont(new Font("Arial",Font.BOLD,14));  
        this.setBackground(Color.white);
        listeServeur=this;
        refreshList = new Timer();
        refreshList.schedule(new RefreshJlist(), 0, 18 * 1000);

    }
    
    public void addServeur(InfoServeur infos) {
        boolean contient=false;
        if(liste.size()!=0) {
            for(int i=0;i<liste.size() && !contient;++i) {
                if(liste.get(i).equals(infos)) {
                    if(infos.getClients() !=liste.get(i).getClients()) {
                        liste.set(i,infos);
                        SwingUtilities.invokeLater(new UpdateList());
                    }
                    contient=true;
                }           
            }
        }
        if (contient == false) {
            /* si la donnee recue n'est pas deja dans la liste*/
            liste.add(infos);
            SwingUtilities.invokeLater(new UpdateList());
        }
    }

   public void verifier(InfoServeur info) {
        for(int i=0;i<liste.size();++i) {
            if(liste.get(i).equals(info))
            position=i;
        }
        or.verifierConnection(position,info);
    }


    public int getSelectedPosition() {
        return position;
    }

    /**
     * class UpdateList qui est un Thread qui met a jour la jlist et il est invoque
     * par la methode invokeLater();
     */
    private class UpdateList implements Runnable {
        public void run() {
            listeServeur.removeAll();
            if(liste.size()>=limit){
                limit=liste.size();
                listeServeur.setLayout(new GridLayout(limit+10,1));
            }
            JPanel panelaux = new JPanel(new GridLayout(1,6,10,10));
            JLabel label1 = new JLabel("Nom");
            JLabel label2 = new JLabel("Type");
            JLabel label3 = new JLabel("Capacite");
            JLabel label4 = new JLabel("Host");
            panelaux.add(label1,BorderLayout.CENTER);
            panelaux.add(label2,BorderLayout.CENTER);
            panelaux.add(label3,BorderLayout.CENTER);
            panelaux.add(label4,BorderLayout.CENTER);
            panelaux.add(new JLabel(""));
            panelaux.add(new JLabel(""));
            listeServeur.add(panelaux);
            if (liste.size() != 0) {
                for (int i = 0; i <= liste.size() - 1; i++) {
                    JPanel panel = new ServerPanel(listeServeur,liste.get(i));
                    listeServeur.add(panel); // ajoute chaque element de la liste arrayList a la liste model
                }
            }
            listeServeur.validate();
            listeServeur.repaint();
        }
    }
    
    
    
    /**
     * Classe refreshJlist qui herite de TimerTask qui nettoie la <InfoServeur>liste
     * pour mettre a jour la disparition des serveurs;
     */
    private class RefreshJlist extends TimerTask {
        public void run() {
            liste.clear();
            SwingUtilities.invokeLater(new UpdateList());
        }
    }
    
    public String getAdresseByIndex(int index) {
        return liste.get(index).getAdresse();
    }
}

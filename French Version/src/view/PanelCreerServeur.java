package view;

import java.awt.BorderLayout;

import java.awt.Dimension;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;

import java.text.NumberFormat;

import javax.imageio.ImageIO;


import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import model.InfoServeur;

/**
 * fenetre de renseignement pour creer un serveur
 * classe qui herite de Jpanel.
 */
public class PanelCreerServeur extends JPanel {
    @SuppressWarnings("compatibility:2693087837549418492")
    private static final long serialVersionUID = 1L;
    private JCheckBox checkbox = new JCheckBox();
    private JPasswordField mdpServeur = new JPasswordField();
    private JFormattedTextField tailleServeur;
    private JTextField nomServeur;
    private static int TAILLE_SERVEUR_DEFAULT = 25;

    /**
     * constructeur de la fenetre CreerServeur
     */
    public PanelCreerServeur() {
        JLabel[] labels = new JLabel[4];
        labels[0] = new JLabel("");
        try {
            BufferedImage myPicture = ImageIO.read(this.getClass().getClassLoader().getResource("server.png"));
            labels[0] = new JLabel(new ImageIcon(myPicture));
        }catch (IOException e) {
            System.out.println("Image error");
        }
        nomServeur = new JTextField();
        this.setLayout(new BorderLayout());
        NumberFormat f = NumberFormat.getNumberInstance();
        f.setParseIntegerOnly(true);
        f.setMaximumIntegerDigits(3);
        tailleServeur = new JFormattedTextField(f);
        nomServeur.setPreferredSize(new Dimension(80,20));
        tailleServeur.setPreferredSize(new Dimension(80,20));
        mdpServeur.setPreferredSize(new Dimension(80,20));
        tailleServeur.setText("25");
        labels[1] = new JLabel("Nom du Groupe  :");
        labels[2] = new JLabel("Taille du Groupe :");
        labels[3] = new JLabel("Mot de passe     :");
        JPanel panel0= new JPanel();
        panel0.add(labels[0]);
        JPanel panel1 = new JPanel(new BorderLayout(10,10));
        panel1.add(labels[1],BorderLayout.WEST);
        panel1.add(nomServeur,BorderLayout.EAST);
        JPanel panel2 = new JPanel(new BorderLayout(10,10));
        panel2.add(labels[2],BorderLayout.WEST);
        panel2.add(tailleServeur,BorderLayout.EAST);
        JPanel panel3 = new JPanel(new BorderLayout(23,10));
        panel3.add(labels[3],BorderLayout.WEST);
        panel3.add(mdpServeur,BorderLayout.CENTER);
        panel3.add(checkbox,BorderLayout.EAST);
        JPanel panel4 = new JPanel(new BorderLayout());
        panel4.add(panel1,BorderLayout.NORTH);
        panel4.add(panel2,BorderLayout.SOUTH);
        this.add(panel0,BorderLayout.NORTH);
        this.add(panel4,BorderLayout.WEST);
        this.add(panel3,BorderLayout.SOUTH);
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

    public String getServerType() {
        if (mdpServeur.isEnabled() == false) {
            return "public";
        } else {
            return "prive";
        }
    }

    public String getNomServeur() {
        return nomServeur.getText();
    }

    public int getTailleServeur() {
        int taille;
        try {
            taille = Math.abs(Integer.parseInt(tailleServeur.getText()));
        } catch (NumberFormatException nfe) {
            taille = TAILLE_SERVEUR_DEFAULT;
        }
        if (taille == 0)
            taille = TAILLE_SERVEUR_DEFAULT;
        return taille;
    }

    public String getPassword() {
        return (new String(mdpServeur.getPassword()));
    }
}

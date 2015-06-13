package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import java.awt.image.BufferedImage;

import java.io.File;

import java.io.IOException;

import javax.imageio.ImageIO;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

/**
 * fenetre de dialogue pour renseigner le mot de passe et le pseudo
 * classe privee qui herite de Jpanel
 */
public class PanelLogin extends JPanel {
    @SuppressWarnings("compatibility:8855321997026087171")
    private static final long serialVersionUID = 1L;
    private JPanel jPanel2 = new JPanel();
    private JPanel jPanel3 = new JPanel();
    private JLabel jLabel1 = new JLabel();
    private JLabel jLabel2 = new JLabel();
    private GridLayout gridLayout1 = new GridLayout();
    private GridLayout gridLayout2 = new GridLayout();
    private JTextField nomUtilisateur = new JTextField();
    private JPasswordField password = new JPasswordField();

    /**
     * constructeur de la fenetre de dialogue Login
     */
    public PanelLogin(String pseudo) {
        JLabel picLabel = new JLabel("");
        
        try {
        
            BufferedImage myPicture = ImageIO.read(this.getClass().getClassLoader().getResource("user.png"));
            picLabel = new JLabel(new ImageIcon(myPicture));
            this.add(picLabel);
        }catch (IOException e) {
            System.out.println("Image error");
        }

        nomUtilisateur.setText(pseudo);
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
        this.add(picLabel,BorderLayout.NORTH);
        this.add(jPanel2, BorderLayout.CENTER);
        this.add(jPanel3, BorderLayout.SOUTH);
        jPanel3.add(jLabel2, BorderLayout.WEST);
        jPanel3.add(password, BorderLayout.CENTER);
    }
    
    public String getPassword() {
        return (new String(password.getPassword()));
    }
    
    public String getNomUtilisateur() {
        return nomUtilisateur.getText();
    }
}


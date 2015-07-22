package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

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

import model.Language;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class CreateServerPanel extends JPanel {
    private static final Logger log = LogManager.getLogger();
    private JCheckBox checkbox = new JCheckBox();
    private JPasswordField serverPassword = new JPasswordField();
    private JFormattedTextField serverSize;
    private JTextField serverName;
    private static int SERVER_SIZE_DEFAULT = 25;
    private Language language;

    public CreateServerPanel(Language language) {
        this.language=language;  
        log.info("Creation of a server");
        
        JLabel[] labels = new JLabel[4];
        labels[0] = new JLabel("");
        try {
            BufferedImage myPicture = ImageIO.read(this.getClass().getClassLoader().getResource("resources/server.png"));
            labels[0] = new JLabel(new ImageIcon(myPicture));
        }catch (IOException e) {
            System.out.println("Image error");
        }
        serverName = new JTextField();
        this.setLayout(new BorderLayout());
        NumberFormat f = NumberFormat.getNumberInstance();
        f.setParseIntegerOnly(true);
        f.setMaximumIntegerDigits(3);
        serverSize = new JFormattedTextField(f);
        serverName.setPreferredSize(new Dimension(80,20));
        serverSize.setPreferredSize(new Dimension(80,20));
        serverPassword.setPreferredSize(new Dimension(80,20));
        serverSize.setText("25");
        labels[1] = new JLabel(language.getValue("NAME"));
        labels[2] = new JLabel(language.getValue("SIZE"));
        labels[3] = new JLabel(language.getValue("PASSWORD"));
        JPanel panel0= new JPanel();
        panel0.add(labels[0],BorderLayout.EAST);
        JPanel panel1 = new JPanel(new GridLayout(1,3));
        panel1.add(labels[1],BorderLayout.EAST);
        panel1.add(serverName);
        JPanel panel2 = new JPanel(new GridLayout(1,3));
        panel2.add(labels[2],BorderLayout.EAST);
        panel2.add(serverSize);
        JPanel panel3 = new JPanel(new GridLayout(1,3));
        panel3.add(labels[3],BorderLayout.EAST);
        panel3.add(serverPassword);
        panel3.add(checkbox);
        JPanel panel4 = new JPanel(new BorderLayout());
        panel4.add(panel1,BorderLayout.NORTH);
        panel4.add(panel2,BorderLayout.SOUTH);
        this.add(panel0,BorderLayout.NORTH);
        this.add(panel4,BorderLayout.WEST);
        this.add(panel3,BorderLayout.SOUTH);
        serverPassword.setEnabled(false);
        checkbox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (checkbox.isSelected()) {
                    serverPassword.setEnabled(true);
                } else {
                    serverPassword.setEnabled(false);
                    serverPassword.setText("");
                }

            }
        });
    }

    public String getServerType() {
        if (serverPassword.isEnabled() == false) {
            return "public";
        } else {
            return "prive";
        }
    }

    public String getServerName() {
        return serverName.getText();
    }

    public int getServerSize() {
        int taille;
        try {
            taille = Math.abs(Integer.parseInt(serverSize.getText()));
        } catch (NumberFormatException nfe) {
            taille = SERVER_SIZE_DEFAULT;
        }
        if (taille == 0)
            taille = SERVER_SIZE_DEFAULT;
        return taille;
    }

    public String getPassword() {
        return (new String(serverPassword.getPassword()));
    }
    
}

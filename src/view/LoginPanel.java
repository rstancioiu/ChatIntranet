package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;

import java.io.IOException;

import javax.imageio.ImageIO;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

import model.Language;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoginPanel extends JPanel {
    
    private static final Logger log = LogManager.getLogger();
    private JPanel jPanel2 = new JPanel();
    private JPanel jPanel3 = new JPanel();
    private JLabel jLabel1 = new JLabel();
    private JLabel jLabel2 = new JLabel();
    private GridLayout gridLayout1 = new GridLayout();
    private GridLayout gridLayout2 = new GridLayout();
    private JTextField userName = new JTextField();
    private JPasswordField password = new JPasswordField();
    
    private Language language;

    public LoginPanel(String alias,Language language) {
        this.language=language;
        JLabel picLabel = new JLabel("");
        
        log.info(alias + " is trying to log in");
        
        try {
        
            BufferedImage myPicture = ImageIO.read(this.getClass().getClassLoader().getResource("resources/user.png"));
            picLabel = new JLabel(new ImageIcon(myPicture));
            this.add(picLabel);
        }catch (IOException e) {
            System.out.println("Image error");
        }

        userName.setText(alias);
        this.setLayout(new BorderLayout());
        password.setText("");
        jPanel2.setLayout(gridLayout2);
        jPanel3.setLayout(gridLayout1);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, jPanel2, jPanel3);
        splitPane.setResizeWeight(0.8);
        add(splitPane, BorderLayout.CENTER);
        splitPane.setContinuousLayout(true);
        jLabel1.setText(language.getValue("ALIAS"));
        jLabel1.setLabelFor(userName);
        userName.setPreferredSize(new Dimension(80, 20));
        jLabel2.setText(language.getValue("PASSWORD"));
        password.setPreferredSize(new Dimension(80, 20));
        jPanel2.add(jLabel1, BorderLayout.WEST);
        jPanel2.add(userName, BorderLayout.CENTER);
        this.add(picLabel,BorderLayout.NORTH);
        this.add(jPanel2, BorderLayout.CENTER);
        this.add(jPanel3, BorderLayout.SOUTH);
        jPanel3.add(jLabel2, BorderLayout.WEST);
        jPanel3.add(password, BorderLayout.CENTER);
    }
    
    public String getPassword() {
        return (new String(password.getPassword()));
    }
    
    public String getUserName() {
        return userName.getText();
    }
}


package view;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import model.Language;

public class Application {
    private static final Logger log = LogManager.getLogger();

    public Application() {
        Object[] selectionValues = { "English", "French" ,"Romanian" };
        String initialSelection = "English";
        Object selection =
            JOptionPane.showInputDialog(null, "Select language : ", "Language", JOptionPane.PLAIN_MESSAGE, null,
                                        selectionValues, initialSelection);
        String select = "default";
        if (selection != null) {
            select = selection.toString();
        }
        Language languageChosen = new Language(select);
        log.info("Language chosen :" + select);
        log.info(languageChosen.getValue("SEND"));
        Window window = new Window(languageChosen);
        window.setTitle("ChatIntranet");
        window.setVisible(true);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = window.getSize();
        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }
        window.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        new Application();
    }
}

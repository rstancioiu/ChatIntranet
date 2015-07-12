package view;

import java.awt.Dimension;

import java.awt.Toolkit;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import model.Language;

public class Application {


    public Application() {
        Object[] selectionValues = { "English", "French"};
        String initialSelection = "English";
        Object selection =
            JOptionPane.showInputDialog(null, "Select language : ", "Language", JOptionPane.PLAIN_MESSAGE, null,
                                        selectionValues, initialSelection);
        if (selection != null) {
            Language languageChosen = new Language(selection.toString());
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

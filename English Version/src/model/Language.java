package model;

import java.io.File;
import java.io.FileInputStream;

import java.nio.channels.FileChannel;

import java.util.ResourceBundle;

public class Language {
    private ResourceBundle rb;

    public Language(String language) {
        if (language.equals("English")) {
            rb = ResourceBundle.getBundle("language.language_en");
            copy("language/language_en.properties", "language/language.properties");
        } else if (language.equals("French")) {
            rb = ResourceBundle.getBundle("language.language_fr");
            copy("language/language_fr.properties", "language/language.properties");
        } else {
            rb = ResourceBundle.getBundle("language.language");
        }
    }

    public void copy(String filePath1, String filePath2) {
        try {
            File file1 = new File(filePath1);
            File file2 = new File(filePath2);
            FileChannel src = new FileInputStream(file1).getChannel();
            FileChannel dest = new FileInputStream(file2).getChannel();
            dest.transferFrom(src, 0, src.size());
        } catch (Exception e) {
        }
    }

    public String getValue(String key) {
        return rb.getString(key);
    }
}

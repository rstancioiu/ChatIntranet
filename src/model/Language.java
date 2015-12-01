package model;

import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Language {

    private static final Logger log = LogManager.getLogger();
    private ResourceBundle rb;

    public Language(String language) {
        if (language.equals("English")) {
            rb = ResourceBundle.getBundle("language.language_en");
        } else if (language.equals("French")) {
            rb = ResourceBundle.getBundle("language.language_fr");
        } else if(language.equals("Romanian")) {
            rb = ResourceBundle.getBundle("language.language_ro");
        } else {
            rb = ResourceBundle.getBundle("language.language");
        }
    }

    public String getValue(String key) {
        return rb.getString(key);
    }
}

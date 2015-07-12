package model;

import java.util.ResourceBundle;

public class Language {
    private ResourceBundle rb;
        
    public Language(String language) {
        if(language.equals("English")){
            rb = ResourceBundle.getBundle("language.language_en");
        }
        else if(language.equals("French")) {
            rb = ResourceBundle.getBundle("language.language_fr");
        }
        else {
            rb = ResourceBundle.getBundle("language.language");
        }
    }
    
    public String getValue(String key) {
        return rb.getString(key);
    }
}

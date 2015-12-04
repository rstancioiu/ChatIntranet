package model;

import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Language handles the chosen language
 * 
 * @author Afkid
 */
public class Language {

	private static final Logger log = LogManager.getLogger();
	private ResourceBundle rb;

	/**
	 * Constructor of language
	 * 
	 * @param language
	 */
	public Language(String language) {
		if (language.equals("English")) {
			rb = ResourceBundle.getBundle("language.language_en");
		} else if (language.equals("French")) {
			rb = ResourceBundle.getBundle("language.language_fr");
		} else if (language.equals("Romanian")) {
			rb = ResourceBundle.getBundle("language.language_ro");
		} else {
			rb = ResourceBundle.getBundle("language.language");
		}
	}

	/**
	 * @param key
	 * @return a string value that represents the value found in the
	 *         language.properties given the string key
	 */
	public String getValue(String key) {
		return rb.getString(key);
	}
}

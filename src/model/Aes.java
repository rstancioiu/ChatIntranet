package model;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

/**
 * Aes encrypts and decrypts a message 
 * 
 * @author Afkid
 */
public class Aes {
	
	/**
	 * Constructor of the class
	 */
	public Aes() {
		super();
	}

	private static final String ALGO = "AES";
	private static final byte[] keyValueInfos = new byte[] { 'd', 'g', 'e', 'q', 'e', '|', 't', 'm', 'e', '*', ']', 'e',
			't', '.', 'e', 'y' };
	private static final byte[] keyValueMessage = new byte[] { 'a', '2', 'f', 'q', '.', '|', 'p', '/', 'w', 'c', ']',
			'^', '6', '.', 'e', '?' };

	/**
	 * @param Data
	 * @param type
	 * @return an encrypted string 
	 * @throws Exception
	 */
	public String encrypt(String Data, int type) throws Exception {
		Key key;
		if (type == 0)
			key = generateKeyInfos();
		else
			key = generateKeyMessage();
		Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
		c.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(new byte[16]));
		byte[] encVal = c.doFinal(Data.getBytes());
		String encryptedValue = Base64.encodeBase64String(encVal);
		return encryptedValue;
	}

	/**
	 * @param encryptedData
	 * @param type
	 * @return the decrypted string 
	 * @throws Exception
	 */
	public String decrypt(String encryptedData, int type) throws Exception {
		Key key;
		if (type == 0)
			key = generateKeyInfos();
		else
			key = generateKeyMessage();
		Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
		c.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(new byte[16]));
		byte[] decordedValue = Base64.decodeBase64(encryptedData);
		byte[] decValue = c.doFinal(decordedValue);
		String decryptedValue = new String(decValue);
		return decryptedValue;
	}

	private Key generateKeyInfos() throws Exception {
		Key key = new SecretKeySpec(keyValueInfos, ALGO);
		return key;
	}

	private Key generateKeyMessage() throws Exception {
		Key key = new SecretKeySpec(keyValueMessage, ALGO);
		return key;
	}
}

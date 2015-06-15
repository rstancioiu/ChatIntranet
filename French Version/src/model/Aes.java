package model;

import java.security.*;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.*;

public class Aes {
    public Aes() {
        super();
    }

    private static final String ALGO = "AES";
    private static final byte[] keyValueInfos = new byte[] {
        'd', 'g', 'e', 'q', 'e', '|', 't', 'm', 'e', 'c', ']', 'e', 't', '.', 'e', 'y'
    };
    private static final byte[] keyValueMessage = new byte[] {
        'd', 'g', 'e', 'q', 'e', '|', 't', 'm', 'e', 'c', ']', 'e', 't', '.', 'e', 'y'
    };

    public String encrypt(String Data,int type) throws Exception {
        Key key;                                                                     
        if(type==0)
            key = generateKeyInfos();
        else 
            key = generateKeyMessage();
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = c.doFinal(Data.getBytes());
        String encryptedValue = new BASE64Encoder().encode(encVal);
        return encryptedValue;
    }

    public String decrypt(String encryptedData,int type) throws Exception {
        Key key;                                                                     
        if(type==0)
            key = generateKeyInfos();
        else
            key = generateKeyMessage();
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decordedValue = new BASE64Decoder().decodeBuffer(encryptedData);
        byte[] decValue = c.doFinal(decordedValue);
        String decryptedValue = new String(decValue);
        return decryptedValue;
    }

    private Key generateKeyInfos() throws Exception {
        Key key = new SecretKeySpec(keyValueInfos, ALGO);
        return key;
    }
    
    private Key generateKeyMessage() throws Exception {
        Key key = new SecretKeySpec(keyValueInfos, ALGO);
        return key;
    }
}

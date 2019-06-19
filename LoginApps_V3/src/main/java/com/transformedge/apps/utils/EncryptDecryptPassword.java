package com.transformedge.apps.utils;

import java.nio.ByteBuffer;
import java.security.AlgorithmParameters;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.stereotype.Component;

@Component
public class EncryptDecryptPassword {
	
	private final String key = "PBKDF2WithHmacSHA1";
	private final String myPasswordKey = "Hello";
	public String encrypt(String word) throws Exception {

		byte[] ivBytes;
		
		/*you can give whatever you want for password. This is for testing purpose*/
		SecureRandom random = new SecureRandom();
		byte bytes[] = new byte[20];
		random.nextBytes(bytes);
		byte[] saltBytes = bytes;
		
		// Derive the key
		SecretKeyFactory factory = SecretKeyFactory.getInstance(key);
		PBEKeySpec spec = new PBEKeySpec(myPasswordKey.toCharArray(),saltBytes,65556,256);
		SecretKey secretKey = factory.generateSecret(spec);
		SecretKeySpec secret = new SecretKeySpec(secretKey.getEncoded(), "AES");
		
		//encrypting the word
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, secret);
		AlgorithmParameters params = cipher.getParameters();
		ivBytes = params.getParameterSpec(IvParameterSpec.class).getIV();
		byte[] encryptedTextBytes = cipher.doFinal(word.getBytes("UTF-8"));
		//prepend salt and vi
		byte[] buffer = new byte[saltBytes.length + ivBytes.length + encryptedTextBytes.length];
		System.arraycopy(saltBytes, 0, buffer, 0, saltBytes.length);
		System.arraycopy(ivBytes, 0, buffer, saltBytes.length, ivBytes.length);
		System.arraycopy(encryptedTextBytes, 0, buffer, saltBytes.length + ivBytes.length, encryptedTextBytes.length);
		return new Base64().encodeToString(buffer);
	}
	
	  public String decrypt(String encryptedText) throws Exception {
	    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	    //strip off the salt and iv
	    ByteBuffer buffer = ByteBuffer.wrap(new Base64().decode(encryptedText));
	    byte[] saltBytes = new byte[20];
	    buffer.get(saltBytes, 0, saltBytes.length);
	    byte[] ivBytes1 = new byte[cipher.getBlockSize()];
	    buffer.get(ivBytes1, 0, ivBytes1.length);
	    byte[] encryptedTextBytes = new byte[buffer.capacity() - saltBytes.length - ivBytes1.length];
	  
	    buffer.get(encryptedTextBytes);
	    // Deriving the key
	    SecretKeyFactory factory = SecretKeyFactory.getInstance(key);
	    PBEKeySpec spec = new PBEKeySpec(myPasswordKey.toCharArray(), saltBytes, 65556, 256);
	    SecretKey secretKey = factory.generateSecret(spec);
	    SecretKeySpec secret = new SecretKeySpec(secretKey.getEncoded(), "AES");
	    cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(ivBytes1));
	    byte[] decryptedTextBytes = null;
	    try {
	      decryptedTextBytes = cipher.doFinal(encryptedTextBytes);
	    } catch (IllegalBlockSizeException e) {
	        e.printStackTrace();
	    } catch (BadPaddingException e) {
	        e.printStackTrace();
	    }
	   
	    return new String(decryptedTextBytes);
	  }
	
	public static class Test{
		static EncryptDecryptPassword encryptDecryptPassword = new EncryptDecryptPassword();
		public static void main(String[] args) {
			try {
				String password = encryptDecryptPassword.encrypt("AvinashSingh");
				System.out.println("encrypted password :::"+password);
				System.out.println("decrypted password :::"+encryptDecryptPassword.decrypt(password));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}

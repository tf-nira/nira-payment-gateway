package io.mosip.gateway.payment.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

import javax.crypto.Cipher;

import org.springframework.stereotype.Component;

@Component
public class SignerUtil {
	
	public static String encrypt(String plainText, String uraCertificatePath) throws Exception {
		 X509Certificate cert;
		 try (FileInputStream inStream = new FileInputStream(uraCertificatePath)) {
		 CertificateFactory cf = CertificateFactory.getInstance("X.509");
		 cert = (X509Certificate) cf.generateCertificate(inStream);
		 }
		 PublicKey publicKey = cert.getPublicKey();
		 Cipher encryptCipher = Cipher.getInstance("RSA");
		 encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
		 byte[] cipherText = encryptCipher.doFinal(plainText.getBytes("UTF-8"));
		 
		 return Base64.getEncoder().encodeToString(cipherText);
	}
	
	
	
	public static byte[] generateSignature(String encstr, String privateKeyPath, String alias, String
			keystorePassword) {
			 byte[] signature = null;
			 try {
			 java.security.KeyStore keyStoreFile = java.security.KeyStore.getInstance("PKCS12");
			 keyStoreFile.load(new FileInputStream(privateKeyPath), keystorePassword.toCharArray());
			 PrivateKey privateKey = (PrivateKey) keyStoreFile.getKey(alias,
			keystorePassword.toCharArray());
			 Signature dsa = Signature.getInstance("SHA1withRSA");
			 dsa.initSign(privateKey);
			 //byte[] buffer = encstr.getBytes(StandardCharsets.UTF_16LE);
			 byte[] buffer = encstr.getBytes();
			 dsa.update(buffer, 0, buffer.length);
			 signature = dsa.sign();
			 } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException |
			UnrecoverableKeyException | InvalidKeyException | SignatureException e) {
			 System.out.println(e.getMessage());
			 }
			 return signature;
	}
}

package com.kite9.server.persistence.github;

import java.security.PrivateKey;
import java.security.Signature;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.Calendar;
import java.util.Date;

/**
 * Constructs a JWT token for use with RSA authentication.
 * 
 * @author Rob Moffat
 *
 */
public class JWTHelper {
	
	public static String JWT_HEADER = "{\"alg\":\"RS256\"}";
	
	public static String JWT_CLAIMS = "{" + 
			"\"iat\":%d," + 
			"\"exp\":%d," + 
			"\"iss\":52943" + 
			"}";
	
	public static String createSignedJwt(PrivateKey privateKey) throws Exception { 
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MINUTE, 10);
		Date d2 = c.getTime();
		Date d1 = new Date();
		return createSignedJwt(d1.getTime() / 1000, d2.getTime() / 1000, privateKey);
	}
	
	/**
	 * @param user  Common Name of Symphony User
	 * @param expiration  Seconds from epoch (1.1.1970) to expiry 
	 * @param privateKey your RSA private key, matching the public key on the pod.
	 */
	public static String createSignedJwt(long iat, long expiration, PrivateKey privateKey) throws Exception { 
		String constructedClaims = String.format(JWT_CLAIMS, iat, expiration);
	    return createSignedJwtFromClaims(constructedClaims, privateKey);
	}

	public static String createSignedJwtFromClaims(String constructedClaims, PrivateKey privateKey) throws Exception {
		Encoder enc = Base64.getUrlEncoder().withoutPadding();
	    String part1 = enc.encodeToString(JWT_HEADER.getBytes());
	    String part2 = enc.encodeToString(constructedClaims.getBytes());
			
		Signature sigBuilder = Signature.getInstance("SHA256withRSA");
		sigBuilder.initSign(privateKey);
		sigBuilder.update((part1+"."+part2).getBytes());
	    byte[] sig = sigBuilder.sign();
	     
	    String part3 = enc.encodeToString(sig);
	    
	    String out = part1+"."+part2+"."+part3;
	    return out;
	}
	
	public static String decodeJwt(String in) {
		String[] parts = in.split("\\.");
		Decoder dec = Base64.getUrlDecoder();
		return new String(dec.decode(parts[0])) + new String(dec.decode(parts[1]));
	}
	
	      
}
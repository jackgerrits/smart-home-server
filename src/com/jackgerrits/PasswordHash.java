package com.jackgerrits;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Created by Jack on 2/05/2015.
 */
public class PasswordHash {
    private static final String algorithm = "SHA-256";

    public static String getHash(String password){
        MessageDigest digest = null;
        byte[] hash = new byte[0];

        try {
            digest = MessageDigest.getInstance(algorithm);
            hash = digest.digest(password.getBytes("UTF-8"));

        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return bytesToHex(hash);
    }

    public static boolean validatePassword(String password){
        Options ops = Options.get();
        String correctHash = ops.getPasswordHash();
        correctHash = correctHash.toLowerCase();
        System.out.println("Correct: "+ correctHash);
        String queryHash = getHash(password);
        queryHash = queryHash.toLowerCase();
        System.out.println("Query: "+ queryHash);

        return correctHash.equals(queryHash);
    }

    //function from http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java credit: @maybeWeCouldStealAVar
    public static String bytesToHex(byte[] bytes) {
        final char[] hexArray = "0123456789ABCDEF".toCharArray();

        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }

        return new String(hexChars);
    }
}

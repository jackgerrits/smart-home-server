package com.jackgerrits;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility methods for hashing and comparing passwords
 * @author jackgerrits
 */
public class PasswordHash {
    private static final String algorithm = "SHA-256";

    /**
     * Hashes supplied password with SHA-256
     * @param password password to be hashed
     * @return result encoded in hex
     */
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

    /**
     * Hashes inputted password and compares result with stored hash for authentication
     * @param password query password to be hashed and compared
     * @return true if hashes match
     */
    public static boolean validatePassword(String password){
        Options ops = Options.get();
        String correctHash = ops.getPasswordHash();
        correctHash = correctHash.toLowerCase();
        String queryHash = getHash(password);
        queryHash = queryHash.toLowerCase();

        return correctHash.equals(queryHash);
    }

    /**
     * Converts byte array into hex encoded string. Credit: @maybeWeCouldStealAVar [http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java]
     * @param bytes byte array to convert
     * @return encoded string
     */
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

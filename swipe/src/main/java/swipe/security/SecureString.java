package swipe.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * Used to securely store student ID numbers using Java Crypto hashing
 * https://stackoverflow.com/questions/2624192/good-hash-function-for-strings
 */
public class SecureString {
    /*The string contents AFTER hashing the input string*/
    private String contents;

    public static String SHA256 = "SHA-256";

    public SecureString(String stringToEncrypt) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance(SHA256);
        messageDigest.update(stringToEncrypt.getBytes());
        contents = new String(messageDigest.digest());
    }

    public String getContents() {
        return contents;
    }

    @Override
    public String toString() {
        return "SecureString{" +
                "contents='" + contents + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SecureString that = (SecureString) o;
        return Objects.equals(contents, that.contents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contents);
    }
}

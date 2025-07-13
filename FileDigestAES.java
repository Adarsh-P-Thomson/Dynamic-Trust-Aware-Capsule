import java.io.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

public class FileDigestAES {

    // Generate SHA-256 digest of a file (used as AES key source)
    public static byte[] getDigestFromFile(String filePath) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream fis = new FileInputStream(filePath)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }
        return digest.digest(); // 32 bytes (256 bits)
    }

    // Derive AES key from the digest (first 16 bytes for AES-128)
    public static SecretKeySpec getAESKeyFromDigest(byte[] digest) {
        return new SecretKeySpec(digest, 0, 16, "AES");
    }

    // Encrypt a file with AES
    public static void encryptFile(String inputFile, String outputFile, SecretKeySpec key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);

        try (FileInputStream fis = new FileInputStream(inputFile);
             FileOutputStream fos = new FileOutputStream(outputFile);
             CipherOutputStream cos = new CipherOutputStream(fos, cipher)) {

            byte[] buffer = new byte[8192];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                cos.write(buffer, 0, read);
            }
        }
    }

    // Decrypt a file with AES
    public static void decryptFile(String inputFile, String outputFile, SecretKeySpec key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);

        try (FileInputStream fis = new FileInputStream(inputFile);
             CipherInputStream cis = new CipherInputStream(fis, cipher);
             FileOutputStream fos = new FileOutputStream(outputFile)) {

            byte[] buffer = new byte[8192];
            int read;
            while ((read = cis.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }
        }
    }

    public static void main(String[] args) {
        try {
            if (args.length != 3) {
                System.out.println("Usage: java FileDigestAES <encrypt|decrypt> <targetFile> <keySourceFile>");
                return;
            }

            String mode = args[0];
            String targetFile = args[1];
            String keySourceFile = args[2];

            byte[] digest = getDigestFromFile(keySourceFile);
            SecretKeySpec aesKey = getAESKeyFromDigest(digest);

            if (mode.equalsIgnoreCase("encrypt")) {
                encryptFile(targetFile, targetFile + ".enc", aesKey);
                System.out.println("Encrypted to: " + targetFile + ".enc");
            } else if (mode.equalsIgnoreCase("decrypt")) {
                try {
                    decryptFile(targetFile, targetFile + ".dec", aesKey);
                    System.out.println("Decrypted to: " + targetFile + ".dec");
                } catch (BadPaddingException | IllegalBlockSizeException e) {
                    System.out.println("❌ Decryption failed!");
                    System.out.println("Reason: Possibly the wrong key, or the encrypted file is corrupted/incomplete.");
                    e.printStackTrace();
                }
            } else {
                System.out.println("Unknown mode: " + mode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

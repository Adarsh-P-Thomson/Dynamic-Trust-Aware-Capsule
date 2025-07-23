/*
================================================================================
File: src/main/java/com/dta/adminapp/services/CpsxCreatorService.java (NEW)
Description: Core logic for building the .cpsx file.
================================================================================
*/
package com.dta.adminapp.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.ZonedDateTime;
import java.util.Formatter;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CpsxCreatorService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public static class CreationResult {
        public final String originalFileHash;
        public final String encryptedKeyHex;

        public CreationResult(String originalFileHash, String encryptedKeyHex) {
            this.originalFileHash = originalFileHash;
            this.encryptedKeyHex = encryptedKeyHex;
        }
    }

    public CreationResult createCapsuleFile(File sourceFile, File destinationCpsxFile, String capsuleName, ZonedDateTime expiresAt) throws Exception {
        // 1. Generate a new random key for this capsule
        SecureRandom random = new SecureRandom();
        byte[] keyBytes = new byte[32]; // 256 bits for AES-256
        random.nextBytes(keyBytes);
        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");

        // 2. Encrypt the source file
        byte[] iv = new byte[12]; // 96 bits for GCM
        random.nextBytes(iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv); // 128 bit auth tag length
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
        byte[] encryptedContent = cipher.doFinal(Files.readAllBytes(sourceFile.toPath()));

        // Prepend IV to the encrypted content for storage
        byte[] encryptedPayload = new byte[iv.length + encryptedContent.length];
        System.arraycopy(iv, 0, encryptedPayload, 0, iv.length);
        System.arraycopy(encryptedContent, 0, encryptedPayload, iv.length, encryptedContent.length);

        // 3. Create the manifest
        ObjectNode manifest = objectMapper.createObjectNode();
        manifest.put("capsule_id", UUID.randomUUID().toString());
        manifest.put("created_by", "admin@dtacapsule.com"); // Placeholder
        manifest.put("created_at", ZonedDateTime.now().toString());
        if (expiresAt != null) {
            manifest.put("expires_at", expiresAt.toString());
        }

        // 4. Package into a .cpsx (ZIP) file
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(destinationCpsxFile))) {
            // Add manifest
            ZipEntry manifestEntry = new ZipEntry("manifest.json");
            zos.putNextEntry(manifestEntry);
            zos.write(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(manifest));
            zos.closeEntry();

            // Add encrypted payload
            ZipEntry payloadEntry = new ZipEntry("payload/" + sourceFile.getName() + ".enc");
            zos.putNextEntry(payloadEntry);
            zos.write(encryptedPayload);
            zos.closeEntry();
        }

        // 5. Calculate hash of the ORIGINAL file for the database
        String originalFileHash = calculateSHA256(sourceFile);

        // 6. Return the hash and the hex-encoded key
        return new CreationResult(originalFileHash, bytesToHex(secretKey.getEncoded()));
    }

    private String calculateSHA256(File file) throws Exception {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                sha256.update(buffer, 0, bytesRead);
            }
        }
        return bytesToHex(sha256.digest());
    }

    private String bytesToHex(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
}
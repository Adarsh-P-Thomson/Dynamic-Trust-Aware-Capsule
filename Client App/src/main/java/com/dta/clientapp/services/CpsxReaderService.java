/*
================================================================================
File: src/main/java/com/dta/clientapp/services/CpsxReaderService.java
Description: Core logic for decrypting and handling .cpsx files.
================================================================================
*/
package com.dta.clientapp.services;

import com.dta.clientapp.models.OpenedCapsule;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.zip.ZipInputStream;

public class CpsxReaderService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenedCapsule decryptAndOpen(File cpsxFile, String hexKey) throws Exception {
        byte[] keyBytes = hexToBytes(hexKey);
        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");

        byte[] manifestBytes = null;
        byte[] encryptedPayload = null;
        String originalFileName = cpsxFile.getName().replace(".cpsx", "");

        // **FIX:** Read each entry from the zip stream into a byte array first.
        // This prevents one read operation from closing the stream for the next one.
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(cpsxFile.toPath()))) {
            var entry = zis.getNextEntry();
            while (entry != null) {
                if (entry.getName().equals("manifest.json")) {
                    manifestBytes = readEntry(zis);
                } else if (entry.getName().startsWith("payload/")) {
                    originalFileName = new File(entry.getName()).getName().replace(".enc", "");
                    encryptedPayload = readEntry(zis);
                }
                entry = zis.getNextEntry();
            }
        }

        if (manifestBytes == null || encryptedPayload == null) {
            throw new IOException("Invalid capsule: missing manifest or payload.");
        }

        // Now parse and decrypt from the byte arrays, not the stream.
        JsonNode manifest = objectMapper.readTree(manifestBytes);

        ByteArrayInputStream bis = new ByteArrayInputStream(encryptedPayload);
        byte[] iv = new byte[12];
        bis.read(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
        byte[] decryptedContent = cipher.doFinal(bis.readAllBytes());

        Path tempDir = Files.createTempDirectory("dta_capsule_" + System.currentTimeMillis());
        File decryptedFile = new File(tempDir.toFile(), originalFileName);
        Files.write(decryptedFile.toPath(), decryptedContent);

        ZonedDateTime expiresAt = null;
        if (manifest.has("expires_at")) {
            expiresAt = ZonedDateTime.parse(manifest.get("expires_at").asText());
        }

        System.out.println("Successfully decrypted and opened capsule at: " + tempDir);
        return new OpenedCapsule(cpsxFile.getName(), tempDir.toFile(), expiresAt);
    }
    
    // Helper method to safely read all bytes for a single zip entry.
    private byte[] readEntry(ZipInputStream zis) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int len;
        while ((len = zis.read(buffer)) > 0) {
            baos.write(buffer, 0, len);
        }
        return baos.toByteArray();
    }

    private byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                 + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
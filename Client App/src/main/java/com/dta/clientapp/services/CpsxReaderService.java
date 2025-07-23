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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.zip.ZipInputStream;

public class CpsxReaderService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenedCapsule decryptAndOpen(File cpsxFile, String hexKey) throws Exception {
        // For now, we assume the key is provided directly. In a real app,
        // this key would be securely fetched from the server.
        byte[] keyBytes = hexToBytes(hexKey);
        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");

        JsonNode manifest = null;
        byte[] encryptedPayload = null;

        // 1. Unzip the capsule file in memory
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(cpsxFile.toPath()))) {
            var entry = zis.getNextEntry();
            while (entry != null) {
                if (entry.getName().equals("manifest.json")) {
                    manifest = objectMapper.readTree(zis);
                } else if (entry.getName().startsWith("payload/")) {
                    encryptedPayload = zis.readAllBytes();
                }
                entry = zis.getNextEntry();
            }
        }

        if (manifest == null || encryptedPayload == null) {
            throw new IOException("Invalid capsule: missing manifest or payload.");
        }

        // 2. Decrypt the payload
        ByteArrayInputStream bis = new ByteArrayInputStream(encryptedPayload);
        byte[] iv = new byte[12];
        bis.read(iv); // Read the IV from the start of the payload

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
        byte[] decryptedContent = cipher.doFinal(bis.readAllBytes());

        // 3. Create a temporary directory for the decrypted file
        Path tempDir = Files.createTempDirectory("dta_capsule_" + System.currentTimeMillis());
        File decryptedFile = new File(tempDir.toFile(), cpsxFile.getName().replace(".cpsx", ""));
        Files.write(decryptedFile.toPath(), decryptedContent);

        // 4. Parse expiry date from manifest
        ZonedDateTime expiresAt = null;
        if (manifest.has("expires_at")) {
            expiresAt = ZonedDateTime.parse(manifest.get("expires_at").asText());
        }

        System.out.println("Successfully decrypted and opened capsule at: " + tempDir);
        return new OpenedCapsule(cpsxFile.getName(), tempDir, expiresAt);
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
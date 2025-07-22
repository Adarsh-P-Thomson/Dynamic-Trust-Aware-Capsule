package com.capsule;


import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.SecretKey;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.KeyPair;
import java.time.LocalDateTime;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CapsuleCreator {

    public static void createCapsule(File inputDir, File outputCpsxFile, String createdBy, String userId, LocalDateTime expiryDate) throws Exception {
        // 1. Create working temp folder
        Path tempDir = Files.createTempDirectory("capsule");
        Path payloadDir = tempDir.resolve("payload");
        Files.createDirectories(payloadDir);

        // 2. Generate AES key and IV
        SecretKey aesKey = CryptoUtils.generateAESKey();
        byte[] iv = CryptoUtils.generateIV();

        // 3. Encrypt each file
        Map<String, String> checksums = new LinkedHashMap<>();
        for (File file : Objects.requireNonNull(inputDir.listFiles())) {
            File encrypted = payloadDir.resolve(file.getName() + ".enc").toFile();
            CryptoUtils.encryptFile(file, encrypted, aesKey, iv);
            String hash = getSHA256(encrypted);
            checksums.put(encrypted.getName(), hash);
        }

        // 4. Create manifest.json
        Manifest manifest = new Manifest();
        manifest.setCapsuleId(UUID.randomUUID().toString());
        manifest.setCreatedBy(createdBy);
        manifest.setCreatedAt(LocalDateTime.now());
        manifest.setExpiresAt(expiryDate);
        manifest.setAccessLevel("L2");
        manifest.setUserId(userId);
        new ObjectMapper().writeValue(tempDir.resolve("manifest.json").toFile(), manifest);

        // 5. Create checksums.sha256
        File checksumFile = tempDir.resolve("checksums.sha256").toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(checksumFile))) {
            for (Map.Entry<String, String> entry : checksums.entrySet()) {
                writer.write(entry.getValue() + "  " + entry.getKey());
                writer.newLine();
            }
        }

        // 6. Sign manifest + checksums
        KeyPair keyPair = SignUtils.generateKeyPair(); // Store these if needed
        byte[] signature = SignUtils.signFile(tempDir.resolve("manifest.json").toFile(), keyPair.getPrivate());
        Files.write(tempDir.resolve("access.sig"), signature);

        // 7. Zip everything into .cpsx
        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(outputCpsxFile))) {
            Files.walk(tempDir)
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    try {
                        String relativePath = tempDir.relativize(path).toString();
                        zipOut.putNextEntry(new ZipEntry(relativePath));
                        Files.copy(path, zipOut);
                        zipOut.closeEntry();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        }

        // Cleanup
        deleteDirectory(tempDir.toFile());
        System.out.println("✅ Capsule created: " + outputCpsxFile.getAbsolutePath());
    }

    private static String getSHA256(File file) throws Exception {
        byte[] bytes = Files.readAllBytes(file.toPath());
        return Base64.getEncoder().encodeToString(
            java.security.MessageDigest.getInstance("SHA-256").digest(bytes)
        );
    }

    private static void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            for (File f : Objects.requireNonNull(dir.listFiles())) {
                deleteDirectory(f);
            }
        }
        dir.delete();
    }
}

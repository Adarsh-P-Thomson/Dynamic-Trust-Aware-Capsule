/*
================================================================================
File: src/main/java/com/dta/adminapp/services/CapsuleService.java (NEW)
Description: Handles API calls related to capsules.
================================================================================
*/
package com.dta.adminapp.services;

import com.dta.adminapp.models.Capsule;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CapsuleService {
    private static final String API_BASE_URL = "http://localhost:3001/api/admin";
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public List<Capsule> getAllCapsules(String token) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/capsules"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Capsule>>() {});
        } else {
            throw new IOException("Failed to fetch capsules: " + response.body());
        }
    }

    public void createCapsule(String token, String name, String description, String fileHash, String encryptedKey, ZonedDateTime expiresAt) throws IOException, InterruptedException {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("capsule_name", name);
        payload.put("description", description);
        payload.put("file_hash_sha256", fileHash);
        payload.put("encrypted_key", encryptedKey);
        if (expiresAt != null) {
            // **FIX:** Use a custom formatter to produce the exact format PostgreSQL requires.
            // The 'XXX' pattern creates the offset with a colon (e.g., +05:30), which is standard.
            // This avoids including the non-standard Java-specific zone ID like '[Asia/Calcutta]'.
            DateTimeFormatter postgresFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
            String formattedTimestamp = expiresAt.format(postgresFormatter);
            payload.put("expires_at", formattedTimestamp);
        }
        // A default policy can be added here if needed
        payload.set("policy", objectMapper.createObjectNode());


        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/capsules"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 201) {
            throw new IOException("Failed to create capsule: " + response.statusCode() + " " + response.body());
        }
    }
}
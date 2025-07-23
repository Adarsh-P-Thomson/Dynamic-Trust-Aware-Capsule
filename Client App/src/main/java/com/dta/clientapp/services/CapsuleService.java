/*
================================================================================
File: src/main/java/com/dta/clientapp/services/CapsuleService.java (NEW)
Description: Handles client-side API calls for capsules, like fetching keys.
================================================================================
*/
package com.dta.clientapp.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public class CapsuleService {
    private static final String API_BASE_URL = "http://localhost:3001/api/client";
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Optional<String> fetchDecryptionKey(String token, String capsuleId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/capsules/" + capsuleId + "/key"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JsonNode rootNode = objectMapper.readTree(response.body());
            return Optional.ofNullable(rootNode.get("encryptedKey").asText());
        } else {
            // Log or handle specific errors like 403 Forbidden or 404 Not Found
            System.err.println("Failed to fetch key: " + response.statusCode() + " - " + response.body());
            return Optional.empty();
        }
    }
}
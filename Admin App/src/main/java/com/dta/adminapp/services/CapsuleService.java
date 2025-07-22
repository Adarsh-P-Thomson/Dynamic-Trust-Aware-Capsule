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
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
            // In a real app, you'd throw a more specific exception
            throw new IOException("Failed to fetch capsules: " + response.statusCode());
        }
    }
}
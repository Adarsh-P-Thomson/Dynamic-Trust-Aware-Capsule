/*
================================================================================
File: src/main/java/com/dta/clientapp/services/SessionService.java
Description: Manages the list of opened capsules and their expiry.
================================================================================
*/
package com.dta.clientapp.services;

import com.dta.clientapp.models.OpenedCapsule;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.ZonedDateTime;
import java.util.Timer;
import java.util.TimerTask;

public class SessionService {
    private static final SessionService INSTANCE = new SessionService();
    private final ObservableList<OpenedCapsule> openedCapsules = FXCollections.observableArrayList();
    private final ShredderService shredderService = new ShredderService();
    private final Timer expiryTimer = new Timer(true); // Daemon thread

    private SessionService() {
        // Check for expired capsules every minute
        expiryTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkAndHandleExpirations();
            }
        }, 60 * 1000, 60 * 1000);
    }

    public static SessionService getInstance() {
        return INSTANCE;
    }

    public ObservableList<OpenedCapsule> getOpenedCapsules() {
        return openedCapsules;
    }

    public void addOpenedCapsule(OpenedCapsule capsule) {
        Platform.runLater(() -> openedCapsules.add(capsule));
    }

    private void checkAndHandleExpirations() {
        System.out.println("Checking for expired capsules...");
        openedCapsules.stream()
            .filter(c -> c.getExpiresAt() != null && c.getExpiresAt().isBefore(ZonedDateTime.now()))
            .forEach(expiredCapsule -> {
                System.out.println("Capsule '" + expiredCapsule.getName() + "' has expired. Shredding temp data.");
                try {
                    shredderService.shred(expiredCapsule.getTempDirectory());
                    Platform.runLater(() -> openedCapsules.remove(expiredCapsule));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
    }

    public void shredAllSessionData() {
        expiryTimer.cancel(); // Stop the timer
        openedCapsules.forEach(capsule -> {
            try {
                shredderService.shred(capsule.getTempDirectory());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        openedCapsules.clear();
    }
}
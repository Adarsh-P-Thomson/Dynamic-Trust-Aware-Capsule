package com.capsule;

import java.util.*;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        /*if (args.length < 5) {
            System.out.println("Usage:");
            System.out.println("  java -jar capsule-creator.jar <input_folder> <output_file.cpsx> <createdByEmail> <userId> <expiryDate:yyyy-MM-ddTHH:mm>");
            return;
        }

        String inputFolderPath = args[0];
        String outputFilePath = args[1];
        String createdBy = args[2];
        String userId = args[3];
        String expiryRaw = args[4];

        File inputFolder = new File(inputFolderPath);
        File outputFile = new File(outputFilePath);

        if (!inputFolder.exists() || !inputFolder.isDirectory()) {
            System.err.println("❌ Input folder is invalid.");
            return;
        }*/
        Scanner scanner=new Scanner(System.in);
        System.out.println("--- Capsule Creator ---");

            // --- Gather all required inputs from the user ---
            System.out.print("Enter the path to the input folder: ");
            String inputFolder1 = scanner.nextLine();

            System.out.print("Enter the desired output file path (e.g., my-capsule.cpsx): ");
            String outputFile1 = scanner.nextLine();

            System.out.print("Enter the creator's email: ");
            String createdBy = scanner.nextLine();

            System.out.print("Enter the authorized User ID for the policy: ");
            String userId = scanner.nextLine();

            System.out.print("Enter the expiry date and time (format: yyyy-MM-ddTHH:mm): ");
            String expiryRaw = scanner.nextLine();

            // --- Validate inputs and create the capsule ---
            File inputFolder = new File(inputFolder1);
            File outputFile = new File(outputFile1);

            // Check if the source folder is valid before proceeding.
            if (!inputFolder.exists() || !inputFolder.isDirectory()) {
                System.err.println("❌ Input folder is invalid or does not exist.");
                return;
            }


        try {
            LocalDateTime expiryDate = LocalDateTime.parse(expiryRaw);
            CapsuleCreator.createCapsule(inputFolder, outputFile, createdBy, userId, expiryDate);
        } catch (DateTimeParseException e) {
            System.err.println("❌ Invalid date format. Use yyyy-MM-ddTHH:mm (e.g. 2025-07-20T23:00)");
        } catch (Exception e) {
            System.err.println("❌ Failed to create capsule: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

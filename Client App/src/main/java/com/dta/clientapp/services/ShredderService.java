/*
================================================================================
File: src/main/java/com/dta/clientapp/services/ShredderService.java
Description: Securely deletes files and directories.
================================================================================
*/
package com.dta.clientapp.services;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.SecureRandom;

public class ShredderService {
    public void shred(File file) throws IOException {
        if (file.isDirectory()) {
            // Recursively shred all files in the directory
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    shred(f);
                }
            }
            // Delete the now-empty directory
            if (!file.delete()) {
                 System.err.println("Failed to delete directory: " + file.getAbsolutePath());
            }
        } else {
            // Overwrite the file with random data before deleting
            if (file.exists()) {
                try (RandomAccessFile raf = new RandomAccessFile(file, "rws")) {
                    SecureRandom random = new SecureRandom();
                    for (long i = 0; i < raf.length(); i++) {
                        raf.seek(i);
                        raf.write(random.nextInt());
                    }
                }
                if (!file.delete()) {
                    System.err.println("Failed to delete shredded file: " + file.getAbsolutePath());
                }
            }
        }
    }
}
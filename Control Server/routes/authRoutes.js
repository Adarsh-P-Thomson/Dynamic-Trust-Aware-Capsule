/*
================================================================================
File: routes/authRoutes.js
Description: API routes for user authentication (login/register). (UPDATED)
================================================================================
*/

const express = require('express');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const { pgPool } = require('../config/db');
const { logEvent } = require('../services/logger');
const config = require('../config/config');

const router = express.Router();

// Generic function to generate JWT
const generateToken = (id, username, role) => {
    return jwt.sign({ id, username, role }, config.jwt.secret, {
        expiresIn: config.jwt.expiresIn,
    });
};

// --- Admin Login with Hash Debugging ---
router.post('/admin/login', async (req, res) => {
    const { email, password } = req.body;
    
    if (!email || !password) {
        return res.status(400).json({ message: "Email and password are required." });
    }

    try {
        const { rows } = await pgPool.query('SELECT * FROM dta_capsule.admins WHERE email = $1', [email]);

        if (rows.length === 0) {
            return res.status(401).json({ message: "Invalid admin credentials" });
        }

        const admin = rows[0];
        
        // --- HASH DEBUGGING LOGS ---
        console.log(`[AUTH-DEBUG] HASH FROM DB:   ${admin.password_hash}`);
        const newHashForDebug = await bcrypt.hash(password, 10); // Use 10 rounds, matching the seed script
        console.log(`[AUTH-DEBUG] HASH FROM REQ:  ${newHashForDebug}`);
        // --- END DEBUGGING ---

        const isMatch = await bcrypt.compare(password, admin.password_hash);

        if (isMatch) {
            const token = generateToken(admin.admin_id, admin.username, 'admin');
            res.json({ message: "Admin login successful", token });
        } else {
            res.status(401).json({ message: "Invalid admin credentials" });
        }
    } catch (error) {
        console.error('[AUTH-CRITICAL] Error during admin login process:', error);
        res.status(500).json({ message: "Server error during login" });
    }
});


// --- Client Login with Hash Debugging ---
router.post('/client/login', async (req, res) => {
    const { email, password } = req.body;

    if (!email || !password) {
        return res.status(400).json({ message: "Email and password are required." });
    }

    try {
        const { rows } = await pgPool.query('SELECT * FROM dta_capsule.clients WHERE email = $1', [email]);
        
        if (rows.length === 0) {
            return res.status(401).json({ message: "Invalid client credentials" });
        }
        
        const client = rows[0];

        // --- HASH DEBUGGING LOGS ---
        console.log(`[AUTH-DEBUG] HASH FROM DB:   ${client.password_hash}`);
        const newHashForDebug = await bcrypt.hash(password, 10); // Use 10 rounds, matching the seed script
        console.log(`[AUTH-DEBUG] HASH FROM REQ:  ${newHashForDebug}`);
        // --- END DEBUGGING ---

        const isMatch = await bcrypt.compare(password, client.password_hash);

        if (isMatch) {
            const token = generateToken(client.client_id, client.username, 'client');
            res.json({ message: "Client login successful", token });
        } else {
            res.status(401).json({ message: "Invalid client credentials" });
        }
    } catch (error) {
        console.error('[AUTH-CRITICAL] Error during client login process:', error);
        res.status(500).json({ message: "Server error during login" });
    }
});

module.exports = router;

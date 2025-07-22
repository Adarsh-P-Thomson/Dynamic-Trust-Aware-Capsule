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

// --- Admin Login with Enhanced Logging ---
router.post('/admin/login', async (req, res) => {
    const { email, password } = req.body;
    console.log(`[AUTH-DEBUG] Admin login attempt for email: ${email}`);

    if (!email || !password) {
        return res.status(400).json({ message: "Email and password are required." });
    }

    try {
        const { rows } = await pgPool.query('SELECT * FROM dta_capsule.admins WHERE email = $1', [email]);

        if (rows.length === 0) {
            console.log(`[AUTH-DEBUG] Failure: No admin found with email: ${email}`);
            return res.status(401).json({ message: "Invalid admin credentials" });
        }

        const admin = rows[0];
        console.log(`[AUTH-DEBUG] Admin found: ${admin.username}. Comparing password...`);
        
        //console.log(`[AUTH-DEBUG] Stored hash: ${password}`);


        const isMatch = await bcrypt.compare(password, admin.password_hash);
             //console.log(`[AUTH-DEBUG] Stored hash: ${admin.password_hash}`);
        //console.log(`[AUTH-DEBUG] Provided password (hashed for comparison): ${bcrypt.hashSync(password, 10)}`); // This is for debugging only, never store or log plain passwords
        



        if (isMatch) {
            console.log(`[AUTH-DEBUG] Success: Password matched for ${admin.username}.`);
            const token = generateToken(admin.admin_id, admin.username, 'admin');
            res.json({ message: "Admin login successful", token });
        } else {
            console.log(`[AUTH-DEBUG] Failure: Password does not match for ${admin.username}.`);
            res.status(401).json({ message: "Invalid admin credentials" });
        }
    } catch (error) {
        console.error('[AUTH-DEBUG] CRITICAL: Error during admin login process:', error);
        res.status(500).json({ message: "Server error during login" });
    }
});


// --- Client Login ---
router.post('/client/login', async (req, res) => {
    const { email, password } = req.body;
    // This route would have similar logging in a full implementation
    const { rows } = await pgPool.query('SELECT * FROM dta_capsule.clients WHERE email = $1', [email]);
    if (rows.length > 0 && await bcrypt.compare(password, rows[0].password_hash)) {
        const client = rows[0];
        const token = generateToken(client.client_id, client.username, 'client');
        res.json({ message: "Client login successful", token });
    } else {
        res.status(401).json({ message: "Invalid client credentials" });
    }
});

module.exports = router;


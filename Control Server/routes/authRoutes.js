/*
================================================================================
File: routes/authRoutes.js
Description: API routes for user authentication (login/register).
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

// --- Admin Login ---
router.post('/admin/login', async (req, res) => {
    const { email, password } = req.body;
    // ... (Full implementation would query the 'admins' table, check password, and return a JWT)
    // This is a placeholder for brevity
    const { rows } = await pgPool.query('SELECT * FROM dta_capsule.admins WHERE email = $1', [email]);
    if (rows.length > 0 && await bcrypt.compare(password, rows[0].password_hash)) {
        const admin = rows[0];
        const token = generateToken(admin.admin_id, admin.username, 'admin');
        res.json({ message: "Admin login successful", token });
    } else {
        res.status(401).json({ message: "Invalid admin credentials" });
    }
});

// --- Client Login ---
router.post('/client/login', async (req, res) => {
    const { email, password } = req.body;
    // ... (Full implementation would query the 'clients' table)
    const { rows } = await pgPool.query('SELECT * FROM dta_capsule.clients WHERE email = $1', [email]);
    if (rows.length > 0 && await bcrypt.compare(password, rows[0].password_hash)) {
        const client = rows[0];
        const token = generateToken(client.client_id, client.username, 'client');
        res.json({ message: "Client login successful", token });
    } else {
        res.status(401).json({ message: "Invalid client credentials" });
    }
});


// Note: Registration routes would be similar, using bcrypt.hash to store passwords.

module.exports = router;


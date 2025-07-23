
/*
================================================================================
File: routes/adminRoutes.js
Description: Protected API routes for administrators.
================================================================================
*/

const express = require('express');
const { protect } = require('../middleware/authMiddleware');
const { pgPool } = require('../config/db');
const { logEvent } = require('../services/logger');

const router = express.Router();

// All routes in this file are protected and require the 'admin' role
router.use(protect(['admin']));

// --- Create a new capsule ---
router.post('/capsules', async (req, res) => {
    const { capsule_name, description, file_hash_sha256, encrypted_key, policy, expires_at } = req.body;
    const creator_admin_id = req.user.id; // from JWT

    // Basic validation
    if (!capsule_name || !file_hash_sha256 || !encrypted_key) {
        return res.status(400).json({ message: "Missing required fields: capsule_name, file_hash_sha256, encrypted_key" });
    }

    try {
        const query = `
            INSERT INTO dta_capsule.capsule_details 
            (creator_admin_id, capsule_name, description, file_hash_sha256, encrypted_key, policy, expires_at)
            VALUES ($1, $2, $3, $4, $5, $6, $7) 
            RETURNING *
        `;
        
        // Convert the hex string key from the client into a Buffer for the BYTEA column
        const keyBuffer = Buffer.from(encrypted_key, 'hex');

        const values = [
            creator_admin_id,
            capsule_name,
            description || null,
            file_hash_sha256,
            keyBuffer,
            policy || {},
            expires_at || null
        ];

        const result = await pgPool.query(query, values);
        const newCapsule = result.rows[0];

        // Log the successful creation event
        logEvent({
            actor: { id: req.user.id, username: req.user.username, type: 'admin' },
            action: { type: 'CAPSULE_CREATE', outcome: 'success' },
            resource: { type: 'capsule', id: newCapsule.capsule_id }
        });

        res.status(201).json(newCapsule);

    } catch (error) {
        console.error("Error creating capsule:", error);
        logEvent({
            actor: { id: req.user.id, username: req.user.username, type: 'admin' },
            action: { type: 'CAPSULE_CREATE', outcome: 'failure' },
            context: { error: error.message }
        });
        res.status(500).json({ message: "Server error while creating capsule" });
    }
});

// --- Grant access to a capsule ---
router.post('/capsules/:capsuleId/grant', async (req, res) => {
    const { capsuleId } = req.params;
    const { clientId, access_expires_at } = req.body;
    const granting_admin_id = req.user.id;

    // ... (Full implementation would insert into capsule_access_grants)
    res.json({ message: `Access to capsule ${capsuleId} granted to client ${clientId}` });
});

// --- Get all capsules ---
router.get('/capsules', async (req, res) => {
     const { rows } = await pgPool.query('SELECT capsule_id, capsule_name, status, lifecycle_status, created_at, expires_at FROM dta_capsule.capsule_details ORDER BY created_at DESC');
     res.json(rows);
});


module.exports = router;
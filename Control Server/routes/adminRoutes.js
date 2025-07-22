
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

    try {
        const result = await pgPool.query(
            `INSERT INTO dta_capsule.capsule_details (creator_admin_id, capsule_name, description, file_hash_sha256, encrypted_key, policy, expires_at)
             VALUES ($1, $2, $3, $4, $5, $6, $7) RETURNING *`,
            [creator_admin_id, capsule_name, description, file_hash_sha256, Buffer.from(encrypted_key, 'hex'), policy, expires_at]
        );
        const newCapsule = result.rows[0];

        logEvent({ /* ... log capsule creation ... */ });
        res.status(201).json(newCapsule);
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: "Error creating capsule" });
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
    // ... (Full implementation would query capsule_details)
     const { rows } = await pgPool.query('SELECT capsule_id, capsule_name, status, lifecycle_status, created_at, expires_at FROM dta_capsule.capsule_details');
     res.json(rows);
});


module.exports = router;


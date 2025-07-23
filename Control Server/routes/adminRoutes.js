
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
    const creator_admin_id = req.user.id;

    if (!capsule_name || !file_hash_sha256 || !encrypted_key) {
        return res.status(400).json({ message: "Missing required fields" });
    }

    try {
        const query = `
            INSERT INTO dta_capsule.capsule_details 
            (creator_admin_id, capsule_name, description, file_hash_sha256, encrypted_key, policy, expires_at)
            VALUES ($1, $2, $3, $4, $5, $6, $7) RETURNING *`;
        
        const keyBuffer = Buffer.from(encrypted_key, 'hex');
        const values = [creator_admin_id, capsule_name, description, file_hash_sha256, keyBuffer, policy, expires_at];
        const result = await pgPool.query(query, values);
        const newCapsule = result.rows[0];

        logEvent({ /* ... */ });
        res.status(201).json(newCapsule);
    } catch (error) {
        console.error("Error creating capsule:", error);
        res.status(500).json({ message: "Server error while creating capsule" });
    }
});

// --- Grant access to a capsule ---
router.post('/capsules/:capsuleId/grant', async (req, res) => {
    const { capsuleId } = req.params;
    const { clientEmail } = req.body;
    const granting_admin_id = req.user.id;

    if (!clientEmail) {
        return res.status(400).json({ message: "clientEmail is required." });
    }

    try {
        const clientResult = await pgPool.query('SELECT client_id FROM dta_capsule.clients WHERE email = $1', [clientEmail]);
        if (clientResult.rows.length === 0) {
            return res.status(404).json({ message: `Client with email ${clientEmail} not found.` });
        }
        const clientId = clientResult.rows[0].client_id;

        const grantQuery = `
            INSERT INTO dta_capsule.capsule_access_grants (capsule_id, client_id, granting_admin_id)
            VALUES ($1, $2, $3)
            ON CONFLICT (capsule_id, client_id) DO NOTHING`;
        await pgPool.query(grantQuery, [capsuleId, clientId, granting_admin_id]);
        
        logEvent({
            actor: { id: granting_admin_id, username: req.user.username, type: 'admin' },
            action: { type: 'ACCESS_GRANT', outcome: 'success' },
            resource: { type: 'capsule', id: capsuleId },
            context: { grantedTo: clientId }
        });

        res.status(200).json({ message: `Access granted to ${clientEmail}` });
    } catch (error) {
        console.error("Error granting access:", error);
        res.status(500).json({ message: "Server error while granting access" });
    }
});

// --- Toggle Lock Status for a capsule ---
router.post('/capsules/:capsuleId/toggle-lock', async (req, res) => {
    const { capsuleId } = req.params;
    
    try {
        const currentStatusResult = await pgPool.query('SELECT status FROM dta_capsule.capsule_details WHERE capsule_id = $1', [capsuleId]);
        if (currentStatusResult.rows.length === 0) {
            return res.status(404).json({ message: "Capsule not found." });
        }
        
        const currentStatus = currentStatusResult.rows[0].status;
        const newStatus = currentStatus === 'locked' ? 'unlocked' : 'locked';

        const updateQuery = 'UPDATE dta_capsule.capsule_details SET status = $1 WHERE capsule_id = $2';
        await pgPool.query(updateQuery, [newStatus, capsuleId]);

        logEvent({
            actor: { id: req.user.id, username: req.user.username, type: 'admin' },
            action: { type: 'TOGGLE_LOCK', outcome: 'success' },
            resource: { type: 'capsule', id: capsuleId },
            context: { newStatus }
        });

        res.status(200).json({ message: `Capsule status changed to ${newStatus}` });

    } catch (error) {
        console.error("Error toggling lock status:", error);
        res.status(500).json({ message: "Server error while toggling lock status" });
    }
});

// --- Initiate Delete for a capsule ---
router.post('/capsules/:capsuleId/initiate-delete', async (req, res) => {
    const { capsuleId } = req.params;
    
    try {
        const newExpiryTime = new Date(Date.now() + 60 * 1000).toISOString();
        const updateQuery = `
            UPDATE dta_capsule.capsule_details
            SET lifecycle_status = 'marked_for_destruction',
                expires_at = $1,
                destruction_marked_at = NOW()
            WHERE capsule_id = $2`;
        const result = await pgPool.query(updateQuery, [newExpiryTime, capsuleId]);

        if (result.rowCount === 0) {
            return res.status(404).json({ message: "Capsule not found." });
        }
        
        logEvent({
            actor: { id: req.user.id, username: req.user.username, type: 'admin' },
            action: { type: 'INITIATE_DELETE', outcome: 'success' },
            resource: { type: 'capsule', id: capsuleId }
        });

        res.status(200).json({ message: "Capsule has been marked for deletion." });
    } catch (error) {
        console.error("Error initiating delete:", error);
        res.status(500).json({ message: "Server error while initiating delete" });
    }
});

// --- Get all capsules ---
router.get('/capsules', async (req, res) => {
     const { rows } = await pgPool.query('SELECT capsule_id, capsule_name, status, lifecycle_status, created_at, expires_at FROM dta_capsule.capsule_details ORDER BY created_at DESC');
     res.json(rows);
});


module.exports = router;

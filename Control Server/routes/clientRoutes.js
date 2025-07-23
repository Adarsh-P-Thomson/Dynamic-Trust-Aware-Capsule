/*
================================================================================
File: routes/clientRoutes.js
Description: Protected API routes for clients.
================================================================================
*/
const express = require('express');
const { protect } = require('../middleware/authMiddleware');
const { pgPool } = require('../config/db');
const { logEvent } = require('../services/logger');

const router = express.Router();

// All routes here are for clients
router.use(protect(['client']));

// --- Get the decryption key for a specific capsule ---
router.get('/capsules/:capsuleId/key', async (req, res) => {
    const { capsuleId } = req.params;
    const clientId = req.user.id; // from JWT

    try {
        // 1. Verify this client has been granted access to this capsule
        const accessQuery = `
            SELECT grant_id FROM dta_capsule.capsule_access_grants
            WHERE capsule_id = $1 
              AND client_id = $2
              AND is_revoked = FALSE 
              AND (access_expires_at IS NULL OR access_expires_at > NOW())
        `;
        const accessResult = await pgPool.query(accessQuery, [capsuleId, clientId]);

        if (accessResult.rows.length === 0) {
            logEvent({
                actor: { id: clientId, username: req.user.username, type: 'client' },
                action: { type: 'KEY_FETCH_FORBIDDEN', outcome: 'failure' },
                resource: { type: 'capsule', id: capsuleId }
            });
            return res.status(403).json({ message: "Forbidden: You do not have access to this capsule." });
        }

        // 2. If access is verified, fetch the capsule's key, ensuring it is active AND unlocked
        const keyQuery = `
            SELECT encrypted_key FROM dta_capsule.capsule_details 
            WHERE capsule_id = $1 
              AND lifecycle_status = 'active'
              AND status = 'unlocked'
        `;
        const keyResult = await pgPool.query(keyQuery, [capsuleId]);

        if (keyResult.rows.length === 0) {
             return res.status(404).json({ message: "Capsule not found, is not active, or is locked." });
        }

        const encryptedKey = keyResult.rows[0].encrypted_key;

        // 3. Return the key to the client (as a hex string)
        logEvent({
            actor: { id: clientId, username: req.user.username, type: 'client' },
            action: { type: 'KEY_FETCH', outcome: 'success' },
            resource: { type: 'capsule', id: capsuleId }
        });

        res.json({
            capsuleId: capsuleId,
            encryptedKey: encryptedKey.toString('hex')
        });

    } catch (error) {
        console.error("Error fetching capsule key:", error);
        res.status(500).json({ message: "Server error while fetching capsule key" });
    }
});


module.exports = router;

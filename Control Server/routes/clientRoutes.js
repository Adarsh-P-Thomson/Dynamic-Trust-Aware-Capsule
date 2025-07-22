/*
================================================================================
File: routes/clientRoutes.js
Description: Protected API routes for clients.
================================================================================
*/

const express = require('express');
const { protect } = require('../middleware/authMiddleware');
const { pgPool } = require('../config/db');

const router = express.Router();

// All routes here are for clients
router.use(protect(['client']));


// --- Get all capsules accessible to the logged-in client ---
router.get('/capsules', async (req, res) => {
    const clientId = req.user.id;
    try {
        const result = await pgPool.query(
            `SELECT cd.* FROM dta_capsule.capsule_details cd
             JOIN dta_capsule.capsule_access_grants cag ON cd.capsule_id = cag.capsule_id
             WHERE cag.client_id = $1 AND cag.is_revoked = FALSE AND (cag.access_expires_at IS NULL OR cag.access_expires_at > NOW())
             AND cd.status = 'unlocked' AND cd.lifecycle_status = 'active'`,
            [clientId]
        );
        res.json(result.rows);
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: "Error fetching accessible capsules" });
    }
});

// --- Get details for a specific accessible capsule ---
router.get('/capsules/:capsuleId', async (req, res) => {
    const { capsuleId } = req.params;
    const clientId = req.user.id;

    // ... (Full implementation would verify access first, then return details)
    // This requires a more complex query joining capsule_details and capsule_access_grants
    res.json({ message: `Fetching details for capsule ${capsuleId}` });
});


module.exports = router;


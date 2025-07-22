/*
================================================================================
File: middleware/authMiddleware.js
Description: JWT authentication middleware to protect routes.
================================================================================
*/

const jwt = require('jsonwebtoken');
const config = require('../config/config');
const { logEvent } = require('../services/logger');

const protect = (roles = []) => {
    return (req, res, next) => {
        let token;
        if (req.headers.authorization && req.headers.authorization.startsWith('Bearer')) {
            try {
                // Get token from header
                token = req.headers.authorization.split(' ')[1];

                // Verify token
                const decoded = jwt.verify(token, config.jwt.secret);

                // Attach user to the request
                req.user = decoded;

                // Role-based authorization
                if (roles.length > 0 && !roles.includes(req.user.role)) {
                    logEvent({
                        actor: { id: req.user.id, username: req.user.username, type: req.user.role },
                        action: { type: 'AUTH_FORBIDDEN', outcome: 'failure' },
                        resource: { type: 'endpoint', id: req.originalUrl }
                    });
                    return res.status(403).json({ message: 'Forbidden: You do not have the required role.' });
                }

                next();
            } catch (error) {
                res.status(401).json({ message: 'Not authorized, token failed' });
            }
        }

        if (!token) {
            res.status(401).json({ message: 'Not authorized, no token' });
        }
    };
};

module.exports = { protect };
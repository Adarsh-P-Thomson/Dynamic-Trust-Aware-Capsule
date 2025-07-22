/*
================================================================================
File: server.js (Main Entry Point)
Description: Initializes the Express server, connects to databases, and sets up
             all middleware and routes.
================================================================================
*/

const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const { connectPostgres, connectMongo } = require('./config/db');
const { logEvent } = require('./services/logger');
const config = require('./config/config');

// Import Routes
const authRoutes = require('./routes/authRoutes');
const adminRoutes = require('./routes/adminRoutes');
const clientRoutes = require('./routes/clientRoutes');

const app = express();
const PORT = config.port || 3001;

// --- Core Middleware ---
app.use(helmet()); // Basic security headers
app.use(cors());   // Enable Cross-Origin Resource Sharing
app.use(express.json()); // To parse JSON bodies
app.use(express.urlencoded({ extended: true })); // To parse URL-encoded bodies

// --- API Routes ---
app.get('/', (req, res) => {
    res.json({
        message: 'DTA Capsule Control Server is running.',
        status: 'active',
        timestamp: new Date().toISOString()
    });
});

app.use('/api/auth', authRoutes);
app.use('/api/admin', adminRoutes);
app.use('/api/client', clientRoutes);


// --- Error Handling Middleware ---
app.use((err, req, res, next) => {
    console.error(err.stack);

    // Log the error event to MongoDB
    logEvent({
        actor: { type: 'system' },
        action: { type: 'SYSTEM_ERROR', outcome: 'failure' },
        context: {
            error: err.message,
            stack: err.stack,
            request: {
                method: req.method,
                endpoint: req.originalUrl,
                ip: req.ip
            }
        }
    });

    res.status(500).send('Something broke!');
});


// --- Start Server ---
const startServer = async () => {
    try {
        // Ensure database connections are established before starting the server
        await connectPostgres();
        await connectMongo();

        app.listen(PORT, () => {
            console.log(`DTA Capsule Server listening on port ${PORT}`);
            logEvent({
                actor: { type: 'system', username: 'server_startup' },
                action: { type: 'SERVER_START', outcome: 'success' },
                resource: { type: 'system' }
            });
        });
    } catch (error) {
        console.error("Failed to start server:", error);
        process.exit(1);
    }
};

startServer();

/*
================================================================================
File: services/logger.js
Description: Service for writing structured logs to the MongoDB database.
================================================================================
*/

const { getMongoDb } = require('../config/db');
const { v4: uuidv4 } = require('uuid');

const logEvent = async (logData) => {
    try {
        const db = getMongoDb();
        const eventsCollection = db.collection('events');

        const event = {
            _id: uuidv4(),
            correlationId: logData.correlationId || uuidv4(),
            timestamp: new Date(),
            ...logData
        };

        await eventsCollection.insertOne(event);
    } catch (error) {
        console.error("Failed to write log to MongoDB:", error);
    }
};

module.exports = { logEvent };

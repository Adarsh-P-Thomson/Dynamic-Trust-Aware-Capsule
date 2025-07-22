/*
================================================================================
File: config/db.js
Description: Handles database connections for PostgreSQL and MongoDB.
================================================================================
*/

const { Pool } = require('pg');
const { MongoClient } = require('mongodb');
const config = require('./config');

// --- PostgreSQL Connection ---
const pgPool = new Pool(config.postgres);

const connectPostgres = async () => {
    try {
        await pgPool.query('SELECT NOW()');
        console.log('PostgreSQL connected successfully.');
    } catch (error) {
        console.error('PostgreSQL connection failed:', error);
        throw error;
    }
};


// --- MongoDB Connection ---
let mongoClient;
let mongoDb;

const connectMongo = async () => {
    try {
        mongoClient = new MongoClient(config.mongo.uri);
        await mongoClient.connect();
        mongoDb = mongoClient.db(); // Get default DB from URI
        console.log('MongoDB connected successfully.');
    } catch (error) {
        console.error('MongoDB connection failed:', error);
        throw error;
    }
};

const getMongoDb = () => {
    if (!mongoDb) {
        throw new Error('MongoDB has not been connected yet.');
    }
    return mongoDb;
};


module.exports = {
    pgPool,
    connectPostgres,
    connectMongo,
    getMongoDb
};


/*
================================================================================
File: config/config.js
Description: Centralized configuration settings.
================================================================================
*/

require('dotenv').config(); // Use .env file for environment variables

module.exports = {
    port: process.env.PORT || 3001,
    postgres: {
        user: process.env.PG_USER || 'postgres' ,
        host: process.env.PG_HOST || 'localhost' ,
        database: process.env.PG_DATABASE  || 'dta_logs' ,
        password: process.env.PG_PASSWORD || null ,
        port: process.env.PG_PORT || 5432,
    },
    mongo: {
        uri: process.env.MONGO_URI || 'mongodb://localhost:27017/dta_logs'
    },
    jwt: {
        secret: process.env.JWT_SECRET || 'a-very-strong-and-secret-key-for-jwt',
        expiresIn: '1h' // Token expiration time
    }
};
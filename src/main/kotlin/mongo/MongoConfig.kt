package org.kotMongo.mongo

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase


object MongoConfig {
    private lateinit var mongoClient: MongoClient
    private lateinit var database: MongoDatabase

    /**
     * Connects KotMongo to the specified MongoDB instance.
     *
     * The connection string should be in the format of "mongodb://localhost:27017".
     * The database name is the name of the database to connect to.
     *
     * @param connectionString the connection string to the MongoDB instance
     * @param databaseName the name of the database to connect to
     */
    fun connect(connectionString: String, databaseName: String) {
        mongoClient = MongoClients.create(connectionString)
        database = mongoClient.getDatabase(databaseName)
    }

    /**
     * Gets the MongoDatabase instance that KotMongo is connected to.
     *
     * @return the database KotMongo is connected to
     */
    fun getDatabase(): MongoDatabase {
        return database
    }

    /**
     * Gets the underlying MongoClient.
     *
     * You usually won't need this unless you want to do something
     * that isn't supported by KotMongo.
     *
     * @return the underlying MongoClient
     */

    fun getClient(): MongoClient {
        return mongoClient
    }

    /**
     * Closes the underlying MongoClient.
     *
     * You should call this when you're finished with the database to release
     * any system resources.
     */
    fun close() {
        mongoClient.close()
    }
}
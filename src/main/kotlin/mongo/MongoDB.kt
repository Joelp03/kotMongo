package org.kotMongo.mongo

import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase

class MongoDB {
    fun connect(): MongoDatabase {
        try {
            val client = MongoClients.create("mongodb://localhost:27017")
            val database = client.getDatabase("kotmongo_db")
            println("connected to mongo db: ${database.name}")
            return database
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }

    }
}
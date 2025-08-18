package org.kotMongo

import org.kotMongo.model.User
import org.kotMongo.mongo.MongoDB
import org.kotMongo.repository.MongoRepository


fun main() {

    val mongoDB = MongoDB()
    val database = mongoDB.connect()
    val userRepo = MongoRepository(User::class, database)

    // Insertar
    val user = User(id = "1", name = "Joel", age = 25)
    userRepo.insert(user)


    val found = userRepo.findAll()
    println("find all: $found")
}
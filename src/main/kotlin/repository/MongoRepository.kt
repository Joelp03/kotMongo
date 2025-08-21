package org.kotMongo.repository

import kotlin.reflect.KClass
import org.kotMongo.mongo.Filter
import org.kotMongo.mongo.MongoProvider

abstract class MongoRepository<T: Any>(private val entityClass: KClass<T>) {
    private val provider = MongoProvider()

    fun insert(entity: T): T {
        return provider.insert(entity)
    }

    fun insertMany(entities: List<T>): List<T> {
        return provider.insertMany(entities)
    }

    fun findById(id: String): T? {
        return provider.findById(id, entityClass)
    }

    fun findOne(filter: Filter): T? {
       return provider.findOne(filter, entityClass)
    }

    fun find(filter: Filter): List<T> {
        return provider.find(filter, entityClass)
    }

    fun findAll(): List<T> {
        return provider.findAll(entityClass)
    }

    fun upsert(filter: Filter, entity: T): T {
        return provider.upsert(filter, entity)
    }

    fun deleteOne(filter: Filter): Boolean {
        return provider.deleteOne(filter, entityClass)
    }
}
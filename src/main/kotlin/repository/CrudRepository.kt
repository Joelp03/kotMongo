package org.kotMongo.repository

import kotlinx.serialization.InternalSerializationApi


interface CrudRepository<T : Any> {
    fun insert(entity: T)
    fun findById(id: String): T?
    fun findAll(): List<T>
    /*   fun update(id: String, entity: T): Boolean
       fun delete(id: String): Boolean*/
}
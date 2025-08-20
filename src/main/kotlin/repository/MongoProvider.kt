package org.kotMongo.repository

import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import org.bson.BsonString
import org.bson.types.ObjectId
import org.bson.Document as BsonDocument
import org.kotMongo.annotations.Document
import org.kotMongo.annotations.Field
import org.kotMongo.annotations.Id
import org.kotMongo.mongo.MongoConfig
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaField

class MongoProvider {
    private val database = MongoConfig.getDatabase()

    fun <T: Any> insert(entity: T): T {
        val collection = getCollection(entity::class)
        val doc = entityToDoc(entity)
        println("📦 Document entity: ${doc}")
        collection.insertOne(doc)
        return entity
    }

    fun <T: Any> findById(id: String, entityClass: KClass<T>): T? {
        val collection = getCollection(entityClass)
        val doc = collection.find(Filters.eq("_id", id)).first() ?: return null
        return docToEntity(doc, entityClass)
    }

    private fun <T: Any> getCollection(entityClass: KClass<T>): MongoCollection<BsonDocument> {
        val documentAnnotation = entityClass.findAnnotation<Document>() ?:
        throw IllegalArgumentException("Entity class $entityClass is not annotated with @Document")

        val collection = database.getCollection(documentAnnotation.value)
        return collection
    }

    private fun <T: Any> entityToDoc(entity: T): BsonDocument {
        val bsonDoc = BsonDocument()
        val entityClass = entity::class

        for (property in entityClass.memberProperties) {
            val field = property.javaField ?: continue
            field.isAccessible = true
            val fieldAnnotation = field.getAnnotation(Field::class.java)
            val idAnnotation = field.getAnnotation(Id::class.java)

            val fieldName = when {
                idAnnotation != null -> "_id"
                fieldAnnotation != null && fieldAnnotation.value.isNotEmpty() -> fieldAnnotation.value
                else -> property.name
            }

            val value = field.get(entity)
            if (value != null) {
                if(idAnnotation != null && value is String) {
                     bsonDoc[fieldName] =  value
                } else {
                    bsonDoc[fieldName] = value
                }
            }
        }
        return bsonDoc
    }

    private fun <T: Any> docToEntity(doc: BsonDocument, entityClass: KClass<T>): T {
        val constructor = entityClass.primaryConstructor!!
        val params = mutableMapOf<String, Any?>()
        for (param in constructor.parameters) {

            val property = entityClass.memberProperties.find { it.name == param.name }
            val field = property?.javaField ?: continue

            val fieldAnnotation = field.getAnnotation(Field::class.java)
            val idAnnotation = field.getAnnotation(Id::class.java)

            val fieldName = when {
                idAnnotation != null -> "_id"
                fieldAnnotation != null && fieldAnnotation.value.isNotEmpty() -> fieldAnnotation.value
                else -> property.name
            }

            val value = doc[fieldName]

            params[param.name!!] = when {
                idAnnotation != null && value is ObjectId -> value.toString()
                else -> value
            }
        }

        println("params: $params")

        return constructor.callBy(params.mapKeys { (name, _) ->
            constructor.parameters.find { it.name == name }!!
        })
    }
}
package org.kotMongo.repository

import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import org.bson.types.ObjectId
import org.bson.Document as BsonDocument
import org.kotMongo.annotations.Document
import org.kotMongo.annotations.Field
import org.kotMongo.annotations.Id
import org.kotMongo.mongo.AndFilter
import org.kotMongo.mongo.EqFilter
import org.kotMongo.mongo.Filter
import org.kotMongo.mongo.GtFilter
import org.kotMongo.mongo.GteFilter
import org.kotMongo.mongo.InFilter
import org.kotMongo.mongo.LtFilter
import org.kotMongo.mongo.LteFilter
import org.kotMongo.mongo.MongoConfig
import org.kotMongo.mongo.NeFilter
import org.kotMongo.mongo.OrFilter
import org.kotMongo.mongo.RegexFilter
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
        collection.insertOne(doc)
        return entity
    }

    fun <T: Any> insertMany(entities: List<T>): List<T> {
        val collection = getCollection(entities.first()::class)
        val docs = entities.map { entityToDoc(it) }
        collection.insertMany(docs)
        return entities
    }

    fun <T: Any> find(filter: Filter, entityClass: KClass<T>): List<T> {
        val collection = getCollection(entityClass)
        val resolvedFilter = resolveFilter(filter, entityClass)
        val docs = collection.find(resolvedFilter).toList()
        return docs.map { docToEntity(it, entityClass) }
    }

    fun <T: Any> findOne(filter: Filter, entityClass: KClass<T>): T? {
        val collection = getCollection(entityClass)
        val resolvedFilter = resolveFilter(filter, entityClass)
        val doc = collection.find(resolvedFilter).first() ?: return null
        return docToEntity(doc, entityClass)
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

        return constructor.callBy(params.mapKeys { (name, _) ->
            constructor.parameters.find { it.name == name }!!
        })
    }


    private fun <T : Any> resolveFilter(filter: Filter, entityClass: KClass<T>): BsonDocument {
        return when (filter) {
            is EqFilter<*> -> BsonDocument(getActualFieldName(entityClass, filter.field), filter.value)
            is NeFilter<*> -> BsonDocument(getActualFieldName(entityClass, filter.field), BsonDocument("\$ne", filter.value))
            is GtFilter<*> -> BsonDocument(getActualFieldName(entityClass, filter.field), BsonDocument("\$gt", filter.value))
            is GteFilter<*> -> BsonDocument(getActualFieldName(entityClass, filter.field), BsonDocument("\$gte", filter.value))
            is LtFilter<*> -> BsonDocument(getActualFieldName(entityClass, filter.field), BsonDocument("\$lt", filter.value))
            is LteFilter<*> -> BsonDocument(getActualFieldName(entityClass, filter.field), BsonDocument("\$lte", filter.value))
            is InFilter<*> -> BsonDocument(getActualFieldName(entityClass, filter.field), BsonDocument("\$in", filter.values))
            is RegexFilter -> BsonDocument(getActualFieldName(entityClass, filter.field), BsonDocument("\$regex", filter.pattern).append("\$options", filter.options))
            is AndFilter -> BsonDocument("\$and", filter.filters.map { resolveFilter(it, entityClass) })
            is OrFilter -> BsonDocument("\$or", filter.filters.map { resolveFilter(it, entityClass) })
        }
    }

    private fun <T : Any> getIdField(entityClass: KClass<T>) =
        entityClass.memberProperties.find { property ->
            property.javaField?.getAnnotation(Id::class.java) != null
        }

    private fun <T : Any> getActualFieldName(entityClass: KClass<T>, propertyName: String): String {
        val property = entityClass.memberProperties.find { it.name == propertyName }
        val field = property?.javaField
        val fieldAnnotation = field?.getAnnotation(Field::class.java)
        val idAnnotation = field?.getAnnotation(Id::class.java)

        return when {
            idAnnotation != null -> "_id"
            fieldAnnotation != null && fieldAnnotation.value.isNotEmpty() -> fieldAnnotation.value
            else -> propertyName
        }
    }
}

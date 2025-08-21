package org.kotMongo.mongo

import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import org.bson.Document as BsonDocument
import org.bson.types.ObjectId
import org.kotMongo.annotations.Document
import org.kotMongo.annotations.Field
import org.kotMongo.annotations.Id
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaField

class MongoProvider {
    private val database = MongoConfig.getDatabase()

    fun <T: Any> insert(entity: T): T {
        val collection = getCollection(entity::class)
        val doc = entityToDocument(entity)
        collection.insertOne(doc)
        return entity
    }

    fun <T: Any> insertMany(entities: List<T>): List<T> {
        val collection = getCollection(entities.first()::class)
        val docs = entities.map { entityToDocument(it) }
        collection.insertMany(docs)
        return entities
    }

    fun <T: Any> find(filter: Filter, entityClass: KClass<T>): List<T> {
        val collection = getCollection(entityClass)
        val resolvedFilter = resolveFilter(filter, entityClass)
        val docs = collection.find(resolvedFilter).toList()
        return docs.map { documentToEntity(it, entityClass) }
    }

    fun <T : Any> findAll(entityClass: KClass<T>): List<T> {
        val collection = getCollection(entityClass)
        return collection.find().map { documentToEntity(it, entityClass) }.toList()
    }

    fun <T: Any> findOne(filter: Filter, entityClass: KClass<T>): T? {
        val collection = getCollection(entityClass)
        val resolvedFilter = resolveFilter(filter, entityClass)
        val doc = collection.find(resolvedFilter).first() ?: return null
        return documentToEntity(doc, entityClass)
    }

    fun <T: Any> findById(id: String, entityClass: KClass<T>): T? {
        val collection = getCollection(entityClass)
        val doc = collection.find(Filters.eq("_id", id)).first() ?: return null
        return documentToEntity(doc, entityClass)
    }

    fun <T : Any> upsert(filter: Filter, entity: T): T {
        val collection = getCollection(entity::class)
        val document = entityToDocument(entity)
        val filterDoc = resolveFilter(filter, entity::class)

        collection.replaceOne(filterDoc, document, ReplaceOptions().upsert(true))
        return entity
    }

    fun <T : Any> deleteOne(filter: Filter, entityClass: KClass<T>): Boolean {
        val collection = getCollection(entityClass)
        val filterDoc = resolveFilter(filter, entityClass)
        val result = collection.deleteOne(filterDoc)
        return result.deletedCount > 0
    }




    private fun <T: Any> getCollection(entityClass: KClass<T>): MongoCollection<BsonDocument> {
        val documentAnnotation = entityClass.findAnnotation<Document>() ?:
        throw IllegalArgumentException("Entity class $entityClass is not annotated with @Document")

        val collection = database.getCollection(documentAnnotation.value)
        return collection
    }


    /**
     * Convert a class entity into a Document BSON [BsonDocument], which will be saved in the database.
     * During the conversion, the annotations [Field] and [Id] will be respected, to control how the property is stored in the document.
     * @param entity the entity to convert
     *
     * Annotation supported:
     * - @Id: Indicates that the field is the unique identifier and controls how the property is stored in the document.
     * - @Field: Converts a custom field into a  Bson Document.
     *
     * @return [Document]
     */
    private fun <T: Any> entityToDocument(entity: T): BsonDocument {
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

    /**
     * Convert a Bson Document into an entity.
     * Assign the document values  to the constructors  based on the following:
     * the document values will be assigned to the constructor parameters based on the following rules:
     * @param doc the document to convert
     * @param entityClass the class of the entity
     *
     * - If a field is annotated with @Id, the value will be taken from the _id field in the document.
     * - If a field is annotated with @Field("name"), the value will be taken from the specified field.
     * - Otherwise, the field name will be matched directly to the document property.
     * @return [T]
     *
     * */
    private fun <T: Any> documentToEntity(doc: BsonDocument, entityClass: KClass<T>): T {
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
    /**
     *
     * Get the real names of the mongodb fields from the names of the class properties.
     *
     * @param entityClass - the class of the entity (e.g., `User::class`).
     * @param propertyName The name of the property in kotlin (e.g., "name").
     * @return The name of the field in MongoDB:
     * - If the property has the annotation `@Id`, return "_id".
     * - if the property has the annotation `@Field` with value, return the value.
    * */
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

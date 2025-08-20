package org.kotMongo.repository

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.kotMongo.annotations.Document
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlinx.serialization.InternalSerializationApi
import org.bson.Document as BsonDocument

@OptIn(InternalSerializationApi::class)
class MongoRepository<T : Any>(
    private val clazz: KClass<T>,
    val db: MongoDatabase
) : CrudRepository<T> {

    private val json = Json { ignoreUnknownKeys = true }

    private val collection: MongoCollection<BsonDocument>

    init {
        val docAnnotation = clazz.findAnnotation<Document>()
            ?: throw IllegalArgumentException("Class ${clazz.simpleName} is not annotated with @Document")

        collection = db.getCollection(docAnnotation.value)

    }

    override fun insert(entity: T) {
        val json = Json.encodeToString(clazz.serializer(), entity)
        val document = BsonDocument.parse(json)
        collection.insertOne(document)
    }

    override fun findById(id: String): T? {
        val doc = collection.find(Filters.eq("id", id)).first() ?: return null
        println("📦 Document in DB not see: ${doc.toJson()}")
        return json.decodeFromString(clazz.serializer(), doc.toJson())
    }

    override fun findAll(): List<T> {
        return collection.find().map { doc ->
            val docJson = doc.toJson()
            println("📦 Document in DB: $docJson") // debug
            json.decodeFromString(clazz.serializer(), docJson)
        }.toList()
    }
}

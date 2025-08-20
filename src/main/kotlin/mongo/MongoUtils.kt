package org.kotMongo.mongo

import org.bson.Document as BsonDocument
import kotlin.reflect.KProperty1

sealed class Filter {
    abstract fun toBsonDocument(): BsonDocument
}

data class EqFilter<T>(val field: String, val value: T) : Filter() {
    override fun toBsonDocument(): BsonDocument = BsonDocument(field, value)
}

data class NeFilter<T>(val field: String, val value: T) : Filter() {
    override fun toBsonDocument(): BsonDocument = BsonDocument(field, BsonDocument("\$ne", value))
}

data class GtFilter<T>(val field: String, val value: T) : Filter() {
    override fun toBsonDocument(): BsonDocument = BsonDocument(field, BsonDocument("\$gt", value))
}

data class GteFilter<T>(val field: String, val value: T) : Filter() {
    override fun toBsonDocument(): BsonDocument = BsonDocument(field, BsonDocument("\$gte", value))
}

data class LtFilter<T>(val field: String, val value: T) : Filter() {
    override fun toBsonDocument(): BsonDocument = BsonDocument(field, BsonDocument("\$lt", value))
}

data class LteFilter<T>(val field: String, val value: T) : Filter() {
    override fun toBsonDocument(): BsonDocument = BsonDocument(field, BsonDocument("\$lte", value))
}

data class InFilter<T>(val field: String, val values: List<T>) : Filter() {
    override fun toBsonDocument(): BsonDocument = BsonDocument(field, BsonDocument("\$in", values))
}

data class RegexFilter(val field: String, val pattern: String, val options: String = "i") : Filter() {
    override fun toBsonDocument(): BsonDocument = BsonDocument(field, BsonDocument("\$regex", pattern).append("\$options", options))
}

data class AndFilter(val filters: List<Filter>) : Filter() {
    override fun toBsonDocument(): BsonDocument = BsonDocument("\$and", filters.map { it.toBsonDocument() })
}

data class OrFilter(val filters: List<Filter>) : Filter() {
    override fun toBsonDocument(): BsonDocument = BsonDocument("\$or", filters.map { it.toBsonDocument() })
}


infix fun <T, V> KProperty1<T, V>.eq(value: V): Filter = EqFilter(this.name, value)
infix fun <T, V> KProperty1<T, V>.ne(value: V): Filter = NeFilter(this.name, value)
infix fun <T, V> KProperty1<T, V>.gt(value: V): Filter = GtFilter(this.name, value)
infix fun <T, V> KProperty1<T, V>.gte(value: V): Filter = GteFilter(this.name, value)
infix fun <T, V> KProperty1<T, V>.lt(value: V): Filter = LtFilter(this.name, value)
infix fun <T, V> KProperty1<T, V>.lte(value: V): Filter = LteFilter(this.name, value)
infix fun <T, V> KProperty1<T, V>.`in`(values: List<V>): Filter = InFilter(this.name, values)
infix fun <T> KProperty1<T, String>.regex(pattern: String): Filter = RegexFilter(this.name, pattern)

// logical operators
infix fun Filter.and(other: Filter): Filter = AndFilter(listOf(this, other))
infix fun Filter.or(other: Filter): Filter = OrFilter(listOf(this, other))

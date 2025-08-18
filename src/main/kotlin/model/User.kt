package org.kotMongo.model

import kotlinx.serialization.Serializable
import org.kotMongo.annotations.Document
import org.kotMongo.annotations.Id

@Serializable
@Document("users")
data class User(
    @Id val id: String,
    val name: String,
    val age: Int
)
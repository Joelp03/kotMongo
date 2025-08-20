package schemas

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.kotMongo.annotations.Document
import org.kotMongo.annotations.Field
import org.kotMongo.annotations.Id

@Serializable
@Document("users")
data class User(
    @Id
    val id: String,
    @Field("first_name")
    val name: String,
    val age: Int
)
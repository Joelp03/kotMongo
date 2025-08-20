package schemas

import org.kotMongo.annotations.Document
import org.kotMongo.annotations.Field
import org.kotMongo.annotations.Id


@Document("jedis")
data class Jedi(
    @Id
    val id: String,
    val name: String,
    @Field("light_saber_color")
    val lightSaberColor: String,
    val rank: String, // Padawan, Knight, Master
    val age: Int,
    @Field("is_active")
    val active: Boolean = true,
)
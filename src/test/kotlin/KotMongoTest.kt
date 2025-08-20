import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import schemas.User
import org.kotMongo.mongo.MongoConfig
import org.kotMongo.mongo.gt
import repository.UserRepository
import java.util.UUID
import kotlin.test.assertEquals


class KotMongoTest {

    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun setUp() {
        MongoConfig.connect("mongodb://localhost:27017", "kotmongo_db")
        userRepository = UserRepository()
    }

    @AfterEach
    fun tearDown() {
         MongoConfig.getDatabase().drop()
    }


    @Test
    fun `should save a user successfully`() {
        val user = User(id = UUID.randomUUID().toString(), name = "Joel", age = 25)
        userRepository.insert(user)
        val userFound = userRepository.findById(user.id)
        assertEquals(user, userFound)
    }


    @Test
    fun `should find users who are at least 40 years old`() {
        val users = (1..5).map {
            User(
                id = UUID.randomUUID().toString(),
                name = "user$it",
                age = 15 * it
            )
        }

        userRepository.insertMany(users)
        val usersFound = userRepository.find(User::age gt 40)

        assertEquals(3, usersFound.size)
    }

}
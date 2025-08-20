import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import schemas.User
import org.kotMongo.mongo.MongoConfig
import org.kotMongo.repository.MongoRepository
import kotlin.test.assertEquals


class KotMongoTest {

    lateinit var userRepository: MongoRepository<User>

    @BeforeEach
    fun setUp() {
        MongoConfig.connect("mongodb://localhost:27017", "kotmongo_db")
        userRepository = MongoRepository(User::class, MongoConfig.getDatabase())
    }

    @AfterEach
    fun tearDown() {
         MongoConfig.getDatabase().drop()
    }


    @Test
    fun `should save a user`() {
        val user = User(id = "1", name = "Joel", age = 25)
        userRepository.insert(user)
        val userFound = userRepository.findById(user.id)
        assertEquals(user, userFound)
    }

    @Test
    fun `should find all users`() {
        val user = User(id = "1", name = "Jade", age = 25)
        userRepository.insert(user)
        val userFound = userRepository.findAll()
        assertEquals(listOf(user), userFound)
    }

}
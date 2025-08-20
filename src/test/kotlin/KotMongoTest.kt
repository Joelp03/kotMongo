import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import schemas.User
import org.kotMongo.mongo.MongoConfig
import org.kotMongo.repository.MongoProvider
import org.kotMongo.repository.MongoRepository
import java.util.UUID
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
        val user = User(id = "id", name = "Joel", age = 25)
        userRepository.insert(user)
        val provider = MongoProvider()
        provider.insert(user)
        val userFound = userRepository.findById(user.id!!)
        assertEquals(user, userFound)
    }

    @Test
    fun `should save a user successfully`() {
        val user = User(id = UUID.randomUUID().toString(), name = "Joel", age = 25)
        userRepository.insert(user)
        val provider = MongoProvider()
        provider.insert(user)
        val userFound = provider.findById(user.id, User::class)

        assertEquals(user, userFound)
    }

    @Test
    fun `should find all users`() {
        val user = User(id = UUID.randomUUID().toString(), name = "Jade", age = 25)
        userRepository.insert(user)
        val userFound = userRepository.findAll()
        assertEquals(listOf(user), userFound)
    }

}
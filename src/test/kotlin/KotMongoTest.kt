import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import schemas.User
import org.kotMongo.mongo.MongoConfig
import org.kotMongo.mongo.and
import org.kotMongo.mongo.eq
import org.kotMongo.mongo.gt
import org.kotMongo.mongo.inF
import org.kotMongo.mongo.regex
import repository.JediRepository
import repository.UserRepository
import schemas.Jedi
import java.util.UUID
import kotlin.test.assertEquals


class KotMongoTest {

    private lateinit var userRepository: UserRepository
    private lateinit var jediRepository: JediRepository

    @BeforeEach
    fun setUp() {
        MongoConfig.connect("mongodb://localhost:27017", "kotmongo_db")
        userRepository = UserRepository()
        jediRepository = JediRepository()
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
    fun `should find user by name`() {
        val users = (1..5).map {
            User(
                id = UUID.randomUUID().toString(),
                name = "user$it",
                age = 15 * it
            )
        }
        val user = users.first()
        userRepository.insertMany(users)

        val userFound = userRepository.findOne(User::name eq  user.name)
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

    @Test
    fun `should upsert a jedi successfully`() {
        val jedi = `given a list of jedi is saved`().first()

        val inactiveJedi = jedi.copy(active = false)

        jediRepository.upsert(Jedi::name eq jedi.name, inactiveJedi)

        val jediFound = jediRepository.findById(jedi.id)

        assertEquals(inactiveJedi, jediFound)
    }

    @Test
    fun `should delete a jedi successfully`() {
        val jedis = `given a list of jedi is saved`()

        jediRepository.deleteOne(Jedi::name eq jedis.first().name)

        val jedisFound = jediRepository.findAll()

        assertEquals(jedisFound.size, jedis.size - 1)
    }

    @Test
    fun `should find jedi with active status is true and stable color is green`() {

        val jedi = `given a list of jedi is saved`()


        val jediFound = jediRepository.find(Jedi::active eq true and (Jedi::lightSaberColor eq "Green"))

        assertEquals(2, jediFound.size)
    }

    @Test
    fun `should find jedi with rank specific`() {
        val jedis = `given a list of jedi is saved`()

        val specificRanks = jediRepository.find(Jedi::rank inF listOf("Master", "Knight"))

        assertEquals(3, specificRanks.size)
    }

    @Test
    fun `should find jedi by pattern`() {
        val jedis = `given a list of jedi is saved`()

        val jedisFound = jediRepository.find(Jedi::name regex  "yo")

        assertEquals(1, jedisFound.size)
    }


    fun `given a list of jedi is saved`(): List<Jedi> {
        val yoda = Jedi(
            id = UUID.randomUUID().toString(),
            name = "Yoda",
            lightSaberColor = "Green",
            rank = "Master",
            age = 900,
            active = true,
            power = 17700
        )

        val lukeSkywalker = Jedi(
            id = UUID.randomUUID().toString(),
            name = "Luke Skywalker",
            lightSaberColor = "Green",
            rank = "Knight",
            age = 25,
            active = true,
            power = 14500
        )

        val obiWan = Jedi(
            id = UUID.randomUUID().toString(),
            name = "Obi-Wan Kenobi",
            lightSaberColor = "Blue",
            rank = "Master",
            age = 57,
            active = false,
            power = 13400
        )

        val jedis = listOf(yoda, lukeSkywalker, obiWan)

        jediRepository.insertMany(jedis)

        return jedis
    }

}
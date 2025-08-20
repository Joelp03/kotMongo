package repository

import org.kotMongo.repository.MongoRepository
import schemas.User

class UserRepository : MongoRepository<User>(User::class)
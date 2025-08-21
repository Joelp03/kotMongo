package repository

import org.kotMongo.repository.MongoRepository
import schemas.Jedi

class JediRepository: MongoRepository<Jedi>(Jedi::class)
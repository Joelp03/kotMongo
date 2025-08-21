# KotMongo

A lightweight Kotlin MongoDB ODM (Object Document Mapper) that provides a simple and type-safe way to interact with MongoDB databases.

## Features

- 🎯 **Type-safe queries** - Leverage Kotlin's type system for compile-time query validation
- 📝 **Annotation-based mapping** - Simple annotations for document and field mapping
- 🔍 **Fluent query API** - Intuitive query building with operators like `eq`, `gt`, `and`, `regex`
- 🚀 **Easy repository pattern** - Abstract base repository for common CRUD operations
- 🧪 **Test-friendly** - Built with testing in mind

## Quick Start

### Prerequisites

- Kotlin 2.1.20+
- JDK 21+
- MongoDB instance (local or remote)

### Setup

1. Clone the repository
2. Start MongoDB using Docker Compose:
```bash
docker-compose up -d
```

3. Run the tests to verify everything works:
```bash
./gradlew test
```

## Usage

### Define Your Data Models

Use annotations to map your Kotlin data classes to MongoDB documents:

```kotlin
@Document("users")
data class User(
    @Id
    val id: String,
    @Field("first_name")
    val name: String,
    val age: Int
)
```

### Create Repositories

Extend `MongoRepository` for your entities:

```kotlin
class UserRepository : MongoRepository<User>(User::class)
```

### Perform Database Operations

```kotlin
// Connect to MongoDB
MongoConfig.connect("mongodb://localhost:27017", "your_database")

val userRepository = UserRepository()

// Insert a user
val user = User(id = UUID.randomUUID().toString(), name = "John", age = 30)
userRepository.insert(user)

// Find users with type-safe queries
val adults = userRepository.find(User::age gt 18)
val johnDoe = userRepository.findOne(User::name eq "John")

// Complex queries
val activeAdults = userRepository.find(
    User::age gt 18 and (User::active eq true)
)
```

## Available Query Operators

- `eq` - Equals
- `gt` - Greater than
- `lt` - Less than
- `gte` - Greater than or equal
- `lte` - Less than or equal
- `inF` - In array
- `regex` - Regular expression match
- `and` - Logical AND
- `or` - Logical OR

## Annotations

- `@Document("collection_name")` - Maps class to MongoDB collection
- `@Id` - Marks field as document ID
- `@Field("field_name")` - Maps property to different field name in MongoDB
- `@Indexed` - Creates index on field

## Repository Methods

The base `MongoRepository` provides:

- `insert(entity)` - Insert single document
- `insertMany(entities)` - Insert multiple documents
- `findById(id)` - Find by ID
- `findOne(filter)` - Find single document by filter
- `find(filter)` - Find multiple documents by filter
- `findAll()` - Find all documents
- `upsert(filter, entity)` - Update or insert document
- `deleteOne(filter)` - Delete single document

## Project Structure

```
src/
├── main/kotlin/
│   ├── annotations/          # Custom annotations
│   ├── mongo/               # MongoDB connection and utilities
│   └── repository/          # Repository base classes
└── test/kotlin/
    ├── repository/          # Repository implementations
    └── schemas/             # Test data models
```

## Dependencies

- MongoDB Java Driver 4.11.0
- Kotlinx Serialization 1.7.1
- Kotlin Reflection 2.0.0

## Development

### Running Tests

```bash
./gradlew test
```

### Building

```bash
./gradlew build
```

## License

This project is open source and available under the [MIT License](LICENSE).
plugins {
    kotlin("jvm") version "2.1.20"
    kotlin("plugin.serialization") version "2.0.0"
}

group = "org.kotMongo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

    // Driver MongoDB
    implementation("org.mongodb:mongodb-driver-sync:4.11.0")

    // kotlinx.serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.0.0")


    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
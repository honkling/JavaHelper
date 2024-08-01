plugins {
    kotlin("jvm") version "2.0.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

group = "me.honkling"
version = "0.1.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io/")
}

dependencies {
    implementation("org.slf4j:slf4j-simple:2.0.13")
    implementation("dev.kord:kord-core:0.14.0")
    implementation("cc.ekblad:4koma:1.2.0")
    implementation(kotlin("reflect"))
}

kotlin {
    jvmToolchain(19)
}

application {
    mainClass = "me.honkling.javahelper.MainKt"
}
package me.honkling.javahelper.config

import cc.ekblad.toml.TomlMapper
import cc.ekblad.toml.decode
import java.io.File
import java.lang.IllegalStateException

inline fun <reified T> createResource(name: String, mapper: TomlMapper): T {
    val file = File(name)

    if (!file.exists()) {
        val resource = ConfigToml::class.java.getResource("/$name")
            ?: throw IllegalStateException("Resource /$name does not exist.")

        file.writeText(resource.readText())
    }

    return mapper.decode(file.toPath())
}
package me.honkling.javahelper.config

import cc.ekblad.toml.encodeTo
import cc.ekblad.toml.tomlMapper
import java.io.File

private val mapper = tomlMapper {}
var tagsToml = reloadTagsToml()

data class TagsToml(
    val tags: MutableMap<String, Tag>
) {
    data class Tag(
        val name: String,
        var aliases: List<String>,
        var content: String
    )
}

fun reloadTagsToml(): TagsToml {
    return createResource("tags.toml", mapper)
}

fun writeTagsToml() {
    val file = File("tags.toml")
    mapper.encodeTo(file.toPath(), tagsToml)
}
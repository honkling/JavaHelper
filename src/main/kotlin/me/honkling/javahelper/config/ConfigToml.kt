package me.honkling.javahelper.config

import cc.ekblad.toml.tomlMapper

var configToml = reloadConfigToml()

data class ConfigToml(
    val token: String,
    val prefix: String,
    val admins: List<String>
)

fun reloadConfigToml(): ConfigToml {
    val mapper = tomlMapper {}

    return createResource("config.toml", mapper)
}

package me.honkling.javahelper

import dev.kord.core.Kord
import dev.kord.gateway.Intent
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import me.honkling.javahelper.config.configToml
import me.honkling.javahelper.manager.registerCommands
import me.honkling.javahelper.manager.registerEvents
import org.slf4j.simple.SimpleLogger
import org.slf4j.simple.SimpleLoggerFactory
import java.util.logging.LogManager

lateinit var kord: Kord
val logger = SimpleLoggerFactory().getLogger("JavaHelper")

suspend fun main() {
    kord = Kord(configToml.token)

    registerCommands("me.honkling.javahelper.command")
    registerEvents("me.honkling.javahelper.event")

    kord.login {
        @OptIn(PrivilegedIntent::class)
        intents = Intents(Intent.Guilds, Intent.GuildMessages, Intent.MessageContent)
    }
}
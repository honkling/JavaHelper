package me.honkling.javahelper.command

import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.entity.application.GlobalChatInputCommand
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.string
import me.honkling.javahelper.config.configToml
import me.honkling.javahelper.config.reloadConfigToml
import me.honkling.javahelper.config.reloadTagsToml
import me.honkling.javahelper.config.tagsToml
import me.honkling.javahelper.manager.annotation.Command

@Command(Command.Type.REGISTRY)
private suspend fun register(kord: Kord): GlobalChatInputCommand {
    return kord.createGlobalChatInputCommand("reload", "Admin utility for configuring the bot") {
        string("file", "The config file to reload.") {
            choice("config", "config")
            choice("tags", "tags")
            required = true
        }
    }
}

@Command(Command.Type.EXECUTION)
private suspend fun execute(interaction: ChatInputCommandInteraction) {
    val file = interaction.command.strings["file"]!!

    if (interaction.user.id.toString() !in configToml.admins) {
        interaction.respondPublic { content = "You don't have permission to do that." }
        return
    }

    when (file) {
        "config" -> {
            configToml = reloadConfigToml()
            interaction.respondPublic { content = "Reloaded config.toml." }
        }
        "tags" -> {
            tagsToml = reloadTagsToml()
            interaction.respondPublic { content = "Reloaded tags.toml." }
        }
        else -> interaction.respondPublic { content = "That is not a valid config file." }
    }
}
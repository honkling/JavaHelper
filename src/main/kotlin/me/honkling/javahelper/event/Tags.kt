package me.honkling.javahelper.event

import dev.kord.core.Kord
import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.message.EmbedBuilder
import me.honkling.javahelper.config.configToml
import me.honkling.javahelper.config.tagsToml
import me.honkling.javahelper.manager.annotation.Event

@Event
private fun register(kord: Kord) {
    kord.on<MessageCreateEvent> {
        val messageContent = message.content
        val author = message.author

        if (!messageContent.startsWith(configToml.prefix) || author == null || author.isBot)
            return@on

        val args = messageContent.split(" ")
        val name = args[0].removePrefix(configToml.prefix).lowercase()
        val tag = tagsToml.tags.values.find { it.name == name || name in it.aliases }
            ?: return@on

        val embed = EmbedBuilder()
        embed.description = tag.content
        embed.footer {
            val discriminator =
                if (author.discriminator == "0") ""
                else "#${author.discriminator}"
            text = "Requested by ${author.username}$discriminator"
            icon = author.avatar?.cdnUrl?.toUrl()
                ?: author.defaultAvatar.cdnUrl.toUrl()
        }

        message.reply { embeds = mutableListOf(embed) }
    }
}
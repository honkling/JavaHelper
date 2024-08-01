package me.honkling.javahelper.lib

import dev.kord.core.behavior.reply
import dev.kord.core.entity.Message
import dev.kord.rest.builder.message.AllowedMentionsBuilder
import dev.kord.rest.builder.message.create.MessageCreateBuilder

suspend fun Message.replyNoPing(builder: MessageCreateBuilder.() -> Unit) {
    reply {
        builder.invoke(this)
        allowedMentions = AllowedMentionsBuilder()
    }
}
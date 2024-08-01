package me.honkling.javahelper.command

import dev.kord.common.Color
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Choice
import dev.kord.common.entity.Permission
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.TextChannelBehavior
import dev.kord.core.behavior.channel.asChannelOf
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.behavior.interaction.suggest
import dev.kord.core.behavior.interaction.suggestString
import dev.kord.core.behavior.reply
import dev.kord.core.entity.Message
import dev.kord.core.entity.application.GlobalChatInputCommand
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.entity.interaction.AutoCompleteInteraction
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.core.entity.interaction.SubCommand
import dev.kord.core.event.interaction.AutoCompleteInteractionCreateEvent
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.component.ButtonBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.interaction.subCommand
import dev.kord.rest.builder.message.EmbedBuilder
import me.honkling.javahelper.config.TagsToml
import me.honkling.javahelper.config.tagsToml
import me.honkling.javahelper.config.writeTagsToml
import me.honkling.javahelper.lib.createInteraction
import me.honkling.javahelper.manager.annotation.Command

@Command(Command.Type.REGISTRY)
private suspend fun register(kord: Kord): GlobalChatInputCommand {
    return kord.createGlobalChatInputCommand("tags", "Manage chat tags.") {
        subCommand("add", "Add a new chat tag.") {
            string("name", "The name of the tag.") {
                required = true
            }

            string("aliases", "A list of aliases, separated by commas.") {
                required = false
            }
        }

        subCommand("edit", "Edit a chat tag.") {
            string("name", "The name of the tag.") {
                required = true
                autocomplete = true
            }

            string("aliases", "A list of aliases, separated by commas.") {
                required = false
            }
        }

        subCommand("delete", "Delete a chat tag.") {
            string("name", "The name of the tag.") {
                required = true
                autocomplete = true
            }
        }

        subCommand("info", "View information on a tag.") {
            string("name", "The name of the tag.") {
                required = true
                autocomplete = true
            }
        }

        subCommand("list", "List all chat tags.")
    }
}

@Command(Command.Type.EXECUTION)
private suspend fun execute(interaction: ChatInputCommandInteraction) {
    val subcommand = interaction.command as SubCommand

    suspend fun getContent(handler: suspend (Message) -> Unit) {
        createInteraction {
            subscribe<MessageCreateEvent> {
                if (message.author != interaction.user)
                    return@subscribe

                resolve()
                handler.invoke(message)
            }
        }
    }

    suspend fun getTag(): TagsToml.Tag? {
        val name = subcommand.strings["name"]!!.lowercase()
        val tag = tagsToml.tags.values.find { it.name == name || name in it.aliases }

        if (tag == null) {
            interaction.respondPublic { content = "There is no tag with that name or alias." }
            return null
        }

        return tag
    }

    val guildMember = interaction.user.asMember((interaction.channel.asChannelOf<TextChannel>()).guildId)
    if (subcommand.name in listOf("add", "edit", "delete") && guildMember.permissions?.contains(Permission.ModerateMembers) != true) {
        interaction.respondPublic { content = "You don't have permission to do that." }
        return
    }

    when (subcommand.name) {
        "add" -> {
            val name = subcommand.strings["name"]!!.lowercase()
            val aliases = subcommand.strings["aliases"]?.split(",")?.map { it.trim().lowercase() }

            if (name in tagsToml.tags) {
                interaction.respondPublic { content = "That tag already exists. If you want to edit the contents, please use `/tags edit`." }
                return
            }

            val aliasConflictingTag = tagsToml.tags.values.find { name in it.aliases }
            if (aliasConflictingTag != null) {
                interaction.respondPublic { content = "The tag `${aliasConflictingTag.name}` already has an alias with that name." }
                return
            }

            interaction.respondPublic { content = "Please send the contents of the tag." }

            getContent { message ->
                val aliasesDisplay = aliases?.let { " with aliases `${it.joinToString("`, `")}`" } ?: ""
                tagsToml.tags[name] = TagsToml.Tag(name, aliases ?: emptyList(), message.content)
                writeTagsToml()

                message.reply { content = "Created tag `${name}`$aliasesDisplay." }
            }
        }
        "edit" -> {
            val tag = getTag() ?: return
            val aliases = subcommand.strings["aliases"]?.split(",")?.map { it.trim().lowercase() }

            if (aliases != null)
                tag.aliases = aliases

            interaction.respondPublic {
                val row = ActionRowBuilder()
                row.interactionButton(ButtonStyle.Danger, "dont_modify_content") {
                    label = "Don't modify tag contents"
                }

                content = "Please send the contents of the tag."
                components = mutableListOf(row)
            }

            createInteraction {
                getContent { message ->
                    tag.content = message.content
                    writeTagsToml()
                    message.reply { content = "Updated the tag." }
                    resolve()
                }

                subscribe<ButtonInteractionCreateEvent> { resolve ->
                    if (this.interaction.componentId != "dont_modify_content" || this.interaction.user != interaction.user)
                        return@subscribe

                    writeTagsToml()
                    this.interaction.respondPublic { content = "Updated the tag." }
                    resolve()
                }
            }
        }
        "delete" -> {
            val tag = getTag() ?: return
            tagsToml.tags.remove(tag.name)
            writeTagsToml()

            interaction.respondPublic { content = "Deleted the tag `${tag.name}`." }
        }
        "info" -> {
            val tag = getTag() ?: return
            val embed = EmbedBuilder()

            embed.title = "`${tag.name}` Information"
//            embed.color = Color(217, 83, 86)

            embed.field("Aliases") {
                if (tag.aliases.isEmpty())
                    "There are no aliases."
                else "`${tag.aliases.joinToString("`, `")}`"
            }

            interaction.respondPublic { embeds = mutableListOf(embed) }
        }
        "list" -> {
            interaction.respondPublic {
                val tags = tagsToml.tags

                content = "There ${if (tags.size == 1) "is" else "are"} ${tags.size} tag"

                if (tags.size != 1)
                    content += "s"

                for (tag in tags.values) {
                    content += "\n`${tag.name}`"

                    if (tag.aliases.isNotEmpty())
                        content += " (aliases are `${tag.aliases.joinToString("`, `")}`)"
                }
            }
        }
    }
}

@Command(Command.Type.AUTOCOMPLETE)
private suspend fun autocomplete(interaction: AutoCompleteInteraction) {
    interaction.suggestString {
        for (tag in tagsToml.tags.values) {
            choice(tag.name, tag.name)

            for (alias in tag.aliases)
                choice(alias, tag.name)
        }
    }
}
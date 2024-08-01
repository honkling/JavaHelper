package me.honkling.javahelper.manager

import dev.kord.core.entity.application.GlobalChatInputCommand
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.core.event.interaction.AutoCompleteInteractionCreateEvent
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import me.honkling.javahelper.kord
import me.honkling.javahelper.logger
import me.honkling.javahelper.manager.annotation.Command
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspend
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.kotlinFunction

suspend fun registerCommands(pkg: String) {
    val classes = getClassesInPackage(pkg) { getMethodIfPresent<Command>(it) != null }

    for (clazz in classes)
        registerCommand(clazz)
}

private suspend fun registerCommand(clazz: Class<*>) {
    val methods = getMethodsWithAnnotation<Command>(clazz)
    val registry = methods.find { it.second.type == Command.Type.REGISTRY }?.first
        ?: throw IllegalStateException("Missing registry for command with execution method.")
    val execution = methods.find { it.second.type == Command.Type.EXECUTION }?.first
        ?: throw IllegalStateException("Missing execution for command with registry method.")
    val autocomplete = methods.find { it.second.type == Command.Type.AUTOCOMPLETE }?.first

    val registryFunction = registry.kotlinFunction!! as KFunction<GlobalChatInputCommand>
    registryFunction.isAccessible = true
    val command = registryFunction.callSuspend(kord)

    kord.on<ChatInputCommandInteractionCreateEvent> {
        if (interaction.invokedCommandName != command.name)
            return@on

        val executionFunction = execution.kotlinFunction!!
        executionFunction.isAccessible = true
        executionFunction.callSuspend(interaction)
    }

    if (autocomplete != null) {
        kord.on<AutoCompleteInteractionCreateEvent> {
            if (interaction.command.rootName != command.name)
                return@on

            val autocompleteFunction = autocomplete.kotlinFunction!!
            autocompleteFunction.isAccessible = true
            autocompleteFunction.callSuspend(interaction)
        }
    }

    logger.info("Registered commands in ${clazz.simpleName}")
}

private fun getParameter(parameters: List<KParameter>, index: Int): KClass<*>? {
    if (index + 1 >= parameters.size)
        return null

    return parameters[index].type.classifier as KClass<*>?
}
package me.honkling.javahelper.event

import dev.kord.core.Kord
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.on
import me.honkling.javahelper.logger
import me.honkling.javahelper.manager.annotation.Event

@Event
private fun register(kord: Kord) {
    kord.on<ReadyEvent> {
        logger.info("i like kotlin")
    }
}
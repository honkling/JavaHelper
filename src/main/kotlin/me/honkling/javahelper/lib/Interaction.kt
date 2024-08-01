package me.honkling.javahelper.lib

import dev.kord.core.event.Event
import dev.kord.core.on
import kotlinx.coroutines.Job
import me.honkling.javahelper.kord

class InteractionContext {
    val jobs = mutableListOf<Job>()

    suspend inline fun <reified T : Event> subscribe(crossinline handler: suspend T.(() -> Unit) -> Unit) {
        jobs += kord.on<T> { handler.invoke(this, ::resolve) }
    }

    fun resolve() {
        for (job in jobs)
            job.cancel()
    }
}

suspend fun createInteraction(builder: suspend InteractionContext.() -> Unit) {
    val context = InteractionContext()
    builder.invoke(context)
}
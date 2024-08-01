package me.honkling.javahelper.manager

import me.honkling.javahelper.kord
import me.honkling.javahelper.logger
import me.honkling.javahelper.manager.annotation.Event

fun registerEvents(pkg: String) {
    val classes = getClassesInPackage(pkg) { getMethodIfPresent<Event>(it) != null }

    for (clazz in classes)
        registerEvent(clazz)
}

private fun registerEvent(clazz: Class<*>) {
    val method = getMethodIfPresent<Event>(clazz)
        ?: return logger.warn("Found no register method for class ${clazz.simpleName}")

    method.invoke(null, kord)
    logger.info("Registered events in ${clazz.simpleName}")
}


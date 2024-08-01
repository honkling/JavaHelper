package me.honkling.javahelper.manager

import dev.kord.core.Kord
import me.honkling.javahelper.config.ConfigToml
import java.io.File
import java.lang.reflect.Method
import java.util.function.BiConsumer
import java.util.jar.JarEntry
import java.util.jar.JarInputStream

fun scanJar(predicate: (String) -> Boolean, callback: BiConsumer<JarEntry, String>) {
    val path = ConfigToml::class.java.protectionDomain.codeSource.location.toURI()
    val jar = File(path)
    val stream = JarInputStream(jar.inputStream())

    while (true) {
        val entry = stream.nextJarEntry ?: break
        val entryName = entry.name

        if (!predicate.invoke(entryName))
            continue

        callback.accept(entry, entryName)
    }
}

fun getClassesInPackage(pkg: String, predicate: (Class<*>) -> Boolean = { true }): List<Class<*>> {
    val classes = mutableListOf<Class<*>>()
    val directory = pkg.replace('.', '/')

    scanJar({ n -> n.startsWith(directory) && n.endsWith(".class") && "$" !in n }) { _, entryName ->
        val clazz = ConfigToml::class.java.classLoader.loadClass(entryName
            .replace(".class", "")
            .replace('/', '.'))

        if (!predicate.invoke(clazz))
            return@scanJar

        classes.add(clazz)
    }

    return classes
}

inline fun <reified T : Annotation> getMethodIfPresent(clazz: Class<*>): Method? {
    val methods = clazz.declaredMethods

    for (method in methods) {
        val firstType = method.parameterTypes.firstOrNull()

        if (!method.isAnnotationPresent(T::class.java) || firstType != Kord::class.java)
            continue

        method.isAccessible = true
        return method
    }

    return null
}

inline fun <reified T : Annotation> getMethodsWithAnnotation(clazz: Class<*>): List<Pair<Method, T>> {
    return clazz.declaredMethods
        .filter { it.isAnnotationPresent(T::class.java) }
        .map { it to it.getAnnotation(T::class.java) }
}
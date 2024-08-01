package me.honkling.javahelper.manager.annotation

@Target(AnnotationTarget.FUNCTION)
annotation class Command(val type: Type) {
    enum class Type {
        REGISTRY,
        EXECUTION,
        AUTOCOMPLETE
    }
}

package site.addzero.kcp.transformoverload.annotations

import java.io.File
import java.lang.reflect.Method
import java.lang.reflect.Modifier

fun <Source, Target> transformOverloadLiftIterable(
    source: Iterable<Source>,
    owner: Any?,
    ownerClassName: String,
    methodName: String,
): Iterable<Target> {
    return source.map { element -> invokeTransform<Target>(owner, ownerClassName, methodName, element) }
}

fun <Source, Target> transformOverloadLiftCollection(
    source: Collection<Source>,
    owner: Any?,
    ownerClassName: String,
    methodName: String,
): Collection<Target> {
    return source.map { element -> invokeTransform<Target>(owner, ownerClassName, methodName, element) }
}

fun <Source, Target> transformOverloadLiftList(
    source: List<Source>,
    owner: Any?,
    ownerClassName: String,
    methodName: String,
): List<Target> {
    return source.map { element -> invokeTransform<Target>(owner, ownerClassName, methodName, element) }
}

fun <Source, Target> transformOverloadLiftSet(
    source: Set<Source>,
    owner: Any?,
    ownerClassName: String,
    methodName: String,
): Set<Target> {
    return source.map { element -> invokeTransform<Target>(owner, ownerClassName, methodName, element) }.toSet()
}

fun <Source, Target> transformOverloadLiftSequence(
    source: Sequence<Source>,
    owner: Any?,
    ownerClassName: String,
    methodName: String,
): Sequence<Target> {
    return source.map { element -> invokeTransform<Target>(owner, ownerClassName, methodName, element) }
}

@Suppress("UNCHECKED_CAST")
private fun <Target> invokeTransform(
    owner: Any?,
    ownerClassName: String,
    methodName: String,
    element: Any?,
): Target {
    val ownerClass = Class.forName(ownerClassName)
    val method = resolveTransformMethod(ownerClass, owner != null, methodName)
    val receiver = if (Modifier.isStatic(method.modifiers)) {
        null
    } else {
        owner ?: error("Missing bound receiver for $ownerClassName#$methodName")
    }
    return method.invoke(receiver, element) as Target
}

private fun resolveTransformMethod(
    ownerClass: Class<*>,
    hasBoundReceiver: Boolean,
    methodName: String,
): Method {
    return ownerClass.methods.firstOrNull { method ->
        method.name == methodName &&
            method.parameterCount == 1 &&
            Modifier.isStatic(method.modifiers) == !hasBoundReceiver
    } ?: error("Unable to resolve transform method ${ownerClass.name}#$methodName")
}

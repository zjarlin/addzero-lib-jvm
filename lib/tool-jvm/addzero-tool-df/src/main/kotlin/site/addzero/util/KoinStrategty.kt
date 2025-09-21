package site.addzero.util

import org.koin.core.annotation.Single
import org.koin.java.KoinJavaComponent.inject

 fun <T> injectList(): List<T> {
    @Single
    class TempContainer(val strategys: List<T>)
    val temp by inject<TempContainer>(TempContainer::class.java)
    return temp.strategys
}

fun <T> getSupportStrategty(predicate: (T) -> Boolean): T? {
    val listT = injectList<T>()
    val firstOrNull = listT.firstOrNull { predicate(it) }
    return firstOrNull
}

inline fun <reified T> inject(): T {
    val t by inject<T>(T::class.java)
    return t
}


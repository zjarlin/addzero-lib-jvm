package site.addzero.example

import site.addzero.kcp.transformoverload.annotations.GenerateTransformOverloads
import java.util.concurrent.CompletableFuture

@GenerateTransformOverloads
interface KSaveCommandCreator {
    fun <E : Any> saveEntitiesCommand(
        entities: Iterable<E>,
    ): KBatchEntitySaveCommand<E>
}

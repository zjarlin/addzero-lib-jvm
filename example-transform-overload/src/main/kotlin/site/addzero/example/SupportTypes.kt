package site.addzero.example

data class KBatchEntitySaveCommand<E : Any>(
    val entities: List<E>,
)

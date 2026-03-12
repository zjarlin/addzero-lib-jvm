package site.addzero.example

import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

class TransformOverloadTest1 {

    @Test
    fun `generates iterable input overload for saveEntitiesCommand`() {
        val generated = KSaveCommandCreator::class.java.methods
            .firstOrNull { method ->
                method.name == "saveEntitiesCommandViaToEntityInput" &&
                    method.parameterTypes.singleOrNull() == Iterable::class.java
            }

        assertNotNull(generated)
    }
}
